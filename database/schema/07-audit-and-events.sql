-- =============================================
-- Audit Trail and Event Sourcing Schema
-- Modern Reservation Management System
-- =============================================

-- =============================================================================
-- AUDIT TRAIL SYSTEM (Immutable Event Store)
-- =============================================================================

-- Audit Events (Immutable Event Store)
CREATE TABLE audit_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),

    -- Event Metadata
    event_type VARCHAR(100) NOT NULL, -- reservation.created, payment.processed, etc.
    aggregate_type VARCHAR(50) NOT NULL, -- reservation, guest, payment, etc.
    aggregate_id UUID NOT NULL, -- ID of the entity being audited

    -- Context Information
    property_id UUID, -- For property-specific events
    tenant_id UUID, -- For multi-tenant support
    correlation_id UUID, -- For tracing related events
    causation_id UUID, -- Event that caused this event

    -- Event Data
    event_data JSONB NOT NULL, -- The actual event payload
    metadata JSONB DEFAULT '{}', -- Additional metadata

    -- User Context
    user_id UUID, -- User who triggered the event
    user_type VARCHAR(50), -- guest, staff, system, api
    user_agent TEXT, -- Browser/API client information
    ip_address INET, -- Source IP address

    -- Timing
    event_timestamp TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

    -- Event Version (for schema evolution)
    event_version INTEGER NOT NULL DEFAULT 1,

    -- Compliance and Legal
    retention_until DATE, -- GDPR/legal retention requirements
    is_sensitive BOOLEAN DEFAULT FALSE, -- Contains PII/sensitive data

    CONSTRAINT audit_events_valid_timestamp CHECK (event_timestamp <= CURRENT_TIMESTAMP)
) PARTITION BY RANGE (created_at);

-- Create monthly partitions for audit events
DO $$
BEGIN
    FOR i IN 0..11 LOOP
        EXECUTE format('CREATE TABLE audit_events_%s PARTITION OF audit_events
                       FOR VALUES FROM (%L) TO (%L)',
                       to_char(CURRENT_DATE + (i || ' months')::interval, 'YYYY_MM'),
                       (DATE_TRUNC('month', CURRENT_DATE) + (i || ' months')::interval)::date,
                       (DATE_TRUNC('month', CURRENT_DATE) + ((i+1) || ' months')::interval)::date);
    END LOOP;
END $$;

-- Audit Snapshots (for performance optimization)
CREATE TABLE audit_snapshots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id UUID NOT NULL,

    -- Snapshot Data
    snapshot_data JSONB NOT NULL,
    event_sequence BIGINT NOT NULL, -- Last event sequence included

    -- Metadata
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(aggregate_type, aggregate_id)
);

-- System Configuration (key-value store)
CREATE TABLE system_config (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    property_id UUID, -- NULL for global config

    -- Configuration Key-Value
    config_key VARCHAR(255) NOT NULL,
    config_value JSONB NOT NULL,
    data_type VARCHAR(50) NOT NULL CHECK (data_type IN ('string', 'number', 'boolean', 'json', 'array')),

    -- Configuration Metadata
    category VARCHAR(100) NOT NULL, -- feature flags, integrations, ui_settings, etc.
    description TEXT,
    is_encrypted BOOLEAN DEFAULT FALSE,
    is_sensitive BOOLEAN DEFAULT FALSE,

    -- Validation
    validation_rules JSONB, -- JSON schema for validation
    default_value JSONB,

    -- Status
    is_active BOOLEAN DEFAULT TRUE,

    -- Version Control
    config_version INTEGER DEFAULT 1,
    previous_value JSONB,

    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    UNIQUE(property_id, config_key)
);

-- =============================================================================
-- PERFORMANCE AND MONITORING INDEXES
-- =============================================================================

-- Audit Events Indexes
CREATE INDEX idx_audit_events_aggregate ON audit_events(aggregate_type, aggregate_id);
CREATE INDEX idx_audit_events_property ON audit_events(property_id, event_timestamp);
CREATE INDEX idx_audit_events_user ON audit_events(user_id, event_timestamp);
CREATE INDEX idx_audit_events_type ON audit_events(event_type, event_timestamp);
CREATE INDEX idx_audit_events_correlation ON audit_events(correlation_id);
CREATE INDEX idx_audit_events_retention ON audit_events(retention_until) WHERE retention_until IS NOT NULL;

-- Audit Snapshots Indexes
CREATE INDEX idx_audit_snapshots_aggregate ON audit_snapshots(aggregate_type, aggregate_id);
CREATE INDEX idx_audit_snapshots_sequence ON audit_snapshots(event_sequence);

-- System Config Indexes
CREATE INDEX idx_system_config_property ON system_config(property_id, config_key) WHERE is_active;
CREATE INDEX idx_system_config_category ON system_config(category) WHERE is_active;

-- =============================================================================
-- TRIGGERS FOR AUDIT AND UPDATES
-- =============================================================================

-- Apply updated_at triggers
CREATE TRIGGER update_system_config_updated_at
    BEFORE UPDATE ON system_config
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =============================================================================
-- SCHEMA DOCUMENTATION
-- =============================================================================

COMMENT ON TABLE audit_events IS 'Immutable event store for complete audit trail and event sourcing';
COMMENT ON TABLE audit_snapshots IS 'Periodic snapshots of aggregate state for performance optimization';
COMMENT ON TABLE system_config IS 'Flexible key-value configuration system with property-level overrides';

-- =============================================================================
-- AUDIT FUNCTIONS
-- =============================================================================

-- Function to create audit event
CREATE OR REPLACE FUNCTION create_audit_event(
    p_event_type VARCHAR(100),
    p_aggregate_type VARCHAR(50),
    p_aggregate_id UUID,
    p_event_data JSONB,
    p_user_id UUID DEFAULT NULL,
    p_property_id UUID DEFAULT NULL
) RETURNS UUID AS $$
DECLARE
    event_uuid UUID;
BEGIN
    INSERT INTO audit_events (
        event_type, aggregate_type, aggregate_id, event_data,
        user_id, property_id, user_type, ip_address
    ) VALUES (
        p_event_type, p_aggregate_type, p_aggregate_id, p_event_data,
        p_user_id, p_property_id,
        COALESCE(current_setting('application.user_type', true), 'system'),
        COALESCE(current_setting('application.client_ip', true), '127.0.0.1')::inet
    ) RETURNING id INTO event_uuid;

    RETURN event_uuid;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
