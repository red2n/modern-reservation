-- Create tenant_cache table for local tenant caching in reservation-engine
-- This table is synchronized from Tenant Service via Kafka events

CREATE TABLE IF NOT EXISTS tenant_cache (
    -- Primary Key (same as in Tenant Service)
    id UUID PRIMARY KEY,

    -- Basic Information
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    type VARCHAR(50) NOT NULL,  -- TenantType enum
    status VARCHAR(50) NOT NULL,  -- TenantStatus enum

    -- Contact Information
    email VARCHAR(255),
    phone VARCHAR(50),
    website VARCHAR(255),

    -- JSONB Columns for flexible data
    address JSONB,
    business_info JSONB,
    config JSONB,
    subscription JSONB,
    metadata JSONB,

    -- Cache Synchronization
    last_synced_at TIMESTAMP,

    -- Audit Fields (from source Tenant Service)
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,  -- Soft delete from source

    -- Local Cache Management
    cache_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    cache_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Indexes for fast lookups
CREATE INDEX IF NOT EXISTS idx_tenant_cache_slug ON tenant_cache(slug);
CREATE INDEX IF NOT EXISTS idx_tenant_cache_type ON tenant_cache(type);
CREATE INDEX IF NOT EXISTS idx_tenant_cache_status ON tenant_cache(status);
CREATE INDEX IF NOT EXISTS idx_tenant_cache_email ON tenant_cache(email);
CREATE INDEX IF NOT EXISTS idx_tenant_cache_deleted_at ON tenant_cache(deleted_at);
CREATE INDEX IF NOT EXISTS idx_tenant_cache_last_synced ON tenant_cache(last_synced_at);

-- Add comments for documentation
COMMENT ON TABLE tenant_cache IS 'Local read-only cache of tenant data synchronized from Tenant Service via Kafka events. Updates happen ONLY via TenantEventConsumer.';
COMMENT ON COLUMN tenant_cache.id IS 'Tenant ID (same as in Tenant Service)';
COMMENT ON COLUMN tenant_cache.last_synced_at IS 'Timestamp when this cache entry was last updated from Kafka event';
COMMENT ON COLUMN tenant_cache.cache_created_at IS 'When this cache entry was first created locally';
COMMENT ON COLUMN tenant_cache.cache_updated_at IS 'When this cache entry was last updated locally';
