-- Sample Rate Plans and Rates
-- Creates rate plans, rate_plan_rates, and seasonal rates

DO $$
DECLARE
    v_system_tenant_id UUID := '00000000-0000-0000-0000-000000000001';
    v_system_user_id UUID := '00000000-0000-0000-0000-000000000002';
    v_property_ids UUID[];
    v_room_ids UUID[];
    v_rate_plan_id UUID;
    v_rate_plan_ids UUID[];
    i INT;
    j INT;
    k INT;
    current_date_iter DATE;
BEGIN
    -- Get all property IDs and room IDs
    SELECT ARRAY_AGG(id) INTO v_property_ids FROM properties WHERE tenant_id = v_system_tenant_id;
    SELECT ARRAY_AGG(id) INTO v_room_ids FROM rooms LIMIT 150;

    IF array_length(v_property_ids, 1) IS NULL THEN
        RAISE EXCEPTION 'Properties must be created first!';
    END IF;

    IF array_length(v_room_ids, 1) IS NULL THEN
        RAISE EXCEPTION 'Rooms must be created first!';
    END IF;

    -- Create 4 rate plans per property (40 total)
    FOR i IN 1..array_length(v_property_ids, 1) LOOP
        -- Standard Rate Plan
        INSERT INTO rate_plans (
            property_id, name, description, rate_plan_code,
            cancellation_policy, advance_booking_days, min_stay, max_stay,
            discount_percent, is_refundable, requires_deposit, deposit_percent,
            is_active, created_by, updated_by
        ) VALUES (
            v_property_ids[i],
            'Standard Rate',
            'Our standard flexible rate with free cancellation',
            'STD' || i,
            'Free cancellation up to 24 hours before check-in',
            0, 1, 30,
            0.00, true, false, 0.00,
            true, v_system_user_id, v_system_user_id
        ) RETURNING id INTO v_rate_plan_id;
        v_rate_plan_ids := array_append(v_rate_plan_ids, v_rate_plan_id);

        -- Early Bird Rate Plan
        INSERT INTO rate_plans (
            property_id, name, description, rate_plan_code,
            cancellation_policy, advance_booking_days, min_stay, max_stay,
            discount_percent, is_refundable, requires_deposit, deposit_percent,
            is_active, created_by, updated_by
        ) VALUES (
            v_property_ids[i],
            'Early Bird Special',
            'Save 15% when you book 14 days in advance',
            'EARLY' || i,
            'Non-refundable',
            14, 1, 30,
            15.00, false, true, 50.00,
            true, v_system_user_id, v_system_user_id
        ) RETURNING id INTO v_rate_plan_id;
        v_rate_plan_ids := array_append(v_rate_plan_ids, v_rate_plan_id);

        -- Weekend Getaway Rate Plan
        INSERT INTO rate_plans (
            property_id, name, description, rate_plan_code,
            cancellation_policy, advance_booking_days, min_stay, max_stay,
            discount_percent, is_refundable, requires_deposit, deposit_percent,
            is_active, created_by, updated_by
        ) VALUES (
            v_property_ids[i],
            'Weekend Getaway',
            'Special weekend rate for 2+ night stays',
            'WKND' || i,
            'Free cancellation up to 48 hours before check-in',
            0, 2, 7,
            10.00, true, false, 0.00,
            true, v_system_user_id, v_system_user_id
        ) RETURNING id INTO v_rate_plan_id;
        v_rate_plan_ids := array_append(v_rate_plan_ids, v_rate_plan_id);

        -- Extended Stay Rate Plan
        INSERT INTO rate_plans (
            property_id, name, description, rate_plan_code,
            cancellation_policy, advance_booking_days, min_stay, max_stay,
            discount_percent, is_refundable, requires_deposit, deposit_percent,
            is_active, created_by, updated_by
        ) VALUES (
            v_property_ids[i],
            'Extended Stay',
            'Best value for stays of 7+ nights',
            'EXT' || i,
            'Free cancellation up to 7 days before check-in',
            7, 7, 90,
            20.00, true, true, 25.00,
            true, v_system_user_id, v_system_user_id
        ) RETURNING id INTO v_rate_plan_id;
        v_rate_plan_ids := array_append(v_rate_plan_ids, v_rate_plan_id);
    END LOOP;

    RAISE NOTICE '40 rate plans created (4 per property)';

    -- Create rate_plan_rates for next 60 days for each rate plan and room combination
    -- To keep it manageable, we'll create rates for 5 rooms per rate plan
    FOR i IN 1..array_length(v_rate_plan_ids, 1) LOOP
        FOR j IN 1..5 LOOP  -- 5 rooms per rate plan
            IF ((i-1) * 5 + j) <= array_length(v_room_ids, 1) THEN
                current_date_iter := CURRENT_DATE;
                FOR k IN 1..60 LOOP  -- 60 days of rates
                    INSERT INTO rate_plan_rates (
                        rate_plan_id,
                        room_id,
                        date,
                        rate,
                        currency,
                        min_stay,
                        max_stay,
                        closed_to_arrival,
                        closed_to_departure,
                        created_by,
                        updated_by
                    ) VALUES (
                        v_rate_plan_ids[i],
                        v_room_ids[((i-1) * 5 + j)],
                        current_date_iter,
                        (100 + (i * 10) + (j * 5) + (EXTRACT(DOW FROM current_date_iter) * 10))::DECIMAL(10,2),
                        'USD',
                        1,
                        30,
                        false,
                        false,
                        v_system_user_id,
                        v_system_user_id
                    );
                    current_date_iter := current_date_iter + INTERVAL '1 day';
                END LOOP;
            END IF;
        END LOOP;
    END LOOP;

    RAISE NOTICE 'Rate plan rates created for next 60 days';

    -- Create seasonal rates for each property
    FOR i IN 1..array_length(v_property_ids, 1) LOOP
        -- Summer Season (June-August)
        INSERT INTO seasonal_rates (
            property_id, name, start_date, end_date,
            adjustment_type, adjustment_value,
            days_of_week, min_nights, priority,
            is_active, created_by, updated_by
        ) VALUES (
            v_property_ids[i],
            'Summer Peak Season',
            DATE_TRUNC('year', CURRENT_DATE)::DATE + INTERVAL '5 months',  -- June 1
            DATE_TRUNC('year', CURRENT_DATE)::DATE + INTERVAL '8 months' - INTERVAL '1 day',  -- Aug 31
            'percentage',
            25.00,
            '[1,2,3,4,5,6,7]'::jsonb,
            1,
            1,
            true,
            v_system_user_id,
            v_system_user_id
        );

        -- Holiday Season (December-January)
        INSERT INTO seasonal_rates (
            property_id, name, start_date, end_date,
            adjustment_type, adjustment_value,
            days_of_week, min_nights, priority,
            is_active, created_by, updated_by
        ) VALUES (
            v_property_ids[i],
            'Holiday Season',
            DATE_TRUNC('year', CURRENT_DATE)::DATE + INTERVAL '11 months',  -- Dec 1
            DATE_TRUNC('year', CURRENT_DATE)::DATE + INTERVAL '13 months' - INTERVAL '1 day',  -- Jan 31 next year
            'percentage',
            35.00,
            '[1,2,3,4,5,6,7]'::jsonb,
            2,
            2,
            true,
            v_system_user_id,
            v_system_user_id
        );

        -- Weekend Surcharge
        INSERT INTO seasonal_rates (
            property_id, name, start_date, end_date,
            adjustment_type, adjustment_value,
            days_of_week, min_nights, priority,
            is_active, created_by, updated_by
        ) VALUES (
            v_property_ids[i],
            'Weekend Premium',
            CURRENT_DATE,
            CURRENT_DATE + INTERVAL '1 year',
            'fixed_amount',
            25.00,
            '[5,6,7]'::jsonb,  -- Friday, Saturday, Sunday
            1,
            3,
            true,
            v_system_user_id,
            v_system_user_id
        );
    END LOOP;

    RAISE NOTICE '% seasonal rates created', (SELECT COUNT(*) FROM seasonal_rates);
    RAISE NOTICE 'Total rate plans: %', array_length(v_rate_plan_ids, 1);
    RAISE NOTICE 'Total rate_plan_rates: %', (SELECT COUNT(*) FROM rate_plan_rates);
END $$;
