-- Sample Properties Data
-- This file contains sample data for properties and related tables

-- Sample Properties (10 properties)
INSERT INTO properties (id, name, address, city, state, country, postal_code, phone, email, website, timezone, currency, property_type, star_rating, chain_id, total_rooms, check_in_time, check_out_time, cancellation_policy, created_at, updated_at) VALUES
('550e8400-e29b-41d4-a716-446655440000'::uuid, 'Grand Plaza Hotel', '123 Main Street', 'New York', 'NY', 'USA', '10001', '+1-212-555-0100', 'info@grandplaza.com', 'https://grandplaza.com', 'America/New_York', 'USD', 'HOTEL', 5, NULL, 200, '15:00:00', '11:00:00', '24_HOURS_FREE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440001'::uuid, 'Seaside Resort & Spa', '456 Ocean Drive', 'Miami', 'FL', 'USA', '33139', '+1-305-555-0200', 'reservations@seasideresort.com', 'https://seasideresort.com', 'America/New_York', 'USD', 'RESORT', 4, NULL, 150, '16:00:00', '12:00:00', 'FLEXIBLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440002'::uuid, 'Mountain View Lodge', '789 Summit Road', 'Denver', 'CO', 'USA', '80202', '+1-303-555-0300', 'contact@mountainview.com', 'https://mountainview.com', 'America/Denver', 'USD', 'HOTEL', 3, NULL, 80, '14:00:00', '10:00:00', 'MODERATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440003'::uuid, 'Downtown Business Suites', '321 Corporate Blvd', 'Chicago', 'IL', 'USA', '60601', '+1-312-555-0400', 'info@downtownsuites.com', 'https://downtownsuites.com', 'America/Chicago', 'USD', 'HOTEL', 4, NULL, 120, '15:00:00', '11:00:00', 'STRICT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440004'::uuid, 'Coastal Inn & Marina', '654 Harbor Way', 'San Francisco', 'CA', 'USA', '94102', '+1-415-555-0500', 'reservations@coastalinn.com', 'https://coastalinn.com', 'America/Los_Angeles', 'USD', 'HOTEL', 3, NULL, 60, '15:00:00', '11:00:00', '24_HOURS_FREE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440005'::uuid, 'Historic Heritage Hotel', '987 Old Town Square', 'Boston', 'MA', 'USA', '02108', '+1-617-555-0600', 'info@heritagehotel.com', 'https://heritagehotel.com', 'America/New_York', 'USD', 'HOTEL', 4, NULL, 100, '15:00:00', '12:00:00', 'MODERATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440006'::uuid, 'Lakefront Paradise Resort', '147 Lakeshore Drive', 'Orlando', 'FL', 'USA', '32801', '+1-407-555-0700', 'welcome@lakefrontparadise.com', 'https://lakefrontparadise.com', 'America/New_York', 'USD', 'RESORT', 5, NULL, 250, '16:00:00', '11:00:00', 'FLEXIBLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440007'::uuid, 'Urban Boutique Hotel', '258 Fashion Avenue', 'Los Angeles', 'CA', 'USA', '90012', '+1-213-555-0800', 'reservations@urbanboutique.com', 'https://urbanboutique.com', 'America/Los_Angeles', 'USD', 'HOTEL', 4, NULL, 75, '15:00:00', '11:00:00', 'MODERATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440008'::uuid, 'Airport Executive Inn', '369 Terminal Road', 'Dallas', 'TX', 'USA', '75201', '+1-214-555-0900', 'info@airportexecutive.com', 'https://airportexecutive.com', 'America/Chicago', 'USD', 'HOTEL', 3, NULL, 90, '14:00:00', '10:00:00', '24_HOURS_FREE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440009'::uuid, 'Garden Valley Retreat', '741 Garden Path', 'Portland', 'OR', 'USA', '97201', '+1-503-555-1000', 'contact@gardenvalley.com', 'https://gardenvalley.com', 'America/Los_Angeles', 'USD', 'RESORT', 4, NULL, 110, '15:00:00', '11:00:00', 'FLEXIBLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Sample Property Amenities (50 amenities across properties)
INSERT INTO property_amenities (id, property_id, amenity_name, amenity_type, description, is_free, created_at) VALUES
-- Grand Plaza Hotel amenities
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440000'::uuid, 'Free WiFi', 'CONNECTIVITY', 'High-speed wireless internet throughout property', true, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440000'::uuid, 'Fitness Center', 'FITNESS', '24/7 state-of-the-art gym', true, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440000'::uuid, 'Swimming Pool', 'RECREATION', 'Indoor heated pool', true, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440000'::uuid, 'Spa & Wellness', 'SPA', 'Full-service spa and massage', false, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440000'::uuid, 'Restaurant', 'DINING', 'Fine dining restaurant', false, CURRENT_TIMESTAMP),
-- Seaside Resort & Spa amenities
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440001'::uuid, 'Free WiFi', 'CONNECTIVITY', 'High-speed wireless internet', true, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440001'::uuid, 'Beach Access', 'RECREATION', 'Private beach access', true, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440001'::uuid, 'Outdoor Pool', 'RECREATION', 'Infinity pool with ocean view', true, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440001'::uuid, 'Water Sports', 'RECREATION', 'Kayaking, paddleboarding, snorkeling', false, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440001'::uuid, 'Full-Service Spa', 'SPA', 'Luxury spa treatments', false, CURRENT_TIMESTAMP),
-- Mountain View Lodge amenities
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440002'::uuid, 'Free WiFi', 'CONNECTIVITY', 'Complimentary internet', true, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440002'::uuid, 'Ski Storage', 'RECREATION', 'Heated ski and snowboard storage', true, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440002'::uuid, 'Hot Tub', 'SPA', 'Outdoor hot tub with mountain views', true, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440002'::uuid, 'Fireplace Lounge', 'COMMON_AREA', 'Cozy lounge with fireplace', true, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440002'::uuid, 'Free Breakfast', 'DINING', 'Continental breakfast included', true, CURRENT_TIMESTAMP),
-- Downtown Business Suites amenities
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440003'::uuid, 'Free WiFi', 'CONNECTIVITY', 'Business-class internet', true, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440003'::uuid, 'Business Center', 'BUSINESS', '24/7 business center with printing', true, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440003'::uuid, 'Meeting Rooms', 'BUSINESS', 'Conference rooms available', false, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440003'::uuid, 'Valet Parking', 'PARKING', 'Valet parking service', false, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440003'::uuid, 'Executive Lounge', 'COMMON_AREA', 'Exclusive lounge for business travelers', false, CURRENT_TIMESTAMP),
-- Coastal Inn & Marina amenities
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440004'::uuid, 'Free WiFi', 'CONNECTIVITY', 'Wireless internet access', true, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440004'::uuid, 'Marina Access', 'RECREATION', 'Private marina slips available', false, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440004'::uuid, 'Seafood Restaurant', 'DINING', 'Fresh seafood dining', false, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440004'::uuid, 'Free Parking', 'PARKING', 'Complimentary parking', true, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440004'::uuid, 'Pet Friendly', 'PET', 'Pets welcome with deposit', false, CURRENT_TIMESTAMP),
-- Historic Heritage Hotel amenities
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440005'::uuid, 'Free WiFi', 'CONNECTIVITY', 'High-speed internet', true, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440005'::uuid, 'Historic Tours', 'RECREATION', 'Guided historic building tours', true, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440005'::uuid, 'Library', 'COMMON_AREA', 'Historic library and reading room', true, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440005'::uuid, 'Fine Dining', 'DINING', 'Award-winning restaurant', false, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440005'::uuid, 'Concierge', 'SERVICE', '24/7 concierge service', true, CURRENT_TIMESTAMP),
-- Lakefront Paradise Resort amenities
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440006'::uuid, 'Free WiFi', 'CONNECTIVITY', 'Resort-wide WiFi', true, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440006'::uuid, 'Water Park', 'RECREATION', 'Family water park', true, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440006'::uuid, 'Kids Club', 'RECREATION', 'Supervised kids activities', false, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440006'::uuid, 'Multiple Pools', 'RECREATION', '5 themed swimming pools', true, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440006'::uuid, 'Golf Course', 'RECREATION', '18-hole championship golf course', false, CURRENT_TIMESTAMP),
-- Urban Boutique Hotel amenities
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440007'::uuid, 'Free WiFi', 'CONNECTIVITY', 'Ultra-fast WiFi', true, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440007'::uuid, 'Rooftop Bar', 'DINING', 'Trendy rooftop bar and lounge', false, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440007'::uuid, 'Art Gallery', 'COMMON_AREA', 'Rotating art exhibitions', true, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440007'::uuid, 'Fashion Concierge', 'SERVICE', 'Personal shopping service', false, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440007'::uuid, 'Designer Furnishings', 'ROOM', 'Luxury designer interiors', true, CURRENT_TIMESTAMP),
-- Airport Executive Inn amenities
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440008'::uuid, 'Free WiFi', 'CONNECTIVITY', 'High-speed internet', true, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440008'::uuid, 'Airport Shuttle', 'TRANSPORTATION', 'Free 24/7 airport shuttle', true, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440008'::uuid, 'Park & Fly', 'PARKING', 'Extended parking packages', false, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440008'::uuid, 'Quick Check-in', 'SERVICE', 'Express check-in kiosks', true, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440008'::uuid, 'Grab & Go Cafe', 'DINING', '24/7 convenience store', true, CURRENT_TIMESTAMP),
-- Garden Valley Retreat amenities
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440009'::uuid, 'Free WiFi', 'CONNECTIVITY', 'Complimentary WiFi', true, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440009'::uuid, 'Yoga Classes', 'FITNESS', 'Daily yoga and meditation', true, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440009'::uuid, 'Organic Restaurant', 'DINING', 'Farm-to-table dining', false, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440009'::uuid, 'Nature Trails', 'RECREATION', 'Hiking and biking trails', true, CURRENT_TIMESTAMP),
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440009'::uuid, 'Garden Tours', 'RECREATION', 'Botanical garden tours', true, CURRENT_TIMESTAMP);
