#!/bin/bash

# Modern Reservation System - Business Services Status Check Script
# This script checks the health and status of all business services

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
NC='\033[0m' # No Color

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

# Function to check service health endpoint
check_health() {
    local service_name=$1
    local port=$2
    local context_path=$3
    local timeout=5

    local health_url="http://localhost:$port$context_path/actuator/health"

    # Check if service responds to health endpoint
    if curl -s --max-time $timeout "$health_url" >/dev/null 2>&1; then
        local health_status=$(curl -s --max-time $timeout "$health_url" | grep -o '"status":"[^"]*"' | cut -d'"' -f4 2>/dev/null || echo "UNKNOWN")
        if [ "$health_status" = "UP" ]; then
            echo -e "${GREEN}✅ $service_name: HEALTHY${NC}"
            echo -e "ℹ️  Health endpoint: $health_url"
        else
            echo -e "${YELLOW}⚠️  $service_name: RESPONDING (Status: $health_status)${NC}"
            echo -e "ℹ️  Health endpoint: $health_url"
        fi
        return 0
    else
        return 1
    fi
}

# Function to check if service is accessible via HTTP
check_http_accessibility() {
    local service_name=$1
    local port=$2
    local context_path=$3

    local base_url="http://localhost:$port$context_path"

    if curl -s --max-time 3 "$base_url" >/dev/null 2>&1; then
        echo -e "${GREEN}✅ $service_name: ${base_url} (ACCESSIBLE)${NC}"
        return 0
    else
        echo -e "${RED}❌ $service_name: ${base_url} (NOT ACCESSIBLE)${NC}"
        return 1
    fi
}

# Function to check a single business service
check_business_service() {
    local service_name=$1
    local port=$2
    local context_path=$3
    local pid_file="$BASE_DIR/${service_name}.pid"

    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${CYAN} 🔍 Checking: $service_name${NC}"
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

    # Check PID file
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if [ ! -z "$pid" ] && ps -p "$pid" > /dev/null 2>&1; then
            echo -e "${GREEN}✅ $service_name: Running (PID: $pid)${NC}"

            # Check if port is being used by this process
            local port_pid=$(lsof -ti :$port 2>/dev/null || echo "")
            if [ "$port_pid" = "$pid" ]; then
                echo -e "${GREEN}✅ Port $port: Used by $service_name (PID: $pid)${NC}"
            else
                echo -e "${YELLOW}⚠️  Port $port: Used by different process (PID: $port_pid)${NC}"
            fi
        else
            echo -e "${RED}❌ $service_name: PID file exists but process not running${NC}"
            echo -e "ℹ️  Stale PID file: $pid_file"
        fi
    else
        echo -e "${RED}❌ $service_name: PID file not found${NC}"
    fi

    # Check port status
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        local port_process=$(lsof -Pi :$port -sTCP:LISTEN 2>/dev/null | tail -n 1 | awk '{print $1, $2}' || echo "Unknown")
        echo -e "${YELLOW}ℹ️  Port $port: OCCUPIED by $port_process${NC}"
    else
        echo -e "${BLUE}ℹ️  Port $port: FREE${NC}"
    fi

    # Check health endpoint if port is occupied
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        if check_health "$service_name" "$port" "$context_path"; then
            return 0  # Service is healthy
        else
            echo -e "${RED}❌ $service_name: Health check failed${NC}"
            return 1  # Service is not healthy
        fi
    else
        echo -e "${RED}❌ $service_name: Service not running${NC}"
        return 1
    fi
}

# Main function
main() {
    echo -e "${BLUE}[$(date '+%Y-%m-%d %H:%M:%S')] Checking Modern Reservation Business Services Status${NC}"
    echo ""

    # Business services configuration
    # Format: "service-name:port:context-path"
    declare -a BUSINESS_SERVICES=(
        "reservation-engine:8081:/reservation-engine"
        "availability-calculator:8083:/availability-calculator"
        "payment-processor:8084:/payment-processor"
        "rate-management:8085:/rate-management"
        "analytics-engine:8086:/analytics-engine"
    )

    local healthy_services=0
    local total_services=${#BUSINESS_SERVICES[@]}

    # Check each business service
    for service_config in "${BUSINESS_SERVICES[@]}"; do
        IFS=':' read -r service_name port context_path <<< "$service_config"

        if check_business_service "$service_name" "$port" "$context_path"; then
            healthy_services=$((healthy_services + 1))
        fi
        echo ""
    done

    # Service URLs section
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${CYAN} 🌐 Service URLs${NC}"
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

    for service_config in "${BUSINESS_SERVICES[@]}"; do
        IFS=':' read -r service_name port context_path <<< "$service_config"
        check_http_accessibility "$service_name" "$port" "$context_path"
    done

    echo ""

    # Overall status summary
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${CYAN} 📊 OVERALL STATUS${NC}"
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

    if [ $healthy_services -eq $total_services ]; then
        echo -e "${GREEN}✅ All business services are healthy: $healthy_services/$total_services${NC}"
        echo -e "${GREEN}🎉 Business services are ready to serve requests!${NC}"
    elif [ $healthy_services -gt 0 ]; then
        echo -e "${YELLOW}⚠️  Partially healthy: $healthy_services/$total_services services are running${NC}"
        echo -e "${YELLOW}💥 Some business services need attention${NC}"
    else
        echo -e "${RED}❌ No services are healthy: $healthy_services/$total_services${NC}"
        echo -e "${RED}💥 Business services need to be started${NC}"
    fi

    echo ""
    echo -e "${BLUE}Commands:${NC}"
    echo -e "${GREEN}  Start business services: scripts/start-business-services.sh${NC}"
    echo -e "${RED}  Stop business services:  scripts/stop-business-services.sh${NC}"
    echo -e "${BLUE}  Check status:            scripts/check-business-services.sh${NC}"

    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

    # Return appropriate exit code
    if [ $healthy_services -eq $total_services ]; then
        return 0
    else
        return 1
    fi
}

# Run main function
main "$@"
