-- =====================================================
-- 05-reference-data.sql
-- Reference Data and System Configuration
-- Date: 2025-10-06
-- Updated: 2025-10-08 (Multi-tenancy reference data added)
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
    ('1.0.0', 'Initial schema generated from entity classes - October 2025'),
    ('2.0.0', 'Multi-tenancy support added - October 2025');

-- =====================================================
-- SAMPLE TENANT DATA (for development/testing)
-- =====================================================

-- Insert a default system tenant for development
INSERT INTO tenants (
    id,
    name,
    slug,
    type,
    status,
    email,
    config,
    subscription,
    created_by
) VALUES (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', -- Fixed UUID for development
    'Demo Hotel Chain',
    'demo-hotel-chain',
    'CHAIN',
    'ACTIVE',
    'admin@demo-hotel-chain.com',
    '{
        "brandingEnabled": true,
        "logoUrl": "https://example.com/logo.png",
        "primaryColor": "#0066CC",
        "secondaryColor": "#FF6600",
        "enableMultiProperty": true,
        "enableChannelManager": true,
        "enableAdvancedReporting": true,
        "enablePaymentProcessing": true,
        "enableLoyaltyProgram": true,
        "maxProperties": 50,
        "maxUsers": 100,
        "maxReservationsPerMonth": null,
        "defaultCurrency": "USD",
        "defaultLanguage": "en",
        "defaultTimezone": "America/New_York"
    }'::jsonb,
    '{
        "plan": "ENTERPRISE",
        "startDate": "2025-01-01",
        "endDate": "2026-01-01",
        "trialEndDate": null,
        "billingCycle": "YEARLY",
        "amount": 9999.00,
        "currency": "USD",
        "paymentMethod": "invoice"
    }'::jsonb,
    'system'
) ON CONFLICT (id) DO NOTHING;

-- Insert a sample independent property tenant
INSERT INTO tenants (
    id,
    name,
    slug,
    type,
    status,
    email,
    config,
    subscription,
    created_by
) VALUES (
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', -- Fixed UUID for development
    'Boutique Beach Resort',
    'boutique-beach-resort',
    'INDEPENDENT',
    'TRIAL',
    'owner@boutiquebeach.com',
    '{
        "brandingEnabled": true,
        "enableMultiProperty": false,
        "enableChannelManager": false,
        "enableAdvancedReporting": false,
        "enablePaymentProcessing": true,
        "enableLoyaltyProgram": false,
        "maxProperties": 1,
        "maxUsers": 5,
        "maxReservationsPerMonth": 100,
        "defaultCurrency": "USD",
        "defaultLanguage": "en",
        "defaultTimezone": "America/Los_Angeles"
    }'::jsonb,
    '{
        "plan": "STARTER",
        "startDate": "2025-10-01",
        "endDate": null,
        "trialEndDate": "2025-11-01",
        "billingCycle": "MONTHLY",
        "amount": 49.00,
        "currency": "USD"
    }'::jsonb,
    'system'
) ON CONFLICT (id) DO NOTHING;

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

-- Tenants table trigger
CREATE TRIGGER trigger_tenants_updated_at
    BEFORE UPDATE ON tenants
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- User-Tenant Association table trigger
CREATE TRIGGER trigger_user_tenant_associations_updated_at
    BEFORE UPDATE ON user_tenant_associations
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

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
ANALYZE tenants;
ANALYZE user_tenant_associations;
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
