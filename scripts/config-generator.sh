#!/bin/bash

# ============================================
# Configuration Generator Script
# ============================================
# Generates configuration files from centralized port registry

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

# Generate master .env file
generate_master_env() {
    log_info "Generating master .env.ports file..."
    run_port_manager config-master > "$PROJECT_ROOT/.env.ports"
    log_success "Generated: .env.ports"
}

# Generate Spring Boot configs for Java services
generate_spring_configs() {
    log_info "Generating Spring Boot configurations..."

    local java_services=(
        "reservation-engine"
        "availability-calculator"
        "rate-management"
        "payment-processor"
        "analytics-engine"
        "tenant-service"
    )

    for service in "${java_services[@]}"; do
        local service_dir="$PROJECT_ROOT/apps/backend/java-services/business-services/$service"
        if [ -d "$service_dir" ]; then
            local output_file="$service_dir/src/main/resources/application-ports.yml"
            run_port_manager config-spring "$service" > "$output_file"
            log_success "Generated: $service/application-ports.yml"
        fi
    done

    # Infrastructure services
    local infra_services=(
        "config-server"
        "eureka-server"
        "gateway-service"
    )

    for service in "${infra_services[@]}"; do
        local service_dir="$PROJECT_ROOT/apps/backend/java-services/infrastructure/$service"
        if [ -d "$service_dir" ]; then
            local output_file="$service_dir/src/main/resources/application-ports.yml"
            run_port_manager config-spring "$service" > "$output_file"
            log_success "Generated: $service/application-ports.yml"
        fi
    done
}

# Generate Node.js .env files
generate_node_configs() {
    log_info "Generating Node.js .env configurations..."

    local node_services=(
        "auth-service"
        "notification-service"
        "websocket-service"
    )

    for service in "${node_services[@]}"; do
        local service_dir="$PROJECT_ROOT/apps/backend/node-services/$service"
        if [ -d "$service_dir" ]; then
            local output_file="$service_dir/.env.ports"
            run_port_manager config-node "$service" > "$output_file"
            log_success "Generated: $service/.env.ports"
        fi
    done
}

# Generate TypeScript constants
generate_typescript_constants() {
    log_info "Generating TypeScript constants..."
    local output_file="$PROJECT_ROOT/libs/shared/schemas/src/ports.ts"
    run_port_manager config-ts > "$output_file"
    log_success "Generated: libs/shared/schemas/src/ports.ts"
}

# Generate Java constants
generate_java_constants() {
    log_info "Generating Java constants..."
    local output_dir="$PROJECT_ROOT/apps/backend/java-services/infrastructure/config-server/src/main/java/com/modernreservation/config"
    mkdir -p "$output_dir"
    local output_file="$output_dir/ServicePorts.java"
    run_port_manager config-java > "$output_file"
    log_success "Generated: ServicePorts.java"
}

# Generate Kubernetes ConfigMap
generate_k8s_config() {
    log_info "Generating Kubernetes ConfigMap..."
    local output_file="$PROJECT_ROOT/k8s/base/configmap-ports.yaml"
    run_port_manager config-k8s > "$output_file"
    log_success "Generated: k8s/base/configmap-ports.yaml"
}

# Generate all configurations
generate_all() {
    log_info "Generating all configuration files from port registry..."
    echo ""

    generate_master_env
    generate_spring_configs
    generate_node_configs
    generate_typescript_constants
    generate_java_constants
    generate_k8s_config

    echo ""
    log_success "✅ All configuration files generated successfully!"
    echo ""
    log_info "Next steps:"
    echo "  1. Review generated files"
    echo "  2. Update services to import from application-ports.yml"
    echo "  3. Rebuild services: ./dev.sh build"
    echo "  4. Restart services: ./dev.sh clean"
}

# Show help
show_help() {
    cat << EOF
${GREEN}Configuration Generator${NC}

Generates configuration files from centralized port registry.

Usage: $0 <command>

Commands:
  ${BLUE}all${NC}               Generate all configuration files
  ${BLUE}master${NC}            Generate master .env.ports file
  ${BLUE}spring${NC}            Generate Spring Boot configs
  ${BLUE}node${NC}              Generate Node.js .env files
  ${BLUE}typescript${NC}        Generate TypeScript constants
  ${BLUE}java${NC}              Generate Java constants
  ${BLUE}k8s${NC}               Generate Kubernetes ConfigMap

Generated Files:
  .env.ports                              Master environment file
  apps/backend/java-services/*/application-ports.yml
  apps/backend/node-services/*/.env.ports
  libs/shared/schemas/src/ports.ts        TypeScript constants
  k8s/base/configmap-ports.yaml           Kubernetes config

Examples:
  $0 all              # Generate all configs
  $0 spring           # Only Spring Boot configs
  $0 node             # Only Node.js configs

EOF
}

# Main
case "${1:-all}" in
    all)
        generate_all
        ;;
    master)
        generate_master_env
        ;;
    spring)
        generate_spring_configs
        ;;
    node)
        generate_node_configs
        ;;
    typescript|ts)
        generate_typescript_constants
        ;;
    java)
        generate_java_constants
        ;;
    k8s|kubernetes)
        generate_k8s_config
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        log_error "Unknown command: $1"
        echo ""
        show_help
        exit 1
        ;;
esac
