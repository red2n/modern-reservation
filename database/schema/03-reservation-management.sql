-- Reservation Management Tables
-- Tables for managing reservations, bookings, and related data

-- Main reservations table (partitioned by check_in_date)
CREATE TABLE reservations (
    id UUID NOT NULL,
    confirmation_number VARCHAR(12) NOT NULL,
    property_id UUID NOT NULL,
    room_id UUID NOT NULL,
    guest_id UUID NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    adults INTEGER NOT NULL CHECK (adults > 0),
    children INTEGER NOT NULL DEFAULT 0 CHECK (children >= 0),
    total_amount DECIMAL(10,2) NOT NULL CHECK (total_amount >= 0),
    currency CHAR(3) NOT NULL DEFAULT 'USD',
    status reservation_status NOT NULL DEFAULT 'pending',
    source reservation_source NOT NULL DEFAULT 'direct',
    rate_code VARCHAR(50),
    rate_plan_id UUID,
    special_requests TEXT,
    guest_notes TEXT,
    internal_notes TEXT,
    corporate_code VARCHAR(50),
    group_code VARCHAR(50),
    market_segment VARCHAR(50),
    additional_guests JSONB NOT NULL DEFAULT '[]',

    -- Check-in/out details
    actual_check_in_time TIMESTAMP WITH TIME ZONE,
    actual_check_out_time TIMESTAMP WITH TIME ZONE,
    room_keys JSONB,
    deposit_amount DECIMAL(10,2) DEFAULT 0.00,
    identification_verified BOOLEAN NOT NULL DEFAULT false,
    signature_obtained BOOLEAN NOT NULL DEFAULT false,
    special_instructions TEXT,

    -- Final billing
    final_bill DECIMAL(10,2),
    outstanding_charges DECIMAL(10,2) DEFAULT 0.00,
    room_condition room_condition,
    guest_satisfaction_score INTEGER CHECK (guest_satisfaction_score BETWEEN 1 AND 5),
    feedback TEXT,

    -- Cancellation details
    cancelled_at TIMESTAMP WITH TIME ZONE,
    cancellation_reason TEXT,
    cancellation_fee DECIMAL(10,2) DEFAULT 0.00,
    refund_amount DECIMAL(10,2) DEFAULT 0.00,

    -- Metadata
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID NOT NULL,

    PRIMARY KEY (id, check_in_date),
    FOREIGN KEY (property_id) REFERENCES properties(id),
    FOREIGN KEY (room_id) REFERENCES rooms(id),
    FOREIGN KEY (guest_id) REFERENCES guests(id)
) PARTITION BY RANGE (check_in_date);

-- Create initial partitions for current and future months
SELECT create_monthly_partition('reservations', DATE_TRUNC('month', CURRENT_DATE)::DATE);
SELECT create_monthly_partition('reservations', DATE_TRUNC('month', CURRENT_DATE + INTERVAL '1 month')::DATE);
SELECT create_monthly_partition('reservations', DATE_TRUNC('month', CURRENT_DATE + INTERVAL '2 months')::DATE);

-- Reservation status history for tracking changes
CREATE TABLE reservation_status_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    reservation_id UUID NOT NULL,
    old_status reservation_status,
    new_status reservation_status NOT NULL,
    reason TEXT,
    notes TEXT,
    changed_by UUID NOT NULL,
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Reservation modifications/amendments
CREATE TABLE reservation_modifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    reservation_id UUID NOT NULL,
    modification_type VARCHAR(50) NOT NULL, -- date_change, room_change, guest_change, etc.
    old_values JSONB NOT NULL,
    new_values JSONB NOT NULL,
    price_difference DECIMAL(10,2) DEFAULT 0.00,
    reason TEXT,
    notes TEXT,
    approved_by UUID,
    applied_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL
);

-- Reservation additional services/charges
CREATE TABLE reservation_charges (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    reservation_id UUID NOT NULL,
    charge_type VARCHAR(50) NOT NULL, -- room, tax, service, amenity, penalty, etc.
    description VARCHAR(255) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'USD',
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    tax_rate DECIMAL(5,4) DEFAULT 0.0000,
    tax_amount DECIMAL(10,2) DEFAULT 0.00,
    is_inclusive BOOLEAN NOT NULL DEFAULT false,
    charge_date DATE NOT NULL,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL
);

-- Reservation room assignments (for handling room changes)
CREATE TABLE reservation_room_assignments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    reservation_id UUID NOT NULL,
    room_id UUID NOT NULL,
    assigned_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    unassigned_at TIMESTAMP WITH TIME ZONE,
    is_current BOOLEAN NOT NULL DEFAULT true,
    reason TEXT,
    assigned_by UUID NOT NULL,
    FOREIGN KEY (room_id) REFERENCES rooms(id)
);

-- Guest reviews and ratings
CREATE TABLE reviews (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    reservation_id UUID NOT NULL,
    guest_id UUID NOT NULL,
    property_id UUID NOT NULL,
    overall_rating INTEGER NOT NULL CHECK (overall_rating BETWEEN 1 AND 5),
    cleanliness_rating INTEGER CHECK (cleanliness_rating BETWEEN 1 AND 5),
    service_rating INTEGER CHECK (service_rating BETWEEN 1 AND 5),
    location_rating INTEGER CHECK (location_rating BETWEEN 1 AND 5),
    value_rating INTEGER CHECK (value_rating BETWEEN 1 AND 5),
    amenities_rating INTEGER CHECK (amenities_rating BETWEEN 1 AND 5),
    title VARCHAR(255),
    comment TEXT,
    is_verified BOOLEAN NOT NULL DEFAULT false,
    is_published BOOLEAN NOT NULL DEFAULT false,
    response TEXT,
    responded_by UUID,
    responded_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (guest_id) REFERENCES guests(id),
    FOREIGN KEY (property_id) REFERENCES properties(id)
);

-- Indexes for reservations (will be created on each partition)
CREATE INDEX idx_reservations_confirmation_number ON reservations(confirmation_number);
CREATE INDEX idx_reservations_property_id ON reservations(property_id);
CREATE INDEX idx_reservations_room_id ON reservations(room_id);
CREATE INDEX idx_reservations_guest_id ON reservations(guest_id);
CREATE INDEX idx_reservations_status ON reservations(status);
CREATE INDEX idx_reservations_source ON reservations(source);
CREATE INDEX idx_reservations_check_out_date ON reservations(check_out_date);
CREATE INDEX idx_reservations_created_at ON reservations(created_at);
CREATE INDEX idx_reservations_is_deleted ON reservations(is_deleted) WHERE is_deleted = false;
CREATE INDEX idx_reservations_rate_plan_id ON reservations(rate_plan_id) WHERE rate_plan_id IS NOT NULL;

-- Indexes for reservation status history
CREATE INDEX idx_reservation_status_history_reservation_id ON reservation_status_history(reservation_id);
CREATE INDEX idx_reservation_status_history_changed_at ON reservation_status_history(changed_at);
CREATE INDEX idx_reservation_status_history_new_status ON reservation_status_history(new_status);

-- Indexes for reservation modifications
CREATE INDEX idx_reservation_modifications_reservation_id ON reservation_modifications(reservation_id);
CREATE INDEX idx_reservation_modifications_type ON reservation_modifications(modification_type);
CREATE INDEX idx_reservation_modifications_created_at ON reservation_modifications(created_at);

-- Indexes for reservation charges
CREATE INDEX idx_reservation_charges_reservation_id ON reservation_charges(reservation_id);
CREATE INDEX idx_reservation_charges_type ON reservation_charges(charge_type);
CREATE INDEX idx_reservation_charges_date ON reservation_charges(charge_date);

-- Indexes for room assignments
CREATE INDEX idx_reservation_room_assignments_reservation_id ON reservation_room_assignments(reservation_id);
CREATE INDEX idx_reservation_room_assignments_room_id ON reservation_room_assignments(room_id);
CREATE INDEX idx_reservation_room_assignments_current ON reservation_room_assignments(is_current) WHERE is_current = true;

-- Indexes for reviews
CREATE INDEX idx_reviews_reservation_id ON reviews(reservation_id);
CREATE INDEX idx_reviews_guest_id ON reviews(guest_id);
CREATE INDEX idx_reviews_property_id ON reviews(property_id);
CREATE INDEX idx_reviews_rating ON reviews(overall_rating);
CREATE INDEX idx_reviews_published ON reviews(is_published) WHERE is_published = true;
CREATE INDEX idx_reviews_created_at ON reviews(created_at);

-- Unique constraints (must include partitioning column for partitioned tables)
ALTER TABLE reservations ADD CONSTRAINT uq_reservations_confirmation_number UNIQUE(confirmation_number, check_in_date);

-- Check constraints
ALTER TABLE reservations ADD CONSTRAINT chk_reservations_check_dates CHECK (check_out_date > check_in_date);
ALTER TABLE reservations ADD CONSTRAINT chk_reservations_total_guests CHECK (adults + children <= 10);
ALTER TABLE reservation_charges ADD CONSTRAINT chk_reservation_charges_amount CHECK (amount >= 0);

-- Triggers for updated_at
CREATE TRIGGER trigger_reservations_updated_at
    BEFORE UPDATE ON reservations
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_reviews_updated_at
    BEFORE UPDATE ON reviews
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Function to automatically create partitions
CREATE OR REPLACE FUNCTION create_reservation_partition_if_not_exists()
RETURNS TRIGGER AS $$
DECLARE
    partition_date DATE;
BEGIN
    partition_date := DATE_TRUNC('month', NEW.check_in_date)::DATE;

    -- Try to create partition, ignore if already exists
    BEGIN
        PERFORM create_monthly_partition('reservations', partition_date);
    EXCEPTION
        WHEN duplicate_table THEN
            -- Partition already exists, continue
            NULL;
    END;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to auto-create partitions
CREATE TRIGGER trigger_reservation_partition_creation
    BEFORE INSERT ON reservations
    FOR EACH ROW
    EXECUTE FUNCTION create_reservation_partition_if_not_exists();
