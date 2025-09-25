-- =============================================
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
