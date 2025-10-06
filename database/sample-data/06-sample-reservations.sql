-- Sample Reservations with Charges and History
-- Creates 150 reservations across different statuses

DO $$
DECLARE
    v_system_tenant_id UUID := '00000000-0000-0000-0000-000000000001';
    v_system_user_id UUID := '00000000-0000-0000-0000-000000000002';
    v_property_ids UUID[];
    v_room_ids UUID[];
    v_guest_ids UUID[];
    v_rate_plan_ids UUID[];
    v_reservation_id UUID;
    v_reservation_ids UUID[];
    v_check_in DATE;
    v_check_out DATE;
    v_nights INT;
    v_total_amount DECIMAL(10,2);
    i INT;
    statuses TEXT[] := ARRAY['confirmed', 'checked_in', 'checked_out', 'pending', 'cancelled'];
    sources TEXT[] := ARRAY['direct', 'booking_dot_com', 'expedia', 'phone', 'walk_in'];
BEGIN
    -- Get all necessary IDs
    SELECT ARRAY_AGG(id) INTO v_property_ids FROM properties WHERE tenant_id = v_system_tenant_id;
    SELECT ARRAY_AGG(id) INTO v_room_ids FROM rooms WHERE is_active = true LIMIT 150;
    SELECT ARRAY_AGG(id) INTO v_guest_ids FROM guests WHERE tenant_id = v_system_tenant_id LIMIT 100;
    SELECT ARRAY_AGG(id) INTO v_rate_plan_ids FROM rate_plans;

    IF array_length(v_property_ids, 1) IS NULL OR array_length(v_room_ids, 1) IS NULL OR
       array_length(v_guest_ids, 1) IS NULL THEN
        RAISE EXCEPTION 'Properties, rooms, and guests must be created first!';
    END IF;

    -- Create 150 reservations with different statuses and dates
    FOR i IN 1..150 LOOP
        v_reservation_id := uuid_generate_v4();

        -- Distribute reservations across past, present, and future
        -- Keep all dates within partitioned range (October-December 2025)
        IF i <= 50 THEN
            -- Past reservations (checked out) - early October
            v_check_in := DATE '2025-10-01' + (i % 5 || ' days')::INTERVAL;
            v_check_out := v_check_in + ((2 + (i % 3)) || ' days')::INTERVAL;
        ELSIF i <= 100 THEN
            -- Current and recent reservations - mid October to early November
            v_check_in := CURRENT_DATE - ((i % 5) || ' days')::INTERVAL;
            v_check_out := v_check_in + ((2 + (i % 5)) || ' days')::INTERVAL;
        ELSE
            -- Future reservations - rest of November and December
            v_check_in := DATE '2025-11-01' + ((i % 60) || ' days')::INTERVAL;
            v_check_out := v_check_in + ((2 + (i % 5)) || ' days')::INTERVAL;
        END IF;

        v_nights := (v_check_out - v_check_in);
        v_total_amount := (150 + (i * 3.5) + (v_nights * 25))::DECIMAL(10,2);

        -- Insert reservation
        INSERT INTO reservations (
            id, confirmation_number, property_id, room_id, guest_id,
            check_in_date, check_out_date, adults, children,
            total_amount, currency, status, source,
            rate_plan_id, special_requests,
            actual_check_in_time, actual_check_out_time,
            deposit_amount, identification_verified, signature_obtained,
            is_deleted, created_by, updated_by
        ) VALUES (
            v_reservation_id,
            'MR' || LPAD(i::TEXT, 8, '0') || UPPER(SUBSTRING(MD5(random()::TEXT), 1, 2)),
            v_property_ids[1 + ((i - 1) % array_length(v_property_ids, 1))],
            v_room_ids[1 + ((i - 1) % array_length(v_room_ids, 1))],
            v_guest_ids[1 + ((i - 1) % array_length(v_guest_ids, 1))],
            v_check_in,
            v_check_out,
            1 + (i % 3),  -- 1-3 adults
            CASE WHEN i % 4 = 0 THEN 1 + (i % 2) ELSE 0 END,  -- 0-2 children
            v_total_amount,
            'USD',
            CASE
                WHEN i <= 50 THEN 'checked_out'
                WHEN i <= 100 AND i % 10 < 7 THEN 'confirmed'
                WHEN i <= 100 AND i % 10 < 9 THEN 'checked_in'
                WHEN i <= 100 THEN 'cancelled'
                WHEN i % 20 = 0 THEN 'pending'
                ELSE 'confirmed'
            END::reservation_status,
            sources[1 + (i % 5)]::reservation_source,
            CASE WHEN v_rate_plan_ids IS NOT NULL AND array_length(v_rate_plan_ids, 1) > 0
                THEN v_rate_plan_ids[1 + ((i - 1) % array_length(v_rate_plan_ids, 1))]
                ELSE NULL END,
            CASE WHEN i % 5 = 0 THEN 'Early check-in requested' ELSE NULL END,
            CASE WHEN i <= 100 THEN v_check_in + INTERVAL '15 hours' ELSE NULL END,
            CASE WHEN i <= 50 THEN v_check_out + INTERVAL '11 hours' ELSE NULL END,
            CASE WHEN i % 3 = 0 THEN (v_total_amount * 0.20)::DECIMAL(10,2) ELSE 0.00 END,
            (i <= 100),
            (i <= 100),
            false,
            v_system_user_id,
            v_system_user_id
        );

        v_reservation_ids := array_append(v_reservation_ids, v_reservation_id);

        -- Create room charges
        INSERT INTO reservation_charges (
            reservation_id, charge_type, description, amount, currency,
            quantity, unit_price, tax_rate, tax_amount, charge_date, created_by
        ) VALUES (
            v_reservation_id,
            'room',
            'Room charge for ' || v_nights || ' night(s)',
            (v_total_amount * 0.85)::DECIMAL(10,2),
            'USD',
            v_nights,
            ((v_total_amount * 0.85) / v_nights)::DECIMAL(10,2),
            0.0875,
            ((v_total_amount * 0.85) * 0.0875)::DECIMAL(10,2),
            v_check_in,
            v_system_user_id
        );

        -- Add tax charge
        INSERT INTO reservation_charges (
            reservation_id, charge_type, description, amount, currency,
            quantity, unit_price, tax_rate, tax_amount, charge_date, created_by
        ) VALUES (
            v_reservation_id,
            'tax',
            'Room tax',
            ((v_total_amount * 0.85) * 0.0875)::DECIMAL(10,2),
            'USD',
            1,
            ((v_total_amount * 0.85) * 0.0875)::DECIMAL(10,2),
            0.0000,
            0.00,
            v_check_in,
            v_system_user_id
        );

        -- Add service charges for some reservations
        IF i % 3 = 0 THEN
            INSERT INTO reservation_charges (
                reservation_id, charge_type, description, amount, currency,
                quantity, unit_price, tax_rate, tax_amount, charge_date, created_by
            ) VALUES (
                v_reservation_id,
                'service',
                'Resort fee',
                35.00,
                'USD',
                v_nights,
                35.00,
                0.0875,
                (35.00 * 0.0875)::DECIMAL(10,2),
                v_check_in,
                v_system_user_id
            );
        END IF;

        -- Create status history for completed reservations
        IF i <= 100 THEN
            INSERT INTO reservation_status_history (
                reservation_id, old_status, new_status, reason, changed_by
            ) VALUES
                (v_reservation_id, NULL, 'pending', 'New reservation created', v_system_user_id),
                (v_reservation_id, 'pending', 'confirmed', 'Payment confirmed', v_system_user_id);

            IF i <= 50 OR (i <= 100 AND i % 10 >= 7) THEN
                INSERT INTO reservation_status_history (
                    reservation_id, old_status, new_status, reason, changed_by
                ) VALUES
                    (v_reservation_id, 'confirmed', 'checked_in', 'Guest checked in', v_system_user_id);
            END IF;

            IF i <= 50 THEN
                INSERT INTO reservation_status_history (
                    reservation_id, old_status, new_status, reason, changed_by
                ) VALUES
                    (v_reservation_id, 'checked_in', 'checked_out', 'Guest checked out', v_system_user_id);
            END IF;

            IF i > 50 AND i <= 100 AND i % 10 >= 9 THEN
                INSERT INTO reservation_status_history (
                    reservation_id, old_status, new_status, reason, changed_by
                ) VALUES
                    (v_reservation_id, 'confirmed', 'cancelled', 'Guest requested cancellation', v_system_user_id);
            END IF;
        END IF;

        -- Create room assignment
        INSERT INTO reservation_room_assignments (
            reservation_id, room_id, assigned_at, is_current, assigned_by
        ) VALUES (
            v_reservation_id,
            v_room_ids[1 + ((i - 1) % array_length(v_room_ids, 1))],
            CURRENT_TIMESTAMP - ((150 - i) || ' hours')::INTERVAL,
            true,
            v_system_user_id
        );
    END LOOP;

    RAISE NOTICE '150 reservations created';
    RAISE NOTICE '- Past (checked_out): 50';
    RAISE NOTICE '- Current/Recent (various): 50';
    RAISE NOTICE '- Future (confirmed): 50';
    RAISE NOTICE 'Total charges: %', (SELECT COUNT(*) FROM reservation_charges);
    RAISE NOTICE 'Total status history records: %', (SELECT COUNT(*) FROM reservation_status_history);
END $$;
