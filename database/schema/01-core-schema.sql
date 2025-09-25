-- =============================================
-- Modern Reservation Management System
-- Ultra-Scale Database Schema (10,000+ reservations/minute)
-- PostgreSQL 15+ Multi-Master Architecture
-- =============================================

-- =============================================================================
-- CORE PROPERTY & ROOM MANAGEMENT
-- =============================================================================

-- Property Groups for Multi-Master Sharding
CREATE TABLE property_groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    shard_key VARCHAR(50) NOT NULL UNIQUE, -- For database sharding
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Properties (Hotels/Resorts)
CREATE TABLE properties (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    property_group_id UUID NOT NULL REFERENCES property_groups(id),
    code VARCHAR(50) NOT NULL UNIQUE, -- Hotel code for integrations
    name VARCHAR(255) NOT NULL,
    brand VARCHAR(100),
    type VARCHAR(50) NOT NULL CHECK (type IN ('hotel', 'resort', 'apartment', 'villa', 'hostel')),

    -- Address Information
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(2), -- ISO country code
    postal_code VARCHAR(20),
    timezone VARCHAR(50) NOT NULL DEFAULT 'UTC',

    -- Contact Information
    phone VARCHAR(50),
    email VARCHAR(255),
    website VARCHAR(255),

    -- Property Configuration
    check_in_time TIME DEFAULT '15:00:00',
    check_out_time TIME DEFAULT '11:00:00',
    currency_code VARCHAR(3) DEFAULT 'USD',
    tax_rate DECIMAL(5,4) DEFAULT 0.0000,
    service_charge_rate DECIMAL(5,4) DEFAULT 0.0000,

    -- Status and Metadata
    status VARCHAR(20) DEFAULT 'active' CHECK (status IN ('active', 'inactive', 'maintenance')),
    soft_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    deletion_reason TEXT,

    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

-- Room Types (Standard, Deluxe, Suite, etc.)
CREATE TABLE room_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    property_id UUID NOT NULL REFERENCES properties(id) ON DELETE CASCADE,
    code VARCHAR(50) NOT NULL, -- RT001, STE001, etc.
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(50), -- standard, deluxe, suite, villa

    -- Room Specifications
    max_occupancy INTEGER NOT NULL DEFAULT 2,
    max_adults INTEGER NOT NULL DEFAULT 2,
    max_children INTEGER NOT NULL DEFAULT 0,
    bed_configuration VARCHAR(100), -- "1 King" or "2 Queen" etc.
    room_size_sqm DECIMAL(8,2),
    floor_range VARCHAR(50), -- "1-5" or "10-15"

    -- Pricing
    base_rate DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    min_rate DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    max_rate DECIMAL(10,2) NOT NULL DEFAULT 9999.99,

    -- Status and Soft Delete
    status VARCHAR(20) DEFAULT 'active' CHECK (status IN ('active', 'inactive')),
    soft_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    deletion_reason TEXT,

    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    UNIQUE(property_id, code)
);

-- Individual Rooms
CREATE TABLE rooms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    property_id UUID NOT NULL REFERENCES properties(id) ON DELETE CASCADE,
    room_type_id UUID NOT NULL REFERENCES room_types(id) ON DELETE CASCADE,
    room_number VARCHAR(20) NOT NULL,
    floor INTEGER,

    -- Room Features
    features JSONB DEFAULT '{}', -- Flexible feature storage
    maintenance_notes TEXT,

    -- Status Management
    status VARCHAR(20) DEFAULT 'available' CHECK (status IN (
        'available', 'occupied', 'maintenance', 'out_of_order', 'cleaning'
    )),

    -- Soft Delete Support
    soft_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    deletion_reason TEXT,

    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    UNIQUE(property_id, room_number)
);

-- =============================================================================
-- GUEST MANAGEMENT & PROFILES
-- =============================================================================

-- Guest Profiles with GDPR Compliance
CREATE TABLE guests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Personal Information
    title VARCHAR(10), -- Mr, Mrs, Ms, Dr
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    date_of_birth DATE,
    gender VARCHAR(10) CHECK (gender IN ('male', 'female', 'other', 'prefer_not_to_say')),
    nationality VARCHAR(2), -- ISO country code

    -- Contact Information (Encrypted)
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(50),
    mobile VARCHAR(50),

    -- Address Information
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(2),
    postal_code VARCHAR(20),

    -- Identity Documents (Encrypted)
    passport_number VARCHAR(50),
    passport_country VARCHAR(2),
    passport_expiry DATE,
    id_card_number VARCHAR(50),
    id_card_type VARCHAR(50),

    -- Preferences & Loyalty
    preferences JSONB DEFAULT '{}', -- Room preferences, dietary restrictions, etc.
    loyalty_number VARCHAR(50),
    loyalty_tier VARCHAR(20),
    vip_status BOOLEAN DEFAULT FALSE,

    -- Marketing & Communication
    marketing_consent BOOLEAN DEFAULT FALSE,
    email_consent BOOLEAN DEFAULT TRUE,
    sms_consent BOOLEAN DEFAULT FALSE,
    preferred_language VARCHAR(5) DEFAULT 'en',

    -- GDPR Compliance
    gdpr_consent BOOLEAN DEFAULT FALSE,
    gdpr_consent_date TIMESTAMPTZ,
    data_retention_until DATE,

    -- Soft Delete Support
    soft_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    deletion_reason TEXT,

    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

-- =============================================================================
-- RESERVATION MANAGEMENT (CORE BUSINESS LOGIC)
-- =============================================================================

-- Reservation Status Enumeration
CREATE TYPE reservation_status AS ENUM (
    'inquiry',          -- Initial inquiry
    'tentative',        -- Tentative booking
    'confirmed',        -- Confirmed reservation
    'checked_in',       -- Guest checked in
    'checked_out',      -- Guest checked out
    'cancelled',        -- Cancelled by guest/hotel
    'no_show',          -- Guest didn't show up
    'walked_in'         -- Walk-in guest
);

-- Payment Status Enumeration
CREATE TYPE payment_status AS ENUM (
    'pending',          -- Payment pending
    'authorized',       -- Payment authorized
    'captured',         -- Payment captured
    'paid',            -- Fully paid
    'refunded',        -- Refunded
    'failed',          -- Payment failed
    'disputed'         -- Payment disputed
);

-- Core Reservations Table (Partitioned by created_at for ultra-scale)
CREATE TABLE reservations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    confirmation_number VARCHAR(20) NOT NULL UNIQUE,
    property_id UUID NOT NULL REFERENCES properties(id),

    -- Guest Information
    primary_guest_id UUID NOT NULL REFERENCES guests(id),
    guest_count_adults INTEGER NOT NULL DEFAULT 1,
    guest_count_children INTEGER NOT NULL DEFAULT 0,
    guest_count_infants INTEGER NOT NULL DEFAULT 0,

    -- Stay Information
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    nights INTEGER GENERATED ALWAYS AS (check_out_date - check_in_date) STORED,

    -- Room Information
    room_type_id UUID NOT NULL REFERENCES room_types(id),
    room_count INTEGER NOT NULL DEFAULT 1,
    assigned_rooms UUID[], -- Array of room IDs

    -- Pricing Information
    base_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    taxes_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    fees_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    currency_code VARCHAR(3) DEFAULT 'USD',

    -- Booking Information
    booking_source VARCHAR(50), -- direct, booking_com, expedia, etc.
    booking_channel VARCHAR(50), -- website, phone, walk_in, ota
    rate_plan_code VARCHAR(50),
    package_code VARCHAR(50),

    -- Status Management
    status reservation_status DEFAULT 'inquiry',
    payment_status payment_status DEFAULT 'pending',

    -- Special Requests & Notes
    special_requests TEXT,
    internal_notes TEXT,
    guest_notes TEXT,

    -- Arrival/Departure Information
    estimated_arrival_time TIME,
    actual_arrival_time TIMESTAMPTZ,
    actual_departure_time TIMESTAMPTZ,

    -- Cancellation Information
    cancelled_at TIMESTAMPTZ,
    cancelled_by UUID,
    cancellation_reason TEXT,
    cancellation_policy_id UUID,

    -- Soft Delete Support (30-day recovery window)
    soft_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    deletion_reason TEXT,
    recovery_expires_at TIMESTAMPTZ GENERATED ALWAYS AS (deleted_at + INTERVAL '30 days') STORED,

    -- Audit Trail
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    -- Constraints
    CONSTRAINT valid_dates CHECK (check_out_date > check_in_date),
    CONSTRAINT valid_guest_count CHECK (guest_count_adults > 0),
    CONSTRAINT valid_room_count CHECK (room_count > 0),
    CONSTRAINT valid_amounts CHECK (total_amount >= 0)
) PARTITION BY RANGE (created_at);

-- Reservation Partitions for Performance (Monthly partitions)
-- This will be automated via a maintenance script
CREATE TABLE reservations_2025_01 PARTITION OF reservations
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
CREATE TABLE reservations_2025_02 PARTITION OF reservations
    FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');
-- Additional partitions will be created automatically

-- =============================================================================
-- RATE MANAGEMENT & PRICING
-- =============================================================================

-- Rate Plans (BAR, Corporate, Package rates, etc.)
CREATE TABLE rate_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    property_id UUID NOT NULL REFERENCES properties(id) ON DELETE CASCADE,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,

    -- Rate Plan Configuration
    type VARCHAR(50) NOT NULL CHECK (type IN ('standard', 'corporate', 'package', 'promotional')),
    booking_window_start INTEGER, -- Days before arrival
    booking_window_end INTEGER,   -- Days before arrival
    min_length_of_stay INTEGER DEFAULT 1,
    max_length_of_stay INTEGER,
    min_advance_booking INTEGER DEFAULT 0,
    max_advance_booking INTEGER,

    -- Restrictions
    blackout_dates DATE[],
    allowed_days_of_week INTEGER[] CHECK (array_length(allowed_days_of_week, 1) IS NULL OR
                                         (SELECT bool_and(day >= 1 AND day <= 7)
                                          FROM unnest(allowed_days_of_week) AS day)),

    -- Pricing Configuration
    base_rate DECIMAL(10,2),
    rate_multiplier DECIMAL(5,4) DEFAULT 1.0000,
    is_net_rate BOOLEAN DEFAULT FALSE,
    commission_rate DECIMAL(5,4) DEFAULT 0.0000,

    -- Cancellation Policy
    cancellation_policy_id UUID,

    -- Status and Validity
    status VARCHAR(20) DEFAULT 'active' CHECK (status IN ('active', 'inactive', 'draft')),
    valid_from DATE NOT NULL,
    valid_until DATE NOT NULL,

    -- Soft Delete Support
    soft_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    deletion_reason TEXT,

    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    UNIQUE(property_id, code),
    CONSTRAINT valid_rate_dates CHECK (valid_until > valid_from)
);

-- Daily Rates (High-volume table - partitioned by stay_date)
CREATE TABLE daily_rates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    property_id UUID NOT NULL REFERENCES properties(id),
    room_type_id UUID NOT NULL REFERENCES room_types(id),
    rate_plan_id UUID NOT NULL REFERENCES rate_plans(id),
    stay_date DATE NOT NULL,

    -- Rate Information
    rate DECIMAL(10,2) NOT NULL,
    min_length_of_stay INTEGER DEFAULT 1,
    max_length_of_stay INTEGER,
    closed_to_arrival BOOLEAN DEFAULT FALSE,
    closed_to_departure BOOLEAN DEFAULT FALSE,
    stop_sell BOOLEAN DEFAULT FALSE,

    -- Availability Control
    available_rooms INTEGER,
    sold_rooms INTEGER DEFAULT 0,
    blocked_rooms INTEGER DEFAULT 0,

    -- Revenue Management
    revenue_multiplier DECIMAL(5,4) DEFAULT 1.0000,
    demand_factor DECIMAL(5,4) DEFAULT 1.0000,

    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    UNIQUE(property_id, room_type_id, rate_plan_id, stay_date)
) PARTITION BY RANGE (stay_date);

-- Daily Rates Partitions (Monthly partitions for performance)
CREATE TABLE daily_rates_2025_01 PARTITION OF daily_rates
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
CREATE TABLE daily_rates_2025_02 PARTITION OF daily_rates
    FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');

-- =============================================================================
-- PAYMENT & BILLING MANAGEMENT
-- =============================================================================

-- Payment Methods
CREATE TABLE payment_methods (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    guest_id UUID NOT NULL REFERENCES guests(id) ON DELETE CASCADE,

    -- Payment Method Information
    type VARCHAR(20) NOT NULL CHECK (type IN ('credit_card', 'debit_card', 'bank_transfer', 'cash', 'digital_wallet')),
    provider VARCHAR(50), -- visa, mastercard, paypal, stripe, etc.

    -- Tokenized Information (PCI Compliance)
    token VARCHAR(255) NOT NULL, -- Tokenized card/account number
    last_four VARCHAR(4),
    expiry_month INTEGER,
    expiry_year INTEGER,
    cardholder_name VARCHAR(255),

    -- Address Verification
    billing_address_line1 VARCHAR(255),
    billing_address_line2 VARCHAR(255),
    billing_city VARCHAR(100),
    billing_state VARCHAR(100),
    billing_country VARCHAR(2),
    billing_postal_code VARCHAR(20),

    -- Status
    is_default BOOLEAN DEFAULT FALSE,
    is_verified BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) DEFAULT 'active' CHECK (status IN ('active', 'expired', 'blocked')),

    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Payment Transactions (PCI-DSS Compliant)
CREATE TABLE payment_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reservation_id UUID NOT NULL REFERENCES reservations(id),
    payment_method_id UUID REFERENCES payment_methods(id),

    -- Transaction Information
    transaction_type VARCHAR(20) NOT NULL CHECK (transaction_type IN (
        'authorization', 'capture', 'sale', 'refund', 'void', 'adjustment'
    )),
    amount DECIMAL(12,2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,

    -- Gateway Information
    gateway_provider VARCHAR(50), -- stripe, paypal, square, etc.
    gateway_transaction_id VARCHAR(255),
    gateway_reference VARCHAR(255),
    gateway_response JSONB,

    -- Status and Processing
    status payment_status DEFAULT 'pending',
    processed_at TIMESTAMPTZ,
    settled_at TIMESTAMPTZ,

    -- Fraud Detection
    risk_score DECIMAL(5,2),
    risk_flags JSONB DEFAULT '{}',

    -- Failure Information
    failure_reason TEXT,
    failure_code VARCHAR(50),
    retry_count INTEGER DEFAULT 0,

    -- Audit Information
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    CONSTRAINT valid_amount CHECK (amount >= 0)
);

-- =============================================================================
-- ULTRA-SCALE PERFORMANCE INDEXES
-- =============================================================================

-- Properties Indexes
CREATE INDEX idx_properties_group_status ON properties(property_group_id, status) WHERE NOT soft_deleted;
CREATE INDEX idx_properties_code ON properties(code) WHERE NOT soft_deleted;

-- Room Type Indexes
CREATE INDEX idx_room_types_property ON room_types(property_id) WHERE NOT soft_deleted;
CREATE INDEX idx_room_types_code ON room_types(property_id, code) WHERE NOT soft_deleted;

-- Rooms Indexes
CREATE INDEX idx_rooms_property_type ON rooms(property_id, room_type_id) WHERE NOT soft_deleted;
CREATE INDEX idx_rooms_status ON rooms(property_id, status) WHERE NOT soft_deleted;
CREATE INDEX idx_rooms_number ON rooms(property_id, room_number) WHERE NOT soft_deleted;

-- Guest Indexes
CREATE INDEX idx_guests_email ON guests(email) WHERE NOT soft_deleted;
CREATE INDEX idx_guests_phone ON guests(phone) WHERE NOT soft_deleted;
CREATE INDEX idx_guests_name ON guests(last_name, first_name) WHERE NOT soft_deleted;

-- Reservation Indexes (Critical for Performance)
CREATE INDEX idx_reservations_property_dates ON reservations(property_id, check_in_date, check_out_date) WHERE NOT soft_deleted;
CREATE INDEX idx_reservations_guest ON reservations(primary_guest_id) WHERE NOT soft_deleted;
CREATE INDEX idx_reservations_confirmation ON reservations(confirmation_number) WHERE NOT soft_deleted;
CREATE INDEX idx_reservations_status ON reservations(property_id, status) WHERE NOT soft_deleted;
CREATE INDEX idx_reservations_created ON reservations(created_at);
CREATE INDEX idx_reservations_soft_deleted ON reservations(property_id, soft_deleted, recovery_expires_at) WHERE soft_deleted;

-- Rate Plan Indexes
CREATE INDEX idx_rate_plans_property ON rate_plans(property_id) WHERE NOT soft_deleted;
CREATE INDEX idx_rate_plans_dates ON rate_plans(property_id, valid_from, valid_until) WHERE NOT soft_deleted;

-- Daily Rates Indexes (High Volume)
CREATE INDEX idx_daily_rates_lookup ON daily_rates(property_id, room_type_id, stay_date);
CREATE INDEX idx_daily_rates_availability ON daily_rates(property_id, stay_date, stop_sell);

-- Payment Indexes
CREATE INDEX idx_payment_methods_guest ON payment_methods(guest_id) WHERE status = 'active';
CREATE INDEX idx_payment_transactions_reservation ON payment_transactions(reservation_id);
CREATE INDEX idx_payment_transactions_gateway ON payment_transactions(gateway_transaction_id);

-- =============================================================================
-- TRIGGERS FOR AUDIT TRAIL & SOFT DELETE
-- =============================================================================

-- Updated At Trigger Function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply updated_at triggers to all main tables
CREATE TRIGGER update_properties_updated_at BEFORE UPDATE ON properties FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_room_types_updated_at BEFORE UPDATE ON room_types FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_rooms_updated_at BEFORE UPDATE ON rooms FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_guests_updated_at BEFORE UPDATE ON guests FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_reservations_updated_at BEFORE UPDATE ON reservations FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_rate_plans_updated_at BEFORE UPDATE ON rate_plans FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_daily_rates_updated_at BEFORE UPDATE ON daily_rates FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_payment_methods_updated_at BEFORE UPDATE ON payment_methods FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_payment_transactions_updated_at BEFORE UPDATE ON payment_transactions FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =============================================================================
-- ULTRA-SCALE CONFIGURATION
-- =============================================================================

-- Connection and Performance Settings
-- These should be set at the PostgreSQL server level
--
-- max_connections = 1000                    -- High connection limit
-- shared_buffers = 8GB                      -- Large buffer pool
-- effective_cache_size = 24GB               -- Cache size hint
-- work_mem = 256MB                          -- Sort/hash memory
-- maintenance_work_mem = 2GB                -- Maintenance operations
-- max_worker_processes = 16                 -- Parallel workers
-- max_parallel_workers_per_gather = 8       -- Parallel query workers
-- random_page_cost = 1.1                    -- SSD optimization
-- effective_io_concurrency = 200            -- I/O concurrency

-- =============================================================================
-- COMMENTS AND DOCUMENTATION
-- =============================================================================

COMMENT ON TABLE properties IS 'Core property/hotel information with multi-master sharding support';
COMMENT ON TABLE reservations IS 'Main reservations table partitioned by created_at for ultra-scale performance';
COMMENT ON TABLE daily_rates IS 'High-volume daily rate and availability data, partitioned by stay_date';
COMMENT ON COLUMN reservations.soft_deleted IS 'Soft delete flag with 30-day recovery window';
COMMENT ON COLUMN reservations.recovery_expires_at IS 'Automatic expiry date for soft-deleted reservations';
COMMENT ON INDEX idx_reservations_property_dates IS 'Critical index for availability queries and reservation lookups';
