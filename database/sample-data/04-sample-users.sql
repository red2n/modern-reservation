-- Sample Users with Roles and Permissions
-- Creates 50 users with various roles

DO $$
DECLARE
    v_system_tenant_id UUID := '00000000-0000-0000-0000-000000000001';
    v_system_user_id UUID := '00000000-0000-0000-0000-000000000002';
    v_property_ids UUID[];
    v_user_id UUID;
    v_role_ids UUID[];
    v_admin_role_id UUID;
    v_manager_role_id UUID;
    v_staff_role_id UUID;
    v_viewer_role_id UUID;
    i INT;
    user_names TEXT[] := ARRAY['admin', 'manager', 'frontdesk', 'receptionist', 'housekeeper', 'maintenance', 'accountant', 'sales', 'supervisor', 'coordinator'];
BEGIN
    -- Get all property IDs
    SELECT ARRAY_AGG(id) INTO v_property_ids FROM properties WHERE tenant_id = v_system_tenant_id LIMIT 10;

    -- Create roles
    INSERT INTO roles (name, description, is_system_role, permissions, is_active) VALUES
        ('Administrator', 'Full system access', true,
         '["users:*", "properties:*", "reservations:*", "payments:*", "reports:*", "settings:*"]'::jsonb, true)
    RETURNING id INTO v_admin_role_id;

    INSERT INTO roles (name, description, is_system_role, permissions, is_active) VALUES
        ('Property Manager', 'Manage property operations', true,
         '["properties:read", "properties:update", "reservations:*", "guests:*", "reports:read"]'::jsonb, true)
    RETURNING id INTO v_manager_role_id;

    INSERT INTO roles (name, description, is_system_role, permissions, is_active) VALUES
        ('Front Desk Staff', 'Handle check-ins and guest services', true,
         '["reservations:read", "reservations:create", "reservations:update", "guests:read", "guests:update", "rooms:read"]'::jsonb, true)
    RETURNING id INTO v_staff_role_id;

    INSERT INTO roles (name, description, is_system_role, permissions, is_active) VALUES
        ('Viewer', 'Read-only access', true,
         '["properties:read", "reservations:read", "guests:read", "reports:read"]'::jsonb, true)
    RETURNING id INTO v_viewer_role_id;

    v_role_ids := ARRAY[v_admin_role_id, v_manager_role_id, v_staff_role_id, v_viewer_role_id];

    -- Create 50 users
    FOR i IN 1..50 LOOP
        INSERT INTO users (
            tenant_id, email, password_hash, first_name, last_name, phone,
            email_verified, email_verified_at, last_login_at,
            is_active, created_by, updated_by
        ) VALUES (
            v_system_tenant_id,
            user_names[1 + (i % 10)] || i || '@modernreservation.com',
            '$2a$10$' || MD5(random()::TEXT) || MD5(random()::TEXT),  -- Mock bcrypt hash
            INITCAP(user_names[1 + (i % 10)]),
            'User' || i,
            '+1-555-' || LPAD(i::TEXT, 4, '0'),
            true,
            CURRENT_TIMESTAMP - (i || ' days')::INTERVAL,
            CASE WHEN i % 3 = 0 THEN CURRENT_TIMESTAMP - (i % 10 || ' hours')::INTERVAL ELSE NULL END,
            true,
            v_system_user_id,
            v_system_user_id
        ) RETURNING id INTO v_user_id;

        -- Assign roles based on user number
        IF i <= 5 THEN
            -- First 5 are admins
            INSERT INTO user_roles (user_id, role_id, assigned_by, is_active)
            VALUES (v_user_id, v_admin_role_id, v_system_user_id, true);
        ELSIF i <= 15 THEN
            -- Next 10 are managers
            INSERT INTO user_roles (user_id, role_id, assigned_by, is_active)
            VALUES (v_user_id, v_manager_role_id, v_system_user_id, true);
        ELSIF i <= 35 THEN
            -- Next 20 are staff
            INSERT INTO user_roles (user_id, role_id, assigned_by, is_active)
            VALUES (v_user_id, v_staff_role_id, v_system_user_id, true);
        ELSE
            -- Rest are viewers
            INSERT INTO user_roles (user_id, role_id, assigned_by, is_active)
            VALUES (v_user_id, v_viewer_role_id, v_system_user_id, true);
        END IF;

        -- Assign users to properties
        -- Admins get access to all properties
        IF i <= 5 THEN
            FOR j IN 1..array_length(v_property_ids, 1) LOOP
                INSERT INTO user_properties (user_id, property_id, role, permissions, assigned_by, is_active)
                VALUES (
                    v_user_id,
                    v_property_ids[j],
                    'administrator',
                    '["*"]'::jsonb,
                    v_system_user_id,
                    true
                );
            END LOOP;
        -- Others get assigned to specific properties
        ELSIF v_property_ids IS NOT NULL AND array_length(v_property_ids, 1) > 0 THEN
            INSERT INTO user_properties (user_id, property_id, role, permissions, assigned_by, is_active)
            VALUES (
                v_user_id,
                v_property_ids[1 + (i % array_length(v_property_ids, 1))],
                CASE
                    WHEN i <= 15 THEN 'property_manager'
                    WHEN i <= 35 THEN 'front_desk'
                    ELSE 'viewer'
                END,
                CASE
                    WHEN i <= 15 THEN '["reservations:*", "guests:*", "rooms:*"]'
                    WHEN i <= 35 THEN '["reservations:create", "reservations:read", "guests:read", "guests:update"]'
                    ELSE '["reservations:read", "guests:read"]'
                END::jsonb,
                v_system_user_id,
                true
            );
        END IF;

        -- Create API keys for every 3rd user
        IF i % 3 = 0 THEN
            INSERT INTO api_keys (
                name, key_hash, key_prefix, permissions,
                rate_limit_per_minute, created_by, property_id,
                is_active, expires_at
            ) VALUES (
                'API Key for ' || user_names[1 + (i % 10)] || i,
                MD5(v_user_id::TEXT || random()::TEXT),
                'mrk_' || SUBSTRING(MD5(random()::TEXT), 1, 8),
                '["reservations:read", "availability:read"]'::jsonb,
                1000,
                v_user_id,
                CASE WHEN i % 6 = 0 THEN v_property_ids[1 + (i % array_length(v_property_ids, 1))] ELSE NULL END,
                true,
                CURRENT_TIMESTAMP + INTERVAL '1 year'
            );
        END IF;

        -- Create active sessions for recent users
        IF i <= 10 THEN
            INSERT INTO user_sessions (
                user_id, session_token, refresh_token,
                ip_address, user_agent, device_info,
                created_at, last_accessed_at, expires_at, is_active
            ) VALUES (
                v_user_id,
                MD5(random()::TEXT || v_user_id::TEXT),
                MD5(random()::TEXT || v_user_id::TEXT || 'refresh'),
                ('192.168.1.' || (i % 255))::INET,
                'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
                FORMAT('{"browser": "Chrome", "os": "Windows", "device": "Desktop"}')::jsonb,
                CURRENT_TIMESTAMP - (i || ' hours')::INTERVAL,
                CURRENT_TIMESTAMP - ((i % 5) || ' minutes')::INTERVAL,
                CURRENT_TIMESTAMP + INTERVAL '7 days',
                true
            );
        END IF;
    END LOOP;

    RAISE NOTICE '50 users created: 5 admins, 10 managers, 20 staff, 15 viewers';
    RAISE NOTICE 'API keys created: %', (SELECT COUNT(*) FROM api_keys);
    RAISE NOTICE 'Active sessions: %', (SELECT COUNT(*) FROM user_sessions WHERE is_active = true);
END $$;
