#!/bin/bash

# Modern Reservation System - Infrastructure Services Shutdown Script
# This script stops all infrastructure services gracefully

set -e

echo "🛑 Stopping Modern Reservation Infrastructure Services..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Project directories
PROJECT_ROOT="/home/subramani/modern-reservation"
JAVA_SERVICES_DIR="$PROJECT_ROOT/apps/backend/java-services"

# Function to stop a service
stop_service() {
    local service_dir=$1
    local service_name=$2

    cd "$JAVA_SERVICES_DIR/$service_dir"

    if [ -f "$service_name.pid" ]; then
        local pid=$(cat "$service_name.pid")
        if ps -p $pid > /dev/null 2>&1; then
            echo -e "${YELLOW}🛑 Stopping $service_name (PID: $pid)...${NC}"
            kill $pid

            # Wait for graceful shutdown
            local attempt=1
            while [ $attempt -le 10 ] && ps -p $pid > /dev/null 2>&1; do
                sleep 2
                ((attempt++))
            done

            # Force kill if still running
            if ps -p $pid > /dev/null 2>&1; then
                echo -e "${RED}⚠️  Force killing $service_name...${NC}"
                kill -9 $pid
            fi

            echo -e "${GREEN}✅ $service_name stopped${NC}"
        else
            echo -e "${BLUE}ℹ️  $service_name was not running${NC}"
        fi
        rm -f "$service_name.pid"
    else
        echo -e "${BLUE}ℹ️  No PID file found for $service_name${NC}"
    fi
}

echo -e "${YELLOW}📋 Stopping services in reverse order:${NC}"
echo "1. Gateway Service"
echo "2. Zipkin Server"
echo "3. Eureka Server"
echo "4. Config Server"
echo ""

# Stop services in reverse order
stop_service "infrastructure/gateway-service" "gateway-service"
stop_service "infrastructure/zipkin-server" "zipkin-server"
stop_service "infrastructure/eureka-server" "eureka-server"
stop_service "infrastructure/config-server" "config-server"

# Clean up any remaining Java processes (be careful with this)
echo -e "${YELLOW}🧹 Cleaning up any remaining processes...${NC}"
pkill -f "spring-boot:run" || true

echo ""
echo -e "${GREEN}🎉 All infrastructure services have been stopped!${NC}"
echo -e "${BLUE}💡 Log files are preserved in each service's logs/ directory${NC}"
