-- Sample Data for Room Availability Table
-- Generates 500 realistic room availability records with dates and pricing

INSERT INTO availability.room_availability (
    id, property_id, room_type_id, room_number, room_category,
    availability_date, availability_status,
    base_rate, current_rate, min_rate, max_rate,
    total_rooms, available_rooms, occupied_rooms, maintenance_rooms, blocked_rooms,
    minimum_stay, maximum_stay,
    closed_to_arrival, closed_to_departure, stop_sell,
    currency, notes,
    created_at, updated_at
)
SELECT
    gen_random_uuid(),
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
    -- Cycle through 5 room types
    (ARRAY[
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'::uuid,
        'cccccccc-cccc-cccc-cccc-cccccccccccc'::uuid,
        'dddddddd-dddd-dddd-dddd-dddddddddddd'::uuid,
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee'::uuid,
        'ffffffff-ffff-ffff-ffff-ffffffffffff'::uuid
    ])[1 + (s % 5)] AS room_type_id,
    -- Room number (specific rooms)
    (100 + (s % 400))::text AS room_number,
    -- Room category distribution
    (ARRAY['STANDARD', 'STANDARD', 'DELUXE', 'DELUXE', 'SUITE',
           'EXECUTIVE', 'PRESIDENTIAL'])[1 + (s % 7)]::room_category AS room_category,
    -- Availability date (spread over next year)
    CURRENT_DATE + (s % 365) AS availability_date,
    -- Status distribution (mostly available)
    (ARRAY['AVAILABLE', 'AVAILABLE', 'AVAILABLE', 'AVAILABLE', 'AVAILABLE',
           'OCCUPIED', 'OCCUPIED', 'MAINTENANCE', 'BLOCKED', 'RESERVED'])[1 + (s % 10)]::availability_status AS availability_status,
    -- Base rate ($80-$600 depending on category)
    (80 + (s % 520) +
        CASE (ARRAY['STANDARD', 'STANDARD', 'DELUXE', 'DELUXE', 'SUITE', 'EXECUTIVE', 'PRESIDENTIAL'])[1 + (s % 7)]
            WHEN 'STANDARD' THEN 0
            WHEN 'DELUXE' THEN 100
            WHEN 'SUITE' THEN 200
            WHEN 'EXECUTIVE' THEN 300
            WHEN 'PRESIDENTIAL' THEN 500
        END
    )::numeric(10,2) AS base_rate,
    -- Current rate (base rate with variance)
    (80 + (s % 520) +
        CASE (ARRAY['STANDARD', 'STANDARD', 'DELUXE', 'DELUXE', 'SUITE', 'EXECUTIVE', 'PRESIDENTIAL'])[1 + (s % 7)]
            WHEN 'STANDARD' THEN 0
            WHEN 'DELUXE' THEN 100
            WHEN 'SUITE' THEN 200
            WHEN 'EXECUTIVE' THEN 300
            WHEN 'PRESIDENTIAL' THEN 500
        END + ((s % 40) - 20)
    )::numeric(10,2) AS current_rate,
    -- Min rate (70% of base)
    ((80 + (s % 520)) * 0.7)::numeric(10,2) AS min_rate,
    -- Max rate (180% of base)
    ((80 + (s % 520)) * 1.8)::numeric(10,2) AS max_rate,
    -- Total rooms for this type/date
    10 + (s % 40) AS total_rooms,
    -- Available rooms (varies by status)
    CASE (ARRAY['AVAILABLE', 'AVAILABLE', 'AVAILABLE', 'AVAILABLE', 'AVAILABLE',
           'OCCUPIED', 'OCCUPIED', 'MAINTENANCE', 'BLOCKED', 'RESERVED'])[1 + (s % 10)]
        WHEN 'AVAILABLE' THEN 10 + (s % 40)
        WHEN 'OCCUPIED' THEN (s % 10)
        WHEN 'RESERVED' THEN (s % 15)
        ELSE (s % 5)
    END AS available_rooms,
    -- Occupied rooms
    CASE (ARRAY['AVAILABLE', 'AVAILABLE', 'AVAILABLE', 'AVAILABLE', 'AVAILABLE',
           'OCCUPIED', 'OCCUPIED', 'MAINTENANCE', 'BLOCKED', 'RESERVED'])[1 + (s % 10)]
        WHEN 'OCCUPIED' THEN (10 + (s % 40)) - (s % 10)
        WHEN 'RESERVED' THEN (10 + (s % 40)) - (s % 15)
        ELSE (s % 5)
    END AS occupied_rooms,
    -- Maintenance rooms
    CASE WHEN s % 20 = 0 THEN (s % 3) ELSE 0 END AS maintenance_rooms,
    -- Blocked rooms
    CASE WHEN s % 25 = 0 THEN (s % 5) ELSE 0 END AS blocked_rooms,
    -- Minimum stay (1-7 nights, higher on weekends)
    CASE WHEN EXTRACT(DOW FROM CURRENT_DATE + (s % 365)) IN (5, 6)
         THEN 2 + (s % 3)
         ELSE 1 + (s % 2)
    END AS minimum_stay,
    -- Maximum stay (7-30 nights)
    CASE WHEN s % 5 = 0 THEN NULL ELSE 7 + (s % 24) END AS maximum_stay,
    -- Closed to arrival (10% of dates)
    (s % 10) = 0 AS closed_to_arrival,
    -- Closed to departure (5% of dates)
    (s % 20) = 0 AS closed_to_departure,
    -- Stop sell (3% of dates)
    (s % 33) = 0 AS stop_sell,
    -- Currency
    (ARRAY['USD', 'EUR', 'GBP'])[1 + (s % 3)] AS currency,
    -- Notes (20% have notes)
    CASE WHEN s % 5 = 0 THEN
        (ARRAY['High demand period', 'Special event in area', 'Conference booking',
               'Renovation scheduled', 'Peak season pricing', 'Holiday surcharge',
               'Group booking available', 'Last rooms available', 'Advance purchase only',
               'Weekend minimum stay'])[1 + (s % 10)]
    ELSE NULL END AS notes,
    CURRENT_TIMESTAMP - (s || ' hours')::interval AS created_at,
    CASE WHEN s % 3 = 0 THEN CURRENT_TIMESTAMP - ((s / 2) || ' hours')::interval ELSE NULL END AS updated_at
FROM generate_series(1, 500) AS s;

-- Verify the insert
SELECT COUNT(*) as total_availability_records FROM availability.room_availability;
SELECT availability_status, COUNT(*) FROM availability.room_availability GROUP BY availability_status ORDER BY availability_status;
SELECT room_category, COUNT(*) FROM availability.room_availability GROUP BY room_category ORDER BY COUNT(*) DESC;
