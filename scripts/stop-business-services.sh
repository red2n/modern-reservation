#!/bin/bash

# Modern Reservation System - Business Services Shutdown Script
# This script gracefully stops all business services

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

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

# Get port for a service
get_service_port() {
    local service_name=$1
    case "$service_name" in
        "reservation-engine") echo "8081" ;;
        "availability-calculator") echo "8083" ;;
        "payment-processor") echo "8084" ;;
        "rate-management") echo "8085" ;;
        "analytics-engine") echo "8086" ;;
        *) echo "" ;;
    esac
}

# Function to stop a service by PID file
stop_service() {
    local service_name=$1
    local pid_file="$BASE_DIR/${service_name}.pid"

    if [ ! -f "$pid_file" ]; then
        print_warning "PID file for $service_name not found"
        # Check if service is running on its expected port
        local port=$(get_service_port "$service_name")
        if [ ! -z "$port" ] && lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
            print_warning "$service_name appears to be running on port $port but no PID file found"
            return 1
        else
            # Service not running, consider it successfully "stopped"
            return 0
        fi
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
        # If process is not running, consider it successfully "stopped"
        return 0
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

# Function to stop all services by port (fallback method)
stop_services_by_port() {
    local ports=("8081" "8083" "8084" "8085" "8086")
    local service_names=("reservation-engine" "availability-calculator" "payment-processor" "rate-management" "analytics-engine")

    print_status "Attempting to stop services by port (fallback method)..."

    for i in "${!ports[@]}"; do
        local port=${ports[$i]}
        local service_name=${service_names[$i]}

        local pid=$(lsof -ti :$port 2>/dev/null)
        if [ ! -z "$pid" ]; then
            print_status "Found $service_name running on port $port (PID: $pid)"
            print_status "Stopping $service_name..."

            # Try graceful shutdown
            kill -TERM "$pid" 2>/dev/null
            sleep 5

            # Check if still running
            if ps -p "$pid" > /dev/null 2>&1; then
                print_warning "Force killing $service_name (PID: $pid)..."
                kill -KILL "$pid" 2>/dev/null
            fi

            # Verify stopped
            if ! ps -p "$pid" > /dev/null 2>&1; then
                print_success "$service_name stopped successfully"
            else
                print_error "Failed to stop $service_name"
            fi
        fi
    done
}

# Main function
main() {
    print_status "Stopping Modern Reservation Business Services"

    # Business services to stop (reverse order of startup for proper dependency shutdown)
    declare -a SERVICES=(
        "analytics-engine"
        "rate-management"
        "payment-processor"
        "availability-calculator"
        "reservation-engine"
    )

    local stopped_count=0
    local total_services=${#SERVICES[@]}

    print_status "Attempting to stop $total_services business services..."

    # Try to stop services using PID files
    for service_name in "${SERVICES[@]}"; do
        if stop_service "$service_name"; then
            stopped_count=$((stopped_count + 1))
        fi
        sleep 1
    done

    # If some services couldn't be stopped by PID, try port-based approach
    if [ $stopped_count -lt $total_services ]; then
        print_warning "Some services couldn't be stopped by PID files, trying port-based approach..."
        stop_services_by_port
    fi

    # Clean up any remaining PID files
    print_status "Cleaning up remaining PID files..."
    cd "$BASE_DIR"
    rm -f reservation-engine.pid availability-calculator.pid payment-processor.pid rate-management.pid analytics-engine.pid

    print_status "========================================="
    print_status "BUSINESS SERVICES SHUTDOWN SUMMARY"
    print_status "========================================="

    # Final verification
    local running_services=0
    local ports=("8081" "8083" "8084" "8085" "8086")
    local service_names=("reservation-engine" "availability-calculator" "payment-processor" "rate-management" "analytics-engine")

    for i in "${!ports[@]}"; do
        local port=${ports[$i]}
        local service_name=${service_names[$i]}

        if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
            print_warning "$service_name is still running on localhost:$port"
            running_services=$((running_services + 1))
        else
            print_success "$service_name stopped (localhost:$port is free)"
        fi
    done

    if [ $running_services -eq 0 ]; then
        print_success "All business services stopped successfully! 🎉"
        print_status ""
        print_status "All localhost business service ports are now free:"
        print_status "  • localhost:8081 (reservation-engine) - Network Isolated"
        print_status "  • localhost:8083 (availability-calculator) - Network Isolated"
        print_status "  • localhost:8084 (payment-processor) - Network Isolated"
        print_status "  • localhost:8085 (rate-management) - Network Isolated"
        print_status "  • localhost:8086 (analytics-engine) - Network Isolated"
        print_status ""
        print_status "Note: Business services use network isolation (localhost binding)"
        print_status "External access available only through Gateway: http://localhost:8080"
    else
        print_warning "$running_services business services are still running"
        print_warning "You may need to manually stop them using 'kill -9 <pid>'"
    fi

    print_status "========================================="
}

# Trap to handle script interruption
cleanup() {
    print_warning "Script interrupted. Some services may still be running."
    exit 1
}

trap cleanup SIGINT SIGTERM

# Run main function
main "$@"
