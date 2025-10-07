# Quick Reference: Working with Avro Events

## Creating a New Event

### 1. Define Avro Schema
Create `.avsc` file in `libs/shared/backend-utils/src/main/avro/`

```json
{
  "type": "record",
  "name": "PaymentProcessedEvent",
  "namespace": "com.reservation.shared.events",
  "doc": "Event published when a payment is processed",
  "fields": [
    {
      "name": "eventId",
      "type": {"type": "string", "avro.java.string": "String"},
      "doc": "Unique event identifier (UUID)"
    },
    {
      "name": "eventType",
      "type": {"type": "string", "avro.java.string": "String"},
      "doc": "Type of event (PAYMENT_PROCESSED)"
    },
    {
      "name": "timestamp",
      "type": {"type": "long", "logicalType": "timestamp-millis"},
      "doc": "Event timestamp in milliseconds"
    },
    {
      "name": "version",
      "type": "int",
      "doc": "Schema version"
    },
    {
      "name": "paymentId",
      "type": {"type": "string", "avro.java.string": "String"},
      "doc": "UUID of the payment"
    },
    {
      "name": "amount",
      "type": {"type": "string", "avro.java.string": "String"},
      "doc": "Payment amount (use string for precision)"
    },
    {
      "name": "currency",
      "type": {"type": "string", "avro.java.string": "String"},
      "doc": "Currency code (USD, EUR, etc.)"
    },
    {
      "name": "status",
      "type": {"type": "string", "avro.java.string": "String"},
      "doc": "Payment status (SUCCESS, FAILED, PENDING)"
    }
  ]
}
```

### 2. Generate Java Classes
```bash
cd libs/shared/backend-utils
mvn clean install -DskipTests
```

### 3. Use in Your Service

#### Publish Event
```java
@Service
public class PaymentService {

    @Autowired
    private EventPublisher eventPublisher;

    public void processPayment(Payment payment) {
        // Process payment logic...

        // Create Avro event
        PaymentProcessedEvent event = PaymentProcessedEvent.newBuilder()
            .setEventId(UUID.randomUUID().toString())
            .setEventType("PAYMENT_PROCESSED")
            .setTimestamp(Instant.now())
            .setVersion(1)
            .setPaymentId(payment.getId())
            .setAmount(payment.getAmount().toString())
            .setCurrency(payment.getCurrency())
            .setStatus(payment.getStatus().name())
            .build();

        // Publish to Kafka
        eventPublisher.publish("payment.processed", event);
    }
}
```

#### Consume Event
```java
@Service
public class PaymentAnalyticsConsumer {

    @KafkaListener(
        topics = "payment.processed",
        groupId = "analytics-engine"
    )
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        log.info("Processing payment: {}", event.getPaymentId());
        log.info("Amount: {} {}", event.getAmount(), event.getCurrency());

        // Your business logic here
    }
}
```

---

## Avro Data Types

### Primitive Types
```json
"type": "string"           // String
"type": "int"              // 32-bit integer
"type": "long"             // 64-bit integer
"type": "float"            // Single precision
"type": "double"           // Double precision
"type": "boolean"          // true/false
"type": "bytes"            // Binary data
"type": "null"             // Null value
```

### Logical Types
```json
// Timestamp (milliseconds since epoch)
{"type": "long", "logicalType": "timestamp-millis"}

// Date (days since epoch)
{"type": "int", "logicalType": "date"}

// UUID
{"type": "string", "logicalType": "uuid"}

// Decimal (for money)
{
  "type": "bytes",
  "logicalType": "decimal",
  "precision": 10,
  "scale": 2
}
```

### Complex Types
```json
// Optional field
{
  "name": "middleName",
  "type": ["null", "string"],
  "default": null
}

// Array
{
  "name": "tags",
  "type": {"type": "array", "items": "string"}
}

// Enum
{
  "name": "status",
  "type": {
    "type": "enum",
    "name": "ReservationStatus",
    "symbols": ["PENDING", "CONFIRMED", "CANCELLED"]
  }
}

// Record (nested object)
{
  "name": "address",
  "type": {
    "type": "record",
    "name": "Address",
    "fields": [
      {"name": "street", "type": "string"},
      {"name": "city", "type": "string"}
    ]
  }
}
```

---

## Schema Evolution Rules

### ✅ Backward Compatible (Old consumers can read new data)
- Add optional fields (with defaults)
- Remove optional fields

```json
// Version 1
{"name": "email", "type": "string"}

// Version 2 - Add optional field
{"name": "phoneNumber", "type": ["null", "string"], "default": null}
```

### ✅ Forward Compatible (New consumers can read old data)
- Add fields with defaults
- Remove fields

```json
// Version 1
{
  "name": "status",
  "type": "string"
}

// Version 2 - Add field with default
{
  "name": "priority",
  "type": "string",
  "default": "NORMAL"
}
```

### ❌ Breaking Changes (Requires new topic/version)
- Change field type
- Remove required field
- Rename field

---

## Common Patterns

### 1. Money/Decimal Values
**Always use string for precision!**

```json
{
  "name": "totalAmount",
  "type": {"type": "string", "avro.java.string": "String"},
  "doc": "Amount as string (e.g., '150.00')"
}
```

```java
// Publish
.setTotalAmount(amount.toString())

// Consume
BigDecimal amount = new BigDecimal(event.getTotalAmount());
```

### 2. Dates
**Use ISO-8601 strings or timestamp-millis**

```json
// As string
{
  "name": "checkInDate",
  "type": "string",
  "doc": "ISO-8601 date (YYYY-MM-DD)"
}

// As timestamp
{
  "name": "createdAt",
  "type": {"type": "long", "logicalType": "timestamp-millis"}
}
```

### 3. Enums
**Use strings for flexibility**

```json
{
  "name": "status",
  "type": {"type": "string", "avro.java.string": "String"},
  "doc": "Status: PENDING, CONFIRMED, CANCELLED"
}
```

### 4. UUIDs
**Use strings with UUID logical type**

```json
{
  "name": "id",
  "type": {
    "type": "string",
    "avro.java.string": "String",
    "logicalType": "uuid"
  }
}
```

---

## Testing

### Test Event Publishing
```bash
# Start service
cd apps/backend/java-services/business-services/reservation-engine
mvn spring-boot:run

# Publish test event (in another terminal)
PASSWORD=$(grep "Using generated security password" /tmp/reservation-engine-avro.log | tail -1 | awk '{print $NF}')
curl -u user:$PASSWORD "http://localhost:8081/reservation-engine/api/test/kafka"
```

### Check Schema Registry
```bash
# List all schemas
curl http://localhost:8085/subjects

# Get specific schema
curl http://localhost:8085/subjects/reservation.created-value/versions/latest | jq .

# Check compatibility
curl -X POST -H "Content-Type: application/json" \
  --data '{"schema": "..."}' \
  http://localhost:8085/compatibility/subjects/reservation.created-value/versions/latest
```

### View Messages in Kafka UI
1. Open: http://localhost:8090
2. Navigate to **Topics** → **reservation.created**
3. Click **Messages** tab
4. See Avro-deserialized content

---

## Troubleshooting

### Schema Not Registered
**Error**: `Schema not found`

**Check:**
```bash
# Is Schema Registry running?
curl http://localhost:8085/

# Can service reach it?
docker ps | grep schema-registry
```

**Fix**: Ensure `spring.kafka.properties.schema.registry.url: http://localhost:8085`

### Incompatible Schema
**Error**: `Schema being registered is incompatible`

**Check compatibility:**
```bash
curl http://localhost:8085/config/reservation.created-value
```

**Fix**: Follow backward/forward compatibility rules

### Serialization Error
**Error**: `Failed to serialize event`

**Check:**
1. All required fields set in builder
2. Field types match schema
3. Schema Registry accessible

```java
// Ensure all required fields
ReservationCreatedEvent event = ReservationCreatedEvent.newBuilder()
    .setEventId(...)         // Required
    .setEventType(...)       // Required
    .setTimestamp(...)       // Required
    .setVersion(...)         // Required
    .setReservationId(...)   // Required
    // ... all required fields
    .build();
```

---

## Useful Commands

```bash
# Rebuild with Avro
cd libs/shared/backend-utils && mvn clean install -DskipTests

# Check generated classes
ls -la libs/shared/backend-utils/target/generated-sources/avro/com/reservation/shared/events/

# View Schema Registry logs
docker logs modern-reservation-schema-registry

# View Kafka logs
docker logs modern-reservation-kafka

# List topics
docker exec modern-reservation-kafka kafka-topics --list --bootstrap-server localhost:9092

# Describe topic
docker exec modern-reservation-kafka kafka-topics --describe --topic reservation.created --bootstrap-server localhost:9092
```

---

## Resources

- **Avro Docs**: https://avro.apache.org/docs/current/
- **Schema Registry**: https://docs.confluent.io/platform/current/schema-registry/
- **Kafka UI**: http://localhost:8090
- **Schema Registry API**: http://localhost:8085

---

**Quick Start Checklist:**
- [ ] Create `.avsc` schema file
- [ ] Run `mvn clean install` in backend-utils
- [ ] Use builder pattern to create event
- [ ] Call `eventPublisher.publish(topic, event)`
- [ ] Verify in Kafka UI (http://localhost:8090)
- [ ] Check Schema Registry (http://localhost:8085/subjects)

**Need Help?** Check `/home/subramani/modern-reservation/AVRO_MIGRATION_COMPLETE.md`
