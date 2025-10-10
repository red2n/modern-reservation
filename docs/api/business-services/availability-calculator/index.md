# Availability Calculator API

The Availability Calculator service handles real-time room availability computation, inventory management, and booking conflict resolution for the Modern Reservation System.

## 🎯 Overview

| Property | Value |
|----------|-------|
| **Service Name** | availability-calculator |
| **Port** | 8081 |
| **Health Check** | `/actuator/health` |
| **API Base URL** | `http://localhost:8081/api/v1/availability` |
| **OpenAPI Spec** | `/v3/api-docs` |
| **Swagger UI** | `/swagger-ui.html` |

## 🚀 Quick Start

### Start the Service
```bash
# Via dev script
./dev.sh start availability-calculator

# Via Maven
cd apps/backend/java-services/business-services/availability-calculator
mvn spring-boot:run
```

### Health Check
```bash
curl http://localhost:8081/actuator/health
```

## 🏨 Core Features

### Real-time Availability
- **Instant availability checks** for date ranges
- **Room type availability** with capacity management
- **Overbooking protection** with configurable limits
- **Block and allocation management**

### Inventory Management
- **Room inventory tracking** across properties
- **Seasonal availability patterns**
- **Maintenance blocking** and room closures
- **Group booking allocations**

### Performance Optimization
- **Parallel processing** for multi-property queries
- **Intelligent caching** with Redis
- **Bulk availability checks** for OTA integrations
- **Optimistic locking** for high-concurrency scenarios

## 🔌 API Endpoints

### Check Availability
```http
GET /api/v1/availability/check
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
```

**Query Parameters:**
- `propertyId` (string, required): Property identifier
- `checkIn` (string, required): Check-in date (ISO 8601)
- `checkOut` (string, required): Check-out date (ISO 8601)
- `roomType` (string, optional): Specific room type
- `guests` (integer, optional): Number of guests (default: 2)
- `rooms` (integer, optional): Number of rooms requested (default: 1)

**Response:**
```json
{
  "availability": {
    "available": true,
    "propertyId": "prop-123",
    "checkIn": "2025-10-15",
    "checkOut": "2025-10-17",
    "nights": 2,
    "totalRooms": 50
  },
  "roomTypes": [
    {
      "typeId": "standard",
      "typeName": "Standard Room",
      "available": 15,
      "total": 30,
      "rate": {
        "baseRate": 120.00,
        "totalRate": 240.00,
        "currency": "USD"
      }
    },
    {
      "typeId": "deluxe", 
      "typeName": "Deluxe Room",
      "available": 8,
      "total": 15,
      "rate": {
        "baseRate": 180.00,
        "totalRate": 360.00,
        "currency": "USD"
      }
    }
  ],
  "restrictions": {
    "minimumStay": 1,
    "maximumStay": 14,
    "closedToArrival": false,
    "closedToDeparture": false
  }
}
```

### Bulk Availability Check
```http
POST /api/v1/availability/bulk-check
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
Content-Type: application/json
```

**Request Body:**
```json
{
  "requests": [
    {
      "propertyId": "prop-123",
      "checkIn": "2025-10-15",
      "checkOut": "2025-10-17",
      "roomType": "standard",
      "rooms": 2
    },
    {
      "propertyId": "prop-124", 
      "checkIn": "2025-10-20",
      "checkOut": "2025-10-22",
      "roomType": "deluxe",
      "rooms": 1
    }
  ]
}
```

**Response:**
```json
{
  "results": [
    {
      "requestId": 0,
      "available": true,
      "availableRooms": 15,
      "rate": {
        "baseRate": 120.00,
        "totalRate": 480.00
      }
    },
    {
      "requestId": 1,
      "available": true,
      "availableRooms": 8,
      "rate": {
        "baseRate": 180.00,
        "totalRate": 360.00
      }
    }
  ],
  "processedAt": "2025-10-10T12:00:00Z"
}
```

### Hold Inventory
```http
POST /api/v1/availability/hold
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
Content-Type: application/json
```

**Request Body:**
```json
{
  "propertyId": "prop-123",
  "checkIn": "2025-10-15",
  "checkOut": "2025-10-17",
  "roomType": "standard",
  "rooms": 2,
  "holdDuration": 600,
  "reservationId": "res-temp-123"
}
```

**Response:**
```json
{
  "holdId": "hold-456",
  "propertyId": "prop-123",
  "roomType": "standard",
  "rooms": 2,
  "expiresAt": "2025-10-10T12:10:00Z",
  "status": "ACTIVE"
}
```

### Release Hold
```http
DELETE /api/v1/availability/hold/{holdId}
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
```

**Response:**
```json
{
  "holdId": "hold-456",
  "status": "RELEASED",
  "releasedAt": "2025-10-10T12:05:00Z"
}
```

### Inventory Status
```http
GET /api/v1/availability/inventory/{propertyId}
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
```

**Query Parameters:**
- `startDate` (string, required): Start date for inventory report
- `endDate` (string, required): End date for inventory report
- `roomType` (string, optional): Filter by room type

**Response:**
```json
{
  "propertyId": "prop-123",
  "period": {
    "startDate": "2025-10-15",
    "endDate": "2025-10-30"
  },
  "inventory": [
    {
      "date": "2025-10-15",
      "roomTypes": {
        "standard": {
          "total": 30,
          "available": 25,
          "occupied": 5,
          "blocked": 0,
          "held": 2
        },
        "deluxe": {
          "total": 15,
          "available": 12,
          "occupied": 3,
          "blocked": 0,
          "held": 0
        }
      }
    }
  ]
}
```

## 📡 Event Processing

### Kafka Event Consumers

**Reservation Events**
```yaml
Topic: reservation.created
Handler: updateInventoryOnReservation()
Purpose: Reduce available inventory when booking is confirmed
```

**Reservation Cancellation**
```yaml
Topic: reservation.cancelled
Handler: restoreInventoryOnCancellation()  
Purpose: Restore inventory when booking is cancelled
```

**Rate Updates**
```yaml
Topic: rate.updated
Handler: invalidateAvailabilityCache()
Purpose: Clear cached availability when rates change
```

### Event Publishing

**Availability Updated**
```yaml
Topic: availability.updated
Schema:
  - propertyId: string
  - date: date
  - roomType: string
  - totalRooms: integer
  - availableRooms: integer
  - updatedAt: timestamp
```

## 🔧 Configuration

### Application Properties
```yaml
# Availability Calculator Configuration
availability:
  cache:
    ttl: 300s              # Cache availability for 5 minutes
    max-entries: 10000     # Maximum cache entries
    
  processing:
    parallel-threads: 8    # Parallel processing threads
    batch-size: 100        # Bulk check batch size
    
  holds:
    default-duration: 600s # Default hold duration (10 minutes)
    max-duration: 3600s    # Maximum hold duration (1 hour)
    cleanup-interval: 60s  # Expired hold cleanup interval
    
  inventory:
    overbooking-limit: 5   # Overbooking percentage
    lookahead-days: 365    # Inventory lookahead period

# Database Configuration
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/availability
    username: availability_user
    password: ${AVAILABILITY_DB_PASSWORD}
    
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        jdbc:
          batch_size: 50
        order_inserts: true
        order_updates: true

# Redis Configuration  
spring:
  redis:
    host: localhost
    port: 6379
    database: 3
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 20
        max-wait: -1ms
```

### Environment Variables
```bash
# Database
AVAILABILITY_DB_PASSWORD=availability_secure_password
AVAILABILITY_DB_POOL_SIZE=20

# Cache
REDIS_URL=redis://localhost:6379/3
AVAILABILITY_CACHE_TTL=300

# Performance
AVAILABILITY_PARALLEL_THREADS=8
AVAILABILITY_BATCH_SIZE=100

# Monitoring
OTEL_SERVICE_NAME=availability-calculator
OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317
```

## 📈 Performance & Optimization

### Performance Metrics
- **Availability Check**: <50ms (95th percentile)
- **Bulk Checks**: <200ms for 100 properties
- **Inventory Updates**: <10ms per room/night
- **Cache Hit Ratio**: >90% during peak hours

### Optimization Strategies

**Database Optimization**
```sql
-- Critical indexes for performance
CREATE INDEX idx_inventory_property_date ON room_inventory(property_id, date);
CREATE INDEX idx_inventory_room_type ON room_inventory(room_type, date);
CREATE INDEX idx_holds_expiry ON inventory_holds(expires_at) WHERE status = 'ACTIVE';
```

**Caching Strategy**
```java
// Multi-level caching
@Cacheable(value = "availability", key = "#propertyId + ':' + #checkIn + ':' + #checkOut")
public AvailabilityResult checkAvailability(String propertyId, LocalDate checkIn, LocalDate checkOut)

// Cache warming for popular properties
@Scheduled(fixedRate = 300000) // Every 5 minutes
public void warmAvailabilityCache()
```

**Parallel Processing**
```java
// Parallel availability checks
CompletableFuture<AvailabilityResult>[] futures = requests.stream()
    .map(request -> CompletableFuture.supplyAsync(() -> 
        checkSingleAvailability(request), executor))
    .toArray(CompletableFuture[]::new);
```

## 🧪 Testing

### Unit Tests
```bash
cd apps/backend/java-services/business-services/availability-calculator
mvn test
```

### Integration Tests  
```bash
mvn verify -P integration-tests
```

### Load Testing
```bash
# Test availability endpoint under load
artillery run tests/load/availability-load-test.yml

# Test bulk availability
artillery run tests/load/bulk-availability-load-test.yml
```

**Sample Load Test Configuration:**
```yaml
# tests/load/availability-load-test.yml
config:
  target: 'http://localhost:8081'
  phases:
    - duration: 60   # 1 minute
      arrivalRate: 100  # 100 requests per second
scenarios:
  - name: "Check Availability"
    requests:
      - get:
          url: "/api/v1/availability/check"
          qs:
            propertyId: "prop-123" 
            checkIn: "2025-10-15"
            checkOut: "2025-10-17"
```

## 🔍 Monitoring & Observability

### Key Metrics
```
# Request metrics
http_requests_total{service="availability-calculator"}
http_request_duration_seconds{service="availability-calculator"}

# Business metrics
availability_checks_total
availability_cache_hits_total
availability_cache_misses_total
inventory_holds_active
inventory_holds_expired_total

# Performance metrics
availability_check_duration_seconds
bulk_availability_batch_size
inventory_update_duration_seconds
```

### Health Checks
```bash
# Basic health
curl http://localhost:8081/actuator/health

# Detailed health with dependencies
curl http://localhost:8081/actuator/health/detail

# Custom availability health indicator
curl http://localhost:8081/actuator/health/availability
```

## 🛠️ Development

### Local Development
```bash
# Run with development profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run with debug and specific configuration
mvn spring-boot:run \
  -Dspring-boot.run.profiles=dev \
  -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5006"
```

### Database Schema
```sql
-- Core inventory table
CREATE TABLE room_inventory (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    property_id VARCHAR(50) NOT NULL,
    room_type VARCHAR(50) NOT NULL,
    date DATE NOT NULL,
    total_rooms INTEGER NOT NULL,
    available_rooms INTEGER NOT NULL,
    blocked_rooms INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, property_id, room_type, date)
);

-- Inventory holds table
CREATE TABLE inventory_holds (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hold_id VARCHAR(50) UNIQUE NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    property_id VARCHAR(50) NOT NULL,
    room_type VARCHAR(50) NOT NULL,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    rooms INTEGER NOT NULL,
    reservation_id VARCHAR(50),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 🚨 Troubleshooting

### Common Issues

**High Response Times**
```bash
# Check database connection pool
curl http://localhost:8081/actuator/metrics/hikaricp.connections.active

# Monitor cache performance  
curl http://localhost:8081/actuator/metrics/cache.gets

# Check parallel processing metrics
curl http://localhost:8081/actuator/metrics/executor.active
```

**Cache Issues**
```bash
# Monitor Redis connection
redis-cli -h localhost -p 6379 ping

# Check cache statistics
redis-cli -h localhost -p 6379 info stats

# Clear availability cache
redis-cli -h localhost -p 6379 flushdb
```

**Inventory Discrepancies**
```bash
# Check for expired holds
curl http://localhost:8081/actuator/metrics/availability.holds.expired

# Verify inventory consistency
./scripts/verify-inventory-consistency.sh

# Rebuild inventory cache
curl -X POST http://localhost:8081/api/v1/availability/admin/rebuild-cache
```

---

## 📚 Related Documentation
- [Business Services Overview](../index.md)
- [Rate Management Service](../rate-management/)
- [Reservation Engine](../reservation-engine/)