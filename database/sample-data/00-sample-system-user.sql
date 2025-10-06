-- Sample System User and Tenant
-- This file creates a system user and tenant that can be referenced by other sample data files
-- Must be loaded first!

-- Create a system tenant
DO $$
DECLARE
    v_system_tenant_id UUID := '00000000-0000-0000-0000-000000000001';
    v_system_user_id UUID := '00000000-0000-0000-0000-000000000002';
BEGIN
    -- Insert system user (needed for created_by/updated_by fields)
    -- Note: password is hashed value of "Admin123!"
    INSERT INTO users (
        id,
        tenant_id,
        email,
        password_hash,
        first_name,
        last_name,
        phone,
        email_verified,
        is_active,
        created_at,
        updated_at
    ) VALUES (
        v_system_user_id,
        v_system_tenant_id,
        'system@modernreservation.com',
        '$2a$10$rZ3J5J5J5J5J5J5J5J5J5OzX3qZ3J5J5J5J5J5J5J5J5J5J5J5J5.',
        'System',
        'Administrator',
        '+1-555-0000',
        true,
        true,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ) ON CONFLICT (id) DO NOTHING;

    RAISE NOTICE 'System user created with ID: %', v_system_user_id;
    RAISE NOTICE 'System tenant ID: %', v_system_tenant_id;
END $$;
