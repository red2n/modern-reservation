# Quick Start: Implementing Kafka in Modern Reservation System
## Step-by-Step Implementation Guide

---

## 🎯 Overview

This guide provides a **practical, hands-on approach** to implementing Kafka communication between your Java business services. Follow these steps sequentially.

---

## Phase 1: Setup Kafka Infrastructure (Day 1)

### Step 1: Add Kafka to Docker Compose

**File:** `infrastructure/docker/docker-compose-infrastructure.yml`

Add after the Redis service:

```yaml
  # Apache Kafka with KRaft
  kafka:
    image: bitnami/kafka:3.6
    container_name: modern-reservation-kafka
    ports:
      - "9092:9092"
      - "9094:9094"
    environment:
      - KAFKA_CFG_NODE_ID=1
      - KAFKA_CFG_PROCESS_ROLES=controller,broker
      - KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=1@kafka:9093
      - KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER
      - KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093,EXTERNAL://:9094
      - KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092,EXTERNAL://localhost:9094
      - KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,EXTERNAL:PLAINTEXT
      - KAFKA_CFG_INTER_BROKER_LISTENER_NAME=PLAINTEXT
      - KAFKA_CFG_AUTO_CREATE_TOPICS_ENABLE=true
      - KAFKA_CFG_NUM_PARTITIONS=3
      - KAFKA_CFG_DEFAULT_REPLICATION_FACTOR=1
    volumes:
      - kafka_data:/bitnami/kafka
    networks:
      - modern-reservation-network
    restart: unless-stopped

  # Kafka UI for monitoring
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

# Add to volumes section
volumes:
  kafka_data:
```

### Step 2: Start Kafka

```bash
cd /home/subramani/modern-reservation
bash infra.sh start
```

### Step 3: Verify Kafka is Running

```bash
# Check Docker container
docker ps | grep kafka

# Check Kafka UI
# Open browser: http://localhost:8090
```

### Step 4: Update Infrastructure Scripts

Add Kafka check to `scripts/check-infrastructure.sh`:

```bash
# After Redis check, add:
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

# Update Kafka UI check
if docker ps --filter "name=modern-reservation-kafka-ui" --format "{{.Names}}" | grep -q "modern-reservation-kafka-ui"; then
    print_table_row "kafka-ui" "DOCKER" "8090" "Monitoring ready"
else
    print_table_row "kafka-ui" "STOPPED" "8090" "Container not running"
fi
```

---

## Phase 2: Create Shared Event Library (Day 2-3)

### Step 1: Create Event Models Package

**Location:** `libs/shared/backend-utils/src/main/java/com/modernreservation/shared/`

Create this directory structure:
```
events/
├── BaseEvent.java
├── EventPublisher.java
└── reservation/
    ├── ReservationCreatedEvent.java
    ├── ReservationModifiedEvent.java
    ├── ReservationCancelledEvent.java
    └── ReservationCheckedInEvent.java
```

### Step 2: Create Base Event Class

**File:** `BaseEvent.java`

```java
package com.modernreservation.shared.events;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent {
    private String eventId;
    private String eventType;
    private String eventVersion;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
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

### Step 3: Create Reservation Created Event

**File:** `ReservationCreatedEvent.java`

```java
package com.modernreservation.shared.events.reservation;

import com.modernreservation.shared.events.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ReservationCreatedEvent extends BaseEvent {

    private String reservationId;
    private String confirmationNumber;
    private String propertyId;
    private String guestId;
    private GuestDetails guestDetails;
    private StayDetails stayDetails;
    private PricingDetails pricingDetails;
    private String status;
    private String reservationSource;

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
    }

    @Data
    @NoArgsConstructor
    public static class PricingDetails {
        private BigDecimal roomRate;
        private BigDecimal taxes;
        private BigDecimal fees;
        private BigDecimal totalAmount;
        private String currency;
    }
}
```

### Step 4: Create Event Publisher

**File:** `EventPublisher.java`

```java
package com.modernreservation.shared.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public CompletableFuture<SendResult<String, String>> publish(BaseEvent event) {
        try {
            String topic = event.getEventType();
            String key = event.getEventId();
            String payload = objectMapper.writeValueAsString(event);

            log.info("📤 Publishing event: type={}, id={}",
                    event.getEventType(), event.getEventId());

            return kafkaTemplate.send(topic, key, payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("❌ Failed to publish event: {}", event.getEventType(), ex);
                        } else {
                            log.info("✅ Event published: topic={}, partition={}, offset={}",
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        }
                    });
        } catch (Exception e) {
            log.error("❌ Error serializing event", e);
            return CompletableFuture.failedFuture(e);
        }
    }
}
```

### Step 5: Build Shared Library

```bash
cd /home/subramani/modern-reservation/libs/shared/backend-utils
mvn clean install
```

---

## Phase 3: Update Service Dependencies (Day 4)

### Step 1: Add Kafka Dependencies

Add to **ALL** service `pom.xml` files:

```xml
<!-- Spring Kafka -->
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>

<!-- Shared Events Library -->
<dependency>
    <groupId>com.modernreservation</groupId>
    <artifactId>backend-utils</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- Jackson for JSON -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>
```

### Step 2: Update Application YAML

Add to **ALL** service `application.yml`:

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}

    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      acks: all
      retries: 3

    consumer:
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      group-id: ${spring.application.name}-group
      auto-offset-reset: earliest
      enable-auto-commit: false
```

---

## Phase 4: Implement Producer (Reservation Engine) (Day 5)

### Step 1: Create Kafka Configuration

**File:** `reservation-engine/src/main/java/com/modernreservation/reservationengine/config/KafkaConfig.java`

```java
package com.modernreservation.reservationengine.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
public class KafkaConfig {
    // Configuration is in application.yml
    // This class just enables Kafka
}
```

### Step 2: Update Reservation Service

**File:** `ReservationService.java`

Add these changes:

```java
import com.modernreservation.shared.events.EventPublisher;
import com.modernreservation.shared.events.reservation.ReservationCreatedEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final EventPublisher eventPublisher;  // ADD THIS

    @CacheEvict(value = "reservations", allEntries = true)
    @Transactional
    public ReservationResponseDTO createReservation(ReservationRequestDTO request) {
        log.info("Creating new reservation for guest: {} {}",
                request.guestFirstName(), request.guestLastName());

        // Existing logic to create reservation
        Reservation reservation = buildReservation(request);
        Reservation saved = reservationRepository.save(reservation);

        // NEW: Publish event
        publishReservationCreatedEvent(saved);

        return toResponseDTO(saved);
    }

    // NEW METHOD
    private void publishReservationCreatedEvent(Reservation reservation) {
        ReservationCreatedEvent event = new ReservationCreatedEvent("reservation-engine");

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
        event.setReservationSource(reservation.getSource().name());

        // Publish async
        eventPublisher.publish(event);

        log.info("📤 Published reservation.created event for {}",
                reservation.getConfirmationNumber());
    }
}
```

---

## Phase 5: Implement Consumers (Day 6-7)

### Payment Processor Consumer

**File:** `payment-processor/src/main/java/com/modernreservation/paymentprocessor/consumer/PaymentEventConsumer.java`

```java
package com.modernreservation.paymentprocessor.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modernreservation.shared.events.reservation.ReservationCreatedEvent;
import com.modernreservation.paymentprocessor.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics = "reservation.created",
        groupId = "payment-processor-group"
    )
    public void handleReservationCreated(@Payload String message, Acknowledgment ack) {
        try {
            log.info("📥 Received reservation.created event");

            ReservationCreatedEvent event = objectMapper.readValue(
                    message, ReservationCreatedEvent.class);

            log.info("Processing payment for reservation: {}",
                    event.getConfirmationNumber());

            // TODO: Implement payment logic
            // paymentService.initiatePaymentForReservation(event);

            ack.acknowledge();  // Manual commit
            log.info("✅ Payment processing completed");

        } catch (Exception e) {
            log.error("❌ Error processing reservation.created event", e);
            // Don't acknowledge - message will be retried
        }
    }
}
```

### Availability Calculator Consumer

**File:** `availability-calculator/src/main/java/com/modernreservation/availabilitycalculator/consumer/AvailabilityEventConsumer.java`

```java
package com.modernreservation.availabilitycalculator.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modernreservation.shared.events.reservation.ReservationCreatedEvent;
import com.modernreservation.availabilitycalculator.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AvailabilityEventConsumer {

    private final AvailabilityService availabilityService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics = "reservation.created",
        groupId = "availability-calculator-group"
    )
    public void handleReservationCreated(@Payload String message, Acknowledgment ack) {
        try {
            log.info("📥 Received reservation.created event");

            ReservationCreatedEvent event = objectMapper.readValue(
                    message, ReservationCreatedEvent.class);

            log.info("Reducing inventory for reservation: {}",
                    event.getConfirmationNumber());

            // Reduce availability
            availabilityService.reduceAvailability(
                    UUID.fromString(event.getPropertyId()),
                    UUID.fromString(event.getStayDetails().getRoomTypeId()),
                    event.getStayDetails().getCheckInDate(),
                    event.getStayDetails().getCheckOutDate()
            );

            ack.acknowledge();
            log.info("✅ Inventory reduced successfully");

        } catch (Exception e) {
            log.error("❌ Error reducing inventory", e);
        }
    }
}
```

---

## Phase 6: Testing (Day 8)

### Test 1: Check Kafka UI

1. Open http://localhost:8090
2. Verify topics are created
3. Check messages are being published

### Test 2: Create a Reservation

```bash
curl -X POST http://localhost:8080/reservation-engine/api/v1/reservations \
  -H "Content-Type: application/json" \
  -d '{
    "propertyId": "550e8400-e29b-41d4-a716-446655440000",
    "guestId": "650e8400-e29b-41d4-a716-446655440000",
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

### Test 3: Check Logs

```bash
# Reservation Engine - Should show event published
docker logs reservation-engine-container 2>&1 | grep "Publishing event"

# Payment Processor - Should show event received
docker logs payment-processor-container 2>&1 | grep "Received reservation.created"

# Availability Calculator - Should show event received
docker logs availability-calculator-container 2>&1 | grep "Received reservation.created"
```

### Test 4: Verify in Kafka UI

1. Go to Topics → reservation.created
2. Click "Messages"
3. See the published event with full payload

---

## 📋 Implementation Checklist

### Infrastructure
- [ ] Kafka added to docker-compose
- [ ] Kafka UI added for monitoring
- [ ] Containers started and healthy
- [ ] Access Kafka UI at http://localhost:8090

### Shared Library
- [ ] BaseEvent class created
- [ ] ReservationCreatedEvent created
- [ ] EventPublisher created
- [ ] Shared library built: `mvn clean install`

### Service Updates
- [ ] Kafka dependencies added to all services
- [ ] application.yml updated with Kafka config
- [ ] KafkaConfig class created in each service

### Producer (Reservation Engine)
- [ ] EventPublisher injected
- [ ] publishReservationCreatedEvent method added
- [ ] Event publishing after reservation save
- [ ] Logs show event published

### Consumers
- [ ] Payment Processor consumer created
- [ ] Availability Calculator consumer created
- [ ] Analytics Engine consumer created (optional)
- [ ] Consumers listening to topics

### Testing
- [ ] Create test reservation via API
- [ ] Check Kafka UI for messages
- [ ] Verify logs show event flow
- [ ] Verify database updates in consumers

---

## 🔧 Common Issues & Solutions

### Issue 1: Kafka Container Won't Start
```bash
# Check logs
docker logs modern-reservation-kafka

# Solution: Remove volume and restart
docker-compose down -v
docker-compose up -d
```

### Issue 2: Events Not Being Received
```bash
# Check consumer group
docker exec modern-reservation-kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 --list

# Check topic exists
docker exec modern-reservation-kafka kafka-topics.sh \
  --bootstrap-server localhost:9092 --list
```

### Issue 3: Serialization Errors
- Check Jackson dependencies in pom.xml
- Verify ObjectMapper bean configuration
- Check event class has no-arg constructor

---

## 📊 Monitoring Commands

```bash
# List topics
docker exec modern-reservation-kafka kafka-topics.sh \
  --bootstrap-server localhost:9092 --list

# Describe topic
docker exec modern-reservation-kafka kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --describe --topic reservation.created

# Check consumer groups
docker exec modern-reservation-kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 --list

# Check consumer lag
docker exec modern-reservation-kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --describe --group payment-processor-group
```

---

## 🎓 Next Steps After Implementation

1. **Add More Events**: Implement `reservation.cancelled`, `payment.authorized`
2. **Error Handling**: Add dead letter queue for failed messages
3. **Monitoring**: Set up Prometheus metrics
4. **Performance**: Tune partition counts based on load
5. **Documentation**: Update API docs with async flows

---

**Quick Start Version:** 1.0
**Last Updated:** October 6, 2025
