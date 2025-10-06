#!/bin/bash

# Modern Reservation System - Infrastructure Services Startup Script
# This script starts all infrastructure services one by one in the correct order

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Parse command-line arguments
RESTART_MODE=false
if [ "$1" = "--restart" ]; then
    RESTART_MODE=true
fi

# Base directory - dynamically detect script location (go up one level from scripts folder)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
INFRA_DIR="$BASE_DIR/apps/backend/java-services/infrastructure"

# Function to print colored output
print_status() {
    echo -e "${BLUE}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1"
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

# Function to print table header for startup summary
print_summary_table_header() {
    printf "${BLUE}┌─────────────────────┬────────────┬──────────────────────────────────────────────────────┐${NC}\n"
    printf "${BLUE}│ %-19s │ %-10s │ %-52s │${NC}\n" "Service" "Status" "URL"
    printf "${BLUE}├─────────────────────┼────────────┼──────────────────────────────────────────────────────┤${NC}\n"
}

# Function to print summary table row
print_summary_table_row() {
    local service=$1
    local status=$2
    local url=$3

    # Determine color based on status
    local color=$NC
    case $status in
        "STARTED") color=$GREEN ;;
        "FAILED") color=$RED ;;
        "SKIPPED") color=$YELLOW ;;
    esac

    printf "${color}│ %-19s │ %-10s │ %-52s │${NC}\n" "$service" "$status" "$url"
}

# Function to print summary table footer
print_summary_table_footer() {
    printf "${BLUE}└─────────────────────┴────────────┴──────────────────────────────────────────────────────┘${NC}\n"
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

# Function to wait for service to be ready
wait_for_service() {
    local service_name=$1
    local port=$2
    local max_attempts=60
    local attempt=1

    print_status "Waiting for $service_name to be ready on port $port..."

    while [ $attempt -le $max_attempts ]; do
        if curl -s -f http://localhost:$port/actuator/health >/dev/null 2>&1 || \
           curl -s -f http://localhost:$port/health >/dev/null 2>&1 || \
           nc -z localhost $port >/dev/null 2>&1; then
            print_success "$service_name is ready on port $port"
            return 0
        fi

        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done

    print_error "$service_name failed to start within $(($max_attempts * 2)) seconds"
    return 1
}

# Function to start infrastructure services via Docker Compose
start_docker_infrastructure() {
    local docker_compose_file="$BASE_DIR/infrastructure/docker/docker-compose-infrastructure.yml"

    print_status "Starting Docker infrastructure services (Zipkin, PostgreSQL, Redis, Consul, pgAdmin)..."

    if [ ! -f "$docker_compose_file" ]; then
        print_error "Docker Compose file not found: $docker_compose_file"
        return 1
    fi

    # Check if services are already running (only in smart start mode)
    if [ "$RESTART_MODE" = false ]; then
        if docker ps --filter "name=modern-reservation-zipkin" --format "{{.Names}}" | grep -q "modern-reservation-zipkin"; then
            print_success "Docker infrastructure services are already running"
            return 0
        fi
    fi

    # Clean up any existing containers (always in restart mode, only if needed in smart mode)
    if [ "$RESTART_MODE" = true ]; then
        print_status "Restart mode: Cleaning up existing Docker containers..."
    else
        print_status "Cleaning up any existing Docker containers..."
    fi
    docker compose -f "$docker_compose_file" down --remove-orphans 2>/dev/null || true
    # Also remove any manually created containers with our naming pattern
    docker rm -f modern-reservation-zipkin modern-reservation-postgres modern-reservation-redis modern-reservation-consul modern-reservation-pgadmin 2>/dev/null || true

    # Start infrastructure services via Docker Compose
    print_status "Executing: docker compose up -d for infrastructure services"
    if docker compose -f "$docker_compose_file" up -d; then
        print_success "Docker infrastructure services started successfully"

        # Wait for Zipkin to be ready
        if wait_for_service "zipkin-server" 9411; then
            print_success "Zipkin is ready on port 9411"
            return 0
        else
            print_warning "Zipkin may still be starting up"
            return 0  # Don't fail the whole process
        fi
    else
        print_error "Failed to start Docker infrastructure services"
        return 1
    fi
}

# Function to build parent Maven project
build_parent_project() {
    local java_services_dir="$BASE_DIR/apps/backend/java-services"

    print_status "Building parent Maven project..."

    # Change to Java services directory
    cd "$java_services_dir"

    # Create centralized logs directory if it doesn't exist
    mkdir -p logs/infrastructure

    # Build parent project
    if mvn clean install -DskipTests > "logs/parent-build.log" 2>&1; then
        print_success "Parent Maven project built successfully"
        return 0
    else
        print_error "Failed to build parent Maven project. Check $java_services_dir/logs/parent-build.log for details"
        return 1
    fi
}

# Function to start a service
start_service() {
    local service_name=$1
    local service_dir=$2
    local port=$3
    local wait_time=${4:-10}

    print_status "Starting $service_name..."

    # In restart mode, we skip health checks and force restart
    if [ "$RESTART_MODE" = false ]; then
        # Smart start mode - check if service is already running and healthy
        local pid_file="$BASE_DIR/${service_name}.pid"
        if [ -f "$pid_file" ]; then
            local pid=$(cat "$pid_file")
            if [ ! -z "$pid" ] && ps -p "$pid" > /dev/null 2>&1; then
                # Process is running, check if it's healthy
                if curl -s -f "http://localhost:$port/actuator/health" >/dev/null 2>&1; then
                    print_success "$service_name is already running and healthy on port $port"
                    return 0
                fi
            fi
        fi

        # Check if port is already in use by another process
        if ! check_port $port; then
            print_warning "Port $port is already in use. Checking if it's our service..."
            if curl -s -f "http://localhost:$port/actuator/health" >/dev/null 2>&1; then
                print_success "$service_name is already running on port $port"
                return 0
            else
                print_error "Port $port is occupied by another process"
                return 1
            fi
        fi
    else
        # Restart mode - kill any existing process first
        print_status "Restart mode: stopping any existing $service_name process..."
        local pid_file="$BASE_DIR/${service_name}.pid"
        if [ -f "$pid_file" ]; then
            local pid=$(cat "$pid_file")
            if [ ! -z "$pid" ] && ps -p "$pid" > /dev/null 2>&1; then
                kill $pid && sleep 2
                if ps -p "$pid" > /dev/null 2>&1; then
                    kill -9 $pid
                fi
                print_status "Stopped existing $service_name process (PID: $pid)"
            fi
            rm -f "$pid_file"
        fi
    fi

    # Change to service directory
    cd "$INFRA_DIR/$service_dir"

    # Create centralized logs directory if it doesn't exist
    local java_services_dir="$BASE_DIR/apps/backend/java-services"
    mkdir -p "$java_services_dir/logs/infrastructure"

    # Ensure service is compiled (quick check)
    print_status "Ensuring $service_name is compiled..."
    if mvn compile -q > "$java_services_dir/logs/infrastructure/${service_name}-compile.log" 2>&1; then
        print_success "$service_name compilation verified"
    else
        print_error "Failed to compile $service_name. Check $java_services_dir/logs/infrastructure/${service_name}-compile.log for details"
        return 1
    fi

    # Start the service in background
    print_status "Executing: mvn spring-boot:run for $service_name"
    nohup mvn spring-boot:run > "$java_services_dir/logs/infrastructure/${service_name}.log" 2>&1 &
    local pid=$!

    # Save PID
    echo $pid > "$BASE_DIR/${service_name}.pid"
    print_status "$service_name started with PID $pid"

    # Wait a bit for the service to initialize
    sleep $wait_time

    # Wait for service to be ready
    if wait_for_service "$service_name" $port; then
        print_success "$service_name is running successfully on port $port"
        return 0
    else
        print_error "Failed to start $service_name"
        return 1
    fi
}

# Function to run infrastructure health check
run_infrastructure_check() {
    local check_script="$SCRIPT_DIR/check-infrastructure.sh"

    if [ -f "$check_script" ]; then
        print_status "Running infrastructure health check first..."
        if bash "$check_script" >/dev/null 2>&1; then
            print_success "Infrastructure health check passed - some services may already be running"
            return 0
        else
            print_status "Infrastructure health check indicates services need to be started"
            return 1
        fi
    else
        print_warning "Infrastructure check script not found: $check_script"
        return 1
    fi
}

# Function to cleanup on exit
cleanup() {
    print_warning "Script interrupted. Cleaning up..."
    exit 1
}

# Set trap for cleanup
trap cleanup INT TERM

# Main execution
main() {
    print_status "Starting Modern Reservation Infrastructure Services"
    print_status "Base directory: $BASE_DIR"
    print_status "Infrastructure directory: $INFRA_DIR"

    # Handle restart mode vs smart start mode
    if [ "$RESTART_MODE" = true ]; then
        print_status "Restart mode enabled - stopping all services first..."
        "$SCRIPT_DIR/stop-infrastructure.sh" || true
        print_status "Cleaning up all PID files for fresh restart..."
        cd "$BASE_DIR"
        rm -f *.pid
    else
        # Smart start mode - only check health, don't clean up PID files
        print_status "Smart start mode - checking service health first..."
        run_infrastructure_check
    fi

    # Build parent Maven project first
    if ! build_parent_project; then
        print_error "❌ Failed to build parent Maven project. Cannot proceed with service startup."
        exit 1
    fi

    # Start Docker infrastructure services first
    if ! start_docker_infrastructure; then
        print_error "❌ Failed to start Docker infrastructure services. Cannot proceed."
        exit 1
    fi

    # Java service startup order (dependencies first)
    declare -a SERVICES=(
        "config-server:config-server:8888:15"
        "eureka-server:eureka-server:8761:20"
        "gateway-service:gateway-service:8080:15"
    )

    local success_count=1  # Docker services already started
    local total_services=$((${#SERVICES[@]} + 1))  # +1 for Docker services

    for service_config in "${SERVICES[@]}"; do
        IFS=':' read -r service_name service_dir port wait_time <<< "$service_config"

        print_status "========================================="
        print_status "Starting service: $service_name"
        print_status "Directory: $service_dir"
        print_status "Port: $port"
        print_status "========================================="

        if start_service "$service_name" "$service_dir" "$port" "$wait_time"; then
            success_count=$((success_count + 1))
            print_success "✅ $service_name started successfully"
        else
            print_error "❌ Failed to start $service_name"
            print_warning "Check logs at: $BASE_DIR/apps/backend/java-services/logs/infrastructure/${service_name}.log"
            print_error "❌ Infrastructure startup failed. Fix $service_name before continuing."
            exit 1
        fi

        echo ""
        sleep 2
    done

    # Final status report in table format
    echo ""
    if [ $success_count -eq $total_services ]; then
        echo -e "${GREEN}🎉 INFRASTRUCTURE STARTUP COMPLETE${NC}"
    else
        echo -e "${YELLOW}⚠️  INFRASTRUCTURE STARTUP PARTIAL${NC}"
    fi
    echo ""

    print_summary_table_header

    # Display each service status
    declare -A SERVICE_URLS=(
        ["config-server"]="http://localhost:8888"
        ["eureka-server"]="http://localhost:8761"
        ["zipkin-server"]="http://localhost:9411 (Docker)"
        ["gateway-service"]="http://localhost:8080"
    )

    for service in config-server eureka-server zipkin-server gateway-service; do
        local url="${SERVICE_URLS[$service]}"

        # Check if service was started successfully
        local status="FAILED"
        if [ "$service" = "zipkin-server" ]; then
            # Special handling for Docker Zipkin
            if docker ps --filter "name=modern-reservation-zipkin" --format "{{.Names}}" | grep -q "modern-reservation-zipkin"; then
                status="STARTED"
            fi
        else
            # Check if service is actually accessible (more reliable than PID files)
            local port
            case $service in
                "config-server") port=8888 ;;
                "eureka-server") port=8761 ;;
                "gateway-service") port=8080 ;;
            esac

            if curl -s -f "http://localhost:$port/actuator/health" >/dev/null 2>&1; then
                status="STARTED"
            elif lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
                status="STARTED"  # Port is occupied, assume service is running
            fi
        fi

        print_summary_table_row "$service" "$status" "$url"
    done

    print_summary_table_footer

    echo ""
    printf "${GREEN}✅ Services started: %d/%d${NC}\n" $success_count $total_services
    echo ""
    echo -e "${BLUE}💡 NEXT STEPS${NC}"
    printf "   Stop: ${RED}./scripts/stop-infrastructure.sh${NC}  |  Check: ${BLUE}./scripts/check-infrastructure.sh${NC}  |  Business: ${GREEN}./scripts/start-business-services.sh${NC}\n"
}

# Run main function
main "$@"
