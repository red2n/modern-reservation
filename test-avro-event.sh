#!/bin/bash

# Test Avro Event Publishing
# This script creates a test reservation which will publish an Avro event to Kafka

echo "=========================================="
echo " Testing Avro Event Publishing"
echo "=========================================="
echo

# Step 1: Check if Schema Registry is accessible
echo "1. Checking Schema Registry connectivity..."
SCHEMA_REGISTRY_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8085/)
if [ "$SCHEMA_REGISTRY_STATUS" == "200" ]; then
    echo "   ✓ Schema Registry is UP (http://localhost:8085)"
else
    echo "   ✗ Schema Registry is DOWN"
    exit 1
fi
echo

# Step 2: Check current schemas (should be empty initially)
echo "2. Current registered schemas:"
SCHEMAS=$(curl -s http://localhost:8085/subjects)
echo "   $SCHEMAS"
echo

# Step 3: Create a test reservation (triggers Avro event)
echo "3. Creating test reservation..."
RESERVATION_PAYLOAD='{
  "guestId": "550e8400-e29b-41d4-a716-446655440001",
  "propertyId": "550e8400-e29b-41d4-a716-446655440002",
  "roomTypeId": "550e8400-e29b-41d4-a716-446655440003",
  "checkInDate": "2025-12-20",
  "checkOutDate": "2025-12-25",
  "numberOfAdults": 2,
  "numberOfChildren": 1,
  "numberOfInfants": 0,
  "specialRequests": "Late check-in, high floor",
  "totalAmount": 1250.00,
  "currency": "USD",
  "channelType": "DIRECT",
  "source": "WEB_PORTAL"
}'

echo "   Payload: $RESERVATION_PAYLOAD"
echo
echo "   Sending request to: http://localhost:8081/reservation-engine/api/reservations"

# Note: This endpoint requires authentication
# Get the generated password from logs: grep "Using generated security password" /tmp/reservation-engine-avro.log
PASSWORD=$(grep "Using generated security password" /tmp/reservation-engine-avro.log | tail -1 | awk '{print $NF}')

if [ -z "$PASSWORD" ]; then
    echo "   ✗ Could not find generated password in logs"
    echo "   Please check logs manually: tail -100 /tmp/reservation-engine-avro.log"
    exit 1
fi

echo "   Using password: $PASSWORD"
echo

RESPONSE=$(curl -s -u user:$PASSWORD \
  -X POST \
  -H "Content-Type: application/json" \
  -d "$RESERVATION_PAYLOAD" \
  http://localhost:8081/reservation-engine/api/reservations)

echo "   Response: $RESPONSE"
echo

# Step 4: Check if schema was registered
echo "4. Checking if schema was registered..."
sleep 2
SCHEMAS_AFTER=$(curl -s http://localhost:8085/subjects)
echo "   Registered schemas: $SCHEMAS_AFTER"
echo

# Step 5: If schema exists, get its details
if [ "$SCHEMAS_AFTER" != "[]" ]; then
    echo "5. Schema details:"
    for subject in $(echo "$SCHEMAS_AFTER" | jq -r '.[]'); do
        echo "   Subject: $subject"
        SCHEMA_INFO=$(curl -s http://localhost:8085/subjects/$subject/versions/latest)
        echo "   $SCHEMA_INFO" | jq .
        echo
    done
else
    echo "5. No schemas registered yet"
    echo "   This might indicate the event wasn't published"
    echo "   Check application logs: tail -50 /tmp/reservation-engine-avro.log"
fi

echo "=========================================="
echo " Test Complete"
echo "=========================================="
