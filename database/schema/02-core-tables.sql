-- =====================================================
-- 02-core-tables.sql
-- Core Table Definitions from Entity Classes
-- Generated from JPA Entities
-- Date: 2025-10-06
-- Updated: 2025-10-08 (Multi-tenancy support added)
-- =====================================================

-- =====================================================
-- MULTI-TENANCY TABLES
-- =====================================================

-- Tenants Table (Organizations)
CREATE TABLE tenants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- Basic Information
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(200) UNIQUE NOT NULL,
    type tenant_type NOT NULL,
    status tenant_status NOT NULL DEFAULT 'TRIAL',

    -- Contact Information
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    website VARCHAR(500),

    -- Address
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country CHAR(2), -- ISO 3166-1 alpha-2

    -- Business Information
    tax_id VARCHAR(100),
    business_license VARCHAR(100),
    registration_number VARCHAR(100),

    -- Configuration (stored as JSONB for flexibility)
    config JSONB NOT NULL DEFAULT '{
        "brandingEnabled": true,
        "enableMultiProperty": true,
        "enableChannelManager": false,
        "enableAdvancedReporting": false,
        "enablePaymentProcessing": true,
        "enableLoyaltyProgram": false,
        "maxProperties": 5,
        "maxUsers": 10,
        "defaultCurrency": "USD",
        "defaultLanguage": "en",
        "defaultTimezone": "UTC"
    }'::jsonb,

    -- Subscription Information (stored as JSONB)
    subscription JSONB NOT NULL DEFAULT '{
        "plan": "FREE",
        "startDate": null,
        "endDate": null,
        "trialEndDate": null,
        "billingCycle": "MONTHLY",
        "amount": 0,
        "currency": "USD"
    }'::jsonb,

    -- Metadata (custom fields)
    metadata JSONB DEFAULT '{}'::jsonb,

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    -- Soft Delete
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),

    -- Optimistic Locking
    version BIGINT DEFAULT 0,

    -- Constraints
    CONSTRAINT tenants_slug_format CHECK (slug ~ '^[a-z0-9-]+$')
);

COMMENT ON TABLE tenants IS 'Multi-tenant organizations (chains, franchises, independent properties)';
COMMENT ON COLUMN tenants.id IS 'Unique tenant identifier';
COMMENT ON COLUMN tenants.name IS 'Tenant organization name';
COMMENT ON COLUMN tenants.slug IS 'URL-friendly unique identifier';
COMMENT ON COLUMN tenants.type IS 'Type of tenant organization';
COMMENT ON COLUMN tenants.status IS 'Current tenant account status';
COMMENT ON COLUMN tenants.config IS 'Tenant configuration settings (JSONB)';
COMMENT ON COLUMN tenants.subscription IS 'Subscription and billing information (JSONB)';
COMMENT ON COLUMN tenants.metadata IS 'Custom metadata fields (JSONB)';
COMMENT ON COLUMN tenants.deleted_at IS 'Soft delete timestamp';

-- User-Tenant Association Table (Many-to-Many)
CREATE TABLE user_tenant_associations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    role tenant_role NOT NULL,
    permissions TEXT[] DEFAULT ARRAY[]::TEXT[],
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_primary BOOLEAN NOT NULL DEFAULT false,
    assigned_by UUID,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    last_accessed_at TIMESTAMP,

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    -- Constraints
    UNIQUE(user_id, tenant_id)
);

COMMENT ON TABLE user_tenant_associations IS 'Many-to-many relationship between users and tenants';
COMMENT ON COLUMN user_tenant_associations.user_id IS 'Reference to user';
COMMENT ON COLUMN user_tenant_associations.tenant_id IS 'Reference to tenant';
COMMENT ON COLUMN user_tenant_associations.role IS 'User role within the tenant';
COMMENT ON COLUMN user_tenant_associations.permissions IS 'Additional granular permissions';
COMMENT ON COLUMN user_tenant_associations.is_primary IS 'Primary tenant for the user';
COMMENT ON COLUMN user_tenant_associations.last_accessed_at IS 'Last time user accessed this tenant';

-- =====================================================
-- PROPERTY MANAGEMENT TABLES
-- =====================================================

-- Properties Table
CREATE TABLE properties (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,

    -- Basic Information
    property_code VARCHAR(50) UNIQUE NOT NULL,
    property_name VARCHAR(200) NOT NULL,
    property_type VARCHAR(50) NOT NULL,
    description TEXT,

    -- Contact Information
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    website VARCHAR(500),

    -- Address
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country CHAR(2) NOT NULL, -- ISO 3166-1 alpha-2

    -- Property Details
    total_rooms INTEGER NOT NULL CHECK (total_rooms > 0),
    total_floors INTEGER,
    check_in_time VARCHAR(5) DEFAULT '15:00',
    check_out_time VARCHAR(5) DEFAULT '11:00',
    currency CHAR(3) NOT NULL DEFAULT 'USD',
    timezone VARCHAR(50) DEFAULT 'UTC',

    -- Amenities (stored as JSONB array)
    amenities JSONB DEFAULT '[]'::jsonb,

    -- Configuration
    config JSONB DEFAULT '{}'::jsonb,

    -- Status
    is_active BOOLEAN NOT NULL DEFAULT true,

    -- Metadata
    metadata JSONB DEFAULT '{}'::jsonb,

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    -- Soft Delete
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),

    -- Optimistic Locking
    version BIGINT DEFAULT 0
);

COMMENT ON TABLE properties IS 'Property/hotel information table';
COMMENT ON COLUMN properties.tenant_id IS 'Multi-tenancy: Tenant owner of this property';
COMMENT ON COLUMN properties.property_code IS 'Unique property identifier code';
COMMENT ON COLUMN properties.property_type IS 'Type of property (HOTEL, RESORT, MOTEL, etc.)';
COMMENT ON COLUMN properties.total_rooms IS 'Total number of rooms in property';
COMMENT ON COLUMN properties.amenities IS 'Property amenities (JSONB array)';
COMMENT ON COLUMN properties.config IS 'Property-specific configuration (JSONB)';

-- =====================================================
-- GUEST MANAGEMENT TABLES
-- =====================================================

-- Guests Table
CREATE TABLE guests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,

    -- Personal Information
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),

    -- Additional Contact
    alternate_email VARCHAR(255),
    alternate_phone VARCHAR(20),

    -- Address
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country CHAR(2), -- ISO 3166-1 alpha-2

    -- Profile Details
    date_of_birth DATE,
    nationality VARCHAR(3), -- ISO 3166-1 alpha-3
    passport_number VARCHAR(50),
    id_type VARCHAR(50),
    id_number VARCHAR(100),

    -- Preferences (stored as JSONB)
    preferences JSONB DEFAULT '{
        "roomType": null,
        "bedType": null,
        "smokingPreference": "NON_SMOKING",
        "floorPreference": null,
        "specialRequests": []
    }'::jsonb,

    -- Guest Type & Status
    guest_type VARCHAR(50) DEFAULT 'INDIVIDUAL',
    vip_status BOOLEAN DEFAULT false,
    blacklisted BOOLEAN DEFAULT false,

    -- Loyalty Program
    loyalty_tier VARCHAR(50),
    loyalty_points INTEGER DEFAULT 0,

    -- Notes
    notes TEXT,

    -- Metadata
    metadata JSONB DEFAULT '{}'::jsonb,

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    -- Soft Delete
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),

    -- Optimistic Locking
    version BIGINT DEFAULT 0
);

COMMENT ON TABLE guests IS 'Guest profiles and information';
COMMENT ON COLUMN guests.tenant_id IS 'Multi-tenancy: Tenant owner of this guest record';
COMMENT ON COLUMN guests.email IS 'Primary email address (unique)';
COMMENT ON COLUMN guests.preferences IS 'Guest preferences (JSONB)';
COMMENT ON COLUMN guests.guest_type IS 'Type of guest (INDIVIDUAL, CORPORATE, GROUP, etc.)';
COMMENT ON COLUMN guests.vip_status IS 'VIP guest flag';
COMMENT ON COLUMN guests.loyalty_points IS 'Accumulated loyalty points';

-- =====================================================
-- USER MANAGEMENT TABLES
-- =====================================================

-- Users Table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- Basic Information
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,

    -- Personal Information
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),

    -- Profile
    avatar_url VARCHAR(500),
    title VARCHAR(100),
    department VARCHAR(100),

    -- Status
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_verified BOOLEAN NOT NULL DEFAULT false,
    email_verified BOOLEAN NOT NULL DEFAULT false,

    -- Security
    two_factor_enabled BOOLEAN NOT NULL DEFAULT false,
    last_login_at TIMESTAMP,
    last_login_ip VARCHAR(45),
    failed_login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP,
    password_changed_at TIMESTAMP,

    -- Preferences
    preferences JSONB DEFAULT '{
        "language": "en",
        "timezone": "UTC",
        "theme": "light",
        "notifications": {
            "email": true,
            "sms": false,
            "push": true
        }
    }'::jsonb,

    -- Metadata
    metadata JSONB DEFAULT '{}'::jsonb,

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    -- Soft Delete
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),

    -- Optimistic Locking
    version BIGINT DEFAULT 0
);

COMMENT ON TABLE users IS 'System users and authentication';
COMMENT ON COLUMN users.username IS 'Unique username for login';
COMMENT ON COLUMN users.email IS 'Unique email address';
COMMENT ON COLUMN users.password_hash IS 'Bcrypt hashed password';
COMMENT ON COLUMN users.two_factor_enabled IS '2FA enabled flag';
COMMENT ON COLUMN users.failed_login_attempts IS 'Failed login attempt counter';
COMMENT ON COLUMN users.locked_until IS 'Account lock expiration time';
COMMENT ON COLUMN users.preferences IS 'User preferences (JSONB)';

-- =====================================================
-- RATE MANAGEMENT TABLES
-- =====================================================

-- Rates Table
CREATE TABLE rates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    property_id UUID NOT NULL,
    room_type_id UUID NOT NULL,
    rate_code VARCHAR(50) NOT NULL,
    rate_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    rate_strategy rate_strategy NOT NULL,
    rate_status rate_status NOT NULL,
    season_type season_type,
    base_rate NUMERIC(10, 2) NOT NULL,
    current_rate NUMERIC(10, 2) NOT NULL,
    minimum_rate NUMERIC(10, 2),
    maximum_rate NUMERIC(10, 2),
    currency CHAR(3) NOT NULL DEFAULT 'USD',
    effective_date DATE NOT NULL,
    expiry_date DATE,
    minimum_stay INTEGER,
    maximum_stay INTEGER,
    advance_booking_days INTEGER,
    maximum_booking_days INTEGER,
    is_refundable BOOLEAN NOT NULL DEFAULT true,
    is_modifiable BOOLEAN NOT NULL DEFAULT true,
    cancellation_hours INTEGER,
    tax_inclusive BOOLEAN NOT NULL DEFAULT false,
    service_fee_inclusive BOOLEAN NOT NULL DEFAULT false,
    occupancy_multiplier NUMERIC(5, 2),
    demand_multiplier NUMERIC(5, 2),
    competitive_adjustment NUMERIC(5, 2),
    priority_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

COMMENT ON TABLE rates IS 'Rate management table storing pricing information';
COMMENT ON COLUMN rates.tenant_id IS 'Multi-tenancy: Tenant owner of this rate';
COMMENT ON COLUMN rates.property_id IS 'Reference to property';
COMMENT ON COLUMN rates.room_type_id IS 'Reference to room type';
COMMENT ON COLUMN rates.rate_code IS 'Unique rate identifier code';
COMMENT ON COLUMN rates.rate_strategy IS 'Pricing strategy applied';
COMMENT ON COLUMN rates.rate_status IS 'Current rate status';
COMMENT ON COLUMN rates.season_type IS 'Seasonal pricing period';
COMMENT ON COLUMN rates.base_rate IS 'Original base rate';
COMMENT ON COLUMN rates.current_rate IS 'Currently active rate after adjustments';
COMMENT ON COLUMN rates.occupancy_multiplier IS 'Occupancy-based pricing multiplier';
COMMENT ON COLUMN rates.demand_multiplier IS 'Demand-based pricing multiplier';
COMMENT ON COLUMN rates.competitive_adjustment IS 'Market competition adjustment';
COMMENT ON COLUMN rates.priority_order IS 'Display/selection priority';
COMMENT ON COLUMN rates.version IS 'Optimistic locking version';

-- =====================================================
-- RESERVATION ENGINE TABLES
-- =====================================================

-- Reservations Table
CREATE TABLE reservations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    confirmation_number VARCHAR(20) UNIQUE NOT NULL,
    property_id UUID NOT NULL,
    guest_id UUID,
    guest_first_name VARCHAR(100) NOT NULL,
    guest_last_name VARCHAR(100) NOT NULL,
    guest_email VARCHAR(255) NOT NULL,
    guest_phone VARCHAR(20),
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    nights INTEGER NOT NULL,
    room_type_id UUID,
    room_number VARCHAR(10),
    adults INTEGER NOT NULL CHECK (adults > 0),
    children INTEGER DEFAULT 0,
    infants INTEGER DEFAULT 0,
    room_rate NUMERIC(10, 2) NOT NULL,
    taxes NUMERIC(10, 2),
    fees NUMERIC(10, 2),
    total_amount NUMERIC(10, 2) NOT NULL,
    currency CHAR(3) DEFAULT 'USD',
    status reservation_status NOT NULL DEFAULT 'PENDING',
    source reservation_source NOT NULL,
    special_requests TEXT,
    internal_notes TEXT,
    booking_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    arrival_time VARCHAR(5),
    departure_time VARCHAR(5),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

COMMENT ON TABLE reservations IS 'Reservation bookings table';
COMMENT ON COLUMN reservations.tenant_id IS 'Multi-tenancy: Tenant owner of this reservation';
COMMENT ON COLUMN reservations.confirmation_number IS 'Unique booking confirmation number';
COMMENT ON COLUMN reservations.property_id IS 'Reference to property';
COMMENT ON COLUMN reservations.guest_id IS 'Reference to guest profile (if registered)';
COMMENT ON COLUMN reservations.nights IS 'Number of nights for stay';
COMMENT ON COLUMN reservations.status IS 'Current reservation status';
COMMENT ON COLUMN reservations.source IS 'Booking channel/source';
COMMENT ON COLUMN reservations.special_requests IS 'Guest special requests';
COMMENT ON COLUMN reservations.internal_notes IS 'Internal staff notes';

-- Reservation Status History Table
CREATE TABLE reservation_status_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    reservation_id UUID NOT NULL,
    old_status reservation_status,
    new_status reservation_status,
    reason TEXT,
    notes TEXT,
    changed_by UUID,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE reservation_status_history IS 'Audit trail for reservation status changes';
COMMENT ON COLUMN reservation_status_history.tenant_id IS 'Multi-tenancy: Inherited from reservation';
COMMENT ON COLUMN reservation_status_history.reservation_id IS 'Reference to reservation';
COMMENT ON COLUMN reservation_status_history.old_status IS 'Previous status';
COMMENT ON COLUMN reservation_status_history.new_status IS 'New status';
COMMENT ON COLUMN reservation_status_history.changed_by IS 'User who made the change';

-- =====================================================
-- PAYMENT PROCESSOR TABLES
-- =====================================================

-- Payments Table
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    payment_reference VARCHAR(100) UNIQUE NOT NULL,
    reservation_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    amount NUMERIC(10, 2) NOT NULL CHECK (amount >= 0.01),
    currency CHAR(3) NOT NULL,
    processing_fee NUMERIC(10, 2) CHECK (processing_fee >= 0),
    payment_method payment_method NOT NULL,
    transaction_type transaction_type NOT NULL,
    status payment_status NOT NULL DEFAULT 'PENDING',
    gateway_provider VARCHAR(50),
    gateway_transaction_id VARCHAR(100),
    authorization_code VARCHAR(50),
    card_last_four CHAR(4),
    card_brand VARCHAR(20),
    billing_name VARCHAR(100),
    billing_email VARCHAR(100),
    billing_address VARCHAR(255),
    description VARCHAR(500),
    failure_reason VARCHAR(255),
    refunded_amount NUMERIC(10, 2) DEFAULT 0.00,
    authorized_at TIMESTAMP,
    captured_at TIMESTAMP,
    settled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

COMMENT ON TABLE payments IS 'Payment transactions table';
COMMENT ON COLUMN payments.tenant_id IS 'Multi-tenancy: Critical for financial isolation';
COMMENT ON COLUMN payments.payment_reference IS 'Unique payment reference number';
COMMENT ON COLUMN payments.reservation_id IS 'Reference to reservation';
COMMENT ON COLUMN payments.customer_id IS 'Reference to customer';
COMMENT ON COLUMN payments.payment_method IS 'Method of payment';
COMMENT ON COLUMN payments.transaction_type IS 'Type of transaction';
COMMENT ON COLUMN payments.status IS 'Payment processing status';
COMMENT ON COLUMN payments.gateway_provider IS 'Payment gateway provider name';
COMMENT ON COLUMN payments.gateway_transaction_id IS 'External gateway transaction ID';
COMMENT ON COLUMN payments.card_last_four IS 'Last 4 digits of card (PCI compliant)';
COMMENT ON COLUMN payments.refunded_amount IS 'Total amount refunded';

-- =====================================================
-- AVAILABILITY CALCULATOR TABLES
-- =====================================================

-- Create availability schema
CREATE SCHEMA IF NOT EXISTS availability;

-- Room Availability Table
CREATE TABLE availability.room_availability (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    property_id UUID NOT NULL,
    room_type_id UUID NOT NULL,
    room_number VARCHAR(20),
    room_category room_category NOT NULL,
    availability_date DATE NOT NULL,
    availability_status availability_status NOT NULL DEFAULT 'AVAILABLE',
    base_rate NUMERIC(10, 2) CHECK (base_rate >= 0),
    current_rate NUMERIC(10, 2) CHECK (current_rate >= 0),
    min_rate NUMERIC(10, 2) CHECK (min_rate >= 0),
    max_rate NUMERIC(10, 2) CHECK (max_rate >= 0),
    total_rooms INTEGER NOT NULL CHECK (total_rooms >= 1 AND total_rooms <= 1000),
    available_rooms INTEGER NOT NULL CHECK (available_rooms >= 0),
    occupied_rooms INTEGER NOT NULL DEFAULT 0,
    maintenance_rooms INTEGER NOT NULL DEFAULT 0,
    blocked_rooms INTEGER NOT NULL DEFAULT 0,
    minimum_stay INTEGER NOT NULL DEFAULT 1,
    maximum_stay INTEGER,
    closed_to_arrival BOOLEAN NOT NULL DEFAULT false,
    closed_to_departure BOOLEAN NOT NULL DEFAULT false,
    stop_sell BOOLEAN NOT NULL DEFAULT false,
    currency CHAR(3) NOT NULL DEFAULT 'USD',
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

COMMENT ON SCHEMA availability IS 'Schema for availability management';
COMMENT ON TABLE availability.room_availability IS 'Daily room availability and pricing';
COMMENT ON COLUMN availability.room_availability.tenant_id IS 'Multi-tenancy: Tenant owner of availability data';
COMMENT ON COLUMN availability.room_availability.property_id IS 'Reference to property';
COMMENT ON COLUMN availability.room_availability.room_type_id IS 'Reference to room type';
COMMENT ON COLUMN availability.room_availability.room_number IS 'Specific room number (optional)';
COMMENT ON COLUMN availability.room_availability.availability_date IS 'Date for availability';
COMMENT ON COLUMN availability.room_availability.availability_status IS 'Current availability status';
COMMENT ON COLUMN availability.room_availability.total_rooms IS 'Total room inventory';
COMMENT ON COLUMN availability.room_availability.available_rooms IS 'Currently available rooms';
COMMENT ON COLUMN availability.room_availability.closed_to_arrival IS 'CTA restriction flag';
COMMENT ON COLUMN availability.room_availability.closed_to_departure IS 'CTD restriction flag';
COMMENT ON COLUMN availability.room_availability.stop_sell IS 'Stop sell flag';

-- =====================================================
-- ANALYTICS ENGINE TABLES
-- =====================================================

-- Analytics Metrics Table
CREATE TABLE analytics_metrics (
    metric_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    metric_type metric_type NOT NULL,
    property_id UUID,
    room_type_id UUID,
    rate_plan_id UUID,
    channel_id UUID,
    user_id UUID,
    time_granularity time_granularity NOT NULL,
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL,
    metric_value NUMERIC(19, 4),
    count_value BIGINT,
    percentage_value NUMERIC(5, 2),
    currency_code CHAR(3),
    formatted_value VARCHAR(255),
    status analytics_status NOT NULL DEFAULT 'PENDING',
    calculated_at TIMESTAMP,
    expires_at TIMESTAMP,
    calculation_duration_ms BIGINT,
    data_points_count INTEGER CHECK (data_points_count >= 0),
    confidence_score NUMERIC(3, 2) CHECK (confidence_score >= 0 AND confidence_score <= 1),
    baseline_value NUMERIC(19, 4),
    target_value NUMERIC(19, 4),
    variance_percentage NUMERIC(5, 2),
    trend_direction VARCHAR(10) CHECK (trend_direction IN ('UP', 'DOWN', 'STABLE', 'UNKNOWN')),
    seasonality_factor NUMERIC(5, 4),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

COMMENT ON TABLE analytics_metrics IS 'Analytics metrics and KPIs';
COMMENT ON COLUMN analytics_metrics.tenant_id IS 'Multi-tenancy: Tenant owner of analytics data';
COMMENT ON COLUMN analytics_metrics.metric_type IS 'Type of metric being tracked';
COMMENT ON COLUMN analytics_metrics.time_granularity IS 'Time period granularity';
COMMENT ON COLUMN analytics_metrics.period_start IS 'Metric calculation period start';
COMMENT ON COLUMN analytics_metrics.period_end IS 'Metric calculation period end';
COMMENT ON COLUMN analytics_metrics.metric_value IS 'Calculated metric value';
COMMENT ON COLUMN analytics_metrics.confidence_score IS 'Statistical confidence (0-1)';
COMMENT ON COLUMN analytics_metrics.trend_direction IS 'Metric trend direction';

-- Analytics Metric Dimensions Table (for @ElementCollection)
CREATE TABLE analytics_metric_dimensions (
    metric_id UUID NOT NULL,
    dimension_key VARCHAR(50) NOT NULL,
    dimension_value VARCHAR(255),
    PRIMARY KEY (metric_id, dimension_key)
);

COMMENT ON TABLE analytics_metric_dimensions IS 'Key-value dimensions for metrics';

-- Analytics Reports Table
CREATE TABLE analytics_reports (
    report_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    report_name VARCHAR(200) NOT NULL,
    report_type VARCHAR(50) NOT NULL CHECK (
        report_type IN ('DASHBOARD', 'EXECUTIVE', 'OPERATIONAL', 'FINANCIAL',
                       'OCCUPANCY', 'REVENUE', 'CUSTOMER', 'CUSTOM', 'SCHEDULED')
    ),
    report_description TEXT,
    property_id UUID,
    time_granularity time_granularity NOT NULL,
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL,
    status analytics_status NOT NULL DEFAULT 'PENDING',
    generation_started_at TIMESTAMP,
    generation_completed_at TIMESTAMP,
    scheduled_at TIMESTAMP,
    created_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

COMMENT ON TABLE analytics_reports IS 'Generated analytics reports';
COMMENT ON COLUMN analytics_reports.tenant_id IS 'Multi-tenancy: Tenant owner of report';
COMMENT ON COLUMN analytics_reports.report_type IS 'Type of report';
COMMENT ON COLUMN analytics_reports.status IS 'Report generation status';

-- Report Property IDs Table (for @ElementCollection)
CREATE TABLE report_property_ids (
    report_id UUID NOT NULL,
    property_id UUID NOT NULL,
    PRIMARY KEY (report_id, property_id)
);

COMMENT ON TABLE report_property_ids IS 'Multi-property associations for reports';
