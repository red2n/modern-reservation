# Port Manager

Centralized port management for Modern Reservation microservices architecture.

## Features

- 🔐 **Security-First**: Separates internal and external services
- 🎯 **Port Registry**: All service ports defined in one place
- ✅ **Validation**: Checks for port conflicts and duplicates
- 📊 **Reporting**: Detailed port usage and security reports
- 🚀 **Dynamic Allocation**: Automatically allocate ports for new services

## Installation

```bash
cd libs/shared/port-manager
npm install
npm run build
```

## Usage

### CLI Commands

```bash
# List all services and ports
npx port-manager list

# Check for conflicts and usage
npx port-manager check

# Generate detailed report
npx port-manager report

# Generate security report
npx port-manager security

# Export for Docker Compose
npx port-manager docker-env > .env.ports

# Validate configuration
npx port-manager validate
```

### Programmatic Usage

```typescript
import { PortRegistry, PortValidator } from '@modern-reservation/port-manager';

// Get all services
const services = PortRegistry.getAllServices();

// Get service by name
const gateway = PortRegistry.getServiceByName('gateway-service');

// Check if port is in use
const inUse = PortValidator.isPortInUse(8080);

// Get security report
const report = PortRegistry.getSecurityReport();
```

## Port Allocation

| Range | Category | Services |
|-------|----------|----------|
| 3000-3099 | Frontend | Guest Portal, Admin Portal |
| 3100-3199 | Node.js Services | Auth, Notification, WebSocket |
| 5432-5449 | Databases | PostgreSQL |
| 6379-6399 | Cache | Redis |
| 8080-8099 | Gateways | Spring Gateway |
| 8100-8199 | Java Services | Business services |
| 8761-8799 | Service Discovery | Eureka |
| 8888-8899 | Config | Config Server |

## Security Model

- **External Services** (🌐): Exposed to the internet
  - Gateway Service (8080) - ONLY API entry point
  - Frontend apps (3000-3099)
  - Dev tools (in development only)

- **Internal Services** (🔒): Not exposed
  - All backend services
  - Databases
  - Message queues
  - Cache services

## Integration

Use with dev.sh:

```bash
./dev.sh port-check
./dev.sh port-report
./dev.sh port-export
```
