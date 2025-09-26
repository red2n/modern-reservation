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

# Function to start zipkin server using standalone jar
start_zipkin_service() {
    local service_name="zipkin-server"
    local port=9411
    local wait_time=10

    print_status "Starting $service_name..."

    # Check if port is already in use
    if ! check_port $port; then
        print_warning "Port $port is already in use. Checking if it's our service..."
        if wait_for_service "$service_name" $port; then
            print_success "$service_name is already running on port $port"
            return 0
        else
            print_error "Port $port is occupied by another process"
            return 1
        fi
    fi

    # Change to zipkin-server directory
    cd "$INFRA_DIR/zipkin-server"

    # Create logs directory if it doesn't exist
    mkdir -p logs

    # Start zipkin server using standalone jar
    print_status "Executing: java -jar zipkin.jar for $service_name"
    nohup java -jar zipkin.jar --server.port=$port > "logs/${service_name}.log" 2>&1 &
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

# Function to start a service
start_service() {
    local service_name=$1
    local service_dir=$2
    local port=$3
    local wait_time=${4:-10}

    print_status "Starting $service_name..."

    # Check if port is already in use
    if ! check_port $port; then
        print_warning "Port $port is already in use. Checking if it's our service..."
        if wait_for_service "$service_name" $port; then
            print_success "$service_name is already running on port $port"
            return 0
        else
            print_error "Port $port is occupied by another process"
            return 1
        fi
    fi

    # Change to service directory
    cd "$INFRA_DIR/$service_dir"

    # Create logs directory if it doesn't exist
    mkdir -p logs

    # Start the service in background
    print_status "Executing: mvn spring-boot:run for $service_name"
    nohup mvn spring-boot:run > "logs/${service_name}.log" 2>&1 &
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

    # Clean up any existing PID files
    print_status "Cleaning up old PID files..."
    cd "$BASE_DIR"
    rm -f *.pid

    # Service startup order (dependencies first)
    declare -a SERVICES=(
        "config-server:config-server:8888:15"
        "eureka-server:eureka-server:8761:20"
        "zipkin-server:zipkin-server:9411:10"
        "gateway-service:gateway-service:8080:15"
    )

    local success_count=0
    local total_services=${#SERVICES[@]}

    for service_config in "${SERVICES[@]}"; do
        IFS=':' read -r service_name service_dir port wait_time <<< "$service_config"

        print_status "========================================="
        print_status "Starting service: $service_name"
        print_status "Directory: $service_dir"
        print_status "Port: $port"
        print_status "========================================="

        # Handle zipkin-server specially (uses standalone jar)
        if [ "$service_name" = "zipkin-server" ]; then
            if start_zipkin_service; then
                success_count=$((success_count + 1))
                print_success "✅ $service_name started successfully"
            else
                print_error "❌ Failed to start $service_name"
                print_warning "Check logs at: $INFRA_DIR/$service_dir/logs/${service_name}.log"
            fi
        else
            if start_service "$service_name" "$service_dir" "$port" "$wait_time"; then
                success_count=$((success_count + 1))
                print_success "✅ $service_name started successfully"
            else
                print_error "❌ Failed to start $service_name"
                print_warning "Check logs at: $INFRA_DIR/$service_dir/logs/${service_name}.log"
            fi
        fi

        echo ""
        sleep 2
    done

    # Final status report
    print_status "========================================="
    print_status "INFRASTRUCTURE STARTUP SUMMARY"
    print_status "========================================="
    print_success "Successfully started: $success_count/$total_services services"

    if [ $success_count -eq $total_services ]; then
        print_success "🎉 All infrastructure services are running!"
        print_status "Service URLs:"
        print_status "  • Config Server:  http://localhost:8888"
        print_status "  • Eureka Server:  http://localhost:8761"
        print_status "  • Zipkin Server:  http://localhost:9411"
        print_status "  • Gateway Service: http://localhost:8080"
    else
        print_warning "⚠️  Some services failed to start. Check the logs for details."
    fi

    print_status "========================================="
    print_status "To stop all services, run: scripts/stop-infrastructure.sh"
    print_status "To check service status, run: scripts/check-infrastructure.sh"
}

# Run main function
main "$@"
