# 🔄 Clean Restart Guide

## Quick Start

The `clean-restart.sh` script provides a one-command solution to reset and test your entire application from scratch.

## Basic Usage

```bash
# Full clean restart (recommended for testing)
./scripts/clean-restart.sh

# Keep database data
./scripts/clean-restart.sh --keep-data

# Skip Maven clean (faster restart)
./scripts/clean-restart.sh --skip-maven-clean

# Skip database initialization
./scripts/clean-restart.sh --skip-db-setup

# Combine options
./scripts/clean-restart.sh --keep-data --skip-maven-clean
```

## What It Does

The script performs a complete 9-step process:

### 1️⃣ **Stop All Services**
- Stops business services (reservation-engine, etc.)
- Stops infrastructure services (Eureka, Config Server)
- Kills any remaining Spring Boot processes

### 2️⃣ **Clean Docker Resources**
- Removes all modern-reservation containers
- Removes networks
- Optionally removes volumes (unless `--keep-data` is used)

### 3️⃣ **Verify Ports**
- Checks ports: 5432, 6379, 8081, 8085, 8090, 8761, 8888, 9092, 9411
- Kills any processes blocking these ports
- Ensures clean slate for restart

### 4️⃣ **Clean Maven Artifacts**
- Cleans backend-utils
- Rebuilds with Avro schema generation
- Cleans reservation-engine
- Ensures fresh builds

### 5️⃣ **Start Infrastructure**
- Starts Docker containers:
  - PostgreSQL (5432)
  - Redis (6379)
  - Kafka (9092)
  - Schema Registry (8085)
  - Kafka UI (8090)
  - Zipkin (9411)
  - PgAdmin (5050)

### 6️⃣ **Verify Health**
- Waits for each service to be ready
- Tests connectivity
- Ensures all services are healthy

### 7️⃣ **Initialize Database**
- Runs database setup script
- Creates tables and schema
- Loads reference data

### 8️⃣ **Build & Start Services**
- Compiles reservation-engine
- Starts with Avro support
- Waits for service to be ready

### 9️⃣ **Test Avro Events**
- Publishes test event to Kafka
- Verifies schema registration
- Confirms end-to-end flow

---

## Script Options

| Option | Description |
|--------|-------------|
| `--keep-data` | Preserves database volumes (don't delete data) |
| `--skip-maven-clean` | Skip Maven clean build (faster, use existing artifacts) |
| `--skip-db-setup` | Skip database initialization (assumes DB already set up) |
| `--help, -h` | Show help message |

---

## Use Cases

### 🧪 **Full Clean Test** (Recommended)
```bash
./scripts/clean-restart.sh
```
**When**: Testing from scratch, debugging issues, verifying clean slate
**Time**: ~2-3 minutes

### ⚡ **Quick Restart** (Keep Data)
```bash
./scripts/clean-restart.sh --keep-data --skip-maven-clean
```
**When**: Quick restart without losing database data
**Time**: ~1-2 minutes

### 🔄 **Code Changes Only**
```bash
./scripts/clean-restart.sh --keep-data --skip-db-setup
```
**When**: You changed code but DB schema is same
**Time**: ~1.5 minutes

### 💾 **Fresh Database** (Keep Builds)
```bash
./scripts/clean-restart.sh --skip-maven-clean
```
**When**: Testing database migrations, want fresh DB but not rebuild
**Time**: ~1.5 minutes

---

## After Script Completes

### ✅ Services Available

**Infrastructure:**
- PostgreSQL: `localhost:5432`
- Redis: `localhost:6379`
- Kafka: `localhost:9092`
- Schema Registry: `http://localhost:8085`
- Kafka UI: `http://localhost:8090`
- Zipkin: `http://localhost:9411`
- PgAdmin: `http://localhost:5050`

**Business Services:**
- Reservation Engine: `http://localhost:8081/reservation-engine`

### 🧪 Test Commands

The script prints the test commands at the end. Example:

```bash
# Test Kafka event publishing
curl -u user:PASSWORD http://localhost:8081/reservation-engine/api/test/kafka

# Check registered schemas
curl http://localhost:8085/subjects

# View Kafka UI
open http://localhost:8090
```

**Password Location**: `/tmp/reservation-engine-password.txt`

### 📊 Monitoring

```bash
# View service logs
tail -f /tmp/reservation-engine.log

# Check service status
./infra.sh status-all

# Check infrastructure health
./infra.sh status

# View Docker containers
docker ps | grep modern-reservation
```

---

## Troubleshooting

### Script Fails at Port Check
**Problem**: Ports are still in use

**Solution**:
```bash
# Find what's using the port
lsof -i :8081

# Kill the process
kill -9 <PID>

# Or let script retry
./scripts/clean-restart.sh
```

### Schema Registry Not Starting
**Problem**: Schema Registry fails health check

**Solution**:
```bash
# Check logs
docker logs modern-reservation-schema-registry

# Restart just Schema Registry
docker restart modern-reservation-schema-registry

# Wait and check
curl http://localhost:8085/
```

### Reservation Engine Fails to Start
**Problem**: Service crashes during startup

**Solution**:
```bash
# Check logs
tail -100 /tmp/reservation-engine.log

# Look for errors
grep -i "error\|exception" /tmp/reservation-engine.log

# Check if port 8081 is blocked
lsof -i :8081
```

### Maven Build Fails
**Problem**: Avro generation or compilation errors

**Solution**:
```bash
# Clean everything
cd libs/shared/backend-utils
mvn clean install -DskipTests

# Check generated files
ls -la target/generated-sources/avro/

# Retry script
cd ../../../..
./clean-restart.sh --skip-db-setup
```

---

## Integration with Existing Scripts

The `clean-restart.sh` script uses your existing infrastructure scripts:

```bash
# Uses:
./scripts/infra.sh stop-all           # Stop services
./scripts/infra.sh stop-business      # Stop business services
./scripts/setup-database.sh           # Initialize database

# Compatible with:
./scripts/infra.sh start-all          # Start services normally
./scripts/infra.sh status-all         # Check status
./scripts/docker-infra.sh health      # Docker health
```

---

## Examples

### Daily Development
```bash
# Morning: Start fresh
./scripts/clean-restart.sh --keep-data --skip-maven-clean

# During day: Quick restarts
./scripts/infra.sh stop-business
./scripts/infra.sh start-business

# Evening: Full cleanup
./scripts/infra.sh stop-all
```

### Testing Avro Changes
```bash
# 1. Modify Avro schemas
vim libs/shared/backend-utils/src/main/avro/ReservationCreatedEvent.avsc

# 2. Clean restart (rebuilds Avro)
./scripts/clean-restart.sh --keep-data

# 3. Test
curl -u user:$(cat /tmp/reservation-engine-password.txt) \
  http://localhost:8081/reservation-engine/api/test/kafka

# 4. Check new schema
curl http://localhost:8085/subjects/reservation.created-value/versions/latest | jq .
```

### Database Schema Changes
```bash
# 1. Modify SQL schema
vim database/schema/03-reservation-management.sql

# 2. Full restart with new DB
./scripts/clean-restart.sh

# 3. Verify tables
docker exec -it modern-reservation-postgres psql -U postgres -d modern_reservation_dev -c "\dt"
```

### CI/CD Integration
```bash
# In your CI pipeline
./scripts/clean-restart.sh --skip-maven-clean
if [ $? -eq 0 ]; then
  echo "All tests passed!"
  # Run integration tests here
else
  echo "Startup failed!"
  exit 1
fi
```

---

## Performance Tips

### Fastest Restart (Development)
```bash
# Only restart what changed
./scripts/infra.sh stop-business
# ... make code changes ...
cd apps/backend/java-services/business-services/reservation-engine
mvn spring-boot:run
```

### Faster Full Restart
```bash
# Skip rebuilding if no code changes
./scripts/clean-restart.sh --keep-data --skip-maven-clean --skip-db-setup
```

### Full Clean (Testing/Debugging)
```bash
# Nuclear option - everything from scratch
./scripts/clean-restart.sh
```

---

## What Gets Preserved

### With `--keep-data`:
- ✅ Database tables and data
- ✅ Redis cache data
- ✅ Kafka topics and messages
- ❌ Running processes (all killed)
- ❌ PID files (all removed)

### Without `--keep-data`:
- ❌ Everything removed
- ✅ Fresh database
- ✅ Clean Kafka
- ✅ Empty Redis

---

## Script Output

The script provides detailed, color-coded output:

```
🔄 CLEAN RESTART - Modern Reservation System
╔════════════════════════════════════════════════════════════════╗
║ 📛 STEP 1: Stopping All Services
╚════════════════════════════════════════════════════════════════╝

▶ Stopping business services...
  ✅ Business services stopped

▶ Stopping infrastructure services...
  ✅ Infrastructure services stopped

... (continues for all 9 steps) ...

✨ CLEAN RESTART COMPLETE!
╔════════════════════════════════════════════════════════════════╗
║                    SERVICES READY                              ║
╚════════════════════════════════════════════════════════════════╝

✅ Total time: 127s
✅ All systems operational!
```

---

## Quick Reference Card

```bash
# Clean everything and restart
./scripts/clean-restart.sh

# Quick restart (keep data)
./scripts/clean-restart.sh --keep-data --skip-maven-clean

# After restart, test:
curl -u user:$(cat /tmp/reservation-engine-password.txt) \
  http://localhost:8081/reservation-engine/api/test/kafka

# Check status:
./scripts/infra.sh status-all

# View logs:
tail -f /tmp/reservation-engine.log

# Stop everything:
./scripts/infra.sh stop-all
```

---

**Created**: 2025-10-07
**Purpose**: Streamline development testing workflow
**Frequency**: Use whenever you need a clean slate for testing
