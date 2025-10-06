-- Sample Availability Data
-- This file contains sample data for room availability and channel availability

-- Sample Room Availability (1000+ records)
DO $$
DECLARE
    room_rec RECORD;
    availability_date DATE;
    counter INTEGER := 0;
BEGIN
    FOR room_rec IN (SELECT id, property_id FROM rooms ORDER BY id LIMIT 100) LOOP
        -- Create 10 days of availability for each room
        FOR i IN 0..9 LOOP
            counter := counter + 1;
            availability_date := CURRENT_DATE + (i || ' days')::INTERVAL;

            INSERT INTO room_availability (
                id, property_id, room_id, availability_date,
                is_available, status, base_rate, current_rate,
                minimum_stay, maximum_stay, closed_to_arrival, closed_to_departure,
                created_at, updated_at
            ) VALUES (
                gen_random_uuid(),
                room_rec.property_id,
                room_rec.id,
                availability_date,
                (counter % 5 != 0),  -- 80% available
                CASE (counter % 5)
                    WHEN 0 THEN 'BOOKED'
                    WHEN 1 THEN 'AVAILABLE'
                    WHEN 2 THEN 'AVAILABLE'
                    WHEN 3 THEN 'AVAILABLE'
                    ELSE 'AVAILABLE'
                END,
                100.00 + (counter % 50) * 5,
                120.00 + (counter % 50) * 5,
                CASE (counter % 10) WHEN 0 THEN 2 ELSE 1 END,
                CASE (counter % 10) WHEN 5 THEN 7 ELSE NULL END,
                false,
                false,
                CURRENT_TIMESTAMP,
                CURRENT_TIMESTAMP
            );
        END LOOP;
    END LOOP;
END $$;

-- Sample Channel Availability (200 records)
DO $$
DECLARE
    room_rec RECORD;
    property_rec RECORD;
    channels TEXT[] := ARRAY['DIRECT', 'BOOKING_COM', 'EXPEDIA', 'AIRBNB', 'HOTELS_COM'];
    channel TEXT;
    availability_date DATE;
    counter INTEGER := 0;
BEGIN
    FOR room_rec IN (SELECT id, property_id FROM rooms ORDER BY id LIMIT 40) LOOP
        FOR i IN 1..5 LOOP
            counter := counter + 1;
            channel := channels[i];
            availability_date := CURRENT_DATE + ((counter % 30) || ' days')::INTERVAL;

            INSERT INTO channel_availability (
                id, property_id, room_id, channel_name, availability_date,
                available_quantity, sold_quantity, rate_amount, commission_rate,
                is_active, created_at, updated_at
            ) VALUES (
                gen_random_uuid(),
                room_rec.property_id,
                room_rec.id,
                channel,
                availability_date,
                10,
                (counter % 10),
                150.00 + (counter % 30) * 5,
                CASE channel
                    WHEN 'DIRECT' THEN 0.00
                    WHEN 'BOOKING_COM' THEN 15.00
                    WHEN 'EXPEDIA' THEN 18.00
                    WHEN 'AIRBNB' THEN 12.00
                    ELSE 15.00
                END,
                true,
                CURRENT_TIMESTAMP,
                CURRENT_TIMESTAMP
            );
        END LOOP;
    END LOOP;
END $$;

-- Sample Yield Rules (50 rules)
DO $$
DECLARE
    property_ids UUID[];
    property_id UUID;
    i INTEGER;
BEGIN
    SELECT ARRAY_AGG(id) INTO property_ids FROM properties;

    FOR i IN 1..50 LOOP
        property_id := property_ids[((i - 1) % ARRAY_LENGTH(property_ids, 1)) + 1];

        INSERT INTO yield_rules (
            id, property_id, rule_name, rule_type, priority,
            occupancy_threshold, days_before_arrival, rate_adjustment,
            multiplier, minimum_rate, maximum_rate, is_active,
            effective_date, expiry_date, created_at, updated_at
        ) VALUES (
            gen_random_uuid(),
            property_id,
            CASE (i % 4)
                WHEN 0 THEN 'High Occupancy Rule'
                WHEN 1 THEN 'Low Occupancy Discount'
                WHEN 2 THEN 'Last Minute Booking'
                ELSE 'Early Bird Special'
            END || ' ' || i::TEXT,
            CASE (i % 3)
                WHEN 0 THEN 'OCCUPANCY_BASED'
                WHEN 1 THEN 'TIME_BASED'
                ELSE 'DEMAND_BASED'
            END,
            i % 10,
            CASE (i % 3)
                WHEN 0 THEN 0.80
                WHEN 1 THEN 0.50
                ELSE NULL
            END,
            CASE (i % 4)
                WHEN 0 THEN 7
                WHEN 1 THEN 14
                WHEN 2 THEN 30
                ELSE NULL
            END,
            CASE (i % 3)
                WHEN 0 THEN 20.00
                WHEN 1 THEN -10.00
                ELSE 0.00
            END,
            1.0 + ((i % 5) * 0.1),
            80.00 + (i * 2),
            200.00 + (i * 5),
            (i % 7 != 0),
            CURRENT_DATE - INTERVAL '30 days',
            CURRENT_DATE + INTERVAL '180 days',
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        );
    END LOOP;
END $$;
