-- Sample Room Availability and Channel Availability
-- Creates availability records for the next 60 days

DO $$
DECLARE
    v_system_user_id UUID := '00000000-0000-0000-0000-000000000002';
    v_room_ids UUID[];
    v_room_id UUID;
    current_date_iter DATE;
    i INT;
    j INT;
    channels TEXT[] := ARRAY['booking_com', 'expedia', 'airbnb', 'direct'];
BEGIN
    -- Get all active room IDs
    SELECT ARRAY_AGG(id) INTO v_room_ids FROM rooms WHERE is_active = true;

    IF array_length(v_room_ids, 1) IS NULL THEN
        RAISE EXCEPTION 'Rooms must be created first!';
    END IF;

    RAISE NOTICE 'Creating availability for % rooms', array_length(v_room_ids, 1);

    -- Create room_availability for next 60 days for each room
    FOR i IN 1..array_length(v_room_ids, 1) LOOP
        v_room_id := v_room_ids[i];
        current_date_iter := CURRENT_DATE;

        FOR j IN 1..60 LOOP
            INSERT INTO room_availability (
                room_id, date, is_available, available_units,
                min_stay, max_stay,
                closed_to_arrival, closed_to_departure,
                base_price, currency,
                created_by, updated_by
            ) VALUES (
                v_room_id,
                current_date_iter,
                -- 90% of days available, 10% not available
                (RANDOM() > 0.10),
                1,
                CASE
                    WHEN EXTRACT(DOW FROM current_date_iter) IN (5, 6) THEN 2  -- Weekend min stay
                    ELSE 1
                END,
                30,
                false,
                false,
                (120 + (i * 2) + (EXTRACT(DOW FROM current_date_iter) * 10) + (j * 0.5))::DECIMAL(10,2),
                'USD',
                v_system_user_id,
                v_system_user_id
            );

            current_date_iter := current_date_iter + INTERVAL '1 day';
        END LOOP;
    END LOOP;

    RAISE NOTICE 'Room availability created: %', (SELECT COUNT(*) FROM room_availability);

    -- Create channel_availability for a subset of rooms (first 50 rooms, 4 channels each)
    FOR i IN 1..LEAST(50, array_length(v_room_ids, 1)) LOOP
        v_room_id := v_room_ids[i];

        FOR k IN 1..4 LOOP  -- 4 channels
            current_date_iter := CURRENT_DATE;

            FOR j IN 1..30 LOOP  -- 30 days per channel
                INSERT INTO channel_availability (
                    room_id, channel_id, date, available_units,
                    rate, currency,
                    min_stay, max_stay,
                    closed_to_arrival, closed_to_departure
                ) VALUES (
                    v_room_id,
                    channels[k],
                    current_date_iter,
                    CASE WHEN RANDOM() > 0.15 THEN 1 ELSE 0 END,  -- 85% available
                    (125 + (i * 2) + (k * 5) + (EXTRACT(DOW FROM current_date_iter) * 8))::DECIMAL(10,2),
                    'USD',
                    CASE
                        WHEN EXTRACT(DOW FROM current_date_iter) IN (5, 6) THEN 2
                        ELSE 1
                    END,
                    30,
                    false,
                    false
                );

                current_date_iter := current_date_iter + INTERVAL '1 day';
            END LOOP;
        END LOOP;
    END LOOP;

    RAISE NOTICE 'Channel availability created: %', (SELECT COUNT(*) FROM channel_availability);
END $$;
