-- Sample Data for Reservations Table
-- Generates 500 realistic reservation records with various statuses and sources

INSERT INTO reservations (
    id, confirmation_number, property_id, guest_id,
    guest_first_name, guest_last_name, guest_email, guest_phone,
    check_in_date, check_out_date, nights,
    room_type_id, room_number,
    adults, children, infants,
    room_rate, taxes, fees, total_amount, currency,
    status, source,
    special_requests, internal_notes,
    booking_date, arrival_time, departure_time
)
SELECT
    gen_random_uuid(),
    'RES-' || TO_CHAR(CURRENT_DATE, 'YYYYMMDD') || '-' || LPAD(s::text, 6, '0') AS confirmation_number,
    -- Cycle through 10 properties
    (ARRAY[
        '11111111-1111-1111-1111-111111111111'::uuid,
        '22222222-2222-2222-2222-222222222222'::uuid,
        '33333333-3333-3333-3333-333333333333'::uuid,
        '44444444-4444-4444-4444-444444444444'::uuid,
        '55555555-5555-5555-5555-555555555555'::uuid,
        '66666666-6666-6666-6666-666666666666'::uuid,
        '77777777-7777-7777-7777-777777777777'::uuid,
        '88888888-8888-8888-8888-888888888888'::uuid,
        '99999999-9999-9999-9999-999999999999'::uuid,
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'::uuid
    ])[1 + (s % 10)] AS property_id,
    -- Guest ID (80% have guest IDs, 20% are walk-ins)
    CASE WHEN s % 5 = 0 THEN NULL
    ELSE gen_random_uuid()
    END AS guest_id,
    -- Guest first names
    (ARRAY['John', 'Jane', 'Michael', 'Emily', 'David', 'Sarah', 'Robert', 'Lisa', 'James', 'Mary',
           'William', 'Patricia', 'Richard', 'Jennifer', 'Thomas', 'Linda', 'Charles', 'Barbara', 'Daniel', 'Susan'])[1 + (s % 20)] AS guest_first_name,
    -- Guest last names
    (ARRAY['Smith', 'Johnson', 'Williams', 'Brown', 'Jones', 'Garcia', 'Miller', 'Davis', 'Rodriguez', 'Martinez',
           'Hernandez', 'Lopez', 'Gonzalez', 'Wilson', 'Anderson', 'Thomas', 'Taylor', 'Moore', 'Jackson', 'Martin'])[1 + (s % 20)] AS guest_last_name,
    -- Email
    LOWER((ARRAY['John', 'Jane', 'Michael', 'Emily', 'David', 'Sarah', 'Robert', 'Lisa', 'James', 'Mary',
           'William', 'Patricia', 'Richard', 'Jennifer', 'Thomas', 'Linda', 'Charles', 'Barbara', 'Daniel', 'Susan'])[1 + (s % 20)]) ||
    '.' ||
    LOWER((ARRAY['Smith', 'Johnson', 'Williams', 'Brown', 'Jones', 'Garcia', 'Miller', 'Davis', 'Rodriguez', 'Martinez',
           'Hernandez', 'Lopez', 'Gonzalez', 'Wilson', 'Anderson', 'Thomas', 'Taylor', 'Moore', 'Jackson', 'Martin'])[1 + (s % 20)]) ||
    s || '@email.com' AS guest_email,
    -- Phone number
    '+1-' || (200 + (s % 800)) || '-' || (100 + (s % 900)) || '-' || (1000 + (s % 9000)) AS guest_phone,
    -- Check-in date (spread over past 180 days to future 180 days)
    CURRENT_DATE + ((s % 360) - 180) AS check_in_date,
    -- Check-out date (1-14 nights stay)
    CURRENT_DATE + ((s % 360) - 180) + (1 + (s % 14)) AS check_out_date,
    -- Nights (1-14)
    1 + (s % 14) AS nights,
    -- Room type
    (ARRAY[
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'::uuid,
        'cccccccc-cccc-cccc-cccc-cccccccccccc'::uuid,
        'dddddddd-dddd-dddd-dddd-dddddddddddd'::uuid,
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee'::uuid,
        'ffffffff-ffff-ffff-ffff-ffffffffffff'::uuid
    ])[1 + (s % 5)] AS room_type_id,
    -- Room number (60% assigned, 40% not yet assigned)
    CASE WHEN s % 10 < 6 THEN (100 + (s % 400))::text ELSE NULL END AS room_number,
    -- Adults (1-4)
    1 + (s % 4) AS adults,
    -- Children (0-3, 60% have no children)
    CASE WHEN s % 10 < 6 THEN 0 ELSE (s % 3) END AS children,
    -- Infants (0-2, 80% have no infants)
    CASE WHEN s % 10 < 8 THEN 0 ELSE (s % 2) END AS infants,
    -- Room rate per night ($100-$500)
    (100 + (s % 400))::numeric(10,2) AS room_rate,
    -- Taxes (10-15% of room rate * nights)
    (((100 + (s % 400)) * (1 + (s % 14))) * (0.10 + (s % 5) * 0.01))::numeric(10,2) AS taxes,
    -- Fees (5-10% of room rate * nights)
    (((100 + (s % 400)) * (1 + (s % 14))) * (0.05 + (s % 5) * 0.01))::numeric(10,2) AS fees,
    -- Total amount (room rate * nights + taxes + fees)
    (((100 + (s % 400)) * (1 + (s % 14))) * 1.20)::numeric(10,2) AS total_amount,
    -- Currency
        (ARRAY['USD', 'EUR', 'GBP'])[1 + (s % 3)] AS currency,
    -- Status distribution (mostly completed)
    (ARRAY['CONFIRMED', 'CONFIRMED', 'CONFIRMED', 'CONFIRMED', 'PENDING',
           'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED', 'NO_SHOW', 'ON_HOLD'])[1 + (s % 10)]::reservation_status AS status,
    -- Source distribution
    (ARRAY['DIRECT', 'BOOKING_COM', 'EXPEDIA', 'AIRBNB', 'PHONE', 'EMAIL', 'WALK_IN',
           'HOTELS_COM', 'AGODA', 'MOBILE_APP', 'CORPORATE', 'TRAVEL_AGENT'])[1 + (s % 12)]::reservation_source AS source,
    -- Special requests (50% have requests)
    CASE WHEN s % 2 = 0 THEN
        (ARRAY['Late check-in after 10 PM', 'Early check-in requested', 'High floor preferred',
               'Quiet room away from elevator', 'King bed preferred', 'Twin beds requested',
               'Extra pillows needed', 'Hypoallergenic room', 'Accessible room required',
               'Connecting rooms needed'])[1 + (s % 10)]
    ELSE NULL END AS special_requests,
    -- Internal notes (30% have notes)
    CASE WHEN s % 10 < 3 THEN
        (ARRAY['VIP guest', 'Repeat customer', 'First time guest', 'Group booking',
               'Corporate account', 'Special occasion - anniversary', 'Honeymoon package',
               'Extended stay', 'Early departure possible', 'Late checkout approved'])[1 + (s % 10)]
    ELSE NULL END AS internal_notes,
    -- Booking date (7-90 days before check-in)
    CURRENT_DATE + ((s % 360) - 180) - (7 + (s % 83)) AS booking_date,
    -- Arrival time (50% specify time)
    CASE WHEN s % 2 = 0 THEN
        (ARRAY['14:00', '15:00', '16:00', '17:00', '18:00', '19:00', '20:00', '21:00', '22:00'])[1 + (s % 9)]
    ELSE NULL END AS arrival_time,
    -- Departure time (50% specify time)
    CASE WHEN s % 2 = 1 THEN
        (ARRAY['09:00', '10:00', '11:00', '12:00', '13:00'])[1 + (s % 5)]
    ELSE NULL END AS departure_time
FROM generate_series(1, 500) AS s;

-- Verify the insert
SELECT COUNT(*) as total_reservations FROM reservations;
SELECT status, COUNT(*) FROM reservations GROUP BY status ORDER BY status;
SELECT source, COUNT(*) FROM reservations GROUP BY source ORDER BY COUNT(*) DESC LIMIT 5;
