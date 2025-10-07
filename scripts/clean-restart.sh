#!/bin/bash

# Modern Reservation System - Clean Restart Script
# This script performs a complete cleanup and restart from scratch
# Use this when you want to test everything with a fresh environment

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
NC='\033[0m' # No Color

# Base directory - script is now in scripts/ folder
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$BASE_DIR"

# Configuration
KEEP_DATA=${KEEP_DATA:-false}  # Set to true to keep database data
SKIP_MAVEN_CLEAN=${SKIP_MAVEN_CLEAN:-false}  # Set to true to skip Maven clean
SKIP_DATABASE_SETUP=${SKIP_DATABASE_SETUP:-false}  # Set to true to skip database initialization

# Print functions
print_header() {
    echo ""
    echo -e "${CYAN}╔════════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║${NC} ${MAGENTA}$1${NC}"
    echo -e "${CYAN}╚════════════════════════════════════════════════════════════════╝${NC}"
    echo ""
}

print_step() {
    echo -e "${BLUE}▶${NC} ${GREEN}$1${NC}"
}

print_substep() {
    echo -e "  ${YELLOW}→${NC} $1"
}

print_success() {
    echo -e "  ${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "  ${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "  ${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "  ${CYAN}ℹ  $1${NC}"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --keep-data)
            KEEP_DATA=true
            shift
            ;;
        --skip-maven-clean)
            SKIP_MAVEN_CLEAN=true
            shift
            ;;
        --skip-db-setup)
            SKIP_DATABASE_SETUP=true
            shift
            ;;
        --help|-h)
            echo "Modern Reservation System - Clean Restart Script"
            echo ""
            echo "Usage: $0 [options]"
            echo ""
            echo "Options:"
            echo "  --keep-data           Keep database data (don't remove volumes)"
            echo "  --skip-maven-clean    Skip Maven clean build"
            echo "  --skip-db-setup       Skip database initialization"
            echo "  --help, -h            Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0                              # Full clean restart"
            echo "  $0 --keep-data                  # Restart but keep database data"
            echo "  $0 --skip-maven-clean           # Restart without Maven clean"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Confirmation
print_header "🔄 CLEAN RESTART - Modern Reservation System"
echo -e "${YELLOW}This will:${NC}"
echo "  1. Stop all running services (Java + Docker)"
echo "  2. Remove all Docker containers and networks"
if [ "$KEEP_DATA" = false ]; then
    echo "  3. Remove all Docker volumes (⚠️  DATABASE DATA WILL BE LOST)"
else
    echo "  3. Keep Docker volumes (database data preserved)"
fi
if [ "$SKIP_MAVEN_CLEAN" = false ]; then
    echo "  4. Clean and rebuild Maven artifacts"
else
    echo "  4. Skip Maven clean (use existing builds)"
fi
echo "  5. Start all infrastructure services"
echo "  6. Wait for services to be healthy"
if [ "$SKIP_DATABASE_SETUP" = false ]; then
    echo "  7. Initialize database schema"
else
    echo "  7. Skip database initialization"
fi
echo "  8. Build and start reservation-engine"
echo "  9. Run Avro event tests"
echo ""
read -p "Continue? (y/N) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Aborted."
    exit 0
fi

# Start time
START_TIME=$(date +%s)

# ============================================================================
# STEP 1: STOP ALL SERVICES
# ============================================================================
print_header "📛 STEP 1: Stopping All Services"

print_step "Stopping business services..."
"$SCRIPT_DIR/infra.sh" stop-business 2>&1 | grep -E "✅|⚠️|Stopped|stopped" || true
print_success "Business services stopped"

print_step "Stopping infrastructure services..."
"$SCRIPT_DIR/infra.sh" stop 2>&1 | grep -E "✅|⚠️|Stopped|stopped" || true
print_success "Infrastructure services stopped"

print_step "Killing any remaining Spring Boot processes..."
pkill -9 -f "spring-boot:run" 2>/dev/null || true
pkill -9 -f "mvn.*spring-boot:run" 2>/dev/null || true
print_success "All Spring Boot processes killed"

# ============================================================================
# STEP 2: CLEAN DOCKER RESOURCES
# ============================================================================
print_header "🐳 STEP 2: Cleaning Docker Resources"

print_step "Stopping and removing Docker containers..."
cd "$BASE_DIR/infrastructure/docker"
if [ "$KEEP_DATA" = false ]; then
    docker compose -f docker-compose-infrastructure.yml down -v --remove-orphans 2>&1 | grep -v "^$" || true
    print_success "Containers, volumes, and networks removed"
else
    docker compose -f docker-compose-infrastructure.yml down --remove-orphans 2>&1 | grep -v "^$" || true
    print_success "Containers and networks removed (volumes kept)"
fi
cd "$BASE_DIR"

print_step "Removing orphaned containers..."
docker ps -a --filter "name=modern-reservation" -q | xargs -r docker rm -f 2>/dev/null || true
print_success "Orphaned containers removed"

# ============================================================================
# STEP 3: VERIFY PORTS ARE FREE
# ============================================================================
print_header "🔌 STEP 3: Verifying Ports"

print_step "Checking if required ports are free..."
PORTS=(5432 6379 8081 8085 8090 8761 8888 9092 9411)
PORTS_IN_USE=()

for port in "${PORTS[@]}"; do
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        PORTS_IN_USE+=($port)
        print_warning "Port $port is still in use"
        # Try to kill process on port
        PID=$(lsof -Pi :$port -sTCP:LISTEN -t 2>/dev/null)
        if [ ! -z "$PID" ]; then
            print_substep "Killing process $PID on port $port"
            kill -9 $PID 2>/dev/null || true
            sleep 1
        fi
    fi
done

# Check again
PORTS_IN_USE=()
for port in "${PORTS[@]}"; do
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        PORTS_IN_USE+=($port)
    fi
done

if [ ${#PORTS_IN_USE[@]} -eq 0 ]; then
    print_success "All required ports are free"
else
    print_error "Ports still in use: ${PORTS_IN_USE[*]}"
    echo ""
    echo -e "${YELLOW}Manual intervention required. Run:${NC}"
    for port in "${PORTS_IN_USE[@]}"; do
        echo "  lsof -i :$port"
    done
    exit 1
fi

# ============================================================================
# STEP 4: CLEAN MAVEN ARTIFACTS
# ============================================================================
if [ "$SKIP_MAVEN_CLEAN" = false ]; then
    print_header "🧹 STEP 4: Cleaning Maven Artifacts"

    print_step "Cleaning backend-utils (Avro schemas)..."
    cd "$BASE_DIR/libs/shared/backend-utils"
    mvn clean -q 2>&1 | grep -E "BUILD|ERROR" || true
    print_success "backend-utils cleaned"

    print_step "Rebuilding backend-utils with Avro..."
    mvn install -DskipTests -q 2>&1 | grep -E "BUILD|ERROR" || true
    if [ -f "target/backend-utils-1.0.0.jar" ]; then
        print_success "backend-utils built successfully"
        print_substep "Avro classes generated in target/generated-sources/avro/"
    else
        print_error "backend-utils build failed"
        exit 1
    fi

    print_step "Cleaning reservation-engine..."
    cd "$BASE_DIR/apps/backend/java-services/business-services/reservation-engine"
    mvn clean -q 2>&1 | grep -E "BUILD|ERROR" || true
    print_success "reservation-engine cleaned"

    cd "$BASE_DIR"
else
    print_header "⏭️  STEP 4: Skipping Maven Clean"
    print_info "Using existing Maven artifacts"
fi

# ============================================================================
# STEP 5: START INFRASTRUCTURE
# ============================================================================
print_header "🚀 STEP 5: Starting Infrastructure Services"

print_step "Starting Docker infrastructure..."
cd "$BASE_DIR/infrastructure/docker"
docker compose -f docker-compose-infrastructure.yml up -d 2>&1 | grep -v "^$" | head -20 || true
cd "$BASE_DIR"

print_substep "Waiting for services to initialize (30 seconds)..."
sleep 30

# ============================================================================
# STEP 6: VERIFY INFRASTRUCTURE HEALTH
# ============================================================================
print_header "🏥 STEP 6: Verifying Infrastructure Health"

print_step "Checking PostgreSQL..."
for i in {1..10}; do
    if docker exec modern-reservation-postgres pg_isready -U postgres >/dev/null 2>&1; then
        print_success "PostgreSQL is ready (localhost:5432)"
        break
    fi
    if [ $i -eq 10 ]; then
        print_error "PostgreSQL failed to start"
        exit 1
    fi
    sleep 2
done

print_step "Checking Redis..."
for i in {1..10}; do
    if docker exec modern-reservation-redis redis-cli ping >/dev/null 2>&1; then
        print_success "Redis is ready (localhost:6379)"
        break
    fi
    if [ $i -eq 10 ]; then
        print_error "Redis failed to start"
        exit 1
    fi
    sleep 2
done

print_step "Checking Kafka..."
for i in {1..20}; do
    if docker exec modern-reservation-kafka kafka-broker-api-versions --bootstrap-server localhost:9092 >/dev/null 2>&1; then
        print_success "Kafka is ready (localhost:9092)"
        break
    fi
    if [ $i -eq 20 ]; then
        print_error "Kafka failed to start"
        exit 1
    fi
    sleep 3
done

print_step "Checking Schema Registry..."
for i in {1..15}; do
    if curl -s http://localhost:8085/ >/dev/null 2>&1; then
        print_success "Schema Registry is ready (http://localhost:8085)"
        break
    fi
    if [ $i -eq 15 ]; then
        print_error "Schema Registry failed to start"
        exit 1
    fi
    sleep 2
done

print_step "Checking Kafka UI..."
for i in {1..10}; do
    if curl -s http://localhost:8090/ >/dev/null 2>&1; then
        print_success "Kafka UI is ready (http://localhost:8090)"
        break
    fi
    if [ $i -eq 10 ]; then
        print_warning "Kafka UI not responding, but continuing..."
    fi
    sleep 2
done

print_step "Checking Zipkin..."
for i in {1..10}; do
    if curl -s http://localhost:9411/health >/dev/null 2>&1; then
        print_success "Zipkin is ready (http://localhost:9411)"
        break
    fi
    if [ $i -eq 10 ]; then
        print_warning "Zipkin not responding, but continuing..."
    fi
    sleep 2
done

# ============================================================================
# STEP 7: INITIALIZE DATABASE
# ============================================================================
if [ "$SKIP_DATABASE_SETUP" = false ]; then
    print_header "💾 STEP 7: Initializing Database Schema"

    print_step "Running database setup script..."
    if [ -f "$SCRIPT_DIR/setup-database.sh" ]; then
        chmod +x "$SCRIPT_DIR/setup-database.sh"
        "$SCRIPT_DIR/setup-database.sh" 2>&1 | grep -E "✅|⚠️|Created|SUCCESS|ERROR" | head -20 || true
        print_success "Database schema initialized"
    else
        print_warning "Database setup script not found, skipping..."
    fi
else
    print_header "⏭️  STEP 7: Skipping Database Initialization"
    print_info "Database schema not modified"
fi

# ============================================================================
# STEP 8: BUILD AND START RESERVATION-ENGINE
# ============================================================================
print_header "🏗️  STEP 8: Building and Starting Reservation Engine"

print_step "Compiling reservation-engine..."
cd "$BASE_DIR/apps/backend/java-services/business-services/reservation-engine"
mvn clean compile -DskipTests -q 2>&1 | grep -E "BUILD|ERROR" || true
if [ $? -eq 0 ]; then
    print_success "reservation-engine compiled successfully"
else
    print_error "reservation-engine compilation failed"
    cd "$BASE_DIR"
    exit 1
fi

print_step "Starting reservation-engine..."
nohup mvn spring-boot:run -DskipTests > /tmp/reservation-engine.log 2>&1 &
RESERVATION_PID=$!
print_substep "Started with PID: $RESERVATION_PID"
print_substep "Logs: tail -f /tmp/reservation-engine.log"

cd "$BASE_DIR"

print_substep "Waiting for service to start (25 seconds)..."
sleep 25

# Check if service is running
if ! ps -p $RESERVATION_PID > /dev/null 2>&1; then
    print_error "reservation-engine failed to start"
    echo ""
    echo -e "${YELLOW}Last 30 lines of log:${NC}"
    tail -30 /tmp/reservation-engine.log
    exit 1
fi

# Check if port 8081 is listening
for i in {1..10}; do
    if lsof -Pi :8081 -sTCP:LISTEN -t >/dev/null 2>&1; then
        print_success "reservation-engine is running on port 8081"
        break
    fi
    if [ $i -eq 10 ]; then
        print_error "reservation-engine not responding on port 8081"
        exit 1
    fi
    sleep 2
done

# ============================================================================
# STEP 9: TEST AVRO EVENT PUBLISHING
# ============================================================================
print_header "🧪 STEP 9: Testing Avro Event Publishing"

print_step "Getting security password..."
PASSWORD=$(grep "Using generated security password" /tmp/reservation-engine.log | tail -1 | awk '{print $NF}' || echo "")
if [ -z "$PASSWORD" ]; then
    print_warning "Could not extract password from logs"
    print_info "You may need to test manually"
else
    print_substep "Password: $PASSWORD"
fi

print_step "Publishing test Avro event..."
sleep 5
RESPONSE=$(curl -s -u user:$PASSWORD "http://localhost:8081/reservation-engine/api/test/kafka" 2>/dev/null || echo "")

if echo "$RESPONSE" | grep -q "success"; then
    print_success "Test event published successfully!"
    print_substep "Response: $RESPONSE"
else
    print_warning "Could not publish test event (might need manual retry)"
    print_info "Try: curl -u user:PASSWORD http://localhost:8081/reservation-engine/api/test/kafka"
fi

print_step "Verifying Schema Registry..."
sleep 3
SCHEMAS=$(curl -s http://localhost:8085/subjects 2>/dev/null || echo "[]")
if echo "$SCHEMAS" | grep -q "reservation.created"; then
    print_success "Schema registered: reservation.created-value"
else
    print_warning "Schema not yet registered (may need to retry test)"
fi

# ============================================================================
# STEP 10: SUMMARY
# ============================================================================
END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

print_header "✨ CLEAN RESTART COMPLETE!"

echo -e "${GREEN}╔════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║                    SERVICES READY                              ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${CYAN}🗄️  Infrastructure Services:${NC}"
echo -e "  • PostgreSQL:       http://localhost:5432"
echo -e "  • Redis:            http://localhost:6379"
echo -e "  • Kafka:            http://localhost:9092"
echo -e "  • Schema Registry:  http://localhost:8085"
echo -e "  • Kafka UI:         http://localhost:8090"
echo -e "  • Zipkin:           http://localhost:9411"
echo -e "  • PgAdmin:          http://localhost:5050"
echo ""
echo -e "${CYAN}🚀 Business Services:${NC}"
echo -e "  • Reservation Engine: http://localhost:8081/reservation-engine"
echo ""
echo -e "${CYAN}📊 Monitoring:${NC}"
echo -e "  • Kafka Topics:     http://localhost:8090/ui/clusters/local/topics"
echo -e "  • Registered Schemas: http://localhost:8085/subjects"
echo -e "  • Service Logs:     tail -f /tmp/reservation-engine.log"
echo ""
echo -e "${CYAN}🧪 Test Commands:${NC}"
if [ ! -z "$PASSWORD" ]; then
    echo -e "  • Test Kafka:       ${YELLOW}curl -u user:$PASSWORD http://localhost:8081/reservation-engine/api/test/kafka${NC}"
else
    echo -e "  • Test Kafka:       curl -u user:PASSWORD http://localhost:8081/reservation-engine/api/test/kafka"
fi
echo -e "  • Check Schemas:    ${YELLOW}curl http://localhost:8085/subjects${NC}"
echo -e "  • View Kafka UI:    ${YELLOW}open http://localhost:8090${NC}"
echo ""
echo -e "${GREEN}✅ Total time: ${DURATION}s${NC}"
echo -e "${GREEN}✅ All systems operational!${NC}"
echo ""
echo -e "${YELLOW}💡 Tip: Use './scripts/infra.sh status-all' to check service health${NC}"
echo ""

# Save password to temp file for convenience
if [ ! -z "$PASSWORD" ]; then
    echo "$PASSWORD" > /tmp/reservation-engine-password.txt
    echo -e "${CYAN}ℹ  Password saved to: /tmp/reservation-engine-password.txt${NC}"
fi

exit 0
