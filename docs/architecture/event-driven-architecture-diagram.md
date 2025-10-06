# Event-Driven Architecture - Visual Diagrams
## Modern Reservation System

---

## 🏗️ High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            API Gateway (8080)                                │
│                     Spring Cloud Gateway / GraphQL                           │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      │ HTTP Requests
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         Business Services Layer                              │
├────────────────┬────────────────┬────────────────┬────────────────┬─────────┤
│  Reservation   │  Availability  │      Rate      │    Payment     │Analytics│
│    Engine      │   Calculator   │  Management    │   Processor    │ Engine  │
│   (8081)       │    (8082)      │    (8083)      │    (8084)      │ (8086)  │
│                │                │                │                │         │
│  - Create      │  - Check       │  - Dynamic     │  - Authorize   │ - KPIs  │
│  - Update      │  - Update      │    Pricing     │  - Capture     │ - Reports│
│  - Cancel      │  - Block       │  - Apply       │  - Refund      │ - Trends│
│  - Status      │  - Release     │  - History     │  - Settlement  │ - Alerts│
└────────┬───────┴────────┬───────┴────────┬───────┴────────┬───────┴─────────┘
         │                │                │                │
         │                │                │                │
         │ Publish Events │ Publish Events │ Publish Events │ Publish Events
         └────────────────┴────────────────┴────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                     Apache Kafka Event Streaming Platform                   │
│                            (Port 9092, 9094)                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│  Topics:                                                                     │
│  ├─ reservation.created      ├─ payment.authorized    ├─ analytics.*       │
│  ├─ reservation.confirmed    ├─ payment.captured      ├─ rate.updated      │
│  ├─ reservation.modified     ├─ payment.failed        ├─ rate.applied      │
│  ├─ reservation.cancelled    ├─ payment.refunded      └─ availability.*    │
│  ├─ reservation.checkin      ├─ payment.settled                            │
│  └─ reservation.checkout     └─ payment.initiated                          │
│                                                                              │
│  Partitions: 3-10 per topic  |  Replication Factor: 3 (production)         │
│  Retention: 7 days           |  Consumer Groups: Per service               │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      │ Consume Events
         ┌────────────────┬───────────┴───────────┬────────────────┐
         │                │                       │                │
         ▼                ▼                       ▼                ▼
┌────────────────┐ ┌────────────────┐  ┌────────────────┐ ┌──────────────┐
│  Each Service  │ │  Each Service  │  │  Each Service  │ │  Analytics   │
│   Consumes     │ │   Consumes     │  │   Consumes     │ │   Engine     │
│   Events       │ │   Events       │  │   Events       │ │  Consumes    │
│   Relevant     │ │   Relevant     │  │   Relevant     │ │     ALL      │
│   To Its       │ │   To Its       │  │   To Its       │ │   Events     │
│   Domain       │ │   Domain       │  │   Domain       │ │              │
└────────┬───────┘ └────────┬───────┘  └────────┬───────┘ └──────┬───────┘
         │                  │                    │                │
         ▼                  ▼                    ▼                ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                      PostgreSQL Database (5432)                              │
│                        Multi-Master Cluster                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│  Each Service has its own schema:                                           │
│  ├─ reservations schema    ├─ payments schema    ├─ analytics schema       │
│  ├─ availability schema    ├─ rates schema       └─ shared reference data  │
│                                                                              │
│  Pattern: Database per Service (Microservices best practice)               │
└─────────────────────────────────────────────────────────────────────────────┘
         │                  │                    │                │
         ▼                  ▼                    ▼                ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         Redis Cache (6379)                                   │
│                     21-node Cluster (Production)                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔄 Event Flow: Complete Reservation Journey

```
┌──────────────┐
│   Guest      │
│  (Client)    │
└──────┬───────┘
       │ HTTP POST /api/reservations
       ▼
┌──────────────────────────────────────────────────────────────────┐
│                   API Gateway (8080)                             │
│  - Authentication                                                │
│  - Rate Limiting                                                 │
│  - Routing                                                       │
└──────────────────────────┬───────────────────────────────────────┘
                           │
                           │ Route to Reservation Engine
                           ▼
┌──────────────────────────────────────────────────────────────────┐
│              Reservation Engine (8081)                           │
│  ┌────────────────────────────────────────────────────┐         │
│  │ 1. Validate Request                                 │         │
│  │ 2. Check Business Rules                             │         │
│  │ 3. Generate Confirmation Number                     │         │
│  │ 4. Save to Database (reservations schema)           │         │
│  │ 5. Publish Event: reservation.created               │  ───┐   │
│  └────────────────────────────────────────────────────┘     │   │
│                                                              │   │
│  ✅ Returns: 201 Created with confirmation number          │   │
└──────────────────────────────────────────────────────────────┼───┘
                                                               │
       ┌───────────────────────────────────────────────────────┘
       │
       │ Event Published to Kafka
       ▼
┌──────────────────────────────────────────────────────────────────┐
│                     Kafka Topic: reservation.created             │
│  ┌────────────────────────────────────────────────────┐         │
│  │ Event Payload:                                      │         │
│  │ {                                                   │         │
│  │   "eventId": "uuid",                                │         │
│  │   "eventType": "reservation.created",               │         │
│  │   "confirmationNumber": "RSV-2025-001234",          │         │
│  │   "propertyId": "PROP-001",                         │         │
│  │   "guestDetails": {...},                            │         │
│  │   "stayDetails": {...},                             │         │
│  │   "pricingDetails": {...}                           │         │
│  │ }                                                   │         │
│  └────────────────────────────────────────────────────┘         │
└────┬──────────────┬──────────────┬──────────────┬───────────────┘
     │              │              │              │
     │ Consume      │ Consume      │ Consume      │ Consume
     │              │              │              │
     ▼              ▼              ▼              ▼

┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│ Availability │ │   Payment    │ │     Rate     │ │  Analytics   │
│ Calculator   │ │  Processor   │ │  Management  │ │   Engine     │
│   (8082)     │ │    (8084)    │ │    (8083)    │ │   (8086)     │
└──────┬───────┘ └──────┬───────┘ └──────┬───────┘ └──────┬───────┘
       │                │                │                │
       │ Process        │ Process        │ Process        │ Process
       │ Event          │ Event          │ Event          │ Event
       ▼                ▼                ▼                ▼

┌──────────────────────────────────────────────────────────────────┐
│ PARALLEL PROCESSING (All services run simultaneously)           │
├──────────────────────────────────────────────────────────────────┤
│                                                                   │
│ ┌────────────────────────────────────────────────────┐          │
│ │ Availability Calculator:                            │          │
│ │ 1. Reduce room inventory                            │          │
│ │ 2. Update availability_calendar table               │          │
│ │ 3. Check for overbooking                            │          │
│ │ 4. Publish: availability.updated                    │ ───┐     │
│ └────────────────────────────────────────────────────┘    │     │
│                                                            │     │
│ ┌────────────────────────────────────────────────────┐    │     │
│ │ Payment Processor:                                  │    │     │
│ │ 1. Create payment record                            │    │     │
│ │ 2. Call payment gateway API                         │    │     │
│ │ 3. Authorize payment                                │    │     │
│ │ 4. Store payment token securely                     │    │     │
│ │ 5. Publish: payment.authorized                      │ ───┼─┐   │
│ └────────────────────────────────────────────────────┘    │ │   │
│                                                            │ │   │
│ ┌────────────────────────────────────────────────────┐    │ │   │
│ │ Rate Management:                                    │    │ │   │
│ │ 1. Record rate application                          │    │ │   │
│ │ 2. Update rate usage statistics                     │    │ │   │
│ │ 3. Trigger dynamic pricing review                   │    │ │   │
│ │ 4. Publish: rate.applied                            │ ───┼─┼─┐ │
│ └────────────────────────────────────────────────────┘    │ │ │ │
│                                                            │ │ │ │
│ ┌────────────────────────────────────────────────────┐    │ │ │ │
│ │ Analytics Engine:                                   │    │ │ │ │
│ │ 1. Record reservation event                         │    │ │ │ │
│ │ 2. Update real-time dashboards                      │    │ │ │ │
│ │ 3. Calculate occupancy metrics                      │    │ │ │ │
│ │ 4. Update revenue forecasts                         │    │ │ │ │
│ │ 5. Publish: analytics.reservation                   │ ───┼─┼─┼─┘
│ └────────────────────────────────────────────────────┘    │ │ │
│                                                            │ │ │
└────────────────────────────────────────────────────────────┼─┼─┼──┘
                                                             │ │ │
       ┌─────────────────────────────────────────────────────┘ │ │
       │ ┌─────────────────────────────────────────────────────┘ │
       │ │ ┌─────────────────────────────────────────────────────┘
       │ │ │
       ▼ ▼ ▼
┌──────────────────────────────────────────────────────────────────┐
│                          Kafka Topics                            │
│  ├─ availability.updated                                         │
│  ├─ payment.authorized                                           │
│  ├─ rate.applied                                                 │
│  └─ analytics.reservation                                        │
└────┬──────────────┬──────────────────────────────────────────────┘
     │              │
     │ Consume      │ Consume
     │              │
     ▼              ▼

┌──────────────┐ ┌──────────────┐
│ Reservation  │ │     Rate     │
│   Engine     │ │  Management  │
│   (8081)     │ │    (8083)    │
└──────┬───────┘ └──────┬───────┘
       │                │
       │                │ Adjust dynamic pricing
       │                │ based on availability
       │                ▼
       │         Update rates in database
       │
       │ Update reservation status
       │ to CONFIRMED (payment authorized)
       ▼
Update database

┌──────────────────────────────────────────────────────────────────┐
│  FINAL STATE:                                                     │
│  ✅ Reservation: CONFIRMED in database                           │
│  ✅ Inventory: Reduced for booked dates                          │
│  ✅ Payment: Authorized and token stored                         │
│  ✅ Rates: Usage tracked, pricing adjusted                       │
│  ✅ Analytics: Metrics updated in real-time                      │
│                                                                   │
│  Total Time: ~500ms (all parallel processing)                    │
│  vs Synchronous: ~2000ms (sequential HTTP calls)                │
└──────────────────────────────────────────────────────────────────┘
```

---

## 🔄 Event Flow: Reservation Cancellation

```
Guest cancels reservation
       │
       ▼
┌──────────────────────────────────────────────────────────────────┐
│              Reservation Engine (8081)                           │
│  1. Validate cancellation request                                │
│  2. Check cancellation policy                                    │
│  3. Update status to CANCELLED                                   │
│  4. Publish: reservation.cancelled                               │
└────┬─────────────────────────────────────────────────────────────┘
     │
     ▼
┌──────────────────────────────────────────────────────────────────┐
│             Kafka Topic: reservation.cancelled                   │
└────┬──────────────┬──────────────┬────────────────────────────────┘
     │              │              │
     ▼              ▼              ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│ Availability │ │   Payment    │ │  Analytics   │
│ Calculator   │ │  Processor   │ │   Engine     │
└──────┬───────┘ └──────┬───────┘ └──────┬───────┘
       │                │                │
       │                │                │
       ▼                ▼                ▼
  Restore         Initiate         Update
  Inventory       Refund           Metrics
       │                │                │
       └────────┬───────┴────────────────┘
                │
                ▼
        All updates complete
```

---

## 🎯 Consumer Group Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│              Kafka Topic: reservation.created                   │
│                    (3 Partitions)                               │
├───────────────────┬───────────────────┬─────────────────────────┤
│   Partition 0     │   Partition 1     │   Partition 2           │
│   Messages 1-100  │   Messages 101-200│   Messages 201-300      │
└─────────┬─────────┴─────────┬─────────┴─────────┬───────────────┘
          │                   │                   │
          │                   │                   │
┌─────────▼───────────────────▼───────────────────▼───────────────┐
│         Consumer Group: availability-calculator-group           │
├─────────────────┬───────────────────┬───────────────────────────┤
│  Consumer 1     │  Consumer 2       │  Consumer 3               │
│  (Thread 1)     │  (Thread 2)       │  (Thread 3)               │
│  Reads P0       │  Reads P1         │  Reads P2                 │
│  100 msg/sec    │  100 msg/sec      │  100 msg/sec              │
└─────────────────┴───────────────────┴───────────────────────────┘
                    Total: 300 messages/sec

┌─────────────────────────────────────────────────────────────────┐
│         Consumer Group: payment-processor-group                 │
├─────────────────┬───────────────────┬───────────────────────────┤
│  Consumer 1     │  Consumer 2       │  Consumer 3               │
│  Reads P0       │  Reads P1         │  Reads P2                 │
│  (Independent   │  (Independent     │  (Independent             │
│   from above)   │   from above)     │   from above)             │
└─────────────────┴───────────────────┴───────────────────────────┘

Key Points:
- Each consumer group gets ALL messages
- Within a group, each partition read by ONE consumer
- Groups process independently
- Scalability: Add more partitions + consumers
```

---

## 🏛️ Service Communication Matrix

### Before Kafka (HTTP/REST - Tight Coupling)

```
┌───────────────┬──────────┬──────────┬──────┬─────────┬──────────┐
│               │Reserv.   │Avail.    │Rate  │Payment  │Analytics │
├───────────────┼──────────┼──────────┼──────┼─────────┼──────────┤
│Reservation    │    -     │  HTTP    │ HTTP │  HTTP   │  HTTP    │
│Engine         │          │  Call    │ Call │  Call   │  Call    │
├───────────────┼──────────┼──────────┼──────┼─────────┼──────────┤
│Availability   │  HTTP    │    -     │  -   │   -     │    -     │
│Calculator     │  Call    │          │      │         │          │
├───────────────┼──────────┼──────────┼──────┼─────────┼──────────┤
│Rate           │  HTTP    │  HTTP    │  -   │   -     │    -     │
│Management     │  Call    │  Call    │      │         │          │
├───────────────┼──────────┼──────────┼──────┼─────────┼──────────┤
│Payment        │  HTTP    │    -     │  -   │   -     │    -     │
│Processor      │  Call    │          │      │         │          │
├───────────────┼──────────┼──────────┼──────┼─────────┼──────────┤
│Analytics      │    -     │    -     │  -   │   -     │    -     │
│Engine         │          │          │      │         │          │
└───────────────┴──────────┴──────────┴──────┴─────────┴──────────┘

Problems:
❌ 8 direct dependencies
❌ If one service down, others fail
❌ Synchronous blocking calls
❌ Difficult to scale independently
```

### After Kafka (Event-Driven - Loose Coupling)

```
┌───────────────┬──────────┬──────────┬──────┬─────────┬──────────┐
│               │Reserv.   │Avail.    │Rate  │Payment  │Analytics │
├───────────────┼──────────┼──────────┼──────┼─────────┼──────────┤
│Reservation    │    -     │  Event   │Event │  Event  │  Event   │
│Engine         │          │  Pub     │ Pub  │  Pub    │  Pub     │
├───────────────┼──────────┼──────────┼──────┼─────────┼──────────┤
│Availability   │  Event   │    -     │Event │   -     │  Event   │
│Calculator     │  Sub     │          │ Pub  │         │  Pub     │
├───────────────┼──────────┼──────────┼──────┼─────────┼──────────┤
│Rate           │  Event   │  Event   │  -   │   -     │  Event   │
│Management     │  Sub     │  Sub     │      │         │  Pub     │
├───────────────┼──────────┼──────────┼──────┼─────────┼──────────┤
│Payment        │  Event   │    -     │  -   │   -     │  Event   │
│Processor      │  Sub+Pub │          │      │         │  Pub     │
├───────────────┼──────────┼──────────┼──────┼─────────┼──────────┤
│Analytics      │  Event   │  Event   │Event │  Event  │    -     │
│Engine         │  Sub     │  Sub     │ Sub  │  Sub    │          │
└───────────────┴──────────┴──────────┴──────┴─────────┴──────────┘

Benefits:
✅ Services don't know each other
✅ Services work independently
✅ Asynchronous non-blocking
✅ Easy to scale each service
✅ Easy to add new consumers
✅ Complete event history
```

---

## 📊 Topic Partitioning Strategy

```
Topic: reservation.created
Partitions: 3 (for load distribution)

Partition Assignment (by property ID):
┌─────────────────────────────────────────────────────────────┐
│  Partition 0: property IDs ending in 0, 3, 6, 9            │
│  ├─ PROP-001, PROP-004, PROP-007, PROP-010                 │
│  └─ Consumer 1 processes these                             │
├─────────────────────────────────────────────────────────────┤
│  Partition 1: property IDs ending in 1, 4, 7               │
│  ├─ PROP-002, PROP-005, PROP-008, PROP-011                 │
│  └─ Consumer 2 processes these                             │
├─────────────────────────────────────────────────────────────┤
│  Partition 2: property IDs ending in 2, 5, 8               │
│  ├─ PROP-003, PROP-006, PROP-009, PROP-012                 │
│  └─ Consumer 3 processes these                             │
└─────────────────────────────────────────────────────────────┘

Benefits:
✅ Even load distribution
✅ Messages for same property always in same partition (ordering)
✅ Parallel processing across properties
✅ Easy to scale (add more partitions)
```

---

## 🔐 Security & Reliability Patterns

```
┌─────────────────────────────────────────────────────────────────┐
│                    Reliability Patterns                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. EXACTLY-ONCE SEMANTICS                                      │
│     Producer: enable.idempotence=true                           │
│     Consumer: Idempotent processing                             │
│                                                                  │
│  2. RETRY WITH BACKOFF                                          │
│     Initial: 100ms → 200ms → 400ms → 800ms → 1600ms           │
│     Max retries: 5                                              │
│                                                                  │
│  3. DEAD LETTER QUEUE                                           │
│     Failed messages → reservation.created.dlq                   │
│     Manual investigation and replay                             │
│                                                                  │
│  4. CIRCUIT BREAKER                                             │
│     If consumer fails 10 times → OPEN circuit                  │
│     Wait 30 seconds → Try again (HALF-OPEN)                    │
│                                                                  │
│  5. CONSUMER LAG MONITORING                                     │
│     Alert if lag > 1000 messages                               │
│     Alert if lag age > 5 minutes                               │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📈 Scalability Model

```
Current Load: 100 reservations/minute
Future Load: 10,000 reservations/minute (100x increase)

Scaling Strategy:

┌─────────────────────────────────────────────────────────────────┐
│  Kafka Cluster:                                                  │
│  ├─ Current: 1 broker, 3 partitions                             │
│  └─ Scale to: 3-5 brokers, 10-20 partitions                     │
├─────────────────────────────────────────────────────────────────┤
│  Service Instances:                                              │
│  ├─ Reservation Engine: 1 → 5 instances                        │
│  ├─ Availability Calc:  1 → 3 instances                        │
│  ├─ Payment Processor:  1 → 3 instances                        │
│  ├─ Rate Management:    1 → 2 instances                        │
│  └─ Analytics Engine:   1 → 2 instances                        │
├─────────────────────────────────────────────────────────────────┤
│  Expected Performance:                                           │
│  ├─ Throughput: 10,000 reservations/minute                     │
│  ├─ Latency: < 100ms (p95)                                     │
│  ├─ Consumer Lag: < 100 messages                               │
│  └─ CPU Usage: < 70% per service                               │
└─────────────────────────────────────────────────────────────────┘
```

---

**Last Updated:** October 6, 2025
**Document Version:** 1.0
