-- =====================================================
-- master-schema.sql
-- Master Schema Deployment Script
-- Runs all schema files in correct order
-- Generated: 2025-10-06
-- =====================================================

\echo '=========================================='
\echo 'Modern Reservation System - Database Setup'
\echo 'Generated from JPA Entity Classes'
\echo 'Version: 1.0.0'
\echo '=========================================='
\echo ''

\echo 'Step 1/5: Creating extensions and enum types...'
\i 01-extensions-and-types.sql
\echo '✓ Extensions and types created'
\echo ''

\echo 'Step 2/5: Creating core tables...'
\i 02-core-tables.sql
\echo '✓ Core tables created'
\echo ''

\echo 'Step 3/5: Creating indexes...'
\i 03-indexes.sql
\echo '✓ Indexes created'
\echo ''

\echo 'Step 4/5: Adding constraints...'
\i 04-constraints.sql
\echo '✓ Constraints added'
\echo ''

\echo 'Step 5/5: Loading reference data and triggers...'
\i 05-reference-data.sql
\echo '✓ Reference data and triggers loaded'
\echo ''

\echo '=========================================='
\echo 'Database schema deployment complete!'
\echo ''
\echo 'Tables created:'
\echo '  - rates'
\echo '  - reservations'
\echo '  - reservation_status_history'
\echo '  - payments'
\echo '  - availability.room_availability'
\echo '  - analytics_metrics'
\echo '  - analytics_metric_dimensions'
\echo '  - analytics_reports'
\echo '  - report_property_ids'
\echo ''
\echo 'Total: 9 tables across 2 schemas (public, availability)'
\echo '=========================================='
\echo ''

-- Display table counts
\echo 'Verifying schema deployment...'
\echo ''

SELECT 'rates' as table_name, count(*) as row_count FROM rates
UNION ALL
SELECT 'reservations', count(*) FROM reservations
UNION ALL
SELECT 'reservation_status_history', count(*) FROM reservation_status_history
UNION ALL
SELECT 'payments', count(*) FROM payments
UNION ALL
SELECT 'room_availability', count(*) FROM availability.room_availability
UNION ALL
SELECT 'analytics_metrics', count(*) FROM analytics_metrics
UNION ALL
SELECT 'analytics_reports', count(*) FROM analytics_reports
ORDER BY table_name;

\echo ''
\echo 'Schema version:'
SELECT * FROM schema_version;

\echo ''
\echo '✅ All schema objects created successfully!'
