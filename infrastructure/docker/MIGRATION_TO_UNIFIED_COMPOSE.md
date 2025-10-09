# Docker Compose Migration - Consolidated to Single File

## 📋 Migration Summary

Previously, the Modern Reservation system used **3 separate Docker Compose files**:
- `docker-compose-infrastructure.yml` - External dependencies (PostgreSQL, Redis, Kafka, etc.)
- `docker-compose-observability.yml` - OpenTelemetry stack (Jaeger, Prometheus, Grafana)
- `docker-compose-services.yml` - Business application services

These have been **consolidated into a single `docker-compose.yml`** for simplified management.

## 🎯 Benefits of Unified Approach

### ✅ **Simplified Operations**
- Single entry point: `docker-compose.yml`
- No need to remember multiple file names
- Consistent service startup order with proper dependencies

### ✅ **Better Dependency Management**
- Clear service layer organization (Infrastructure → Observability → Applications)
- Proper health checks and startup order
- Single network configuration

### ✅ **Enhanced Developer Experience**
- `./dev.sh docker-start` - Start complete stack with one command
- Clear service grouping in the compose file
- Updated dev.sh commands for new observability stack

## 🏗️ New Service Architecture

### **Infrastructure Layer** (External Dependencies)
```yaml
services:
  postgres:     # Database
  pgadmin:      # Database UI
  redis:        # Cache
  kafka:        # Message Queue
  schema-registry: # Avro Schema Management
  kafka-ui:     # Kafka Monitoring
```

### **Observability Layer** (OpenTelemetry Stack)
```yaml
services:
  otel-collector: # OpenTelemetry data processing
  jaeger:         # Distributed tracing UI
  prometheus:     # Metrics collection
  grafana:        # Visualization dashboards
```

### **Application Layer** (Business Services)
```yaml
services:
  gateway-service:    # API Gateway
  reservation-engine: # Core business logic
  payment-processor:  # Payment handling
```

## 🔄 Migration Changes

### **Removed Legacy Services**
- ❌ **Zipkin** - Replaced with **Jaeger + OpenTelemetry**
- All service configurations updated to use OpenTelemetry OTLP endpoints

### **Updated Service Configuration**
- **Environment Variables**: Services now use `OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4317`
- **Dependencies**: Proper service startup order with health checks
- **Network**: Single `modern-reservation-network` for all services

### **Enhanced dev.sh Commands**
```bash
# New observability UI commands
./dev.sh ui-jaeger      # http://localhost:16686
./dev.sh ui-prometheus  # http://localhost:9090
./dev.sh ui-grafana     # http://localhost:3000

# Improved Docker commands
./dev.sh docker-start   # Start complete stack
./dev.sh docker-stop    # Stop all services
./dev.sh docker-status  # Check health of all services
```

## 📊 Access Points

| Service | URL | Purpose |
|---------|-----|---------|
| **Jaeger** | http://localhost:16686 | Distributed tracing |
| **Prometheus** | http://localhost:9090 | Metrics collection |
| **Grafana** | http://localhost:3000 | Dashboards (admin/admin123) |
| **Kafka UI** | http://localhost:8090 | Message queue monitoring |
| **PgAdmin** | http://localhost:5050 | Database management |

## 🔧 Usage Examples

### Start Complete Stack
```bash
./dev.sh docker-start
# Starts: PostgreSQL, Redis, Kafka, Jaeger, Prometheus, Grafana, OpenTelemetry Collector
```

### Start Individual Layers
```bash
# Infrastructure only
docker compose up -d postgres redis kafka schema-registry kafka-ui pgadmin

# Observability only
docker compose up -d otel-collector jaeger prometheus grafana

# Applications only
docker compose up -d gateway-service reservation-engine payment-processor
```

### Check Service Health
```bash
./dev.sh docker-status
# Shows health status of all running services
```

## 📁 File Organization

```
infrastructure/docker/
├── docker-compose.yml              # ✅ NEW: Unified configuration
├── otel-collector.yml              # OpenTelemetry Collector config
├── prometheus.yml                  # Prometheus scraping config
├── grafana/provisioning/           # Grafana datasources
└── old-compose-files/              # 📦 BACKUP: Previous files
    ├── docker-compose-infrastructure.yml
    ├── docker-compose-observability.yml
    └── docker-compose-services.yml
```

## 🎉 Ready to Use

The unified Docker Compose configuration is now ready for use. All previous functionality is preserved with improved organization and simplified commands.

**Quick Start**: `./dev.sh docker-start` to launch the complete modern reservation stack!
