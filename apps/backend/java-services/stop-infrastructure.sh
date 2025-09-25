#!/bin/bash

# Modern Reservation System - Complete Services Stop Script
# This script stops all infrastructure and business services

echo "🛑 Stopping Modern Reservation System Services..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Project root directory
PROJECT_ROOT="/home/subramani/modern-reservation"
JAVA_SERVICES_DIR="$PROJECT_ROOT/apps/backend/java-services"

# Function to stop a service
stop_service() {
    local service_dir=$1
    local service_name=$2
    local port=$3

    echo -e "${BLUE}🛑 Stopping $service_name...${NC}"

    cd "$JAVA_SERVICES_DIR/$service_dir"

    # Check if PID file exists
    if [ -f "$service_name.pid" ]; then
        local pid=$(cat "$service_name.pid" 2>/dev/null || echo "")
        if [ ! -z "$pid" ]; then
            if kill -0 "$pid" 2>/dev/null; then
                echo -e "${YELLOW}Stopping $service_name (PID: $pid)...${NC}"
                kill -15 "$pid" 2>/dev/null || true

                # Wait for graceful shutdown
                local attempt=1
                while [ $attempt -le 10 ] && kill -0 "$pid" 2>/dev/null; do
                    sleep 1
                    ((attempt++))
                done

                # Force kill if still running
                if kill -0 "$pid" 2>/dev/null; then
                    echo -e "${YELLOW}Force killing $service_name...${NC}"
                    kill -9 "$pid" 2>/dev/null || true
                fi

                echo -e "${GREEN}✅ $service_name stopped${NC}"
            else
                echo -e "${YELLOW}$service_name was not running${NC}"
            fi
        fi
        rm -f "$service_name.pid"
    else
        echo -e "${YELLOW}No PID file found for $service_name${NC}"
    fi

    # Also kill any process on the port
    local port_pid=$(lsof -ti:$port 2>/dev/null || true)
    if [ ! -z "$port_pid" ]; then
        echo -e "${YELLOW}Killing process on port $port (PID: $port_pid)...${NC}"
        kill -9 "$port_pid" 2>/dev/null || true
    fi
}

# Parse command line arguments
INFRASTRUCTURE_ONLY=false
STOP_ONLY=""

while [[ $# -gt 0 ]]; do
    case $1 in
        --infrastructure-only|-i)
            INFRASTRUCTURE_ONLY=true
            shift
            ;;
        --service|-s)
            STOP_ONLY="$2"
            shift 2
            ;;
        --help|-h)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --infrastructure-only, -i      Stop only infrastructure services"
            echo "  --service SERVICE, -s SERVICE  Stop only specified service"
            echo "  --help, -h                     Show this help message"
            echo ""
            echo "Available services:"
            echo "  config-server, eureka-server, zipkin-server, gateway-service"
            echo "  reservation-engine, availability-calculator, payment-processor"
            echo "  rate-management, analytics-engine"
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Function to stop specific service
stop_specific_service() {
    local service_name=$1
    case $service_name in
        config-server)
            stop_service "infrastructure/config-server" "config-server" "8888"
            ;;
        eureka-server)
            stop_service "infrastructure/eureka-server" "eureka-server" "8761"
            ;;
        zipkin-server)
            stop_service "infrastructure/zipkin-server" "zipkin-server" "9411"
            ;;
        gateway-service)
            stop_service "infrastructure/gateway-service" "gateway-service" "8080"
            ;;
        reservation-engine)
            stop_service "business-services/reservation-engine" "reservation-engine" "8081"
            ;;
        availability-calculator)
            stop_service "business-services/availability-calculator" "availability-calculator" "8083"
            ;;
        payment-processor)
            stop_service "business-services/payment-processor" "payment-processor" "8084"
            ;;
        rate-management)
            stop_service "business-services/rate-management" "rate-management" "8085"
            ;;
        analytics-engine)
            stop_service "business-services/analytics-engine" "analytics-engine" "8086"
            ;;
        *)
            echo -e "${RED}❌ Unknown service: $service_name${NC}"
            exit 1
            ;;
    esac
}

# Stop specific service if requested
if [ ! -z "$STOP_ONLY" ]; then
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}              STOPPING SINGLE SERVICE: $STOP_ONLY              ${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
    stop_specific_service "$STOP_ONLY"
    exit 0
fi

echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}                      STOPPING SERVICES                        ${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"

# Stop business services first (if not infrastructure-only)
if [ "$INFRASTRUCTURE_ONLY" = false ]; then
    echo -e "${BLUE}Stopping Business Services...${NC}"

    stop_service "business-services/analytics-engine" "analytics-engine" "8086"
    stop_service "business-services/rate-management" "rate-management" "8085"
    stop_service "business-services/payment-processor" "payment-processor" "8084"
    stop_service "business-services/availability-calculator" "availability-calculator" "8083"
    stop_service "business-services/reservation-engine" "reservation-engine" "8081"

    echo ""
fi

# Stop infrastructure services (in reverse order)
echo -e "${BLUE}Stopping Infrastructure Services...${NC}"

stop_service "infrastructure/gateway-service" "gateway-service" "8080"
stop_service "infrastructure/zipkin-server" "zipkin-server" "9411"
stop_service "infrastructure/eureka-server" "eureka-server" "8761"
stop_service "infrastructure/config-server" "config-server" "8888"

echo ""
echo -e "${GREEN}🎉 All services stopped successfully!${NC}"
echo ""

# Clean up any remaining processes on known ports
echo -e "${BLUE}Cleaning up any remaining processes...${NC}"
PORTS=(8888 8761 9411 8080 8081 8083 8084 8085 8086)

for port in "${PORTS[@]}"; do
    pid=$(lsof -ti:$port 2>/dev/null || true)
    if [ ! -z "$pid" ]; then
        echo -e "${YELLOW}Killing remaining process on port $port (PID: $pid)...${NC}"
        kill -9 "$pid" 2>/dev/null || true
    fi
done

echo -e "${GREEN}✅ System cleanup completed!${NC}"
echo ""
echo -e "${BLUE}💡 To start services again, run: ./start-infrastructure.sh${NC}"
