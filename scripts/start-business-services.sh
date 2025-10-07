#!/bin/bash

# Modern Reservation System - Business Services Startup Script
# This script starts all implemented business services after infrastructure is ready

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\03    # Format: "service-name:directory:port:startup-wait:context-path"
    declare -a BUSINESS_SERVICES=(
        "reservation-engine:reservation-engine:8081:45:/reservation-engine"
        "availability-calculator:availability-calculator:8083:40:/availability-calculator"
        "payment-processor:payment-processor:8084:35:/payment-processor"
        "rate-management:rate-management:8087:35:/rate-management"
        "analytics-engine:analytics-engine:8086:45:/analytics-engine"
    )No Color

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
BUSINESS_SERVICES_DIR="$BASE_DIR/apps/backend/java-services/business-services"

# Print functions
print_status() {
    echo -e "${BLUE}[$(date '+%Y-%m-%d %H:%M:%S')] $1${NC}"
}

print_success() {
    echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')] ✅ $1${NC}"
}

print_error() {
    echo -e "${RED}[$(date '+%Y-%m-%d %H:%M:%S')] ❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')] ⚠️  $1${NC}"
}

# Function to check if a port is available
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        return 1  # Port is occupied
    else
        return 0  # Port is free
    fi
}

# Function to wait for a service to be ready
wait_for_service() {
    local service_name=$1
    local port=$2
    local timeout=${3:-60}
    local health_endpoint=${4:-"/actuator/health"}

    print_status "Waiting for $service_name to be ready on port $port..."

    local count=0
    while [ $count -lt $timeout ]; do
        if curl -s "http://localhost:$port$health_endpoint" >/dev/null 2>&1; then
            print_success "$service_name is ready!"
            return 0
        fi
        sleep 2
        count=$((count + 2))
        echo -n "."
    done

    echo ""
    print_error "$service_name failed to start within $timeout seconds"
    return 1
}

# Function to run infrastructure health check
run_infrastructure_check() {
    local check_script="$SCRIPT_DIR/check-infrastructure.sh"

    print_status "Checking infrastructure prerequisites..."

    if [ -f "$check_script" ]; then
        if bash "$check_script" >/dev/null 2>&1; then
            print_success "Infrastructure services are running and healthy"
            return 0
        else
            print_error "Infrastructure services are not ready"
            print_error "Please start infrastructure services first: ./scripts/start-infrastructure.sh"
            return 1
        fi
    else
        print_warning "Infrastructure check script not found: $check_script"
        # Fallback to basic port checking
        local required_ports=("8888" "8761" "8080")  # Config, Eureka, Gateway
        local required_services=("config-server" "eureka-server" "gateway-service")

        for i in "${!required_ports[@]}"; do
            local port=${required_ports[$i]}
            local service=${required_services[$i]}

            if check_port $port; then
                print_error "$service is not running on port $port"
                print_error "Please start infrastructure services first: ./scripts/start-infrastructure.sh"
                return 1
            else
                print_success "$service is running on port $port"
            fi
        done
        return 0
    fi
}

# Function to run business services health check
run_business_services_check() {
    local check_script="$SCRIPT_DIR/check-business-services.sh"

    if [ -f "$check_script" ]; then
        print_status "Running business services health check first..."
        if bash "$check_script" >/dev/null 2>&1; then
            print_success "Business services health check passed - some services may already be running"
            return 0
        else
            print_status "Business services health check indicates services need to be started"
            return 1
        fi
    else
        print_warning "Business services check script not found: $check_script"
        return 1
    fi
}

# Function to wait for service registration with Eureka (Service Discovery)
wait_for_eureka_registration() {
    local service_name=$1
    local max_attempts=${2:-30}  # Increased timeout to 30 attempts (90 seconds)
    local attempt=1

    print_status "Waiting for $service_name to register with Eureka Discovery Server..."

    while [ $attempt -le $max_attempts ]; do
        # Try multiple approaches to check registration
        if curl -s "http://localhost:8761/eureka/apps" | grep -i "$service_name" > /dev/null 2>&1; then
            print_success "$service_name registered with Eureka! 🎯"
            return 0
        fi

        # Alternative check: Look for service in Eureka instances
        if curl -s "http://localhost:8761/eureka/apps/${service_name^^}" | grep -i "instance" > /dev/null 2>&1; then
            print_success "$service_name registered with Eureka! 🎯"
            return 0
        fi

        echo -n "."
        sleep 3
        ((attempt++))
    done

    echo ""
    print_warning "$service_name not registered with Eureka within $((max_attempts * 3)) seconds"
    print_status "Service may still be starting up. Check Eureka dashboard: http://localhost:8761"
    return 1
}

# Function to build parent Maven project
build_parent_project() {
    print_status "Building parent Maven project..."
    cd "$BASE_DIR/apps/backend/java-services"

    # Create centralized logs directory
    mkdir -p logs/business-services

    # Build the parent project (compile all modules)
    if mvn clean compile -q > logs/parent-build.log 2>&1; then
        print_success "Parent Maven project built successfully"
        return 0
    else
        print_error "Failed to build parent Maven project. Check $BASE_DIR/apps/backend/java-services/logs/parent-build.log for details"
        print_error "❌ Failed to build parent Maven project. Cannot proceed with service startup."
        return 1
    fi
}

# Function to start a business service
start_business_service() {
    local service_name=$1
    local service_dir=$2
    local port=$3
    local startup_wait=${4:-30}
    local context_path=${5:-""}

    print_status "========================================="
    print_status "Starting service: $service_name"
    print_status "Directory: $service_dir"
    print_status "Port: $port"
    print_status "========================================="

    # First, check if service is already running and healthy
    local pid_file="$BASE_DIR/${service_name}.pid"
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if [ ! -z "$pid" ] && ps -p "$pid" > /dev/null 2>&1; then
            # Process is running, check if it's healthy
            if curl -s -f "http://localhost:$port$context_path/actuator/health" >/dev/null 2>&1; then
                print_success "$service_name is already running and healthy on port $port"
                return 0
            fi
        fi
    fi

    # Check if port is already occupied by another service
    if ! check_port $port; then
        print_warning "$service_name localhost:$port is already in use"

        # Check if it's our service and healthy
        if curl -s -f "http://localhost:$port$context_path/actuator/health" >/dev/null 2>&1; then
            print_success "$service_name is already running and healthy on port $port"
            return 0
        else
            local existing_pid=$(lsof -ti :$port)
            if [ ! -z "$existing_pid" ]; then
                print_warning "Process $existing_pid is using localhost:$port but service is not healthy"
            fi
            return 1
        fi
    fi

    # Navigate to service directory
    cd "$BUSINESS_SERVICES_DIR/$service_dir"

    # Create centralized logs directory if it doesn't exist
    local java_services_dir="$BASE_DIR/apps/backend/java-services"
    mkdir -p "$java_services_dir/logs/business-services"

    # Ensure service is compiled
    print_status "Ensuring $service_name is compiled..."
    if mvn compile -q > "$java_services_dir/logs/business-services/${service_name}-compile.log" 2>&1; then
        print_success "$service_name compilation verified"
    else
        print_error "Failed to compile $service_name. Check $java_services_dir/logs/business-services/${service_name}-compile.log for details"
        return 1
    fi

    print_status "Starting $service_name..."
    print_status "Executing: mvn spring-boot:run for $service_name"

    # Start the service in background
    nohup mvn spring-boot:run > "$java_services_dir/logs/business-services/${service_name}.log" 2>&1 &
    local pid=$!

    # Save PID to file
    echo $pid > "$BASE_DIR/${service_name}.pid"
    print_success "$service_name started with PID $pid"

    # Wait for service to be ready
    if wait_for_service "$service_name" "$port" "$startup_wait" "$context_path/actuator/health"; then
        print_success "$service_name is healthy and ready on localhost:$port (Network Isolated)"

        # Wait for service to register with Eureka (Service Discovery)
        wait_for_eureka_registration "$service_name" 10

        return 0
    else
        print_error "$service_name failed to start properly"
        return 1
    fi
}

# Main function
main() {
    print_status "Starting Modern Reservation Business Services"
    print_status "Base directory: $BASE_DIR"
    print_status "Business services directory: $BUSINESS_SERVICES_DIR"

    # Run business services health check first
    run_business_services_check || true  # Don't exit if health check fails - services may need to be started

    # Check infrastructure prerequisites
    if ! run_infrastructure_check; then
        exit 1
    fi

    # Build parent Maven project first
    if ! build_parent_project; then
        exit 1
    fi

    # Clean up any existing business service PID files
    print_status "Cleaning up old business service PID files..."
    cd "$BASE_DIR"
    rm -f reservation-engine.pid availability-calculator.pid payment-processor.pid rate-management.pid analytics-engine.pid

    # Business services startup order (dependencies first)
    # Format: "service-name:directory:port:startup-wait:context-path"
    declare -a BUSINESS_SERVICES=(
        "reservation-engine:reservation-engine:8081:45:/reservation-engine"
        "availability-calculator:availability-calculator:8083:40:/availability-calculator"
        "payment-processor:payment-processor:8084:35:/payment-processor"
        "rate-management:rate-management:8085:35:/rate-management"
analytics-engine:analytics-engine:8086:45:/analytics-engine
    )

    local success_count=0
    local total_services=${#BUSINESS_SERVICES[@]}

    print_status "Starting $total_services business services..."

    for service_config in "${BUSINESS_SERVICES[@]}"; do
        IFS=':' read -r service_name service_dir port startup_wait context_path <<< "$service_config"

        if start_business_service "$service_name" "$service_dir" "$port" "$startup_wait" "$context_path"; then
            success_count=$((success_count + 1))
        else
            print_error "Failed to start $service_name"
        fi

        # Brief pause between service starts
        sleep 5
    done

    print_status "========================================="
    print_status "BUSINESS SERVICES STARTUP SUMMARY"
    print_status "========================================="

    if [ $success_count -eq $total_services ]; then
        print_success "All $total_services business services started successfully! 🎉"
        print_status ""
        print_status "Service URLs:"
        print_status "  • Reservation Engine:    http://localhost:8081/reservation-engine"
        print_status "  • Availability Calculator: http://localhost:8083/availability-calculator"
        print_status "  • Payment Processor:     http://localhost:8084/payment-processor"
        print_status "  • Rate Management:       http://localhost:8087/rate-management"
        print_status "  • Analytics Engine:      http://localhost:8086/analytics-engine"
        print_status ""
        print_status "Health Check URLs:"
        print_status "  • All services:          /actuator/health"
        print_status "  • Service registry:      http://localhost:8761 (Eureka)"
        print_status "  • API Gateway:           http://localhost:8080"
        print_status ""
        print_success "Business services are ready to accept requests!"
    else
        print_error "Only $success_count out of $total_services business services started successfully"
        print_warning "Check individual service logs in $BASE_DIR/apps/backend/java-services/logs/business-services/"
        exit 1
    fi

    print_status "========================================="
}

# Trap to handle script interruption
cleanup() {
    print_warning "Script interrupted. Cleaning up..."
    exit 1
}

trap cleanup SIGINT SIGTERM

# Run main function
main "$@"
