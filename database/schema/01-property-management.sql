-- Property Management Tables
-- Tables for managing properties, rooms, and related data

-- Properties table
CREATE TABLE properties (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    property_type property_type NOT NULL,
    address TEXT NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    country VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20),
    timezone VARCHAR(50) NOT NULL DEFAULT 'UTC',
    phone VARCHAR(20),
    email VARCHAR(255),
    website VARCHAR(255),
    check_in_time TIME NOT NULL DEFAULT '15:00',
    check_out_time TIME NOT NULL DEFAULT '11:00',
    currency CHAR(3) NOT NULL DEFAULT 'USD',
    tax_rate DECIMAL(5,4) NOT NULL DEFAULT 0.0000,
    amenities JSONB NOT NULL DEFAULT '[]',
    policies JSONB NOT NULL DEFAULT '{}',
    images JSONB NOT NULL DEFAULT '[]',
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID NOT NULL
);

-- Rooms table
CREATE TABLE rooms (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    property_id UUID NOT NULL,
    room_number VARCHAR(20) NOT NULL,
    room_type VARCHAR(100) NOT NULL,
    floor INTEGER,
    capacity INTEGER NOT NULL CHECK (capacity > 0),
    base_price DECIMAL(10,2) NOT NULL CHECK (base_price >= 0),
    currency CHAR(3) NOT NULL DEFAULT 'USD',
    size DECIMAL(8,2),
    bed_type VARCHAR(50),
    bed_count INTEGER NOT NULL DEFAULT 1,
    amenities JSONB NOT NULL DEFAULT '[]',
    description TEXT,
    images JSONB NOT NULL DEFAULT '[]',
    status room_status NOT NULL DEFAULT 'available',
    housekeeping_status VARCHAR(50) DEFAULT 'clean',
    maintenance_notes TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID NOT NULL,
    FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE
);

-- Room types table for standardization
CREATE TABLE room_types (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    property_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    base_occupancy INTEGER NOT NULL,
    max_occupancy INTEGER NOT NULL,
    base_price DECIMAL(10,2) NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'USD',
    amenities JSONB NOT NULL DEFAULT '[]',
    images JSONB NOT NULL DEFAULT '[]',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID NOT NULL,
    FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE,
    UNIQUE(property_id, name)
);

-- Property amenities lookup table
CREATE TABLE property_amenities (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(50) NOT NULL,
    description TEXT,
    icon VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Room amenities lookup table
CREATE TABLE room_amenities (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(50) NOT NULL,
    description TEXT,
    icon VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for properties
CREATE INDEX idx_properties_tenant_id ON properties(tenant_id);
CREATE INDEX idx_properties_property_type ON properties(property_type);
CREATE INDEX idx_properties_city_country ON properties(city, country);
CREATE INDEX idx_properties_is_active ON properties(is_active) WHERE is_active = true;
CREATE INDEX idx_properties_is_deleted ON properties(is_deleted) WHERE is_deleted = false;
CREATE INDEX idx_properties_created_at ON properties(created_at);

-- Indexes for rooms
CREATE INDEX idx_rooms_property_id ON rooms(property_id);
CREATE INDEX idx_rooms_room_number ON rooms(property_id, room_number);
CREATE INDEX idx_rooms_room_type ON rooms(room_type);
CREATE INDEX idx_rooms_status ON rooms(status);
CREATE INDEX idx_rooms_capacity ON rooms(capacity);
CREATE INDEX idx_rooms_is_active ON rooms(is_active) WHERE is_active = true;
CREATE INDEX idx_rooms_is_deleted ON rooms(is_deleted) WHERE is_deleted = false;
CREATE INDEX idx_rooms_amenities_gin ON rooms USING gin(amenities);

-- Indexes for room types
CREATE INDEX idx_room_types_property_id ON room_types(property_id);
CREATE INDEX idx_room_types_name ON room_types(name);
CREATE INDEX idx_room_types_occupancy ON room_types(base_occupancy, max_occupancy);

-- Unique constraints
ALTER TABLE properties ADD CONSTRAINT uq_properties_tenant_name UNIQUE(tenant_id, name);
ALTER TABLE rooms ADD CONSTRAINT uq_rooms_property_room_number UNIQUE(property_id, room_number);

-- Triggers for updated_at
CREATE TRIGGER trigger_properties_updated_at
    BEFORE UPDATE ON properties
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_rooms_updated_at
    BEFORE UPDATE ON rooms
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_room_types_updated_at
    BEFORE UPDATE ON room_types
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
