# Node.js Microservices - Updated

## Architecture Overview
- **Node.js LTS** optimized for I/O intensive operations
- **Event-driven architecture** with Kafka integration
- **Lightweight services** for high-concurrency operations
- **Real-time capabilities** with WebSocket support
- **Service Discovery** with Eureka integration

## Implemented Services ✅

### 1. Authentication Service (`auth-service/`) ⭐ **NEW**
- **Purpose**: JWT-based authentication and authorization
- **Performance**: High-concurrency auth requests
- **Port**: 3100
- **Features**:
  - JWT token generation/validation
  - Role-based access control (RBAC)
  - Permission-based authorization
  - Demo user support for development
  - Eureka service discovery registration
  - Health check endpoints
- **Tech Stack**: Fastify, @fastify/jwt, bcrypt, Pino, Eureka
- **Status**: ✅ Running and integrated with Gateway
- **Documentation**: See [AUTH_SERVICE_NODE_MIGRATION.md](../../../docs/AUTH_SERVICE_NODE_MIGRATION.md)

### Demo Users Available
All use password: `demo123`
- `admin@hotel.com` - HOTEL_ADMIN
- `frontdesk@hotel.com` - FRONT_DESK
- `reservations@hotel.com` - RESERVATION_MANAGER
- `manager@hotel.com` - MANAGER
- `finance@hotel.com` - FINANCE
- `housekeeping@hotel.com` - HOUSEKEEPING
- `guest@example.com` - GUEST

## Planned Services 🚧

### 2. API Gateway (`api-gateway/`)
- **Purpose**: Primary entry point and request routing (Alternative to Spring Cloud Gateway)
- **Performance**: 50,000+ requests/second throughput
- **Features**:
  - GraphQL Federation gateway
  - Rate limiting (Redis-based)
  - Circuit breaker patterns
  - Request/response transformation
- **Tech Stack**: Fastify, Apollo Gateway, Redis

### 3. WebSocket Service (`websocket-service/`)
- **Purpose**: Real-time bidirectional communication
- **Performance**: 100,000+ concurrent WebSocket connections
- **Features**:
  - Real-time reservation updates
  - Live availability changes
  - Staff notifications
  - Guest messaging
- **Tech Stack**: Socket.IO, Redis Adapter, Clustering

### 4. Notification Service (`notification-service/`)
- **Purpose**: Multi-channel notification delivery
- **Performance**: 10,000+ notifications/minute processing
- **Features**:
  - Email notifications (SMTP/SendGrid)
  - SMS notifications (Twilio/AWS SNS)
  - Push notifications (FCM/APNs)
  - In-app notifications
  - Template management
- **Tech Stack**: Bull Queue, Redis, Multiple providers

### 5. Channel Manager (`channel-manager/`)
- **Purpose**: OTA and external system integrations
- **Performance**: 1,000+ API calls/minute to external systems
- **Features**:
  - Booking.com integration
  - Expedia connectivity
  - Airbnb synchronization
  - Rate and inventory sync
  - Booking import/export
- **Tech Stack**: Fastify, Axios, Bull Queue

### 6. File Upload Service (`file-upload-service/`)
- **Purpose**: Document and media upload handling
- **Features**:
  - Guest document uploads (ID, credit card)
  - Property images
  - Invoice/receipt storage
  - Multi-cloud storage support (S3, Azure Blob)
- **Tech Stack**: Fastify, Multer, AWS SDK

### 7. Audit Service (`audit-service/`)
- **Purpose**: System-wide audit logging
- **Features**:
  - User action logging
  - Data change tracking
  - Compliance reporting
  - Security event monitoring
- **Tech Stack**: Fastify, Kafka Consumer, TimescaleDB

### 8. Housekeeping Service (`housekeeping-service/`)
- **Purpose**: Real-time housekeeping status updates
- **Features**:
  - Room status management
  - Task assignment
  - Staff location tracking
  - Maintenance requests
- **Tech Stack**: Fastify, WebSocket, Redis

## Why Node.js for These Services?

### ✅ Perfect for Node.js
- **Authentication**: High-concurrency JWT operations, stateless, lightweight
- **WebSocket**: Event-driven model excels at persistent connections
- **Notifications**: Async I/O for external API calls
- **File Uploads**: Streaming large files efficiently
- **Real-time Updates**: Event loop perfect for pub/sub patterns
- **External Integrations**: Non-blocking HTTP requests to OTAs

### ❌ Not for Node.js
- Complex business logic computations
- Heavy CPU-intensive operations
- Long-running synchronous processes
- (Those stay in Java services: reservation engine, analytics, etc.)

## Tech Stack

### Core Framework
- **Fastify** - High-performance web framework (3x faster than Express)
- **TypeScript** - Type safety across services

### Authentication & Security
- **@fastify/jwt** - JWT token management
- **bcrypt** - Password hashing
- **@fastify/cors** - CORS handling

### Service Discovery
- **eureka-js-client** - Eureka service registration

### Logging & Monitoring
- **Pino** - Ultra-fast JSON logger
- **pino-pretty** - Development log formatting

### Data & Messaging
- **Redis** - Caching and pub/sub
- **Kafka** - Event streaming (via Node.js clients)

### Code Quality
- **Biome.js** - Fast formatter and linter
- **Vitest** - Fast unit testing

### Shared Libraries
- **@modern-reservation/schemas** - Shared types and validation

## Architecture Decision: Auth Service in Node.js

**Why we moved authentication from Java to Node.js:**

1. ✅ **Lightweight Operations** - JWT token operations are CPU-light
2. ✅ **High Concurrency** - Auth gets hit frequently, Node.js handles it well
3. ✅ **Fast I/O** - Quick database lookups for user validation
4. ✅ **Stateless** - Perfect for Node.js event-driven model
5. ✅ **Simple Logic** - No complex computations needed
6. ✅ **Performance** - Lower memory footprint than Java
7. ✅ **Development Speed** - Faster iteration for auth features

See full analysis in [AUTH_SERVICE_NODE_MIGRATION.md](../../../docs/AUTH_SERVICE_NODE_MIGRATION.md)

## Development

### Service Structure
All services follow this standard structure:

```
service-name/
├── .env                    # Environment configuration
├── .env.example            # Environment template
├── .gitignore             # Git ignore
├── biome.json             # Code quality config
├── package.json           # Dependencies and scripts
├── tsconfig.json          # TypeScript config
├── README.md              # Service documentation
└── src/
    ├── index.ts           # Entry point
    ├── routes/            # API route handlers
    ├── services/          # Business logic
    ├── repositories/      # Data access layer
    └── utils/             # Utility functions
```

### Running Services

```bash
# Install dependencies
npm install

# Run in development mode (with hot reload)
npm run dev

# Build for production
npm run build

# Run production build
npm start

# Check code style
npm run check

# Fix code style issues
npm run fix

# Run tests
npm test
```

### Authentication Service Example

```bash
# Start auth service
cd auth-service
npm install
npm run dev

# Test health check
curl http://localhost:3100/health

# Test login
curl -X POST http://localhost:3100/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@hotel.com","password":"demo123"}'

# Test through Gateway
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@hotel.com","password":"demo123"}'
```

## Service Integration

### With Eureka
Services register with Eureka for service discovery:

```typescript
import { registerWithEureka } from './utils/eureka.client';

const port = 3100;
await server.listen({ port, host: '0.0.0.0' });

if (process.env.EUREKA_URL) {
  await registerWithEureka(port);
}
```

### With Gateway
Services are routed through Spring Cloud Gateway:

```yaml
# Gateway routes Node.js services
- id: auth-service
  uri: http://localhost:3100
  predicates:
    - Path=/auth/**
```

### With Kafka
Services consume/produce events:

```typescript
// Example: Audit service consumes auth events
kafka.consumer.on('message', (message) => {
  // Process audit event
});
```

## Code Quality Standards

### Biome.js Configuration
All services use Biome.js for:
- Fast formatting (2 spaces, single quotes, 100 line width)
- Linting (TypeScript best practices)
- Import organization

```bash
# Check all issues
npm run check

# Auto-fix issues
npm run fix
```

### TypeScript Configuration
- Target: ES2022
- Strict mode enabled
- CommonJS modules
- Source maps for debugging

## Monitoring & Health Checks

All services expose:
- `GET /health` - Basic health status
- `GET /health/ready` - Readiness probe (K8s)
- `GET /health/live` - Liveness probe (K8s)

## Security

- JWT tokens for authentication
- CORS configuration per service
- Rate limiting (planned)
- Input validation using Zod schemas
- Bcrypt for password hashing

## Performance Targets

| Service | Throughput | Latency (p95) |
|---------|-----------|---------------|
| Auth Service | 10,000+ req/s | < 50ms |
| WebSocket | 100,000 connections | < 10ms |
| Notifications | 10,000/min | < 100ms |
| API Gateway | 50,000 req/s | < 20ms |

## Next Steps

- [ ] Add rate limiting to auth service
- [ ] Implement API Gateway (Node.js version)
- [ ] Create WebSocket service for real-time updates
- [ ] Build notification service
- [ ] Implement channel manager for OTA integrations
- [ ] Add comprehensive logging and tracing
- [ ] Set up service monitoring dashboards

## Resources

- [Fastify Documentation](https://www.fastify.io/)
- [Eureka JS Client](https://github.com/jquatier/eureka-js-client)
- [Biome.js](https://biomejs.dev/)
- [Auth Service Migration](../../../docs/AUTH_SERVICE_NODE_MIGRATION.md)
