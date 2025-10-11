-- =====================================================
-- 04-payment-management.sql
-- Payment Management Database Schema
-- Separate database for payment processing and compliance
-- Date: 2025-10-11
-- =====================================================

-- Enable required PostgreSQL extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =====================================================
-- PAYMENT ENUMS (replicated for isolated database)
-- =====================================================

CREATE TYPE payment_method AS ENUM (
    'CREDIT_CARD',
    'DEBIT_CARD',
    'CASH',
    'BANK_TRANSFER',
    'PAYPAL',
    'STRIPE',
    'APPLE_PAY',
    'GOOGLE_PAY',
    'CRYPTOCURRENCY',
    'CHECK',
    'WIRE_TRANSFER',
    'OTHER'
);

CREATE TYPE payment_status AS ENUM (
    'PENDING',
    'AUTHORIZED',
    'CAPTURED',
    'COMPLETED',
    'FAILED',
    'CANCELLED',
    'REFUNDED',
    'PARTIALLY_REFUNDED',
    'EXPIRED',
    'PROCESSING',
    'DECLINED'
);

CREATE TYPE transaction_type AS ENUM (
    'CHARGE',
    'REFUND',
    'AUTHORIZATION',
    'CAPTURE',
    'VOID',
    'ADJUSTMENT',
    'CHARGEBACK',
    'REVERSAL'
);

-- =====================================================
-- PAYMENT TRANSACTIONS TABLE
-- =====================================================

CREATE TABLE payment_transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    payment_reference VARCHAR(100) UNIQUE NOT NULL,
    reservation_id UUID NOT NULL,
    customer_id UUID NOT NULL,

    -- Amount Details
    amount NUMERIC(10, 2) NOT NULL CHECK (amount >= 0.01),
    currency CHAR(3) NOT NULL,
    processing_fee NUMERIC(10, 2) CHECK (processing_fee >= 0),
    net_amount NUMERIC(10, 2),

    -- Payment Method
    payment_method payment_method NOT NULL,
    transaction_type transaction_type NOT NULL,
    status payment_status NOT NULL DEFAULT 'PENDING',

    -- Gateway Information
    gateway_provider VARCHAR(50),
    gateway_transaction_id VARCHAR(100),
    gateway_response TEXT,
    authorization_code VARCHAR(50),

    -- Card Information (PCI Compliant - tokenized)
    card_token VARCHAR(255),
    card_last_four CHAR(4),
    card_brand VARCHAR(20),
    card_expiry_month SMALLINT,
    card_expiry_year SMALLINT,

    -- Billing Information
    billing_name VARCHAR(100),
    billing_email VARCHAR(100),
    billing_phone VARCHAR(20),
    billing_address_line1 VARCHAR(255),
    billing_address_line2 VARCHAR(255),
    billing_city VARCHAR(100),
    billing_state VARCHAR(100),
    billing_postal_code VARCHAR(20),
    billing_country CHAR(2),

    -- Transaction Details
    description VARCHAR(500),
    failure_reason VARCHAR(255),
    refunded_amount NUMERIC(10, 2) DEFAULT 0.00,

    -- Timestamps
    authorized_at TIMESTAMP,
    captured_at TIMESTAMP,
    settled_at TIMESTAMP,
    failed_at TIMESTAMP,
    refunded_at TIMESTAMP,

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    -- Security
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),

    -- Metadata
    metadata JSONB DEFAULT '{}'::jsonb,

    -- Optimistic Locking
    version BIGINT DEFAULT 0
);

COMMENT ON TABLE payment_transactions IS 'Payment transactions with PCI compliance';
COMMENT ON COLUMN payment_transactions.tenant_id IS 'Multi-tenancy: Tenant owner';
COMMENT ON COLUMN payment_transactions.payment_reference IS 'Unique payment reference';
COMMENT ON COLUMN payment_transactions.card_token IS 'Tokenized card data (PCI compliant)';
COMMENT ON COLUMN payment_transactions.net_amount IS 'Amount after fees';

-- =====================================================
-- PAYMENT AUDIT LOG
-- =====================================================

CREATE TABLE payment_audit_log (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payment_id UUID NOT NULL,
    tenant_id UUID NOT NULL,

    -- Audit Information
    action VARCHAR(50) NOT NULL,
    old_status payment_status,
    new_status payment_status,

    -- Details
    details TEXT,
    error_message TEXT,

    -- Actor
    performed_by UUID,
    performed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Context
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),

    -- Additional Data
    metadata JSONB DEFAULT '{}'::jsonb
);

COMMENT ON TABLE payment_audit_log IS 'Audit trail for all payment operations';

-- =====================================================
-- REFUNDS TABLE
-- =====================================================

CREATE TABLE refunds (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    payment_id UUID NOT NULL,
    refund_reference VARCHAR(100) UNIQUE NOT NULL,

    -- Amount
    refund_amount NUMERIC(10, 2) NOT NULL CHECK (refund_amount > 0),
    currency CHAR(3) NOT NULL,

    -- Status
    status payment_status NOT NULL DEFAULT 'PENDING',

    -- Reason
    refund_reason VARCHAR(500),
    refund_type VARCHAR(50) NOT NULL,

    -- Gateway
    gateway_refund_id VARCHAR(100),
    gateway_response TEXT,

    -- Timestamps
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    completed_at TIMESTAMP,

    -- Actor
    requested_by UUID,
    approved_by UUID,

    -- Audit
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    -- Metadata
    metadata JSONB DEFAULT '{}'::jsonb
);

COMMENT ON TABLE refunds IS 'Refund transactions tracking';

-- =====================================================
-- PAYMENT METHODS (STORED PAYMENT METHODS)
-- =====================================================

CREATE TABLE stored_payment_methods (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    customer_id UUID NOT NULL,

    -- Payment Method Info
    payment_method payment_method NOT NULL,
    card_token VARCHAR(255) NOT NULL,
    card_last_four CHAR(4),
    card_brand VARCHAR(20),
    card_expiry_month SMALLINT,
    card_expiry_year SMALLINT,

    -- Status
    is_default BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,

    -- Billing
    billing_name VARCHAR(100),
    billing_address_line1 VARCHAR(255),
    billing_city VARCHAR(100),
    billing_postal_code VARCHAR(20),
    billing_country CHAR(2),

    -- Gateway
    gateway_customer_id VARCHAR(100),
    gateway_payment_method_id VARCHAR(100),

    -- Audit
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    last_used_at TIMESTAMP,

    -- Metadata
    metadata JSONB DEFAULT '{}'::jsonb
);

COMMENT ON TABLE stored_payment_methods IS 'Tokenized stored payment methods';

-- =====================================================
-- INDEXES
-- =====================================================

-- Payment Transactions Indexes
CREATE INDEX idx_payment_trans_tenant_id ON payment_transactions (tenant_id);
CREATE INDEX idx_payment_trans_reference ON payment_transactions (payment_reference);
CREATE INDEX idx_payment_trans_reservation ON payment_transactions (reservation_id);
CREATE INDEX idx_payment_trans_customer ON payment_transactions (customer_id);
CREATE INDEX idx_payment_trans_status ON payment_transactions (status);
CREATE INDEX idx_payment_trans_gateway ON payment_transactions (gateway_provider, gateway_transaction_id);
CREATE INDEX idx_payment_trans_created ON payment_transactions (created_at DESC);
CREATE INDEX idx_payment_trans_method ON payment_transactions (payment_method);

-- Payment Audit Log Indexes
CREATE INDEX idx_payment_audit_payment_id ON payment_audit_log (payment_id);
CREATE INDEX idx_payment_audit_tenant_id ON payment_audit_log (tenant_id);
CREATE INDEX idx_payment_audit_performed_at ON payment_audit_log (performed_at DESC);
CREATE INDEX idx_payment_audit_action ON payment_audit_log (action);

-- Refunds Indexes
CREATE INDEX idx_refunds_payment_id ON refunds (payment_id);
CREATE INDEX idx_refunds_tenant_id ON refunds (tenant_id);
CREATE INDEX idx_refunds_reference ON refunds (refund_reference);
CREATE INDEX idx_refunds_status ON refunds (status);
CREATE INDEX idx_refunds_requested_at ON refunds (requested_at DESC);

-- Stored Payment Methods Indexes
CREATE INDEX idx_stored_payment_customer ON stored_payment_methods (customer_id);
CREATE INDEX idx_stored_payment_tenant ON stored_payment_methods (tenant_id);
CREATE INDEX idx_stored_payment_active ON stored_payment_methods (customer_id, is_active) WHERE is_active = true;
CREATE INDEX idx_stored_payment_default ON stored_payment_methods (customer_id) WHERE is_default = true;

-- =====================================================
-- CONSTRAINTS
-- =====================================================

-- Payment Audit Log
ALTER TABLE payment_audit_log
    ADD CONSTRAINT fk_payment_audit_payment
    FOREIGN KEY (payment_id)
    REFERENCES payment_transactions(id)
    ON DELETE CASCADE;

-- Refunds
ALTER TABLE refunds
    ADD CONSTRAINT fk_refund_payment
    FOREIGN KEY (payment_id)
    REFERENCES payment_transactions(id)
    ON DELETE RESTRICT;

-- One default payment method per customer
CREATE UNIQUE INDEX idx_one_default_payment_method
    ON stored_payment_methods (customer_id)
    WHERE is_default = true;

-- =====================================================
-- FUNCTIONS AND TRIGGERS
-- =====================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers for updated_at
CREATE TRIGGER update_payment_transactions_updated_at
    BEFORE UPDATE ON payment_transactions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_refunds_updated_at
    BEFORE UPDATE ON refunds
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_stored_payment_methods_updated_at
    BEFORE UPDATE ON stored_payment_methods
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- GRANTS
-- =====================================================

COMMENT ON DATABASE current_database() IS 'Payment Management Database - PCI Compliant Storage';
