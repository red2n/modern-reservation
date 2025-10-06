# Kafka Implementation Guide
## Event-Driven Communication Between Java Business Services

### Document Information
- **Date:** October 6, 2025
- **Project:** Modern Reservation System
- **Architecture:** Event-Driven Microservices with Apache Kafka

---

## 📋 Executive Summary

### Current State Analysis
Your system currently uses:
- ✅ **Kafka configured** in `application.yml` (bootstrap-servers: localhost:9092)
- ✅ **KafkaTemplate** already injected in services (Reservation, Payment, Availability)
- ✅ **Synchronous patterns** ready for transformation
- ⚠️ **HTTP/REST** communication exists via Feign Clients (if any)
- ⚠️ **Kafka not running** in Docker infrastructure yet

### Services Overview
```
┌─────────────────────────────────────────────────────────────────┐
│                    Business Services Layer                       │
├─────────────────────────────────────────────────────────────────┤
│  1. Reservation Engine      (Port 8081) - Order coordinator     │
│  2. Availability Calculator (Port 8082) - Inventory manager     │
│  3. Rate Management         (Port 8083) - Pricing engine        │
│  4. Payment Processor       (Port 8084) - Financial operations  │
│  5. Analytics Engine        (Port 8086) - Data aggregator       │
│  6. Batch Processor         (Port 8085) - Background jobs       │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🎯 Big Picture: Event-Driven Architecture

### Architecture Transformation

```
BEFORE (Synchronous - Request/Response):
┌──────────────┐  HTTP/REST   ┌──────────────┐
│ Reservation  │──────────────>│ Availability │
│   Engine     │<──────────────│ Calculator   │
└──────────────┘   Response    └──────────────┘
                                        │
                      HTTP/REST         │
                                        ▼
                                ┌──────────────┐
                                │   Payment    │
                                │  Processor   │
                                └──────────────┘

AFTER (Asynchronous - Event-Driven):
┌──────────────┐                              ┌──────────────┐
│ Reservation  │──┐                        ┌──│ Availability │
│   Engine     │  │  reservation.created   │  │ Calculator   │
└──────────────┘  │                        │  └──────────────┘
                  ▼                        │
         ┌────────────────┐               │  ┌──────────────┐
         │  Kafka Broker  │───────────────┼──│   Payment    │
         │  Event Stream  │               │  │  Processor   │
         └────────────────┘               │  └──────────────┘
                  │                        │
                  │   availability.updated │  ┌──────────────┐
                  └────────────────────────┼──│  Analytics   │
                                           │  │   Engine     │
                                           └──└──────────────┘
```

### Key Benefits

| Aspect | Before (HTTP/REST) | After (Kafka Events) |
|--------|-------------------|---------------------|
| **Coupling** | Tight (Service knows others) | Loose (Services don't know each other) |
| **Availability** | Failure cascades | Services work independently |
| **Scalability** | Limited by slowest service | Each service scales independently |
| **Performance** | Synchronous blocking | Asynchronous non-blocking |
| **Auditability** | Logs only | Complete event history |
| **Replay** | Not possible | Can replay events |
| **Real-time** | Polling required | Push-based notifications |

---

## 🏗️ Event-Driven Architecture Design

### 1. Event Types & Topics Structure

```yaml
Kafka Topics:
├── reservation-events/
│   ├── reservation.created          # New reservation made
│   ├── reservation.confirmed        # Reservation confirmed
│   ├── reservation.modified         # Details changed
│   ├── reservation.cancelled        # Booking cancelled
│   ├── reservation.checkin          # Guest checked in
│   └── reservation.checkout         # Guest checked out
│
├── availability-events/
│   ├── availability.checked         # Availability query
│   ├── availability.updated         # Room inventory changed
│   ├── availability.blocked         # Rooms blocked
│   └── availability.released        # Rooms released
│
├── payment-events/
│   ├── payment.initiated            # Payment started
│   ├── payment.authorized           # Payment authorized
│   ├── payment.captured             # Payment captured
│   ├── payment.failed               # Payment failed
│   ├── payment.refunded             # Refund processed
│   └── payment.settled              # Payment settled
│
├── rate-events/
│   ├── rate.created                 # New rate plan
│   ├── rate.updated                 # Rate changed
│   ├── rate.applied                 # Rate applied to booking
│   └── rate.expired                 # Rate no longer valid
│
└── analytics-events/
    ├── analytics.reservation        # Reservation metrics
    ├── analytics.revenue            # Revenue metrics
    └── analytics.occupancy          # Occupancy metrics
```

### 2. Event Choreography vs Orchestration

**RECOMMENDED: Event Choreography (Reactive)**

Each service reacts to events independently:

```
Booking Flow Example:

1. Guest creates reservation
   └──> Reservation Engine publishes: reservation.created
        ├──> Availability Calculator consumes → Updates inventory
        ├──> Payment Processor consumes → Initiates payment
        ├──> Analytics Engine consumes → Records metrics
        └──> Rate Management consumes → Tracks rate usage

2. Payment authorized
   └──> Payment Processor publishes: payment.authorized
        └──> Reservation Engine consumes → Updates status to CONFIRMED

3. Availability updated
   └──> Availability Calculator publishes: availability.updated
        ├──> Rate Management consumes → Adjusts dynamic pricing
        └──> Analytics Engine consumes → Updates occupancy metrics
```

**Alternative: Saga Orchestration (Complex workflows)**

For critical multi-step transactions requiring rollback:

```
Reservation Saga Coordinator:
├── Step 1: Check Availability → availability-service
├── Step 2: Calculate Pricing → rate-service
├── Step 3: Authorize Payment → payment-service
├── Step 4: Create Reservation → reservation-service
└── Compensations: If any step fails, rollback previous steps
```

---

## 📊 Event Schema Design

### Standard Event Structure

```json
{
  "eventId": "uuid-v4",
  "eventType": "reservation.created",
  "eventVersion": "1.0",
  "timestamp": "2025-10-06T13:45:00Z",
  "source": "reservation-engine",
  "correlationId": "trace-id-for-distributed-tracing",
  "causationId": "parent-event-id",
  "aggregate": {
    "type": "Reservation",
    "id": "reservation-uuid"
  },
  "payload": {
    // Business data specific to event type
  },
  "metadata": {
    "userId": "user-who-triggered",
    "ipAddress": "client-ip",
    "userAgent": "client-info"
  }
}
```

### Example: Reservation Created Event

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "eventType": "reservation.created",
  "eventVersion": "1.0",
  "timestamp": "2025-10-06T13:45:00Z",
  "source": "reservation-engine",
  "correlationId": "trace-123-456",
  "aggregate": {
    "type": "Reservation",
    "id": "RSV-2025-001234"
  },
  "payload": {
    "confirmationNumber": "RSV-2025-001234",
    "propertyId": "PROP-001",
    "guestId": "GUEST-789",
    "guestDetails": {
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "phone": "+1234567890"
    },
    "stayDetails": {
      "checkInDate": "2025-11-01",
      "checkOutDate": "2025-11-05",
      "nights": 4,
      "roomTypeId": "ROOM-TYPE-DELUXE",
      "adults": 2,
      "children": 1
    },
    "pricing": {
      "roomRate": 150.00,
      "taxes": 18.00,
      "fees": 7.50,
      "totalAmount": 175.50,
      "currency": "USD"
    },
    "status": "CONFIRMED",
    "source": "DIRECT",
    "specialRequests": "Late check-in requested"
  },
  "metadata": {
    "createdBy": "booking-api",
    "clientIp": "192.168.1.100"
  }
}
```

---

## 🔧 Implementation Strategy

### Phase 1: Infrastructure Setup (Week 1)

#### 1.1 Add Kafka to Docker Infrastructure

**File:** `infrastructure/docker/docker-compose-infrastructure.yml`

```yaml
  # Apache Kafka with KRaft (No Zookeeper needed)
  kafka:
    image: bitnami/kafka:3.6
    container_name: modern-reservation-kafka
    ports:
      - "9092:9092"
      - "9094:9094"
    environment:
      # KRaft settings (Kafka without Zookeeper)
      - KAFKA_CFG_NODE_ID=1
      - KAFKA_CFG_PROCESS_ROLES=controller,broker
      - KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=1@kafka:9093
      - KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER

      # Listeners
      - KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093,EXTERNAL://:9094
      - KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092,EXTERNAL://localhost:9094
      - KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,EXTERNAL:PLAINTEXT

      # Inter-broker
      - KAFKA_CFG_INTER_BROKER_LISTENER_NAME=PLAINTEXT

      # Topic defaults
      - KAFKA_CFG_AUTO_CREATE_TOPICS_ENABLE=true
      - KAFKA_CFG_NUM_PARTITIONS=3
      - KAFKA_CFG_DEFAULT_REPLICATION_FACTOR=1

      # Log settings
      - KAFKA_CFG_LOG_RETENTION_HOURS=168  # 7 days
      - KAFKA_CFG_LOG_SEGMENT_BYTES=1073741824  # 1GB
    volumes:
      - kafka_data:/bitnami/kafka
    networks:
      - modern-reservation-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD-SHELL", "kafka-broker-api-versions.sh --bootstrap-server localhost:9092"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Kafka UI (Optional - for monitoring)
  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    container_name: modern-reservation-kafka-ui
    ports:
      - "8090:8080"
    environment:
      - KAFKA_CLUSTERS_0_NAME=local
      - KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS=kafka:9092
    depends_on:
      - kafka
    networks:
      - modern-reservation-network
    restart: unless-stopped

volumes:
  kafka_data:
```

#### 1.2 Update Infrastructure Scripts

Add Kafka to status checks in `scripts/check-infrastructure.sh`:

```bash
# Check Kafka
if docker ps --filter "name=modern-reservation-kafka" --format "{{.Names}}" | grep -q "modern-reservation-kafka"; then
    if docker exec modern-reservation-kafka kafka-broker-api-versions.sh --bootstrap-server localhost:9092 >/dev/null 2>&1; then
        print_table_row "kafka" "DOCKER" "9092" "Broker ready"
    else
        print_table_row "kafka" "WARNING" "9092" "Container running, broker not ready"
    fi
else
    print_table_row "kafka" "STOPPED" "9092" "Container not running"
fi
```

---

### Phase 2: Shared Event Library (Week 2)

#### 2.1 Create Event Models Module

**Location:** `libs/shared/backend-utils/src/main/java/com/modernreservation/shared/events/`

```java
// BaseEvent.java
package com.modernreservation.shared.events;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent {
    private String eventId;
    private String eventType;
    private String eventVersion;
    private Instant timestamp;
    private String source;
    private String correlationId;
    private String causationId;

    protected BaseEvent(String eventType, String source) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.eventVersion = "1.0";
        this.timestamp = Instant.now();
        this.source = source;
    }
}
```

```java
// ReservationCreatedEvent.java
package com.modernreservation.shared.events.reservation;

import com.modernreservation.shared.events.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReservationCreatedEvent extends BaseEvent {

    private String reservationId;
    private String confirmationNumber;
    private String propertyId;
    private String guestId;
    private GuestDetails guestDetails;
    private StayDetails stayDetails;
    private PricingDetails pricingDetails;
    private String status;
    private String source;

    public ReservationCreatedEvent() {
        super("reservation.created", "reservation-engine");
    }

    @Data
    public static class GuestDetails {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
    }

    @Data
    public static class StayDetails {
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        private Integer nights;
        private String roomTypeId;
        private Integer adults;
        private Integer children;
    }

    @Data
    public static class PricingDetails {
        private BigDecimal roomRate;
        private BigDecimal taxes;
        private BigDecimal fees;
        private BigDecimal totalAmount;
        private String currency;
    }
}
```

#### 2.2 Event Publisher Service

```java
// EventPublisher.java
package com.modernreservation.shared.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modernreservation.shared.events.BaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Publish event to Kafka topic
     * Topic name derived from event type (e.g., "reservation.created")
     */
    public CompletableFuture<SendResult<String, String>> publish(BaseEvent event) {
        try {
            String topic = event.getEventType();
            String key = event.getEventId();
            String payload = objectMapper.writeValueAsString(event);

            log.info("Publishing event: type={}, id={}, correlationId={}",
                    event.getEventType(), event.getEventId(), event.getCorrelationId());

            return kafkaTemplate.send(topic, key, payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish event: {}", event.getEventType(), ex);
                        } else {
                            log.info("Event published successfully: topic={}, partition={}, offset={}",
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        }
                    });
        } catch (Exception e) {
            log.error("Error serializing event", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Publish to specific topic (override default)
     */
    public CompletableFuture<SendResult<String, String>> publishToTopic(String topic, BaseEvent event) {
        try {
            String key = event.getEventId();
            String payload = objectMapper.writeValueAsString(event);
            return kafkaTemplate.send(topic, key, payload);
        } catch (Exception e) {
            log.error("Error publishing to topic: {}", topic, e);
            return CompletableFuture.failedFuture(e);
        }
    }
}
```

---

### Phase 3: Service Implementation (Week 3-4)

#### 3.1 Producer Example: Reservation Engine

**Update:** `ReservationService.java`

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final EventPublisher eventPublisher;  // Inject event publisher

    @CacheEvict(value = "reservations", allEntries = true)
    @Transactional
    public ReservationResponseDTO createReservation(ReservationRequestDTO request) {
        log.info("Creating new reservation for guest: {} {}",
                request.guestFirstName(), request.guestLastName());

        // 1. Create reservation entity (existing logic)
        Reservation reservation = buildReservation(request);
        Reservation saved = reservationRepository.save(reservation);

        // 2. Publish event to Kafka (NEW)
        publishReservationCreatedEvent(saved);

        // 3. Return response
        return toResponseDTO(saved);
    }

    private void publishReservationCreatedEvent(Reservation reservation) {
        ReservationCreatedEvent event = new ReservationCreatedEvent();
        event.setReservationId(reservation.getId().toString());
        event.setConfirmationNumber(reservation.getConfirmationNumber());
        event.setPropertyId(reservation.getPropertyId().toString());
        event.setGuestId(reservation.getGuestId().toString());

        // Guest details
        ReservationCreatedEvent.GuestDetails guestDetails = new ReservationCreatedEvent.GuestDetails();
        guestDetails.setFirstName(reservation.getGuestFirstName());
        guestDetails.setLastName(reservation.getGuestLastName());
        guestDetails.setEmail(reservation.getGuestEmail());
        guestDetails.setPhone(reservation.getGuestPhone());
        event.setGuestDetails(guestDetails);

        // Stay details
        ReservationCreatedEvent.StayDetails stayDetails = new ReservationCreatedEvent.StayDetails();
        stayDetails.setCheckInDate(reservation.getCheckInDate());
        stayDetails.setCheckOutDate(reservation.getCheckOutDate());
        stayDetails.setNights(reservation.getNights());
        stayDetails.setRoomTypeId(reservation.getRoomTypeId().toString());
        stayDetails.setAdults(reservation.getAdults());
        stayDetails.setChildren(reservation.getChildren());
        event.setStayDetails(stayDetails);

        // Pricing details
        ReservationCreatedEvent.PricingDetails pricingDetails = new ReservationCreatedEvent.PricingDetails();
        pricingDetails.setRoomRate(reservation.getRoomRate());
        pricingDetails.setTaxes(reservation.getTaxes());
        pricingDetails.setFees(reservation.getFees());
        pricingDetails.setTotalAmount(reservation.getTotalAmount());
        pricingDetails.setCurrency(reservation.getCurrency());
        event.setPricingDetails(pricingDetails);

        event.setStatus(reservation.getStatus().name());
        event.setSource(reservation.getSource().name());

        // Publish asynchronously
        eventPublisher.publish(event);
    }
}
```

#### 3.2 Consumer Example: Payment Processor

**Create:** `PaymentEventConsumer.java`

```java
package com.modernreservation.paymentprocessor.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modernreservation.shared.events.reservation.ReservationCreatedEvent;
import com.modernreservation.paymentprocessor.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    /**
     * Listen to reservation.created events
     * Automatically initiate payment authorization
     */
    @KafkaListener(
        topics = "reservation.created",
        groupId = "payment-processor-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleReservationCreated(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received event from topic: {}, partition: {}, offset: {}",
                topic, partition, offset);

        try {
            ReservationCreatedEvent event = objectMapper.readValue(
                    message, ReservationCreatedEvent.class);

            log.info("Processing reservation.created event for reservation: {}",
                    event.getConfirmationNumber());

            // Business logic: Initiate payment authorization
            paymentService.initiatePaymentForReservation(event);

            log.info("Successfully processed reservation.created event");

        } catch (Exception e) {
            log.error("Error processing reservation.created event", e);
            // TODO: Implement dead letter queue or retry logic
        }
    }
}
```

#### 3.3 Consumer Example: Availability Calculator

**Create:** `AvailabilityEventConsumer.java`

```java
package com.modernreservation.availabilitycalculator.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modernreservation.shared.events.reservation.ReservationCreatedEvent;
import com.modernreservation.shared.events.reservation.ReservationCancelledEvent;
import com.modernreservation.availabilitycalculator.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AvailabilityEventConsumer {

    private final AvailabilityService availabilityService;
    private final ObjectMapper objectMapper;

    /**
     * Reduce inventory when reservation created
     */
    @KafkaListener(
        topics = "reservation.created",
        groupId = "availability-calculator-group"
    )
    public void handleReservationCreated(String message) {
        try {
            ReservationCreatedEvent event = objectMapper.readValue(
                    message, ReservationCreatedEvent.class);

            log.info("Reducing inventory for reservation: {}",
                    event.getConfirmationNumber());

            availabilityService.reduceAvailability(
                    event.getPropertyId(),
                    event.getStayDetails().getRoomTypeId(),
                    event.getStayDetails().getCheckInDate(),
                    event.getStayDetails().getCheckOutDate()
            );

        } catch (Exception e) {
            log.error("Error reducing availability", e);
        }
    }

    /**
     * Restore inventory when reservation cancelled
     */
    @KafkaListener(
        topics = "reservation.cancelled",
        groupId = "availability-calculator-group"
    )
    public void handleReservationCancelled(String message) {
        try {
            ReservationCancelledEvent event = objectMapper.readValue(
                    message, ReservationCancelledEvent.class);

            log.info("Restoring inventory for cancelled reservation: {}",
                    event.getConfirmationNumber());

            availabilityService.restoreAvailability(
                    event.getPropertyId(),
                    event.getRoomTypeId(),
                    event.getCheckInDate(),
                    event.getCheckOutDate()
            );

        } catch (Exception e) {
            log.error("Error restoring availability", e);
        }
    }
}
```

---

### Phase 4: Configuration (All Services)

#### 4.1 Kafka Configuration

**Create:** `KafkaConfig.java` in each service

```java
package com.modernreservation.reservationengine.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    // Producer Configuration
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");  // Wait for all replicas
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);  // Exactly-once semantics
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // Consumer Configuration
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "reservation-engine-group");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);  // Manual commit for reliability
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);  // 3 consumer threads
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }
}
```

#### 4.2 Application YAML Updates

**Update:** `application.yml` in ALL services

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}

    # Producer settings
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      acks: all
      retries: 3
      properties:
        enable.idempotence: true
        max.in.flight.requests.per.connection: 5

    # Consumer settings
    consumer:
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      group-id: ${spring.application.name}-group
      auto-offset-reset: earliest
      enable-auto-commit: false
      max-poll-records: 100
      properties:
        isolation.level: read_committed  # Only read committed messages

    # Admin settings for topic creation
    admin:
      properties:
        bootstrap.servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
```

---

## 🎭 Use Case Scenarios

### Scenario 1: Complete Reservation Flow

```
Step 1: Guest creates reservation via API
  ↓
Reservation Engine:
  - Validates request
  - Saves to database
  - Publishes: reservation.created
  ↓
Parallel Processing:
  ├─> Availability Calculator:
  │     - Consumes reservation.created
  │     - Reduces room inventory
  │     - Publishes: availability.updated
  │
  ├─> Payment Processor:
  │     - Consumes reservation.created
  │     - Initiates payment authorization
  │     - Publishes: payment.authorized (or payment.failed)
  │
  ├─> Rate Management:
  │     - Consumes availability.updated
  │     - Adjusts dynamic pricing
  │     - Publishes: rate.updated
  │
  └─> Analytics Engine:
        - Consumes all events
        - Updates dashboards
        - Calculates metrics
```

### Scenario 2: Reservation Cancellation

```
Guest cancels reservation
  ↓
Reservation Engine:
  - Updates status to CANCELLED
  - Publishes: reservation.cancelled
  ↓
Parallel Processing:
  ├─> Availability Calculator:
  │     - Restores room inventory
  │     - Publishes: availability.updated
  │
  ├─> Payment Processor:
  │     - Initiates refund (if applicable)
  │     - Publishes: payment.refunded
  │
  └─> Analytics Engine:
        - Updates cancellation metrics
        - Tracks cancellation reasons
```

### Scenario 3: Dynamic Pricing Adjustment

```
High demand detected
  ↓
Analytics Engine:
  - Analyzes occupancy trends
  - Publishes: analytics.high-demand
  ↓
Rate Management:
  - Consumes analytics.high-demand
  - Applies surge pricing algorithm
  - Publishes: rate.updated
  ↓
Availability Calculator:
  - Consumes rate.updated
  - Updates current rates
  - Publishes: availability.updated
```

---

## 📦 Maven Dependencies

Add to **ALL** service `pom.xml`:

```xml
<!-- Kafka -->
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>

<!-- JSON Processing -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>

<!-- Testing -->
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka-test</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 🔍 Monitoring & Observability

### Kafka UI Access

After starting Kafka UI container:
- **URL:** http://localhost:8090
- **Features:**
  - View topics and messages
  - Monitor consumer lag
  - Inspect event payloads
  - Track throughput metrics

### Metrics to Monitor

```yaml
Key Metrics:
├── Producer Metrics:
│   ├── Messages sent/sec
│   ├── Average latency
│   ├── Failed sends
│   └── Buffer utilization
│
├── Consumer Metrics:
│   ├── Messages consumed/sec
│   ├── Consumer lag
│   ├── Failed messages
│   └── Rebalance frequency
│
└── Topic Metrics:
    ├── Message count
    ├── Partition distribution
    ├── Retention usage
    └── Throughput (MB/s)
```

---

## 🚀 Migration Checklist

### Week 1: Infrastructure
- [ ] Add Kafka to docker-compose-infrastructure.yml
- [ ] Add Kafka UI for monitoring
- [ ] Update infrastructure scripts (start/stop/check)
- [ ] Test Kafka connectivity
- [ ] Verify topic auto-creation

### Week 2: Shared Library
- [ ] Create event models in shared library
- [ ] Implement EventPublisher utility
- [ ] Create event serialization/deserialization
- [ ] Write unit tests for event models
- [ ] Document event schemas

### Week 3: Producer Implementation
- [ ] Update Reservation Engine to publish events
- [ ] Update Payment Processor to publish events
- [ ] Update Availability Calculator to publish events
- [ ] Update Rate Management to publish events
- [ ] Test event publishing

### Week 4: Consumer Implementation
- [ ] Create event consumers in each service
- [ ] Implement business logic for event handling
- [ ] Add error handling and retries
- [ ] Test event consumption
- [ ] Verify end-to-end flows

### Week 5: Testing & Optimization
- [ ] Integration testing
- [ ] Performance testing
- [ ] Monitor consumer lag
- [ ] Tune partition counts
- [ ] Implement dead letter queues
- [ ] Add distributed tracing

### Week 6: Production Readiness
- [ ] Remove HTTP/REST communication (if any)
- [ ] Update API Gateway routing
- [ ] Configure production Kafka settings
- [ ] Set up monitoring alerts
- [ ] Create runbooks for operations
- [ ] Train team on event-driven patterns

---

## 🎓 Best Practices

### 1. **Event Design**
- ✅ Events are immutable facts (past tense: "created", "updated")
- ✅ Include complete business context in payload
- ✅ Use correlation IDs for tracing
- ✅ Version your events for evolution

### 2. **Consumer Patterns**
- ✅ Idempotent consumers (handle duplicates gracefully)
- ✅ Manual offset commits for reliability
- ✅ Dead letter queues for poison messages
- ✅ Circuit breakers for downstream failures

### 3. **Error Handling**
- ✅ Retry with exponential backoff
- ✅ Log failed events for debugging
- ✅ Alert on consumer lag
- ✅ Monitor event processing time

### 4. **Performance**
- ✅ Batch processing where possible
- ✅ Parallelize with multiple partitions
- ✅ Optimize payload size
- ✅ Use compression for large messages

### 5. **Testing**
- ✅ Use embedded Kafka for integration tests
- ✅ Test consumer idempotency
- ✅ Verify event ordering
- ✅ Test failure scenarios

---

## 📚 Next Steps

1. **Start with Infrastructure**: Get Kafka running in Docker
2. **Create One Event**: Implement `reservation.created` end-to-end
3. **Add Monitoring**: Set up Kafka UI and metrics
4. **Iterate**: Add more events gradually
5. **Optimize**: Tune based on production metrics

---

## 📖 Additional Resources

- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring Kafka Reference](https://spring.io/projects/spring-kafka)
- [Event-Driven Microservices Patterns](https://microservices.io/patterns/data/event-driven-architecture.html)
- [Saga Pattern Implementation](https://microservices.io/patterns/data/saga.html)

---

**Document Version:** 1.0
**Last Updated:** October 6, 2025
**Next Review:** After Phase 1 completion
