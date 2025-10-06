-- Sample Guest Reviews
-- Creates reviews for checked-out reservations

DO $$
DECLARE
    v_reservation_data RECORD;
    i INT := 0;
    review_titles TEXT[] := ARRAY[
        'Excellent stay!', 'Great experience', 'Wonderful hotel', 'Perfect getaway',
        'Highly recommend', 'Amazing service', 'Beautiful property', 'Very comfortable',
        'Outstanding hospitality', 'Will return soon', 'Exceeded expectations',
        'Fantastic location', 'Clean and modern', 'Lovely atmosphere', 'Top notch',
        'Impressive amenities', 'Superb quality', 'Delightful stay', 'Very pleased',
        'Memorable experience'
    ];
    review_comments TEXT[] := ARRAY[
        'The staff was incredibly friendly and helpful throughout our stay. The room was clean and comfortable.',
        'Beautiful property with excellent amenities. The location was perfect for exploring the city.',
        'We had a wonderful time. The room was spacious and the bed was very comfortable. Would definitely stay again.',
        'Great value for money. The breakfast was delicious and the pool area was well-maintained.',
        'The hotel exceeded our expectations in every way. Special thanks to the front desk staff.',
        'Perfect for a weekend getaway. The spa services were exceptional and very relaxing.',
        'Clean, modern, and well-appointed rooms. The view from our balcony was stunning.',
        'Excellent service from check-in to check-out. The concierge was particularly helpful.',
        'The room was exactly as described. Great amenities and very quiet despite being in the city center.',
        'Loved everything about this hotel. The restaurant had great food and the staff was attentive.'
    ];
    management_responses TEXT[] := ARRAY[
        'Thank you so much for your wonderful review! We''re thrilled you enjoyed your stay.',
        'We appreciate your kind words! Our team works hard to provide exceptional service.',
        'Thank you for choosing our hotel. We look forward to welcoming you back soon!',
        'We''re so glad you had a great experience. Your feedback means a lot to us.',
        'Thank you for the 5-star review! We can''t wait to host you again.'
    ];
BEGIN
    -- Create reviews for checked-out reservations
    FOR v_reservation_data IN
        SELECT r.id, r.guest_id, r.property_id, r.check_out_date
        FROM reservations r
        WHERE r.status = 'checked_out'
        ORDER BY r.check_out_date DESC
        LIMIT 80
    LOOP
        i := i + 1;

        INSERT INTO reviews (
            reservation_id, guest_id, property_id,
            overall_rating,
            cleanliness_rating,
            service_rating,
            location_rating,
            value_rating,
            amenities_rating,
            title,
            comment,
            is_verified,
            is_published,
            response,
            responded_by,
            responded_at
        ) VALUES (
            v_reservation_data.id,
            v_reservation_data.guest_id,
            v_reservation_data.property_id,
            -- Overall rating between 3 and 5 (weighted toward higher)
            CASE
                WHEN i % 10 <= 7 THEN 5
                WHEN i % 10 = 8 THEN 4
                ELSE 3
            END,
            -- Individual ratings
            3 + (RANDOM() * 2)::INT,  -- 3-5
            3 + (RANDOM() * 2)::INT,
            3 + (RANDOM() * 2)::INT,
            3 + (RANDOM() * 2)::INT,
            3 + (RANDOM() * 2)::INT,
            -- Title and comment
            review_titles[1 + (i % 20)],
            review_comments[1 + (i % 10)] ||
                CASE
                    WHEN i % 5 = 0 THEN ' The check-in process was smooth and efficient.'
                    WHEN i % 7 = 0 THEN ' Great attention to detail throughout the property.'
                    ELSE ''
                END,
            true,  -- Verified (all are verified)
            true,  -- Published
            -- Management response (70% get responses)
            CASE
                WHEN i % 10 <= 7 THEN management_responses[1 + (i % 5)]
                ELSE NULL
            END,
            -- Responded by (system user if there's a response)
            CASE
                WHEN i % 10 <= 7 THEN '00000000-0000-0000-0000-000000000002'::UUID
                ELSE NULL
            END,
            -- Response timestamp
            CASE
                WHEN i % 10 <= 7 THEN v_reservation_data.check_out_date + ((1 + (i % 3)) || ' days')::INTERVAL
                ELSE NULL
            END
        );
    END LOOP;

    RAISE NOTICE '% guest reviews created for checked-out reservations', i;
    RAISE NOTICE 'Average rating: %', (
        SELECT ROUND(AVG(overall_rating)::NUMERIC, 2)
        FROM reviews
    );
    RAISE NOTICE 'Reviews with management response: %', (
        SELECT COUNT(*)
        FROM reviews
        WHERE response IS NOT NULL
    );
END $$;
