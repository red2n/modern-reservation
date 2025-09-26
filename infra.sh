#!/bin/bash

# Modern Reservation System - Infrastructure Management Wrapper
# This script provides convenient commands to manage infrastructure services

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Base directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SCRIPTS_DIR="$SCRIPT_DIR/scripts"

# Function to print usage
show_usage() {
    echo -e "${CYAN}Modern Reservation System - Complete Service Management${NC}"
    echo ""
    echo -e "${YELLOW}Infrastructure Commands:${NC}"
    echo -e "  ${GREEN}./infra.sh start${NC}          - Start all infrastructure services"
    echo -e "  ${RED}./infra.sh stop${NC}           - Stop all infrastructure services"
    echo -e "  ${BLUE}./infra.sh status${NC}         - Check infrastructure services status"
    echo ""
    echo -e "${YELLOW}Business Services Commands:${NC}"
    echo -e "  ${GREEN}./infra.sh start-business${NC}  - Start all business services"
    echo -e "  ${RED}./infra.sh stop-business${NC}   - Stop all business services"
    echo -e "  ${BLUE}./infra.sh status-business${NC} - Check business services status"
    echo ""
    echo -e "${YELLOW}Complete System Commands:${NC}"
    echo -e "  ${GREEN}./infra.sh start-all${NC}      - Start infrastructure + business services"
    echo -e "  ${RED}./infra.sh stop-all${NC}       - Stop all services (business + infrastructure)"
    echo -e "  ${BLUE}./infra.sh status-all${NC}     - Check status of all services"
    echo ""
    echo -e "${YELLOW}Docker Infrastructure Commands:${NC}"
    echo -e "  ${GREEN}./infra.sh docker-infra-start${NC}  - Start external services (Zipkin, PostgreSQL, Redis) via Docker"
    echo -e "  ${RED}./infra.sh docker-infra-stop${NC}   - Stop external Docker services"
    echo -e "  ${BLUE}./infra.sh docker-infra-status${NC} - Check Docker infrastructure status"
    echo ""
    echo -e "${YELLOW}Other Commands:${NC}"
    echo -e "  ${YELLOW}./infra.sh help${NC}          - Show this help message"
    echo ""
    echo -e "${CYAN}Direct script access:${NC}"
    echo -e "  ${GREEN}scripts/start-infrastructure.sh${NC}    - Infrastructure startup (with Docker Zipkin)"
    echo -e "  ${GREEN}scripts/start-business-services.sh${NC}  - Business services startup"
    echo -e "  ${RED}scripts/stop-infrastructure.sh${NC}     - Infrastructure shutdown (with Docker Zipkin)"
    echo -e "  ${RED}scripts/stop-business-services.sh${NC}   - Business services shutdown"
    echo -e "  ${BLUE}scripts/check-infrastructure.sh${NC}    - Infrastructure status (with Docker Zipkin)"
    echo -e "  ${BLUE}scripts/check-business-services.sh${NC}  - Business services status"
    echo -e "  ${GREEN}./docker-infra.sh${NC}               - Direct Docker infrastructure management"
    echo ""
}

# Main command handling
case "${1:-help}" in
    "start")
        echo -e "${GREEN}🚀 Starting infrastructure services...${NC}"
        exec "$SCRIPTS_DIR/start-infrastructure.sh"
        ;;
    "stop")
        echo -e "${RED}🛑 Stopping infrastructure services...${NC}"
        exec "$SCRIPTS_DIR/stop-infrastructure.sh"
        ;;
    "status"|"check")
        echo -e "${BLUE}📊 Checking infrastructure services status...${NC}"
        exec "$SCRIPTS_DIR/check-infrastructure.sh"
        ;;
    "start-business")
        echo -e "${GREEN}🚀 Starting business services...${NC}"
        exec "$SCRIPTS_DIR/start-business-services.sh"
        ;;
    "stop-business")
        echo -e "${RED}🛑 Stopping business services...${NC}"
        exec "$SCRIPTS_DIR/stop-business-services.sh"
        ;;
    "status-business"|"check-business")
        echo -e "${BLUE}📊 Checking business services status...${NC}"
        exec "$SCRIPTS_DIR/check-business-services.sh"
        ;;
    "start-all")
        echo -e "${GREEN}🚀 Starting all services (infrastructure + business)...${NC}"
        echo -e "${BLUE}Step 1/2: Starting infrastructure services...${NC}"
        "$SCRIPTS_DIR/start-infrastructure.sh"
        if [ $? -eq 0 ]; then
            echo -e "${BLUE}Step 2/2: Starting business services...${NC}"
            exec "$SCRIPTS_DIR/start-business-services.sh"
        else
            echo -e "${RED}❌ Infrastructure startup failed. Skipping business services.${NC}"
            exit 1
        fi
        ;;
    "stop-all")
        echo -e "${RED}🛑 Stopping all services (business + infrastructure)...${NC}"
        echo -e "${BLUE}Step 1/2: Stopping business services...${NC}"
        "$SCRIPTS_DIR/stop-business-services.sh" || true
        echo -e "${BLUE}Step 2/2: Stopping infrastructure services...${NC}"
        exec "$SCRIPTS_DIR/stop-infrastructure.sh"
        ;;
    "status-all"|"check-all")
        echo -e "${BLUE}📊 Checking all services status...${NC}"
        echo -e "${CYAN}=== INFRASTRUCTURE SERVICES ===${NC}"
        "$SCRIPTS_DIR/check-infrastructure.sh"
        echo ""
        echo ""
        echo -e "${CYAN}=== BUSINESS SERVICES ===${NC}"
        exec "$SCRIPTS_DIR/check-business-services.sh"
        ;;
    "docker-infra-start")
        echo -e "${GREEN}🐳 Starting Docker infrastructure services...${NC}"
        exec "$SCRIPT_DIR/docker-infra.sh" infra-start
        ;;
    "docker-infra-stop")
        echo -e "${RED}🐳 Stopping Docker infrastructure services...${NC}"
        exec "$SCRIPT_DIR/docker-infra.sh" infra-stop
        ;;
    "docker-infra-status")
        echo -e "${BLUE}🐳 Checking Docker infrastructure status...${NC}"
        exec "$SCRIPT_DIR/docker-infra.sh" health
        ;;
    "help"|"-h"|"--help")
        show_usage
        ;;
    *)
        echo -e "${RED}❌ Unknown command: $1${NC}"
        echo ""
        show_usage
        exit 1
        ;;
esac
