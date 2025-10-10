# Analytics Engine API

The Analytics Engine provides real-time analytics, reporting, and business intelligence for the Modern Reservation System.

## 🎯 Overview

| Property | Value |
|----------|-------|
| **Service Name** | analytics-engine |
| **Port** | 8080 |
| **Health Check** | `/actuator/health` |
| **API Base URL** | `http://localhost:8080/api/v1/analytics` |
| **OpenAPI Spec** | `/v3/api-docs` |
| **Swagger UI** | `/swagger-ui.html` |

## 🚀 Quick Start

### Start the Service
```bash
# Via dev script
./dev.sh start analytics-engine

# Via Maven
cd apps/backend/java-services/business-services/analytics-engine
mvn spring-boot:run
```

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

## 📊 Core Features

### Real-time Analytics
- **Reservation metrics** (bookings per hour/day/month)
- **Revenue analytics** (daily/monthly revenue, average booking value)
- **Occupancy rates** (current, historical, forecasted)
- **Guest analytics** (demographics, booking patterns)

### Business Intelligence
- **Performance dashboards** for management
- **Operational metrics** for staff
- **Financial reporting** for accounting
- **Predictive analytics** for revenue management

## 🔌 API Endpoints

### Analytics Overview
```http
GET /api/v1/analytics/overview
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
```

**Response:**
```json
{
  "summary": {
    "totalReservations": 1250,
    "totalRevenue": 125000.00,
    "occupancyRate": 85.5,
    "averageBookingValue": 100.00
  },
  "period": "last_30_days",
  "generatedAt": "2025-10-10T12:00:00Z"
}
```

### Reservation Metrics
```http
GET /api/v1/analytics/reservations
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
```

**Query Parameters:**
- `period` (string): `today`, `week`, `month`, `year`, `custom`
- `startDate` (string): ISO date (required for custom period)
- `endDate` (string): ISO date (required for custom period)
- `propertyId` (string): Filter by property
- `roomType` (string): Filter by room type

**Response:**
```json
{
  "metrics": {
    "totalBookings": 45,
    "confirmedBookings": 42,
    "cancelledBookings": 3,
    "averageBookingValue": 150.00,
    "bookingsByStatus": {
      "CONFIRMED": 42,
      "PENDING": 0,
      "CANCELLED": 3
    }
  },
  "timeline": [
    {
      "date": "2025-10-10",
      "bookings": 12,
      "revenue": 1800.00
    }
  ]
}
```

### Revenue Analytics
```http
GET /api/v1/analytics/revenue
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
```

**Response:**
```json
{
  "revenue": {
    "total": 125000.00,
    "bySource": {
      "direct": 75000.00,
      "ota": 35000.00,
      "phone": 15000.00
    },
    "byRoomType": {
      "standard": 50000.00,
      "deluxe": 45000.00,
      "suite": 30000.00
    }
  },
  "trends": {
    "monthOverMonth": 12.5,
    "yearOverYear": 25.3
  }
}
```

### Occupancy Analytics
```http
GET /api/v1/analytics/occupancy
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
```

**Response:**
```json
{
  "occupancy": {
    "current": 85.5,
    "forecast": [
      {
        "date": "2025-10-11",
        "predicted": 88.2
      }
    ],
    "historical": [
      {
        "date": "2025-10-09",
        "actual": 82.1
      }
    ]
  },
  "breakdown": {
    "byRoomType": {
      "standard": 90.0,
      "deluxe": 85.0,
      "suite": 75.0
    }
  }
}
```

### Guest Analytics
```http
GET /api/v1/analytics/guests
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
```

**Response:**
```json
{
  "demographics": {
    "ageGroups": {
      "18-25": 15,
      "26-35": 35,
      "36-50": 30,
      "51+": 20
    },
    "geography": {
      "domestic": 70,
      "international": 30
    }
  },
  "behavior": {
    "averageStayLength": 2.5,
    "returnGuestRate": 35.5,
    "seasonalTrends": []
  }
}
```

## 📡 Event Consumption

The Analytics Engine consumes the following Kafka events:

### Reservation Events
```yaml
Topic: reservation.created
Schema:
  - reservationId: string
  - guestId: string
  - propertyId: string
  - checkIn: date
  - checkOut: date
  - totalAmount: decimal
  - status: enum
```

### Payment Events
```yaml
Topic: payment.processed
Schema:
  - paymentId: string
  - reservationId: string
  - amount: decimal
  - method: string
  - status: enum
```

## 🔧 Configuration

### Application Properties
```yaml
# Analytics Engine Configuration
analytics:
  retention:
    raw-data: 90d        # Raw event data retention
    aggregated: 365d     # Aggregated metrics retention

  processing:
    batch-size: 1000
    parallel-workers: 4

  cache:
    metrics-ttl: 300s    # Cache metrics for 5 minutes
    dashboard-ttl: 60s   # Cache dashboard data for 1 minute

# Kafka Configuration
spring:
  kafka:
    consumer:
      group-id: analytics-engine
      auto-offset-reset: latest
      value-deserializer: io.confluent.kafka.serializers.KafkaAvroDeserializer
```

### Environment Variables
```bash
# Database
ANALYTICS_DB_URL=jdbc:postgresql://localhost:5432/analytics
ANALYTICS_DB_USERNAME=analytics_user
ANALYTICS_DB_PASSWORD=analytics_pass

# Cache
REDIS_URL=redis://localhost:6379
REDIS_DB=2

# Monitoring
OTEL_SERVICE_NAME=analytics-engine
OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317
```

## 📈 Performance

### Metrics
- **Query Response Time**: <100ms (95th percentile)
- **Event Processing**: 10,000 events/minute
- **Dashboard Load**: <2 seconds
- **Memory Usage**: <512MB under normal load

### Optimization
- **Database indexing** on frequently queried columns
- **Redis caching** for expensive aggregations
- **Batch processing** for historical data
- **Connection pooling** for database efficiency

## 🧪 Testing

### Unit Tests
```bash
cd apps/backend/java-services/business-services/analytics-engine
mvn test
```

### Integration Tests
```bash
mvn verify -P integration-tests
```

### API Testing
```bash
# Test analytics overview
curl -H "Authorization: Bearer <token>" \
     -H "X-Tenant-ID: tenant1" \
     http://localhost:8080/api/v1/analytics/overview

# Test with date range
curl -H "Authorization: Bearer <token>" \
     -H "X-Tenant-ID: tenant1" \
     "http://localhost:8080/api/v1/analytics/reservations?period=custom&startDate=2025-10-01&endDate=2025-10-10"
```

## 🔍 Monitoring & Observability

### OpenTelemetry Traces
View distributed traces in Jaeger:
- **URL**: http://localhost:16686
- **Service**: analytics-engine

### Prometheus Metrics
Key metrics exposed:
```
# Request metrics
http_requests_total{service="analytics-engine"}
http_request_duration_seconds{service="analytics-engine"}

# Business metrics
analytics_events_processed_total
analytics_query_duration_seconds
analytics_cache_hits_total
analytics_cache_misses_total
```

### Health Checks
```bash
# Application health
curl http://localhost:8080/actuator/health

# Detailed health (includes dependencies)
curl http://localhost:8080/actuator/health/detail
```

## 🛠️ Development

### Local Development
```bash
# Run with development profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run with debug
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

### Database Migrations
```sql
-- V001__create_analytics_tables.sql
CREATE TABLE analytics_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    event_data JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP
);

CREATE INDEX idx_analytics_events_tenant_type ON analytics_events(tenant_id, event_type);
CREATE INDEX idx_analytics_events_created ON analytics_events(created_at);
```

## 🚨 Troubleshooting

### Common Issues

**High Memory Usage**
```bash
# Check memory usage
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Tune JVM settings
export JAVA_OPTS="-Xmx512m -XX:+UseG1GC"
```

**Slow Queries**
```bash
# Enable query logging
logging.level.org.hibernate.SQL=DEBUG

# Check slow queries in logs
tail -f logs/analytics-engine.log | grep "slow query"
```

**Event Processing Lag**
```bash
# Check Kafka consumer lag
docker exec -it modern-reservation-kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --describe --group analytics-engine
```

---

## 📚 Related Documentation
- [Business Services Overview](../index.md)
- [Multi-Tenancy Guide](../../../MULTI_TENANCY.md)
- [OpenTelemetry Configuration](../../../references/)
