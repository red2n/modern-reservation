-- Sample Data for Analytics Tables
-- Generates 500 realistic analytics metrics and 100 reports

-- Insert 500 analytics metrics
INSERT INTO analytics_metrics (
    metric_id, metric_type, property_id, room_type_id, rate_plan_id, channel_id, user_id,
    time_granularity, period_start, period_end,
    metric_value, count_value, percentage_value,
    currency_code, formatted_value,
    status, calculated_at, expires_at, calculation_duration_ms,
    data_points_count, confidence_score,
    baseline_value, target_value, variance_percentage,
    trend_direction, seasonality_factor
)
SELECT
    gen_random_uuid(),
    -- Metric type distribution
    (ARRAY['OCCUPANCY_RATE', 'ADR', 'REVPAR', 'TOTAL_REVENUE', 'BOOKING_COUNT',
           'CANCELLATION_RATE', 'NO_SHOW_RATE', 'AVERAGE_LENGTH_OF_STAY', 'LEAD_TIME',
           'CONVERSION_RATE'])[1 + (s % 10)]::metric_type AS metric_type,
    -- Property ID (80% have property, 20% are aggregate)
    CASE WHEN s % 5 = 0 THEN NULL
    ELSE (ARRAY[
        '11111111-1111-1111-1111-111111111111'::uuid,
        '22222222-2222-2222-2222-222222222222'::uuid,
        '33333333-3333-3333-3333-333333333333'::uuid,
        '44444444-4444-4444-4444-444444444444'::uuid,
        '55555555-5555-5555-5555-555555555555'::uuid
    ])[1 + (s % 5)]
    END AS property_id,
    -- Room type ID (optional)
    CASE WHEN s % 3 = 0 THEN NULL
    ELSE (ARRAY[
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'::uuid,
        'cccccccc-cccc-cccc-cccc-cccccccccccc'::uuid,
        'dddddddd-dddd-dddd-dddd-dddddddddddd'::uuid
    ])[1 + (s % 3)]
    END AS room_type_id,
    -- Rate plan ID (optional)
    CASE WHEN s % 4 = 0 THEN NULL ELSE gen_random_uuid() END AS rate_plan_id,
    -- Channel ID (optional)
    CASE WHEN s % 4 = 0 THEN NULL ELSE gen_random_uuid() END AS channel_id,
    -- User ID (optional)
    CASE WHEN s % 5 = 0 THEN NULL ELSE gen_random_uuid() END AS user_id,
    -- Time granularity
    (ARRAY['DAILY', 'DAILY', 'DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY'])[1 + (s % 7)]::time_granularity AS time_granularity,
    -- Period start (last year to today)
    CURRENT_TIMESTAMP - ((s * 7) || ' days')::interval AS period_start,
    -- Period end (based on granularity)
    CASE (ARRAY['DAILY', 'DAILY', 'DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY'])[1 + (s % 7)]
        WHEN 'DAILY' THEN CURRENT_TIMESTAMP - ((s * 7) || ' days')::interval + interval '1 day'
        WHEN 'WEEKLY' THEN CURRENT_TIMESTAMP - ((s * 7) || ' days')::interval + interval '7 days'
        WHEN 'MONTHLY' THEN CURRENT_TIMESTAMP - ((s * 7) || ' days')::interval + interval '1 month'
        WHEN 'QUARTERLY' THEN CURRENT_TIMESTAMP - ((s * 7) || ' days')::interval + interval '3 months'
        WHEN 'YEARLY' THEN CURRENT_TIMESTAMP - ((s * 7) || ' days')::interval + interval '1 year'
    END AS period_end,
    -- Metric value (varies by type)
    CASE (ARRAY['OCCUPANCY_RATE', 'ADR', 'REVPAR', 'TOTAL_REVENUE', 'BOOKING_COUNT',
           'CANCELLATION_RATE', 'NO_SHOW_RATE', 'AVERAGE_LENGTH_OF_STAY', 'LEAD_TIME',
           'CONVERSION_RATE'])[1 + (s % 10)]
        WHEN 'OCCUPANCY_RATE' THEN (50 + (s % 50))::numeric(19,4)
        WHEN 'ADR' THEN (150 + (s % 350))::numeric(19,4)
        WHEN 'REVPAR' THEN (100 + (s % 300))::numeric(19,4)
        WHEN 'TOTAL_REVENUE' THEN (5000 + (s % 45000))::numeric(19,4)
        WHEN 'AVERAGE_LENGTH_OF_STAY' THEN (2 + (s % 10) * 0.5)::numeric(19,4)
        WHEN 'LEAD_TIME' THEN (7 + (s % 83))::numeric(19,4)
        WHEN 'CONVERSION_RATE' THEN (10 + (s % 40))::numeric(19,4)
        ELSE (s % 100)::numeric(19,4)
    END AS metric_value,
    -- Count value (for countable metrics)
    CASE (ARRAY['OCCUPANCY_RATE', 'ADR', 'REVPAR', 'TOTAL_REVENUE', 'BOOKING_COUNT',
           'CANCELLATION_RATE', 'NO_SHOW_RATE', 'AVERAGE_LENGTH_OF_STAY', 'LEAD_TIME',
           'CONVERSION_RATE'])[1 + (s % 10)]
        WHEN 'BOOKING_COUNT' THEN (10 + (s % 90))::bigint
        WHEN 'CANCELLATION_RATE' THEN (1 + (s % 20))::bigint
        WHEN 'NO_SHOW_RATE' THEN (0 + (s % 10))::bigint
        ELSE NULL
    END AS count_value,
    -- Percentage value (for rate metrics)
    CASE (ARRAY['OCCUPANCY_RATE', 'ADR', 'REVPAR', 'TOTAL_REVENUE', 'BOOKING_COUNT',
           'CANCELLATION_RATE', 'NO_SHOW_RATE', 'AVERAGE_LENGTH_OF_STAY', 'LEAD_TIME',
           'CONVERSION_RATE'])[1 + (s % 10)]
        WHEN 'OCCUPANCY_RATE' THEN (50 + (s % 50))::numeric(5,2)
        WHEN 'CONVERSION_RATE' THEN (10 + (s % 40))::numeric(5,2)
        WHEN 'CANCELLATION_RATE' THEN (5 + (s % 15))::numeric(5,2)
        WHEN 'NO_SHOW_RATE' THEN (1 + (s % 9))::numeric(5,2)
        ELSE NULL
    END AS percentage_value,
    -- Currency code (for monetary metrics)
    CASE (ARRAY['OCCUPANCY_RATE', 'ADR', 'REVPAR', 'TOTAL_REVENUE', 'BOOKING_COUNT',
           'CANCELLATION_RATE', 'NO_SHOW_RATE', 'AVERAGE_LENGTH_OF_STAY', 'LEAD_TIME',
           'CONVERSION_RATE'])[1 + (s % 10)]
        WHEN 'ADR' THEN 'USD'
        WHEN 'REVPAR' THEN 'USD'
        WHEN 'TOTAL_REVENUE' THEN 'USD'
        ELSE NULL
    END AS currency_code,
    -- Formatted value
    CASE (ARRAY['OCCUPANCY_RATE', 'ADR', 'REVPAR', 'TOTAL_REVENUE', 'BOOKING_COUNT',
           'CANCELLATION_RATE', 'NO_SHOW_RATE', 'AVERAGE_LENGTH_OF_STAY', 'LEAD_TIME',
           'CONVERSION_RATE'])[1 + (s % 10)]
        WHEN 'OCCUPANCY_RATE' THEN (50 + (s % 50))::text || '%'
        WHEN 'ADR' THEN '$' || (150 + (s % 350))::text
        WHEN 'REVPAR' THEN '$' || (100 + (s % 300))::text
        WHEN 'TOTAL_REVENUE' THEN '$' || (5000 + (s % 45000))::text
        WHEN 'CONVERSION_RATE' THEN (10 + (s % 40))::text || '%'
        WHEN 'CANCELLATION_RATE' THEN (5 + (s % 15))::text || '%'
        WHEN 'NO_SHOW_RATE' THEN (1 + (s % 9))::text || '%'
        ELSE (s % 100)::text
    END AS formatted_value,
    -- Status (mostly completed)
    (ARRAY['COMPLETED', 'COMPLETED', 'COMPLETED', 'COMPLETED', 'CALCULATING',
           'PENDING', 'FAILED', 'EXPIRED', 'CACHED'])[1 + (s % 9)]::analytics_status AS status,
    -- Calculated at
    CURRENT_TIMESTAMP - ((s * 2) || ' hours')::interval AS calculated_at,
    -- Expires at (24-72 hours after calculation)
    CURRENT_TIMESTAMP - ((s * 2) || ' hours')::interval + ((24 + (s % 48)) || ' hours')::interval AS expires_at,
    -- Calculation duration (50-500ms)
    (50 + (s % 450))::bigint AS calculation_duration_ms,
    -- Data points count
    (10 + (s % 990))::integer AS data_points_count,
    -- Confidence score (0.75-1.00)
    (0.75 + (s % 25) * 0.01)::numeric(3,2) AS confidence_score,
    -- Baseline value (90-110% of metric value)
    CASE (ARRAY['OCCUPANCY_RATE', 'ADR', 'REVPAR', 'TOTAL_REVENUE', 'BOOKING_COUNT',
           'CANCELLATION_RATE', 'NO_SHOW_RATE', 'AVERAGE_LENGTH_OF_STAY', 'LEAD_TIME',
           'CONVERSION_RATE'])[1 + (s % 10)]
        WHEN 'OCCUPANCY_RATE' THEN ((50 + (s % 50)) * 0.95)::numeric(19,4)
        WHEN 'ADR' THEN ((150 + (s % 350)) * 0.95)::numeric(19,4)
        WHEN 'REVPAR' THEN ((100 + (s % 300)) * 0.95)::numeric(19,4)
        WHEN 'TOTAL_REVENUE' THEN ((5000 + (s % 45000)) * 0.95)::numeric(19,4)
        ELSE NULL
    END AS baseline_value,
    -- Target value (110-120% of baseline)
    CASE (ARRAY['OCCUPANCY_RATE', 'ADR', 'REVPAR', 'TOTAL_REVENUE', 'BOOKING_COUNT',
           'CANCELLATION_RATE', 'NO_SHOW_RATE', 'AVERAGE_LENGTH_OF_STAY', 'LEAD_TIME',
           'CONVERSION_RATE'])[1 + (s % 10)]
        WHEN 'OCCUPANCY_RATE' THEN ((50 + (s % 50)) * 1.15)::numeric(19,4)
        WHEN 'ADR' THEN ((150 + (s % 350)) * 1.15)::numeric(19,4)
        WHEN 'REVPAR' THEN ((100 + (s % 300)) * 1.15)::numeric(19,4)
        WHEN 'TOTAL_REVENUE' THEN ((5000 + (s % 45000)) * 1.15)::numeric(19,4)
        ELSE NULL
    END AS target_value,
    -- Variance percentage (-20% to +20%)
    (((s % 40) - 20))::numeric(5,2) AS variance_percentage,
    -- Trend direction
    (ARRAY['UP', 'UP', 'UP', 'DOWN', 'DOWN', 'STABLE', 'UNKNOWN'])[1 + (s % 7)] AS trend_direction,
    -- Seasonality factor (0.7-1.3)
    (0.7 + (s % 60) * 0.01)::numeric(5,4) AS seasonality_factor
FROM generate_series(1, 500) AS s;

-- Insert 100 analytics reports
INSERT INTO analytics_reports (
    report_id, report_name, report_type, report_description,
    property_id, time_granularity,
    period_start, period_end,
    status, generation_started_at
)
SELECT
    gen_random_uuid(),
    -- Report name
    (ARRAY['Daily Occupancy Report', 'Weekly Revenue Report', 'Monthly Performance Dashboard',
           'Executive Summary', 'Operational Metrics', 'Financial Analysis',
           'Customer Analytics', 'Booking Trends', 'Rate Performance', 'Channel Analysis'])[1 + (s % 10)] ||
    ' - ' || TO_CHAR(CURRENT_DATE - (s || ' days')::interval, 'YYYY-MM-DD') AS report_name,
    -- Report type
    (ARRAY['DASHBOARD', 'EXECUTIVE', 'OPERATIONAL', 'FINANCIAL', 'OCCUPANCY',
           'REVENUE', 'CUSTOMER', 'CUSTOM', 'SCHEDULED'])[1 + (s % 9)] AS report_type,
    -- Description
    'Automated ' || (ARRAY['daily', 'weekly', 'monthly', 'quarterly'])[1 + (s % 4)] || ' report generated for analysis' AS report_description,
    -- Property ID (70% have property, 30% are aggregate)
    CASE WHEN s % 10 < 7 THEN
        (ARRAY[
            '11111111-1111-1111-1111-111111111111'::uuid,
            '22222222-2222-2222-2222-222222222222'::uuid,
            '33333333-3333-3333-3333-333333333333'::uuid
        ])[1 + (s % 3)]
    ELSE NULL END AS property_id,
    -- Time granularity
    (ARRAY['DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY'])[1 + (s % 5)]::time_granularity AS time_granularity,
    -- Period start
    CURRENT_TIMESTAMP - ((s * 7) || ' days')::interval AS period_start,
    -- Period end
    CASE (ARRAY['DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY'])[1 + (s % 5)]
        WHEN 'DAILY' THEN CURRENT_TIMESTAMP - ((s * 7) || ' days')::interval + interval '1 day'
        WHEN 'WEEKLY' THEN CURRENT_TIMESTAMP - ((s * 7) || ' days')::interval + interval '7 days'
        WHEN 'MONTHLY' THEN CURRENT_TIMESTAMP - ((s * 7) || ' days')::interval + interval '1 month'
        WHEN 'QUARTERLY' THEN CURRENT_TIMESTAMP - ((s * 7) || ' days')::interval + interval '3 months'
        WHEN 'YEARLY' THEN CURRENT_TIMESTAMP - ((s * 7) || ' days')::interval + interval '1 year'
    END AS period_end,
    -- Status
    (ARRAY['COMPLETED', 'COMPLETED', 'COMPLETED', 'CALCULATING', 'PENDING', 'FAILED'])[1 + (s % 6)]::analytics_status AS status,
    -- Generation started at
    CURRENT_TIMESTAMP - ((s * 2) || ' hours')::interval AS generation_started_at
FROM generate_series(1, 100) AS s;

-- Verify the inserts
SELECT COUNT(*) as total_metrics FROM analytics_metrics;
SELECT COUNT(*) as total_reports FROM analytics_reports;
SELECT metric_type, COUNT(*) FROM analytics_metrics GROUP BY metric_type ORDER BY COUNT(*) DESC LIMIT 5;
SELECT report_type, COUNT(*) FROM analytics_reports GROUP BY report_type ORDER BY COUNT(*) DESC;
