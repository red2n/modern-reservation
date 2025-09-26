#!/bin/bash

# Modern Reservation System - Infrastructure Services Status Check Script
# This script checks the status of all infrastructure services

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Base directory - dynamically detect script location (go up one level from scripts folder)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

# Function to print colored output
print_status() {
    echo -e "${BLUE}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${CYAN}ℹ️  $1${NC}"
}

# Function to check service health
check_service_health() {
    local service_name=$1
    local port=$2
    local health_endpoint=$3

    # Try different health check endpoints
    local endpoints=("/actuator/health" "/health" "")

    for endpoint in "${endpoints[@]}"; do
        local url="http://localhost:${port}${endpoint}"
        if curl -s -f "$url" >/dev/null 2>&1; then
            local response=$(curl -s "$url" 2>/dev/null)
            if [[ "$response" == *"UP"* ]] || [[ "$response" == *"200"* ]] || [[ -n "$response" ]]; then
                return 0  # Healthy
            fi
        fi
    done

    # If no HTTP endpoint works, try basic port check
    if nc -z localhost "$port" >/dev/null 2>&1; then
        return 0  # Port is open, assume healthy
    fi

    return 1  # Not healthy
}

# Function to check Docker Zipkin service
check_zipkin_docker() {
    local service_name="zipkin-server"
    local port=9411

    print_status "Checking $service_name (Docker)..."

    # Check if Docker container is running
    if docker ps --filter "name=modern-reservation-zipkin" --format "{{.Names}}" | grep -q "modern-reservation-zipkin"; then
        # Check if service is responding
        if check_service_health "$service_name" "$port"; then
            print_success "✅ $service_name is healthy (Docker container)"
            return 0
        else
            print_warning "⚠️  $service_name Docker container is running but not responding on port $port"
            return 1
        fi
    else
        print_error "❌ $service_name Docker container is not running"
        return 1
    fi
}

# Function to check a single service
check_service() {
    local service_name=$1
    local port=$2
    local pid_file="$BASE_DIR/${service_name}.pid"

    echo -e "\n${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${CYAN} 🔍 Checking: $service_name${NC}"
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

    # Check PID file
    if [ ! -f "$pid_file" ]; then
        print_error "$service_name: PID file not found"
        print_info "Port $port: $(if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then echo "OCCUPIED (by other process)"; else echo "FREE"; fi)"
        return 1
    fi

    local pid=$(cat "$pid_file")

    if [ -z "$pid" ]; then
        print_error "$service_name: Empty PID file"
        rm -f "$pid_file"
        return 1
    fi

    # Check if process is running
    if ! ps -p "$pid" > /dev/null 2>&1; then
        print_error "$service_name: Process not running (PID: $pid)"
        rm -f "$pid_file"
        print_info "Port $port: $(if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then echo "OCCUPIED (by other process)"; else echo "FREE"; fi)"
        return 1
    fi

    # Get process info
    local process_info=$(ps -p "$pid" -o pid,ppid,cmd --no-headers)
    print_info "Process: $process_info"

    # Check port
    if ! lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        print_warning "$service_name: Process running but port $port not listening"
        return 1
    fi

    local port_pid=$(lsof -Pi :$port -sTCP:LISTEN -t 2>/dev/null)
    if [ "$port_pid" != "$pid" ]; then
        print_warning "$service_name: Port $port occupied by different process (PID: $port_pid)"
        return 1
    fi

    # Check service health
    if check_service_health "$service_name" "$port"; then
        print_success "$service_name: HEALTHY (PID: $pid, Port: $port)"

        # Try to get additional info
        local health_url="http://localhost:${port}/actuator/health"
        local info_url="http://localhost:${port}/actuator/info"

        if curl -s -f "$health_url" >/dev/null 2>&1; then
            local health_status=$(curl -s "$health_url" 2>/dev/null | grep -o '"status":"[^"]*' | cut -d'"' -f4)
            if [ -n "$health_status" ]; then
                print_info "Health Status: $health_status"
            fi
        fi

        return 0
    else
        print_warning "$service_name: Process and port OK, but health check failed"
        return 1
    fi
}

# Function to show service URLs
show_service_urls() {
    echo -e "\n${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${CYAN} 🌐 Service URLs${NC}"
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

    declare -A SERVICE_URLS=(
        ["config-server"]="http://localhost:8888"
        ["eureka-server"]="http://localhost:8761"
        ["zipkin-server"]="http://localhost:9411 (Docker)"
        ["gateway-service"]="http://localhost:8080"
    )

    for service in "${!SERVICE_URLS[@]}"; do
        local url="${SERVICE_URLS[$service]}"
        if check_service_health "$service" "$(echo $url | cut -d':' -f3)"; then
            print_success "$service: $url"
        else
            print_error "$service: $url (NOT ACCESSIBLE)"
        fi
    done
}

# Main execution
main() {
    print_status "Checking Modern Reservation Infrastructure Services Status"

    cd "$BASE_DIR"

    # Services to check
    declare -a SERVICES=(
        "config-server:8888"
        "eureka-server:8761"
        "zipkin-server:9411"
        "gateway-service:8080"
    )

    local healthy_count=0
    local total_services=${#SERVICES[@]}

    for service_config in "${SERVICES[@]}"; do
        IFS=':' read -r service_name port <<< "$service_config"
        # Handle zipkin-server specially (Docker container)
        if [ "$service_name" = "zipkin-server" ]; then
            if check_zipkin_docker; then
                healthy_count=$((healthy_count + 1))
            fi
        else
            if check_service "$service_name" "$port"; then
                healthy_count=$((healthy_count + 1))
            fi
        fi
    done

    # Show service URLs
    show_service_urls

    # Final status report
    echo -e "\n${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${CYAN} 📊 OVERALL STATUS${NC}"
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

    if [ $healthy_count -eq $total_services ]; then
        print_success "All infrastructure services are healthy! ($healthy_count/$total_services)"
        echo -e "${GREEN}🎉 Infrastructure is ready for business services!${NC}"
    elif [ $healthy_count -gt 0 ]; then
        print_warning "Some services are healthy: $healthy_count/$total_services"
        echo -e "${YELLOW}⚠️  Check failed services and restart if needed${NC}"
    else
        print_error "No services are healthy: $healthy_count/$total_services"
        echo -e "${RED}💥 Infrastructure needs to be started${NC}"
    fi

    echo -e "\n${CYAN}Commands:${NC}"
    echo -e "  Start services: ${GREEN}scripts/start-infrastructure.sh${NC}"
    echo -e "  Stop services:  ${RED}scripts/stop-infrastructure.sh${NC}"
    echo -e "  Check status:   ${BLUE}scripts/check-infrastructure.sh${NC}"
    echo ""
}

# Run main function
main "$@"
