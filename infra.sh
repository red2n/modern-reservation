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
    echo -e "${CYAN}🔧 INFRASTRUCTURE MANAGEMENT${NC}"
    echo ""
    printf "${CYAN}┌────────────────────┬─────────────────────────────────────────────────┐${NC}\n"
    printf "${CYAN}│ %-18s │ %-47s │${NC}\n" "Command" "Description"
    printf "${CYAN}├────────────────────┼─────────────────────────────────────────────────┤${NC}\n"
    printf "${GREEN}│ %-18s │ %-47s │${NC}\n" "start" "Start infrastructure services"
    printf "${GREEN}│ %-18s │ %-47s │${NC}\n" "start --restart" "Force restart infrastructure services"
    printf "${GREEN}│ %-18s │ %-47s │${NC}\n" "start-business" "Start business services"
    printf "${GREEN}│ %-18s │ %-47s │${NC}\n" "start-all" "Start infrastructure + business services"
    printf "${GREEN}│ %-18s │ %-47s │${NC}\n" "start-all --restart" "Force restart all services"
    printf "${RED}│ %-18s │ %-47s │${NC}\n" "stop" "Stop infrastructure services"
    printf "${RED}│ %-18s │ %-47s │${NC}\n" "stop-business" "Stop business services"
    printf "${RED}│ %-18s │ %-47s │${NC}\n" "stop-all" "Stop all services"
    printf "${BLUE}│ %-18s │ %-47s │${NC}\n" "status" "Check infrastructure status"
    printf "${BLUE}│ %-18s │ %-47s │${NC}\n" "status-business" "Check business services status"
    printf "${BLUE}│ %-18s │ %-47s │${NC}\n" "status-all" "Check all services status"
    printf "${YELLOW}│ %-18s │ %-47s │${NC}\n" "eureka" "Open Eureka Dashboard"
    printf "${CYAN}└────────────────────┴─────────────────────────────┘${NC}\n"
    echo ""
    printf "${YELLOW}💡 Quick Start:${NC} ./infra.sh start-all  |  ${RED}Stop All:${NC} ./infra.sh stop-all  |  ${BLUE}Check:${NC} ./infra.sh status-all\n"
    printf "${YELLOW}💡 Force Restart:${NC} ./infra.sh start-all --restart  |  ${GREEN}Smart Start:${NC} ./infra.sh start-all\n"
}

# Parse restart flag
RESTART_MODE=""
if [ "$2" = "--restart" ]; then
    RESTART_MODE="--restart"
fi

# Main command handling
case "${1:-help}" in
    "start")
        if [ "$RESTART_MODE" = "--restart" ]; then
            echo -e "${YELLOW}🔄 Force restarting infrastructure services...${NC}"
            exec "$SCRIPTS_DIR/start-infrastructure.sh" --restart
        else
            echo -e "${GREEN}🚀 Starting infrastructure services...${NC}"
            exec "$SCRIPTS_DIR/start-infrastructure.sh"
        fi
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
        if [ "$RESTART_MODE" = "--restart" ]; then
            echo -e "${YELLOW}🔄 Force restarting business services...${NC}"
            exec "$SCRIPTS_DIR/start-business-services.sh" --restart
        else
            echo -e "${GREEN}🚀 Starting business services...${NC}"
            exec "$SCRIPTS_DIR/start-business-services.sh"
        fi
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
        if [ "$RESTART_MODE" = "--restart" ]; then
            echo -e "${YELLOW}🔄 Force restarting all services (infrastructure + business)...${NC}"
            echo -e "${BLUE}Step 1/2: Force restarting infrastructure services...${NC}"
            "$SCRIPTS_DIR/start-infrastructure.sh" --restart
        else
            echo -e "${GREEN}🚀 Starting all services (infrastructure + business)...${NC}"
            echo -e "${BLUE}Step 1/2: Starting infrastructure services...${NC}"
            "$SCRIPTS_DIR/start-infrastructure.sh"
        fi
        if [ $? -eq 0 ]; then
            echo -e "${BLUE}Step 2/2: Starting business services...${NC}"
            if [ "$RESTART_MODE" = "--restart" ]; then
                exec "$SCRIPTS_DIR/start-business-services.sh" --restart
            else
                exec "$SCRIPTS_DIR/start-business-services.sh"
            fi
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
    "eureka")
        echo -e "${CYAN}🔍 Opening Eureka Dashboard...${NC}"
        echo -e "${BLUE}Eureka Dashboard: http://localhost:8761${NC}"
        if command -v xdg-open > /dev/null; then
            xdg-open http://localhost:8761
        elif command -v open > /dev/null; then
            open http://localhost:8761
        else
            echo -e "${YELLOW}Please open http://localhost:8761 in your browser${NC}"
        fi
        ;;
    "discovery-status")
        echo -e "${BLUE}🔍 Checking service discovery status...${NC}"
        "$SCRIPTS_DIR/check-infrastructure.sh" | grep -A 20 "SERVICE DISCOVERY STATUS"
        ;;
    "help"|"-h"|"--help")
        show_usage
        ;;
    *)
        echo -e "${RED}❌ Unknown command: '$1'${NC} - Use one of these commands:"
        echo ""
        show_usage
        exit 1
        ;;
esac
