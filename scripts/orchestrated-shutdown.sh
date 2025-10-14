#!/bin/bash

###############################################################################
# Modern Reservation - Orchestrated Shutdown Script
#
# This script stops all services in reverse order
###############################################################################

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Project root
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

log_error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ERROR:${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] WARNING:${NC} $1"
}

# Function to stop a service by PID file
stop_service() {
    local service_name=$1
    local pid_file="/tmp/${service_name}.pid"

    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p $pid > /dev/null 2>&1; then
            log "Stopping $service_name (PID: $pid)..."
            kill $pid 2>/dev/null || true

            # Wait for process to stop
            local count=0
            while ps -p $pid > /dev/null 2>&1 && [ $count -lt 10 ]; do
                sleep 1
                count=$((count + 1))
            done

            # Force kill if still running
            if ps -p $pid > /dev/null 2>&1; then
                log_warn "Force killing $service_name..."
                kill -9 $pid 2>/dev/null || true
            fi

            log "✅ Stopped $service_name"
        else
            log_warn "$service_name PID file exists but process not running"
        fi
        rm -f "$pid_file"
    else
        log_warn "$service_name PID file not found"
    fi
}

log "=========================================="
log "🛑 MODERN RESERVATION - ORCHESTRATED SHUTDOWN"
log "=========================================="

# Stop business services first
log ""
log "Stopping Business Services..."
stop_service "analytics-engine"
stop_service "payment-processor"
stop_service "rate-management"
stop_service "availability-calculator"
stop_service "reservation-engine"

# Stop auth service
log ""
log "Stopping Auth Service..."
stop_service "auth-service"

# Stop infrastructure services
log ""
log "Stopping Infrastructure Services..."
stop_service "tenant-service"
stop_service "gateway-service"
stop_service "eureka-server"
stop_service "config-server"

# Stop Docker containers (optional)
log ""
read -p "Stop Docker containers? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    log "Stopping Docker containers..."
    cd "$PROJECT_ROOT"
    docker-compose -f infrastructure/docker/docker-compose.yml down
    log "✅ Docker containers stopped"
else
    log "Docker containers left running"
fi

log ""
log "=========================================="
log "✅ SHUTDOWN COMPLETE!"
log "=========================================="
