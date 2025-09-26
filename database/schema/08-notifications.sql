-- =============================================
-- Notification System Schema
-- Modern Reservation Management System
-- =============================================

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

-- Notification History (for tracking sent notifications)
CREATE TABLE notification_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Reference to original notification
    notification_queue_id UUID REFERENCES notification_queue(id),

    -- Recipient Information
    recipient_type VARCHAR(50) NOT NULL,
    recipient_id UUID,
    recipient_email VARCHAR(255),
    recipient_phone VARCHAR(50),

    -- Notification Details
    notification_type VARCHAR(50) NOT NULL,
    template_key VARCHAR(255),
    subject VARCHAR(500),

    -- Processing Results
    status VARCHAR(20) NOT NULL CHECK (status IN ('sent', 'failed', 'bounced', 'delivered', 'opened', 'clicked')),
    sent_at TIMESTAMPTZ NOT NULL,

    -- Provider Information
    provider VARCHAR(50),
    provider_message_id VARCHAR(255),
    provider_response JSONB,

    -- Context
    property_id UUID,
    reservation_id UUID,

    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
) PARTITION BY RANGE (created_at);

-- Create monthly partitions for notification history
DO $$
BEGIN
    FOR i IN 0..11 LOOP
        EXECUTE format('CREATE TABLE notification_history_%s PARTITION OF notification_history
                       FOR VALUES FROM (%L) TO (%L)',
                       to_char(CURRENT_DATE + (i || ' months')::interval, 'YYYY_MM'),
                       (DATE_TRUNC('month', CURRENT_DATE) + (i || ' months')::interval)::date,
                       (DATE_TRUNC('month', CURRENT_DATE) + ((i+1) || ' months')::interval)::date);
    END LOOP;
END $$;

-- =============================================================================
-- INDEXES FOR PERFORMANCE
-- =============================================================================

-- Notification Template Indexes
CREATE INDEX idx_notification_templates_property ON notification_templates(property_id, template_key) WHERE is_active;
CREATE INDEX idx_notification_templates_type ON notification_templates(template_type) WHERE is_active;

-- Notification Queue Indexes
CREATE INDEX idx_notification_queue_status ON notification_queue(status, scheduled_for);
CREATE INDEX idx_notification_queue_recipient ON notification_queue(recipient_type, recipient_id);
CREATE INDEX idx_notification_queue_retry ON notification_queue(next_retry_at) WHERE status = 'failed';
CREATE INDEX idx_notification_queue_property ON notification_queue(property_id, created_at);

-- Notification History Indexes
CREATE INDEX idx_notification_history_recipient ON notification_history(recipient_type, recipient_id);
CREATE INDEX idx_notification_history_status ON notification_history(status, sent_at);
CREATE INDEX idx_notification_history_property ON notification_history(property_id, sent_at);

-- =============================================================================
-- TRIGGERS
-- =============================================================================

-- Apply updated_at triggers
CREATE TRIGGER update_notification_templates_updated_at
    BEFORE UPDATE ON notification_templates
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_notification_queue_updated_at
    BEFORE UPDATE ON notification_queue
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =============================================================================
-- INITIAL NOTIFICATION TEMPLATES
-- =============================================================================

INSERT INTO notification_templates (template_key, name, template_type, subject_template, body_template, variables) VALUES
('booking_confirmation', 'Booking Confirmation', 'email', 'Booking Confirmation - {{confirmation_number}}',
 'Dear {{guest_name}}, your reservation {{confirmation_number}} has been confirmed for {{property_name}} from {{check_in_date}} to {{check_out_date}}.',
 '["guest_name", "confirmation_number", "check_in_date", "check_out_date", "property_name", "total_amount"]'),

('payment_reminder', 'Payment Reminder', 'email', 'Payment Reminder for {{confirmation_number}}',
 'Dear {{guest_name}}, this is a reminder about the pending payment of {{amount_due}} for your reservation {{confirmation_number}}. Payment is due by {{due_date}}.',
 '["guest_name", "confirmation_number", "amount_due", "due_date", "payment_link"]'),

('check_in_reminder', 'Check-in Reminder', 'email', 'Check-in Reminder - {{confirmation_number}}',
 'Dear {{guest_name}}, we look forward to welcoming you to {{property_name}} tomorrow for your stay from {{check_in_date}} to {{check_out_date}}.',
 '["guest_name", "confirmation_number", "property_name", "check_in_date", "check_out_date", "check_in_time"]'),

('cancellation_confirmation', 'Cancellation Confirmation', 'email', 'Cancellation Confirmation - {{confirmation_number}}',
 'Dear {{guest_name}}, your reservation {{confirmation_number}} has been cancelled as requested. {{refund_details}}',
 '["guest_name", "confirmation_number", "cancellation_date", "refund_amount", "refund_details"]');

-- =============================================================================
-- SCHEMA DOCUMENTATION
-- =============================================================================

COMMENT ON TABLE notification_templates IS 'Configurable notification templates for all communication types';
COMMENT ON TABLE notification_queue IS 'Asynchronous notification processing queue with retry logic';
COMMENT ON TABLE notification_history IS 'Historical record of all sent notifications with delivery status';
