#!/bin/bash

# Modern Reservation System - Infrastructure Services Stop Script
# This script stops all infrastructure services gracefully

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

# Function to stop a service by PID file
stop_service() {
    local service_name=$1
    local pid_file="$BASE_DIR/${service_name}.pid"

    if [ ! -f "$pid_file" ]; then
        print_warning "PID file for $service_name not found"
        return 1
    fi

    local pid=$(cat "$pid_file")

    if [ -z "$pid" ]; then
        print_warning "Empty PID file for $service_name"
        rm -f "$pid_file"
        return 1
    fi

    # Check if process is running
    if ! ps -p "$pid" > /dev/null 2>&1; then
        print_warning "$service_name (PID: $pid) is not running"
        rm -f "$pid_file"
        return 1
    fi

    print_status "Stopping $service_name (PID: $pid)..."

    # Try graceful shutdown first
    kill -TERM "$pid" 2>/dev/null

    # Wait for graceful shutdown
    local count=0
    while [ $count -lt 30 ] && ps -p "$pid" > /dev/null 2>&1; do
        sleep 1
        count=$((count + 1))
    done

    # Force kill if still running
    if ps -p "$pid" > /dev/null 2>&1; then
        print_warning "$service_name did not stop gracefully, forcing shutdown..."
        kill -KILL "$pid" 2>/dev/null
        sleep 2
    fi

    # Final check
    if ps -p "$pid" > /dev/null 2>&1; then
        print_error "Failed to stop $service_name (PID: $pid)"
        return 1
    else
        print_success "$service_name stopped successfully"
        rm -f "$pid_file"
        return 0
    fi
}

# Function to stop Docker Zipkin service
stop_zipkin_docker() {
    local service_name="zipkin-server"

    print_status "Stopping $service_name Docker container..."

    # Check if container exists and is running
    if docker ps --filter "name=modern-reservation-zipkin" --format "{{.Names}}" | grep -q "modern-reservation-zipkin"; then
        docker stop modern-reservation-zipkin > /dev/null 2>&1
        docker rm modern-reservation-zipkin > /dev/null 2>&1

        # Wait a moment and verify it's stopped
        sleep 2
        if ! docker ps --filter "name=modern-reservation-zipkin" --format "{{.Names}}" | grep -q "modern-reservation-zipkin"; then
            print_success "$service_name Docker container stopped successfully"
            return 0
        else
            print_error "Failed to stop $service_name Docker container"
            return 1
        fi
    else
        print_warning "$service_name Docker container is not running"
        return 0
    fi
}

# Main execution
main() {
    print_status "Stopping Modern Reservation Infrastructure Services"

    cd "$BASE_DIR"

    # Services to stop (in reverse order of startup)
    declare -a SERVICES=(
        "gateway-service"
        "zipkin-server"
        "eureka-server"
        "config-server"
    )

    local stopped_count=0
    local total_found=0

    for service_name in "${SERVICES[@]}"; do
        # Handle zipkin-server specially (Docker container)
        if [ "$service_name" = "zipkin-server" ]; then
            total_found=$((total_found + 1))
            if stop_zipkin_docker; then
                stopped_count=$((stopped_count + 1))
            fi
        elif [ -f "${service_name}.pid" ]; then
            total_found=$((total_found + 1))
            if stop_service "$service_name"; then
                stopped_count=$((stopped_count + 1))
            fi
        else
            print_warning "No PID file found for $service_name"
        fi
        sleep 1
    done

    # Clean up any remaining PID files
    print_status "Cleaning up remaining PID files..."
    rm -f *.pid

    # Final status report
    print_status "========================================="
    print_status "INFRASTRUCTURE SHUTDOWN SUMMARY"
    print_status "========================================="

    if [ $total_found -eq 0 ]; then
        print_warning "No running services found"
    else
        print_success "Successfully stopped: $stopped_count/$total_found services"

        if [ $stopped_count -eq $total_found ]; then
            print_success "🎉 All infrastructure services stopped successfully!"
        else
            print_warning "⚠️  Some services may not have stopped properly"
        fi
    fi

    print_status "========================================="
}

# Run main function
main "$@"
