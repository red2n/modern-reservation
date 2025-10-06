# Kafka Implementation Plan for Modern Reservation System
## Phase-by-Phase Execution Plan for Reservation Engine

**Date Created:** October 6, 2025
**Target Service:** Reservation Engine (Entry Point)
**Estimated Timeline:** 5 Days
**Status:** Ready to Execute

---

## 📋 Executive Summary

This plan outlines the **step-by-step implementation** of Kafka event-driven architecture for the Reservation Engine service, which will serve as the **main entry point** and primary event producer for the system.

### Implementation Order
1. ✅ **Phase 0**: Infrastructure Setup (Kafka + Kafka UI in Docker)
2. ✅ **Phase 1**: Shared Event Library Creation
3. ✅ **Phase 2**: Reservation Engine Producer Implementation
4. ✅ **Phase 3**: Testing & Verification
5. ✅ **Phase 4**: Documentation & Handoff

---

## 🎯 Why Reservation Engine First?

### Strategic Reasons
1. **Entry Point**: Receives external requests from API Gateway
2. **Event Producer**: Publishes events that other services consume
3. **Business Critical**: Core booking functionality
4. **Clear Boundaries**: Well-defined domain with clear events
5. **High Impact**: Most other services depend on reservation events

### Event Flow
```
Guest Request → API Gateway → Reservation Engine (Producer)
                                        ↓
                                  Publishes Events
                                        ↓
                                    Kafka Topics
                                        ↓
                    ┌───────────────────┼───────────────────┐
                    ↓                   ↓                   ↓
            Availability            Payment             Analytics
            (Consumer)            (Consumer)            (Consumer)
```

---

## 📅 Implementation Timeline

```
Day 1: Infrastructure Setup (Phase 0)
├─ Morning (2-3 hours)
│  ├─ Add Kafka to docker-compose
│  ├─ Add Kafka UI
│  └─ Test connectivity
└─ Afternoon (2-3 hours)
   ├─ Update infrastructure scripts
   └─ Verify all services

Day 2: Shared Event Library (Phase 1)
├─ Morning (3-4 hours)
│  ├─ Create event package structure
│  ├─ BaseEvent class
│  └─ EventPublisher utility
└─ Afternoon (2-3 hours)
   ├─ ReservationCreatedEvent
   ├─ Build shared library
   └─ Unit tests

Day 3: Reservation Engine - Setup (Phase 2.1)
├─ Morning (2-3 hours)
│  ├─ Add Kafka dependencies
│  ├─ Update application.yml
│  └─ Create KafkaConfig
└─ Afternoon (3-4 hours)
   ├─ Inject EventPublisher
   ├─ Create event mapping methods
   └─ Test compilation

Day 4: Reservation Engine - Implementation (Phase 2.2)
├─ Morning (3-4 hours)
│  ├─ Implement publishReservationCreatedEvent
│  ├─ Implement publishReservationModifiedEvent
│  └─ Implement publishReservationCancelledEvent
└─ Afternoon (2-3 hours)
   ├─ Integration with service methods
   ├─ Error handling
   └─ Logging

Day 5: Testing & Verification (Phase 3)
├─ Morning (3-4 hours)
│  ├─ Unit tests
│  ├─ Integration tests
│  └─ End-to-end tests
└─ Afternoon (2-3 hours)
   ├─ Kafka UI verification
   ├─ Performance testing
   └─ Documentation
```

---

## 🚀 PHASE 0: Infrastructure Setup

### Objective
Set up Kafka and Kafka UI in Docker infrastructure, ensuring they're ready for service integration.

### Tasks

#### Task 0.1: Add Kafka to Docker Compose
**File:** `infrastructure/docker/docker-compose-infrastructure.yml`

**Action:** Add Kafka service after Redis section

```yaml
  # Apache Kafka with KRaft (No Zookeeper)
  kafka:
    image: bitnami/kafka:3.6
    container_name: modern-reservation-kafka
    ports:
      - "9092:9092"      # Internal access
      - "9094:9094"      # External access
    environment:
      # KRaft settings
      - KAFKA_CFG_NODE_ID=1
      - KAFKA_CFG_PROCESS_ROLES=controller,broker
      - KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=1@kafka:9093
      - KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER

      # Listeners
      - KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093,EXTERNAL://:9094
      - KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092,EXTERNAL://localhost:9094
      - KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,EXTERNAL:PLAINTEXT
      - KAFKA_CFG_INTER_BROKER_LISTENER_NAME=PLAINTEXT

      # Topic settings
      - KAFKA_CFG_AUTO_CREATE_TOPICS_ENABLE=true
      - KAFKA_CFG_NUM_PARTITIONS=3
      - KAFKA_CFG_DEFAULT_REPLICATION_FACTOR=1

      # Retention
      - KAFKA_CFG_LOG_RETENTION_HOURS=168  # 7 days
      - KAFKA_CFG_LOG_SEGMENT_BYTES=1073741824  # 1GB

      # Performance
      - KAFKA_CFG_COMPRESSION_TYPE=snappy
      - KAFKA_HEAP_OPTS=-Xmx1G -Xms1G
    volumes:
      - kafka_data:/bitnami/kafka
    networks:
      - modern-reservation-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD-SHELL", "kafka-broker-api-versions.sh --bootstrap-server localhost:9092 || exit 1"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s

  # Kafka UI for Monitoring
  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    container_name: modern-reservation-kafka-ui
    ports:
      - "8090:8080"
    environment:
      - KAFKA_CLUSTERS_0_NAME=modern-reservation
      - KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS=kafka:9092
      - KAFKA_CLUSTERS_0_METRICS_PORT=9092
      - DYNAMIC_CONFIG_ENABLED=true
      - LOGGING_LEVEL_ROOT=INFO
      - LOGGING_LEVEL_COM_PROVECTUS=INFO
    depends_on:
      kafka:
        condition: service_healthy
    networks:
      - modern-reservation-network
    restart: unless-stopped

# Add to volumes section
volumes:
  kafka_data:
```

**Verification:**
```bash
# Start infrastructure
cd /home/subramani/modern-reservation
bash infra.sh start

# Check Kafka container
docker ps | grep kafka

# Check Kafka UI
# Browser: http://localhost:8090
```

#### Task 0.2: Update Infrastructure Scripts

**File:** `scripts/check-infrastructure.sh`

**Location:** After Redis check (around line 300)

**Add:**
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

    # Check Kafka UI
    if docker ps --filter "name=modern-reservation-kafka-ui" --format "{{.Names}}" | grep -q "modern-reservation-kafka-ui"; then
        print_table_row "kafka-ui" "DOCKER" "8090" "Monitoring ready"
    else
        print_table_row "kafka-ui" "STOPPED" "8090" "Container not running"
    fi
```

**Update service count:**
Change `docker_services=4` to `docker_services=6`

**File:** `scripts/stop-infrastructure.sh`

**Update cleanup list (around line 168):**
```bash
local containers=("modern-reservation-postgres" "modern-reservation-redis" "modern-reservation-kafka" "modern-reservation-kafka-ui" "modern-reservation-kafka" "modern-reservation-zookeeper")
```

#### Task 0.3: Test Kafka Infrastructure

**Commands:**
```bash
# 1. Start all infrastructure
bash infra.sh start

# 2. Check status
bash infra.sh status

# Expected output should show:
# kafka        DOCKER     9092        Broker ready
# kafka-ui     DOCKER     8090        Monitoring ready

# 3. Access Kafka UI
# Browser: http://localhost:8090

# 4. Test topic creation
docker exec modern-reservation-kafka kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --create --topic test-topic \
  --partitions 3 \
  --replication-factor 1

# 5. List topics
docker exec modern-reservation-kafka kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --list

# 6. Delete test topic
docker exec modern-reservation-kafka kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --delete --topic test-topic
```

**Success Criteria:**
- ✅ Kafka container running and healthy
- ✅ Kafka UI accessible at http://localhost:8090
- ✅ Can create/list/delete topics
- ✅ No errors in docker logs

---

## 🔧 PHASE 1: Shared Event Library

### Objective
Create reusable event models and utilities in the shared backend-utils library.

### Directory Structure
```
libs/shared/backend-utils/src/main/java/com/modernreservation/shared/
├── events/
│   ├── BaseEvent.java                    # Abstract base class
│   ├── EventPublisher.java               # Publishing utility
│   └── reservation/
│       ├── ReservationCreatedEvent.java
│       ├── ReservationModifiedEvent.java
│       ├── ReservationCancelledEvent.java
│       ├── ReservationCheckedInEvent.java
│       └── ReservationCheckedOutEvent.java
```

### Task 1.1: Create BaseEvent Class

**File:** `libs/shared/backend-utils/src/main/java/com/modernreservation/shared/events/BaseEvent.java`

```java
package com.modernreservation.shared.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Base Event Class
 *
 * All domain events should extend this class to ensure
 * consistent event structure across the system.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseEvent {

    /**
     * Unique identifier for this event instance
     */
    private String eventId;

    /**
     * Type of event (e.g., "reservation.created")
     */
    private String eventType;

    /**
     * Event schema version for evolution compatibility
     */
    private String eventVersion;

    /**
     * When the event occurred
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant timestamp;

    /**
     * Source service that published the event
     */
    private String source;

    /**
     * Correlation ID for distributed tracing
     */
    private String correlationId;

    /**
     * ID of the event that caused this event (causality tracking)
     */
    private String causationId;

    /**
     * Constructor for derived classes
     */
    protected BaseEvent(String eventType, String source) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.eventVersion = "1.0";
        this.timestamp = Instant.now();
        this.source = source;
    }
}
```

### Task 1.2: Create EventPublisher Utility

**File:** `libs/shared/backend-utils/src/main/java/com/modernreservation/shared/events/EventPublisher.java`

```java
package com.modernreservation.shared.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Event Publisher
 *
 * Centralized utility for publishing domain events to Kafka.
 * Handles serialization, logging, and error handling.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Publish event to Kafka
     * Topic is derived from event type (e.g., reservation.created)
     *
     * @param event The event to publish
     * @return CompletableFuture with send result
     */
    public CompletableFuture<SendResult<String, String>> publish(BaseEvent event) {
        try {
            String topic = event.getEventType();
            String key = event.getEventId();
            String payload = objectMapper.writeValueAsString(event);

            log.info("📤 Publishing event: type={}, id={}, correlationId={}",
                    event.getEventType(),
                    event.getEventId(),
                    event.getCorrelationId());

            log.debug("Event payload: {}", payload);

            return kafkaTemplate.send(topic, key, payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("❌ Failed to publish event: type={}, id={}",
                                    event.getEventType(),
                                    event.getEventId(),
                                    ex);
                        } else {
                            log.info("✅ Event published successfully: topic={}, partition={}, offset={}",
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        }
                    });
        } catch (Exception e) {
            log.error("❌ Error serializing event: type={}, id={}",
                    event.getEventType(),
                    event.getEventId(),
                    e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Publish event to a specific topic (override default)
     *
     * @param topic Custom topic name
     * @param event The event to publish
     * @return CompletableFuture with send result
     */
    public CompletableFuture<SendResult<String, String>> publishToTopic(String topic, BaseEvent event) {
        try {
            String key = event.getEventId();
            String payload = objectMapper.writeValueAsString(event);

            log.info("📤 Publishing event to custom topic: topic={}, type={}, id={}",
                    topic,
                    event.getEventType(),
                    event.getEventId());

            return kafkaTemplate.send(topic, key, payload);
        } catch (Exception e) {
            log.error("❌ Error publishing to custom topic: topic={}", topic, e);
            return CompletableFuture.failedFuture(e);
        }
    }
}
```

### Task 1.3: Create ReservationCreatedEvent

**File:** `libs/shared/backend-utils/src/main/java/com/modernreservation/shared/events/reservation/ReservationCreatedEvent.java`

```java
package com.modernreservation.shared.events.reservation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.modernreservation.shared.events.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Reservation Created Event
 *
 * Published when a new reservation is successfully created in the system.
 * Consumed by: Payment Processor, Availability Calculator, Rate Management, Analytics Engine
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReservationCreatedEvent extends BaseEvent {

    // Aggregate identifiers
    private String reservationId;
    private String confirmationNumber;
    private String propertyId;
    private String guestId;

    // Nested details
    private GuestDetails guestDetails;
    private StayDetails stayDetails;
    private PricingDetails pricingDetails;

    // Status
    private String status;
    private String reservationSource;
    private String specialRequests;

    public ReservationCreatedEvent(String source) {
        super("reservation.created", source);
    }

    @Data
    @NoArgsConstructor
    public static class GuestDetails {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String country;
    }

    @Data
    @NoArgsConstructor
    public static class StayDetails {
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        private Integer nights;
        private String roomTypeId;
        private Integer adults;
        private Integer children;
        private Integer infants;
        private String arrivalTime;
        private String departureTime;
    }

    @Data
    @NoArgsConstructor
    public static class PricingDetails {
        private BigDecimal roomRate;
        private BigDecimal taxes;
        private BigDecimal fees;
        private BigDecimal totalAmount;
        private String currency;
        private BigDecimal depositAmount;
        private LocalDate depositDueDate;
    }
}
```

### Task 1.4: Build Shared Library

```bash
cd /home/subramani/modern-reservation/libs/shared/backend-utils

# Clean and build
mvn clean install -DskipTests

# Verify build
ls -lh target/*.jar
```

**Success Criteria:**
- ✅ Clean build with no errors
- ✅ JAR file created in target/
- ✅ Classes compiled successfully

---

## 🎯 PHASE 2: Reservation Engine Implementation

### Objective
Integrate Kafka event publishing into Reservation Engine service methods.

### Task 2.1: Add Kafka Dependencies

**File:** `apps/backend/java-services/business-services/reservation-engine/pom.xml`

**Add after existing Kafka dependency:**
```xml
<!-- Kafka already exists, ensure these are present -->

<!-- JSON Processing -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>

<!-- Shared Events Library -->
<dependency>
    <groupId>com.modernreservation</groupId>
    <artifactId>backend-utils</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Task 2.2: Update application.yml

**File:** `apps/backend/java-services/business-services/reservation-engine/src/main/resources/application.yml`

**Add after Redis configuration:**
```yaml
# Kafka Configuration
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}

    # Producer settings
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      acks: all  # Wait for all replicas to acknowledge
      retries: 3
      properties:
        enable.idempotence: true  # Exactly-once semantics
        max.in.flight.requests.per.connection: 5
        compression.type: snappy  # Compression for efficiency
        linger.ms: 10  # Batch messages for better throughput
        batch.size: 16384

    # Admin settings
    admin:
      properties:
        bootstrap.servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
```

### Task 2.3: Create Kafka Configuration Class

**File:** `apps/backend/java-services/business-services/reservation-engine/src/main/java/com/modernreservation/reservationengine/config/KafkaConfig.java`

```java
package com.modernreservation.reservationengine.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Kafka Configuration
 *
 * Enables Kafka functionality and configures ObjectMapper for event serialization.
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    /**
     * ObjectMapper for event serialization
     * Configured to handle Java 8 date/time types
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
```

### Task 2.4: Update ReservationService

**File:** `apps/backend/java-services/business-services/reservation-engine/src/main/java/com/modernreservation/reservationengine/service/ReservationService.java`

**Add imports:**
```java
import com.modernreservation.shared.events.EventPublisher;
import com.modernreservation.shared.events.reservation.ReservationCreatedEvent;
```

**Inject EventPublisher:**
```java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationAuditRepository auditRepository;
    private final EventPublisher eventPublisher;  // ADD THIS
    // ... existing code
}
```

**Update createReservation method:**
```java
@CacheEvict(value = "reservations", allEntries = true)
public ReservationResponseDTO createReservation(ReservationRequestDTO request) {
    log.info("Creating new reservation for guest: {} {}",
            request.guestFirstName(), request.guestLastName());

    // Existing reservation creation logic...
    Reservation saved = reservationRepository.save(reservation);

    // Audit trail
    createAuditRecord(saved, "CREATED", "Reservation created", "SYSTEM");

    // NEW: Publish event to Kafka
    publishReservationCreatedEvent(saved);

    log.info("Reservation created successfully: {}", saved.getConfirmationNumber());
    return toResponseDTO(saved);
}
```

**Add event publishing method:**
```java
/**
 * Publish reservation.created event to Kafka
 */
private void publishReservationCreatedEvent(Reservation reservation) {
    try {
        ReservationCreatedEvent event = new ReservationCreatedEvent("reservation-engine");

        // Set aggregate IDs
        event.setReservationId(reservation.getId().toString());
        event.setConfirmationNumber(reservation.getConfirmationNumber());
        event.setPropertyId(reservation.getPropertyId().toString());
        event.setGuestId(reservation.getGuestId() != null ? reservation.getGuestId().toString() : null);

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
        stayDetails.setInfants(reservation.getInfants());
        stayDetails.setArrivalTime(reservation.getArrivalTime());
        stayDetails.setDepartureTime(reservation.getDepartureTime());
        event.setStayDetails(stayDetails);

        // Pricing details
        ReservationCreatedEvent.PricingDetails pricingDetails = new ReservationCreatedEvent.PricingDetails();
        pricingDetails.setRoomRate(reservation.getRoomRate());
        pricingDetails.setTaxes(reservation.getTaxes());
        pricingDetails.setFees(reservation.getFees());
        pricingDetails.setTotalAmount(reservation.getTotalAmount());
        pricingDetails.setCurrency(reservation.getCurrency());
        pricingDetails.setDepositAmount(reservation.getDepositAmount());
        pricingDetails.setDepositDueDate(reservation.getDepositDueDate());
        event.setPricingDetails(pricingDetails);

        // Status and metadata
        event.setStatus(reservation.getStatus().name());
        event.setReservationSource(reservation.getSource().name());
        event.setSpecialRequests(reservation.getSpecialRequests());

        // Publish asynchronously
        eventPublisher.publish(event);

        log.info("📤 Published reservation.created event for confirmation: {}",
                reservation.getConfirmationNumber());

    } catch (Exception e) {
        // Log error but don't fail the reservation creation
        log.error("❌ Failed to publish reservation.created event for confirmation: {}",
                reservation.getConfirmationNumber(), e);
    }
}
```

---

## ✅ PHASE 3: Testing & Verification

### Test 3.1: Unit Tests

**File:** Create `ReservationServiceKafkaTest.java`

```java
// Test that events are published correctly
@Test
void shouldPublishEventWhenReservationCreated() {
    // Arrange
    ReservationRequestDTO request = createTestRequest();

    // Act
    ReservationResponseDTO response = reservationService.createReservation(request);

    // Assert
    verify(eventPublisher, times(1)).publish(any(ReservationCreatedEvent.class));
}
```

### Test 3.2: Integration Test

**Create a test reservation:**
```bash
curl -X POST http://localhost:8080/reservation-engine/api/v1/reservations \
  -H "Content-Type: application/json" \
  -d '{
    "propertyId": "550e8400-e29b-41d4-a716-446655440000",
    "guestFirstName": "John",
    "guestLastName": "Doe",
    "guestEmail": "john.doe@example.com",
    "guestPhone": "+1234567890",
    "checkInDate": "2025-11-01",
    "checkOutDate": "2025-11-05",
    "roomTypeId": "750e8400-e29b-41d4-a716-446655440000",
    "adults": 2,
    "children": 1,
    "roomRate": 150.00,
    "taxes": 18.00,
    "fees": 7.50,
    "currency": "USD",
    "source": "DIRECT"
  }'
```

### Test 3.3: Kafka UI Verification

1. Open http://localhost:8090
2. Go to Topics
3. Find `reservation.created`
4. View messages
5. Verify payload structure

### Test 3.4: Log Verification

```bash
# Check Reservation Engine logs
tail -f logs/reservation-engine.log | grep "Publishing event"

# Should see:
# 📤 Publishing event: type=reservation.created, id=...
# ✅ Event published successfully: topic=reservation.created, partition=0, offset=0
```

---

## 📊 Success Criteria

### Phase 0 (Infrastructure)
- [ ] Kafka container running and healthy
- [ ] Kafka UI accessible at http://localhost:8090
- [ ] Can create/list topics via CLI
- [ ] Infrastructure scripts updated

### Phase 1 (Shared Library)
- [ ] BaseEvent class created
- [ ] EventPublisher utility created
- [ ] ReservationCreatedEvent created
- [ ] Shared library builds successfully
- [ ] JAR installed in local Maven repo

### Phase 2 (Reservation Engine)
- [ ] Kafka dependencies added
- [ ] application.yml updated
- [ ] KafkaConfig created
- [ ] EventPublisher injected
- [ ] Event publishing method implemented
- [ ] Service compiles without errors

### Phase 3 (Testing)
- [ ] Unit tests pass
- [ ] Can create reservation via API
- [ ] Event visible in Kafka UI
- [ ] Logs show successful publishing
- [ ] Event payload matches schema

---

## 🔍 Troubleshooting Guide

### Issue: Kafka container won't start
```bash
# Check logs
docker logs modern-reservation-kafka

# Solution: Remove volumes and restart
docker-compose down -v
docker-compose up -d
```

### Issue: Events not published
```bash
# Check Kafka connection
docker exec modern-reservation-kafka kafka-broker-api-versions.sh \
  --bootstrap-server localhost:9092

# Check service logs
tail -f logs/reservation-engine.log | grep -i kafka
```

### Issue: Serialization errors
- Verify Jackson dependencies
- Check ObjectMapper configuration
- Ensure event classes have no-arg constructors

---

## 📝 Next Steps (After Phase 3)

1. **Implement More Events**:
   - reservation.modified
   - reservation.cancelled
   - reservation.checkedin
   - reservation.checkedout

2. **Create Consumers**:
   - Payment Processor
   - Availability Calculator
   - Analytics Engine

3. **Add Error Handling**:
   - Dead letter queues
   - Retry logic
   - Circuit breakers

4. **Performance Tuning**:
   - Partition strategy
   - Consumer parallelism
   - Monitoring and alerting

---

**Document Version:** 1.0
**Last Updated:** October 6, 2025
**Status:** ✅ Ready to Execute
