-- Enhanced Analytics Schema with Performance Optimizations
-- Version: 2.0
-- Date: 2025-10-06

-- =====================================================
-- PERFORMANCE INDEXES FOR ANALYTICS
-- =====================================================

-- Composite indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_analytics_reports_property_status_created
    ON analytics_reports(property_id, status, created_at DESC)
    WHERE status IN ('SCHEDULED', 'RUNNING');

CREATE INDEX IF NOT EXISTS idx_analytics_reports_schedule
    ON analytics_reports(next_run_at)
    WHERE status = 'SCHEDULED' AND next_run_at IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_analytics_metrics_type_period
    ON analytics_metrics(metric_type, period_start DESC, period_end DESC);

CREATE INDEX IF NOT EXISTS idx_analytics_metrics_property_date
    ON analytics_metrics(property_id, calculated_at DESC)
    WHERE property_id IS NOT NULL;

-- Partial indexes for active/recent data
CREATE INDEX IF NOT EXISTS idx_analytics_reports_active
    ON analytics_reports(created_at DESC)
    WHERE status IN ('SCHEDULED', 'RUNNING', 'COMPLETED')
    AND created_at > CURRENT_DATE - INTERVAL '30 days';

-- =====================================================
-- PARTITION MANAGEMENT FUNCTION
-- =====================================================

CREATE OR REPLACE FUNCTION create_monthly_partition(
    table_name TEXT,
    partition_date DATE
) RETURNS VOID AS $$
DECLARE
    partition_name TEXT;
    start_date DATE;
    end_date DATE;
BEGIN
    -- Generate partition name
    partition_name := table_name || '_' || TO_CHAR(partition_date, 'YYYY_MM');
    start_date := DATE_TRUNC('month', partition_date);
    end_date := start_date + INTERVAL '1 month';

    -- Check if partition already exists
    IF NOT EXISTS (
        SELECT 1 FROM pg_tables
        WHERE tablename = partition_name
    ) THEN
        EXECUTE format(
            'CREATE TABLE IF NOT EXISTS %I PARTITION OF %I
             FOR VALUES FROM (%L) TO (%L)',
            partition_name, table_name, start_date, end_date
        );

        RAISE NOTICE 'Created partition % for period % to %',
            partition_name, start_date, end_date;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- AUTO-CREATE ANALYTICS PARTITIONS
-- =====================================================

CREATE OR REPLACE FUNCTION auto_create_analytics_partitions()
RETURNS VOID AS $$
DECLARE
    current_month DATE := DATE_TRUNC('month', CURRENT_DATE);
    i INTEGER;
BEGIN
    -- Create partitions for next 6 months
    FOR i IN 0..6 LOOP
        PERFORM create_monthly_partition(
            'analytics_metrics',
            (current_month + (i || ' months')::INTERVAL)::DATE
        );
    END LOOP;

    RAISE NOTICE 'Analytics partitions created/verified for next 6 months';
END;
$$ LANGUAGE plpgsql;

-- Execute initial partition creation
SELECT auto_create_analytics_partitions();

-- =====================================================
-- DATA RETENTION AND ARCHIVAL
-- =====================================================

-- Create archive table for old analytics data
CREATE TABLE IF NOT EXISTS analytics_metrics_archive (
    LIKE analytics_metrics INCLUDING ALL
) PARTITION BY RANGE (calculated_at);

-- Create archive partitions for past years
DO $$
DECLARE
    year_date DATE;
BEGIN
    FOR year_date IN
        SELECT DATE_TRUNC('year', CURRENT_DATE - INTERVAL '3 years')::DATE + (n || ' years')::INTERVAL
        FROM generate_series(0, 2) n
    LOOP
        PERFORM create_monthly_partition('analytics_metrics_archive', year_date);
    END LOOP;
END $$;

-- Function to archive old analytics data
CREATE OR REPLACE FUNCTION archive_old_analytics_metrics(
    retention_months INTEGER DEFAULT 12
) RETURNS TABLE (
    archived_count BIGINT,
    archive_date TIMESTAMP
) AS $$
DECLARE
    cutoff_date DATE;
    rows_archived BIGINT;
BEGIN
    cutoff_date := CURRENT_DATE - (retention_months || ' months')::INTERVAL;

    -- Move old data to archive
    WITH moved_data AS (
        INSERT INTO analytics_metrics_archive
        SELECT * FROM analytics_metrics
        WHERE calculated_at < cutoff_date
        RETURNING *
    )
    SELECT COUNT(*) INTO rows_archived FROM moved_data;

    -- Delete from main table
    DELETE FROM analytics_metrics
    WHERE calculated_at < cutoff_date;

    RETURN QUERY SELECT rows_archived, CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- MATERIALIZED VIEWS FOR AGGREGATED METRICS
-- =====================================================

-- Property-level daily metrics
CREATE MATERIALIZED VIEW IF NOT EXISTS mv_property_daily_metrics AS
SELECT
    property_id,
    DATE_TRUNC('day', calculated_at)::DATE as metric_date,
    COUNT(*) as total_metrics,
    AVG(CAST(metric_value AS NUMERIC)) as avg_value,
    MAX(CAST(metric_value AS NUMERIC)) as max_value,
    MIN(CAST(metric_value AS NUMERIC)) as min_value,
    STDDEV(CAST(metric_value AS NUMERIC)) as value_stddev
FROM analytics_metrics
WHERE property_id IS NOT NULL
  AND metric_value ~ '^[0-9]+\.?[0-9]*$'  -- Only numeric values
GROUP BY property_id, DATE_TRUNC('day', calculated_at)
WITH DATA;

-- Create index on materialized view
CREATE UNIQUE INDEX IF NOT EXISTS idx_mv_property_daily_metrics_pk
    ON mv_property_daily_metrics(property_id, metric_date);

CREATE INDEX IF NOT EXISTS idx_mv_property_daily_metrics_date
    ON mv_property_daily_metrics(metric_date DESC);

-- Occupancy rate trend view
CREATE MATERIALIZED VIEW IF NOT EXISTS mv_occupancy_trends AS
SELECT
    property_id,
    DATE_TRUNC('week', calculated_at)::DATE as week_start,
    metric_type,
    AVG(CAST(metric_value AS NUMERIC)) as avg_occupancy,
    COUNT(*) as sample_count
FROM analytics_metrics
WHERE metric_type = 'OCCUPANCY_RATE'
  AND property_id IS NOT NULL
  AND metric_value ~ '^[0-9]+\.?[0-9]*$'
GROUP BY property_id, DATE_TRUNC('week', calculated_at), metric_type
WITH DATA;

CREATE UNIQUE INDEX IF NOT EXISTS idx_mv_occupancy_trends_pk
    ON mv_occupancy_trends(property_id, week_start, metric_type);

-- =====================================================
-- REFRESH MATERIALIZED VIEWS FUNCTION
-- =====================================================

CREATE OR REPLACE FUNCTION refresh_analytics_views()
RETURNS TABLE (
    view_name TEXT,
    refresh_status TEXT,
    refresh_time INTERVAL
) AS $$
DECLARE
    start_time TIMESTAMP;
    end_time TIMESTAMP;
BEGIN
    -- Refresh property daily metrics
    start_time := CLOCK_TIMESTAMP();
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_property_daily_metrics;
    end_time := CLOCK_TIMESTAMP();

    RETURN QUERY SELECT
        'mv_property_daily_metrics'::TEXT,
        'SUCCESS'::TEXT,
        end_time - start_time;

    -- Refresh occupancy trends
    start_time := CLOCK_TIMESTAMP();
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_occupancy_trends;
    end_time := CLOCK_TIMESTAMP();

    RETURN QUERY SELECT
        'mv_occupancy_trends'::TEXT,
        'SUCCESS'::TEXT,
        end_time - start_time;

EXCEPTION WHEN OTHERS THEN
    RETURN QUERY SELECT
        'ERROR'::TEXT,
        SQLERRM::TEXT,
        '0'::INTERVAL;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- PERFORMANCE MONITORING
-- =====================================================

-- View to monitor table sizes and bloat
CREATE OR REPLACE VIEW v_analytics_table_stats AS
SELECT
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as total_size,
    pg_size_pretty(pg_relation_size(schemaname||'.'||tablename)) as table_size,
    pg_size_pretty(pg_indexes_size(schemaname||'.'||tablename)) as indexes_size,
    n_live_tup as live_rows,
    n_dead_tup as dead_rows,
    ROUND(100.0 * n_dead_tup / NULLIF(n_live_tup + n_dead_tup, 0), 2) as dead_ratio_pct,
    last_vacuum,
    last_autovacuum,
    last_analyze,
    last_autoanalyze
FROM pg_stat_user_tables
WHERE schemaname = 'public'
  AND tablename LIKE 'analytics_%'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- =====================================================
-- SCHEDULED MAINTENANCE
-- =====================================================

-- Note: These should be scheduled via pg_cron or external scheduler

-- Daily: Refresh materialized views (suggested: 2 AM)
-- SELECT refresh_analytics_views();

-- Weekly: Auto-create future partitions (suggested: Sunday 3 AM)
-- SELECT auto_create_analytics_partitions();

-- Monthly: Archive old data (suggested: 1st of month, 4 AM)
-- SELECT * FROM archive_old_analytics_metrics(12);

-- Weekly: Vacuum analyze analytics tables (suggested: Sunday 4 AM)
-- VACUUM ANALYZE analytics_reports;
-- VACUUM ANALYZE analytics_metrics;

COMMENT ON FUNCTION auto_create_analytics_partitions() IS
'Automatically creates monthly partitions for analytics_metrics table for the next 6 months. Should be run weekly.';

COMMENT ON FUNCTION archive_old_analytics_metrics(INTEGER) IS
'Archives analytics metrics older than specified months to archive table. Default: 12 months retention.';

COMMENT ON FUNCTION refresh_analytics_views() IS
'Refreshes all analytics materialized views. Should be run daily during off-peak hours.';
