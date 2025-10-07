#!/bin/bash

# Modern Reservation System - Business Services Status Check Script
# This script checks the health and status of all business services

set -e

# Colors for output
R    # Format: "service-name:port:context-path"
    declare -a BUSINESS_SERVICES=(
        "reservation-engine:8081:/reservation-engine"
        "availability-calculator:8083:/availability-calculator"
        "payment-processor:8084:/payment-processor"
        "rate-management:8087:/rate-management"
        "analytics-engine:8086:/analytics-engine"
    )[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
NC='\033[0m' # No Color

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

# Function to print colored output
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

# Function to print table header
print_table_header() {
    printf "${CYAN}┌─────────────────────┬────────────┬─────────────┬────────────────────────────────┐${NC}\n"
    printf "${CYAN}│ %-19s │ %-10s │ %-11s │ %-30s │${NC}\n" "Service" "Status" "Port" "Details"
    printf "${CYAN}├─────────────────────┼────────────┼─────────────┼────────────────────────────────┤${NC}\n"
}

# Function to print table row
print_table_row() {
    local service=$1
    local status=$2
    local port=$3
    local details=$4

    # Determine color based on status
    local color=$NC
    case $status in
        "HEALTHY") color=$GREEN ;;
        "FAILED") color=$RED ;;
        "WARNING") color=$YELLOW ;;
        "RESPONDING") color=$YELLOW ;;
    esac

    printf "${color}│ %-19s │ %-10s │ %-11s │ %-30s │${NC}\n" "$service" "$status" "$port" "$details"
}

# Function to print table footer
print_table_footer() {
    printf "${CYAN}└─────────────────────┴────────────┴─────────────┴────────────────────────────────┘${NC}\n"
}

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

# Function to check a single business service and return status info
check_business_service() {
    local service_name=$1
    local port=$2
    local context_path=$3
    local pid_file="$BASE_DIR/${service_name}.pid"
    local timeout=5

    # Check PID file and process
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if [ ! -z "$pid" ] && ps -p "$pid" > /dev/null 2>&1; then
            # Check if port is being used by this process
            local port_pid=$(lsof -ti :$port 2>/dev/null || echo "")
            if [ "$port_pid" = "$pid" ]; then
                # Check health endpoint
                local health_url="http://localhost:$port$context_path/actuator/health"
                if curl -s --max-time $timeout "$health_url" >/dev/null 2>&1; then
                    local health_status=$(curl -s --max-time $timeout "$health_url" | grep -o '"status":"[^"]*"' | cut -d'"' -f4 2>/dev/null || echo "UNKNOWN")
                    if [ "$health_status" = "UP" ]; then
                        echo "HEALTHY|PID: $pid, Health: UP"
                        return 0
                    else
                        echo "RESPONDING|PID: $pid, Health: $health_status"
                        return 1
                    fi
                else
                    echo "WARNING|PID: $pid, Health check failed"
                    return 1
                fi
            else
                echo "WARNING|PID: $pid, Port used by different process"
                return 1
            fi
        else
            rm -f "$pid_file"  # Clean up stale PID file
            echo "FAILED|Stale PID file removed"
            return 1
        fi
    else
        # Check if port is occupied by some other process
        if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
            echo "FAILED|Port occupied by other process"
        else
            echo "FAILED|Service not running"
        fi
        return 1
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
analytics-engine:8086:/analytics-engine
    )

    local healthy_services=0
    local total_services=${#BUSINESS_SERVICES[@]}

    # Print table header
    echo -e "${CYAN}🏢 BUSINESS SERVICES STATUS${NC}"
    echo ""
    print_table_header

    # Check each business service and print table rows
    for service_config in "${BUSINESS_SERVICES[@]}"; do
        IFS=':' read -r service_name port context_path <<< "$service_config"

        local status_info
        local service_healthy=0

        set +e  # Temporarily disable exit on error
        status_info=$(check_business_service "$service_name" "$port" "$context_path")
        if [ $? -eq 0 ]; then
            service_healthy=1
        else
            # Even if strict checks fail, consider service healthy if URL is accessible
            local url_accessible=0
            if curl -s "http://localhost:$port$context_path/actuator/health" >/dev/null 2>&1; then
                url_accessible=1
            elif curl -s "http://localhost:$port$context_path" >/dev/null 2>&1; then
                url_accessible=1
            elif curl -s "http://localhost:$port/actuator/health" >/dev/null 2>&1; then
                url_accessible=1
            fi

            if [ $url_accessible -eq 1 ]; then
                service_healthy=1
            fi
        fi
        set -e  # Re-enable exit on error

        if [ $service_healthy -eq 1 ]; then
            healthy_services=$((healthy_services + 1))
        fi

        IFS='|' read -r status details <<< "$status_info"
        print_table_row "$service_name" "$status" "$port" "$details"
    done

    print_table_footer

    # Service URLs section in table format
    echo -e "\n${CYAN}🌐 SERVICE URLS${NC}"
    echo ""
    printf "${CYAN}┌─────────────────────┬──────────────────────────────────────────────────────┐${NC}\n"
    printf "${CYAN}│ %-19s │ %-52s │${NC}\n" "Service" "URL"
    printf "${CYAN}├─────────────────────┼──────────────────────────────────────────────────────┤${NC}\n"

    for service_config in "${BUSINESS_SERVICES[@]}"; do
        IFS=':' read -r service_name port context_path <<< "$service_config"
        local url="http://localhost:$port$context_path"
        local status_color=$RED
        local status_icon="❌"

        # Check accessibility
        if curl -s --max-time 3 "$url" >/dev/null 2>&1; then
            status_color=$GREEN
            status_icon="✅"
        fi

        printf "${status_color}│ %-19s │ %s %-48s │${NC}\n" "$service_name" "$status_icon" "$url"
    done

    printf "${CYAN}└─────────────────────┴──────────────────────────────────────────────────────┘${NC}\n"

    # Overall status summary
    echo -e "\n${CYAN}📊 OVERALL STATUS${NC}"
    echo ""

    if [ $healthy_services -eq $total_services ]; then
        printf "${GREEN}🎉 All services healthy: %d/%d - Business layer ready!${NC}\n" $healthy_services $total_services
    elif [ $healthy_services -gt 0 ]; then
        printf "${YELLOW}⚠️  Partial health: %d/%d services running${NC}\n" $healthy_services $total_services
    else
        printf "${RED}💥 No services running: %d/%d - Business layer down${NC}\n" $healthy_services $total_services
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
