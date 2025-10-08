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

# Detect Docker command to use (with or without sudo)
DOCKER_CMD="docker"
if ! docker ps >/dev/null 2>&1; then
    if sudo docker ps >/dev/null 2>&1; then
        DOCKER_CMD="sudo docker"
    else
        echo "⚠️  Docker is not accessible. Please ensure Docker is running and you have proper permissions."
        exit 1
    fi
fi

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
        "DOCKER") color=$BLUE ;;
    esac

    printf "${color}│ %-19s │ %-10s │ %-11s │ %-30s │${NC}\n" "$service" "$status" "$port" "$details"
}

# Function to print table footer
print_table_footer() {
    printf "${CYAN}└─────────────────────┴────────────┴─────────────┴────────────────────────────────┘${NC}\n"
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

# Function to check Docker Zipkin service and return status info
check_zipkin_docker() {
    local service_name="zipkin-server"
    local port=9411

    # Check if Docker container is running
    if $DOCKER_CMD ps --filter "name=modern-reservation-zipkin" --format "{{.Names}}" | grep -q "modern-reservation-zipkin"; then
        # Check Zipkin specific health endpoint
        if curl -s -f "http://localhost:$port/health" >/dev/null 2>&1; then
            echo "DOCKER|Container healthy"
            return 0
        elif curl -s -f "http://localhost:$port" >/dev/null 2>&1; then
            echo "DOCKER|Container accessible"
            return 0
        else
            echo "WARNING|Container not responding"
            return 1
        fi
    else
        echo "FAILED|Container not running"
        return 1
    fi
}

# Function to check a single service and return status info
check_service() {
    local service_name=$1
    local port=$2
    local pid_file="$BASE_DIR/${service_name}.pid"

    # Check PID file
    if [ ! -f "$pid_file" ]; then
        if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
            echo "FAILED|Port occupied by other process"
        else
            echo "FAILED|PID file not found"
        fi
        return 1
    fi

    local pid=$(cat "$pid_file")

    if [ -z "$pid" ]; then
        rm -f "$pid_file"
        echo "FAILED|Empty PID file"
        return 1
    fi

    # Check if process is running
    if ! ps -p "$pid" > /dev/null 2>&1; then
        rm -f "$pid_file"
        echo "FAILED|Process not running"
        return 1
    fi

    # Check port
    if ! lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo "WARNING|Port not listening"
        return 1
    fi

    local port_pid=$(lsof -Pi :$port -sTCP:LISTEN -t 2>/dev/null)
    # Check if port_pid matches our PID or is a child of our PID (Maven spawns child Java process)
    if [ "$port_pid" != "$pid" ]; then
        # Check if port_pid is a child of our PID
        local parent_pid=$(ps -o ppid= -p "$port_pid" 2>/dev/null | tr -d ' ')
        if [ "$parent_pid" != "$pid" ]; then
            echo "WARNING|Port used by different process"
            return 1
        fi
    fi

    # Check service health
    if check_service_health "$service_name" "$port"; then
        echo "HEALTHY|PID: $pid"
        return 0
    else
        echo "WARNING|Health check failed"
        return 1
    fi
}

# Function to show service URLs in table format
show_service_urls_table() {
    echo -e "\n${CYAN}🌐 SERVICE URLS${NC}"
    echo ""
    printf "${CYAN}┌─────────────────────┬──────────────────────────────────────────────────────┐${NC}\n"
    printf "${CYAN}│ %-19s │ %-52s │${NC}\n" "Service" "URL"
    printf "${CYAN}├─────────────────────┼──────────────────────────────────────────────────────┤${NC}\n"

    declare -A SERVICE_URLS=(
        ["config-server"]="http://localhost:8888"
        ["eureka-server"]="http://localhost:8761"
        ["zipkin-server"]="http://localhost:9411"
        ["gateway-service"]="http://localhost:8080"
    )

    for service in config-server eureka-server zipkin-server gateway-service; do
        local url="${SERVICE_URLS[$service]}"
        local status_color=$RED
        local status_icon="❌"

        # Check accessibility
        if [ "$service" = "zipkin-server" ]; then
            if $DOCKER_CMD ps --filter "name=modern-reservation-zipkin" --format "{{.Names}}" | grep -q "modern-reservation-zipkin"; then
                if curl -s -f "http://localhost:9411/health" >/dev/null 2>&1; then
                    status_color=$GREEN
                    status_icon="✅"
                fi
            fi
        else
            local port=$(echo $url | grep -o '[0-9]*$')
            set +e  # Temporarily disable exit on error
            if check_service_health "$service" "$port"; then
                status_color=$GREEN
                status_icon="✅"
            fi
            set -e  # Re-enable exit on error
        fi

        printf "${status_color}│ %-19s │ %s %-48s │${NC}\n" "$service" "$status_icon" "$url"
    done

    printf "${CYAN}└─────────────────────┴──────────────────────────────────────────────────────┘${NC}\n"
}

# Function to show service discovery status
show_service_discovery_status() {
    echo -e "\n${CYAN}🔍 SERVICE DISCOVERY STATUS${NC}"
    echo ""

    if curl -s http://localhost:8761/actuator/health > /dev/null 2>&1; then
        printf "${GREEN}✅ Eureka Server: AVAILABLE${NC} - Dashboard: ${BLUE}http://localhost:8761${NC}\n"

        # Get registered services from Eureka
        local registered_services=$(curl -s "http://localhost:8761/eureka/apps" 2>/dev/null | grep -o '<name>[^<]*</name>' | sed 's/<[^>]*>//g' | sort -u)

        if [ -z "$registered_services" ]; then
            printf "${YELLOW}⚠️  Business Services: NONE REGISTERED${NC}\n"
            printf "   💡 Run: ${GREEN}./scripts/start-business-services.sh${NC} to register services\n"
        else
            printf "${GREEN}✅ Business Services: REGISTERED${NC}\n"
            while IFS= read -r service; do
                if [ -n "$service" ]; then
                    printf "   • %s\n" "$service"
                fi
            done <<< "$registered_services"
        fi

        printf "\n${CYAN}Service Discovery Features:${NC} ✅ Registration | ✅ Health Monitoring | ✅ Load Balancing | ✅ Communication\n"
    else
        printf "${RED}❌ Eureka Server: NOT AVAILABLE${NC} - Service discovery disabled\n"
        printf "   💡 Start infrastructure: ${GREEN}./scripts/start-infrastructure.sh${NC}\n"
    fi
}

# Function to show Docker services status
show_docker_services_status() {
    echo -e "\n${CYAN}🐳 DOCKER SERVICES STATUS${NC}"
    echo ""
    printf "${CYAN}┌─────────────────────┬────────────┬─────────────┬────────────────────────────────┐${NC}\n"
    printf "${CYAN}│ %-19s │ %-10s │ %-11s │ %-30s │${NC}\n" "Service" "Status" "Port" "Details"
    printf "${CYAN}├─────────────────────┼────────────┼─────────────┼────────────────────────────────┤${NC}\n"

    # Check Zipkin Docker service
    local status_info
    set +e  # Temporarily disable exit on error
    status_info=$(check_zipkin_docker)
    set -e  # Re-enable exit on error

    IFS='|' read -r status details <<< "$status_info"
    print_table_row "zipkin-server" "$status" "9411" "$details"

    # Check other Docker services (PostgreSQL, Redis, pgAdmin)
    # PostgreSQL
    if $DOCKER_CMD ps --filter "name=modern-reservation-postgres" --format "{{.Names}}" | grep -q "modern-reservation-postgres"; then
        if $DOCKER_CMD exec modern-reservation-postgres pg_isready -U postgres >/dev/null 2>&1; then
            print_table_row "postgresql" "DOCKER" "5432" "Database ready"
        else
            print_table_row "postgresql" "WARNING" "5432" "Container running, DB not ready"
        fi
    else
        print_table_row "postgresql" "FAILED" "5432" "Container not running"
    fi

    # pgAdmin
    if $DOCKER_CMD ps --filter "name=modern-reservation-pgadmin" --format "{{.Names}}" | grep -q "modern-reservation-pgadmin"; then
        if curl -s -f http://localhost:5050 >/dev/null 2>&1; then
            print_table_row "pgadmin" "DOCKER" "5050" "GUI ready"
        else
            print_table_row "pgadmin" "WARNING" "5050" "Container running, GUI not ready"
        fi
    else
        print_table_row "pgadmin" "FAILED" "5050" "Container not running"
    fi

    # Redis
    if $DOCKER_CMD ps --filter "name=modern-reservation-redis" --format "{{.Names}}" | grep -q "modern-reservation-redis"; then
        if $DOCKER_CMD exec modern-reservation-redis redis-cli ping >/dev/null 2>&1; then
            print_table_row "redis" "DOCKER" "6379" "Cache ready"
        else
            print_table_row "redis" "WARNING" "6379" "Container running, service not ready"
        fi
    else
        print_table_row "redis" "FAILED" "6379" "Container not running"
    fi

    # Kafka
    if $DOCKER_CMD ps --filter "name=modern-reservation-kafka" --format "{{.Names}}" | grep -q "modern-reservation-kafka"; then
        if $DOCKER_CMD exec modern-reservation-kafka kafka-broker-api-versions.sh --bootstrap-server localhost:9092 >/dev/null 2>&1; then
            print_table_row "kafka" "DOCKER" "9092" "Broker ready"
        else
            print_table_row "kafka" "WARNING" "9092" "Container running, broker not ready"
        fi
    else
        print_table_row "kafka" "FAILED" "9092" "Container not running"
    fi

    # Kafka UI
    if $DOCKER_CMD ps --filter "name=modern-reservation-kafka-ui" --format "{{.Names}}" | grep -q "modern-reservation-kafka-ui"; then
        if curl -s -f http://localhost:8090 >/dev/null 2>&1; then
            print_table_row "kafka-ui" "DOCKER" "8090" "Monitoring ready"
        else
            print_table_row "kafka-ui" "WARNING" "8090" "Container running, UI not ready"
        fi
    else
        print_table_row "kafka-ui" "FAILED" "8090" "Container not running"
    fi

    printf "${CYAN}└─────────────────────┴────────────┴─────────────┴────────────────────────────────┘${NC}\n"
}

# Main function
main() {
    print_status "Checking Modern Reservation Infrastructure Services Status"

    cd "$BASE_DIR"

    # Java Infrastructure Services to check
    declare -a SERVICES=(
        "config-server:8888"
        "eureka-server:8761"
        "gateway-service:8080"
        "tenant-service:8085"
    )

    local healthy_count=0
    local total_services=${#SERVICES[@]}
    local docker_services=6  # zipkin, postgres, pgadmin, redis, kafka, kafka-ui
    total_services=$((total_services + docker_services))

    # Print header
    echo -e "\n${CYAN}🔍 INFRASTRUCTURE SERVICES STATUS${NC}"
    echo ""
    print_table_header

    # Check each Java infrastructure service and print table rows
    for service_config in "${SERVICES[@]}"; do
        IFS=':' read -r service_name port <<< "$service_config"

        local status_info
        local service_healthy=0

        set +e  # Temporarily disable exit on error
        status_info=$(check_service "$service_name" "$port")
        if [ $? -eq 0 ]; then
            service_healthy=1
        else
            # Even if strict checks fail, consider service healthy if URL is accessible
            # Check different endpoints based on service type
            local url_accessible=0
            if curl -s "http://localhost:$port" >/dev/null 2>&1; then
                url_accessible=1
            elif curl -s "http://localhost:$port/actuator/health" >/dev/null 2>&1; then
                url_accessible=1
            elif [ "$service_name" = "eureka-server" ] && curl -s "http://localhost:$port/eureka/apps" >/dev/null 2>&1; then
                url_accessible=1
            fi

            if [ $url_accessible -eq 1 ]; then
                service_healthy=1
            fi
        fi
        set -e  # Re-enable exit on error

        if [ $service_healthy -eq 1 ]; then
            healthy_count=$((healthy_count + 1))
        fi

        IFS='|' read -r status details <<< "$status_info"
        print_table_row "$service_name" "$status" "$port" "$details"
    done

    print_table_footer

    # Check Docker services separately
    show_docker_services_status

    # Count Docker services health
    # Zipkin
    if $DOCKER_CMD ps --filter "name=modern-reservation-zipkin" --format "{{.Names}}" | grep -q "modern-reservation-zipkin" >/dev/null 2>&1; then
        healthy_count=$((healthy_count + 1))
    fi

    # PostgreSQL
    if $DOCKER_CMD ps --filter "name=modern-reservation-postgres" --format "{{.Names}}" | grep -q "modern-reservation-postgres" >/dev/null 2>&1; then
        healthy_count=$((healthy_count + 1))
    fi

    # Redis
    if $DOCKER_CMD ps --filter "name=modern-reservation-redis" --format "{{.Names}}" | grep -q "modern-reservation-redis" >/dev/null 2>&1; then
        healthy_count=$((healthy_count + 1))
    fi

    # Show service URLs in table format
    show_service_urls_table

    # Show Service Discovery Status
    show_service_discovery_status

    # Final status report
    echo -e "\n${CYAN}📊 OVERALL STATUS${NC}"
    echo ""

    if [ $healthy_count -eq $total_services ]; then
        printf "${GREEN}🎉 All services healthy: %d/%d - Infrastructure ready!${NC}\n" $healthy_count $total_services
    elif [ $healthy_count -gt 0 ]; then
        printf "${YELLOW}⚠️  Partial health: %d/%d services running${NC}\n" $healthy_count $total_services
    else
        printf "${RED}💥 No services running: %d/%d - Infrastructure down${NC}\n" $healthy_count $total_services
    fi

    echo -e "\n${CYAN}💡 COMMANDS${NC}"
    printf "   Start: ${GREEN}./scripts/start-infrastructure.sh${NC}  |  Stop: ${RED}./scripts/stop-infrastructure.sh${NC}  |  Check: ${BLUE}./scripts/check-infrastructure.sh${NC}\n"
    echo ""
}

# Run main function
main "$@"
