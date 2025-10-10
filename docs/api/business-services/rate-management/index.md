# Rate Management API

The Rate Management service handles dynamic pricing, rate optimization, and revenue management for the Modern Reservation System.

## 🎯 Overview

| Property | Value |
|----------|-------|
| **Service Name** | rate-management |
| **Port** | 8083 |
| **Health Check** | `/actuator/health` |
| **API Base URL** | `http://localhost:8083/api/v1/rates` |
| **OpenAPI Spec** | `/v3/api-docs` |
| **Swagger UI** | `/swagger-ui.html` |

## 🚀 Quick Start

### Start the Service
```bash
# Via dev script
./dev.sh start rate-management

# Via Maven
cd apps/backend/java-services/business-services/rate-management
mvn spring-boot:run
```

### Health Check
```bash
curl http://localhost:8083/actuator/health
```

## 💰 Core Features

### Dynamic Pricing
- **Real-time Rate Adjustments** based on demand and availability
- **Seasonal Pricing** with automatic adjustments
- **Event-based Pricing** for special occasions and local events
- **Competitor Price Monitoring** and automated responses
- **Yield Management** optimization

### Revenue Management
- **Revenue Optimization** algorithms
- **Demand Forecasting** using historical data and ML
- **Price Elasticity Analysis** for optimal pricing
- **Channel-specific Pricing** for different booking sources
- **Group Rate Management** for bulk bookings

### Rate Types
- **Base Rates** - Standard room rates
- **Package Rates** - Bundled services and amenities
- **Promotional Rates** - Limited-time offers and discounts
- **Corporate Rates** - Negotiated business rates
- **Long Stay Rates** - Extended stay discounts

## 🔌 API Endpoints

### Get Current Rates
```http
GET /api/v1/rates/current
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
```

**Query Parameters:**
- `propertyId` (string, required): Property identifier
- `checkIn` (string, required): Check-in date (ISO 8601)
- `checkOut` (string, required): Check-out date (ISO 8601)
- `roomType` (string, optional): Filter by room type
- `rateType` (string, optional): Filter by rate type (BASE, PACKAGE, PROMOTIONAL)

**Response:**
```json
{
  "propertyId": "prop-123",
  "checkIn": "2025-10-15",
  "checkOut": "2025-10-17",
  "rates": [
    {
      "roomType": "standard",
      "rateType": "BASE",
      "currency": "USD",
      "dailyRates": [
        {
          "date": "2025-10-15",
          "rate": 120.00,
          "availability": 15
        },
        {
          "date": "2025-10-16",
          "rate": 135.00,
          "availability": 12
        }
      ],
      "totalRate": 255.00,
      "averageRate": 127.50,
      "taxes": 25.50,
      "fees": 10.00
    }
  ],
  "restrictions": {
    "minimumStay": 1,
    "maximumStay": 7,
    "advanceBooking": 90
  }
}
```

### Update Base Rates
```http
PUT /api/v1/rates/base
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
Content-Type: application/json
```

**Request Body:**
```json
{
  "propertyId": "prop-123",
  "roomType": "standard",
  "effectiveDate": "2025-10-15",
  "endDate": "2025-12-31",
  "rates": [
    {
      "dayOfWeek": "MONDAY",
      "rate": 120.00
    },
    {
      "dayOfWeek": "FRIDAY",
      "rate": 150.00
    },
    {
      "dayOfWeek": "SATURDAY",
      "rate": 180.00
    }
  ],
  "seasonalAdjustments": [
    {
      "name": "Holiday Season",
      "startDate": "2025-12-20",
      "endDate": "2025-12-31",
      "adjustment": 1.25,
      "adjustmentType": "MULTIPLIER"
    }
  ]
}
```

**Response:**
```json
{
  "rateId": "rate-456",
  "status": "ACTIVE",
  "effectiveDate": "2025-10-15",
  "updatedAt": "2025-10-10T12:00:00Z",
  "affectedDates": 77,
  "previewUrl": "/api/v1/rates/rate-456/preview"
}
```

### Create Promotional Rate
```http
POST /api/v1/rates/promotional
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
Content-Type: application/json
```

**Request Body:**
```json
{
  "propertyId": "prop-123",
  "promotionName": "Early Bird Special",
  "description": "Book 30 days in advance and save 20%",
  "roomTypes": ["standard", "deluxe"],
  "discount": {
    "type": "PERCENTAGE",
    "value": 20.0
  },
  "conditions": {
    "advanceBookingDays": 30,
    "minimumStay": 2,
    "blackoutDates": ["2025-12-25", "2025-12-31"]
  },
  "validity": {
    "startDate": "2025-10-15",
    "endDate": "2025-12-15",
    "stayPeriod": {
      "startDate": "2025-11-01",
      "endDate": "2025-12-31"
    }
  },
  "inventory": {
    "totalRooms": 10,
    "maxPerDay": 3
  }
}
```

**Response:**
```json
{
  "promotionId": "promo-789",
  "status": "ACTIVE",
  "promoCode": "EARLYBIRD2025",
  "createdAt": "2025-10-10T12:00:00Z",
  "estimatedRevenue": 15000.00,
  "estimatedBookings": 25
}
```

### Rate Analysis
```http
GET /api/v1/rates/analysis
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
```

**Query Parameters:**
- `propertyId` (string, required): Property identifier
- `startDate` (string, required): Analysis start date
- `endDate` (string, required): Analysis end date
- `roomType` (string, optional): Filter by room type

**Response:**
```json
{
  "period": {
    "startDate": "2025-10-01",
    "endDate": "2025-10-31"
  },
  "performance": {
    "averageRate": 145.50,
    "occupancyRate": 82.5,
    "revenuePAR": 120.04,
    "rateVariance": 15.2
  },
  "optimization": {
    "potentialRevenue": 25000.00,
    "currentRevenue": 22500.00,
    "opportunityLoss": 2500.00,
    "recommendations": [
      {
        "action": "INCREASE_RATE",
        "dates": ["2025-10-20", "2025-10-21"],
        "currentRate": 120.00,
        "suggestedRate": 140.00,
        "expectedImpact": "+$800 revenue"
      }
    ]
  },
  "competitors": {
    "averageMarketRate": 138.75,
    "position": "ABOVE_MARKET",
    "priceIndex": 105.2
  }
}
```

### Yield Management
```http
POST /api/v1/rates/yield-optimization
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
Content-Type: application/json
```

**Request Body:**
```json
{
  "propertyId": "prop-123",
  "optimizationPeriod": {
    "startDate": "2025-10-15",
    "endDate": "2025-11-15"
  },
  "objectives": {
    "primary": "MAXIMIZE_REVENUE",
    "constraints": {
      "minOccupancy": 70.0,
      "maxRateIncrease": 25.0
    }
  },
  "strategy": "DYNAMIC"
}
```

**Response:**
```json
{
  "optimizationId": "opt-123",
  "status": "COMPLETED",
  "results": {
    "projectedRevenue": 85000.00,
    "currentRevenue": 78000.00,
    "revenueIncrease": 9.0,
    "averageRateChange": 8.5,
    "occupancyImpact": -2.1
  },
  "recommendations": [
    {
      "date": "2025-10-20",
      "roomType": "standard",
      "currentRate": 120.00,
      "optimizedRate": 135.00,
      "confidence": 0.87
    }
  ],
  "implementationUrl": "/api/v1/rates/opt-123/implement"
}
```

## 📡 Event Consumption & Publishing

### Consumed Events
```yaml
# Availability updates affect pricing
Topic: availability.updated
Handler: adjustRatesForAvailability()

# Booking confirmations trigger dynamic pricing
Topic: reservation.created
Handler: updateDemandBasedPricing()

# Competitor rate updates
Topic: competitor.rates.updated
Handler: adjustCompetitivePosition()
```

### Published Events
```yaml
Topic: rate.updated
Schema:
  - propertyId: string
  - roomType: string
  - rateType: enum
  - newRate: decimal
  - effectiveDate: date
  - updatedAt: timestamp

Topic: promotion.created
Schema:
  - promotionId: string
  - propertyId: string
  - promoCode: string
  - discount: object
  - validity: object
```

## 🔧 Configuration

### Application Properties
```yaml
# Rate Management Configuration
rate-management:
  pricing:
    dynamic-pricing-enabled: true
    yield-optimization: true
    competitor-monitoring: true

  algorithms:
    demand-forecasting:
      enabled: true
      model: "LINEAR_REGRESSION"
      training-period-days: 365

    price-optimization:
      algorithm: "GENETIC_ALGORITHM"
      iterations: 1000
      convergence-threshold: 0.001

  external:
    competitor-api:
      enabled: true
      update-frequency: "0 0 */6 * * *"  # Every 6 hours

    market-data:
      provider: "STR"
      api-key: ${STR_API_KEY}

  cache:
    rates-ttl: 300s
    analysis-ttl: 3600s

# Database Configuration
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/rates
    username: rate_user
    password: ${RATE_DB_PASSWORD}
```

### Environment Variables
```bash
# External APIs
STR_API_KEY=your-str-api-key
COMPETITOR_API_KEY=your-competitor-api-key

# Database
RATE_DB_PASSWORD=secure_rate_password

# Machine Learning
ML_MODEL_PATH=/data/models/demand-forecast.pkl
OPTIMIZATION_WORKERS=4

# Monitoring
OTEL_SERVICE_NAME=rate-management
OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317
```

## 📈 Performance & Analytics

### Performance Metrics
- **Rate Calculation**: <100ms (95th percentile)
- **Optimization Execution**: <5 minutes for 30-day period
- **Real-time Updates**: <200ms propagation
- **Forecast Accuracy**: 85% within 10% variance

### Revenue Analytics
```java
// Example: Revenue optimization tracking
@Component
public class RevenueAnalytics {

    @EventListener
    public void trackRateChange(RateUpdatedEvent event) {
        // Calculate revenue impact
        RevenueImpact impact = calculateImpact(
            event.getOldRate(),
            event.getNewRate(),
            event.getAvailability()
        );

        // Store for analysis
        analyticsRepository.save(impact);

        // Publish metrics
        meterRegistry.counter("rate.changes.total",
            "property", event.getPropertyId(),
            "direction", impact.getDirection())
            .increment();
    }
}
```

## 🧪 Testing

### Unit Tests
```bash
cd apps/backend/java-services/business-services/rate-management
mvn test
```

### Rate Testing
```bash
# Test rate calculation
curl -H "Authorization: Bearer $TOKEN" \
     -H "X-Tenant-ID: test-tenant" \
     "http://localhost:8083/api/v1/rates/current?propertyId=prop-123&checkIn=2025-10-15&checkOut=2025-10-17"

# Test promotional rate creation
curl -X POST http://localhost:8083/api/v1/rates/promotional \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: test-tenant" \
  -H "Content-Type: application/json" \
  -d '{
    "propertyId": "prop-123",
    "promotionName": "Test Promotion",
    "discount": {"type": "PERCENTAGE", "value": 15.0},
    "validity": {
      "startDate": "2025-10-15",
      "endDate": "2025-11-15"
    }
  }'

# Test yield optimization
curl -X POST http://localhost:8083/api/v1/rates/yield-optimization \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: test-tenant" \
  -H "Content-Type: application/json" \
  -d '{
    "propertyId": "prop-123",
    "optimizationPeriod": {
      "startDate": "2025-10-15",
      "endDate": "2025-11-15"
    },
    "objectives": {"primary": "MAXIMIZE_REVENUE"}
  }'
```

## 🛠️ Development

### Local Development
```bash
# Run with development profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run with debug
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5008"
```

### Database Schema
```sql
-- Base rates table
CREATE TABLE base_rates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    property_id VARCHAR(50) NOT NULL,
    room_type VARCHAR(50) NOT NULL,
    rate_date DATE NOT NULL,
    rate DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    day_of_week INTEGER,
    is_weekend BOOLEAN DEFAULT FALSE,
    seasonal_multiplier DECIMAL(4,2) DEFAULT 1.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, property_id, room_type, rate_date)
);

-- Promotional rates table
CREATE TABLE promotional_rates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    property_id VARCHAR(50) NOT NULL,
    promotion_name VARCHAR(100) NOT NULL,
    promo_code VARCHAR(20) UNIQUE,
    discount_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(10,2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    stay_start_date DATE,
    stay_end_date DATE,
    conditions JSONB,
    inventory_limit INTEGER,
    used_inventory INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Rate optimization history
CREATE TABLE optimization_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    property_id VARCHAR(50) NOT NULL,
    optimization_date DATE NOT NULL,
    algorithm_used VARCHAR(50),
    revenue_before DECIMAL(12,2),
    revenue_after DECIMAL(12,2),
    performance_metrics JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 🚨 Troubleshooting

### Common Issues

**Rate Optimization Slow**
```bash
# Check optimization queue
curl http://localhost:8083/actuator/metrics/rate.optimization.queue.size

# Monitor algorithm performance
curl http://localhost:8083/actuator/metrics/rate.optimization.duration

# Check database performance
curl http://localhost:8083/actuator/metrics/hikaricp.connections.active
```

**Competitor Data Issues**
```bash
# Check competitor API status
curl http://localhost:8083/actuator/health/competitorApi

# View last sync status
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8083/api/v1/admin/competitor-sync/status

# Manual competitor data refresh
curl -X POST http://localhost:8083/api/v1/admin/competitor-sync/refresh \
     -H "Authorization: Bearer $ADMIN_TOKEN"
```

**Rate Calculation Errors**
```bash
# Validate rate rules
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8083/api/v1/admin/rates/validate/prop-123

# Clear rate cache
curl -X DELETE http://localhost:8083/api/v1/admin/cache/rates \
     -H "Authorization: Bearer $ADMIN_TOKEN"

# Recalculate rates for date range
curl -X POST http://localhost:8083/api/v1/admin/rates/recalculate \
     -H "Authorization: Bearer $ADMIN_TOKEN" \
     -d '{"propertyId": "prop-123", "startDate": "2025-10-15", "endDate": "2025-10-31"}'
```

---

## 📚 Related Documentation
- [Business Services Overview](../index.md)
- [Analytics Engine](../analytics-engine/)
- [Availability Calculator](../availability-calculator/)
- [Revenue Management Best Practices](../../../guides/revenue-management.md)
