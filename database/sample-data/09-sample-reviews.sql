-- Sample Reviews Data
-- This file contains sample data for guest reviews

-- Sample Reviews (80 reviews)
DO $$
DECLARE
    reservation_rec RECORD;
    counter INTEGER := 0;
    review_titles TEXT[] := ARRAY[
        'Excellent Stay!',
        'Great Experience',
        'Wonderful Hotel',
        'Perfect Location',
        'Amazing Service',
        'Good Value',
        'Nice and Clean',
        'Comfortable Rooms',
        'Highly Recommend',
        'Will Visit Again'
    ];
    review_comments TEXT[] := ARRAY[
        'The staff was incredibly friendly and helpful throughout our entire stay.',
        'Room was spacious and clean with all modern amenities.',
        'Great location with easy access to local attractions.',
        'The breakfast buffet was excellent with lots of variety.',
        'Check-in and check-out process was smooth and efficient.',
        'Hotel facilities were well-maintained and modern.',
        'Comfortable beds and quiet environment for a good night sleep.',
        'Beautiful property with great attention to detail.',
        'Value for money was outstanding compared to other hotels.',
        'Would definitely recommend this hotel to friends and family.'
    ];
BEGIN
    FOR reservation_rec IN (
        SELECT r.id as reservation_id, r.guest_id, r.property_id, r.check_out_date
        FROM reservations r
        WHERE r.reservation_status IN ('CHECKED_OUT', 'COMPLETED')
        AND r.check_out_date < CURRENT_DATE
        ORDER BY r.id
        LIMIT 80
    ) LOOP
        counter := counter + 1;

        INSERT INTO reviews (
            id, reservation_id, guest_id, property_id,
            overall_rating, cleanliness_rating, service_rating,
            location_rating, value_rating, amenities_rating,
            review_title, review_text, pros, cons,
            would_recommend, is_verified, is_approved, is_featured,
            helpful_count, management_response, responded_at,
            created_at, updated_at
        ) VALUES (
            gen_random_uuid(),
            reservation_rec.reservation_id,
            reservation_rec.guest_id,
            reservation_rec.property_id,
            3.0 + ((counter % 3) * 0.5) + (CASE WHEN counter % 7 = 0 THEN 1.0 ELSE 0.5 END),  -- 3.5 to 5.0 stars
            3.0 + ((counter % 4) * 0.5),
            3.5 + ((counter % 3) * 0.5),
            4.0 + ((counter % 3) * 0.3),
            3.0 + ((counter % 5) * 0.5),
            3.5 + ((counter % 4) * 0.5),
            review_titles[((counter - 1) % 10) + 1],
            review_comments[((counter - 1) % 10) + 1] || ' ' ||
            review_comments[((counter) % 10) + 1],
            CASE (counter % 3)
                WHEN 0 THEN 'Clean rooms, friendly staff, great location'
                WHEN 1 THEN 'Excellent service, comfortable beds, good breakfast'
                ELSE 'Modern facilities, convenient parking, quiet environment'
            END,
            CASE (counter % 5)
                WHEN 0 THEN 'WiFi could be faster'
                WHEN 1 THEN 'Pool was a bit small'
                WHEN 2 THEN 'Parking fee was extra'
                ELSE NULL
            END,
            (counter % 7 != 0),  -- Most guests would recommend
            true,
            (counter % 10 != 0),  -- 90% approved
            (counter % 15 = 0),  -- 1 in 15 featured
            (counter % 8) + ((counter % 5) * 2),  -- Helpful count
            CASE
                WHEN counter % 4 = 0 THEN 'Thank you for your wonderful review! We are delighted to hear about your positive experience and look forward to welcoming you back soon.'
                ELSE NULL
            END,
            CASE
                WHEN counter % 4 = 0 THEN reservation_rec.check_out_date + INTERVAL '3 days'
                ELSE NULL
            END,
            reservation_rec.check_out_date + ((1 + (counter % 5)) || ' days')::INTERVAL,
            reservation_rec.check_out_date + ((1 + (counter % 5)) || ' days')::INTERVAL
        );
    END LOOP;
END $$;
