#!/bin/bash

# Modern Reservation System - Main Development Control Script
# Single entry point for all development operations

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
NC='\033[0m' # No Color

# Base directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SCRIPTS_DIR="$SCRIPT_DIR/scripts"

# Print banner
print_banner() {
    echo -e "${CYAN}"
    cat << 'EOF'
╔══════════════════════════════════════════════════════════════════╗
║            Modern Reservation System - Dev Control              ║
║                    Single Entry Point for All Operations         ║
╚══════════════════════════════════════════════════════════════════╝
EOF
    echo -e "${NC}"
}

# Show help
show_help() {
    print_banner
    echo -e "${YELLOW}Usage:${NC} $0 <command> [options]"
    echo ""
    echo -e "${CYAN}═══════════════════════════════════════════════════════════════${NC}"
    echo -e "${GREEN}🚀 Service Management Commands:${NC}"
    echo -e "${CYAN}═══════════════════════════════════════════════════════════════${NC}"
    echo ""
    echo -e "  ${GREEN}start${NC}                    Start all services (infrastructure + business)"
    echo -e "  ${GREEN}start-infra${NC}              Start infrastructure services only"
    echo -e "  ${GREEN}start-business${NC}           Start business services only"
    echo -e "  ${GREEN}start --restart${NC}          Force restart all services"
    echo ""
    echo -e "  ${RED}stop${NC}                     Stop all services"
    echo -e "  ${RED}stop-infra${NC}               Stop infrastructure services"
    echo -e "  ${RED}stop-business${NC}            Stop business services"
    echo ""
    echo -e "  ${BLUE}status${NC}                   Check status of all services"
    echo -e "  ${BLUE}status-infra${NC}             Check infrastructure services status"
    echo -e "  ${BLUE}status-business${NC}          Check business services status"
    echo ""
    echo -e "${CYAN}═══════════════════════════════════════════════════════════════${NC}"
    echo -e "${MAGENTA}🔄 Clean Restart Commands:${NC}"
    echo -e "${CYAN}═══════════════════════════════════════════════════════════════${NC}"
    echo ""
    echo -e "  ${MAGENTA}clean${NC}                    Full clean restart (removes all data)"
    echo -e "  ${MAGENTA}clean --keep-data${NC}        Clean restart but keep database data"
    echo -e "  ${MAGENTA}clean --skip-maven${NC}       Clean restart without Maven rebuild"
    echo -e "  ${MAGENTA}clean --skip-db${NC}          Clean restart without database init"
    echo -e "  ${MAGENTA}clean --help${NC}             Show all clean restart options"
    echo ""
    echo -e "${CYAN}═══════════════════════════════════════════════════════════════${NC}"
    echo -e "${YELLOW}🔨 Build Commands:${NC}"
    echo -e "${CYAN}═══════════════════════════════════════════════════════════════${NC}"
    echo ""
    echo -e "  ${YELLOW}build${NC}                    Build all services (Maven compile)"
    echo -e "  ${YELLOW}build --clean${NC}            Clean build all services"
    echo -e "  ${YELLOW}build --test${NC}             Build and run tests"
    echo -e "  ${YELLOW}build-business${NC}           Build only business services"
    echo ""
    echo -e "${CYAN}═══════════════════════════════════════════════════════════════${NC}"
    echo -e "${YELLOW}🧪 Testing & Verification:${NC}"
    echo -e "${CYAN}═══════════════════════════════════════════════════════════════${NC}"
    echo ""
    echo -e "  ${YELLOW}test-avro${NC}                Test Avro event publishing"
    echo -e "  ${YELLOW}check-deps${NC}               Check all dependencies installed"
    echo -e "  ${YELLOW}check-health${NC}             Comprehensive health check"
    echo ""
    echo -e "${CYAN}═══════════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}💾 Database Operations:${NC}"
    echo -e "${CYAN}═══════════════════════════════════════════════════════════════${NC}"
    echo ""
    echo -e "  ${BLUE}db-setup${NC}                 Initialize database schema"
    echo -e "  ${BLUE}db-backup${NC}                Backup database"
    echo -e "  ${BLUE}db-connect${NC}               Connect to database (psql)"
    echo -e "  ${BLUE}redis-connect${NC}            Connect to Redis (redis-cli)"
    echo -e "  ${BLUE}kafka-topics${NC}             List Kafka topics"
    echo ""
    echo -e "${CYAN}═══════════════════════════════════════════════════════════════${NC}"
    echo -e "${CYAN}🐳 Docker Operations:${NC}"
    echo -e "${CYAN}═══════════════════════════════════════════════════════════════${NC}"
    echo ""
    echo -e "  ${CYAN}docker-start${NC}             Start complete Docker stack (infrastructure + observability)"
    echo -e "  ${CYAN}docker-stop${NC}              Stop Docker stack"
    echo -e "  ${CYAN}docker-status${NC}            Check Docker services status with credentials"
    echo -e "  ${CYAN}docker-logs [service]${NC}    View Docker service logs"
    echo -e "  ${CYAN}docker-clean${NC}             🧹 COMPLETE Docker cleanup (containers, images, volumes, networks)"
    echo ""
    echo -e "${CYAN}═══════════════════════════════════════════════════════════════${NC}"
    echo -e "${GREEN}📊 Monitoring & Logs:${NC}"
    echo -e "${CYAN}═══════════════════════════════════════════════════════════════${NC}"
    echo ""
    echo -e "  ${GREEN}logs [service]${NC}           View service logs"
    echo -e "  ${GREEN}ui-kafka${NC}                 Open Kafka UI"
    echo -e "  ${GREEN}ui-eureka${NC}                Open Eureka Dashboard"
    echo -e "  ${GREEN}ui-jaeger${NC}                Open Jaeger Tracing UI"
    echo -e "  ${GREEN}ui-prometheus${NC}            Open Prometheus Metrics"
    echo -e "  ${GREEN}ui-grafana${NC}               Open Grafana Dashboards"
    echo -e "  ${GREEN}ui-pgadmin${NC}               Open PgAdmin"
    echo ""
    echo -e "${CYAN}═══════════════════════════════════════════════════════════════${NC}"
    echo -e "${YELLOW}🎨 Code Quality & Formatting:${NC}"
    echo -e "${CYAN}═══════════════════════════════════════════════════════════════${NC}"
    echo ""
    echo -e "  ${YELLOW}format${NC}                   Format all code with Biome"
    echo -e "  ${YELLOW}format-check${NC}             Check code formatting"
    echo -e "  ${YELLOW}lint${NC}                     Lint all code with Biome"
    echo -e "  ${YELLOW}lint-fix${NC}                 Lint and fix all code"
    echo -e "  ${YELLOW}check${NC}                    Run Biome format + lint checks"
    echo -e "  ${YELLOW}check-fix${NC}                Run Biome format + lint with fixes"
    echo -e "  ${YELLOW}biome-status${NC}             Show Biome installation status"
    echo -e "  ${YELLOW}biome-setup${NC}              Setup/reinstall Biome in all projects"
    echo ""
    echo -e "${CYAN}═══════════════════════════════════════════════════════════════${NC}"
    echo -e "${YELLOW}💡 Quick Examples:${NC}"
    echo -e "${CYAN}═══════════════════════════════════════════════════════════════${NC}"
    echo ""
    echo -e "  ${YELLOW}# Build all services${NC}"
    echo -e "  ./dev.sh build"
    echo ""
    echo -e "  ${YELLOW}# Clean build with tests${NC}"
    echo -e "  ./dev.sh build --clean --test"
    echo ""
    echo -e "  ${GREEN}# Quick start everything${NC}"
    echo -e "  ./dev.sh start"
    echo ""
    echo -e "  ${MAGENTA}# Full clean restart for testing${NC}"
    echo -e "  ./dev.sh clean"
    echo ""
    echo -e "  ${YELLOW}# Format and fix all code${NC}"
    echo -e "  ./dev.sh check-fix"
    echo ""
    echo -e "  ${BLUE}# Check what's running${NC}"
    echo -e "  ./dev.sh status"
    echo ""
    echo -e "  ${RED}# Stop everything${NC}"
    echo -e "  ./dev.sh stop"
    echo ""
    echo -e "${CYAN}═══════════════════════════════════════════════════════════════${NC}"
    echo ""
}

# Execute command
case "${1:-help}" in
    # ========================================================================
    # SERVICE MANAGEMENT
    # ========================================================================
    "start"|"start-all")
        echo -e "${GREEN}🚀 Starting all services...${NC}"
        exec "$SCRIPTS_DIR/infra.sh" start-all "${@:2}"
        ;;
    "start-infra"|"start-infrastructure")
        echo -e "${GREEN}🚀 Starting infrastructure services...${NC}"
        exec "$SCRIPTS_DIR/infra.sh" start "${@:2}"
        ;;
    "start-business")
        echo -e "${GREEN}🚀 Starting business services...${NC}"
        exec "$SCRIPTS_DIR/infra.sh" start-business "${@:2}"
        ;;

    "stop"|"stop-all")
        echo -e "${RED}🛑 Stopping all services...${NC}"
        exec "$SCRIPTS_DIR/infra.sh" stop-all
        ;;
    "stop-infra"|"stop-infrastructure")
        echo -e "${RED}🛑 Stopping infrastructure services...${NC}"
        exec "$SCRIPTS_DIR/infra.sh" stop
        ;;
    "stop-business")
        echo -e "${RED}🛑 Stopping business services...${NC}"
        exec "$SCRIPTS_DIR/infra.sh" stop-business
        ;;

    "status"|"status-all"|"check")
        echo -e "${BLUE}📊 Checking all services status...${NC}"
        exec "$SCRIPTS_DIR/infra.sh" status-all
        ;;
    "status-infra"|"status-infrastructure")
        echo -e "${BLUE}📊 Checking infrastructure status...${NC}"
        exec "$SCRIPTS_DIR/infra.sh" status
        ;;
    "status-business")
        echo -e "${BLUE}📊 Checking business services status...${NC}"
        exec "$SCRIPTS_DIR/infra.sh" status-business
        ;;

    # ========================================================================
    # CLEAN RESTART
    # ========================================================================
    "clean"|"clean-restart"|"restart-clean")
        echo -e "${MAGENTA}🔄 Performing clean restart...${NC}"
        exec "$SCRIPTS_DIR/clean-restart.sh" "${@:2}"
        ;;

    # ========================================================================
    # BUILD COMMANDS
    # ========================================================================
    "build")
        cd "$SCRIPT_DIR/apps/backend/java-services"

        if [[ " ${@:2} " =~ " --clean " ]]; then
            echo -e "${YELLOW}🔨 Clean building all services...${NC}"
            if [[ " ${@:2} " =~ " --test " ]]; then
                mvn clean install
            else
                mvn clean compile
            fi
        elif [[ " ${@:2} " =~ " --test " ]]; then
            echo -e "${YELLOW}🔨 Building and testing all services...${NC}"
            mvn install
        else
            echo -e "${YELLOW}🔨 Building all services...${NC}"
            mvn compile
        fi
        ;;

    "build-business")
        cd "$SCRIPT_DIR/apps/backend/java-services"

        echo -e "${YELLOW}🔨 Building business services...${NC}"
        if [[ " ${@:2} " =~ " --clean " ]]; then
            mvn clean compile -pl business-services/reservation-engine,business-services/availability-calculator,business-services/rate-management,business-services/payment-processor,business-services/analytics-engine
        else
            mvn compile -pl business-services/reservation-engine,business-services/availability-calculator,business-services/rate-management,business-services/payment-processor,business-services/analytics-engine
        fi
        ;;

    # ========================================================================
    # CODE QUALITY & FORMATTING (BIOME)
    # ========================================================================
    "format")
        echo -e "${YELLOW}🎨 Formatting all code with Biome...${NC}"
        exec "$SCRIPTS_DIR/biome-management.sh" format-all
        ;;
    "format-check")
        echo -e "${YELLOW}🔍 Checking code formatting...${NC}"
        exec "$SCRIPTS_DIR/biome-management.sh" format-check
        ;;
    "lint")
        echo -e "${YELLOW}🔍 Linting all code with Biome...${NC}"
        exec "$SCRIPTS_DIR/biome-management.sh" lint-all
        ;;
    "lint-fix")
        echo -e "${YELLOW}🔧 Linting and fixing all code...${NC}"
        exec "$SCRIPTS_DIR/biome-management.sh" lint-fix
        ;;
    "check")
        echo -e "${YELLOW}✅ Running Biome checks (format + lint)...${NC}"
        exec "$SCRIPTS_DIR/biome-management.sh" check-all
        ;;
    "check-fix")
        echo -e "${YELLOW}🔧 Running Biome checks with fixes...${NC}"
        exec "$SCRIPTS_DIR/biome-management.sh" fix-all
        ;;
    "biome-status")
        echo -e "${YELLOW}📊 Checking Biome installation status...${NC}"
        exec "$SCRIPTS_DIR/biome-management.sh" status
        ;;
    "biome-setup")
        echo -e "${YELLOW}🛠️  Setting up Biome in all projects...${NC}"
        exec "$SCRIPTS_DIR/biome-management.sh" setup
        ;;

    # ========================================================================
    # TESTING & VERIFICATION
    # ========================================================================
    "test-avro"|"avro-test")
        echo -e "${YELLOW}🧪 Testing Avro event publishing...${NC}"
        exec "$SCRIPTS_DIR/test-avro-event.sh"
        ;;
    "check-deps"|"check-dependencies"|"deps")
        echo -e "${YELLOW}🔍 Checking dependencies...${NC}"
        exec "$SCRIPTS_DIR/check-dependencies.sh"
        ;;
    "check-health"|"health")
        echo -e "${YELLOW}🏥 Comprehensive health check...${NC}"
        echo ""
        echo -e "${CYAN}=== Infrastructure Services ===${NC}"
        "$SCRIPTS_DIR/check-infrastructure.sh"
        echo ""
        echo -e "${CYAN}=== Business Services ===${NC}"
        exec "$SCRIPTS_DIR/check-business-services.sh"
        ;;

    # ========================================================================
    # DATABASE OPERATIONS
    # ========================================================================
    "db-setup"|"setup-db"|"init-db")
        echo -e "${BLUE}💾 Setting up database...${NC}"
        exec "$SCRIPTS_DIR/setup-database.sh"
        ;;
    "db-backup"|"backup-db")
        echo -e "${BLUE}💾 Backing up database...${NC}"
        exec "$SCRIPTS_DIR/backup-database.sh"
        ;;
    "db-connect"|"psql")
        echo -e "${BLUE}💾 Connecting to database...${NC}"
        docker exec -it modern-reservation-postgres psql -U postgres -d modern_reservation_dev
        ;;
    "redis-connect"|"redis-cli")
        echo -e "${BLUE}🔴 Connecting to Redis...${NC}"
        docker exec -it modern-reservation-redis redis-cli
        ;;
    "kafka-topics")
        echo -e "${BLUE}📡 Listing Kafka topics...${NC}"
        docker exec modern-reservation-kafka kafka-topics --bootstrap-server localhost:9092 --list
        ;;

    # ========================================================================
    # DOCKER OPERATIONS
    # ========================================================================
    "docker-start"|"docker-up")
        echo -e "${CYAN}🐳 Starting complete Docker stack (infrastructure + observability)...${NC}"
        cd "$SCRIPT_DIR/infrastructure/docker"
        docker compose -f docker-compose.yml up -d postgres redis kafka schema-registry kafka-ui pgadmin otel-collector jaeger prometheus grafana
        echo -e "${GREEN}✅ Complete Docker stack started!${NC}"
        echo -e "${CYAN}Access points:${NC}"
        echo -e "  🐘 PostgreSQL: localhost:5432"
        echo -e "  🔴 Redis: localhost:6379"
        echo -e "  📊 Kafka UI: http://localhost:8090"
        echo -e "  🗄️  PgAdmin: http://localhost:5050"
        echo -e "  📊 Jaeger: http://localhost:16686"
        echo -e "  📈 Prometheus: http://localhost:9090"
        echo -e "  📊 Grafana: http://localhost:3000 (admin/admin123)"
        ;;
    "docker-stop"|"docker-down")
        echo -e "${CYAN}🐳 Stopping Docker stack...${NC}"
        cd "$SCRIPT_DIR/infrastructure/docker"
        docker compose -f docker-compose.yml down
        echo -e "${GREEN}✅ Docker stack stopped!${NC}"
        ;;
    "docker-status")
        echo -e "${CYAN}🐳 Checking Docker status...${NC}"
        exec "$SCRIPTS_DIR/docker-infra.sh" health
        ;;
    "docker-logs")
        echo -e "${CYAN}🐳 Viewing Docker logs...${NC}"
        exec "$SCRIPTS_DIR/docker-infra.sh" logs "$2"
        ;;
    "docker-clean"|"docker-cleanup")
        echo -e "${CYAN}🐳 Performing comprehensive Docker cleanup...${NC}"
        echo -e "${YELLOW}This will remove ALL containers, images, volumes, and networks!${NC}"

        # Step 1: Stop and remove compose services
        echo -e "${BLUE}Step 1/7: Stopping Docker Compose services...${NC}"
        cd "$SCRIPT_DIR/infrastructure/docker"
        docker compose -f docker-compose.yml down -v --remove-orphans 2>/dev/null || true

        # Step 2: Remove all containers
        echo -e "${BLUE}Step 2/7: Removing all containers...${NC}"
        docker container prune -f

        # Step 3: Remove all images
        echo -e "${BLUE}Step 3/7: Removing all images...${NC}"
        docker image prune -a -f

        # Step 4: Remove all networks
        echo -e "${BLUE}Step 4/7: Removing unused networks...${NC}"
        docker network prune -f

        # Step 5: Current state verification
        echo -e "${BLUE}Step 5/7: Current Docker state...${NC}"
        echo -e "${CYAN}=== CONTAINERS ===${NC}"
        docker ps -a
        echo -e "${CYAN}=== IMAGES ===${NC}"
        docker images
        echo -e "${CYAN}=== VOLUMES ===${NC}"
        docker volume ls
        echo -e "${CYAN}=== NETWORKS ===${NC}"
        docker network ls

        # Step 6: Remove all volumes
        echo -e "${BLUE}Step 6/7: Removing all volumes...${NC}"
        docker volume prune -f

        # Remove specific named volumes if they exist
        echo -e "${BLUE}Removing legacy named volumes...${NC}"
        docker volume rm docker_kafka_data docker_pgadmin_data docker_postgres_data 2>/dev/null || true

        # Step 7: Final verification
        echo -e "${BLUE}Step 7/7: Final verification...${NC}"
        echo -e "${GREEN}🧹 DOCKER COMPLETELY FLUSHED - FRESH SLATE READY${NC}"
        echo ""
        echo -e "${CYAN}=== FINAL VERIFICATION ===${NC}"
        echo -e "Containers: $(docker ps -a --format '{{.Names}}' | wc -l)"
        echo -e "Images: $(docker images --format '{{.Repository}}' | wc -l)"
        echo -e "Volumes: $(docker volume ls --format '{{.Name}}' | wc -l)"
        echo -e "Networks: $(docker network ls --format '{{.Name}}' | grep -v -E '^(bridge|host|none)$' | wc -l)"
        echo ""
        echo -e "${GREEN}✅ Complete Docker cleanup finished! Ready for fresh start.${NC}"
        ;;

    # ========================================================================
    # MONITORING & LOGS
    # ========================================================================
    "logs")
        if [ -z "$2" ]; then
            echo -e "${YELLOW}Available services:${NC}"
            echo "  - reservation-engine"
            echo "  - config-server"
            echo "  - eureka-server"
            echo "  - gateway-service"
            echo "  - kafka"
            echo "  - postgres"
            echo "  - redis"
            echo ""
            echo "Usage: $0 logs <service-name>"
            exit 0
        fi

        # Check if it's a Docker service
        if docker ps --format '{{.Names}}' | grep -q "modern-reservation-$2"; then
            docker logs -f "modern-reservation-$2"
        elif [ -f "/tmp/$2.log" ]; then
            tail -f "/tmp/$2.log"
        else
            echo -e "${RED}Service logs not found: $2${NC}"
            exit 1
        fi
        ;;

    "ui-kafka"|"kafka-ui")
        echo -e "${GREEN}📊 Opening Kafka UI...${NC}"
        echo "http://localhost:8090"
        xdg-open http://localhost:8090 2>/dev/null || open http://localhost:8090 2>/dev/null || echo "Please open http://localhost:8090 in your browser"
        ;;
    "ui-eureka"|"eureka")
        echo -e "${GREEN}📊 Opening Eureka Dashboard...${NC}"
        echo "http://localhost:8761"
        xdg-open http://localhost:8761 2>/dev/null || open http://localhost:8761 2>/dev/null || echo "Please open http://localhost:8761 in your browser"
        ;;
    "ui-jaeger"|"jaeger")
        echo -e "${GREEN}📊 Opening Jaeger UI...${NC}"
        echo "http://localhost:16686"
        xdg-open http://localhost:16686 2>/dev/null || open http://localhost:16686 2>/dev/null || echo "Please open http://localhost:16686 in your browser"
        ;;
    "ui-prometheus"|"prometheus")
        echo -e "${GREEN}📊 Opening Prometheus...${NC}"
        echo "http://localhost:9090"
        xdg-open http://localhost:9090 2>/dev/null || open http://localhost:9090 2>/dev/null || echo "Please open http://localhost:9090 in your browser"
        ;;
    "ui-grafana"|"grafana")
        echo -e "${GREEN}📊 Opening Grafana...${NC}"
        echo "http://localhost:3000 (admin/admin123)"
        xdg-open http://localhost:3000 2>/dev/null || open http://localhost:3000 2>/dev/null || echo "Please open http://localhost:3000 in your browser"
        ;;
    "ui-pgadmin"|"pgadmin")
        echo -e "${GREEN}📊 Opening PgAdmin...${NC}"
        echo "http://localhost:5050"
        xdg-open http://localhost:5050 2>/dev/null || open http://localhost:5050 2>/dev/null || echo "Please open http://localhost:5050 in your browser"
        ;;

    # ========================================================================
    # HELP
    # ========================================================================
    "help"|"-h"|"--help"|"")
        show_help
        ;;

    # ========================================================================
    # UNKNOWN COMMAND
    # ========================================================================
    *)
        echo -e "${RED}❌ Unknown command: '$1'${NC}"
        echo ""
        echo "Run '$0 help' to see available commands"
        exit 1
        ;;
esac
