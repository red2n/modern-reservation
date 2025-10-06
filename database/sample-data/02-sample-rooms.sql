-- Sample Rooms and Room Types
-- Creates room types and 150 sample rooms

-- System IDs
DO $$
DECLARE
    v_system_tenant_id UUID := '00000000-0000-0000-0000-000000000001';
    v_system_user_id UUID := '00000000-0000-0000-0000-000000000002';
    v_property_ids UUID[];
    v_room_type_ids UUID[];
    v_room_id UUID;
    v_room_number INT;
    i INT;
    j INT;
BEGIN
    -- Get all property IDs
    SELECT ARRAY_AGG(id) INTO v_property_ids FROM properties WHERE tenant_id = v_system_tenant_id;

    IF array_length(v_property_ids, 1) IS NULL OR array_length(v_property_ids, 1) < 10 THEN
        RAISE EXCEPTION 'Properties must be created first! Found % properties', COALESCE(array_length(v_property_ids, 1), 0);
    END IF;

    RAISE NOTICE 'Found % properties', array_length(v_property_ids, 1);

    -- Create room types (3 per property)
    FOR i IN 1..10 LOOP
        -- Standard Room
        INSERT INTO room_types (
            property_id, name, description, base_occupancy, max_occupancy, base_price,
            currency, amenities, is_active, created_by, updated_by
        ) VALUES (
            v_property_ids[i], 'Standard Room', 'Comfortable room with essential amenities',
            2, 3, 129.00, 'USD',
            '["Air Conditioning", "Smart TV", "Free WiFi", "Work Desk", "Coffee Maker"]'::jsonb,
            true, v_system_user_id, v_system_user_id
        );

        -- Deluxe Room
        INSERT INTO room_types (
            property_id, name, description, base_occupancy, max_occupancy, base_price,
            currency, amenities, is_active, created_by, updated_by
        ) VALUES (
            v_property_ids[i], 'Deluxe Room', 'Spacious room with premium amenities and city view',
            2, 4, 189.00, 'USD',
            '["Air Conditioning", "Smart TV", "Free WiFi", "Work Desk", "Coffee Maker", "Mini Bar", "Balcony", "Premium Bedding"]'::jsonb,
            true, v_system_user_id, v_system_user_id
        );

        -- Suite
        INSERT INTO room_types (
            property_id, name, description, base_occupancy, max_occupancy, base_price,
            currency, amenities, is_active, created_by, updated_by
        ) VALUES (
            v_property_ids[i], 'Executive Suite', 'Luxurious suite with separate living area',
            2, 4, 299.00, 'USD',
            '["Air Conditioning", "Smart TV", "Free WiFi", "Work Desk", "Coffee Maker", "Mini Bar", "Balcony", "Premium Bedding", "Bathtub", "Safe", "Soundproofing"]'::jsonb,
            true, v_system_user_id, v_system_user_id
        );
    END LOOP;

    RAISE NOTICE '30 room types created (3 per property)';

    -- Create rooms (15 per property = 150 total)
    v_room_number := 100;
    FOR i IN 1..10 LOOP
        -- 8 Standard Rooms
        FOR j IN 1..8 LOOP
            v_room_number := v_room_number + 1;
            INSERT INTO rooms (
                property_id, room_number, room_type, floor, capacity, base_price, currency,
                size, bed_type, bed_count, amenities, description, status,
                housekeeping_status, is_active, created_by, updated_by
            ) VALUES (
                v_property_ids[i],
                v_room_number::TEXT,
                'Standard Room',
                ((v_room_number / 100) + 1),
                2,
                129.00 + (RANDOM() * 30)::DECIMAL(10,2),
                'USD',
                300.00 + (RANDOM() * 50)::DECIMAL(8,2),
                CASE WHEN j <= 4 THEN 'Queen' ELSE 'King' END,
                CASE WHEN j <= 4 THEN 2 ELSE 1 END,
                '["Air Conditioning", "Smart TV", "Free WiFi", "Work Desk", "Coffee Maker"]'::jsonb,
                'Comfortable standard room with modern amenities',
                'available',
                'clean',
                true,
                v_system_user_id,
                v_system_user_id
            );
        END LOOP;

        -- 5 Deluxe Rooms
        FOR j IN 1..5 LOOP
            v_room_number := v_room_number + 1;
            INSERT INTO rooms (
                property_id, room_number, room_type, floor, capacity, base_price, currency,
                size, bed_type, bed_count, amenities, description, status,
                housekeeping_status, is_active, created_by, updated_by
            ) VALUES (
                v_property_ids[i],
                v_room_number::TEXT,
                'Deluxe Room',
                ((v_room_number / 100) + 1),
                3,
                189.00 + (RANDOM() * 40)::DECIMAL(10,2),
                'USD',
                400.00 + (RANDOM() * 100)::DECIMAL(8,2),
                'King',
                1,
                '["Air Conditioning", "Smart TV", "Free WiFi", "Work Desk", "Coffee Maker", "Mini Bar", "Balcony", "Premium Bedding"]'::jsonb,
                'Spacious deluxe room with premium amenities',
                'available',
                'clean',
                true,
                v_system_user_id,
                v_system_user_id
            );
        END LOOP;

        -- 2 Executive Suites
        FOR j IN 1..2 LOOP
            v_room_number := v_room_number + 1;
            INSERT INTO rooms (
                property_id, room_number, room_type, floor, capacity, base_price, currency,
                size, bed_type, bed_count, amenities, description, status,
                housekeeping_status, is_active, created_by, updated_by
            ) VALUES (
                v_property_ids[i],
                v_room_number::TEXT,
                'Executive Suite',
                ((v_room_number / 100) + 1),
                4,
                299.00 + (RANDOM() * 100)::DECIMAL(10,2),
                'USD',
                650.00 + (RANDOM() * 150)::DECIMAL(8,2),
                'King',
                1,
                '["Air Conditioning", "Smart TV", "Free WiFi", "Work Desk", "Coffee Maker", "Mini Bar", "Balcony", "Premium Bedding", "Bathtub", "Safe", "Soundproofing"]'::jsonb,
                'Luxurious executive suite with separate living area',
                'available',
                'clean',
                true,
                v_system_user_id,
                v_system_user_id
            );
        END LOOP;

        -- Reset room number for next property
        v_room_number := 100 * (i + 1);
    END LOOP;

    RAISE NOTICE '150 rooms created (15 per property: 8 Standard, 5 Deluxe, 2 Suites)';
END $$;
