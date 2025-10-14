#!/bin/bash

set -e

# ============================================
# Secure Docker Management Script
# ============================================
# This script manages Docker deployment with network isolation
# Only Gateway service is exposed externally

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
DOCKER_DIR="$PROJECT_ROOT/infrastructure/docker"
COMPOSE_FILE="$DOCKER_DIR/docker-compose.secure.yml"
ENV_FILE="$DOCKER_DIR/.env.ports"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

log_success() {
    echo -e "${GREEN}✓${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

log_error() {
    echo -e "${RED}✗${NC} $1"
}

# Check if .env.ports exists
check_env_file() {
    if [ ! -f "$ENV_FILE" ]; then
        log_warning ".env.ports not found, creating from template..."
        cp "$DOCKER_DIR/.env.ports.template" "$ENV_FILE"
        log_warning "Please edit $ENV_FILE with your configuration"
        log_warning "Especially update JWT_SECRET and database password!"
        read -p "Press Enter to continue after editing..."
    fi
}

# Validate security configuration
validate_security() {
    log_info "Validating security configuration..."

    # Check if internal network is properly configured
    if ! grep -q "internal: true" "$COMPOSE_FILE"; then
        log_error "Backend network is not marked as internal!"
        exit 1
    fi

    # Check JWT secret
    if grep -q "change-me-in-production" "$ENV_FILE"; then
        log_warning "JWT_SECRET still has default value!"
        log_warning "This is OK for development but MUST be changed for production"
    fi

    log_success "Security configuration validated"
}

# Start services with security
start_secure() {
    log_info "Starting services with secure configuration..."

    check_env_file
    validate_security

    cd "$DOCKER_DIR"
    docker compose -f docker-compose.secure.yml --env-file .env.ports up -d

    log_success "Secure deployment started"
    log_info "Only the following are externally accessible:"
    log_info "  - Gateway: http://localhost:8080"
    log_info "  - Guest Portal: http://localhost:3000"
    log_info ""
    log_info "All backend services are isolated and NOT accessible externally"
}

# Stop services
stop_secure() {
    log_info "Stopping secure deployment..."
    cd "$DOCKER_DIR"
    docker compose -f docker-compose.secure.yml down
    log_success "Secure deployment stopped"
}

# Show status
status() {
    log_info "Service Status:"
    cd "$DOCKER_DIR"
    docker compose -f docker-compose.secure.yml ps
}

# Check network security
check_networks() {
    log_info "Network Configuration:"
    echo ""

    # Show networks
    docker network ls | grep modern-reservation

    echo ""
    log_info "Backend Internal Network (should be internal=true):"
    docker network inspect modern-reservation_backend-internal | grep -A 5 "Internal"

    echo ""
    log_info "Services on each network:"
    docker network inspect modern-reservation_gateway-net -f '{{range .Containers}}{{.Name}} {{end}}'
    echo ""
    docker network inspect modern-reservation_backend-internal -f '{{range .Containers}}{{.Name}} {{end}}'
}

# Test security
test_security() {
    log_info "Testing security configuration..."
    echo ""

    # Test Gateway (should be accessible)
    log_info "Testing Gateway (should be accessible)..."
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health | grep -q "200\|503"; then
        log_success "Gateway is accessible externally ✓"
    else
        log_error "Gateway is NOT accessible (this is a problem)"
    fi

    # Test direct backend service (should NOT be accessible)
    log_info "Testing direct backend service access (should fail)..."
    if curl -s --connect-timeout 2 http://localhost:8101/actuator/health &>/dev/null; then
        log_error "Backend service is accessible externally (SECURITY ISSUE!)"
    else
        log_success "Backend services are properly isolated ✓"
    fi

    echo ""
    log_success "Security test complete"
}

# Show logs
logs() {
    cd "$DOCKER_DIR"
    if [ -z "$1" ]; then
        docker compose -f docker-compose.secure.yml logs -f
    else
        docker compose -f docker-compose.secure.yml logs -f "$1"
    fi
}

# Clean restart
clean() {
    log_warning "Performing clean restart (removes volumes)..."
    cd "$DOCKER_DIR"
    docker compose -f docker-compose.secure.yml down -v
    docker compose -f docker-compose.secure.yml up -d
    log_success "Clean restart complete"
}

# Show help
show_help() {
    cat << EOF
${GREEN}Secure Docker Management${NC}

Usage: $0 <command> [options]

Commands:
  ${BLUE}start${NC}          Start services with secure network isolation
  ${BLUE}stop${NC}           Stop all services
  ${BLUE}restart${NC}        Restart all services
  ${BLUE}status${NC}         Show service status
  ${BLUE}logs [service]${NC} Show logs (optionally for specific service)
  ${BLUE}clean${NC}          Clean restart (removes volumes)
  ${BLUE}networks${NC}       Show network configuration
  ${BLUE}test${NC}           Test security configuration
  ${BLUE}validate${NC}       Validate security settings

Security Features:
  ✓ Only Gateway (8080) exposed externally
  ✓ All backend services on internal-only network
  ✓ Database isolated in separate network
  ✓ Frontend apps can only access Gateway

Examples:
  $0 start              # Start secure deployment
  $0 test               # Test security
  $0 logs gateway       # View Gateway logs
  $0 networks           # Check network isolation

EOF
}

# Main script
case "${1:-}" in
    start)
        start_secure
        ;;
    stop)
        stop_secure
        ;;
    restart)
        stop_secure
        start_secure
        ;;
    status)
        status
        ;;
    logs)
        logs "$2"
        ;;
    clean)
        clean
        ;;
    networks)
        check_networks
        ;;
    test)
        test_security
        ;;
    validate)
        validate_security
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        log_error "Unknown command: ${1:-}"
        echo ""
        show_help
        exit 1
        ;;
esac
