#!/bin/bash

# Modern Reservation System - Docker Infrastructure Management
# This script manages external infrastructure services via Docker

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
DOCKER_DIR="$BASE_DIR/infrastructure/docker"
COMPOSE_FILE="$DOCKER_DIR/docker-compose.yml"

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
    echo "  infra-start     Start all infrastructure services (PostgreSQL, Redis, Kafka)"
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
    echo "  observability   Start OpenTelemetry observability stack (Jaeger, Prometheus, Grafana)"
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
    echo "  $0 infra-start              # Start PostgreSQL, Redis, Kafka"
    echo "  $0 observability            # Start OpenTelemetry stack"
    echo "  $0 logs jaeger              # Show Jaeger logs"
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
    # Start infrastructure layer only (postgres, redis, kafka, schema-registry, kafka-ui, pgadmin)
    docker compose -f docker-compose.yml up -d postgres redis kafka schema-registry kafka-ui pgadmin
    log "Infrastructure services started!"
    log "Access points:"
    log "  � PostgreSQL: localhost:5432"
    log "  � Redis: localhost:6379"
    log "  📊 Kafka UI: http://localhost:8090"
    log "  �️  PgAdmin: http://localhost:5050"
}

stop_infrastructure() {
    log "Stopping infrastructure services..."
    cd "$DOCKER_DIR"
    # Stop infrastructure layer only
    docker compose -f docker-compose.yml stop postgres redis kafka schema-registry kafka-ui pgadmin
    log "Infrastructure services stopped!"
}

# Individual service commands
start_observability() {
    log "Starting observability services..."
    ensure_network
    cd "$DOCKER_DIR"
    docker compose -f docker-compose.yml up -d otel-collector jaeger prometheus grafana
    log "Observability services started!"
    log "Access points:"
    log "  📊 Jaeger UI: http://localhost:16686"
    log "  📈 Prometheus: http://localhost:9090"
    log "  📊 Grafana: http://localhost:3000 (admin/admin123)"
}

start_postgres() {
    log "Starting PostgreSQL service..."
    ensure_network
    cd "$DOCKER_DIR"
    docker compose -f docker-compose.yml up -d postgres
    log "PostgreSQL started! Access at: localhost:5432"
}

start_redis() {
    log "Starting Redis service..."
    ensure_network
    cd "$DOCKER_DIR"
    docker compose -f docker-compose.yml up -d redis
    log "Redis started! Access at: localhost:6379"
}

# Health check with credentials
check_health() {
    log "Checking service health with access information..."
    echo ""

    # Check Jaeger
    if curl -s -f http://localhost:16686/ > /dev/null 2>&1; then
        echo -e "  ${GREEN}✅ Jaeger${NC} - http://localhost:16686 ${BLUE}(No authentication required)${NC}"
    else
        echo -e "  ${RED}❌ Jaeger${NC} - Not responding"
    fi

    # Check Prometheus
    if curl -s -f http://localhost:9090/-/healthy > /dev/null 2>&1; then
        echo -e "  ${GREEN}✅ Prometheus${NC} - http://localhost:9090 ${BLUE}(No authentication required)${NC}"
    else
        echo -e "  ${RED}❌ Prometheus${NC} - Not responding"
    fi

    # Check Grafana
    if curl -s -f http://localhost:3000/api/health > /dev/null 2>&1; then
        echo -e "  ${GREEN}✅ Grafana${NC} - http://localhost:3000 ${YELLOW}(Login: admin/admin123)${NC}"
    else
        echo -e "  ${RED}❌ Grafana${NC} - Not responding"
    fi

    # Check PgAdmin (if running)
    if curl -s -f http://localhost:5050/misc/ping > /dev/null 2>&1; then
        echo -e "  ${GREEN}✅ PgAdmin${NC} - http://localhost:5050 ${YELLOW}(Login: admin@admin.com/admin)${NC}"
    elif docker ps --format '{{.Names}}' | grep -q "modern-reservation-pgadmin"; then
        echo -e "  ${YELLOW}⏳ PgAdmin${NC} - http://localhost:5050 ${YELLOW}(Starting up... Login: admin@admin.com/admin)${NC}"
    else
        echo -e "  ${RED}❌ PgAdmin${NC} - Not running"
    fi

    # Check Kafka UI (if running)
    if curl -s -f http://localhost:8090/actuator/health > /dev/null 2>&1 || curl -s -f http://localhost:8090/ > /dev/null 2>&1; then
        echo -e "  ${GREEN}✅ Kafka UI${NC} - http://localhost:8090 ${BLUE}(No authentication required)${NC}"
    elif docker ps --format '{{.Names}}' | grep -q "modern-reservation-kafka-ui"; then
        echo -e "  ${YELLOW}⏳ Kafka UI${NC} - http://localhost:8090 ${BLUE}(Starting up... No authentication required)${NC}"
    else
        echo -e "  ${RED}❌ Kafka UI${NC} - Not running"
    fi

    echo ""
    echo -e "${CYAN}── Database & Cache ──${NC}"

    # Check PostgreSQL
    if docker exec modern-reservation-postgres pg_isready -U postgres > /dev/null 2>&1; then
        echo -e "  ${GREEN}✅ PostgreSQL${NC} - localhost:5432 ${YELLOW}(User: postgres, Password: postgres)${NC}"
        echo -e "    ${BLUE}💻 Connect:${NC} docker exec -it modern-reservation-postgres psql -U postgres"
        echo -e "    ${BLUE}🔗 JDBC:${NC} jdbc:postgresql://localhost:5432/postgres"
    else
        echo -e "  ${RED}❌ PostgreSQL${NC} - Not responding"
    fi

    # Check Redis
    if docker exec modern-reservation-redis redis-cli ping > /dev/null 2>&1; then
        echo -e "  ${GREEN}✅ Redis${NC} - localhost:6379 ${BLUE}(No authentication required)${NC}"
        echo -e "    ${BLUE}💻 Connect:${NC} docker exec -it modern-reservation-redis redis-cli"
    else
        echo -e "  ${RED}❌ Redis${NC} - Not responding"
    fi

    echo ""
    echo -e "${CYAN}── Message Queue ──${NC}"

    # Check Kafka
    if docker exec modern-reservation-kafka kafka-broker-api-versions --bootstrap-server localhost:9092 > /dev/null 2>&1; then
        echo -e "  ${GREEN}✅ Kafka${NC} - localhost:9092 ${BLUE}(No authentication required)${NC}"
        echo -e "    ${BLUE}🔗 Bootstrap Servers:${NC} localhost:9092"
        echo -e "    ${BLUE}📡 External Access:${NC} localhost:9094"
    else
        echo -e "  ${RED}❌ Kafka${NC} - Not responding"
    fi

    # Check Schema Registry (if running)
    if curl -s -f http://localhost:8085/ > /dev/null 2>&1; then
        echo -e "  ${GREEN}✅ Schema Registry${NC} - http://localhost:8085 ${BLUE}(No authentication required)${NC}"
    elif docker ps --format '{{.Names}}' | grep -q "modern-reservation-schema-registry"; then
        echo -e "  ${YELLOW}⏳ Schema Registry${NC} - http://localhost:8085 ${BLUE}(Starting up...)${NC}"
    else
        echo -e "  ${RED}❌ Schema Registry${NC} - Not running"
    fi

    echo ""
}

# Show logs
show_logs() {
    local service="$1"
    if [ -z "$service" ]; then
        cd "$DOCKER_DIR"
        docker compose -f docker-compose.yml logs -f
    else
        cd "$DOCKER_DIR"
        docker compose -f docker-compose.yml logs -f "$service"
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
    "observability"|"monitoring")
        start_observability
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
