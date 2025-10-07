# dev.sh - Main Entry Point

**The single command you need to remember: `./dev.sh`**

## 🎯 Quick Start

```bash
# See all commands
./dev.sh help

# Start everything
./dev.sh start

# Check status
./dev.sh status

# Stop everything
./dev.sh stop

# Clean restart (testing)
./dev.sh clean
```

## 📋 Command Categories

### 🚀 Service Management
```bash
./dev.sh start              # Start all services
./dev.sh start-infra        # Start infrastructure only
./dev.sh start-business     # Start business services only
./dev.sh start --restart    # Force restart

./dev.sh stop               # Stop all services
./dev.sh stop-infra         # Stop infrastructure only
./dev.sh stop-business      # Stop business services only

./dev.sh status             # Check all services
./dev.sh status-infra       # Check infrastructure
./dev.sh status-business    # Check business services
```

### 🔄 Clean Restart
```bash
./dev.sh clean                      # Full clean restart
./dev.sh clean --keep-data          # Keep database data
./dev.sh clean --skip-maven         # Skip Maven rebuild
./dev.sh clean --skip-db            # Skip database init
./dev.sh clean --help               # More options
```

### 🧪 Testing
```bash
./dev.sh test-avro          # Test Avro events
./dev.sh check-deps         # Check dependencies
./dev.sh check-health       # Full health check
```

### 💾 Database
```bash
./dev.sh db-setup           # Initialize database
./dev.sh db-backup          # Backup database
./dev.sh db-connect         # Connect to PostgreSQL
```

### 🐳 Docker
```bash
./dev.sh docker-start       # Start Docker infrastructure
./dev.sh docker-stop        # Stop Docker infrastructure
./dev.sh docker-status      # Check Docker health
./dev.sh docker-logs kafka  # View service logs
./dev.sh docker-clean       # Clean up containers
```

### 📊 Monitoring
```bash
./dev.sh logs reservation-engine    # View service logs
./dev.sh ui-kafka                   # Open Kafka UI
./dev.sh ui-eureka                  # Open Eureka Dashboard
./dev.sh ui-zipkin                  # Open Zipkin
./dev.sh ui-pgadmin                 # Open PgAdmin
```

## 💡 Common Workflows

### Morning Startup
```bash
./dev.sh start
```

### Testing/Debugging (Fresh Environment)
```bash
./dev.sh clean
```

### Quick Restart (Keep Data)
```bash
./dev.sh clean --keep-data --skip-maven
```

### Code Changes Only
```bash
./dev.sh stop-business
# ... make changes ...
./dev.sh start-business
```

### End of Day
```bash
./dev.sh stop
```

### Check What's Running
```bash
./dev.sh status
```

### View Logs
```bash
./dev.sh logs reservation-engine
./dev.sh logs kafka
```

### Database Access
```bash
./dev.sh db-connect
```

## 🎨 Command Aliases

Many commands have shortcuts:

```bash
# These are the same:
./dev.sh start-all          = ./dev.sh start
./dev.sh stop-all           = ./dev.sh stop
./dev.sh status-all         = ./dev.sh status
./dev.sh check              = ./dev.sh status

# Clean restart aliases:
./dev.sh clean              = ./dev.sh clean-restart
./dev.sh clean              = ./dev.sh restart-clean

# Infrastructure shortcuts:
./dev.sh start-infra        = ./dev.sh start-infrastructure
./dev.sh stop-infra         = ./dev.sh stop-infrastructure
./dev.sh status-infra       = ./dev.sh status-infrastructure

# Database shortcuts:
./dev.sh db-setup           = ./dev.sh setup-db
./dev.sh db-setup           = ./dev.sh init-db
./dev.sh db-backup          = ./dev.sh backup-db
./dev.sh db-connect         = ./dev.sh psql

# Docker shortcuts:
./dev.sh docker-start       = ./dev.sh docker-up
./dev.sh docker-stop        = ./dev.sh docker-down
./dev.sh docker-clean       = ./dev.sh docker-cleanup

# Testing shortcuts:
./dev.sh test-avro          = ./dev.sh avro-test
./dev.sh check-deps         = ./dev.sh check-dependencies
./dev.sh check-deps         = ./dev.sh deps
./dev.sh check-health       = ./dev.sh health

# UI shortcuts:
./dev.sh ui-kafka           = ./dev.sh kafka-ui
./dev.sh ui-eureka          = ./dev.sh eureka
./dev.sh ui-zipkin          = ./dev.sh zipkin
./dev.sh ui-pgadmin         = ./dev.sh pgadmin
```

## 🔍 How It Works

`dev.sh` is a wrapper that delegates to scripts in the `scripts/` folder:

```
dev.sh
├── start          → scripts/infra.sh start-all
├── stop           → scripts/infra.sh stop-all
├── status         → scripts/infra.sh status-all
├── clean          → scripts/clean-restart.sh
├── test-avro      → scripts/test-avro-event.sh
├── db-setup       → scripts/setup-database.sh
├── docker-start   → scripts/docker-infra.sh infra-start
└── ...
```

## 📚 Related Documentation

- **Complete Guide**: `scripts/README.md`
- **Clean Restart**: `docs/CLEAN_RESTART_GUIDE.md`
- **Avro Guide**: `docs/AVRO_QUICK_REFERENCE.md`

## 🆘 Need Help?

```bash
# Show all commands
./dev.sh help

# Show clean restart options
./dev.sh clean --help

# Check available logs
./dev.sh logs
```

---

**Remember**: Just use `./dev.sh` for everything! 🚀
