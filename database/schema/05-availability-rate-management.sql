-- Availability and Rate Management Tables
-- Tables for managing room availability, pricing, and rate plans

-- Rate plans
CREATE TABLE rate_plans (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    property_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    rate_plan_code VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,

    -- Booking rules
    cancellation_policy TEXT NOT NULL,
    advance_booking_days INTEGER CHECK (advance_booking_days >= 0),
    min_stay INTEGER CHECK (min_stay > 0),
    max_stay INTEGER CHECK (max_stay > 0),
    blackout_dates JSONB NOT NULL DEFAULT '[]',

    -- Pricing rules
    discount_percent DECIMAL(5,2) NOT NULL DEFAULT 0.00 CHECK (discount_percent >= 0 AND discount_percent <= 100),
    is_refundable BOOLEAN NOT NULL DEFAULT true,
    requires_deposit BOOLEAN NOT NULL DEFAULT false,
    deposit_percent DECIMAL(5,2) DEFAULT 0.00,

    -- Availability rules
    applies_to_room_types JSONB NOT NULL DEFAULT '[]', -- empty means all room types
    channel_restrictions JSONB NOT NULL DEFAULT '{}',

    -- Metadata
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID NOT NULL,

    FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE
);

-- Room availability (daily inventory)
CREATE TABLE room_availability (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    room_id UUID NOT NULL,
    date DATE NOT NULL,
    is_available BOOLEAN NOT NULL DEFAULT true,
    available_units INTEGER NOT NULL DEFAULT 1 CHECK (available_units >= 0),
    min_stay INTEGER CHECK (min_stay > 0),
    max_stay INTEGER CHECK (max_stay > 0),
    closed_to_arrival BOOLEAN NOT NULL DEFAULT false,
    closed_to_departure BOOLEAN NOT NULL DEFAULT false,

    -- Pricing
    base_price DECIMAL(10,2) CHECK (base_price >= 0),
    currency CHAR(3) NOT NULL DEFAULT 'USD',

    -- Metadata
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID NOT NULL,

    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    UNIQUE(room_id, date)
);

-- Rate plan pricing (daily rates for each rate plan)
CREATE TABLE rate_plan_rates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    rate_plan_id UUID NOT NULL,
    room_id UUID NOT NULL,
    date DATE NOT NULL,
    rate DECIMAL(10,2) NOT NULL CHECK (rate >= 0),
    currency CHAR(3) NOT NULL DEFAULT 'USD',

    -- Override restrictions for this specific date
    min_stay INTEGER CHECK (min_stay > 0),
    max_stay INTEGER CHECK (max_stay > 0),
    closed_to_arrival BOOLEAN NOT NULL DEFAULT false,
    closed_to_departure BOOLEAN NOT NULL DEFAULT false,

    -- Metadata
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID NOT NULL,

    FOREIGN KEY (rate_plan_id) REFERENCES rate_plans(id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    UNIQUE(rate_plan_id, room_id, date)
);

-- Seasonal pricing rules
CREATE TABLE seasonal_rates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    property_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,

    -- Pricing adjustments
    adjustment_type VARCHAR(20) NOT NULL CHECK (adjustment_type IN ('percentage', 'fixed_amount')),
    adjustment_value DECIMAL(10,2) NOT NULL,

    -- Applicability
    applies_to_room_types JSONB NOT NULL DEFAULT '[]',
    applies_to_rate_plans JSONB NOT NULL DEFAULT '[]',
    days_of_week JSONB NOT NULL DEFAULT '[1,2,3,4,5,6,7]', -- 1=Monday, 7=Sunday

    -- Rules
    min_nights INTEGER CHECK (min_nights > 0),
    max_nights INTEGER CHECK (max_nights > 0),
    priority INTEGER NOT NULL DEFAULT 1, -- Higher number = higher priority

    -- Status
    is_active BOOLEAN NOT NULL DEFAULT true,

    -- Metadata
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID NOT NULL,

    FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE
);

-- Promotional rates and discounts
CREATE TABLE promotions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    property_id UUID NOT NULL,
    promotion_code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,

    -- Validity period
    valid_from DATE NOT NULL,
    valid_to DATE NOT NULL,

    -- Discount details
    discount_type VARCHAR(20) NOT NULL CHECK (discount_type IN ('percentage', 'fixed_amount', 'free_nights')),
    discount_value DECIMAL(10,2) NOT NULL CHECK (discount_value > 0),
    max_discount_amount DECIMAL(10,2), -- Cap for percentage discounts

    -- Usage limits
    max_uses INTEGER, -- NULL = unlimited
    uses_per_guest INTEGER DEFAULT 1,
    current_uses INTEGER NOT NULL DEFAULT 0,

    -- Booking requirements
    min_nights INTEGER CHECK (min_nights > 0),
    advance_booking_days INTEGER CHECK (advance_booking_days >= 0),
    min_amount DECIMAL(10,2) CHECK (min_amount >= 0),

    -- Applicability
    applies_to_room_types JSONB NOT NULL DEFAULT '[]',
    applies_to_rate_plans JSONB NOT NULL DEFAULT '[]',
    blackout_dates JSONB NOT NULL DEFAULT '[]',

    -- Status
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_combinable BOOLEAN NOT NULL DEFAULT false,

    -- Metadata
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID NOT NULL,

    FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE,
    UNIQUE(property_id, promotion_code)
);

-- Promotion usage tracking
CREATE TABLE promotion_usage (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    promotion_id UUID NOT NULL,
    reservation_id UUID NOT NULL,
    guest_id UUID NOT NULL,
    discount_amount DECIMAL(10,2) NOT NULL CHECK (discount_amount >= 0),
    currency CHAR(3) NOT NULL DEFAULT 'USD',
    used_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (promotion_id) REFERENCES promotions(id) ON DELETE CASCADE,
    FOREIGN KEY (guest_id) REFERENCES guests(id) ON DELETE CASCADE
);

-- Channel-specific availability and rates
CREATE TABLE channel_availability (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    room_id UUID NOT NULL,
    channel_id VARCHAR(50) NOT NULL, -- booking.com, expedia, etc.
    date DATE NOT NULL,
    available_units INTEGER NOT NULL DEFAULT 0 CHECK (available_units >= 0),
    rate DECIMAL(10,2) CHECK (rate >= 0),
    currency CHAR(3) NOT NULL DEFAULT 'USD',

    -- Channel-specific restrictions
    min_stay INTEGER CHECK (min_stay > 0),
    max_stay INTEGER CHECK (max_stay > 0),
    closed_to_arrival BOOLEAN NOT NULL DEFAULT false,
    closed_to_departure BOOLEAN NOT NULL DEFAULT false,

    -- Sync status
    last_synced_at TIMESTAMP WITH TIME ZONE,
    sync_status VARCHAR(20) DEFAULT 'pending', -- pending, synced, failed

    -- Metadata
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    UNIQUE(room_id, channel_id, date)
);

-- Yield management rules
CREATE TABLE yield_rules (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    property_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,

    -- Conditions
    occupancy_threshold DECIMAL(5,2) CHECK (occupancy_threshold BETWEEN 0 AND 100),
    lead_time_days INTEGER CHECK (lead_time_days >= 0),
    length_of_stay INTEGER CHECK (length_of_stay > 0),
    day_of_week JSONB, -- Array of days 1-7
    season_name VARCHAR(100),

    -- Actions
    price_adjustment_type VARCHAR(20) NOT NULL CHECK (price_adjustment_type IN ('percentage', 'fixed_amount')),
    price_adjustment_value DECIMAL(10,2) NOT NULL,
    availability_adjustment INTEGER DEFAULT 0, -- +/- units

    -- Applicability
    applies_to_room_types JSONB NOT NULL DEFAULT '[]',
    applies_to_rate_plans JSONB NOT NULL DEFAULT '[]',

    -- Control
    is_active BOOLEAN NOT NULL DEFAULT true,
    priority INTEGER NOT NULL DEFAULT 1,

    -- Metadata
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID NOT NULL,

    FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE
);

-- Indexes for rate plans
CREATE INDEX idx_rate_plans_property_id ON rate_plans(property_id);
CREATE INDEX idx_rate_plans_code ON rate_plans(rate_plan_code);
CREATE INDEX idx_rate_plans_active ON rate_plans(is_active) WHERE is_active = true;
CREATE INDEX idx_rate_plans_deleted ON rate_plans(is_deleted) WHERE is_deleted = false;

-- Indexes for room availability
CREATE INDEX idx_room_availability_room_id ON room_availability(room_id);
CREATE INDEX idx_room_availability_date ON room_availability(date);
CREATE INDEX idx_room_availability_room_date ON room_availability(room_id, date);
CREATE INDEX idx_room_availability_available ON room_availability(is_available) WHERE is_available = true;

-- Indexes for rate plan rates
CREATE INDEX idx_rate_plan_rates_rate_plan_id ON rate_plan_rates(rate_plan_id);
CREATE INDEX idx_rate_plan_rates_room_id ON rate_plan_rates(room_id);
CREATE INDEX idx_rate_plan_rates_date ON rate_plan_rates(date);
CREATE INDEX idx_rate_plan_rates_room_date ON rate_plan_rates(room_id, date);

-- Indexes for seasonal rates
CREATE INDEX idx_seasonal_rates_property_id ON seasonal_rates(property_id);
CREATE INDEX idx_seasonal_rates_dates ON seasonal_rates(start_date, end_date);
CREATE INDEX idx_seasonal_rates_active ON seasonal_rates(is_active) WHERE is_active = true;
CREATE INDEX idx_seasonal_rates_priority ON seasonal_rates(priority DESC);

-- Indexes for promotions
CREATE INDEX idx_promotions_property_id ON promotions(property_id);
CREATE INDEX idx_promotions_code ON promotions(promotion_code);
CREATE INDEX idx_promotions_dates ON promotions(valid_from, valid_to);
CREATE INDEX idx_promotions_active ON promotions(is_active) WHERE is_active = true;

-- Indexes for promotion usage
CREATE INDEX idx_promotion_usage_promotion_id ON promotion_usage(promotion_id);
CREATE INDEX idx_promotion_usage_reservation_id ON promotion_usage(reservation_id);
CREATE INDEX idx_promotion_usage_guest_id ON promotion_usage(guest_id);
CREATE INDEX idx_promotion_usage_used_at ON promotion_usage(used_at);

-- Indexes for channel availability
CREATE INDEX idx_channel_availability_room_id ON channel_availability(room_id);
CREATE INDEX idx_channel_availability_channel_id ON channel_availability(channel_id);
CREATE INDEX idx_channel_availability_date ON channel_availability(date);
CREATE INDEX idx_channel_availability_sync_status ON channel_availability(sync_status);

-- Indexes for yield rules
CREATE INDEX idx_yield_rules_property_id ON yield_rules(property_id);
CREATE INDEX idx_yield_rules_active ON yield_rules(is_active) WHERE is_active = true;
CREATE INDEX idx_yield_rules_priority ON yield_rules(priority DESC);

-- Check constraints
ALTER TABLE rate_plans ADD CONSTRAINT chk_rate_plans_min_max_stay CHECK (max_stay IS NULL OR min_stay IS NULL OR max_stay >= min_stay);
ALTER TABLE seasonal_rates ADD CONSTRAINT chk_seasonal_rates_dates CHECK (end_date >= start_date);
ALTER TABLE seasonal_rates ADD CONSTRAINT chk_seasonal_rates_nights CHECK (max_nights IS NULL OR min_nights IS NULL OR max_nights >= min_nights);
ALTER TABLE promotions ADD CONSTRAINT chk_promotions_dates CHECK (valid_to >= valid_from);
ALTER TABLE promotions ADD CONSTRAINT chk_promotions_uses CHECK (max_uses IS NULL OR max_uses > 0);

-- Triggers for updated_at
CREATE TRIGGER trigger_rate_plans_updated_at
    BEFORE UPDATE ON rate_plans
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_room_availability_updated_at
    BEFORE UPDATE ON room_availability
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_rate_plan_rates_updated_at
    BEFORE UPDATE ON rate_plan_rates
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_seasonal_rates_updated_at
    BEFORE UPDATE ON seasonal_rates
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_promotions_updated_at
    BEFORE UPDATE ON promotions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_channel_availability_updated_at
    BEFORE UPDATE ON channel_availability
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_yield_rules_updated_at
    BEFORE UPDATE ON yield_rules
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
