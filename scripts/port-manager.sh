#!/bin/bash

# ============================================
# Port Management Utility Script
# ============================================
# Uses the @modern-reservation/port-manager library

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
PORT_MANAGER_DIR="$PROJECT_ROOT/libs/shared/port-manager"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${BLUE}ℹ${NC} $1"; }
log_success() { echo -e "${GREEN}✓${NC} $1"; }
log_warning() { echo -e "${YELLOW}⚠${NC} $1"; }
log_error() { echo -e "${RED}✗${NC} $1"; }

# Check if port-manager is built
check_port_manager() {
    if [ ! -d "$PORT_MANAGER_DIR/dist" ]; then
        log_warning "Port manager not built, building now..."
        cd "$PORT_MANAGER_DIR"
        npm install
        npm run build
        cd "$PROJECT_ROOT"
        log_success "Port manager built successfully"
    fi
}

# Run port-manager CLI
run_port_manager() {
    check_port_manager
    cd "$PORT_MANAGER_DIR"
    node dist/cli.js "$@"
}

# Show help
show_help() {
    cat << EOF
${GREEN}Port Management Utility${NC}

Usage: $0 <command> [options]

Commands:
  ${BLUE}list${NC}              List all registered services and ports
  ${BLUE}check${NC}             Check for port conflicts and usage
  ${BLUE}report${NC}            Generate detailed port report
  ${BLUE}security${NC}          Generate security report (internal vs external)
  ${BLUE}validate${NC}          Validate all port configurations
  ${BLUE}export-env${NC}        Export as environment variables
  ${BLUE}export-docker${NC}     Export as Docker Compose .env file
  ${BLUE}find <category>${NC}   Find available port in category

Categories:
  FRONTEND, NODE_SERVICE, JAVA_SERVICE, DATABASE,
  CACHE, MESSAGE_QUEUE, GATEWAY, SERVICE_DISCOVERY,
  CONFIG_SERVER

Examples:
  $0 list              # List all services
  $0 check             # Check port status
  $0 security          # Show security report
  $0 export-docker > .env.ports

EOF
}

# Main
case "${1:-}" in
    list)
        run_port_manager list
        ;;
    check)
        run_port_manager check
        ;;
    report)
        run_port_manager report
        ;;
    security)
        run_port_manager security
        ;;
    validate)
        run_port_manager validate
        ;;
    export-env)
        run_port_manager env
        ;;
    export-docker)
        run_port_manager docker-env
        ;;
    find)
        run_port_manager find "$2"
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        if [ -z "$1" ]; then
            show_help
        else
            run_port_manager "$@"
        fi
        ;;
esac
