-- Sample Properties and Amenities
-- Creates 10 sample hotels with realistic data

-- System IDs (must match 00-sample-system-user.sql)
DO $$
DECLARE
    v_system_tenant_id UUID := '00000000-0000-0000-0000-000000000001';
    v_system_user_id UUID := '00000000-0000-0000-0000-000000000002';
    v_property_ids UUID[];
    v_amenity_ids UUID[];
BEGIN
    -- Create property amenities lookup table entries
    INSERT INTO property_amenities (id, name, category, description, icon) VALUES
        (uuid_generate_v4(), 'Free WiFi', 'connectivity', '24/7 high-speed internet', 'wifi'),
        (uuid_generate_v4(), 'Swimming Pool', 'recreation', 'Outdoor heated pool', 'pool'),
        (uuid_generate_v4(), 'Fitness Center', 'recreation', '24-hour gym access', 'fitness'),
        (uuid_generate_v4(), 'Restaurant', 'dining', 'On-site dining', 'restaurant'),
        (uuid_generate_v4(), 'Spa', 'wellness', 'Full-service spa', 'spa'),
        (uuid_generate_v4(), 'Bar/Lounge', 'dining', 'Cocktail lounge', 'bar'),
        (uuid_generate_v4(), 'Room Service', 'service', '24-hour room service', 'room-service'),
        (uuid_generate_v4(), 'Concierge', 'service', 'Concierge services', 'concierge'),
        (uuid_generate_v4(), 'Valet Parking', 'parking', 'Valet parking service', 'parking'),
        (uuid_generate_v4(), 'Business Center', 'business', 'Business facilities', 'business'),
        (uuid_generate_v4(), 'Meeting Rooms', 'business', 'Conference facilities', 'meeting'),
        (uuid_generate_v4(), 'Laundry Service', 'service', 'Laundry and dry cleaning', 'laundry'),
        (uuid_generate_v4(), 'Pet Friendly', 'policy', 'Pets allowed', 'pet'),
        (uuid_generate_v4(), 'Airport Shuttle', 'transport', 'Free airport shuttle', 'shuttle'),
        (uuid_generate_v4(), 'EV Charging', 'parking', 'Electric vehicle charging', 'ev-charger')
    ON CONFLICT (name) DO NOTHING;

    -- Create room amenities lookup table entries
    INSERT INTO room_amenities (id, name, category, description, icon) VALUES
        (uuid_generate_v4(), 'Air Conditioning', 'climate', 'Individual AC control', 'ac'),
        (uuid_generate_v4(), 'Heating', 'climate', 'Individual heating control', 'heater'),
        (uuid_generate_v4(), 'Mini Bar', 'convenience', 'Stocked mini refrigerator', 'minibar'),
        (uuid_generate_v4(), 'Coffee Maker', 'convenience', 'In-room coffee/tea maker', 'coffee'),
        (uuid_generate_v4(), 'Safe', 'security', 'In-room safe', 'safe'),
        (uuid_generate_v4(), 'Work Desk', 'furniture', 'Large work desk', 'desk'),
        (uuid_generate_v4(), 'Smart TV', 'entertainment', 'Smart TV with streaming', 'tv'),
        (uuid_generate_v4(), 'Balcony', 'view', 'Private balcony', 'balcony'),
        (uuid_generate_v4(), 'Ocean View', 'view', 'Ocean view', 'ocean'),
        (uuid_generate_v4(), 'Bathtub', 'bathroom', 'Soaking tub', 'bathtub'),
        (uuid_generate_v4(), 'Rain Shower', 'bathroom', 'Rain shower head', 'shower'),
        (uuid_generate_v4(), 'Premium Bedding', 'comfort', 'Luxury linens', 'bed'),
        (uuid_generate_v4(), 'Soundproofing', 'comfort', 'Soundproof windows', 'soundproof'),
        (uuid_generate_v4(), 'Kitchenette', 'convenience', 'Small kitchen area', 'kitchen'),
        (uuid_generate_v4(), 'Washer/Dryer', 'convenience', 'In-unit laundry', 'washer')
    ON CONFLICT (name) DO NOTHING;

    -- Insert 10 sample properties
    INSERT INTO properties (
        id, tenant_id, name, description, property_type, address, city, state, country, postal_code,
        timezone, phone, email, website, check_in_time, check_out_time, currency, tax_rate,
        amenities, policies, images, is_active, created_by, updated_by
    ) VALUES
    (
        uuid_generate_v4(), v_system_tenant_id, 'Grand Plaza Hotel',
        'Luxury hotel in the heart of downtown with stunning city views',
        'hotel', '123 Main Street', 'New York', 'NY', 'USA', '10001',
        'America/New_York', '+1-212-555-0100', 'info@grandplaza.com', 'https://grandplaza.com',
        '15:00', '11:00', 'USD', 0.0875,
        '["Free WiFi", "Swimming Pool", "Fitness Center", "Restaurant", "Spa", "Bar/Lounge", "Room Service", "Concierge", "Valet Parking", "Business Center"]'::jsonb,
        '{"cancellation_hours": 24, "pet_policy": "Not allowed", "smoking_policy": "Non-smoking", "check_in_age": 21}'::jsonb,
        '[{"url": "https://example.com/grand-plaza-1.jpg", "caption": "Main Lobby"}, {"url": "https://example.com/grand-plaza-2.jpg", "caption": "Pool Area"}]'::jsonb,
        true, v_system_user_id, v_system_user_id
    ),
    (
        uuid_generate_v4(), v_system_tenant_id, 'Seaside Resort & Spa',
        'Beachfront resort with private beach access and world-class spa',
        'resort', '456 Ocean Drive', 'Miami Beach', 'FL', 'USA', '33139',
        'America/New_York', '+1-305-555-0200', 'reservations@seasideresort.com', 'https://seasideresort.com',
        '16:00', '11:00', 'USD', 0.0700,
        '["Free WiFi", "Swimming Pool", "Fitness Center", "Restaurant", "Spa", "Bar/Lounge", "Room Service", "Pet Friendly", "Beach Access"]'::jsonb,
        '{"cancellation_hours": 48, "pet_policy": "Allowed with fee", "smoking_policy": "Non-smoking", "resort_fee": 35.00}'::jsonb,
        '[{"url": "https://example.com/seaside-1.jpg", "caption": "Beach View"}, {"url": "https://example.com/seaside-2.jpg", "caption": "Spa"}]'::jsonb,
        true, v_system_user_id, v_system_user_id
    ),
    (
        uuid_generate_v4(), v_system_tenant_id, 'Mountain Lodge Retreat',
        'Cozy mountain lodge with breathtaking views and outdoor activities',
        'hotel', '789 Mountain Road', 'Aspen', 'CO', 'USA', '81611',
        'America/Denver', '+1-970-555-0300', 'stay@mountainlodge.com', 'https://mountainlodge.com',
        '15:00', '10:00', 'USD', 0.0795,
        '["Free WiFi", "Fitness Center", "Restaurant", "Bar/Lounge", "Ski Storage", "Hot Tub", "Fireplace"]'::jsonb,
        '{"cancellation_hours": 72, "pet_policy": "Not allowed", "smoking_policy": "Non-smoking"}'::jsonb,
        '[{"url": "https://example.com/mountain-1.jpg", "caption": "Lodge Exterior"}]'::jsonb,
        true, v_system_user_id, v_system_user_id
    ),
    (
        uuid_generate_v4(), v_system_tenant_id, 'Urban Business Hotel',
        'Modern hotel designed for business travelers with meeting facilities',
        'hotel', '321 Business Park Way', 'San Francisco', 'CA', 'USA', '94105',
        'America/Los_Angeles', '+1-415-555-0400', 'info@urbanbusiness.com', 'https://urbanbusiness.com',
        '14:00', '12:00', 'USD', 0.0850,
        '["Free WiFi", "Fitness Center", "Business Center", "Meeting Rooms", "Restaurant", "Bar/Lounge", "Airport Shuttle", "EV Charging"]'::jsonb,
        '{"cancellation_hours": 24, "pet_policy": "Not allowed", "smoking_policy": "Non-smoking", "parking_fee": 45.00}'::jsonb,
        '[{"url": "https://example.com/urban-1.jpg", "caption": "Business Center"}]'::jsonb,
        true, v_system_user_id, v_system_user_id
    ),
    (
        uuid_generate_v4(), v_system_tenant_id, 'Historic Downtown Inn',
        'Charming boutique hotel in restored historic building',
        'hotel', '654 Heritage Lane', 'Charleston', 'SC', 'USA', '29401',
        'America/New_York', '+1-843-555-0500', 'welcome@historicinn.com', 'https://historicinn.com',
        '15:00', '11:00', 'USD', 0.0900,
        '["Free WiFi", "Restaurant", "Bar/Lounge", "Concierge", "Valet Parking"]'::jsonb,
        '{"cancellation_hours": 48, "pet_policy": "Not allowed", "smoking_policy": "Non-smoking"}'::jsonb,
        '[{"url": "https://example.com/historic-1.jpg", "caption": "Historic Facade"}]'::jsonb,
        true, v_system_user_id, v_system_user_id
    ),
    (
        uuid_generate_v4(), v_system_tenant_id, 'Luxury Suites Extended Stay',
        'All-suite hotel perfect for extended stays with full kitchens',
        'apartment', '987 Residence Blvd', 'Austin', 'TX', 'USA', '78701',
        'America/Chicago', '+1-512-555-0600', 'reservations@luxurysuites.com', 'https://luxurysuites.com',
        '15:00', '11:00', 'USD', 0.0825,
        '["Free WiFi", "Fitness Center", "Swimming Pool", "Business Center", "Laundry Service", "Pet Friendly", "Grocery Delivery"]'::jsonb,
        '{"cancellation_hours": 24, "pet_policy": "Allowed", "smoking_policy": "Non-smoking", "weekly_discount": 15, "monthly_discount": 25}'::jsonb,
        '[{"url": "https://example.com/luxury-suites-1.jpg", "caption": "Suite Kitchen"}]'::jsonb,
        true, v_system_user_id, v_system_user_id
    ),
    (
        uuid_generate_v4(), v_system_tenant_id, 'Lakeside Conference Center',
        'Hotel and conference center on scenic lakefront property',
        'hotel', '147 Lake Shore Drive', 'Chicago', 'IL', 'USA', '60611',
        'America/Chicago', '+1-312-555-0700', 'events@lakesideconf.com', 'https://lakesideconf.com',
        '15:00', '11:00', 'USD', 0.1025,
        '["Free WiFi", "Fitness Center", "Swimming Pool", "Restaurant", "Bar/Lounge", "Business Center", "Meeting Rooms", "Valet Parking"]'::jsonb,
        '{"cancellation_hours": 72, "pet_policy": "Service animals only", "smoking_policy": "Non-smoking"}'::jsonb,
        '[{"url": "https://example.com/lakeside-1.jpg", "caption": "Lake View"}]'::jsonb,
        true, v_system_user_id, v_system_user_id
    ),
    (
        uuid_generate_v4(), v_system_tenant_id, 'Airport Gateway Inn',
        'Convenient airport hotel with 24-hour shuttle service',
        'hotel', '258 Airport Road', 'Atlanta', 'GA', 'USA', '30320',
        'America/New_York', '+1-404-555-0800', 'bookings@airportgateway.com', 'https://airportgateway.com',
        '14:00', '12:00', 'USD', 0.0800,
        '["Free WiFi", "Fitness Center", "Restaurant", "Business Center", "Airport Shuttle", "Free Parking"]'::jsonb,
        '{"cancellation_hours": 24, "pet_policy": "Not allowed", "smoking_policy": "Smoking rooms available", "shuttle_hours": "24/7"}'::jsonb,
        '[{"url": "https://example.com/airport-1.jpg", "caption": "Exterior"}]'::jsonb,
        true, v_system_user_id, v_system_user_id
    ),
    (
        uuid_generate_v4(), v_system_tenant_id, 'Desert Oasis Resort',
        'Luxury desert resort with golf course and spa',
        'resort', '369 Desert Vista', 'Scottsdale', 'AZ', 'USA', '85251',
        'America/Phoenix', '+1-480-555-0900', 'info@desertoasis.com', 'https://desertoasis.com',
        '16:00', '11:00', 'USD', 0.0850,
        '["Free WiFi", "Swimming Pool", "Fitness Center", "Restaurant", "Spa", "Bar/Lounge", "Golf Course", "Tennis Courts"]'::jsonb,
        '{"cancellation_hours": 72, "pet_policy": "Not allowed", "smoking_policy": "Non-smoking", "resort_fee": 45.00}'::jsonb,
        '[{"url": "https://example.com/desert-1.jpg", "caption": "Pool Area"}]'::jsonb,
        true, v_system_user_id, v_system_user_id
    ),
    (
        uuid_generate_v4(), v_system_tenant_id, 'Coastal Bed & Breakfast',
        'Intimate B&B with personalized service and gourmet breakfasts',
        'villa', '741 Coastal Highway', 'Portland', 'ME', 'USA', '04101',
        'America/New_York', '+1-207-555-1000', 'stay@coastalbnb.com', 'https://coastalbnb.com',
        '15:00', '11:00', 'USD', 0.0550,
        '["Free WiFi", "Complimentary Breakfast", "Bike Rentals", "Beach Access"]'::jsonb,
        '{"cancellation_hours": 48, "pet_policy": "Not allowed", "smoking_policy": "Non-smoking", "minimum_age": 16}'::jsonb,
        '[{"url": "https://example.com/coastal-1.jpg", "caption": "Breakfast Room"}]'::jsonb,
        true, v_system_user_id, v_system_user_id
    );

    RAISE NOTICE '10 sample properties created';
END $$;
