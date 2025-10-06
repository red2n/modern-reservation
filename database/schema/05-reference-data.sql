-- =====================================================
-- 05-reference-data.sql
-- Reference Data and System Configuration
-- Date: 2025-10-06
-- =====================================================

-- This file intentionally left minimal.
-- Reference data will be loaded separately or through application bootstrap.

-- =====================================================
-- SYSTEM METADATA
-- =====================================================

-- Create a metadata table for schema versioning
CREATE TABLE IF NOT EXISTS schema_version (
    version VARCHAR(50) PRIMARY KEY,
    description TEXT,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    applied_by VARCHAR(100) DEFAULT CURRENT_USER
);

INSERT INTO schema_version (version, description) VALUES
    ('1.0.0', 'Initial schema generated from entity classes - October 2025');

-- =====================================================
-- AUDIT LOG TRIGGER FUNCTION
-- =====================================================

-- Create a reusable trigger function for updating updated_at timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION update_updated_at_column() IS
    'Trigger function to automatically update updated_at timestamp on record modification';

-- =====================================================
-- APPLY UPDATE TRIGGERS
-- =====================================================

-- Rates table trigger
CREATE TRIGGER trigger_rates_updated_at
    BEFORE UPDATE ON rates
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Reservations table trigger
CREATE TRIGGER trigger_reservations_updated_at
    BEFORE UPDATE ON reservations
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Payments table trigger
CREATE TRIGGER trigger_payments_updated_at
    BEFORE UPDATE ON payments
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Room Availability table trigger
CREATE TRIGGER trigger_room_availability_updated_at
    BEFORE UPDATE ON availability.room_availability
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Analytics Metrics table trigger
CREATE TRIGGER trigger_analytics_metrics_updated_at
    BEFORE UPDATE ON analytics_metrics
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Analytics Reports table trigger
CREATE TRIGGER trigger_analytics_reports_updated_at
    BEFORE UPDATE ON analytics_reports
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- VACUUM AND ANALYZE
-- =====================================================

-- Analyze all tables for query planner statistics
ANALYZE rates;
ANALYZE reservations;
ANALYZE reservation_status_history;
ANALYZE payments;
ANALYZE availability.room_availability;
ANALYZE analytics_metrics;
ANALYZE analytics_metric_dimensions;
ANALYZE analytics_reports;
ANALYZE report_property_ids;

-- =====================================================
-- COMMENTS
-- =====================================================

COMMENT ON TABLE schema_version IS 'Tracks database schema version history';
COMMENT ON TRIGGER trigger_rates_updated_at ON rates IS 'Auto-updates updated_at on row modification';
COMMENT ON TRIGGER trigger_reservations_updated_at ON reservations IS 'Auto-updates updated_at on row modification';
COMMENT ON TRIGGER trigger_payments_updated_at ON payments IS 'Auto-updates updated_at on row modification';
