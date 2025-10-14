# Port Management & Security Implementation

## 📋 Overview

This document describes the comprehensive port management and network security implementation for the Modern Reservation System. This consolidates three key architectural decisions:

1. **Node.js Authentication Service** migration from Java
2. **Centralized Port Management** with @modern-reservation/port-manager
3. **Network Security with Isolation** - only Gateway exposed externally

## 🎯 Key Principles

### Security-First Architecture
- ✅ Only Gateway Service (8080) exposed externally
- ✅ All backend services on internal-only Docker network
- ✅ Database isolated in separate network
- ✅ Frontend apps can only access Gateway, not internal services

### Centralized Port Registry
- ✅ Single source of truth for all port assignments
- ✅ Automatic conflict detection
- ✅ Internal/External classification
- ✅ Category-based organization

## 🏗️ Architecture Components

### 1. Port Manager Library (`libs/shared/port-manager`)

```typescript
// Import port management
import { PortRegistry, PortValidator } from '@modern-reservation/port-manager';

// Get service port
const gateway = PortRegistry.getServiceByName('gateway-service');
console.log(gateway.port); // 8080

// Check for conflicts
const validation = PortValidator.validateAllPorts();
console.log(validation.conflicts); // []
```

**Key Features:**
- **PortRegistry**: Centralized registry of all services and ports
- **PortValidator**: Checks for conflicts and validates configuration
- **PortAllocator**: Dynamic port allocation for new services
- **CLI Tool**: Command-line interface for port management

### 2. Port Categories

| Category | Range | Purpose |
|----------|-------|---------|
| FRONTEND | 3000-3099 | Guest Portal, Admin Portal, Staff Portal |
| NODE_SERVICE | 3100-3199 | Auth, Notification, WebSocket |
| DATABASE | 5432-5449 | PostgreSQL instances |
| CACHE | 6379-6399 | Redis instances |
| GATEWAY | 8080-8099 | Spring Cloud Gateway |
| JAVA_BUSINESS | 8100-8199 | Business services |
| SERVICE_DISCOVERY | 8761-8799 | Eureka |
| CONFIG | 8888-8899 | Config Server |
| MESSAGE_QUEUE | 2181, 9092, 29092, 8081 | Kafka, Zookeeper |

### 3. Network Security Model

```yaml
# Network Architecture
networks:
  gateway-net:
    # External access - Gateway and Frontend only
    driver: bridge

  backend-internal:
    # NO external access - all backend services
    driver: bridge
    internal: true  # This blocks external connectivity

  db-net:
    # Database isolation
    driver: bridge
    internal: true
```

## 📊 Service Classifications

### External Services (🌐)
Only these services should be accessible from outside Docker:

1. **gateway-service** (8080) - Main API entry point
2. **guest-portal** (3000) - Frontend application
3. **admin-portal** (3010) - Admin UI
4. **staff-portal** (3020) - Staff UI

**Development Tools** (disable in production):
- pgadmin (5050)
- kafka-ui (8090)
- eureka-server (8761) - UI only, service still internal
- jaeger (16686)

### Internal Services (🔒)
These services are NOT accessible externally:

**Node.js Services:**
- auth-service (3100)
- notification-service (3110)
- websocket-service (3120)
- All other Node.js services

**Java Business Services:**
- reservation-engine (8100)
- availability-calculator (8110)
- rate-management (8120)
- payment-processor (8130)
- analytics-engine (8140)
- tenant-service (8150)

**Infrastructure:**
- config-server (8888)
- postgres (5432)
- redis (6379)
- kafka (9092)
- prometheus (9090)
- grafana (3003)

## 🚀 Usage

### CLI Commands

```bash
# List all services and ports
./dev.sh port-list

# Check for conflicts
./dev.sh port-check

# Generate detailed report
./dev.sh port-report

# Security analysis
./dev.sh port-security

# Export for Docker
./dev.sh port-export

# Validate configuration
./dev.sh port-validate
```

### Secure Docker Deployment

```bash
# Start with network isolation
./dev.sh docker-secure-start

# Test security
./dev.sh docker-secure-test

# Check status
./dev.sh docker-secure-status

# Stop secure deployment
./dev.sh docker-secure-stop
```

### Port Management in Code

```typescript
// Get all services
const services = PortRegistry.getAllServices();

// Get external services only
const externalServices = PortRegistry.getExternalServices();

// Get internal services only
const internalServices = PortRegistry.getInternalServices();

// Get service by name
const authService = PortRegistry.getServiceByName('auth-service');

// Check if port is in use
const inUse = PortValidator.isPortInUse(3100);

// Find available port in category
const available = PortValidator.findAvailablePort(3100, 3199);
```

## 🔐 Security Validation

### Testing Security Configuration

```bash
# Run comprehensive security test
./dev.sh docker-secure-test
```

This validates:
1. ✅ Gateway is accessible from outside
2. ✅ Backend services are NOT accessible from outside
3. ✅ Networks are properly configured
4. ✅ internal: true flag is set on backend network

### Expected Results

```
✓ Gateway is accessible externally ✓
✓ Backend services are properly isolated ✓
```

## 📝 Implementation Details

### 1. Node.js Authentication Service

**Location:** `apps/backend/node-services/auth-service/`

**Features:**
- JWT-based authentication
- Demo users (demo@guest.com, demo@host.com)
- Eureka service registration
- Health checks
- Port 3100 (internal-only)

**Migration from Java:**
- ✅ Lighter weight (Node.js vs Spring Boot)
- ✅ Faster startup time
- ✅ Simpler dependencies
- ✅ Consistent with shared schemas

### 2. Port Manager Package

**Location:** `libs/shared/port-manager/`

**Files:**
- `types.ts` - TypeScript interfaces and enums
- `port-registry.ts` - Central port registry
- `port-validator.ts` - Conflict detection and validation
- `port-allocator.ts` - Dynamic port allocation
- `cli.ts` - Command-line interface
- `index.ts` - Package exports

**Build:**
```bash
cd libs/shared/port-manager
npm install
npm run build
```

### 3. Secure Docker Compose

**Location:** `infrastructure/docker/docker-compose.secure.yml`

**Key Features:**
- Network isolation with `internal: true`
- Only Gateway on external network
- All services properly categorized
- Environment variables from `.env.ports`

**Configuration:**
```bash
# Copy template
cp infrastructure/docker/.env.ports.template infrastructure/docker/.env.ports

# Edit configuration
nano infrastructure/docker/.env.ports

# Update JWT_SECRET and database passwords
```

## 🔄 Integration with dev.sh

The main `dev.sh` script now includes:

**Port Management Commands:**
- `./dev.sh port-list`
- `./dev.sh port-check`
- `./dev.sh port-report`
- `./dev.sh port-security`
- `./dev.sh port-export`
- `./dev.sh port-validate`

**Secure Docker Commands:**
- `./dev.sh docker-secure-start`
- `./dev.sh docker-secure-stop`
- `./dev.sh docker-secure-test`
- `./dev.sh docker-secure-validate`

## 📈 Future Enhancements

### Phase 2: Kubernetes
- Helm charts with network policies
- Service mesh (Istio/Linkerd)
- Ingress controller configuration
- Certificate management

### Phase 3: Production
- HashiCorp Vault for secrets
- Dynamic port allocation
- Service discovery integration
- Auto-scaling configuration

## 🧪 Testing

### Manual Testing

```bash
# 1. Build port manager
cd libs/shared/port-manager
npm install && npm run build

# 2. Validate ports
./dev.sh port-validate

# 3. Start secure deployment
./dev.sh docker-secure-start

# 4. Test security
./dev.sh docker-secure-test

# 5. Access Gateway (should work)
curl http://localhost:8080/actuator/health

# 6. Try to access backend service (should fail)
curl http://localhost:8100/actuator/health  # Should timeout
```

### Automated Testing

```bash
# Run all checks
./dev.sh port-validate
./dev.sh docker-secure-validate

# Generate reports
./dev.sh port-report > port-report.txt
./dev.sh port-security > security-report.txt
```

## 📚 References

### Related Documentation
- [Dev Quick Reference](./guides/DEV_QUICK_REFERENCE.md)
- [Docker Security](../infrastructure/docker/README.md)
- [Node.js Auth Service](./AUTH_SERVICE_NODE_MIGRATION.md)
- [Architecture Overview](./architecture/)

### External Resources
- [Docker Network Security](https://docs.docker.com/network/network-tutorial-standalone/)
- [Microservices Security Patterns](https://microservices.io/patterns/security/index.html)
- [Port Management Best Practices](https://12factor.net/port-binding)

## ✅ Checklist

Before deploying to production:

- [ ] Update JWT_SECRET in .env.ports
- [ ] Change database passwords
- [ ] Disable dev tool UIs (PgAdmin, Kafka UI, Eureka UI)
- [ ] Run security validation
- [ ] Test external access to Gateway only
- [ ] Verify backend services are inaccessible
- [ ] Configure firewall rules
- [ ] Set up SSL/TLS certificates
- [ ] Enable monitoring and alerting
- [ ] Document production ports

## 🎯 Success Criteria

✅ **Security:**
- Only Gateway accessible externally
- All backend services isolated
- Database not exposed

✅ **Port Management:**
- No port conflicts
- All services registered
- Clear internal/external classification

✅ **Usability:**
- Simple CLI commands
- Clear reports
- Easy to validate configuration

✅ **Maintainability:**
- Single source of truth
- Well-documented
- Easy to extend

## 🤝 Contributing

When adding a new service:

1. Register port in `libs/shared/port-manager/src/port-registry.ts`
2. Classify as internal or external
3. Rebuild port manager: `npm run build`
4. Validate: `./dev.sh port-validate`
5. Add to docker-compose.secure.yml with correct network
6. Test security: `./dev.sh docker-secure-test`
