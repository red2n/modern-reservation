-- =============-- Execute modular schema files in correct dependency order
\i ../schema/00-extensions-and-types.sql
\i ../schema/01-property-management.sql
\i ../schema/02-guest-management.sql
\i ../schema/03-reservation-management.sql
\i ../schema/04-payment-management.sql
\i ../schema/05-availability-rate-management.sql
\i ../schema/06-user-management.sql
\i ../schema/07-audit-and-events.sql
\i ../schema/08-notifications.sql

-- Create application user
CREATE USER IF NOT EXISTS modernreservation_app WITH ENCRYPTED PASSWORD 'change_me_in_production';
GRANT CONNECT ON DATABASE modernreservation TO modernreservation_app;
GRANT USAGE ON SCHEMA public TO modernreservation_app;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO modernreservation_app;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO modernreservation_app;

-- Record migration
INSERT INTO schema_migrations (version, description, execution_time_ms, checksum)
VALUES (
    'V001',
    'Initial modular schema setup with all domain tables',
    EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - CURRENT_TIMESTAMP)) * 1000,
    'v001_modular_schema'
);

COMMIT;=====================
-- Database Migration V001
-- Initial Schema Setup
-- =============================================

BEGIN;

-- Create migration tracking table
CREATE TABLE IF NOT EXISTS schema_migrations (
    version VARCHAR(50) PRIMARY KEY,
    description TEXT,
    executed_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    execution_time_ms INTEGER,
    checksum VARCHAR(64)
);

-- Execute core schema
\i 01-core-schema.sql

-- Execute audit system schema
\i 02-audit-system.sql

-- Record migration
INSERT INTO schema_migrations (version, description, checksum) VALUES
('001', 'Initial core schema and audit system setup', md5(current_timestamp::text));

COMMIT;

-- Verify installation
DO $$
DECLARE
    table_count INTEGER;
    index_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO table_count FROM information_schema.tables
    WHERE table_schema = 'public' AND table_type = 'BASE TABLE';

    SELECT COUNT(*) INTO index_count FROM pg_indexes
    WHERE schemaname = 'public';

    RAISE NOTICE 'Migration V001 completed successfully:';
    RAISE NOTICE '  - Tables created: %', table_count;
    RAISE NOTICE '  - Indexes created: %', index_count;
    RAISE NOTICE '  - Ready for ultra-scale operations';
END $$;
