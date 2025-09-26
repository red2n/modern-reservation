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
    echo -e "${CYAN}Modern Reservation System - Infrastructure Management${NC}"
    echo ""
    echo -e "${YELLOW}Usage:${NC}"
    echo -e "  ${GREEN}./infra.sh start${NC}   - Start all infrastructure services"
    echo -e "  ${RED}./infra.sh stop${NC}    - Stop all infrastructure services"
    echo -e "  ${BLUE}./infra.sh status${NC}  - Check status of all services"
    echo -e "  ${YELLOW}./infra.sh help${NC}    - Show this help message"
    echo ""
    echo -e "${CYAN}Direct script access:${NC}"
    echo -e "  ${GREEN}scripts/start-infrastructure.sh${NC}"
    echo -e "  ${RED}scripts/stop-infrastructure.sh${NC}"
    echo -e "  ${BLUE}scripts/check-infrastructure.sh${NC}"
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
