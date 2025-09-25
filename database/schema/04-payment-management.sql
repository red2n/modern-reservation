-- Payment and Financial Management Tables
-- Tables for managing payments, refunds, and financial transactions

-- Main payments table
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    reservation_id UUID NOT NULL,
    guest_id UUID NOT NULL,
    amount DECIMAL(12,2) NOT NULL CHECK (amount >= 0),
    currency CHAR(3) NOT NULL DEFAULT 'USD',
    payment_method payment_method NOT NULL,
    payment_provider VARCHAR(100) NOT NULL,
    transaction_id VARCHAR(255) NOT NULL,
    status payment_status NOT NULL DEFAULT 'pending',
    description TEXT,
    reference VARCHAR(255),

    -- Payment details (tokenized/encrypted)
    payment_token VARCHAR(255),
    last_four_digits CHAR(4),
    card_type VARCHAR(50),
    expiry_month INTEGER CHECK (expiry_month BETWEEN 1 AND 12),
    expiry_year INTEGER CHECK (expiry_year >= EXTRACT(YEAR FROM CURRENT_DATE)),
    card_holder_name VARCHAR(255),

    -- Processing details
    processed_at TIMESTAMP WITH TIME ZONE,
    failure_reason TEXT,
    gateway_response JSONB,

    -- Billing information
    billing_address JSONB,

    -- Reconciliation
    reconciled BOOLEAN NOT NULL DEFAULT false,
    reconciled_at TIMESTAMP WITH TIME ZONE,
    reconciliation_reference VARCHAR(255),

    -- Metadata
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID NOT NULL,

    FOREIGN KEY (guest_id) REFERENCES guests(id)
);

-- Payment refunds table
CREATE TABLE refunds (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payment_id UUID NOT NULL,
    original_payment_id UUID, -- For partial refunds tracking
    amount DECIMAL(12,2) NOT NULL CHECK (amount > 0),
    currency CHAR(3) NOT NULL DEFAULT 'USD',
    reason TEXT NOT NULL,
    refund_method VARCHAR(50) NOT NULL DEFAULT 'original_payment_method',
    status VARCHAR(50) NOT NULL DEFAULT 'pending',

    -- Processing details
    processed_at TIMESTAMP WITH TIME ZONE,
    failure_reason TEXT,
    gateway_response JSONB,
    reference_number VARCHAR(255),

    -- Reconciliation
    reconciled BOOLEAN NOT NULL DEFAULT false,
    reconciled_at TIMESTAMP WITH TIME ZONE,

    -- Metadata
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID NOT NULL,

    FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE
);

-- Payment attempts (for tracking failed payments)
CREATE TABLE payment_attempts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    reservation_id UUID NOT NULL,
    guest_id UUID NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    currency CHAR(3) NOT NULL,
    payment_method payment_method NOT NULL,
    payment_provider VARCHAR(100) NOT NULL,

    -- Attempt details
    attempt_number INTEGER NOT NULL DEFAULT 1,
    status VARCHAR(50) NOT NULL, -- pending, success, failed, cancelled
    failure_reason TEXT,
    gateway_response JSONB,

    -- Final payment if successful
    payment_id UUID,

    -- Metadata
    attempted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (guest_id) REFERENCES guests(id),
    FOREIGN KEY (payment_id) REFERENCES payments(id)
);

-- Invoice management
CREATE TABLE invoices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    reservation_id UUID NOT NULL,
    guest_id UUID NOT NULL,
    property_id UUID NOT NULL,

    -- Invoice details
    invoice_date DATE NOT NULL DEFAULT CURRENT_DATE,
    due_date DATE NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL CHECK (subtotal >= 0),
    tax_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(12,2) NOT NULL CHECK (total_amount >= 0),
    currency CHAR(3) NOT NULL DEFAULT 'USD',

    -- Status
    status VARCHAR(50) NOT NULL DEFAULT 'draft', -- draft, sent, paid, overdue, cancelled
    paid_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    balance_due DECIMAL(12,2) NOT NULL DEFAULT 0.00,

    -- Payment terms
    payment_terms TEXT,
    notes TEXT,

    -- Document management
    pdf_path VARCHAR(500),
    sent_at TIMESTAMP WITH TIME ZONE,

    -- Metadata
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID NOT NULL,

    FOREIGN KEY (guest_id) REFERENCES guests(id),
    FOREIGN KEY (property_id) REFERENCES properties(id)
);

-- Invoice line items
CREATE TABLE invoice_line_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    invoice_id UUID NOT NULL,
    line_number INTEGER NOT NULL,
    description VARCHAR(255) NOT NULL,
    quantity DECIMAL(8,2) NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    line_total DECIMAL(12,2) NOT NULL,
    tax_rate DECIMAL(5,4) DEFAULT 0.0000,
    tax_amount DECIMAL(10,2) DEFAULT 0.00,

    -- Item classification
    item_type VARCHAR(50) NOT NULL, -- room, service, tax, fee, etc.
    item_date DATE,

    FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE
);

-- Payment gateway configurations
CREATE TABLE payment_gateways (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    property_id UUID NOT NULL,
    provider_name VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_primary BOOLEAN NOT NULL DEFAULT false,

    -- Configuration (encrypted)
    configuration JSONB NOT NULL,

    -- Supported features
    supports_credit_cards BOOLEAN NOT NULL DEFAULT true,
    supports_debit_cards BOOLEAN NOT NULL DEFAULT true,
    supports_ach BOOLEAN NOT NULL DEFAULT false,
    supports_digital_wallets BOOLEAN NOT NULL DEFAULT false,
    supported_currencies JSONB NOT NULL DEFAULT '["USD"]',

    -- Fees and limits
    transaction_fee_percent DECIMAL(5,4) DEFAULT 0.0000,
    transaction_fee_fixed DECIMAL(8,2) DEFAULT 0.00,
    daily_limit DECIMAL(12,2),
    monthly_limit DECIMAL(15,2),

    -- Metadata
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID NOT NULL,

    FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE
);

-- Financial reporting periods
CREATE TABLE financial_periods (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    property_id UUID NOT NULL,
    period_name VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_closed BOOLEAN NOT NULL DEFAULT false,
    closed_at TIMESTAMP WITH TIME ZONE,
    closed_by UUID,

    -- Summary totals
    total_revenue DECIMAL(15,2) DEFAULT 0.00,
    total_payments DECIMAL(15,2) DEFAULT 0.00,
    total_refunds DECIMAL(15,2) DEFAULT 0.00,
    net_revenue DECIMAL(15,2) DEFAULT 0.00,

    -- Metadata
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID NOT NULL,

    FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE,
    UNIQUE(property_id, period_name)
);

-- Indexes for payments
CREATE INDEX idx_payments_reservation_id ON payments(reservation_id);
CREATE INDEX idx_payments_guest_id ON payments(guest_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_payment_method ON payments(payment_method);
CREATE INDEX idx_payments_transaction_id ON payments(transaction_id);
CREATE INDEX idx_payments_processed_at ON payments(processed_at);
CREATE INDEX idx_payments_amount ON payments(amount);
CREATE INDEX idx_payments_created_at ON payments(created_at);
CREATE INDEX idx_payments_reconciled ON payments(reconciled) WHERE reconciled = false;

-- Indexes for refunds
CREATE INDEX idx_refunds_payment_id ON refunds(payment_id);
CREATE INDEX idx_refunds_status ON refunds(status);
CREATE INDEX idx_refunds_processed_at ON refunds(processed_at);
CREATE INDEX idx_refunds_amount ON refunds(amount);
CREATE INDEX idx_refunds_created_at ON refunds(created_at);

-- Indexes for payment attempts
CREATE INDEX idx_payment_attempts_reservation_id ON payment_attempts(reservation_id);
CREATE INDEX idx_payment_attempts_guest_id ON payment_attempts(guest_id);
CREATE INDEX idx_payment_attempts_status ON payment_attempts(status);
CREATE INDEX idx_payment_attempts_attempted_at ON payment_attempts(attempted_at);

-- Indexes for invoices
CREATE INDEX idx_invoices_invoice_number ON invoices(invoice_number);
CREATE INDEX idx_invoices_reservation_id ON invoices(reservation_id);
CREATE INDEX idx_invoices_guest_id ON invoices(guest_id);
CREATE INDEX idx_invoices_property_id ON invoices(property_id);
CREATE INDEX idx_invoices_status ON invoices(status);
CREATE INDEX idx_invoices_due_date ON invoices(due_date);
CREATE INDEX idx_invoices_invoice_date ON invoices(invoice_date);

-- Indexes for invoice line items
CREATE INDEX idx_invoice_line_items_invoice_id ON invoice_line_items(invoice_id);
CREATE INDEX idx_invoice_line_items_type ON invoice_line_items(item_type);
CREATE INDEX idx_invoice_line_items_date ON invoice_line_items(item_date);

-- Indexes for payment gateways
CREATE INDEX idx_payment_gateways_property_id ON payment_gateways(property_id);
CREATE INDEX idx_payment_gateways_active ON payment_gateways(is_active) WHERE is_active = true;
CREATE INDEX idx_payment_gateways_primary ON payment_gateways(is_primary) WHERE is_primary = true;

-- Indexes for financial periods
CREATE INDEX idx_financial_periods_property_id ON financial_periods(property_id);
CREATE INDEX idx_financial_periods_dates ON financial_periods(start_date, end_date);
CREATE INDEX idx_financial_periods_closed ON financial_periods(is_closed);

-- Check constraints
ALTER TABLE payments ADD CONSTRAINT chk_payments_expiry_date CHECK (
    (expiry_month IS NULL AND expiry_year IS NULL) OR
    (expiry_month IS NOT NULL AND expiry_year IS NOT NULL)
);
ALTER TABLE refunds ADD CONSTRAINT chk_refunds_amount_positive CHECK (amount > 0);
ALTER TABLE invoices ADD CONSTRAINT chk_invoices_due_date CHECK (due_date >= invoice_date);
ALTER TABLE invoices ADD CONSTRAINT chk_invoices_balance CHECK (balance_due = total_amount - paid_amount);
ALTER TABLE financial_periods ADD CONSTRAINT chk_financial_periods_dates CHECK (end_date > start_date);

-- Triggers for updated_at
CREATE TRIGGER trigger_payments_updated_at
    BEFORE UPDATE ON payments
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_refunds_updated_at
    BEFORE UPDATE ON refunds
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_invoices_updated_at
    BEFORE UPDATE ON invoices
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_payment_gateways_updated_at
    BEFORE UPDATE ON payment_gateways
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_financial_periods_updated_at
    BEFORE UPDATE ON financial_periods
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
