-- Sample Guests Data
-- This file contains sample data for guests, addresses, loyalty programs, and preferences

-- First create loyalty programs
INSERT INTO loyalty_programs (id, program_name, description, points_per_dollar, tier_benefits, created_at, updated_at) VALUES
(gen_random_uuid(), 'Gold Rewards', 'Premium loyalty program with exclusive benefits', 10.00, '{"tier1": "5% discount", "tier2": "10% discount", "tier3": "15% discount"}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Silver Rewards', 'Standard loyalty program', 5.00, '{"tier1": "3% discount", "tier2": "5% discount"}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Platinum Elite', 'Elite tier with maximum benefits', 15.00, '{"tier1": "10% discount", "tier2": "20% discount", "tier3": "25% discount"}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Create preference categories
INSERT INTO preference_categories (id, category_name, description, display_order, created_at) VALUES
(gen_random_uuid(), 'Room Preferences', 'Guest room preferences', 1, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Dietary Preferences', 'Food and dietary requirements', 2, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Communication Preferences', 'How guests prefer to be contacted', 3, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Special Requests', 'Additional special requirements', 4, CURRENT_TIMESTAMP);

-- Generate 100 Sample Guests
DO $$
DECLARE
    first_names TEXT[] := ARRAY['John', 'Jane', 'Michael', 'Sarah', 'David', 'Emily', 'James', 'Emma', 'Robert', 'Olivia',
                                 'William', 'Ava', 'Richard', 'Isabella', 'Joseph', 'Sophia', 'Thomas', 'Mia', 'Charles', 'Charlotte',
                                 'Christopher', 'Amelia', 'Daniel', 'Harper', 'Matthew', 'Evelyn', 'Anthony', 'Abigail', 'Mark', 'Elizabeth',
                                 'Donald', 'Sofia', 'Steven', 'Avery', 'Paul', 'Ella', 'Andrew', 'Scarlett', 'Joshua', 'Grace'];
    last_names TEXT[] := ARRAY['Smith', 'Johnson', 'Williams', 'Brown', 'Jones', 'Garcia', 'Miller', 'Davis', 'Rodriguez', 'Martinez',
                                'Hernandez', 'Lopez', 'Gonzalez', 'Wilson', 'Anderson', 'Thomas', 'Taylor', 'Moore', 'Jackson', 'Martin',
                                'Lee', 'Perez', 'Thompson', 'White', 'Harris', 'Sanchez', 'Clark', 'Ramirez', 'Lewis', 'Robinson'];
    guest_id UUID;
    i INTEGER;
    first_name TEXT;
    last_name TEXT;
    email TEXT;
    phone TEXT;
BEGIN
    FOR i IN 1..100 LOOP
        guest_id := gen_random_uuid();
        first_name := first_names[((i - 1) % 40) + 1];
        last_name := last_names[((i - 1) % 30) + 1];
        email := lower(first_name || '.' || last_name || i::TEXT || '@email.com');
        phone := '+1-' || (200 + (i % 800))::TEXT || '-555-' || LPAD((1000 + i)::TEXT, 4, '0');

        -- Insert guest
        INSERT INTO guests (
            id, first_name, last_name, email, phone, date_of_birth,
            nationality, preferred_language, id_type, id_number,
            vip_status, guest_type, created_at, updated_at
        ) VALUES (
            guest_id,
            first_name,
            last_name,
            email,
            phone,
            DATE '1960-01-01' + (i * 100 || ' days')::INTERVAL,
            CASE (i % 5)
                WHEN 0 THEN 'US'
                WHEN 1 THEN 'UK'
                WHEN 2 THEN 'CA'
                WHEN 3 THEN 'AU'
                ELSE 'US'
            END,
            CASE (i % 3)
                WHEN 0 THEN 'en'
                WHEN 1 THEN 'es'
                ELSE 'en'
            END,
            CASE (i % 3)
                WHEN 0 THEN 'PASSPORT'
                WHEN 1 THEN 'DRIVERS_LICENSE'
                ELSE 'NATIONAL_ID'
            END,
            'ID' || LPAD(i::TEXT, 8, '0'),
            (i % 10 = 0),  -- 10% VIP guests
            CASE (i % 4)
                WHEN 0 THEN 'INDIVIDUAL'
                WHEN 1 THEN 'BUSINESS'
                WHEN 2 THEN 'GROUP'
                ELSE 'INDIVIDUAL'
            END,
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        );

        -- Insert guest address
        INSERT INTO guest_addresses (
            id, guest_id, address_type, street_address, city, state, country,
            postal_code, is_primary, created_at, updated_at
        ) VALUES (
            gen_random_uuid(),
            guest_id,
            'HOME',
            (100 + i)::TEXT || ' Main Street',
            CASE (i % 10)
                WHEN 0 THEN 'New York'
                WHEN 1 THEN 'Los Angeles'
                WHEN 2 THEN 'Chicago'
                WHEN 3 THEN 'Houston'
                WHEN 4 THEN 'Phoenix'
                WHEN 5 THEN 'Philadelphia'
                WHEN 6 THEN 'San Antonio'
                WHEN 7 THEN 'San Diego'
                WHEN 8 THEN 'Dallas'
                ELSE 'San Jose'
            END,
            CASE (i % 10)
                WHEN 0 THEN 'NY'
                WHEN 1 THEN 'CA'
                WHEN 2 THEN 'IL'
                WHEN 3 THEN 'TX'
                WHEN 4 THEN 'AZ'
                WHEN 5 THEN 'PA'
                WHEN 6 THEN 'TX'
                WHEN 7 THEN 'CA'
                WHEN 8 THEN 'TX'
                ELSE 'CA'
            END,
            'USA',
            LPAD((10000 + i)::TEXT, 5, '0'),
            true,
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        );
    END LOOP;
END $$;

-- Add guest loyalty memberships (80 guests have loyalty)
DO $$
DECLARE
    guest_rec RECORD;
    loyalty_id UUID;
    counter INTEGER := 0;
BEGIN
    FOR guest_rec IN (SELECT id FROM guests ORDER BY id LIMIT 80) LOOP
        SELECT id INTO loyalty_id FROM loyalty_programs ORDER BY RANDOM() LIMIT 1;
        counter := counter + 1;

        INSERT INTO guest_loyalty (
            id, guest_id, loyalty_program_id, membership_number, points_balance,
            tier_level, enrollment_date, last_activity_date, created_at, updated_at
        ) VALUES (
            gen_random_uuid(),
            guest_rec.id,
            loyalty_id,
            'LM' || LPAD(counter::TEXT, 10, '0'),
            (counter * 100 + RANDOM() * 5000)::INTEGER,
            CASE (counter % 3)
                WHEN 0 THEN 'GOLD'
                WHEN 1 THEN 'SILVER'
                ELSE 'PLATINUM'
            END,
            CURRENT_DATE - (counter * 10 || ' days')::INTERVAL,
            CURRENT_DATE - ((counter % 30) || ' days')::INTERVAL,
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        );
    END LOOP;
END $$;

-- Add guest preferences (150 preferences)
DO $$
DECLARE
    guest_rec RECORD;
    category_rec RECORD;
    pref_values TEXT[] := ARRAY[
        'High Floor', 'Low Floor', 'Away from Elevator', 'Near Elevator',
        'King Bed', 'Twin Beds', 'Non-Smoking', 'Smoking',
        'Vegetarian', 'Vegan', 'Gluten-Free', 'Kosher',
        'Email', 'SMS', 'Phone', 'No Contact',
        'Extra Pillows', 'Feather-Free', 'Hypoallergenic', 'Late Checkout'
    ];
    counter INTEGER := 0;
BEGIN
    FOR guest_rec IN (SELECT id FROM guests ORDER BY id LIMIT 75) LOOP
        FOR category_rec IN (SELECT id FROM preference_categories ORDER BY display_order LIMIT 2) LOOP
            counter := counter + 1;
            INSERT INTO guest_preferences (
                id, guest_id, category_id, preference_key, preference_value,
                notes, created_at, updated_at
            ) VALUES (
                gen_random_uuid(),
                guest_rec.id,
                category_rec.id,
                'PREF_' || counter::TEXT,
                pref_values[((counter - 1) % 20) + 1],
                'Guest preference note',
                CURRENT_TIMESTAMP,
                CURRENT_TIMESTAMP
            );
        END LOOP;
    END LOOP;
END $$;

-- Add guest communications (100 communication records)
DO $$
DECLARE
    guest_rec RECORD;
    counter INTEGER := 0;
BEGIN
    FOR guest_rec IN (SELECT id FROM guests ORDER BY id LIMIT 100) LOOP
        counter := counter + 1;
        INSERT INTO guest_communications (
            id, guest_id, communication_type, subject, message,
            sent_at, sent_by, created_at
        ) VALUES (
            gen_random_uuid(),
            guest_rec.id,
            CASE (counter % 3)
                WHEN 0 THEN 'EMAIL'
                WHEN 1 THEN 'SMS'
                ELSE 'PHONE'
            END,
            CASE (counter % 4)
                WHEN 0 THEN 'Booking Confirmation'
                WHEN 1 THEN 'Special Offer'
                WHEN 2 THEN 'Feedback Request'
                ELSE 'Welcome Message'
            END,
            'Sample communication message for guest',
            CURRENT_TIMESTAMP - ((counter % 60) || ' days')::INTERVAL,
            'system',
            CURRENT_TIMESTAMP
        );
    END LOOP;
END $$;
