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

-- Audit Events Partitions (Monthly partitions for performance)
CREATE TABLE audit_events_2025_01 PARTITION OF audit_events
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
CREATE TABLE audit_events_2025_02 PARTITION OF audit_events
    FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');

-- Audit Event Snapshots (Periodic snapshots for performance)
CREATE TABLE audit_snapshots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id UUID NOT NULL,

    -- Snapshot Data
    snapshot_data JSONB NOT NULL,
    snapshot_version INTEGER NOT NULL,

    -- Latest Event Information
    last_event_id UUID NOT NULL,
    last_event_timestamp TIMESTAMPTZ NOT NULL,

    -- Snapshot Metadata
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) DEFAULT 'system',

    UNIQUE(aggregate_type, aggregate_id, snapshot_version)
);

-- =============================================================================
-- SYSTEM CONFIGURATION & SETTINGS
-- =============================================================================

-- System Configuration
CREATE TABLE system_config (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    property_id UUID REFERENCES properties(id), -- NULL for global settings

    -- Configuration Key-Value
    config_key VARCHAR(255) NOT NULL,
    config_value JSONB NOT NULL,
    config_type VARCHAR(50) NOT NULL CHECK (config_type IN (
        'string', 'number', 'boolean', 'json', 'array'
    )),

    -- Configuration Metadata
    category VARCHAR(100), -- rates, payments, integrations, etc.
    description TEXT,
    is_sensitive BOOLEAN DEFAULT FALSE, -- Encrypted storage required
    is_system BOOLEAN DEFAULT FALSE, -- System-managed, not user-editable

    -- Validation
    validation_rules JSONB DEFAULT '{}', -- JSON schema for validation

    -- Status
    is_active BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    UNIQUE(property_id, config_key)
);

-- =============================================================================
-- USER MANAGEMENT & AUTHENTICATION
-- =============================================================================

-- User Roles and Permissions
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    permissions JSONB NOT NULL DEFAULT '[]', -- Array of permission strings
    is_system_role BOOLEAN DEFAULT FALSE,

    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- System Users (Staff, API users, etc.)
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Authentication
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255), -- Hashed password

    -- Personal Information
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(50),

    -- System Information
    role_id UUID REFERENCES roles(id),
    properties UUID[] DEFAULT '{}', -- Properties user has access to

    -- Account Status
    is_active BOOLEAN DEFAULT TRUE,
    is_verified BOOLEAN DEFAULT FALSE,
    email_verified_at TIMESTAMPTZ,

    -- Security
    failed_login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMPTZ,
    must_change_password BOOLEAN DEFAULT FALSE,
    password_changed_at TIMESTAMPTZ,

    -- Multi-Factor Authentication
    mfa_enabled BOOLEAN DEFAULT FALSE,
    mfa_secret VARCHAR(255), -- TOTP secret
    backup_codes TEXT[], -- Recovery codes

    -- Session Management
    last_login_at TIMESTAMPTZ,
    last_login_ip INET,

    -- Soft Delete
    soft_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    deletion_reason TEXT,

    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

-- User Sessions (for session management)
CREATE TABLE user_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    -- Session Information
    session_token VARCHAR(255) NOT NULL UNIQUE,
    refresh_token VARCHAR(255) UNIQUE,
    device_fingerprint VARCHAR(255),

    -- Session Metadata
    ip_address INET,
    user_agent TEXT,
    device_type VARCHAR(50), -- web, mobile, api

    -- Session Lifecycle
    expires_at TIMESTAMPTZ NOT NULL,
    last_activity_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

    -- Security
    is_active BOOLEAN DEFAULT TRUE,
    terminated_at TIMESTAMPTZ,
    termination_reason VARCHAR(100),

    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT valid_session_expiry CHECK (expires_at > created_at)
);

-- =============================================================================
-- NOTIFICATION SYSTEM
-- =============================================================================

-- Notification Templates
CREATE TABLE notification_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    property_id UUID REFERENCES properties(id), -- NULL for global templates

    -- Template Information
    template_key VARCHAR(255) NOT NULL, -- booking_confirmation, payment_reminder, etc.
    name VARCHAR(255) NOT NULL,
    description TEXT,

    -- Template Content
    subject_template TEXT, -- For email notifications
    body_template TEXT NOT NULL, -- Template with placeholders
    template_type VARCHAR(50) NOT NULL CHECK (template_type IN (
        'email', 'sms', 'push', 'in_app', 'webhook'
    )),

    -- Template Configuration
    variables JSONB DEFAULT '[]', -- Available template variables
    default_language VARCHAR(5) DEFAULT 'en',

    -- Status
    is_active BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    UNIQUE(property_id, template_key, template_type)
);

-- Notification Queue
CREATE TABLE notification_queue (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Target Information
    recipient_type VARCHAR(50) NOT NULL CHECK (recipient_type IN ('guest', 'user', 'external')),
    recipient_id UUID, -- guest_id or user_id
    recipient_email VARCHAR(255),
    recipient_phone VARCHAR(50),

    -- Notification Content
    notification_type VARCHAR(50) NOT NULL, -- email, sms, push, etc.
    template_id UUID REFERENCES notification_templates(id),
    subject VARCHAR(500),
    content TEXT NOT NULL,

    -- Context
    property_id UUID REFERENCES properties(id),
    reservation_id UUID REFERENCES reservations(id),
    related_entity_type VARCHAR(50),
    related_entity_id UUID,

    -- Processing Status
    status VARCHAR(20) DEFAULT 'pending' CHECK (status IN (
        'pending', 'processing', 'sent', 'failed', 'cancelled'
    )),

    -- Scheduling
    scheduled_for TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMPTZ,

    -- Retry Logic
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    next_retry_at TIMESTAMPTZ,

    -- Error Handling
    error_message TEXT,
    error_code VARCHAR(50),

    -- External Provider Information
    provider VARCHAR(50), -- sendgrid, twilio, firebase, etc.
    provider_message_id VARCHAR(255),
    provider_response JSONB,

    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- INTEGRATION & CHANNEL MANAGEMENT
-- =============================================================================

-- Channel Configurations (OTA, GDS, etc.)
CREATE TABLE channels (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    property_id UUID NOT NULL REFERENCES properties(id),

    -- Channel Information
    channel_code VARCHAR(50) NOT NULL, -- booking_com, expedia, direct, etc.
    channel_name VARCHAR(255) NOT NULL,
    channel_type VARCHAR(50) NOT NULL CHECK (channel_type IN (
        'ota', 'gds', 'direct', 'wholesaler', 'corporate'
    )),

    -- Integration Configuration
    api_endpoint VARCHAR(500),
    api_credentials JSONB, -- Encrypted API keys, tokens, etc.
    sync_enabled BOOLEAN DEFAULT TRUE,

    -- Sync Configuration
    sync_rates BOOLEAN DEFAULT TRUE,
    sync_availability BOOLEAN DEFAULT TRUE,
    sync_reservations BOOLEAN DEFAULT TRUE,
    sync_frequency_minutes INTEGER DEFAULT 15,

    -- Rate Configuration
    rate_plan_mappings JSONB DEFAULT '{}', -- Map internal rate plans to channel rate plans
    commission_rate DECIMAL(5,4) DEFAULT 0.0000,

    -- Status
    status VARCHAR(20) DEFAULT 'active' CHECK (status IN ('active', 'inactive', 'testing')),
    last_sync_at TIMESTAMPTZ,
    last_sync_status VARCHAR(20),

    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(property_id, channel_code)
);

-- Channel Sync Logs
CREATE TABLE channel_sync_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    channel_id UUID NOT NULL REFERENCES channels(id),

    -- Sync Information
    sync_type VARCHAR(50) NOT NULL, -- rates, availability, reservations
    sync_direction VARCHAR(20) NOT NULL CHECK (sync_direction IN ('push', 'pull', 'bidirectional')),

    -- Processing Details
    started_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    status VARCHAR(20) NOT NULL CHECK (status IN ('running', 'completed', 'failed', 'timeout')),

    -- Results
    records_processed INTEGER DEFAULT 0,
    records_success INTEGER DEFAULT 0,
    records_failed INTEGER DEFAULT 0,

    -- Error Information
    error_message TEXT,
    error_details JSONB,

    -- Performance Metrics
    processing_time_ms INTEGER,

    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
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

-- System Config Indexes
CREATE INDEX idx_system_config_property ON system_config(property_id, config_key) WHERE is_active;
CREATE INDEX idx_system_config_category ON system_config(category) WHERE is_active;

-- User Management Indexes
CREATE INDEX idx_users_email ON users(email) WHERE NOT soft_deleted;
CREATE INDEX idx_users_username ON users(username) WHERE NOT soft_deleted;
CREATE INDEX idx_users_role ON users(role_id) WHERE NOT soft_deleted AND is_active;

-- Session Indexes
CREATE INDEX idx_user_sessions_token ON user_sessions(session_token) WHERE is_active;
CREATE INDEX idx_user_sessions_user ON user_sessions(user_id, is_active);
CREATE INDEX idx_user_sessions_expiry ON user_sessions(expires_at) WHERE is_active;

-- Notification Indexes
CREATE INDEX idx_notification_queue_status ON notification_queue(status, scheduled_for);
CREATE INDEX idx_notification_queue_recipient ON notification_queue(recipient_type, recipient_id);
CREATE INDEX idx_notification_queue_retry ON notification_queue(next_retry_at) WHERE status = 'failed';

-- Channel Management Indexes
CREATE INDEX idx_channels_property ON channels(property_id, status);
CREATE INDEX idx_channel_sync_logs_channel ON channel_sync_logs(channel_id, started_at);

-- =============================================================================
-- TRIGGERS FOR AUDIT AND SOFT DELETE
-- =============================================================================

-- Apply updated_at triggers
CREATE TRIGGER update_system_config_updated_at BEFORE UPDATE ON system_config FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_notification_templates_updated_at BEFORE UPDATE ON notification_templates FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_notification_queue_updated_at BEFORE UPDATE ON notification_queue FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_channels_updated_at BEFORE UPDATE ON channels FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =============================================================================
-- SCHEMA DOCUMENTATION
-- =============================================================================

COMMENT ON TABLE audit_events IS 'Immutable event store for complete audit trail and event sourcing';
COMMENT ON TABLE audit_snapshots IS 'Periodic snapshots of aggregate state for performance optimization';
COMMENT ON TABLE system_config IS 'Flexible key-value configuration system with property-level overrides';
COMMENT ON TABLE notification_queue IS 'Asynchronous notification processing queue with retry logic';
COMMENT ON TABLE channels IS 'Channel manager integration configuration for OTAs and other distribution channels';

-- =============================================================================
-- INITIAL DATA SETUP
-- =============================================================================

-- Default System Roles
INSERT INTO roles (name, description, permissions, is_system_role) VALUES
('super_admin', 'System Super Administrator', '["*"]', true),
('hotel_admin', 'Hotel Administrator', '["property.manage", "users.manage", "reservations.manage", "rates.manage", "reports.view"]', true),
('front_desk', 'Front Desk Staff', '["reservations.manage", "guests.manage", "checkin.manage", "checkout.manage"]', true),
('housekeeping', 'Housekeeping Staff', '["rooms.manage", "maintenance.manage", "housekeeping.manage"]', true),
('revenue_manager', 'Revenue Manager', '["rates.manage", "availability.manage", "reports.view", "channels.manage"]', true),
('guest', 'Guest User', '["reservation.view", "profile.manage"]', true);

-- Default Notification Templates
INSERT INTO notification_templates (template_key, name, template_type, subject_template, body_template, variables) VALUES
('booking_confirmation', 'Booking Confirmation', 'email', 'Booking Confirmation - {{confirmation_number}}',
 'Dear {{guest_name}}, your reservation {{confirmation_number}} has been confirmed...',
 '["guest_name", "confirmation_number", "check_in_date", "check_out_date", "property_name"]'),
('payment_reminder', 'Payment Reminder', 'email', 'Payment Reminder for {{confirmation_number}}',
 'Dear {{guest_name}}, this is a reminder about the pending payment for your reservation...',
 '["guest_name", "confirmation_number", "amount_due", "due_date"]');
