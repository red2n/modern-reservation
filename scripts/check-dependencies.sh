#!/bin/bash

# Enhanced Dependency Checker for Modern Reservation System
# Verifies all required dependencies before starting services

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

print_status() {
    echo -e "${BLUE}[CHECK] $1${NC}"
}

print_success() {
    echo -e "${GREEN}[✓] $1${NC}"
}

print_error() {
    echo -e "${RED}[✗] $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}[!] $1${NC}"
}

# Track overall health
ALL_CHECKS_PASSED=true

echo "=========================================="
echo "   Dependency Health Check"
echo "=========================================="
echo ""

# Check 1: Environment Variables
print_status "Checking critical environment variables..."
if [ -f "$BASE_DIR/.env" ]; then
    source "$BASE_DIR/.env"
    print_success ".env file found"

    # Check critical variables
    CRITICAL_VARS=("DB_PASSWORD" "JWT_SECRET" "CONFIG_SERVER_PASSWORD")
    for var in "${CRITICAL_VARS[@]}"; do
        if [ -z "${!var}" ]; then
            print_error "$var is not set in .env file"
            ALL_CHECKS_PASSED=false
        else
            print_success "$var is configured"
        fi
    done
else
    print_warning ".env file not found - using defaults (NOT RECOMMENDED for production)"
    print_warning "Copy .env.example to .env and configure properly"
fi
echo ""

# Check 2: Infrastructure Services
print_status "Checking infrastructure services..."
INFRA_SERVICES=(
    "config-server:8888:/config/actuator/health"
    "eureka-server:8761:/eureka/actuator/health"
    "gateway-service:8080:/actuator/health"
)

for service_info in "${INFRA_SERVICES[@]}"; do
    IFS=':' read -r service port path <<< "$service_info"

    if curl -s --max-time 5 "http://localhost:$port$path" | grep -q "UP"; then
        print_success "$service is healthy on port $port"
    else
        print_error "$service is not responding on port $port"
        ALL_CHECKS_PASSED=false
    fi
done
echo ""

# Check 3: Database Connectivity
print_status "Checking database connectivity..."
if command -v psql &> /dev/null; then
    DB_HOST=${DB_HOST:-localhost}
    DB_PORT=${DB_PORT:-5432}
    DB_NAME=${DB_NAME:-modern_reservation}
    DB_USER=${DB_USERNAME:-reservation_user}

    if PGPASSWORD="${DB_PASSWORD}" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT 1;" &> /dev/null; then
        print_success "Database connection successful"

        # Check if schema is initialized
        TABLE_COUNT=$(PGPASSWORD="${DB_PASSWORD}" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';" 2>/dev/null | tr -d ' ')

        if [ "$TABLE_COUNT" -gt 10 ]; then
            print_success "Database schema initialized ($TABLE_COUNT tables found)"
        else
            print_warning "Database schema may not be fully initialized ($TABLE_COUNT tables found)"
            print_warning "Run: ./scripts/setup-database.sh"
        fi
    else
        print_error "Cannot connect to database"
        print_error "Check database credentials and ensure PostgreSQL is running"
        ALL_CHECKS_PASSED=false
    fi
else
    print_warning "psql not found - skipping database connectivity check"
fi
echo ""

# Check 4: Redis Connectivity
print_status "Checking Redis connectivity..."
REDIS_HOST=${REDIS_HOST:-localhost}
REDIS_PORT=${REDIS_PORT:-6379}

if command -v redis-cli &> /dev/null; then
    if redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" ping &> /dev/null; then
        print_success "Redis is accessible"
    else
        print_warning "Redis is not responding"
        print_warning "Some features (caching, rate limiting) may not work"
    fi
else
    print_warning "redis-cli not found - skipping Redis check"
fi
echo ""

# Check 5: Kafka Connectivity (if available)
print_status "Checking Kafka connectivity..."
KAFKA_BOOTSTRAP=${KAFKA_BOOTSTRAP_SERVERS:-localhost:9092}

if command -v kafka-broker-api-versions.sh &> /dev/null; then
    if timeout 5 kafka-broker-api-versions.sh --bootstrap-server "$KAFKA_BOOTSTRAP" &> /dev/null; then
        print_success "Kafka is accessible"
    else
        print_warning "Kafka is not responding"
        print_warning "Event-driven features may not work"
    fi
else
    print_warning "Kafka tools not found - skipping Kafka check"
fi
echo ""

# Check 6: Docker (for optional services)
print_status "Checking Docker availability..."
if command -v docker &> /dev/null; then
    if docker ps &> /dev/null; then
        print_success "Docker is available and running"

        # Check for running containers
        RUNNING_CONTAINERS=$(docker ps --format '{{.Names}}' | grep -E 'postgres|redis|kafka' | wc -l)
        if [ "$RUNNING_CONTAINERS" -gt 0 ]; then
            print_success "$RUNNING_CONTAINERS Docker container(s) running"
        fi
    else
        print_warning "Docker daemon not running"
    fi
else
    print_warning "Docker not installed"
fi
echo ""

# Check 7: Required Java Version
print_status "Checking Java version..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -ge 17 ]; then
        print_success "Java $JAVA_VERSION detected (requirement: Java 17+)"
    else
        print_error "Java $JAVA_VERSION detected, but Java 17+ is required"
        ALL_CHECKS_PASSED=false
    fi
else
    print_error "Java not found - Java 17+ required"
    ALL_CHECKS_PASSED=false
fi
echo ""

# Check 8: Maven availability
print_status "Checking Maven availability..."
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1 | cut -d' ' -f3)
    print_success "Maven $MVN_VERSION detected"
else
    print_warning "Maven not found - required for building services"
fi
echo ""

# Final Summary
echo "=========================================="
if [ "$ALL_CHECKS_PASSED" = true ]; then
    print_success "All critical checks passed! ✓"
    echo ""
    echo "System is ready to start business services."
    exit 0
else
    print_error "Some critical checks failed!"
    echo ""
    echo "Please resolve the issues above before starting services."
    echo "Refer to the documentation for setup instructions."
    exit 1
fi
