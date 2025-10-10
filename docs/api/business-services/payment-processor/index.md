# Payment Processor API

The Payment Processor service handles secure payment processing, transaction management, and financial operations for the Modern Reservation System.

## 🎯 Overview

| Property | Value |
|----------|-------|
| **Service Name** | payment-processor |
| **Port** | 8082 |
| **Health Check** | `/actuator/health` |
| **API Base URL** | `http://localhost:8082/api/v1/payments` |
| **OpenAPI Spec** | `/v3/api-docs` |
| **Swagger UI** | `/swagger-ui.html` |

## 🚀 Quick Start

### Start the Service
```bash
# Via dev script
./dev.sh start payment-processor

# Via Maven
cd apps/backend/java-services/business-services/payment-processor
mvn spring-boot:run
```

### Health Check
```bash
curl http://localhost:8082/actuator/health
```

## 💳 Core Features

### Payment Processing
- **Multiple Payment Methods** (Credit Card, Bank Transfer, Digital Wallets)
- **Secure Tokenization** for sensitive payment data
- **PCI DSS Compliance** with data encryption
- **Real-time Authorization** and capture
- **Automated Refunds** and partial refunds

### Transaction Management
- **Idempotent Operations** for safe retries
- **Transaction State Management** (Pending, Authorized, Captured, Refunded)
- **Settlement Processing** with reconciliation
- **Multi-currency Support** with real-time exchange rates

### Financial Operations
- **Revenue Tracking** per property and tenant
- **Commission Calculations** for channel partners
- **Tax Processing** with regional compliance
- **Financial Reporting** integration

## 🔌 API Endpoints

### Process Payment
```http
POST /api/v1/payments/process
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
Content-Type: application/json
```

**Request Body:**
```json
{
  "reservationId": "res-123",
  "amount": 250.00,
  "currency": "USD",
  "paymentMethod": {
    "type": "CREDIT_CARD",
    "cardToken": "tok_1234567890",
    "cardLast4": "4242",
    "expiryMonth": "12",
    "expiryYear": "2026"
  },
  "billingAddress": {
    "name": "John Doe",
    "addressLine1": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "US"
  },
  "metadata": {
    "propertyId": "prop-456",
    "guestEmail": "john.doe@example.com"
  }
}
```

**Response:**
```json
{
  "paymentId": "pay-789",
  "status": "COMPLETED",
  "amount": 250.00,
  "currency": "USD",
  "transactionId": "txn-abc123",
  "authorizationCode": "AUTH456",
  "processedAt": "2025-10-10T12:00:00Z",
  "fees": {
    "processingFee": 7.50,
    "platformFee": 2.50
  },
  "receipt": {
    "receiptId": "rcpt-def789",
    "receiptUrl": "https://receipts.example.com/rcpt-def789"
  }
}
```

### Authorize Payment
```http
POST /api/v1/payments/authorize
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
Content-Type: application/json
```

**Request Body:**
```json
{
  "reservationId": "res-123",
  "amount": 250.00,
  "currency": "USD",
  "paymentMethod": {
    "type": "CREDIT_CARD",
    "cardToken": "tok_1234567890"
  },
  "holdDuration": 604800
}
```

**Response:**
```json
{
  "authorizationId": "auth-456",
  "status": "AUTHORIZED",
  "amount": 250.00,
  "currency": "USD",
  "expiresAt": "2025-10-17T12:00:00Z",
  "authorizationCode": "AUTH789"
}
```

### Capture Payment
```http
POST /api/v1/payments/{authorizationId}/capture
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
Content-Type: application/json
```

**Request Body:**
```json
{
  "amount": 250.00,
  "finalAmount": true
}
```

**Response:**
```json
{
  "paymentId": "pay-123",
  "captureId": "cap-456",
  "status": "CAPTURED",
  "amount": 250.00,
  "capturedAt": "2025-10-10T12:00:00Z"
}
```

### Refund Payment
```http
POST /api/v1/payments/{paymentId}/refund
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
Content-Type: application/json
```

**Request Body:**
```json
{
  "amount": 100.00,
  "reason": "GUEST_CANCELLATION",
  "description": "Partial refund for early cancellation"
}
```

**Response:**
```json
{
  "refundId": "ref-789",
  "status": "PROCESSED",
  "amount": 100.00,
  "currency": "USD",
  "processedAt": "2025-10-10T12:30:00Z",
  "refundMethod": "ORIGINAL_PAYMENT_METHOD",
  "estimatedArrival": "2025-10-12T12:00:00Z"
}
```

### Payment Status
```http
GET /api/v1/payments/{paymentId}
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
```

**Response:**
```json
{
  "paymentId": "pay-123",
  "reservationId": "res-456",
  "status": "COMPLETED",
  "amount": 250.00,
  "currency": "USD",
  "paymentMethod": {
    "type": "CREDIT_CARD",
    "cardLast4": "4242",
    "cardBrand": "VISA"
  },
  "timeline": [
    {
      "event": "AUTHORIZED",
      "timestamp": "2025-10-10T11:55:00Z",
      "amount": 250.00
    },
    {
      "event": "CAPTURED",
      "timestamp": "2025-10-10T12:00:00Z",
      "amount": 250.00
    }
  ],
  "fees": {
    "processingFee": 7.50,
    "platformFee": 2.50
  }
}
```

## 📡 Event Publishing

The Payment Processor publishes the following Kafka events:

### Payment Processed
```yaml
Topic: payment.processed
Schema:
  - paymentId: string
  - reservationId: string
  - amount: decimal
  - currency: string
  - status: enum (COMPLETED, FAILED, REFUNDED)
  - processedAt: timestamp
  - fees: object
```

### Payment Failed
```yaml
Topic: payment.failed
Schema:
  - paymentId: string
  - reservationId: string
  - amount: decimal
  - failureReason: string
  - failureCode: string
  - failedAt: timestamp
```

### Refund Processed
```yaml
Topic: refund.processed
Schema:
  - refundId: string
  - paymentId: string
  - reservationId: string
  - amount: decimal
  - reason: string
  - processedAt: timestamp
```

## 🔧 Configuration

### Application Properties
```yaml
# Payment Processor Configuration
payment:
  providers:
    stripe:
      enabled: true
      secret-key: ${STRIPE_SECRET_KEY}
      webhook-secret: ${STRIPE_WEBHOOK_SECRET}

    square:
      enabled: false
      access-token: ${SQUARE_ACCESS_TOKEN}

  security:
    encryption-key: ${PAYMENT_ENCRYPTION_KEY}
    token-expiry: 3600s

  processing:
    max-retries: 3
    retry-delay: 5s
    timeout: 30s

  fees:
    processing-rate: 0.029  # 2.9%
    fixed-fee: 0.30
    platform-rate: 0.01    # 1%

# Database Configuration
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/payments
    username: payment_user
    password: ${PAYMENT_DB_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        jdbc:
          batch_size: 50
```

### Environment Variables
```bash
# Payment Providers
STRIPE_SECRET_KEY=sk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...
SQUARE_ACCESS_TOKEN=sq0atp-...

# Security
PAYMENT_ENCRYPTION_KEY=your-32-character-encryption-key
PAYMENT_DB_PASSWORD=secure_payment_password

# Monitoring
OTEL_SERVICE_NAME=payment-processor
OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317
```

## 🔐 Security

### PCI DSS Compliance
- **Data Encryption** - All sensitive payment data encrypted at rest and in transit
- **Tokenization** - Credit card numbers replaced with secure tokens
- **Access Controls** - Role-based access with audit logging
- **Network Security** - TLS 1.3 for all API communications

### Payment Security
```java
// Example: Secure payment processing
@PostMapping("/process")
public ResponseEntity<PaymentResponse> processPayment(
    @Valid @RequestBody PaymentRequest request) {

    // Validate payment method
    paymentValidator.validate(request.getPaymentMethod());

    // Encrypt sensitive data
    EncryptedPaymentData encryptedData = encryptionService
        .encrypt(request.getPaymentMethod());

    // Process with external provider
    PaymentResult result = paymentProvider.process(encryptedData);

    // Audit log (without sensitive data)
    auditService.logPaymentAttempt(request.getReservationId(),
        result.getStatus());

    return ResponseEntity.ok(result.toResponse());
}
```

## 📈 Performance & Monitoring

### Performance Metrics
- **Payment Processing Time**: <2 seconds (95th percentile)
- **Authorization Rate**: 97.5% success rate
- **Refund Processing**: <24 hours
- **Uptime**: 99.99% availability

### Monitoring
```
# Payment metrics
payment_transactions_total{status="completed"}
payment_processing_duration_seconds
payment_authorization_rate
payment_refund_success_rate

# Business metrics
payment_revenue_total{currency="USD"}
payment_fees_collected_total
payment_provider_success_rate{provider="stripe"}
```

## 🧪 Testing

### Unit Tests
```bash
cd apps/backend/java-services/business-services/payment-processor
mvn test
```

### Integration Tests
```bash
mvn verify -P integration-tests
```

### Payment Testing
```bash
# Test with Stripe test cards
# Successful payment
curl -X POST http://localhost:8082/api/v1/payments/process \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: test-tenant" \
  -H "Content-Type: application/json" \
  -d '{
    "reservationId": "test-res-123",
    "amount": 100.00,
    "currency": "USD",
    "paymentMethod": {
      "type": "CREDIT_CARD",
      "cardToken": "tok_visa"
    }
  }'

# Declined payment test
curl -X POST http://localhost:8082/api/v1/payments/process \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: test-tenant" \
  -H "Content-Type: application/json" \
  -d '{
    "reservationId": "test-res-456",
    "amount": 100.00,
    "currency": "USD",
    "paymentMethod": {
      "type": "CREDIT_CARD",
      "cardToken": "tok_chargeDeclined"
    }
  }'
```

## 🛠️ Development

### Local Development
```bash
# Run with development profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run with debug
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5007"
```

### Database Schema
```sql
-- Payments table
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    reservation_id VARCHAR(50) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_method_type VARCHAR(50),
    external_transaction_id VARCHAR(100),
    authorization_code VARCHAR(50),
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Payment methods table (tokenized)
CREATE TABLE payment_methods (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    guest_id VARCHAR(50),
    type VARCHAR(50) NOT NULL,
    token VARCHAR(100) NOT NULL,
    last_four VARCHAR(4),
    expiry_month INTEGER,
    expiry_year INTEGER,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Refunds table
CREATE TABLE refunds (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID REFERENCES payments(id),
    amount DECIMAL(10,2) NOT NULL,
    reason VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    external_refund_id VARCHAR(100),
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 🚨 Troubleshooting

### Common Issues

**Payment Declined**
```bash
# Check payment provider status
curl http://localhost:8082/actuator/health/paymentProvider

# View detailed payment logs
tail -f logs/payment-processor.log | grep "payment_id"

# Test with different payment method
# Use test cards for debugging
```

**Slow Payment Processing**
```bash
# Check payment provider latency
curl http://localhost:8082/actuator/metrics/payment.provider.response.time

# Monitor database connections
curl http://localhost:8082/actuator/metrics/hikaricp.connections.active

# Check for payment queue backlog
curl http://localhost:8082/actuator/metrics/payment.queue.size
```

**Refund Issues**
```bash
# Check refund status
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8082/api/v1/payments/pay-123/refunds

# Manual refund processing
curl -X POST http://localhost:8082/api/v1/admin/refunds/retry \
     -H "Authorization: Bearer $ADMIN_TOKEN" \
     -d '{"refundId": "ref-456"}'
```

---

## 📚 Related Documentation
- [Business Services Overview](../index.md)
- [Analytics Engine](../analytics-engine/)
- [Reservation Engine](../reservation-engine/)
- [Multi-Tenancy Implementation](../../../MULTI_TENANCY.md)
