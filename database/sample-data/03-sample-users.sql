-- Sample Users and Roles Data
-- This file contains sample data for users, roles, and user management

-- Sample Users (50 users)
DO $$
DECLARE
    role_admin_id UUID;
    role_manager_id UUID;
    role_staff_id UUID;
    role_viewer_id UUID;
    user_id UUID;
    i INTEGER;
    property_ids UUID[];
BEGIN
    -- Get role IDs
    SELECT id INTO role_admin_id FROM roles WHERE name = 'ADMIN' LIMIT 1;
    SELECT id INTO role_manager_id FROM roles WHERE name = 'MANAGER' LIMIT 1;
    SELECT id INTO role_staff_id FROM roles WHERE name = 'STAFF' LIMIT 1;
    SELECT id INTO role_viewer_id FROM roles WHERE name = 'VIEWER' LIMIT 1;

    -- Get property IDs
    SELECT ARRAY_AGG(id) INTO property_ids FROM properties;

    -- Create 50 users
    FOR i IN 1..50 LOOP
        user_id := gen_random_uuid();

        INSERT INTO users (
            id, username, email, password_hash, first_name, last_name,
            phone, is_active, is_verified, account_locked, failed_login_attempts,
            last_login_at, created_at, updated_at
        ) VALUES (
            user_id,
            'user' || i::TEXT,
            'user' || i::TEXT || '@modernreservation.com',
            '$2a$10$' || MD5(RANDOM()::TEXT),  -- Mock password hash
            CASE (i % 10)
                WHEN 0 THEN 'Admin'
                WHEN 1 THEN 'Manager'
                WHEN 2 THEN 'Staff'
                ELSE 'User'
            END || i::TEXT,
            'LastName' || i::TEXT,
            '+1-555-' || LPAD(i::TEXT, 7, '0'),
            true,
            (i % 5 != 0),  -- 80% verified
            false,
            0,
            CURRENT_TIMESTAMP - ((i % 30) || ' days')::INTERVAL,
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        );

        -- Assign roles
        IF i <= 5 THEN
            -- First 5 users are admins
            IF role_admin_id IS NOT NULL THEN
                INSERT INTO user_roles (id, user_id, role_id, assigned_at, assigned_by, created_at)
                VALUES (gen_random_uuid(), user_id, role_admin_id, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
            END IF;
        ELSIF i <= 15 THEN
            -- Next 10 are managers
            IF role_manager_id IS NOT NULL THEN
                INSERT INTO user_roles (id, user_id, role_id, assigned_at, assigned_by, created_at)
                VALUES (gen_random_uuid(), user_id, role_manager_id, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
            END IF;
        ELSIF i <= 35 THEN
            -- Next 20 are staff
            IF role_staff_id IS NOT NULL THEN
                INSERT INTO user_roles (id, user_id, role_id, assigned_at, assigned_by, created_at)
                VALUES (gen_random_uuid(), user_id, role_staff_id, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
            END IF;
        ELSE
            -- Rest are viewers
            IF role_viewer_id IS NOT NULL THEN
                INSERT INTO user_roles (id, user_id, role_id, assigned_at, assigned_by, created_at)
                VALUES (gen_random_uuid(), user_id, role_viewer_id, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
            END IF;
        END IF;

        -- Assign to properties (2-3 properties per user)
        IF property_ids IS NOT NULL AND ARRAY_LENGTH(property_ids, 1) > 0 THEN
            FOR j IN 1..(2 + (i % 2)) LOOP
                IF j <= ARRAY_LENGTH(property_ids, 1) THEN
                    INSERT INTO user_properties (id, user_id, property_id, is_primary, created_at)
                    VALUES (
                        gen_random_uuid(),
                        user_id,
                        property_ids[((i + j - 1) % ARRAY_LENGTH(property_ids, 1)) + 1],
                        (j = 1),
                        CURRENT_TIMESTAMP
                    );
                END IF;
            END LOOP;
        END IF;

        -- Create API key for some users (every 3rd user)
        IF i % 3 = 0 THEN
            INSERT INTO api_keys (
                id, user_id, key_name, api_key, key_secret,
                is_active, expires_at, last_used_at, created_at
            ) VALUES (
                gen_random_uuid(),
                user_id,
                'API Key ' || i::TEXT,
                'ak_' || MD5(RANDOM()::TEXT),
                MD5(RANDOM()::TEXT),
                true,
                CURRENT_TIMESTAMP + INTERVAL '1 year',
                CURRENT_TIMESTAMP - ((i % 15) || ' days')::INTERVAL,
                CURRENT_TIMESTAMP
            );
        END IF;

        -- Create session for active users (last 20 users)
        IF i > 30 THEN
            INSERT INTO user_sessions (
                id, user_id, session_token, ip_address, user_agent,
                expires_at, last_activity_at, created_at
            ) VALUES (
                gen_random_uuid(),
                user_id,
                MD5(RANDOM()::TEXT || i::TEXT),
                '192.168.' || ((i % 255) + 1)::TEXT || '.' || ((i % 255) + 1)::TEXT,
                'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
                CURRENT_TIMESTAMP + INTERVAL '24 hours',
                CURRENT_TIMESTAMP,
                CURRENT_TIMESTAMP
            );
        END IF;
    END LOOP;
END $$;

-- Add user activity logs (100 logs)
DO $$
DECLARE
    user_rec RECORD;
    counter INTEGER := 0;
    actions TEXT[] := ARRAY['LOGIN', 'LOGOUT', 'CREATE_RESERVATION', 'UPDATE_RESERVATION',
                             'VIEW_REPORT', 'UPDATE_PROFILE', 'CREATE_GUEST', 'UPDATE_GUEST'];
BEGIN
    FOR user_rec IN (SELECT id FROM users ORDER BY id LIMIT 50) LOOP
        FOR i IN 1..2 LOOP
            counter := counter + 1;
            INSERT INTO user_activity_logs (
                id, user_id, action, resource_type, resource_id,
                ip_address, user_agent, created_at
            ) VALUES (
                gen_random_uuid(),
                user_rec.id,
                actions[((counter - 1) % 8) + 1],
                CASE ((counter - 1) % 4)
                    WHEN 0 THEN 'RESERVATION'
                    WHEN 1 THEN 'GUEST'
                    WHEN 2 THEN 'USER'
                    ELSE 'REPORT'
                END,
                gen_random_uuid(),
                '192.168.' || ((counter % 255) + 1)::TEXT || '.' || ((counter % 255) + 1)::TEXT,
                'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
                CURRENT_TIMESTAMP - ((counter % 60) || ' hours')::INTERVAL
            );
        END LOOP;
    END LOOP;
END $$;
