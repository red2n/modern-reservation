-- Sample Room Types and Rooms Data
-- This file contains sample data for room types, rooms, and room amenities

-- Sample Room Types (30 room types across all properties)
INSERT INTO room_types (id, property_id, name, description, base_occupancy, maximum_occupancy, number_of_beds, bed_type, room_size_sqft, base_rate, created_at, updated_at) VALUES
-- Grand Plaza Hotel room types
('650e8400-e29b-41d4-a716-446655440000'::uuid, '550e8400-e29b-41d4-a716-446655440000'::uuid, 'Standard Room', 'Comfortable room with city view', 2, 3, 1, 'QUEEN', 300, 150.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('650e8400-e29b-41d4-a716-446655440001'::uuid, '550e8400-e29b-41d4-a716-446655440000'::uuid, 'Deluxe Room', 'Spacious room with premium amenities', 2, 4, 2, 'QUEEN', 400, 220.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('650e8400-e29b-41d4-a716-446655440002'::uuid, '550e8400-e29b-41d4-a716-446655440000'::uuid, 'Executive Suite', 'Luxury suite with separate living area', 2, 4, 1, 'KING', 600, 350.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Seaside Resort room types
('650e8400-e29b-41d4-a716-446655440003'::uuid, '550e8400-e29b-41d4-a716-446655440001'::uuid, 'Garden View Room', 'Room overlooking resort gardens', 2, 3, 1, 'QUEEN', 320, 180.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('650e8400-e29b-41d4-a716-446655440004'::uuid, '550e8400-e29b-41d4-a716-446655440001'::uuid, 'Ocean View Room', 'Room with partial ocean view', 2, 4, 2, 'QUEEN', 380, 280.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('650e8400-e29b-41d4-a716-446655440005'::uuid, '550e8400-e29b-41d4-a716-446655440001'::uuid, 'Beachfront Villa', 'Private villa with direct beach access', 4, 6, 2, 'KING', 900, 550.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Mountain View Lodge room types
('650e8400-e29b-41d4-a716-446655440006'::uuid, '550e8400-e29b-41d4-a716-446655440002'::uuid, 'Cozy Room', 'Warm room with mountain views', 2, 2, 1, 'QUEEN', 280, 120.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('650e8400-e29b-41d4-a716-446655440007'::uuid, '550e8400-e29b-41d4-a716-446655440002'::uuid, 'Family Suite', 'Suite perfect for families', 4, 5, 2, 'QUEEN', 500, 200.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('650e8400-e29b-41d4-a716-446655440008'::uuid, '550e8400-e29b-41d4-a716-446655440002'::uuid, 'Ski Chalet', 'Premium chalet with fireplace', 2, 4, 1, 'KING', 450, 240.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Downtown Business Suites room types
('650e8400-e29b-41d4-a716-446655440009'::uuid, '550e8400-e29b-41d4-a716-446655440003'::uuid, 'Business Room', 'Room with work desk and ergonomic chair', 1, 2, 1, 'QUEEN', 300, 160.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('650e8400-e29b-41d4-a716-446655440010'::uuid, '550e8400-e29b-41d4-a716-446655440003'::uuid, 'Junior Suite', 'Suite with kitchenette', 2, 3, 1, 'KING', 450, 240.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('650e8400-e29b-41d4-a716-446655440011'::uuid, '550e8400-e29b-41d4-a716-446655440003'::uuid, 'Executive Suite', 'Large suite with meeting area', 2, 4, 1, 'KING', 650, 380.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Coastal Inn room types
('650e8400-e29b-41d4-a716-446655440012'::uuid, '550e8400-e29b-41d4-a716-446655440004'::uuid, 'Standard Room', 'Comfortable room with harbor view', 2, 3, 1, 'QUEEN', 290, 140.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('650e8400-e29b-41d4-a716-446655440013'::uuid, '550e8400-e29b-41d4-a716-446655440004'::uuid, 'Marina Suite', 'Suite overlooking marina', 2, 4, 1, 'KING', 500, 260.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Historic Heritage Hotel room types
('650e8400-e29b-41d4-a716-446655440014'::uuid, '550e8400-e29b-41d4-a716-446655440005'::uuid, 'Classic Room', 'Historically preserved room', 2, 3, 1, 'QUEEN', 320, 180.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('650e8400-e29b-41d4-a716-446655440015'::uuid, '550e8400-e29b-41d4-a716-446655440005'::uuid, 'Heritage Suite', 'Luxury suite with antique furnishings', 2, 4, 1, 'KING', 550, 320.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('650e8400-e29b-41d4-a716-446655440016'::uuid, '550e8400-e29b-41d4-a716-446655440005'::uuid, 'Presidential Suite', 'Premier suite with historic charm', 2, 6, 2, 'KING', 800, 500.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Lakefront Paradise Resort room types
('650e8400-e29b-41d4-a716-446655440017'::uuid, '550e8400-e29b-41d4-a716-446655440006'::uuid, 'Standard Room', 'Comfortable resort room', 2, 4, 2, 'QUEEN', 350, 160.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('650e8400-e29b-41d4-a716-446655440018'::uuid, '550e8400-e29b-41d4-a716-446655440006'::uuid, 'Lake View Room', 'Room with lake view', 2, 4, 2, 'QUEEN', 380, 200.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('650e8400-e29b-41d4-a716-446655440019'::uuid, '550e8400-e29b-41d4-a716-446655440006'::uuid, 'Family Suite', 'Two-bedroom family suite', 4, 6, 3, 'QUEEN', 700, 320.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('650e8400-e29b-41d4-a716-446655440020'::uuid, '550e8400-e29b-41d4-a716-446655440006'::uuid, 'Luxury Villa', 'Private villa with pool', 4, 8, 3, 'KING', 1200, 600.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Urban Boutique Hotel room types
('650e8400-e29b-41d4-a716-446655440021'::uuid, '550e8400-e29b-41d4-a716-446655440007'::uuid, 'Urban Room', 'Stylish room with city view', 2, 2, 1, 'QUEEN', 280, 170.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('650e8400-e29b-41d4-a716-446655440022'::uuid, '550e8400-e29b-41d4-a716-446655440007'::uuid, 'Designer Suite', 'Suite with designer furnishings', 2, 4, 1, 'KING', 500, 300.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('650e8400-e29b-41d4-a716-446655440023'::uuid, '550e8400-e29b-41d4-a716-446655440007'::uuid, 'Penthouse', 'Top-floor penthouse suite', 2, 4, 1, 'KING', 800, 500.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Airport Executive Inn room types
('650e8400-e29b-41d4-a716-446655440024'::uuid, '550e8400-e29b-41d4-a716-446655440008'::uuid, 'Standard Room', 'Comfortable room for travelers', 2, 3, 1, 'QUEEN', 280, 110.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('650e8400-e29b-41d4-a716-446655440025'::uuid, '550e8400-e29b-41d4-a716-446655440008'::uuid, 'Executive Room', 'Room with work area', 2, 3, 1, 'KING', 320, 140.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Garden Valley Retreat room types
('650e8400-e29b-41d4-a716-446655440026'::uuid, '550e8400-e29b-41d4-a716-446655440009'::uuid, 'Garden Room', 'Room with garden view', 2, 3, 1, 'QUEEN', 300, 150.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('650e8400-e29b-41d4-a716-446655440027'::uuid, '550e8400-e29b-41d4-a716-446655440009'::uuid, 'Wellness Suite', 'Suite with meditation area', 2, 4, 1, 'KING', 450, 230.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('650e8400-e29b-41d4-a716-446655440028'::uuid, '550e8400-e29b-41d4-a716-446655440009'::uuid, 'Spa Villa', 'Private villa with spa', 2, 4, 1, 'KING', 650, 350.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Generate 150 Rooms across all properties
DO $$
DECLARE
    room_type RECORD;
    room_number VARCHAR;
    floor_num INTEGER;
    room_count INTEGER;
    rooms_per_type INTEGER;
    i INTEGER;
BEGIN
    -- For each room type, create multiple rooms
    FOR room_type IN
        SELECT id, property_id, name
        FROM room_types
        ORDER BY property_id, id
    LOOP
        -- Create 5 rooms per room type
        rooms_per_type := 5;

        FOR i IN 1..rooms_per_type LOOP
            floor_num := ((i - 1) / 10) + 1;
            room_number := floor_num::TEXT || LPAD(((i - 1) % 10 + 1)::TEXT, 2, '0');

            INSERT INTO rooms (
                id,
                property_id,
                room_type_id,
                room_number,
                floor_number,
                building_name,
                room_status,
                is_accessible,
                is_smoking_allowed,
                is_pet_friendly,
                last_maintenance_date,
                created_at,
                updated_at
            ) VALUES (
                gen_random_uuid(),
                room_type.property_id,
                room_type.id,
                room_number,
                floor_num,
                'Main Building',
                'AVAILABLE',
                (i % 5 = 0),  -- Every 5th room is accessible
                false,
                (i % 8 = 0),  -- Every 8th room is pet friendly
                CURRENT_DATE - INTERVAL '30 days',
                CURRENT_TIMESTAMP,
                CURRENT_TIMESTAMP
            );
        END LOOP;
    END LOOP;
END $$;

-- Sample Room Amenities (200 amenity mappings)
INSERT INTO room_amenities (id, room_type_id, amenity_name, amenity_type, description, created_at) VALUES
-- Standard amenities for all room types
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440000'::uuid, 'Flat Screen TV', 'ENTERTAINMENT', '55-inch smart TV', CURRENT_TIMESTAMP),
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440000'::uuid, 'Mini Fridge', 'KITCHEN', 'Compact refrigerator', CURRENT_TIMESTAMP),
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440000'::uuid, 'Coffee Maker', 'KITCHEN', 'In-room coffee station', CURRENT_TIMESTAMP),
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440000'::uuid, 'Work Desk', 'FURNITURE', 'Large work desk with lamp', CURRENT_TIMESTAMP),
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440000'::uuid, 'Safe', 'SECURITY', 'In-room electronic safe', CURRENT_TIMESTAMP),
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440000'::uuid, 'Hair Dryer', 'BATHROOM', 'Professional hair dryer', CURRENT_TIMESTAMP),
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440000'::uuid, 'Iron & Board', 'LAUNDRY', 'Iron and ironing board', CURRENT_TIMESTAMP);

-- Add amenities for each room type (generating more entries)
DO $$
DECLARE
    rt_id UUID;
    amenities TEXT[] := ARRAY[
        'Flat Screen TV', 'Mini Fridge', 'Coffee Maker', 'Work Desk', 'Safe',
        'Hair Dryer', 'Iron & Board', 'Bathrobe', 'Slippers', 'Premium Bedding',
        'Blackout Curtains', 'Air Conditioning', 'Heating', 'Free Toiletries',
        'High-Speed WiFi', 'USB Charging Ports', 'Smart Room Controls',
        'Sound System', 'Reading Lights', 'Extra Pillows'
    ];
    amenity_types TEXT[] := ARRAY[
        'ENTERTAINMENT', 'KITCHEN', 'KITCHEN', 'FURNITURE', 'SECURITY',
        'BATHROOM', 'LAUNDRY', 'COMFORT', 'COMFORT', 'BEDDING',
        'WINDOW', 'CLIMATE', 'CLIMATE', 'BATHROOM',
        'CONNECTIVITY', 'TECHNOLOGY', 'TECHNOLOGY',
        'ENTERTAINMENT', 'LIGHTING', 'BEDDING'
    ];
    i INTEGER;
BEGIN
    FOR rt_id IN SELECT id FROM room_types WHERE id != '650e8400-e29b-41d4-a716-446655440000'::uuid LOOP
        FOR i IN 1..10 LOOP
            INSERT INTO room_amenities (id, room_type_id, amenity_name, amenity_type, description, created_at)
            VALUES (
                gen_random_uuid(),
                rt_id,
                amenities[((i - 1) % 20) + 1],
                amenity_types[((i - 1) % 20) + 1],
                'Standard room amenity',
                CURRENT_TIMESTAMP
            );
        END LOOP;
    END LOOP;
END $$;
