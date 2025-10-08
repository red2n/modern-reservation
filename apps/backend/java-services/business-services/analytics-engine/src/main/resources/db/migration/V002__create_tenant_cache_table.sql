-- =====================================================
-- Migration: V002 - Create Tenant Cache Table
-- Description: Local cache for tenant data synchronized from Tenant Service
-- Service: Analytics Engine
-- Date: 2024-10-08
-- Purpose: Enable fast tenant validation and analytics data filtering
-- =====================================================

CREATE TABLE IF NOT EXISTS tenant_cache (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'TRIAL',
    subscription_plan VARCHAR(50),
    last_synced_at TIMESTAMP WITHOUT TIME ZONE,
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    cache_created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cache_updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_tenant_cache_type CHECK (type IN ('HOTEL', 'RESORT', 'HOSTEL', 'VACATION_RENTAL', 'APARTMENT', 'BOUTIQUE_HOTEL', 'MOTEL', 'GUEST_HOUSE', 'BED_AND_BREAKFAST', 'OTHER')),
    CONSTRAINT chk_tenant_cache_status CHECK (status IN ('TRIAL', 'ACTIVE', 'SUSPENDED', 'EXPIRED', 'CANCELLED')),
    CONSTRAINT chk_tenant_cache_subscription CHECK (subscription_plan IS NULL OR subscription_plan IN ('BASIC', 'PROFESSIONAL', 'ENTERPRISE', 'ULTIMATE'))
);

CREATE INDEX idx_tenant_cache_slug ON tenant_cache(slug) WHERE deleted_at IS NULL;
CREATE INDEX idx_tenant_cache_type ON tenant_cache(type) WHERE deleted_at IS NULL;
CREATE INDEX idx_tenant_cache_status ON tenant_cache(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_tenant_cache_deleted_at ON tenant_cache(deleted_at);
CREATE INDEX idx_tenant_cache_last_synced ON tenant_cache(last_synced_at);
CREATE INDEX idx_tenant_cache_analytics_eligible ON tenant_cache(status)
    WHERE (status IN ('ACTIVE', 'TRIAL')) AND deleted_at IS NULL;

COMMENT ON TABLE tenant_cache IS 'Local read-only cache of tenant data for Analytics Engine - ensures proper data segmentation';
COMMENT ON COLUMN tenant_cache.id IS 'Tenant UUID - same as in master Tenant Service';
COMMENT ON COLUMN tenant_cache.status IS 'Tenant status - only ACTIVE and TRIAL can access analytics';
COMMENT ON COLUMN tenant_cache.deleted_at IS 'Soft delete timestamp - blocks analytics access when set';
