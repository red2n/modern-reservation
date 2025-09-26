-- Analytics and Reporting Schema
-- Comprehensive analytics platform for business intelligence and reporting
-- Modern Reservation Management System - Ultra-Scale Architecture

-- =============================================================================
-- ANALYTICS REPORTS
-- =============================================================================

CREATE TABLE analytics_reports (
    report_id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_name                  VARCHAR(200) NOT NULL,
    report_type                  VARCHAR(50) NOT NULL
        CHECK (report_type IN ('DASHBOARD', 'EXECUTIVE', 'OPERATIONAL', 'FINANCIAL',
                               'OCCUPANCY', 'REVENUE', 'CUSTOMER', 'CUSTOM', 'SCHEDULED')),
    report_description          TEXT,
    property_id                 UUID,
    time_granularity           VARCHAR(20) NOT NULL
        CHECK (time_granularity IN ('MINUTE', 'HOUR', 'DAY', 'WEEK', 'MONTH',
                                   'QUARTER', 'YEAR')),
    period_start               TIMESTAMP NOT NULL,
    period_end                 TIMESTAMP NOT NULL,
    status                     VARCHAR(30) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED',
                         'CANCELLED', 'SCHEDULED', 'EXPIRED')),

    -- Generation Tracking
    generation_started_at       TIMESTAMP,
    generated_at               TIMESTAMP,
    generation_completed_at     TIMESTAMP,
    scheduled_at               TIMESTAMP,
    expires_at                 TIMESTAMP,
    generation_duration_ms     BIGINT,

    -- Quality Metrics
    total_metrics_count        INTEGER CHECK (total_metrics_count >= 0),
    successful_metrics_count   INTEGER CHECK (successful_metrics_count >= 0),
    failed_metrics_count       INTEGER CHECK (failed_metrics_count >= 0),
    overall_quality_score      DECIMAL(3,2) CHECK (overall_quality_score >= 0 AND overall_quality_score <= 1),
    data_completeness_percentage DECIMAL(5,2) CHECK (data_completeness_percentage >= 0 AND data_completeness_percentage <= 100),

    -- Output Configuration
    report_format              VARCHAR(20) DEFAULT 'JSON'
        CHECK (report_format IN ('JSON', 'PDF', 'EXCEL', 'CSV', 'HTML', 'DASHBOARD')),
    report_size_bytes          BIGINT,
    file_size_bytes           BIGINT,
    file_path                 VARCHAR(500),
    download_url              VARCHAR(1000),
    file_url                  VARCHAR(500),

    -- Scheduling
    is_scheduled              BOOLEAN NOT NULL DEFAULT FALSE,
    schedule_expression       VARCHAR(100), -- Cron expression
    next_run_at              TIMESTAMP,
    last_run_at              TIMESTAMP,
    is_recurring             BOOLEAN NOT NULL DEFAULT FALSE,
    recurrence_pattern       VARCHAR(50), -- DAILY, WEEKLY, MONTHLY, QUARTERLY
    recurring_schedule       VARCHAR(100),

    -- Access Control
    is_public                BOOLEAN NOT NULL DEFAULT FALSE,
    access_token             VARCHAR(100),
    access_level             VARCHAR(20),
    template_id              UUID,
    parent_report_id         UUID,

    -- Content
    error_message            TEXT,
    warning_messages         TEXT,
    generation_notes         TEXT,
    tags                     VARCHAR(500),
    delivery_options         VARCHAR(1000),
    priority_level           INTEGER DEFAULT 3 CHECK (priority_level >= 1 AND priority_level <= 5),

    -- Audit Fields
    version                  INTEGER NOT NULL DEFAULT 1,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              VARCHAR(100),
    updated_by              VARCHAR(100),

    -- Constraints
    CHECK (period_end > period_start),
    CHECK (successful_metrics_count + failed_metrics_count <= total_metrics_count)
);

-- =============================================================================
-- ANALYTICS METRICS
-- =============================================================================

CREATE TABLE analytics_metrics (
    metric_id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    metric_type                VARCHAR(50) NOT NULL
        CHECK (metric_type IN ('OCCUPANCY_RATE', 'ADR', 'REVPAR', 'REVENUE',
                              'BOOKINGS', 'CANCELLATIONS', 'NO_SHOWS', 'GUEST_SATISFACTION',
                              'LENGTH_OF_STAY', 'CONVERSION_RATE', 'CHANNEL_PERFORMANCE',
                              'CUSTOM', 'KPI')),

    -- Entity References
    property_id                UUID,
    room_type_id              UUID,
    rate_plan_id              UUID,
    channel_id                UUID,
    user_id                   UUID,

    -- Time Configuration
    time_granularity          VARCHAR(20) NOT NULL
        CHECK (time_granularity IN ('MINUTE', 'HOUR', 'DAY', 'WEEK', 'MONTH',
                                   'QUARTER', 'YEAR')),
    period_start              TIMESTAMP NOT NULL,
    period_end                TIMESTAMP NOT NULL,

    -- Metric Values
    metric_value              DECIMAL(19,4),
    count_value              BIGINT,
    percentage_value         DECIMAL(5,2),
    currency_code            VARCHAR(3),
    formatted_value          VARCHAR(255),

    -- Status and Tracking
    status                   VARCHAR(30) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED',
                         'CANCELLED', 'SCHEDULED', 'EXPIRED')),
    calculated_at            TIMESTAMP,
    expires_at              TIMESTAMP,
    calculation_duration_ms  BIGINT,

    -- Quality and Analytics
    data_points_count        INTEGER CHECK (data_points_count >= 0),
    confidence_score         DECIMAL(3,2) CHECK (confidence_score >= 0 AND confidence_score <= 1),
    baseline_value           DECIMAL(19,4),
    target_value            DECIMAL(19,4),
    variance_percentage     DECIMAL(5,2),
    trend_direction         VARCHAR(10)
        CHECK (trend_direction IN ('UP', 'DOWN', 'STABLE', 'UNKNOWN')),
    seasonality_factor      DECIMAL(5,4),

    -- Metadata
    calculation_method       VARCHAR(100),
    data_sources           TEXT,
    quality_score          DECIMAL(3,2) CHECK (quality_score >= 0 AND quality_score <= 1),
    error_message          TEXT,
    warning_messages       TEXT,
    tags                   VARCHAR(500),
    notes                  VARCHAR(1000),

    -- Audit Fields
    version                INTEGER NOT NULL DEFAULT 1,
    created_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by            VARCHAR(100),
    updated_by            VARCHAR(100),

    -- Constraints
    CHECK (period_end > period_start)
);

-- =============================================================================
-- COLLECTION TABLES FOR @ElementCollection MAPPINGS
-- =============================================================================

-- Report Property IDs
CREATE TABLE report_property_ids (
    report_id    UUID NOT NULL REFERENCES analytics_reports(report_id) ON DELETE CASCADE,
    property_id  UUID NOT NULL,
    PRIMARY KEY (report_id, property_id)
);

-- Report Metric Types
CREATE TABLE report_metric_types (
    report_id   UUID NOT NULL REFERENCES analytics_reports(report_id) ON DELETE CASCADE,
    metric_type VARCHAR(50) NOT NULL,
    PRIMARY KEY (report_id, metric_type)
);

-- Report Filters
CREATE TABLE report_filters (
    report_id    UUID NOT NULL REFERENCES analytics_reports(report_id) ON DELETE CASCADE,
    filter_key   VARCHAR(50) NOT NULL,
    filter_value VARCHAR(255),
    PRIMARY KEY (report_id, filter_key)
);

-- Report Parameters
CREATE TABLE report_parameters (
    report_id       UUID NOT NULL REFERENCES analytics_reports(report_id) ON DELETE CASCADE,
    parameter_key   VARCHAR(50) NOT NULL,
    parameter_value TEXT,
    PRIMARY KEY (report_id, parameter_key)
);

-- Report Recipients
CREATE TABLE report_recipients (
    report_id       UUID NOT NULL REFERENCES analytics_reports(report_id) ON DELETE CASCADE,
    recipient_email VARCHAR(255) NOT NULL,
    PRIMARY KEY (report_id, recipient_email)
);

-- Report Visualizations
CREATE TABLE report_visualizations (
    report_id    UUID NOT NULL REFERENCES analytics_reports(report_id) ON DELETE CASCADE,
    chart_type   VARCHAR(50) NOT NULL,
    chart_config TEXT,
    PRIMARY KEY (report_id, chart_type)
);

-- Report Summary Statistics
CREATE TABLE report_summary_stats (
    report_id  UUID NOT NULL REFERENCES analytics_reports(report_id) ON DELETE CASCADE,
    stat_key   VARCHAR(50) NOT NULL,
    stat_value VARCHAR(255),
    PRIMARY KEY (report_id, stat_key)
);

-- Report Output Formats
CREATE TABLE report_output_formats (
    report_id     UUID NOT NULL REFERENCES analytics_reports(report_id) ON DELETE CASCADE,
    output_format VARCHAR(255) NOT NULL,
    PRIMARY KEY (report_id, output_format)
);

-- Analytics Metric Dimensions
CREATE TABLE analytics_metric_dimensions (
    metric_id       UUID NOT NULL REFERENCES analytics_metrics(metric_id) ON DELETE CASCADE,
    dimension_key   VARCHAR(50) NOT NULL,
    dimension_value VARCHAR(255),
    PRIMARY KEY (metric_id, dimension_key)
);

-- Analytics Metric Metadata
CREATE TABLE analytics_metric_metadata (
    metric_id      UUID NOT NULL REFERENCES analytics_metrics(metric_id) ON DELETE CASCADE,
    metadata_key   VARCHAR(50) NOT NULL,
    metadata_value TEXT,
    PRIMARY KEY (metric_id, metadata_key)
);

-- =============================================================================
-- INDEXES FOR PERFORMANCE
-- =============================================================================

-- Analytics Reports Indexes
CREATE INDEX idx_report_type_status ON analytics_reports(report_type, status);
CREATE INDEX idx_property_period ON analytics_reports(property_id, period_start, period_end);
CREATE INDEX idx_generated_at ON analytics_reports(generated_at);
CREATE INDEX idx_scheduled_at ON analytics_reports(scheduled_at);
CREATE INDEX idx_created_by ON analytics_reports(created_by);
CREATE INDEX idx_report_status ON analytics_reports(status);
CREATE INDEX idx_report_next_run ON analytics_reports(next_run_at) WHERE is_scheduled = true;

-- Analytics Metrics Indexes
CREATE INDEX idx_metric_type_period ON analytics_metrics(metric_type, period_start, period_end);
CREATE INDEX idx_property_granularity ON analytics_metrics(property_id, time_granularity);
CREATE INDEX idx_calculation_time ON analytics_metrics(calculated_at);
CREATE INDEX idx_metric_status ON analytics_metrics(status);
CREATE INDEX idx_period_range ON analytics_metrics(period_start, period_end);
CREATE INDEX idx_metric_type_property ON analytics_metrics(metric_type, property_id);

-- =============================================================================
-- TRIGGERS FOR AUDIT FIELDS
-- =============================================================================

-- Function to update timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply to analytics_reports
CREATE TRIGGER update_analytics_reports_updated_at
    BEFORE UPDATE ON analytics_reports
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Apply to analytics_metrics
CREATE TRIGGER update_analytics_metrics_updated_at
    BEFORE UPDATE ON analytics_metrics
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================================================
-- COMMENTS FOR DOCUMENTATION
-- =============================================================================

COMMENT ON TABLE analytics_reports IS 'Generated analytics reports containing multiple metrics and visualizations';
COMMENT ON TABLE analytics_metrics IS 'Individual calculated metrics and KPIs for business analytics';

COMMENT ON COLUMN analytics_reports.report_type IS 'Type of report: DASHBOARD, EXECUTIVE, OPERATIONAL, etc.';
COMMENT ON COLUMN analytics_reports.time_granularity IS 'Time granularity for the report data';
COMMENT ON COLUMN analytics_reports.overall_quality_score IS 'Overall quality score from 0.00 to 1.00';
COMMENT ON COLUMN analytics_reports.schedule_expression IS 'Cron expression for scheduled reports';

COMMENT ON COLUMN analytics_metrics.metric_type IS 'Type of metric: OCCUPANCY_RATE, ADR, REVPAR, etc.';
COMMENT ON COLUMN analytics_metrics.confidence_score IS 'Statistical confidence score from 0.00 to 1.00';
COMMENT ON COLUMN analytics_metrics.trend_direction IS 'Trend direction: UP, DOWN, STABLE, or UNKNOWN';
