-- Sample Guests with Addresses, Loyalty, and Preferences
-- Creates 100 sample guests with complete profiles

DO $$
DECLARE
    v_system_tenant_id UUID := '00000000-0000-0000-0000-000000000001';
    v_system_user_id UUID := '00000000-0000-0000-0000-000000000002';
    v_guest_ids UUID[];
    v_guest_id UUID;
    v_loyalty_program_id UUID;
    v_pref_cat_ids UUID[];
    i INT;
    first_names TEXT[] := ARRAY['James', 'Mary', 'John', 'Patricia', 'Robert', 'Jennifer', 'Michael', 'Linda', 'William', 'Elizabeth',
                                  'David', 'Barbara', 'Richard', 'Susan', 'Joseph', 'Jessica', 'Thomas', 'Sarah', 'Charles', 'Karen',
                                  'Christopher', 'Nancy', 'Daniel', 'Lisa', 'Matthew', 'Betty', 'Anthony', 'Margaret', 'Mark', 'Sandra',
                                  'Donald', 'Ashley', 'Steven', 'Kimberly', 'Paul', 'Emily', 'Andrew', 'Donna', 'Joshua', 'Michelle'];
    last_names TEXT[] := ARRAY['Smith', 'Johnson', 'Williams', 'Brown', 'Jones', 'Garcia', 'Miller', 'Davis', 'Rodriguez', 'Martinez',
                                'Hernandez', 'Lopez', 'Gonzalez', 'Wilson', 'Anderson', 'Thomas', 'Taylor', 'Moore', 'Jackson', 'Martin',
                                'Lee', 'Perez', 'Thompson', 'White', 'Harris', 'Sanchez', 'Clark', 'Ramirez', 'Lewis', 'Robinson'];
    cities TEXT[] := ARRAY['New York', 'Los Angeles', 'Chicago', 'Houston', 'Phoenix', 'Philadelphia', 'San Antonio', 'San Diego', 'Dallas', 'San Jose',
                           'Austin', 'Jacksonville', 'Fort Worth', 'Columbus', 'San Francisco', 'Charlotte', 'Indianapolis', 'Seattle', 'Denver', 'Washington'];
    states TEXT[] := ARRAY['NY', 'CA', 'IL', 'TX', 'AZ', 'PA', 'TX', 'CA', 'TX', 'CA',
                           'TX', 'FL', 'TX', 'OH', 'CA', 'NC', 'IN', 'WA', 'CO', 'DC'];
BEGIN
    -- Create loyalty program
    INSERT INTO loyalty_programs (
        tenant_id, name, description, tiers, points_per_dollar, is_active,
        created_by, updated_by
    ) VALUES (
        v_system_tenant_id,
        'Rewards Plus',
        'Earn points on every stay and unlock exclusive benefits',
        '{"bronze": {"min_points": 0, "benefits": ["Free WiFi"]},
          "silver": {"min_points": 1000, "benefits": ["Free WiFi", "Late Checkout", "Room Upgrade"]},
          "gold": {"min_points": 5000, "benefits": ["Free WiFi", "Late Checkout", "Room Upgrade", "Free Breakfast", "Lounge Access"]},
          "platinum": {"min_points": 10000, "benefits": ["Free WiFi", "Late Checkout", "Room Upgrade", "Free Breakfast", "Lounge Access", "Free Parking"]}}'::jsonb,
        10.00,
        true,
        v_system_user_id,
        v_system_user_id
    ) RETURNING id INTO v_loyalty_program_id;

    -- Create preference categories
    INSERT INTO preference_categories (name, description, category_type, options) VALUES
        ('Room Type', 'Preferred room type', 'room', '["King Bed", "Queen Bed", "Twin Beds", "Suite"]'::jsonb),
        ('Floor Level', 'Preferred floor', 'room', '["High Floor", "Low Floor", "Ground Floor", "No Preference"]'::jsonb),
        ('Pillow Type', 'Preferred pillow', 'room', '["Firm", "Soft", "Hypoallergenic", "Memory Foam"]'::jsonb),
        ('Smoking', 'Smoking preference', 'room', '["Non-Smoking", "Smoking"]'::jsonb),
        ('View', 'Room view preference', 'room', '["Ocean View", "City View", "Garden View", "No Preference"]'::jsonb);

    -- Get the preference category IDs
    SELECT ARRAY_AGG(id ORDER BY name) INTO v_pref_cat_ids FROM preference_categories WHERE category_type = 'room';

    -- Create 100 guests
    FOR i IN 1..100 LOOP
        INSERT INTO guests (
            tenant_id, email, first_name, last_name, phone,
            date_of_birth, nationality, passport_number,
            preferences, emergency_contact, address,
            total_stays, total_spent, is_vip,
            marketing_consent, communication_preferences
        ) VALUES (
            v_system_tenant_id,
            LOWER(first_names[1 + (i % 40)]) || '.' || LOWER(last_names[1 + (i % 30)]) || i || '@email.com',
            first_names[1 + (i % 40)],
            last_names[1 + (i % 30)],
            '+1-' || LPAD((200 + (i % 800))::TEXT, 3, '0') || '-555-' || LPAD(i::TEXT, 4, '0'),
            (CURRENT_DATE - INTERVAL '25 years' - (i || ' days')::INTERVAL)::DATE,
            'USA',
            'US' || LPAD(i::TEXT, 7, '0'),
            '{"newsletter": true, "sms_notifications": false}'::jsonb,
            FORMAT('{"name": "%s", "phone": "+1-555-%s", "relationship": "spouse"}',
                   first_names[1 + ((i + 10) % 40)], LPAD((1000 + i)::TEXT, 4, '0'))::jsonb,
            FORMAT('{"street": "%s Main St", "city": "%s", "state": "%s", "country": "USA", "postal_code": "%s"}',
                   (100 + i), cities[1 + (i % 20)], states[1 + (i % 20)], LPAD((10000 + i)::TEXT, 5, '0'))::jsonb,
            (i % 20),
            (i * 247.50)::DECIMAL(12,2),
            (i % 20 = 0),
            (i % 3 != 0),
            '{"email": true, "sms": false, "phone": false}'::jsonb
        ) RETURNING id INTO v_guest_id;

        v_guest_ids := array_append(v_guest_ids, v_guest_id);

        -- Create guest address record
        INSERT INTO guest_addresses (
            guest_id, address_type, street, city, state, country, postal_code, is_default
        ) VALUES (
            v_guest_id,
            'primary',
            (100 + i) || ' Main St',
            cities[1 + (i % 20)],
            states[1 + (i % 20)],
            'USA',
            LPAD((10000 + i)::TEXT, 5, '0'),
            true
        );

        -- Create loyalty membership (for 80% of guests)
        IF i % 5 != 0 THEN
            INSERT INTO guest_loyalty (
                guest_id, loyalty_program_id, loyalty_number,
                current_tier, total_points, available_points, tier_progress
            ) VALUES (
                v_guest_id,
                v_loyalty_program_id,
                'RP' || LPAD(i::TEXT, 8, '0'),
                CASE
                    WHEN i % 20 = 0 THEN 'platinum'
                    WHEN i % 10 = 0 THEN 'gold'
                    WHEN i % 5 = 0 THEN 'silver'
                    ELSE 'bronze'
                END,
                i * 100,
                i * 50,
                i * 10
            );
        END IF;

        -- Create preferences (2-3 per guest)
        IF i % 2 = 0 THEN
            INSERT INTO guest_preferences (guest_id, category_id, preference_value, priority)
            SELECT v_guest_id, v_pref_cat_ids[1], 'King Bed', 1
            WHERE v_pref_cat_ids[1] IS NOT NULL;
        END IF;

        IF i % 3 = 0 THEN
            INSERT INTO guest_preferences (guest_id, category_id, preference_value, priority)
            SELECT v_guest_id, v_pref_cat_ids[2], 'High Floor', 1
            WHERE v_pref_cat_ids[2] IS NOT NULL;
        END IF;

        IF i % 4 = 0 THEN
            INSERT INTO guest_preferences (guest_id, category_id, preference_value, priority)
            SELECT v_guest_id, v_pref_cat_ids[5], 'Ocean View', 2
            WHERE v_pref_cat_ids[5] IS NOT NULL;
        END IF;
    END LOOP;

    RAISE NOTICE '100 guests created with addresses, % loyalty memberships, and preferences',
                 (SELECT COUNT(*) FROM guest_loyalty);
END $$;
