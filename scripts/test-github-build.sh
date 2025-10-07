#!/bin/bash

# GitHub Actions Build Diagnostic Script
# Run this locally to simulate the GitHub Actions build

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}╔══════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  GitHub Actions Build - Local Simulation               ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════════════╝${NC}"
echo ""

# Check Java version
echo -e "${YELLOW}📋 Checking Java version...${NC}"
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
echo "Current Java version: $JAVA_VERSION"

if [ "$JAVA_VERSION" != "21" ]; then
    echo -e "${RED}❌ ERROR: Java 21 required, but found Java $JAVA_VERSION${NC}"
    echo ""
    echo "Please install Java 21:"
    echo "  sudo apt install openjdk-21-jdk"
    echo ""
    echo "Or set JAVA_HOME to Java 21:"
    echo "  export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64"
    exit 1
fi
echo -e "${GREEN}✅ Java 21 detected${NC}"
echo ""

# Check Maven
echo -e "${YELLOW}📋 Checking Maven...${NC}"
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}❌ Maven not found${NC}"
    echo "Install Maven: sudo apt install maven"
    exit 1
fi
mvn --version
echo -e "${GREEN}✅ Maven detected${NC}"
echo ""

# Step 0: Install Parent POM
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo -e "${YELLOW}🏗️  Step 0: Installing Parent POM${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
cd "$PROJECT_ROOT/apps/backend/java-services"
if mvn clean install -N -DskipTests; then
    echo -e "${GREEN}✅ Parent POM installed successfully${NC}"
else
    echo -e "${RED}❌ Parent POM installation failed${NC}"
    exit 1
fi
echo ""

# Step 1: Build Shared Backend Utils
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo -e "${YELLOW}🏗️  Step 1: Building Shared Backend Utils${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
cd "$PROJECT_ROOT/libs/shared/backend-utils"
if mvn clean install -DskipTests; then
    echo -e "${GREEN}✅ Backend Utils built successfully${NC}"
else
    echo -e "${RED}❌ Backend Utils build failed${NC}"
    exit 1
fi
echo ""

# Step 2: Build Infrastructure Services
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo -e "${YELLOW}🏗️  Step 2: Building Infrastructure Services${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"

cd "$PROJECT_ROOT/apps/backend/java-services/infrastructure"

INFRA_SERVICES=(config-server eureka-server gateway-service)
INFRA_SUCCESS=0
INFRA_FAILED=0

for service in "${INFRA_SERVICES[@]}"; do
    if [ -d "$service" ] && [ -f "$service/pom.xml" ]; then
        echo ""
        echo -e "${YELLOW}Building $service...${NC}"
        cd "$service"
        if mvn clean package -DskipTests; then
            echo -e "${GREEN}✅ $service built successfully${NC}"
            ((INFRA_SUCCESS++))
        else
            echo -e "${RED}❌ $service build failed${NC}"
            ((INFRA_FAILED++))
        fi
        cd ..
    fi
done

echo ""
echo -e "${BLUE}Infrastructure Summary: ${GREEN}$INFRA_SUCCESS passed${NC}, ${RED}$INFRA_FAILED failed${NC}"
echo ""

# Step 3: Build Business Services
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo -e "${YELLOW}🏗️  Step 3: Building Business Services${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"

cd "$PROJECT_ROOT/apps/backend/java-services/business-services"

BUSINESS_SERVICES=(reservation-engine payment-processor rate-management availability-calculator analytics-engine batch-processor)
BUSINESS_SUCCESS=0
BUSINESS_FAILED=0

for service in "${BUSINESS_SERVICES[@]}"; do
    if [ -d "$service" ] && [ -f "$service/pom.xml" ]; then
        echo ""
        echo -e "${YELLOW}Building $service...${NC}"
        cd "$service"
        if mvn clean package -DskipTests; then
            echo -e "${GREEN}✅ $service built successfully${NC}"
            ((BUSINESS_SUCCESS++))
        else
            echo -e "${RED}❌ $service build failed${NC}"
            ((BUSINESS_FAILED++))
        fi
        cd ..
    fi
done

echo ""
echo -e "${BLUE}Business Services Summary: ${GREEN}$BUSINESS_SUCCESS passed${NC}, ${RED}$BUSINESS_FAILED failed${NC}"
echo ""

# Final Summary
echo -e "${BLUE}╔══════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Build Summary                                           ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════════════╝${NC}"
echo ""
echo "Infrastructure Services:"
echo -e "  ${GREEN}✅ Passed: $INFRA_SUCCESS${NC}"
echo -e "  ${RED}❌ Failed: $INFRA_FAILED${NC}"
echo ""
echo "Business Services:"
echo -e "  ${GREEN}✅ Passed: $BUSINESS_SUCCESS${NC}"
echo -e "  ${RED}❌ Failed: $BUSINESS_FAILED${NC}"
echo ""

TOTAL_FAILED=$((INFRA_FAILED + BUSINESS_FAILED))
if [ $TOTAL_FAILED -eq 0 ]; then
    echo -e "${GREEN}🎉 All builds passed! Ready to push to GitHub.${NC}"
    exit 0
else
    echo -e "${RED}❌ $TOTAL_FAILED service(s) failed to build${NC}"
    echo ""
    echo "Tips:"
    echo "  1. Check error messages above"
    echo "  2. Ensure Java 21 is being used"
    echo "  3. Verify pom.xml dependencies"
    echo "  4. Try: ./dev.sh clean"
    exit 1
fi
