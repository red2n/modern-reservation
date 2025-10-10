# Reservation Engine API

The Reservation Engine is the core booking service that handles reservation lifecycle management, booking workflows, and guest services for the Modern Reservation System.

## 🎯 Overview

| Property | Value |
|----------|-------|
| **Service Name** | reservation-engine |
| **Port** | 8084 |
| **Health Check** | `/actuator/health` |
| **API Base URL** | `http://localhost:8084/api/v1/reservations` |
| **OpenAPI Spec** | `/v3/api-docs` |
| **Swagger UI** | `/swagger-ui.html` |

## 🚀 Quick Start

### Start the Service
```bash
# Via dev script
./dev.sh start reservation-engine

# Via Maven
cd apps/backend/java-services/business-services/reservation-engine
mvn spring-boot:run
```

### Health Check
```bash
curl http://localhost:8084/actuator/health
```

## 🏨 Core Features

### Reservation Management
- **Full Booking Lifecycle** from inquiry to check-out
- **Multi-step Booking Process** with hold/release mechanisms
- **Guest Profile Management** with preferences and history
- **Group Reservations** with block management
- **Modification and Cancellation** workflows

### Booking Workflows
- **Real-time Availability** integration
- **Dynamic Pricing** integration
- **Automatic Confirmation** and guest notifications
- **Payment Processing** integration
- **Revenue Recognition** and reporting

### Guest Services
- **Guest Check-in/Check-out** management
- **Room Assignment** optimization
- **Special Requests** handling
- **Loyalty Program** integration
- **Guest Communication** and notifications

## 🔌 API Endpoints

### Create Reservation
```http
POST /api/v1/reservations
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
Content-Type: application/json
```

**Request Body:**
```json
{
  "propertyId": "prop-123",
  "guestInfo": {
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "+1-555-0123",
    "preferences": {
      "roomType": "deluxe",
      "floorPreference": "HIGH",
      "bedType": "KING",
      "smokingPreference": "NON_SMOKING"
    }
  },
  "stayDetails": {
    "checkIn": "2025-10-15",
    "checkOut": "2025-10-17",
    "nights": 2,
    "adults": 2,
    "children": 0,
    "roomType": "deluxe",
    "rateCode": "BAR"
  },
  "paymentDetails": {
    "method": "CREDIT_CARD",
    "cardToken": "tok_1234567890",
    "billingAddress": {
      "name": "John Doe",
      "addressLine1": "123 Main St",
      "city": "New York",
      "state": "NY",
      "zipCode": "10001",
      "country": "US"
    }
  },
  "specialRequests": [
    "Late check-in expected",
    "Honeymoon package"
  ],
  "source": {
    "channel": "DIRECT",
    "medium": "WEBSITE",
    "campaign": "summer-promotion"
  }
}
```

**Response:**
```json
{
  "reservationId": "res-789",
  "confirmationNumber": "ABC123456",
  "status": "CONFIRMED",
  "guestInfo": {
    "guestId": "guest-456",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com"
  },
  "stayDetails": {
    "checkIn": "2025-10-15",
    "checkOut": "2025-10-17",
    "nights": 2,
    "roomType": "deluxe",
    "roomNumber": "TBD"
  },
  "financial": {
    "totalAmount": 350.00,
    "baseAmount": 300.00,
    "taxes": 30.00,
    "fees": 20.00,
    "currency": "USD",
    "paymentStatus": "PAID"
  },
  "timeline": [
    {
      "event": "CREATED",
      "timestamp": "2025-10-10T12:00:00Z",
      "status": "CONFIRMED"
    }
  ],
  "createdAt": "2025-10-10T12:00:00Z"
}
```

### Get Reservation Details
```http
GET /api/v1/reservations/{reservationId}
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
```

**Response:**
```json
{
  "reservationId": "res-789",
  "confirmationNumber": "ABC123456",
  "status": "CONFIRMED",
  "propertyInfo": {
    "propertyId": "prop-123",
    "propertyName": "Grand Hotel Downtown",
    "address": {
      "addressLine1": "456 Hotel Ave",
      "city": "New York",
      "state": "NY",
      "zipCode": "10002",
      "country": "US"
    },
    "contact": {
      "phone": "+1-555-0199",
      "email": "reservations@grandhotel.com"
    }
  },
  "guestInfo": {
    "guestId": "guest-456",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "+1-555-0123",
    "loyaltyMember": true,
    "loyaltyTier": "GOLD"
  },
  "stayDetails": {
    "checkIn": "2025-10-15",
    "checkOut": "2025-10-17",
    "nights": 2,
    "adults": 2,
    "children": 0,
    "roomType": "deluxe",
    "roomNumber": "502",
    "assignedAt": "2025-10-14T10:00:00Z"
  },
  "financial": {
    "totalAmount": 350.00,
    "baseAmount": 300.00,
    "taxes": 30.00,
    "fees": 20.00,
    "currency": "USD",
    "breakdown": [
      {
        "date": "2025-10-15",
        "roomRate": 150.00,
        "taxes": 15.00
      },
      {
        "date": "2025-10-16",
        "roomRate": 150.00,
        "taxes": 15.00
      }
    ],
    "paymentStatus": "PAID",
    "paymentDetails": [
      {
        "paymentId": "pay-123",
        "amount": 350.00,
        "method": "CREDIT_CARD",
        "processedAt": "2025-10-10T12:05:00Z"
      }
    ]
  },
  "specialRequests": [
    "Late check-in expected",
    "Honeymoon package"
  ],
  "amenities": [
    "Welcome champagne",
    "Room upgrade",
    "Late checkout available"
  ],
  "timeline": [
    {
      "event": "CREATED",
      "timestamp": "2025-10-10T12:00:00Z",
      "status": "CONFIRMED",
      "notes": "Reservation created via website"
    },
    {
      "event": "ROOM_ASSIGNED",
      "timestamp": "2025-10-14T10:00:00Z",
      "status": "CONFIRMED",
      "notes": "Room 502 assigned - Deluxe King"
    }
  ]
}
```

### Modify Reservation
```http
PUT /api/v1/reservations/{reservationId}
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
Content-Type: application/json
```

**Request Body:**
```json
{
  "modifications": {
    "stayDetails": {
      "checkOut": "2025-10-18",
      "nights": 3
    },
    "guestInfo": {
      "phone": "+1-555-0124"
    },
    "specialRequests": [
      "Late check-in expected",
      "Honeymoon package",
      "Airport transfer needed"
    ]
  },
  "reason": "Guest requested extension",
  "modifiedBy": "guest"
}
```

**Response:**
```json
{
  "modificationId": "mod-456",
  "reservationId": "res-789",
  "status": "CONFIRMED",
  "changes": {
    "nights": {
      "from": 2,
      "to": 3
    },
    "checkOut": {
      "from": "2025-10-17",
      "to": "2025-10-18"
    },
    "totalAmount": {
      "from": 350.00,
      "to": 525.00
    }
  },
  "additionalPayment": {
    "amount": 175.00,
    "paymentRequired": true,
    "dueDate": "2025-10-15T00:00:00Z"
  },
  "modifiedAt": "2025-10-10T14:30:00Z"
}
```

### Cancel Reservation
```http
DELETE /api/v1/reservations/{reservationId}
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
Content-Type: application/json
```

**Request Body:**
```json
{
  "reason": "GUEST_REQUEST",
  "description": "Guest needs to cancel due to travel restrictions",
  "cancelledBy": "guest",
  "refundRequested": true
}
```

**Response:**
```json
{
  "cancellationId": "cancel-789",
  "reservationId": "res-789",
  "status": "CANCELLED",
  "cancellationPolicy": {
    "policyName": "Standard Cancellation",
    "deadlineDate": "2025-10-13T18:00:00Z",
    "penaltyAmount": 0.00,
    "refundEligible": true
  },
  "refund": {
    "refundAmount": 350.00,
    "refundMethod": "ORIGINAL_PAYMENT_METHOD",
    "processingTime": "3-5 business days",
    "refundId": "ref-123"
  },
  "cancelledAt": "2025-10-10T15:00:00Z"
}
```

### Guest Check-in
```http
POST /api/v1/reservations/{reservationId}/checkin
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
Content-Type: application/json
```

**Request Body:**
```json
{
  "checkInTime": "2025-10-15T16:30:00Z",
  "actualGuests": {
    "adults": 2,
    "children": 0
  },
  "identificationVerified": true,
  "keyCards": 2,
  "staffMember": "staff-123",
  "notes": "VIP guest - provided welcome amenities"
}
```

**Response:**
```json
{
  "checkInId": "checkin-456",
  "reservationId": "res-789",
  "status": "CHECKED_IN",
  "roomDetails": {
    "roomNumber": "502",
    "floor": 5,
    "keyCards": ["KC001234", "KC001235"],
    "keyCardExpiry": "2025-10-17T12:00:00Z"
  },
  "amenities": [
    "WiFi password: Hotel2025",
    "Breakfast: 6:30 AM - 10:30 AM",
    "Pool hours: 6:00 AM - 10:00 PM"
  ],
  "checkInTime": "2025-10-15T16:30:00Z",
  "expectedCheckOut": "2025-10-17T11:00:00Z"
}
```

### Guest Check-out
```http
POST /api/v1/reservations/{reservationId}/checkout
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
Content-Type: application/json
```

**Request Body:**
```json
{
  "checkOutTime": "2025-10-17T10:45:00Z",
  "finalCharges": [
    {
      "description": "Room service",
      "amount": 45.00,
      "date": "2025-10-16"
    },
    {
      "description": "Parking",
      "amount": 30.00,
      "date": "2025-10-16"
    }
  ],
  "roomCondition": "GOOD",
  "keyCardsReturned": 2,
  "staffMember": "staff-456"
}
```

**Response:**
```json
{
  "checkOutId": "checkout-789",
  "reservationId": "res-789",
  "status": "CHECKED_OUT",
  "finalBill": {
    "roomCharges": 350.00,
    "additionalCharges": 75.00,
    "taxes": 42.50,
    "totalAmount": 467.50,
    "balanceDue": 117.50
  },
  "payment": {
    "method": "CARD_ON_FILE",
    "amount": 117.50,
    "paymentId": "pay-456",
    "receiptUrl": "https://receipts.example.com/rec-789"
  },
  "feedback": {
    "surveyUrl": "https://survey.example.com/res-789",
    "loyaltyPoints": 467
  },
  "checkOutTime": "2025-10-17T10:45:00Z"
}
```

## 📡 Event Publishing

The Reservation Engine publishes comprehensive events:

### Reservation Events
```yaml
Topic: reservation.created
Schema:
  - reservationId: string
  - propertyId: string
  - guestId: string
  - checkIn: date
  - checkOut: date
  - roomType: string
  - totalAmount: decimal
  - status: enum
  - source: object

Topic: reservation.modified
Schema:
  - reservationId: string
  - modificationId: string
  - changes: object
  - previousAmount: decimal
  - newAmount: decimal
  - modifiedAt: timestamp

Topic: reservation.cancelled
Schema:
  - reservationId: string
  - cancellationId: string
  - reason: string
  - refundAmount: decimal
  - cancelledAt: timestamp

Topic: guest.checkedin
Schema:
  - reservationId: string
  - guestId: string
  - roomNumber: string
  - checkInTime: timestamp
  - expectedCheckOut: timestamp

Topic: guest.checkedout
Schema:
  - reservationId: string
  - guestId: string
  - roomNumber: string
  - checkOutTime: timestamp
  - finalAmount: decimal
  - satisfaction: object
```

## 🔧 Configuration

### Application Properties
```yaml
# Reservation Engine Configuration
reservation-engine:
  booking:
    hold-duration: 900s  # 15 minutes
    auto-confirmation: true
    payment-required: true

  policies:
    cancellation:
      default-policy: "STANDARD"
      free-cancellation-hours: 48

    modification:
      allowed-modifications: ["DATES", "GUESTS", "ROOM_TYPE"]
      modification-deadline-hours: 24

  room-assignment:
    auto-assignment: true
    assignment-time: "14:00"  # 2 PM day before arrival
    upgrade-logic: "LOYALTY_BASED"

  notifications:
    confirmation-email: true
    reminder-email: true
    reminder-hours-before: 24

  integrations:
    payment-service: true
    availability-service: true
    rate-service: true
    analytics-service: true

# Database Configuration
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/reservations
    username: reservation_user
    password: ${RESERVATION_DB_PASSWORD}

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
# Database
RESERVATION_DB_PASSWORD=secure_reservation_password

# External Services
PAYMENT_SERVICE_URL=http://localhost:8082
AVAILABILITY_SERVICE_URL=http://localhost:8081
RATE_SERVICE_URL=http://localhost:8083

# Notifications
EMAIL_SERVICE_URL=http://localhost:8086
SMS_SERVICE_URL=http://localhost:8087

# Monitoring
OTEL_SERVICE_NAME=reservation-engine
OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317
```

## 📈 Performance & Monitoring

### Performance Metrics
- **Booking Response Time**: <500ms (95th percentile)
- **Reservation Creation**: <2 seconds end-to-end
- **Modification Processing**: <1 second
- **Check-in Process**: <30 seconds
- **System Availability**: 99.99% uptime

### Business Metrics
```
# Reservation metrics
reservations_created_total{property="prop-123",source="direct"}
reservations_cancelled_total{reason="guest_request"}
reservations_modified_total{type="date_change"}

# Financial metrics
reservation_revenue_total{currency="USD"}
average_daily_rate{property="prop-123"}
occupancy_rate{property="prop-123"}

# Guest experience metrics
checkin_duration_seconds
checkout_duration_seconds
guest_satisfaction_score
```

## 🧪 Testing

### Unit Tests
```bash
cd apps/backend/java-services/business-services/reservation-engine
mvn test
```

### Integration Tests
```bash
mvn verify -P integration-tests
```

### API Testing
```bash
# Test reservation creation
curl -X POST http://localhost:8084/api/v1/reservations \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: test-tenant" \
  -H "Content-Type: application/json" \
  -d '{
    "propertyId": "prop-123",
    "guestInfo": {
      "firstName": "Test",
      "lastName": "Guest",
      "email": "test@example.com"
    },
    "stayDetails": {
      "checkIn": "2025-10-15",
      "checkOut": "2025-10-17",
      "roomType": "standard"
    }
  }'

# Test reservation modification
curl -X PUT http://localhost:8084/api/v1/reservations/res-123 \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: test-tenant" \
  -H "Content-Type: application/json" \
  -d '{
    "modifications": {
      "stayDetails": {
        "checkOut": "2025-10-18"
      }
    },
    "reason": "Extension requested"
  }'

# Test check-in
curl -X POST http://localhost:8084/api/v1/reservations/res-123/checkin \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: test-tenant" \
  -H "Content-Type: application/json" \
  -d '{
    "checkInTime": "2025-10-15T15:30:00Z",
    "identificationVerified": true,
    "keyCards": 2
  }'
```

## 🛠️ Development

### Local Development
```bash
# Run with development profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run with debug
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5009"
```

### Database Schema
```sql
-- Reservations table
CREATE TABLE reservations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    property_id VARCHAR(50) NOT NULL,
    confirmation_number VARCHAR(20) UNIQUE NOT NULL,
    guest_id UUID REFERENCES guests(id),
    status VARCHAR(20) NOT NULL,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    nights INTEGER NOT NULL,
    adults INTEGER NOT NULL,
    children INTEGER DEFAULT 0,
    room_type VARCHAR(50),
    room_number VARCHAR(10),
    total_amount DECIMAL(10,2),
    currency VARCHAR(3),
    source VARCHAR(50),
    special_requests TEXT[],
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Reservation timeline table
CREATE TABLE reservation_timeline (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reservation_id UUID REFERENCES reservations(id),
    event_type VARCHAR(50) NOT NULL,
    status VARCHAR(20),
    description TEXT,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Guest check-ins table
CREATE TABLE guest_checkins (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reservation_id UUID REFERENCES reservations(id),
    check_in_time TIMESTAMP NOT NULL,
    key_cards INTEGER,
    staff_member VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 🚨 Troubleshooting

### Common Issues

**Reservation Creation Failure**
```bash
# Check availability service connectivity
curl http://localhost:8084/actuator/health/availabilityService

# Check payment service status
curl http://localhost:8084/actuator/health/paymentService

# View reservation creation logs
tail -f logs/reservation-engine.log | grep "reservation.create"
```

**Room Assignment Issues**
```bash
# Check room assignment queue
curl http://localhost:8084/actuator/metrics/room.assignment.queue.size

# Manual room assignment
curl -X POST http://localhost:8084/api/v1/admin/reservations/res-123/assign-room \
     -H "Authorization: Bearer $ADMIN_TOKEN" \
     -d '{"roomNumber": "502"}'

# View assignment conflicts
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8084/api/v1/admin/room-assignments/conflicts
```

**Check-in/Check-out Problems**
```bash
# Verify guest information
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8084/api/v1/reservations/res-123/guest-verification

# Check room status
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8084/api/v1/admin/rooms/502/status

# Force check-out
curl -X POST http://localhost:8084/api/v1/admin/reservations/res-123/force-checkout \
     -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## 📚 Related Documentation
- [Business Services Overview](../index.md)
- [Payment Processor](../payment-processor/)
- [Availability Calculator](../availability-calculator/)
- [Guest Profile Management](../../../guides/guest-management.md)
