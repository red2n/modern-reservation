#!/bin/bash

# Modern Reservation System - Docker Infrastructure Management
# This script manages external infrastructure services via Docker

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
DOCKER_DIR="$BASE_DIR/infrastructure/docker"
COMPOSE_INFRA="$DOCKER_DIR/docker-compose-infrastructure.yml"
COMPOSE_SERVICES="$DOCKER_DIR/docker-compose-services.yml"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging function
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1" >&2
}

warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

# Help function
show_help() {
    echo "Modern Reservation System - Docker Infrastructure Management"
    echo ""
    echo "Usage: $0 <command> [options]"
    echo ""
    echo "Infrastructure Commands (External Services):"
    echo "  infra-start     Start all infrastructure services (Zipkin, PostgreSQL, Redis)"
    echo "  infra-stop      Stop all infrastructure services"
    echo "  infra-restart   Restart all infrastructure services"
    echo "  infra-status    Show status of infrastructure services"
    echo ""
    echo "Service Commands (Your Applications):"
    echo "  services-start  Start all application services"
    echo "  services-stop   Stop all application services"
    echo "  services-build  Build and start application services"
    echo ""
    echo "Combined Commands:"
    echo "  start-all       Start infrastructure + services"
    echo "  stop-all        Stop everything"
    echo "  restart-all     Restart everything"
    echo ""
    echo "Individual Services:"
    echo "  zipkin          Start only Zipkin"
    echo "  postgres        Start only PostgreSQL"
    echo "  redis           Start only Redis"
    echo ""
    echo "Utility Commands:"
    echo "  logs [service]  Show logs for service"
    echo "  ps              Show running containers"
    echo "  cleanup         Remove stopped containers and unused images"
    echo "  health          Check health of all services"
    echo ""
    echo "Examples:"
    echo "  $0 infra-start              # Start Zipkin, PostgreSQL, Redis"
    echo "  $0 zipkin                   # Start only Zipkin"
    echo "  $0 logs zipkin              # Show Zipkin logs"
    echo "  $0 health                   # Check all services"
}

# Create Docker network if it doesn't exist
ensure_network() {
    if ! docker network ls | grep -q "modern-reservation-network"; then
        log "Creating Docker network: modern-reservation-network"
        docker network create modern-reservation-network
    fi
}

# Infrastructure commands
start_infrastructure() {
    log "Starting infrastructure services..."
    ensure_network
    cd "$DOCKER_DIR"
    docker compose -f docker-compose-infrastructure.yml up -d
    log "Infrastructure services started!"
    log "Access points:"
    log "  📊 Zipkin UI: http://localhost:9411"
    log "  🐘 PostgreSQL: localhost:5432"
    log "  🔴 Redis: localhost:6379"
}

stop_infrastructure() {
    log "Stopping infrastructure services..."
    cd "$DOCKER_DIR"
    docker compose -f docker-compose-infrastructure.yml down
    log "Infrastructure services stopped!"
}

# Individual service commands
start_zipkin() {
    log "Starting Zipkin service..."
    ensure_network
    cd "$DOCKER_DIR"
    docker compose -f docker-compose-infrastructure.yml up -d zipkin
    log "Zipkin started! Access at: http://localhost:9411"
}

start_postgres() {
    log "Starting PostgreSQL service..."
    ensure_network
    cd "$DOCKER_DIR"
    docker compose -f docker-compose-infrastructure.yml up -d postgres
    log "PostgreSQL started! Access at: localhost:5432"
}

start_redis() {
    log "Starting Redis service..."
    ensure_network
    cd "$DOCKER_DIR"
    docker compose -f docker-compose-infrastructure.yml up -d redis
    log "Redis started! Access at: localhost:6379"
}

# Health check
check_health() {
    log "Checking service health..."

    # Check Zipkin
    if curl -s -f http://localhost:9411/api/v2/services > /dev/null 2>&1 || curl -s -f http://localhost:9411/health > /dev/null 2>&1; then
        echo -e "  ${GREEN}✅ Zipkin${NC} - http://localhost:9411"
    else
        echo -e "  ${RED}❌ Zipkin${NC} - Not responding"
    fi

    # Check PostgreSQL
    if sudo docker exec modern-reservation-postgres pg_isready -U postgres > /dev/null 2>&1; then
        echo -e "  ${GREEN}✅ PostgreSQL${NC} - localhost:5432"
    else
        echo -e "  ${RED}❌ PostgreSQL${NC} - Not responding"
    fi

    # Check Redis
    if sudo docker exec modern-reservation-redis redis-cli ping > /dev/null 2>&1; then
        echo -e "  ${GREEN}✅ Redis${NC} - localhost:6379"
    else
        echo -e "  ${RED}❌ Redis${NC} - Not responding"
    fi
}

# Show logs
show_logs() {
    local service="$1"
    if [ -z "$service" ]; then
        cd "$DOCKER_DIR"
        docker compose -f docker-compose-infrastructure.yml logs -f
    else
        cd "$DOCKER_DIR"
        docker compose -f docker-compose-infrastructure.yml logs -f "$service"
    fi
}

# Show running containers
show_containers() {
    docker ps --filter "name=modern-reservation" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
}

# Main command handling
case "$1" in
    "infra-start"|"infrastructure")
        start_infrastructure
        ;;
    "infra-stop")
        stop_infrastructure
        ;;
    "infra-restart")
        stop_infrastructure
        start_infrastructure
        ;;
    "zipkin")
        start_zipkin
        ;;
    "postgres"|"postgresql")
        start_postgres
        ;;
    "redis")
        start_redis
        ;;
    "health"|"status")
        check_health
        ;;
    "logs")
        show_logs "$2"
        ;;
    "ps"|"containers")
        show_containers
        ;;
    "stop-all")
        stop_infrastructure
        ;;
    "cleanup")
        docker container prune -f
        docker image prune -f
        log "Cleanup completed!"
        ;;
    "help"|"-h"|"--help"|"")
        show_help
        ;;
    *)
        error "Unknown command: $1"
        echo ""
        show_help
        exit 1
        ;;
esac
