-- =====================================================
-- Migration: V002 - Create Tenant Cache Table
-- Description: Local cache for tenant data synchronized from Tenant Service
-- Service: Rate Management
-- Date: 2024-10-08
-- =====================================================

-- Create tenant_cache table (simplified version matching TenantCacheDTO)
CREATE TABLE IF NOT EXISTS tenant_cache (
    -- Primary Key
    id UUID PRIMARY KEY,  -- Same UUID as in Tenant Service

    -- Basic Information (from TenantEvent/TenantCacheDTO)
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    type VARCHAR(50) NOT NULL,  -- ENUM: HOTEL, RESORT, HOSTEL, etc.
    status VARCHAR(50) NOT NULL DEFAULT 'TRIAL',  -- ENUM: TRIAL, ACTIVE, SUSPENDED, etc.

    -- Subscription Plan (simple string field)
    subscription_plan VARCHAR(50),  -- ENUM: BASIC, PROFESSIONAL, ENTERPRISE, etc.

    -- Cache Sync Fields
    last_synced_at TIMESTAMP WITHOUT TIME ZONE,  -- When cache was last updated from Kafka
    deleted_at TIMESTAMP WITHOUT TIME ZONE,  -- Soft delete timestamp from Tenant Service

    -- Local Cache Management (auto-managed by JPA)
    cache_created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cache_updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT chk_tenant_cache_type CHECK (type IN ('HOTEL', 'RESORT', 'HOSTEL', 'VACATION_RENTAL', 'APARTMENT', 'BOUTIQUE_HOTEL', 'MOTEL', 'GUEST_HOUSE', 'BED_AND_BREAKFAST', 'OTHER')),
    CONSTRAINT chk_tenant_cache_status CHECK (status IN ('TRIAL', 'ACTIVE', 'SUSPENDED', 'EXPIRED', 'CANCELLED')),
    CONSTRAINT chk_tenant_cache_subscription CHECK (subscription_plan IS NULL OR subscription_plan IN ('BASIC', 'PROFESSIONAL', 'ENTERPRISE', 'ULTIMATE'))
);

-- Create indexes for common queries
CREATE INDEX idx_tenant_cache_slug ON tenant_cache(slug) WHERE deleted_at IS NULL;
CREATE INDEX idx_tenant_cache_type ON tenant_cache(type) WHERE deleted_at IS NULL;
CREATE INDEX idx_tenant_cache_status ON tenant_cache(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_tenant_cache_deleted_at ON tenant_cache(deleted_at);
CREATE INDEX idx_tenant_cache_last_synced ON tenant_cache(last_synced_at);

-- Add comments
COMMENT ON TABLE tenant_cache IS 'Local read-only cache of tenant data synchronized from Tenant Service via Kafka events for Rate Management';
COMMENT ON COLUMN tenant_cache.id IS 'Tenant UUID - same as in master Tenant Service';
COMMENT ON COLUMN tenant_cache.last_synced_at IS 'Timestamp when this cache entry was last updated from Kafka event';
COMMENT ON COLUMN tenant_cache.deleted_at IS 'Soft delete timestamp from Tenant Service (null = active)';
COMMENT ON COLUMN tenant_cache.cache_created_at IS 'When this cache entry was first created locally';
COMMENT ON COLUMN tenant_cache.cache_updated_at IS 'When this cache entry was last modified locally';
