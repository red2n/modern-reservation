#!/bin/bash

# Modern Reservation System - Infrastructure Services Startup Script
# This script starts all infrastructure services in the correct order

set -e

echo "🏗️  Starting Modern Reservation Infrastructure Services..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Project root directory
PROJECT_ROOT="/home/subramani/modern-reservation"
JAVA_SERVICES_DIR="$PROJECT_ROOT/apps/backend/java-services"

# Function to check if a service is running
check_service() {
    local service_name=$1
    local port=$2
    local max_attempts=30
    local attempt=1

    echo -e "${YELLOW}Waiting for $service_name to start on port $port...${NC}"

    while [ $attempt -le $max_attempts ]; do
        if curl -f http://localhost:$port/actuator/health >/dev/null 2>&1; then
            echo -e "${GREEN}✅ $service_name is running!${NC}"
            return 0
        fi
        echo -e "${BLUE}Attempt $attempt/$max_attempts - $service_name not ready yet...${NC}"
        sleep 5
        ((attempt++))
    done

    echo -e "${RED}❌ $service_name failed to start within expected time!${NC}"
    return 1
}

# Function to start a service
start_service() {
    local service_dir=$1
    local service_name=$2
    local port=$3

    echo -e "${BLUE}🚀 Starting $service_name...${NC}"
    cd "$JAVA_SERVICES_DIR/$service_dir"

    # Start service in background
    nohup mvn spring-boot:run > "logs/$service_name.log" 2>&1 &
    echo $! > "$service_name.pid"

    # Wait for service to be ready
    check_service "$service_name" "$port"
}

# Create logs directory
mkdir -p "$JAVA_SERVICES_DIR/infrastructure/config-server/logs"
mkdir -p "$JAVA_SERVICES_DIR/infrastructure/eureka-server/logs"
mkdir -p "$JAVA_SERVICES_DIR/infrastructure/gateway-service/logs"
mkdir -p "$JAVA_SERVICES_DIR/infrastructure/zipkin-server/logs"

echo -e "${YELLOW}📋 Infrastructure Services Startup Order:${NC}"
echo "1. Config Server (Port 8888)"
echo "2. Eureka Server (Port 8761)"
echo "3. Zipkin Server (Port 9411)"
echo "4. Gateway Service (Port 8080)"
echo ""

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}❌ Maven is not installed or not in PATH${NC}"
    exit 1
fi

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo -e "${RED}❌ Java is not installed or not in PATH${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Prerequisites check passed${NC}"
echo ""

# Start services in order
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}                    STARTING INFRASTRUCTURE                    ${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"

# 1. Config Server (must start first)
start_service "infrastructure/config-server" "config-server" "8888"

# 2. Eureka Server (service discovery)
start_service "infrastructure/eureka-server" "eureka-server" "8761"

# 3. Zipkin Server (distributed tracing)
start_service "infrastructure/zipkin-server" "zipkin-server" "9411"

# 4. Gateway Service (API gateway)
start_service "infrastructure/gateway-service" "gateway-service" "8080"

echo ""
echo -e "${GREEN}🎉 All infrastructure services are running!${NC}"
echo ""
echo -e "${YELLOW}📊 Service URLs:${NC}"
echo "• Config Server:    http://localhost:8888/config"
echo "• Eureka Dashboard: http://localhost:8761"
echo "• Zipkin UI:        http://localhost:9411"
echo "• API Gateway:      http://localhost:8080"
echo ""
echo -e "${YELLOW}📋 Management Endpoints:${NC}"
echo "• Config Health:    http://localhost:8888/actuator/health"
echo "• Eureka Health:    http://localhost:8761/actuator/health"
echo "• Zipkin Health:    http://localhost:9411/actuator/health"
echo "• Gateway Health:   http://localhost:8080/actuator/health"
echo ""
echo -e "${BLUE}💡 Use ./stop-infrastructure.sh to stop all services${NC}"
echo -e "${BLUE}💡 Logs are available in the respective service logs/ directories${NC}"
