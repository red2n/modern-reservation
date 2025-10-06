-- Sample Reservations Data
-- This file contains sample data for reservations, room assignments, and charges

-- Sample Reservations (150 reservations)
DO $$
DECLARE
    guest_ids UUID[];
    property_ids UUID[];
    room_ids UUID[];
    user_ids UUID[];
    guest_id UUID;
    property_id UUID;
    room_id UUID;
    user_id UUID;
    reservation_id UUID;
    check_in_date DATE;
    check_out_date DATE;
    i INTEGER;
    nights INTEGER;
    rate DECIMAL(10,2);
BEGIN
    SELECT ARRAY_AGG(id) INTO guest_ids FROM guests;
    SELECT ARRAY_AGG(id) INTO property_ids FROM properties;
    SELECT ARRAY_AGG(id) INTO user_ids FROM users WHERE is_active = true;

    FOR i IN 1..150 LOOP
        guest_id := guest_ids[((i - 1) % ARRAY_LENGTH(guest_ids, 1)) + 1];
        property_id := property_ids[((i - 1) % ARRAY_LENGTH(property_ids, 1)) + 1];
        user_id := user_ids[((i - 1) % ARRAY_LENGTH(user_ids, 1)) + 1];

        -- Get a room from this property
        SELECT id INTO room_id FROM rooms WHERE property_id = property_id ORDER BY RANDOM() LIMIT 1;

        -- Generate reservation dates
        check_in_date := CURRENT_DATE + ((i % 60) - 30 || ' days')::INTERVAL;
        nights := 1 + (i % 7);
        check_out_date := check_in_date + (nights || ' days')::INTERVAL;
        rate := 100.00 + (i * 3);

        reservation_id := gen_random_uuid();

        INSERT INTO reservations (
            id, confirmation_number, property_id, guest_id, room_id,
            check_in_date, check_out_date, number_of_adults, number_of_children,
            total_guests, number_of_nights, room_rate, total_amount,
            booking_source, booking_channel, reservation_status,
            payment_status, special_requests, notes, created_by, updated_by,
            created_at, updated_at
        ) VALUES (
            reservation_id,
            'CONF' || LPAD(i::TEXT, 10, '0'),
            property_id,
            guest_id,
            room_id,
            check_in_date,
            check_out_date,
            1 + (i % 3),
            (i % 3),
            2 + (i % 4),
            nights,
            rate,
            rate * nights,
            CASE (i % 5)
                WHEN 0 THEN 'DIRECT'
                WHEN 1 THEN 'OTA'
                WHEN 2 THEN 'PHONE'
                WHEN 3 THEN 'EMAIL'
                ELSE 'WALK_IN'
            END,
            CASE (i % 4)
                WHEN 0 THEN 'WEBSITE'
                WHEN 1 THEN 'BOOKING_COM'
                WHEN 2 THEN 'EXPEDIA'
                ELSE 'AIRBNB'
            END,
            CASE
                WHEN check_in_date > CURRENT_DATE THEN 'CONFIRMED'
                WHEN check_out_date < CURRENT_DATE THEN
                    CASE (i % 3)
                        WHEN 0 THEN 'CHECKED_OUT'
                        WHEN 1 THEN 'COMPLETED'
                        ELSE 'NO_SHOW'
                    END
                ELSE 'CHECKED_IN'
            END,
            CASE (i % 4)
                WHEN 0 THEN 'PAID'
                WHEN 1 THEN 'PENDING'
                WHEN 2 THEN 'PARTIAL'
                ELSE 'PAID'
            END,
            CASE (i % 5)
                WHEN 0 THEN 'Late check-in requested'
                WHEN 1 THEN 'High floor preferred'
                WHEN 2 THEN 'Extra pillows needed'
                ELSE NULL
            END,
            'Sample reservation notes',
            user_id,
            user_id,
            CURRENT_TIMESTAMP - ((i % 40) || ' days')::INTERVAL,
            CURRENT_TIMESTAMP - ((i % 30) || ' days')::INTERVAL
        );

        -- Add room assignment
        INSERT INTO reservation_room_assignments (
            id, reservation_id, room_id, check_in_date, check_out_date,
            assignment_status, assigned_by, created_at, updated_at
        ) VALUES (
            gen_random_uuid(),
            reservation_id,
            room_id,
            check_in_date,
            check_out_date,
            CASE
                WHEN check_in_date > CURRENT_DATE THEN 'ASSIGNED'
                WHEN check_out_date < CURRENT_DATE THEN 'COMPLETED'
                ELSE 'CHECKED_IN'
            END,
            user_id,
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        );

        -- Add charges (at least room charge)
        INSERT INTO reservation_charges (
            id, reservation_id, charge_type, charge_description,
            amount, charge_date, posted_by, created_at
        ) VALUES (
            gen_random_uuid(),
            reservation_id,
            'ROOM',
            'Room charge for ' || nights || ' nights',
            rate * nights,
            check_in_date,
            user_id,
            CURRENT_TIMESTAMP
        );

        -- Add extra charges (30% of reservations)
        IF i % 3 = 0 THEN
            INSERT INTO reservation_charges (
                id, reservation_id, charge_type, charge_description,
                amount, charge_date, posted_by, created_at
            ) VALUES (
                gen_random_uuid(),
                reservation_id,
                CASE (i % 4)
                    WHEN 0 THEN 'FOOD'
                    WHEN 1 THEN 'MINIBAR'
                    WHEN 2 THEN 'SPA'
                    ELSE 'PARKING'
                END,
                'Additional service charge',
                25.00 + ((i % 10) * 5),
                check_in_date + INTERVAL '1 day',
                user_id,
                CURRENT_TIMESTAMP
            );
        END IF;
    END LOOP;
END $$;

-- Add reservation status history (300 records)
DO $$
DECLARE
    reservation_rec RECORD;
    user_id UUID;
    counter INTEGER := 0;
BEGIN
    SELECT id INTO user_id FROM users WHERE is_active = true ORDER BY RANDOM() LIMIT 1;

    FOR reservation_rec IN (SELECT id, reservation_status FROM reservations ORDER BY id LIMIT 150) LOOP
        counter := counter + 1;

        -- Add initial booking status
        INSERT INTO reservation_status_history (
            id, reservation_id, from_status, to_status,
            changed_by, change_reason, changed_at, created_at
        ) VALUES (
            gen_random_uuid(),
            reservation_rec.id,
            NULL,
            'PENDING',
            user_id,
            'Initial booking',
            CURRENT_TIMESTAMP - ((counter % 40) || ' days')::INTERVAL,
            CURRENT_TIMESTAMP
        );

        -- Add confirmation status
        INSERT INTO reservation_status_history (
            id, reservation_id, from_status, to_status,
            changed_by, change_reason, changed_at, created_at
        ) VALUES (
            gen_random_uuid(),
            reservation_rec.id,
            'PENDING',
            'CONFIRMED',
            user_id,
            'Payment received',
            CURRENT_TIMESTAMP - ((counter % 35) || ' days')::INTERVAL,
            CURRENT_TIMESTAMP
        );
    END LOOP;
END $$;

-- Add reservation modifications (50 modifications)
DO $$
DECLARE
    reservation_rec RECORD;
    user_id UUID;
    counter INTEGER := 0;
BEGIN
    SELECT id INTO user_id FROM users WHERE is_active = true ORDER BY RANDOM() LIMIT 1;

    FOR reservation_rec IN (SELECT id FROM reservations ORDER BY id LIMIT 50) LOOP
        counter := counter + 1;

        INSERT INTO reservation_modifications (
            id, reservation_id, modification_type, previous_value, new_value,
            modified_by, modification_reason, modified_at, created_at
        ) VALUES (
            gen_random_uuid(),
            reservation_rec.id,
            CASE (counter % 3)
                WHEN 0 THEN 'DATE_CHANGE'
                WHEN 1 THEN 'ROOM_CHANGE'
                ELSE 'GUEST_COUNT'
            END,
            '{"old": "value"}',
            '{"new": "value"}',
            user_id,
            'Guest requested change',
            CURRENT_TIMESTAMP - ((counter % 20) || ' days')::INTERVAL,
            CURRENT_TIMESTAMP
        );
    END LOOP;
END $$;
