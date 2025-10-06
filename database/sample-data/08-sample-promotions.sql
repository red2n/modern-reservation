-- Sample Promotions and Notifications Data
-- This file contains sample data for promotions, promotion usage, and notification history

-- Sample Promotions (20 promotions)
DO $$
DECLARE
    property_ids UUID[];
    property_id UUID;
    i INTEGER;
BEGIN
    SELECT ARRAY_AGG(id) INTO property_ids FROM properties;

    FOR i IN 1..20 LOOP
        property_id := CASE
            WHEN i % 3 = 0 THEN NULL  -- Global promotions
            ELSE property_ids[((i - 1) % ARRAY_LENGTH(property_ids, 1)) + 1]
        END;

        INSERT INTO promotions (
            id, property_id, promotion_code, promotion_name, description,
            promotion_type, discount_type, discount_value, minimum_nights,
            minimum_amount, maximum_discount, start_date, end_date,
            booking_start_date, booking_end_date, is_combinable,
            usage_limit, usage_count, is_active, created_at, updated_at
        ) VALUES (
            gen_random_uuid(),
            property_id,
            'PROMO' || LPAD(i::TEXT, 4, '0'),
            CASE (i % 5)
                WHEN 0 THEN 'Summer Sale'
                WHEN 1 THEN 'Weekend Special'
                WHEN 2 THEN 'Extended Stay Discount'
                WHEN 3 THEN 'Early Bird Special'
                ELSE 'Last Minute Deal'
            END || ' ' || i::TEXT,
            'Special promotional offer - limited time only',
            CASE (i % 3)
                WHEN 0 THEN 'SEASONAL'
                WHEN 1 THEN 'LOYALTY'
                ELSE 'PROMOTIONAL'
            END,
            CASE (i % 2)
                WHEN 0 THEN 'PERCENTAGE'
                ELSE 'FIXED_AMOUNT'
            END,
            CASE (i % 2)
                WHEN 0 THEN 10.00 + (i % 5) * 5  -- 10%, 15%, 20%, etc.
                ELSE 20.00 + (i % 5) * 10  -- $20, $30, $40, etc.
            END,
            CASE (i % 4)
                WHEN 0 THEN 2
                WHEN 1 THEN 3
                ELSE NULL
            END,
            CASE (i % 3)
                WHEN 0 THEN 100.00
                WHEN 1 THEN 200.00
                ELSE NULL
            END,
            CASE (i % 2)
                WHEN 0 THEN 50.00
                ELSE NULL
            END,
            CURRENT_DATE - ((i % 30) || ' days')::INTERVAL,
            CURRENT_DATE + ((60 + (i % 30)) || ' days')::INTERVAL,
            CURRENT_DATE - ((i % 30) || ' days')::INTERVAL,
            CURRENT_DATE + ((30 + (i % 30)) || ' days')::INTERVAL,
            (i % 3 != 0),
            CASE (i % 4)
                WHEN 0 THEN 100
                WHEN 1 THEN 500
                WHEN 2 THEN 1000
                ELSE NULL
            END,
            (i * 3) % 50,
            (i % 5 != 0),
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        );
    END LOOP;
END $$;

-- Sample Promotion Usage (50 usage records)
DO $$
DECLARE
    reservation_rec RECORD;
    promotion_rec RECORD;
    counter INTEGER := 0;
BEGIN
    FOR reservation_rec IN (
        SELECT r.id as reservation_id, r.guest_id, r.total_amount
        FROM reservations r
        WHERE r.reservation_status IN ('CONFIRMED', 'CHECKED_IN')
        ORDER BY r.id
        LIMIT 50
    ) LOOP
        counter := counter + 1;

        SELECT * INTO promotion_rec
        FROM promotions
        WHERE is_active = true
        ORDER BY RANDOM()
        LIMIT 1;

        IF promotion_rec.id IS NOT NULL THEN
            INSERT INTO promotion_usage (
                id, promotion_id, reservation_id, guest_id,
                discount_amount, used_at, created_at
            ) VALUES (
                gen_random_uuid(),
                promotion_rec.id,
                reservation_rec.reservation_id,
                reservation_rec.guest_id,
                CASE promotion_rec.discount_type
                    WHEN 'PERCENTAGE' THEN reservation_rec.total_amount * (promotion_rec.discount_value / 100)
                    ELSE LEAST(promotion_rec.discount_value, reservation_rec.total_amount)
                END,
                CURRENT_TIMESTAMP - ((counter % 30) || ' days')::INTERVAL,
                CURRENT_TIMESTAMP
            );

            -- Update promotion usage count
            UPDATE promotions
            SET usage_count = usage_count + 1
            WHERE id = promotion_rec.id;
        END IF;
    END LOOP;
END $$;

-- Sample Notification History (200 notifications)
DO $$
DECLARE
    guest_ids UUID[];
    template_ids UUID[];
    guest_id UUID;
    template_id UUID;
    i INTEGER;
BEGIN
    SELECT ARRAY_AGG(id) INTO guest_ids FROM guests;
    SELECT ARRAY_AGG(id) INTO template_ids FROM notification_templates;

    IF template_ids IS NOT NULL AND ARRAY_LENGTH(template_ids, 1) > 0 THEN
        FOR i IN 1..200 LOOP
            guest_id := guest_ids[((i - 1) % ARRAY_LENGTH(guest_ids, 1)) + 1];
            template_id := template_ids[((i - 1) % ARRAY_LENGTH(template_ids, 1)) + 1];

            INSERT INTO notification_history (
                id, template_id, recipient_type, recipient_id, channel,
                recipient_email, recipient_phone, subject, message,
                notification_status, sent_at, delivered_at, opened_at,
                error_message, retry_count, metadata, created_at
            ) VALUES (
                gen_random_uuid(),
                template_id,
                'GUEST',
                guest_id,
                CASE (i % 3)
                    WHEN 0 THEN 'EMAIL'
                    WHEN 1 THEN 'SMS'
                    ELSE 'PUSH'
                END,
                CASE WHEN i % 3 = 0 THEN 'guest' || i::TEXT || '@email.com' ELSE NULL END,
                CASE WHEN i % 3 = 1 THEN '+1-555-' || LPAD(i::TEXT, 7, '0') ELSE NULL END,
                CASE (i % 5)
                    WHEN 0 THEN 'Booking Confirmation'
                    WHEN 1 THEN 'Check-in Reminder'
                    WHEN 2 THEN 'Thank You for Your Stay'
                    WHEN 3 THEN 'Special Offer'
                    ELSE 'Feedback Request'
                END,
                'Sample notification message for guest',
                CASE (i % 10)
                    WHEN 0 THEN 'PENDING'
                    WHEN 1 THEN 'FAILED'
                    ELSE 'DELIVERED'
                END,
                CURRENT_TIMESTAMP - ((i % 60) || ' days')::INTERVAL,
                CASE WHEN i % 10 NOT IN (0, 1) THEN CURRENT_TIMESTAMP - ((i % 60) || ' days ' || (i % 60) || ' minutes')::INTERVAL ELSE NULL END,
                CASE WHEN i % 10 NOT IN (0, 1) AND i % 3 = 0 THEN CURRENT_TIMESTAMP - ((i % 60) || ' days ' || (i % 120) || ' minutes')::INTERVAL ELSE NULL END,
                CASE WHEN i % 10 = 1 THEN 'Delivery failed: Invalid address' ELSE NULL END,
                i % 3,
                '{"source": "system", "priority": "normal"}',
                CURRENT_TIMESTAMP
            );
        END LOOP;
    END IF;
END $$;
