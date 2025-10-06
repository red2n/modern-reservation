-- Master Sample Data Loader Script
-- Loads all sample data in the correct order
-- Run this after the schema is created

\echo '========================================='
\echo 'Loading Sample Data for Modern Reservation System'
\echo '========================================='
\echo ''

-- Set client encoding
SET client_encoding = 'UTF8';

\echo '📊 Step 1/6: Loading Rates (500 records)...'
\i /home/subramani/modern-reservation/database/sample-data/01-sample-rates.sql
\echo '✅ Rates loaded'
\echo ''

\echo '🏨 Step 2/6: Loading Reservations (500 records)...'
\i /home/subramani/modern-reservation/database/sample-data/02-sample-reservations.sql
\echo '✅ Reservations loaded'
\echo ''

\echo '💳 Step 3/6: Loading Payments (500 records)...'
\i /home/subramani/modern-reservation/database/sample-data/03-sample-payments.sql
\echo '✅ Payments loaded'
\echo ''

\echo '📅 Step 4/6: Loading Room Availability (500 records)...'
\i /home/subramani/modern-reservation/database/sample-data/04-sample-room-availability.sql
\echo '✅ Room Availability loaded'
\echo ''

\echo '📈 Step 5/6: Loading Analytics (500 metrics + 100 reports)...'
\i /home/subramani/modern-reservation/database/sample-data/05-sample-analytics.sql
\echo '✅ Analytics loaded'
\echo ''

\echo '📝 Step 6/6: Loading Reservation Audit History (200 records)...'
\i /home/subramani/modern-reservation/database/sample-data/06-sample-reservation-audit.sql
\echo '✅ Audit History loaded'
\echo ''

\echo '========================================='
\echo 'Sample Data Loading Complete!'
\echo '========================================='
\echo ''
\echo 'Summary:'
SELECT
    'rates' as table_name,
    COUNT(*) as record_count
FROM rates
UNION ALL
SELECT
    'reservations',
    COUNT(*)
FROM reservations
UNION ALL
SELECT
    'payments',
    COUNT(*)
FROM payments
UNION ALL
SELECT
    'room_availability',
    COUNT(*)
FROM availability.room_availability
UNION ALL
SELECT
    'analytics_metrics',
    COUNT(*)
FROM analytics_metrics
UNION ALL
SELECT
    'analytics_reports',
    COUNT(*)
FROM analytics_reports
UNION ALL
SELECT
    'reservation_status_history',
    COUNT(*)
FROM reservation_status_history
ORDER BY table_name;

\echo ''
\echo '✨ Database is ready for testing!'
\echo ''
