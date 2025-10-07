# Scripts Directory

This folder contains all operational scripts for managing the Modern Reservation System.

## 📋 Available Scripts

### 🚀 Main Management Scripts

#### `infra.sh` - Infrastructure Management
Main entry point for managing all services.

```bash
./scripts/infra.sh start-all              # Start all services
./scripts/infra.sh stop-all               # Stop all services
./scripts/infra.sh status-all             # Check all services status
./scripts/infra.sh start                  # Start infrastructure only
./scripts/infra.sh stop                   # Stop infrastructure only
./scripts/infra.sh start-business         # Start business services only
./scripts/infra.sh stop-business          # Stop business services only
./scripts/infra.sh start-all --restart    # Force restart all
```

**Uses**: Coordinates between infrastructure and business service scripts.

---

#### `clean-restart.sh` - Complete Clean Restart
Performs a full cleanup and restart from scratch. Perfect for testing!

```bash
./scripts/clean-restart.sh                          # Full clean restart
./scripts/clean-restart.sh --keep-data              # Keep database data
./scripts/clean-restart.sh --skip-maven-clean       # Skip Maven rebuild
./scripts/clean-restart.sh --skip-db-setup          # Skip database init
./scripts/clean-restart.sh --help                   # Show help
```

**What it does**:
1. Stops all services (Java + Docker)
2. Removes Docker containers/volumes
3. Frees up all ports
4. Cleans and rebuilds Maven artifacts
5. Starts all infrastructure services
6. Verifies health of all services
7. Initializes database schema
8. Builds and starts reservation-engine
9. Tests Avro event publishing

**Time**: 2-3 minutes for full clean restart
**Documentation**: See `docs/CLEAN_RESTART_GUIDE.md`

---

### 🏗️ Infrastructure Scripts

#### `start-infrastructure.sh`
Starts all infrastructure services:
- Config Server (8888)
- Eureka Server (8761)
- API Gateway (8080)
- Zipkin (9411)
- Docker services (PostgreSQL, Redis, Kafka, etc.)

```bash
./scripts/start-infrastructure.sh
./scripts/start-infrastructure.sh --restart    # Force restart
```

#### `stop-infrastructure.sh`
Gracefully stops all infrastructure services.

```bash
./scripts/stop-infrastructure.sh
```

#### `check-infrastructure.sh`
Checks health and status of all infrastructure services.

```bash
./scripts/check-infrastructure.sh
```

---

### 💼 Business Service Scripts

#### `start-business-services.sh`
Starts all business microservices:
- Reservation Engine (8081)
- Availability Calculator (8083)
- Payment Processor (8084)
- Rate Management (8085)
- Analytics Engine (8086)

```bash
./scripts/start-business-services.sh
./scripts/start-business-services.sh --restart    # Force restart
```

#### `stop-business-services.sh`
Gracefully stops all business services.

```bash
./scripts/stop-business-services.sh
```

#### `check-business-services.sh`
Checks health and status of all business services.

```bash
./scripts/check-business-services.sh
```

---

### 🐳 Docker Management

#### `docker-infra.sh`
Manages Docker infrastructure services directly.

```bash
./scripts/docker-infra.sh infra-start       # Start Docker infrastructure
./scripts/docker-infra.sh infra-stop        # Stop Docker infrastructure
./scripts/docker-infra.sh health            # Check Docker health
./scripts/docker-infra.sh zipkin            # Start only Zipkin
./scripts/docker-infra.sh postgres          # Start only PostgreSQL
./scripts/docker-infra.sh redis             # Start only Redis
./scripts/docker-infra.sh logs [service]    # View logs
./scripts/docker-infra.sh ps                # Show containers
./scripts/docker-infra.sh cleanup           # Clean up containers
```

---

### 💾 Database Scripts

#### `setup-database.sh`
Initializes the database schema.

```bash
./scripts/setup-database.sh
```

**What it does**:
- Connects to PostgreSQL
- Creates `modern_reservation_dev` database
- Runs all schema files in order
- Loads reference data

#### `backup-database.sh`
Creates a backup of the database.

```bash
./scripts/backup-database.sh
```

---

### 🧪 Testing Scripts

#### `test-avro-event.sh`
Tests Avro event publishing end-to-end.

```bash
./scripts/test-avro-event.sh
```

**What it does**:
1. Checks Schema Registry connectivity
2. Shows current registered schemas
3. Creates a test reservation
4. Verifies event published to Kafka
5. Checks if schema was registered
6. Displays schema details

---

### 🔍 Utility Scripts

#### `check-dependencies.sh`
Checks if all required dependencies are installed.

```bash
./scripts/check-dependencies.sh
```

Verifies:
- Java 17+
- Maven 3.8+
- Node.js 18+
- Docker & Docker Compose
- PostgreSQL client
- Redis client

---

## 📊 Script Hierarchy

```
infra.sh (Main Entry Point)
├── start-infrastructure.sh
│   ├── docker-infra.sh infra-start
│   └── Individual service startup
├── stop-infrastructure.sh
│   ├── docker-infra.sh infra-stop
│   └── Individual service shutdown
├── start-business-services.sh
│   └── Maven spring-boot:run for each service
├── stop-business-services.sh
│   └── Graceful shutdown of business services
└── check-*.sh
    └── Health checks for all services

clean-restart.sh (Special Purpose)
├── Uses: infra.sh stop-all
├── Docker: docker-compose down -v
├── Maven: clean + install
├── Uses: docker-compose up -d
├── Uses: setup-database.sh
└── Tests: test-avro-event.sh logic
```

---

## 🎯 Common Workflows

### Morning Startup
```bash
# Quick start with existing data
./scripts/infra.sh start-all
```

### Fresh Start (Testing)
```bash
# Complete clean restart
./scripts/clean-restart.sh
```

### Code Changes
```bash
# Restart just business services
./scripts/infra.sh stop-business
# ... make changes ...
./scripts/infra.sh start-business
```

### End of Day
```bash
# Stop everything
./scripts/infra.sh stop-all
```

### Debugging
```bash
# Check what's running
./scripts/infra.sh status-all

# Check specific service
./scripts/check-business-services.sh

# View logs
docker logs modern-reservation-kafka
tail -f /tmp/reservation-engine.log
```

---

## 🔧 Configuration

### Environment Variables

Scripts respect these environment variables:

```bash
# For clean-restart.sh
export KEEP_DATA=true              # Keep database volumes
export SKIP_MAVEN_CLEAN=true       # Skip Maven clean
export SKIP_DATABASE_SETUP=true    # Skip DB initialization

# For infrastructure scripts
export CONFIG_SERVER_URL=http://localhost:8888
export EUREKA_SERVER_URL=http://localhost:8761
```

### PID Files

Service PID files are stored in the project root:
- `config-server.pid`
- `eureka-server.pid`
- `gateway-service.pid`
- `reservation-engine.pid`
- etc.

### Log Files

Service logs are stored in `/tmp/`:
- `/tmp/reservation-engine.log`
- `/tmp/config-server.log`
- `/tmp/eureka-server.log`
- etc.

---

## 🐛 Troubleshooting

### Script Won't Execute
```bash
# Make sure it's executable
chmod +x ./scripts/script-name.sh
```

### Port Already in Use
```bash
# Find what's using the port
lsof -i :8081

# Kill the process
kill -9 <PID>

# Or use clean-restart which handles this
./scripts/clean-restart.sh
```

### Docker Services Not Starting
```bash
# Check Docker status
docker ps -a | grep modern-reservation

# View logs
docker logs modern-reservation-postgres

# Restart infrastructure
./scripts/docker-infra.sh infra-stop
./scripts/docker-infra.sh infra-start
```

### Service Won't Stop
```bash
# Force stop all
pkill -9 -f spring-boot:run

# Clean restart
./scripts/clean-restart.sh
```

---

## 📚 Related Documentation

- **Clean Restart Guide**: `docs/CLEAN_RESTART_GUIDE.md`
- **Avro Quick Reference**: `docs/AVRO_QUICK_REFERENCE.md`
- **Avro Migration**: `AVRO_MIGRATION_COMPLETE.md`
- **Infrastructure Setup**: `infrastructure/README.md`
- **Development Guide**: `docs/development/`

---

## 🎨 Script Conventions

All scripts follow these conventions:

1. **Shebang**: `#!/bin/bash`
2. **Error Handling**: `set -e` (exit on error)
3. **Color Output**: Consistent color scheme
   - 🔵 Blue: Info/Status
   - 🟢 Green: Success
   - 🟡 Yellow: Warning
   - 🔴 Red: Error
4. **Path Detection**: Dynamic script directory detection
5. **Logging**: Timestamp-prefixed messages
6. **Exit Codes**: 0 for success, 1 for failure

---

## 💡 Tips

1. **Use Tab Completion**: All scripts support bash completion
2. **Check Status First**: Run `status-all` before starting services
3. **Read Logs**: Always check logs when services fail
4. **Clean Restart for Tests**: Use `clean-restart.sh` for reliable testing
5. **Keep Data During Development**: Use `--keep-data` flag to speed up restarts

---

**Last Updated**: 2025-10-07
**Maintained By**: Development Team
**Questions?** Check the documentation in `docs/` or run scripts with `--help`
