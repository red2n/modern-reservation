-- Sample Rate Plans and Rates Data
-- This file contains sample data for rate plans, rates, and seasonal rates

-- Sample Rate Plans (40 rate plans)
DO $$
DECLARE
    property_ids UUID[];
    room_type_ids UUID[];
    property_id UUID;
    room_type_id UUID;
    rate_plan_id UUID;
    i INTEGER;
BEGIN
    SELECT ARRAY_AGG(id) INTO property_ids FROM properties;

    FOR i IN 1..40 LOOP
        property_id := property_ids[((i - 1) % ARRAY_LENGTH(property_ids, 1)) + 1];

        rate_plan_id := gen_random_uuid();

        INSERT INTO rate_plans (
            id, property_id, rate_plan_code, rate_plan_name, description,
            is_active, is_public, requires_qualification, minimum_stay, maximum_stay,
            advance_booking_days, cancellation_policy, created_at, updated_at
        ) VALUES (
            rate_plan_id,
            property_id,
            'RP' || LPAD(i::TEXT, 4, '0'),
            CASE (i % 5)
                WHEN 0 THEN 'Standard Rate Plan'
                WHEN 1 THEN 'Weekend Special'
                WHEN 2 THEN 'Corporate Rate'
                WHEN 3 THEN 'Extended Stay Discount'
                ELSE 'Seasonal Promotion'
            END || ' ' || i::TEXT,
            'Sample rate plan description for plan ' || i::TEXT,
            true,
            (i % 3 != 0),
            (i % 5 = 0),
            CASE (i % 4)
                WHEN 0 THEN 1
                WHEN 1 THEN 2
                WHEN 2 THEN 3
                ELSE NULL
            END,
            CASE (i % 4)
                WHEN 0 THEN 7
                WHEN 1 THEN 14
                WHEN 2 THEN 30
                ELSE NULL
            END,
            CASE (i % 3)
                WHEN 0 THEN 0
                WHEN 1 THEN 7
                ELSE 14
            END,
            CASE (i % 3)
                WHEN 0 THEN 'FLEXIBLE'
                WHEN 1 THEN 'MODERATE'
                ELSE 'STRICT'
            END,
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        );
    END LOOP;
END $$;

-- Sample Rate Plan Rates (200 rates)
DO $$
DECLARE
    rate_plan_rec RECORD;
    room_type_ids UUID[];
    room_type_id UUID;
    counter INTEGER := 0;
BEGIN
    FOR rate_plan_rec IN (SELECT id, property_id FROM rate_plans ORDER BY id) LOOP
        -- Get room types for this property
        SELECT ARRAY_AGG(id) INTO room_type_ids
        FROM room_types
        WHERE property_id = rate_plan_rec.property_id;

        IF room_type_ids IS NOT NULL AND ARRAY_LENGTH(room_type_ids, 1) > 0 THEN
            -- Add 5 rates per rate plan
            FOR i IN 1..LEAST(5, ARRAY_LENGTH(room_type_ids, 1)) LOOP
                counter := counter + 1;
                room_type_id := room_type_ids[i];

                INSERT INTO rate_plan_rates (
                    id, rate_plan_id, room_type_id, base_rate,
                    monday_rate, tuesday_rate, wednesday_rate, thursday_rate,
                    friday_rate, saturday_rate, sunday_rate,
                    effective_date, expiry_date, created_at, updated_at
                ) VALUES (
                    gen_random_uuid(),
                    rate_plan_rec.id,
                    room_type_id,
                    100.00 + (counter * 10),
                    95.00 + (counter * 10),
                    95.00 + (counter * 10),
                    95.00 + (counter * 10),
                    95.00 + (counter * 10),
                    120.00 + (counter * 10),
                    130.00 + (counter * 10),
                    110.00 + (counter * 10),
                    CURRENT_DATE - INTERVAL '30 days',
                    CURRENT_DATE + INTERVAL '365 days',
                    CURRENT_TIMESTAMP,
                    CURRENT_TIMESTAMP
                );
            END LOOP;
        END IF;
    END LOOP;
END $$;

-- Sample Rates for rate-management service (100 rates)
DO $$
DECLARE
    property_ids UUID[];
    room_type_ids UUID[];
    property_id UUID;
    room_type_id UUID;
    i INTEGER;
BEGIN
    SELECT ARRAY_AGG(id) INTO property_ids FROM properties;

    FOR i IN 1..100 LOOP
        property_id := property_ids[((i - 1) % ARRAY_LENGTH(property_ids, 1)) + 1];

        -- Get a room type for this property
        SELECT id INTO room_type_id
        FROM room_types
        WHERE property_id = property_id
        ORDER BY RANDOM()
        LIMIT 1;

        IF room_type_id IS NOT NULL THEN
            INSERT INTO rates (
                id, property_id, room_type_id, rate_code, rate_name, description,
                base_rate, current_rate, minimum_rate, maximum_rate, currency,
                effective_date, expiry_date, rate_status, rate_strategy, season_type,
                is_active, is_refundable, is_modifiable, tax_inclusive, service_fee_inclusive,
                minimum_stay, maximum_stay, advance_booking_days, cancellation_hours,
                priority_order, demand_multiplier, occupancy_multiplier,
                created_at, updated_at
            ) VALUES (
                gen_random_uuid(),
                property_id,
                room_type_id,
                'RATE' || LPAD(i::TEXT, 5, '0'),
                CASE (i % 4)
                    WHEN 0 THEN 'Standard Rate'
                    WHEN 1 THEN 'Premium Rate'
                    WHEN 2 THEN 'Discount Rate'
                    ELSE 'Dynamic Rate'
                END || ' ' || i::TEXT,
                'Rate description for rate ' || i::TEXT,
                100.00 + (i * 5),
                100.00 + (i * 5) + ((i % 20) * 2),
                80.00 + (i * 5),
                150.00 + (i * 5),
                'USD',
                CURRENT_DATE - ((i % 60) || ' days')::INTERVAL,
                CURRENT_DATE + ((200 + (i % 200)) || ' days')::INTERVAL,
                CASE (i % 3)
                    WHEN 0 THEN 'ACTIVE'
                    WHEN 1 THEN 'INACTIVE'
                    ELSE 'ACTIVE'
                END,
                CASE (i % 4)
                    WHEN 0 THEN 'FIXED'
                    WHEN 1 THEN 'DYNAMIC'
                    WHEN 2 THEN 'OCCUPANCY_BASED'
                    ELSE 'SEASONAL'
                END,
                CASE (i % 4)
                    WHEN 0 THEN 'HIGH_SEASON'
                    WHEN 1 THEN 'LOW_SEASON'
                    WHEN 2 THEN 'SHOULDER_SEASON'
                    ELSE NULL
                END,
                (i % 5 != 0),
                (i % 3 != 0),
                (i % 2 = 0),
                (i % 4 = 0),
                (i % 5 = 0),
                CASE (i % 3) WHEN 0 THEN 1 WHEN 1 THEN 2 ELSE NULL END,
                CASE (i % 3) WHEN 0 THEN 7 WHEN 1 THEN 14 ELSE NULL END,
                CASE (i % 4) WHEN 0 THEN 0 WHEN 1 THEN 7 WHEN 2 THEN 14 ELSE 21 END,
                CASE (i % 3) WHEN 0 THEN 24 WHEN 1 THEN 48 ELSE 72 END,
                i % 10,
                1.0 + ((i % 10) * 0.1),
                1.0 + ((i % 5) * 0.05),
                CURRENT_TIMESTAMP,
                CURRENT_TIMESTAMP
            );
        END IF;
    END LOOP;
END $$;

-- Sample Seasonal Rates (100 seasonal rates)
DO $$
DECLARE
    property_ids UUID[];
    room_type_ids UUID[];
    property_id UUID;
    room_type_id UUID;
    i INTEGER;
    start_date DATE;
    end_date DATE;
BEGIN
    SELECT ARRAY_AGG(id) INTO property_ids FROM properties;

    FOR i IN 1..100 LOOP
        property_id := property_ids[((i - 1) % ARRAY_LENGTH(property_ids, 1)) + 1];

        SELECT id INTO room_type_id
        FROM room_types
        WHERE property_id = property_id
        ORDER BY RANDOM()
        LIMIT 1;

        -- Create seasonal date ranges
        start_date := CURRENT_DATE + ((i % 12) * 30 || ' days')::INTERVAL;
        end_date := start_date + INTERVAL '45 days';

        IF room_type_id IS NOT NULL THEN
            INSERT INTO seasonal_rates (
                id, property_id, room_type_id, season_name, season_type,
                start_date, end_date, rate_multiplier, base_rate_adjustment,
                minimum_stay, is_active, created_at, updated_at
            ) VALUES (
                gen_random_uuid(),
                property_id,
                room_type_id,
                CASE (i % 4)
                    WHEN 0 THEN 'Summer Special'
                    WHEN 1 THEN 'Winter Wonderland'
                    WHEN 2 THEN 'Spring Break'
                    ELSE 'Fall Festival'
                END || ' ' || i::TEXT,
                CASE (i % 3)
                    WHEN 0 THEN 'PEAK'
                    WHEN 1 THEN 'OFF_PEAK'
                    ELSE 'SHOULDER'
                END,
                start_date,
                end_date,
                CASE (i % 3)
                    WHEN 0 THEN 1.5
                    WHEN 1 THEN 0.8
                    ELSE 1.2
                END,
                CASE (i % 3)
                    WHEN 0 THEN 50.00
                    WHEN 1 THEN -20.00
                    ELSE 10.00
                END,
                CASE (i % 4) WHEN 0 THEN 2 WHEN 1 THEN 3 ELSE NULL END,
                (i % 5 != 0),
                CURRENT_TIMESTAMP,
                CURRENT_TIMESTAMP
            );
        END IF;
    END LOOP;
END $$;
