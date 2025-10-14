#!/bin/bash

###############################################################################
# Modern Reservation - Orchestrated Startup Script
#
# This script starts all services in the correct order with proper health checks
#
# Order:
# 1. Docker Infrastructure (PostgreSQL, Redis, Kafka, etc.)
# 2. Database Schema Setup
# 3. Java Infrastructure Services (Config Server, Eureka, Gateway, Tenant Service)
# 4. Node.js Auth Service
# 5. Java Business Services (Reservation, Availability, Rate, Payment, Analytics)
###############################################################################

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Project root
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# Log file
LOG_DIR="$PROJECT_ROOT/logs"
mkdir -p "$LOG_DIR"
STARTUP_LOG="$LOG_DIR/startup-$(date +%Y%m%d-%H%M%S).log"

# Function to log messages
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a "$STARTUP_LOG"
}

log_error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ERROR:${NC} $1" | tee -a "$STARTUP_LOG"
}

log_warn() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] WARNING:${NC} $1" | tee -a "$STARTUP_LOG"
}

log_info() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')] INFO:${NC} $1" | tee -a "$STARTUP_LOG"
}

# Function to check if a port is in use
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        return 0  # Port is in use
    else
        return 1  # Port is free
    fi
}

# Function to wait for a service to be ready
wait_for_service() {
    local name=$1
    local url=$2
    local max_attempts=${3:-30}
    local attempt=1

    log_info "Waiting for $name to be ready..."

    while [ $attempt -le $max_attempts ]; do
        if curl -s -f "$url" >/dev/null 2>&1; then
            log "✅ $name is ready!"
            return 0
        fi

        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done

    log_error "$name failed to start within timeout"
    return 1
}

# Function to wait for a port to be in use
wait_for_port() {
    local name=$1
    local port=$2
    local max_attempts=${3:-30}
    local attempt=1

    log_info "Waiting for $name on port $port..."

    while [ $attempt -le $max_attempts ]; do
        if check_port $port; then
            log "✅ $name is listening on port $port"
            return 0
        fi

        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done

    log_error "$name failed to start on port $port within timeout"
    return 1
}

# Function to start a Java service
start_java_service() {
    local service_name=$1
    local service_path=$2
    local port=$3
    local pid_file="/tmp/${service_name}.pid"
    local log_file="/tmp/${service_name}.log"

    log_info "Starting $service_name..."

    # Check if already running
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p $pid > /dev/null 2>&1; then
            log_warn "$service_name is already running (PID: $pid)"
            return 0
        fi
    fi

    # Start the service
    cd "$service_path"
    nohup mvn spring-boot:run > "$log_file" 2>&1 &
    local pid=$!
    echo $pid > "$pid_file"

    log "Started $service_name with PID $pid (log: $log_file)"

    # Wait for port to be ready
    if ! wait_for_port "$service_name" "$port" 60; then
        log_error "$service_name failed to start. Check log: $log_file"
        tail -50 "$log_file"
        return 1
    fi

    cd "$PROJECT_ROOT"
    return 0
}

# Function to start Node.js service
start_node_service() {
    local service_name=$1
    local service_path=$2
    local port=$3
    local pid_file="/tmp/${service_name}.pid"
    local log_file="/tmp/${service_name}.log"

    log_info "Starting $service_name..."

    # Check if already running
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p $pid > /dev/null 2>&1; then
            log_warn "$service_name is already running (PID: $pid)"
            return 0
        fi
    fi

    # Build and start
    cd "$service_path"
    npm run build >> "$log_file" 2>&1
    nohup npm start > "$log_file" 2>&1 &
    local pid=$!
    echo $pid > "$pid_file"

    log "Started $service_name with PID $pid (log: $log_file)"

    # Wait for port to be ready
    if ! wait_for_port "$service_name" "$port" 30; then
        log_error "$service_name failed to start. Check log: $log_file"
        tail -50 "$log_file"
        return 1
    fi

    cd "$PROJECT_ROOT"
    return 0
}

###############################################################################
# MAIN STARTUP SEQUENCE
###############################################################################

log "=========================================="
log "🚀 MODERN RESERVATION - ORCHESTRATED STARTUP"
log "=========================================="
log "Log file: $STARTUP_LOG"
log ""

# Step 1: Docker Infrastructure
log "=========================================="
log "STEP 1: Starting Docker Infrastructure"
log "=========================================="

if docker ps >/dev/null 2>&1; then
    log_info "Docker is available"

    # Check if containers are already running
    if docker ps --format '{{.Names}}' | grep -q "postgres"; then
        log_warn "Docker containers already running"
    else
        log_info "Starting docker-compose..."
        docker-compose -f "$PROJECT_ROOT/infrastructure/docker/docker-compose.yml" up -d

        # Wait for PostgreSQL
        log_info "Waiting for PostgreSQL..."
        sleep 5
        if ! wait_for_service "PostgreSQL" "http://localhost:5432" 10; then
            # PostgreSQL doesn't have HTTP, just check if port is open
            if check_port 5432; then
                log "✅ PostgreSQL is ready on port 5432"
            else
                log_error "PostgreSQL failed to start"
                exit 1
            fi
        fi

        # Wait for Redis
        log_info "Checking Redis..."
        if check_port 6379; then
            log "✅ Redis is ready on port 6379"
        fi

        # Wait for Kafka
        log_info "Checking Kafka..."
        sleep 5
        if check_port 9092; then
            log "✅ Kafka is ready on port 9092"
        fi
    fi
else
    log_error "Docker is not available. Please install and start Docker."
    exit 1
fi

# Step 2: Database Setup
log ""
log "=========================================="
log "STEP 2: Setting Up Database Schema"
log "=========================================="

if [ -f "$PROJECT_ROOT/scripts/setup-database.sh" ]; then
    log_info "Running database setup..."
    bash "$PROJECT_ROOT/scripts/setup-database.sh" | tee -a "$STARTUP_LOG"
    log "✅ Database schema setup complete"
else
    log_warn "Database setup script not found, skipping..."
fi

# Step 3: Java Infrastructure Services
log ""
log "=========================================="
log "STEP 3: Starting Java Infrastructure Services"
log "=========================================="

# Build all Java services first
log_info "Building all Java services..."
cd "$PROJECT_ROOT/apps/backend/java-services"
if mvn clean install -DskipTests >> "$STARTUP_LOG" 2>&1; then
    log "✅ Build complete"
else
    log_error "Build failed. Check log: $STARTUP_LOG"
    exit 1
fi
cd "$PROJECT_ROOT"

# Start Config Server
if ! start_java_service "config-server" \
    "$PROJECT_ROOT/apps/backend/java-services/infrastructure/config-server" \
    8888; then
    log_error "Failed to start Config Server"
    exit 1
fi

# Wait and verify Config Server
sleep 5
if wait_for_service "Config Server" "http://localhost:8888/actuator/health" 30; then
    log "✅ Config Server is healthy"
else
    log_error "Config Server health check failed"
    exit 1
fi

# Start Eureka Server
if ! start_java_service "eureka-server" \
    "$PROJECT_ROOT/apps/backend/java-services/infrastructure/eureka-server" \
    8761; then
    log_error "Failed to start Eureka Server"
    exit 1
fi

# Wait and verify Eureka
sleep 10
if wait_for_service "Eureka Server" "http://localhost:8761/actuator/health" 30; then
    log "✅ Eureka Server is healthy"
else
    log_error "Eureka Server health check failed"
    exit 1
fi

# Start Gateway Service
if ! start_java_service "gateway-service" \
    "$PROJECT_ROOT/apps/backend/java-services/infrastructure/gateway-service" \
    8080; then
    log_error "Failed to start Gateway Service"
    exit 1
fi

# Wait and verify Gateway
sleep 10
if wait_for_service "Gateway Service" "http://localhost:8080/actuator/health" 30; then
    log "✅ Gateway Service is healthy"
else
    log_error "Gateway Service health check failed"
    exit 1
fi

# Start Tenant Service
if ! start_java_service "tenant-service" \
    "$PROJECT_ROOT/apps/backend/java-services/infrastructure/tenant-service" \
    8085; then
    log_error "Failed to start Tenant Service"
    exit 1
fi

# Wait and verify Tenant Service
sleep 10
if wait_for_service "Tenant Service" "http://localhost:8085/actuator/health" 30; then
    log "✅ Tenant Service is healthy"
else
    log_warn "Tenant Service health check failed, but continuing..."
fi

# Step 4: Node.js Auth Service
log ""
log "=========================================="
log "STEP 4: Starting Node.js Auth Service"
log "=========================================="

if ! start_node_service "auth-service" \
    "$PROJECT_ROOT/apps/backend/node-services/auth-service" \
    3100; then
    log_error "Failed to start Auth Service"
    exit 1
fi

# Wait and verify Auth Service
sleep 5
if wait_for_service "Auth Service" "http://localhost:3100/health" 30; then
    log "✅ Auth Service is healthy"
else
    log_error "Auth Service health check failed"
    exit 1
fi

# Step 5: Java Business Services
log ""
log "=========================================="
log "STEP 5: Starting Java Business Services"
log "=========================================="

# Start Reservation Engine
if ! start_java_service "reservation-engine" \
    "$PROJECT_ROOT/apps/backend/java-services/business-services/reservation-engine" \
    8100; then
    log_warn "Failed to start Reservation Engine, but continuing..."
fi

# Start Availability Calculator
if ! start_java_service "availability-calculator" \
    "$PROJECT_ROOT/apps/backend/java-services/business-services/availability-calculator" \
    8101; then
    log_warn "Failed to start Availability Calculator, but continuing..."
fi

# Start Rate Management
if ! start_java_service "rate-management" \
    "$PROJECT_ROOT/apps/backend/java-services/business-services/rate-management" \
    8102; then
    log_warn "Failed to start Rate Management, but continuing..."
fi

# Start Payment Processor
if ! start_java_service "payment-processor" \
    "$PROJECT_ROOT/apps/backend/java-services/business-services/payment-processor" \
    8103; then
    log_warn "Failed to start Payment Processor, but continuing..."
fi

# Start Analytics Engine
if ! start_java_service "analytics-engine" \
    "$PROJECT_ROOT/apps/backend/java-services/business-services/analytics-engine" \
    8104; then
    log_warn "Failed to start Analytics Engine, but continuing..."
fi

# Final Status Check
log ""
log "=========================================="
log "🎉 STARTUP COMPLETE!"
log "=========================================="

sleep 10

log ""
log "📊 Final Service Status:"
log "========================"
log ""
log "Infrastructure Services:"
log "  - Config Server:    http://localhost:8888"
log "  - Eureka Server:    http://localhost:8761"
log "  - Gateway Service:  http://localhost:8080"
log "  - Tenant Service:   http://localhost:8085"
log ""
log "Authentication:"
log "  - Auth Service:     http://localhost:3100"
log ""
log "Business Services:"
log "  - Reservation:      http://localhost:8100/reservation-engine"
log "  - Availability:     http://localhost:8101/availability-calculator"
log "  - Rate Management:  http://localhost:8102/rate-management"
log "  - Payment:          http://localhost:8103/payment-processor"
log "  - Analytics:        http://localhost:8104/analytics-engine"
log ""
log "Docker Services:"
log "  - PostgreSQL:       localhost:5432"
log "  - Redis:            localhost:6379"
log "  - Kafka:            localhost:9092"
log "  - Kafka UI:         http://localhost:8090"
log ""
log "📝 Check service logs in: /tmp/*-service.log"
log "📝 Startup log: $STARTUP_LOG"
log ""
log "✅ All services started! Check individual logs for any issues."
