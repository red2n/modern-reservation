-- Guest Management Tables
-- Tables for managing guest profiles, preferences, and related data

-- Guests table
CREATE TABLE guests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    email VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    date_of_birth DATE,
    nationality VARCHAR(50),
    passport_number VARCHAR(50),
    loyalty_number VARCHAR(50),
    preferences JSONB NOT NULL DEFAULT '{}',
    emergency_contact JSONB,
    address JSONB,
    total_stays INTEGER NOT NULL DEFAULT 0,
    total_spent DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    average_rating DECIMAL(3,2),
    is_vip BOOLEAN NOT NULL DEFAULT false,
    marketing_consent BOOLEAN NOT NULL DEFAULT false,
    communication_preferences JSONB NOT NULL DEFAULT '{}',
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Guest addresses table (separate for normalization)
CREATE TABLE guest_addresses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    guest_id UUID NOT NULL,
    address_type VARCHAR(20) NOT NULL DEFAULT 'primary', -- primary, billing, emergency
    street TEXT NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    country VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20),
    is_default BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (guest_id) REFERENCES guests(id) ON DELETE CASCADE
);

-- Guest loyalty programs
CREATE TABLE loyalty_programs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    tiers JSONB NOT NULL, -- tier definitions and benefits
    points_per_dollar DECIMAL(5,2) NOT NULL DEFAULT 1.00,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID NOT NULL
);

-- Guest loyalty points and tiers
CREATE TABLE guest_loyalty (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    guest_id UUID NOT NULL,
    loyalty_program_id UUID NOT NULL,
    loyalty_number VARCHAR(50) NOT NULL,
    current_tier VARCHAR(50) NOT NULL DEFAULT 'bronze',
    total_points INTEGER NOT NULL DEFAULT 0,
    available_points INTEGER NOT NULL DEFAULT 0,
    tier_progress INTEGER NOT NULL DEFAULT 0,
    tier_expiry_date DATE,
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_activity_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (guest_id) REFERENCES guests(id) ON DELETE CASCADE,
    FOREIGN KEY (loyalty_program_id) REFERENCES loyalty_programs(id) ON DELETE CASCADE,
    UNIQUE(guest_id, loyalty_program_id)
);

-- Guest preferences categories
CREATE TABLE preference_categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    category_type VARCHAR(50) NOT NULL, -- room, service, dietary, etc.
    options JSONB NOT NULL, -- available options for this category
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Guest specific preferences
CREATE TABLE guest_preferences (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    guest_id UUID NOT NULL,
    category_id UUID NOT NULL,
    preference_value VARCHAR(255) NOT NULL,
    notes TEXT,
    priority INTEGER NOT NULL DEFAULT 1, -- 1=high, 2=medium, 3=low
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (guest_id) REFERENCES guests(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES preference_categories(id) ON DELETE CASCADE,
    UNIQUE(guest_id, category_id, preference_value)
);

-- Guest communication log
CREATE TABLE guest_communications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    guest_id UUID NOT NULL,
    communication_type VARCHAR(50) NOT NULL, -- email, sms, call, in_person
    subject VARCHAR(255),
    content TEXT,
    direction VARCHAR(20) NOT NULL, -- inbound, outbound
    status VARCHAR(20) NOT NULL DEFAULT 'sent', -- sent, delivered, opened, failed
    sent_by UUID,
    sent_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivered_at TIMESTAMP WITH TIME ZONE,
    opened_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (guest_id) REFERENCES guests(id) ON DELETE CASCADE
);

-- Indexes for guests
CREATE INDEX idx_guests_tenant_id ON guests(tenant_id);
CREATE INDEX idx_guests_email ON guests(email);
CREATE INDEX idx_guests_name ON guests(last_name, first_name);
CREATE INDEX idx_guests_phone ON guests(phone);
CREATE INDEX idx_guests_loyalty_number ON guests(loyalty_number);
CREATE INDEX idx_guests_is_vip ON guests(is_vip) WHERE is_vip = true;
CREATE INDEX idx_guests_total_stays ON guests(total_stays);
CREATE INDEX idx_guests_total_spent ON guests(total_spent);
CREATE INDEX idx_guests_is_deleted ON guests(is_deleted) WHERE is_deleted = false;
CREATE INDEX idx_guests_created_at ON guests(created_at);

-- Indexes for guest addresses
CREATE INDEX idx_guest_addresses_guest_id ON guest_addresses(guest_id);
CREATE INDEX idx_guest_addresses_country ON guest_addresses(country);
CREATE INDEX idx_guest_addresses_is_default ON guest_addresses(is_default) WHERE is_default = true;

-- Indexes for loyalty
CREATE INDEX idx_guest_loyalty_guest_id ON guest_loyalty(guest_id);
CREATE INDEX idx_guest_loyalty_program_id ON guest_loyalty(loyalty_program_id);
CREATE INDEX idx_guest_loyalty_number ON guest_loyalty(loyalty_number);
CREATE INDEX idx_guest_loyalty_tier ON guest_loyalty(current_tier);
CREATE INDEX idx_guest_loyalty_points ON guest_loyalty(available_points);

-- Indexes for preferences
CREATE INDEX idx_guest_preferences_guest_id ON guest_preferences(guest_id);
CREATE INDEX idx_guest_preferences_category_id ON guest_preferences(category_id);
CREATE INDEX idx_guest_preferences_priority ON guest_preferences(priority);

-- Indexes for communications
CREATE INDEX idx_guest_communications_guest_id ON guest_communications(guest_id);
CREATE INDEX idx_guest_communications_type ON guest_communications(communication_type);
CREATE INDEX idx_guest_communications_sent_at ON guest_communications(sent_at);
CREATE INDEX idx_guest_communications_status ON guest_communications(status);

-- Unique constraints
ALTER TABLE guests ADD CONSTRAINT uq_guests_tenant_email UNIQUE(tenant_id, email);
ALTER TABLE loyalty_programs ADD CONSTRAINT uq_loyalty_programs_tenant_name UNIQUE(tenant_id, name);

-- Check constraints
ALTER TABLE guest_loyalty ADD CONSTRAINT chk_guest_loyalty_points CHECK (available_points <= total_points);
ALTER TABLE guest_loyalty ADD CONSTRAINT chk_guest_loyalty_progress CHECK (tier_progress >= 0);

-- Triggers for updated_at
CREATE TRIGGER trigger_guests_updated_at
    BEFORE UPDATE ON guests
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_guest_addresses_updated_at
    BEFORE UPDATE ON guest_addresses
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_loyalty_programs_updated_at
    BEFORE UPDATE ON loyalty_programs
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_guest_preferences_updated_at
    BEFORE UPDATE ON guest_preferences
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
