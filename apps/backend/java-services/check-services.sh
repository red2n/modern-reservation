#!/bin/bash

# Modern Reservation System - Services Status Script
# This script checks the status of all services and provides health information

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Project root directory
PROJECT_ROOT="/home/subramani/modern-reservation"
JAVA_SERVICES_DIR="$PROJECT_ROOT/apps/backend/java-services"

# Function to check service health
check_service_health() {
    local service_name=$1
    local port=$2
    local health_url=$3
    
    # Check if port is open
    if ! netstat -tuln 2>/dev/null | grep -q ":$port "; then
        echo -e "${RED}❌ $service_name - Port $port not listening${NC}"
        return 1
    fi
    
    # Check health endpoint
    if curl -f -s --connect-timeout 5 --max-time 10 "$health_url" >/dev/null 2>&1; then
        echo -e "${GREEN}✅ $service_name - Healthy on port $port${NC}"
        return 0
    else
        echo -e "${YELLOW}⚠️  $service_name - Port $port open but health check failed${NC}"
        return 1
    fi
}

# Function to get service details
get_service_details() {
    local service_name=$1
    local port=$2
    local health_url=$3
    
    if netstat -tuln 2>/dev/null | grep -q ":$port "; then
        local response=$(curl -s --connect-timeout 5 --max-time 10 "$health_url" 2>/dev/null || echo "")
        if [ ! -z "$response" ]; then
            echo -e "${BLUE}  Health Response: $response${NC}"
        fi
    fi
}

echo "🔍 Checking Modern Reservation System Services Status..."
echo ""

echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}                    INFRASTRUCTURE SERVICES                    ${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"

# Infrastructure services
INFRA_HEALTHY=0
INFRA_TOTAL=4

echo -e "${BLUE}Config Server:${NC}"
if check_service_health "Config Server" "8888" "http://localhost:8888/actuator/health"; then
    ((INFRA_HEALTHY++))
    get_service_details "Config Server" "8888" "http://localhost:8888/actuator/health"
fi

echo ""
echo -e "${BLUE}Eureka Server:${NC}"
if check_service_health "Eureka Server" "8761" "http://localhost:8761/actuator/health"; then
    ((INFRA_HEALTHY++))
    get_service_details "Eureka Server" "8761" "http://localhost:8761/actuator/health"
fi

echo ""
echo -e "${BLUE}Zipkin Server:${NC}"
if check_service_health "Zipkin Server" "9411" "http://localhost:9411/actuator/health"; then
    ((INFRA_HEALTHY++))
    get_service_details "Zipkin Server" "9411" "http://localhost:9411/actuator/health"
fi

echo ""
echo -e "${BLUE}Gateway Service:${NC}"
if check_service_health "Gateway Service" "8080" "http://localhost:8080/actuator/health"; then
    ((INFRA_HEALTHY++))
    get_service_details "Gateway Service" "8080" "http://localhost:8080/actuator/health"
fi

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}                     BUSINESS SERVICES                         ${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"

# Business services
BUSINESS_HEALTHY=0
BUSINESS_TOTAL=5

echo -e "${BLUE}Reservation Engine:${NC}"
if check_service_health "Reservation Engine" "8081" "http://localhost:8081/actuator/health"; then
    ((BUSINESS_HEALTHY++))
    get_service_details "Reservation Engine" "8081" "http://localhost:8081/actuator/health"
fi

echo ""
echo -e "${BLUE}Availability Calculator:${NC}"
if check_service_health "Availability Calculator" "8083" "http://localhost:8083/actuator/health"; then
    ((BUSINESS_HEALTHY++))
    get_service_details "Availability Calculator" "8083" "http://localhost:8083/actuator/health"
fi

echo ""
echo -e "${BLUE}Payment Processor:${NC}"
if check_service_health "Payment Processor" "8084" "http://localhost:8084/actuator/health"; then
    ((BUSINESS_HEALTHY++))
    get_service_details "Payment Processor" "8084" "http://localhost:8084/actuator/health"
fi

echo ""
echo -e "${BLUE}Rate Management:${NC}"
if check_service_health "Rate Management" "8085" "http://localhost:8085/actuator/health"; then
    ((BUSINESS_HEALTHY++))
    get_service_details "Rate Management" "8085" "http://localhost:8085/actuator/health"
fi

echo ""
echo -e "${BLUE}Analytics Engine:${NC}"
if check_service_health "Analytics Engine" "8086" "http://localhost:8086/actuator/health"; then
    ((BUSINESS_HEALTHY++))
    get_service_details "Analytics Engine" "8086" "http://localhost:8086/actuator/health"
fi

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}                        SYSTEM SUMMARY                         ${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"

TOTAL_HEALTHY=$((INFRA_HEALTHY + BUSINESS_HEALTHY))
TOTAL_SERVICES=$((INFRA_TOTAL + BUSINESS_TOTAL))

echo -e "${YELLOW}Infrastructure Services: $INFRA_HEALTHY/$INFRA_TOTAL healthy${NC}"
echo -e "${YELLOW}Business Services: $BUSINESS_HEALTHY/$BUSINESS_TOTAL healthy${NC}"
echo -e "${YELLOW}Total System Health: $TOTAL_HEALTHY/$TOTAL_SERVICES services${NC}"

if [ $TOTAL_HEALTHY -eq $TOTAL_SERVICES ]; then
    echo -e "${GREEN}🎉 All services are healthy and running!${NC}"
elif [ $INFRA_HEALTHY -eq $INFRA_TOTAL ]; then
    echo -e "${YELLOW}⚠️  Infrastructure is healthy, some business services may be down${NC}"
elif [ $INFRA_HEALTHY -gt 0 ]; then
    echo -e "${RED}❌ Some infrastructure services are down - system may not function properly${NC}"
else
    echo -e "${RED}❌ Infrastructure services are down - system is not operational${NC}"
fi

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}                       SERVICE DISCOVERY                       ${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"

# Check Eureka registered services
if [ $INFRA_HEALTHY -ge 2 ] && netstat -tuln 2>/dev/null | grep -q ":8761 "; then
    echo -e "${BLUE}Registered services in Eureka:${NC}"
    eureka_response=$(curl -s "http://localhost:8761/eureka/apps" -H "Accept: application/json" 2>/dev/null || echo "")
    if [ ! -z "$eureka_response" ]; then
        echo "$eureka_response" | jq -r '.applications.application[]?.name // empty' 2>/dev/null | while read -r service; do
            if [ ! -z "$service" ]; then
                echo -e "${GREEN}  • $service${NC}"
            fi
        done
    else
        echo -e "${YELLOW}  Could not retrieve registered services${NC}"
    fi
else
    echo -e "${RED}  Eureka Server not available - cannot check registered services${NC}"
fi

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}                         QUICK LINKS                           ${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"

echo -e "${YELLOW}Management URLs:${NC}"
echo "• Config Server:    http://localhost:8888/config"
echo "• Eureka Dashboard: http://localhost:8761"  
echo "• Zipkin UI:        http://localhost:9411"
echo "• API Gateway:      http://localhost:8080"

echo ""
echo -e "${YELLOW}Health Check URLs:${NC}"
echo "• Config Server:    http://localhost:8888/actuator/health"
echo "• Eureka Server:    http://localhost:8761/actuator/health"
echo "• Zipkin Server:    http://localhost:9411/actuator/health"
echo "• Gateway Service:  http://localhost:8080/actuator/health"
echo "• Reservation:      http://localhost:8081/actuator/health"
echo "• Availability:     http://localhost:8083/actuator/health"
echo "• Payment:          http://localhost:8084/actuator/health"
echo "• Rate Mgmt:        http://localhost:8085/actuator/health"
echo "• Analytics:        http://localhost:8086/actuator/health"

echo ""
echo -e "${YELLOW}Management Commands:${NC}"
echo "• Start all services:       ./start-infrastructure.sh"
echo "• Stop all services:        ./stop-infrastructure.sh"
echo "• Start infrastructure:     ./start-infrastructure.sh --infrastructure-only"
echo "• Start specific service:   ./start-infrastructure.sh --service <service-name>"
echo "• Check status again:       ./check-services.sh"

# Exit with error code if not all services are healthy
if [ $TOTAL_HEALTHY -ne $TOTAL_SERVICES ]; then
    exit 1
fi