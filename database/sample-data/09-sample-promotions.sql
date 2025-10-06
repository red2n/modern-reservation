-- Sample Promotions and Usage
-- Creates promotional offers and tracks their usage

DO $$
DECLARE
    v_system_user_id UUID := '00000000-0000-0000-0000-000000000002';
    v_property_ids UUID[];
    v_promotion_id UUID;
    v_reservation_ids UUID[];
    v_guest_ids UUID[];
    i INT;
    j INT;
    promo_types TEXT[] := ARRAY['percentage', 'fixed_amount', 'free_nights'];
    promo_names TEXT[] := ARRAY[
        'Summer Sale', 'Early Bird Special', 'Last Minute Deal', 'Weekend Getaway',
        'Extended Stay Discount', 'Flash Sale', 'Birthday Special', 'Corporate Rate',
        'Military Discount', 'Senior Citizen', 'Student Discount', 'Loyalty Reward',
        'Referral Bonus', 'New Guest Welcome', 'Anniversary Special', 'Holiday Special',
        'Black Friday Deal', 'Cyber Monday', 'Spring Break', 'Winter Escape'
    ];
BEGIN
    -- Get all property IDs, reservations, and guests
    SELECT ARRAY_AGG(id) INTO v_property_ids
    FROM properties
    WHERE tenant_id = '00000000-0000-0000-0000-000000000001';

    SELECT ARRAY_AGG(id) INTO v_reservation_ids
    FROM reservations
    WHERE status != 'cancelled'
    LIMIT 50;

    SELECT ARRAY_AGG(id) INTO v_guest_ids
    FROM guests
    WHERE tenant_id = '00000000-0000-0000-0000-000000000001'
    LIMIT 50;

    IF array_length(v_property_ids, 1) IS NULL THEN
        RAISE EXCEPTION 'Properties must be created first!';
    END IF;

    -- Create 20 promotions (2 per property)
    FOR i IN 1..array_length(v_property_ids, 1) LOOP
        -- Percentage discount promotion
        INSERT INTO promotions (
            property_id, promotion_code, name, description,
            valid_from, valid_to,
            discount_type, discount_value, max_discount_amount,
            max_uses, uses_per_guest, current_uses,
            min_nights, advance_booking_days, min_amount,
            applies_to_room_types, applies_to_rate_plans, blackout_dates,
            is_active, is_combinable,
            created_by, updated_by
        ) VALUES (
            v_property_ids[i],
            'SAVE' || (i * 10) || UPPER(SUBSTRING(MD5(random()::TEXT), 1, 3)),
            promo_names[i],
            'Save ' || (10 + (i * 2)) || '% on your stay with code',
            CURRENT_DATE - INTERVAL '30 days',
            CURRENT_DATE + INTERVAL '90 days',
            'percentage',
            (10 + (i * 2))::DECIMAL(10,2),
            200.00,
            100 + (i * 10),
            3,
            (i * 2),
            CASE WHEN i % 3 = 0 THEN 2 ELSE 1 END,
            CASE WHEN i % 2 = 0 THEN 7 ELSE 0 END,
            CASE WHEN i % 4 = 0 THEN 150.00 ELSE NULL END,
            '[]'::jsonb,  -- Applies to all room types
            '[]'::jsonb,  -- Applies to all rate plans
            '[]'::jsonb,  -- No blackout dates
            true,
            false,
            v_system_user_id,
            v_system_user_id
        ) RETURNING id INTO v_promotion_id;

        -- Create usage records for this promotion
        IF v_reservation_ids IS NOT NULL AND array_length(v_reservation_ids, 1) >= i * 2 THEN
            FOR j IN 1..LEAST(3, i) LOOP  -- 1-3 uses per promotion
                IF (i * 2 + j) <= array_length(v_reservation_ids, 1) AND
                   (i * 2 + j) <= array_length(v_guest_ids, 1) THEN
                    INSERT INTO promotion_usage (
                        promotion_id, reservation_id, guest_id,
                        discount_amount, currency
                    ) VALUES (
                        v_promotion_id,
                        v_reservation_ids[i * 2 + j],
                        v_guest_ids[i * 2 + j],
                        (25 + (i * 3) + (j * 5))::DECIMAL(10,2),
                        'USD'
                    );

                    -- Update current_uses
                    UPDATE promotions
                    SET current_uses = current_uses + 1
                    WHERE id = v_promotion_id;
                END IF;
            END LOOP;
        END IF;

        -- Fixed amount promotion
        INSERT INTO promotions (
            property_id, promotion_code, name, description,
            valid_from, valid_to,
            discount_type, discount_value, max_discount_amount,
            max_uses, uses_per_guest, current_uses,
            min_nights, advance_booking_days, min_amount,
            applies_to_room_types, applies_to_rate_plans, blackout_dates,
            is_active, is_combinable,
            created_by, updated_by
        ) VALUES (
            v_property_ids[i],
            'FIXED' || (i * 10) || UPPER(SUBSTRING(MD5(random()::TEXT), 1, 3)),
            promo_names[10 + i],
            'Get $' || (30 + (i * 5)) || ' off your next stay',
            CURRENT_DATE - INTERVAL '15 days',
            CURRENT_DATE + INTERVAL '60 days',
            'fixed_amount',
            (30 + (i * 5))::DECIMAL(10,2),
            NULL,
            50 + (i * 5),
            2,
            (i),
            1,
            0,
            100.00,
            '[]'::jsonb,
            '[]'::jsonb,
            '[]'::jsonb,
            true,
            true,  -- Can be combined with other offers
            v_system_user_id,
            v_system_user_id
        );
    END LOOP;

    RAISE NOTICE '20 promotions created (2 per property)';
    RAISE NOTICE 'Total promotion usage records: %', (SELECT COUNT(*) FROM promotion_usage);
END $$;
