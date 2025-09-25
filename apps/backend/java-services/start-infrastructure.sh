#!/bin/bash

# Modern Reservation System - Complete Services Startup Script
# This script starts all infrastructure and business services in the correct order

set -e

echo "🏗️  Starting Modern Reservation Complete System..."

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
    local health_endpoint=$3
    local max_attempts=60
    local attempt=1

    echo -e "${YELLOW}Waiting for $service_name to start on port $port...${NC}"

    while [ $attempt -le $max_attempts ]; do
        if curl -f -s --connect-timeout 5 --max-time 10 "$health_endpoint" >/dev/null 2>&1; then
            echo -e "${GREEN}✅ $service_name is running!${NC}"
            return 0
        elif [ $attempt -eq 1 ]; then
            echo -e "${BLUE}Starting $service_name (this may take a few minutes)...${NC}"
        elif [ $((attempt % 6)) -eq 0 ]; then
            echo -e "${BLUE}Still waiting for $service_name... (attempt $attempt/$max_attempts)${NC}"
        fi
        sleep 5
        ((attempt++))
    done

    echo -e "${RED}❌ $service_name failed to start within expected time!${NC}"
    echo -e "${RED}Check logs at: $JAVA_SERVICES_DIR/$(dirname $service_name)/logs/$(basename $service_name).log${NC}"
    return 1
}

# Function to check if port is available
check_port_available() {
    local port=$1
    if netstat -tuln 2>/dev/null | grep -q ":$port "; then
        return 1
    else
        return 0
    fi
}

# Function to kill process on port
kill_port() {
    local port=$1
    local pid=$(lsof -ti:$port 2>/dev/null || true)
    if [ ! -z "$pid" ]; then
        echo -e "${YELLOW}Killing existing process on port $port (PID: $pid)${NC}"
        kill -9 $pid 2>/dev/null || true
        sleep 2
    fi
}

# Function to start a service
start_service() {
    local service_dir=$1
    local service_name=$2
    local port=$3
    local health_endpoint=${4:-"http://localhost:$port/actuator/health"}
    local force_restart=${5:-false}

    echo -e "${BLUE}🚀 Starting $service_name...${NC}"

    # Check if port is already in use
    if ! check_port_available "$port"; then
        if [ "$force_restart" = true ]; then
            echo -e "${YELLOW}Port $port is in use. Killing existing process...${NC}"
            kill_port "$port"
        else
            echo -e "${YELLOW}Port $port is already in use. Service might already be running.${NC}"
            if curl -f -s --connect-timeout 2 --max-time 5 "$health_endpoint" >/dev/null 2>&1; then
                echo -e "${GREEN}✅ $service_name is already running and healthy!${NC}"
                return 0
            else
                echo -e "${YELLOW}Service on port $port is not healthy. Restarting...${NC}"
                kill_port "$port"
            fi
        fi
    fi

    cd "$JAVA_SERVICES_DIR/$service_dir"

    # Create logs directory if it doesn't exist
    mkdir -p logs

    # Check if PID file exists and clean up
    if [ -f "$service_name.pid" ]; then
        local old_pid=$(cat "$service_name.pid" 2>/dev/null || echo "")
        if [ ! -z "$old_pid" ] && kill -0 "$old_pid" 2>/dev/null; then
            echo -e "${YELLOW}Stopping existing $service_name process (PID: $old_pid)${NC}"
            kill -15 "$old_pid" 2>/dev/null || true
            sleep 3
            kill -9 "$old_pid" 2>/dev/null || true
        fi
        rm -f "$service_name.pid"
    fi

    # Start service in background
    echo -e "${BLUE}Launching $service_name with Maven...${NC}"
    nohup mvn spring-boot:run > "logs/$service_name.log" 2>&1 &
    local maven_pid=$!
    echo $maven_pid > "$service_name.pid"

    # Wait for service to be ready
    if check_service "$service_name" "$port" "$health_endpoint"; then
        echo -e "${GREEN}🎉 $service_name started successfully!${NC}"
        return 0
    else
        echo -e "${RED}Failed to start $service_name${NC}"
        # Kill the maven process if health check failed
        if kill -0 "$maven_pid" 2>/dev/null; then
            kill -9 "$maven_pid" 2>/dev/null || true
        fi
        rm -f "$service_name.pid"
        return 1
    fi
}

# Create logs directories
mkdir -p "$JAVA_SERVICES_DIR/infrastructure/config-server/logs"
mkdir -p "$JAVA_SERVICES_DIR/infrastructure/eureka-server/logs"
mkdir -p "$JAVA_SERVICES_DIR/infrastructure/gateway-service/logs"
mkdir -p "$JAVA_SERVICES_DIR/infrastructure/zipkin-server/logs"
mkdir -p "$JAVA_SERVICES_DIR/business-services/reservation-engine/logs"
mkdir -p "$JAVA_SERVICES_DIR/business-services/availability-calculator/logs"
mkdir -p "$JAVA_SERVICES_DIR/business-services/payment-processor/logs"
mkdir -p "$JAVA_SERVICES_DIR/business-services/rate-management/logs"
mkdir -p "$JAVA_SERVICES_DIR/business-services/analytics-engine/logs"

echo -e "${YELLOW}📋 Complete System Startup Order:${NC}"
echo ""
echo -e "${BLUE}INFRASTRUCTURE SERVICES:${NC}"
echo "1. Config Server (Port 8888)"
echo "2. Eureka Server (Port 8761)"
echo "3. Zipkin Server (Port 9411)"
echo "4. Gateway Service (Port 8080)"
echo ""
echo -e "${BLUE}BUSINESS SERVICES:${NC}"
echo "5. Reservation Engine (Port 8081)"
echo "6. Availability Calculator (Port 8083)"
echo "7. Payment Processor (Port 8084)"
echo "8. Rate Management (Port 8085)"
echo "9. Analytics Engine (Port 8086)"
echo ""

# Parse command line arguments
FORCE_RESTART=false
SKIP_BUSINESS=false
START_ONLY=""

while [[ $# -gt 0 ]]; do
    case $1 in
        --force|-f)
            FORCE_RESTART=true
            shift
            ;;
        --infrastructure-only|-i)
            SKIP_BUSINESS=true
            shift
            ;;
        --service|-s)
            START_ONLY="$2"
            shift 2
            ;;
        --help|-h)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --force, -f                    Force restart services even if ports are in use"
            echo "  --infrastructure-only, -i      Start only infrastructure services"
            echo "  --service SERVICE, -s SERVICE  Start only specified service"
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

# Check prerequisites
echo -e "${YELLOW}🔍 Checking prerequisites...${NC}"

if ! command -v mvn &> /dev/null; then
    echo -e "${RED}❌ Maven is not installed or not in PATH${NC}"
    exit 1
fi

if ! command -v java &> /dev/null; then
    echo -e "${RED}❌ Java is not installed or not in PATH${NC}"
    exit 1
fi

if ! command -v curl &> /dev/null; then
    echo -e "${RED}❌ curl is not installed or not in PATH${NC}"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo -e "${RED}❌ Java 17 or higher is required. Current version: $JAVA_VERSION${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Prerequisites check passed${NC}"
echo -e "${GREEN}  • Maven: $(mvn --version | head -n 1)${NC}"
echo -e "${GREEN}  • Java: $(java -version 2>&1 | head -n 1)${NC}"
echo -e "${GREEN}  • Force restart: $FORCE_RESTART${NC}"
echo -e "${GREEN}  • Skip business services: $SKIP_BUSINESS${NC}"
echo ""

# Function to start specific service
start_specific_service() {
    local service_name=$1
    case $service_name in
        config-server)
            start_service "infrastructure/config-server" "config-server" "8888" "http://localhost:8888/actuator/health" "$FORCE_RESTART"
            ;;
        eureka-server)
            start_service "infrastructure/eureka-server" "eureka-server" "8761" "http://localhost:8761/actuator/health" "$FORCE_RESTART"
            ;;
        zipkin-server)
            start_service "infrastructure/zipkin-server" "zipkin-server" "9411" "http://localhost:9411/actuator/health" "$FORCE_RESTART"
            ;;
        gateway-service)
            start_service "infrastructure/gateway-service" "gateway-service" "8080" "http://localhost:8080/actuator/health" "$FORCE_RESTART"
            ;;
        reservation-engine)
            start_service "business-services/reservation-engine" "reservation-engine" "8081" "http://localhost:8081/actuator/health" "$FORCE_RESTART"
            ;;
        availability-calculator)
            start_service "business-services/availability-calculator" "availability-calculator" "8083" "http://localhost:8083/actuator/health" "$FORCE_RESTART"
            ;;
        payment-processor)
            start_service "business-services/payment-processor" "payment-processor" "8084" "http://localhost:8084/actuator/health" "$FORCE_RESTART"
            ;;
        rate-management)
            start_service "business-services/rate-management" "rate-management" "8085" "http://localhost:8085/actuator/health" "$FORCE_RESTART"
            ;;
        analytics-engine)
            start_service "business-services/analytics-engine" "analytics-engine" "8086" "http://localhost:8086/actuator/health" "$FORCE_RESTART"
            ;;
        *)
            echo -e "${RED}❌ Unknown service: $service_name${NC}"
            exit 1
            ;;
    esac
}

# Start specific service if requested
if [ ! -z "$START_ONLY" ]; then
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}              STARTING SINGLE SERVICE: $START_ONLY              ${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
    start_specific_service "$START_ONLY"
    exit 0
fi

# Start all services in order
FAILED_SERVICES=()

echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}                    STARTING INFRASTRUCTURE                    ${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"

# 1. Config Server (must start first)
echo -e "${BLUE}[1/9] Starting Config Server...${NC}"
if ! start_service "infrastructure/config-server" "config-server" "8888" "http://localhost:8888/actuator/health" "$FORCE_RESTART"; then
    FAILED_SERVICES+=("config-server")
fi

# 2. Eureka Server (service discovery)
echo -e "${BLUE}[2/9] Starting Eureka Server...${NC}"
if ! start_service "infrastructure/eureka-server" "eureka-server" "8761" "http://localhost:8761/actuator/health" "$FORCE_RESTART"; then
    FAILED_SERVICES+=("eureka-server")
fi

# 3. Zipkin Server (distributed tracing)
echo -e "${BLUE}[3/9] Starting Zipkin Server...${NC}"
if ! start_service "infrastructure/zipkin-server" "zipkin-server" "9411" "http://localhost:9411/actuator/health" "$FORCE_RESTART"; then
    FAILED_SERVICES+=("zipkin-server")
fi

# 4. Gateway Service (API gateway)
echo -e "${BLUE}[4/9] Starting Gateway Service...${NC}"
if ! start_service "infrastructure/gateway-service" "gateway-service" "8080" "http://localhost:8080/actuator/health" "$FORCE_RESTART"; then
    FAILED_SERVICES+=("gateway-service")
fi

echo ""
echo -e "${GREEN}🎉 Infrastructure services startup completed!${NC}"

# Start business services if not skipped
if [ "$SKIP_BUSINESS" = false ]; then
    echo ""
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}                   STARTING BUSINESS SERVICES                  ${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"

    # Give infrastructure services a moment to fully initialize
    echo -e "${YELLOW}Waiting 10 seconds for infrastructure services to fully initialize...${NC}"
    sleep 10

    # 5. Reservation Engine (core business service)
    echo -e "${BLUE}[5/9] Starting Reservation Engine...${NC}"
    if ! start_service "business-services/reservation-engine" "reservation-engine" "8081" "http://localhost:8081/actuator/health" "$FORCE_RESTART"; then
        FAILED_SERVICES+=("reservation-engine")
    fi

    # 6. Availability Calculator
    echo -e "${BLUE}[6/9] Starting Availability Calculator...${NC}"
    if ! start_service "business-services/availability-calculator" "availability-calculator" "8083" "http://localhost:8083/actuator/health" "$FORCE_RESTART"; then
        FAILED_SERVICES+=("availability-calculator")
    fi

    # 7. Payment Processor
    echo -e "${BLUE}[7/9] Starting Payment Processor...${NC}"
    if ! start_service "business-services/payment-processor" "payment-processor" "8084" "http://localhost:8084/actuator/health" "$FORCE_RESTART"; then
        FAILED_SERVICES+=("payment-processor")
    fi

    # 8. Rate Management
    echo -e "${BLUE}[8/9] Starting Rate Management...${NC}"
    if ! start_service "business-services/rate-management" "rate-management" "8085" "http://localhost:8085/actuator/health" "$FORCE_RESTART"; then
        FAILED_SERVICES+=("rate-management")
    fi

    # 9. Analytics Engine
    echo -e "${BLUE}[9/9] Starting Analytics Engine...${NC}"
    if ! start_service "business-services/analytics-engine" "analytics-engine" "8086" "http://localhost:8086/actuator/health" "$FORCE_RESTART"; then
        FAILED_SERVICES+=("analytics-engine")
    fi

    echo ""
    echo -e "${GREEN}🎉 Business services startup completed!${NC}"
fi

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}                       STARTUP SUMMARY                         ${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"

# Display results
if [ ${#FAILED_SERVICES[@]} -eq 0 ]; then
    echo -e "${GREEN}🎉 ALL SERVICES STARTED SUCCESSFULLY!${NC}"
else
    echo -e "${RED}⚠️  Some services failed to start:${NC}"
    for service in "${FAILED_SERVICES[@]}"; do
        echo -e "${RED}  • $service${NC}"
    done
    echo ""
    echo -e "${YELLOW}� Check the logs in the respective service logs/ directories${NC}"
fi

echo ""
echo -e "${YELLOW}�📊 Service URLs:${NC}"
echo -e "${BLUE}Infrastructure Services:${NC}"
echo "• Config Server:    http://localhost:8888/config"
echo "• Eureka Dashboard: http://localhost:8761"
echo "• Zipkin UI:        http://localhost:9411"
echo "• API Gateway:      http://localhost:8080"

if [ "$SKIP_BUSINESS" = false ]; then
    echo ""
    echo -e "${BLUE}Business Services:${NC}"
    echo "• Reservation Engine:     http://localhost:8081/actuator/health"
    echo "• Availability Calc:      http://localhost:8083/actuator/health"
    echo "• Payment Processor:      http://localhost:8084/actuator/health"
    echo "• Rate Management:        http://localhost:8085/actuator/health"
    echo "• Analytics Engine:       http://localhost:8086/actuator/health"

    echo ""
    echo -e "${BLUE}Gateway Routed Services:${NC}"
    echo "• Reservation Engine:     http://localhost:8080/reservation-engine/actuator/health"
    echo "• Availability Calc:      http://localhost:8080/availability-calculator/actuator/health"
    echo "• Payment Processor:      http://localhost:8080/payment-processor/actuator/health"
    echo "• Rate Management:        http://localhost:8080/rate-management/actuator/health"
    echo "• Analytics Engine:       http://localhost:8080/analytics-engine/actuator/health"
fi

echo ""
echo -e "${YELLOW}� Management Commands:${NC}"
echo "• Stop all services:      ./stop-infrastructure.sh"
echo "• View service logs:      tail -f <service-dir>/logs/<service-name>.log"
echo "• Restart single service: $0 --service <service-name> --force"
echo "• Infrastructure only:    $0 --infrastructure-only"
echo ""
echo -e "${BLUE}💡 All logs are available in the respective service logs/ directories${NC}"

if [ ${#FAILED_SERVICES[@]} -ne 0 ]; then
    exit 1
fi
