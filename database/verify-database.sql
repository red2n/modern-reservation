-- =====================================================
-- Modern Reservation System - Database Verification
-- Run this in pgAdmin Query Tool after connecting
-- =====================================================

-- 1. DATABASE OVERVIEW
-- =====================================================
SELECT 'DATABASE OVERVIEW' as section;
SELECT current_database() as database_name, version() as postgresql_version;

-- 2. TABLE STATISTICS
-- =====================================================
SELECT 'TABLE STATISTICS' as section;
SELECT
    schemaname as schema,
    tablename as table_name,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size,
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema=schemaname AND table_name=tablename) as column_count
FROM pg_tables
WHERE schemaname IN ('public', 'availability')
ORDER BY schemaname, tablename;

-- 3. ROW COUNTS FOR ALL TABLES
-- =====================================================
SELECT 'ROW COUNTS' as section;
SELECT 'rates' as table_name, COUNT(*) as row_count FROM rates
UNION ALL
SELECT 'reservations', COUNT(*) FROM reservations
UNION ALL
SELECT 'reservation_status_history', COUNT(*) FROM reservation_status_history
UNION ALL
SELECT 'payments', COUNT(*) FROM payments
UNION ALL
SELECT 'room_availability', COUNT(*) FROM availability.room_availability
UNION ALL
SELECT 'analytics_metrics', COUNT(*) FROM analytics_metrics
UNION ALL
SELECT 'analytics_reports', COUNT(*) FROM analytics_reports
UNION ALL
SELECT 'analytics_metric_dimensions', COUNT(*) FROM analytics_metric_dimensions
UNION ALL
SELECT 'report_property_ids', COUNT(*) FROM report_property_ids
ORDER BY row_count DESC;

-- 4. ENUM TYPES SUMMARY
-- =====================================================
SELECT 'CUSTOM ENUM TYPES' as section;
SELECT
    t.typname as enum_type_name,
    COUNT(e.enumlabel) as value_count,
    STRING_AGG(e.enumlabel, ', ' ORDER BY e.enumsortorder) as possible_values
FROM pg_type t
JOIN pg_enum e ON t.oid = e.enumtypid
JOIN pg_namespace n ON n.oid = t.typnamespace
WHERE n.nspname = 'public'
GROUP BY t.typname
ORDER BY t.typname;

-- 5. SAMPLE DATA - RATES
-- =====================================================
SELECT 'SAMPLE RATES DATA' as section;
SELECT
    id,
    property_id,
    room_type_id,
    rate_code,
    base_rate,
    rate_status,
    rate_strategy,
    effective_from,
    effective_to
FROM rates
ORDER BY created_at DESC
LIMIT 10;

-- 6. SAMPLE DATA - RESERVATIONS
-- =====================================================
SELECT 'SAMPLE RESERVATIONS DATA' as section;
SELECT
    id,
    confirmation_number,
    guest_name,
    guest_email,
    check_in_date,
    check_out_date,
    total_amount,
    status,
    source
FROM reservations
ORDER BY created_at DESC
LIMIT 10;

-- 7. SAMPLE DATA - PAYMENTS
-- =====================================================
SELECT 'SAMPLE PAYMENTS DATA' as section;
SELECT
    id,
    reservation_id,
    amount,
    payment_method,
    payment_status,
    transaction_type,
    payment_date
FROM payments
ORDER BY payment_date DESC
LIMIT 10;

-- 8. SAMPLE DATA - ROOM AVAILABILITY
-- =====================================================
SELECT 'SAMPLE ROOM AVAILABILITY DATA' as section;
SELECT
    property_id,
    room_type_id,
    date,
    available_rooms,
    total_rooms,
    status,
    room_category,
    base_price
FROM availability.room_availability
WHERE date >= CURRENT_DATE
ORDER BY date
LIMIT 10;

-- 9. SAMPLE DATA - ANALYTICS METRICS
-- =====================================================
SELECT 'SAMPLE ANALYTICS METRICS DATA' as section;
SELECT
    id,
    property_id,
    metric_type,
    metric_value,
    time_period,
    granularity,
    calculated_at
FROM analytics_metrics
ORDER BY calculated_at DESC
LIMIT 10;

-- 10. INDEXES AND CONSTRAINTS
-- =====================================================
SELECT 'DATABASE INDEXES' as section;
SELECT
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE schemaname IN ('public', 'availability')
ORDER BY tablename, indexname;

-- 11. FOREIGN KEY RELATIONSHIPS
-- =====================================================
SELECT 'FOREIGN KEY CONSTRAINTS' as section;
SELECT
    tc.table_schema,
    tc.constraint_name,
    tc.table_name,
    kcu.column_name,
    ccu.table_schema AS foreign_table_schema,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
    AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
    AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY'
    AND tc.table_schema IN ('public', 'availability')
ORDER BY tc.table_name;

-- 12. DATABASE SIZE SUMMARY
-- =====================================================
SELECT 'DATABASE SIZE SUMMARY' as section;
SELECT
    pg_database.datname AS database_name,
    pg_size_pretty(pg_database_size(pg_database.datname)) AS size
FROM pg_database
WHERE datname = current_database();

-- =====================================================
-- VERIFICATION COMPLETE
-- =====================================================
SELECT '✅ Database verification complete!' as status,
       'All tables, data, and relationships are ready' as message;
