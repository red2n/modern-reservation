-- =====================================================
-- 03-indexes.sql
-- Index Definitions for Performance Optimization
-- Generated from Entity @Index annotations
-- Date: 2025-10-06
-- Updated: 2025-10-08 (Multi-tenancy indexes added)
-- =====================================================

-- =====================================================
-- MULTI-TENANCY INDEXES
-- =====================================================

-- Tenants Table Indexes
CREATE INDEX idx_tenants_slug
    ON tenants (slug);

CREATE INDEX idx_tenants_type
    ON tenants (type);

CREATE INDEX idx_tenants_status
    ON tenants (status);

CREATE INDEX idx_tenants_active
    ON tenants (status) WHERE status = 'ACTIVE';

CREATE INDEX idx_tenants_email
    ON tenants (email);

CREATE INDEX idx_tenants_created_at
    ON tenants (created_at DESC);

CREATE INDEX idx_tenants_country
    ON tenants (country) WHERE country IS NOT NULL;

-- GIN index for JSONB config field (for complex queries)
CREATE INDEX idx_tenants_config
    ON tenants USING GIN (config);

-- GIN index for JSONB subscription field
CREATE INDEX idx_tenants_subscription
    ON tenants USING GIN (subscription);

-- Soft delete support
CREATE INDEX idx_tenants_not_deleted
    ON tenants (id) WHERE deleted_at IS NULL;

-- User-Tenant Association Indexes
CREATE INDEX idx_user_tenant_user_id
    ON user_tenant_associations (user_id);

CREATE INDEX idx_user_tenant_tenant_id
    ON user_tenant_associations (tenant_id);

CREATE INDEX idx_user_tenant_role
    ON user_tenant_associations (role);

CREATE INDEX idx_user_tenant_active
    ON user_tenant_associations (user_id, tenant_id)
    WHERE is_active = true;

CREATE INDEX idx_user_tenant_primary
    ON user_tenant_associations (user_id)
    WHERE is_primary = true;

CREATE INDEX idx_user_tenant_expires
    ON user_tenant_associations (expires_at)
    WHERE expires_at IS NOT NULL;

CREATE INDEX idx_user_tenant_last_accessed
    ON user_tenant_associations (tenant_id, last_accessed_at DESC);

-- =====================================================
-- PROPERTIES TABLE INDEXES
-- =====================================================

CREATE INDEX idx_properties_tenant_id
    ON properties (tenant_id);

CREATE INDEX idx_properties_property_code
    ON properties (property_code);

CREATE INDEX idx_properties_property_name
    ON properties (property_name);

CREATE INDEX idx_properties_city_country
    ON properties (city, country);

CREATE INDEX idx_properties_active
    ON properties (is_active) WHERE is_active = true;

CREATE INDEX idx_properties_not_deleted
    ON properties (id) WHERE deleted_at IS NULL;

-- GIN index for JSONB amenities field
CREATE INDEX idx_properties_amenities
    ON properties USING GIN (amenities);

-- =====================================================
-- GUESTS TABLE INDEXES
-- =====================================================

CREATE INDEX idx_guests_tenant_id
    ON guests (tenant_id);

CREATE INDEX idx_guests_email
    ON guests (email);

CREATE INDEX idx_guests_name
    ON guests (last_name, first_name);

CREATE INDEX idx_guests_phone
    ON guests (phone) WHERE phone IS NOT NULL;

CREATE INDEX idx_guests_vip
    ON guests (vip_status) WHERE vip_status = true;

CREATE INDEX idx_guests_guest_type
    ON guests (guest_type);

CREATE INDEX idx_guests_not_deleted
    ON guests (id) WHERE deleted_at IS NULL;

-- GIN index for JSONB preferences field
CREATE INDEX idx_guests_preferences
    ON guests USING GIN (preferences);

-- =====================================================
-- USERS TABLE INDEXES
-- =====================================================

CREATE INDEX idx_users_username
    ON users (username);

CREATE INDEX idx_users_email
    ON users (email);

CREATE INDEX idx_users_active
    ON users (is_active) WHERE is_active = true;

CREATE INDEX idx_users_verified
    ON users (is_verified) WHERE is_verified = true;

CREATE INDEX idx_users_last_login
    ON users (last_login_at DESC);

CREATE INDEX idx_users_not_deleted
    ON users (id) WHERE deleted_at IS NULL;

-- GIN index for JSONB preferences field
CREATE INDEX idx_users_preferences
    ON users USING GIN (preferences);

-- =====================================================
-- RATES TABLE INDEXES
-- =====================================================

-- Add tenant_id to existing indexes
CREATE INDEX idx_rates_tenant_id
    ON rates (tenant_id);

CREATE INDEX idx_rates_tenant_property
    ON rates (tenant_id, property_id);

CREATE INDEX idx_rates_property_room_date
    ON rates (property_id, room_type_id, effective_date);

CREATE INDEX idx_rates_property_date_range
    ON rates (property_id, effective_date, expiry_date);

CREATE INDEX idx_rates_status_strategy
    ON rates (rate_status, rate_strategy);

CREATE INDEX idx_rates_season
    ON rates (season_type, effective_date);

CREATE INDEX idx_rates_active
    ON rates (is_active) WHERE is_active = true;

CREATE INDEX idx_rates_currency
    ON rates (currency);

-- =====================================================
-- RESERVATIONS TABLE INDEXES
-- =====================================================

CREATE INDEX idx_reservations_tenant_id
    ON reservations (tenant_id);

CREATE INDEX idx_reservations_tenant_property
    ON reservations (tenant_id, property_id);

CREATE INDEX idx_reservations_tenant_dates
    ON reservations (tenant_id, check_in_date, check_out_date);

CREATE INDEX idx_reservations_property_id
    ON reservations (property_id);

CREATE INDEX idx_reservations_guest_id
    ON reservations (guest_id) WHERE guest_id IS NOT NULL;

CREATE INDEX idx_reservations_confirmation
    ON reservations (confirmation_number);

CREATE INDEX idx_reservations_check_in_date
    ON reservations (check_in_date);

CREATE INDEX idx_reservations_check_out_date
    ON reservations (check_out_date);

CREATE INDEX idx_reservations_status
    ON reservations (status);

CREATE INDEX idx_reservations_source
    ON reservations (source);

CREATE INDEX idx_reservations_booking_date
    ON reservations (booking_date);

CREATE INDEX idx_reservations_guest_email
    ON reservations (guest_email);

CREATE INDEX idx_reservations_property_dates
    ON reservations (property_id, check_in_date, check_out_date);

CREATE INDEX idx_reservations_property_status
    ON reservations (property_id, status);

-- =====================================================
-- RESERVATION STATUS HISTORY TABLE INDEXES
-- =====================================================

CREATE INDEX idx_res_history_tenant_id
    ON reservation_status_history (tenant_id);

CREATE INDEX idx_res_history_reservation_id
    ON reservation_status_history (reservation_id);

CREATE INDEX idx_res_history_changed_at
    ON reservation_status_history (changed_at DESC);

CREATE INDEX idx_res_history_changed_by
    ON reservation_status_history (changed_by) WHERE changed_by IS NOT NULL;

-- =====================================================
-- PAYMENTS TABLE INDEXES
-- =====================================================

CREATE INDEX idx_payment_tenant_id
    ON payments (tenant_id);

CREATE INDEX idx_payment_tenant_created
    ON payments (tenant_id, created_at DESC);

CREATE INDEX idx_payment_tenant_status
    ON payments (tenant_id, status);

CREATE INDEX idx_payment_reservation_id
    ON payments (reservation_id);

CREATE INDEX idx_payment_customer_id
    ON payments (customer_id);

CREATE INDEX idx_payment_status
    ON payments (status);

CREATE INDEX idx_payment_method
    ON payments (payment_method);

CREATE INDEX idx_payment_created_at
    ON payments (created_at DESC);

CREATE INDEX idx_payment_gateway_transaction_id
    ON payments (gateway_transaction_id) WHERE gateway_transaction_id IS NOT NULL;

CREATE INDEX idx_payment_reference
    ON payments (payment_reference);

CREATE INDEX idx_payment_authorized_at
    ON payments (authorized_at) WHERE authorized_at IS NOT NULL;

-- =====================================================
-- ROOM AVAILABILITY TABLE INDEXES
-- =====================================================

CREATE INDEX idx_availability_tenant_id
    ON availability.room_availability (tenant_id);

CREATE INDEX idx_availability_tenant_property_date
    ON availability.room_availability (tenant_id, property_id, availability_date);

CREATE INDEX idx_availability_property_date
    ON availability.room_availability (property_id, availability_date);

CREATE INDEX idx_availability_room_type_date
    ON availability.room_availability (room_type_id, availability_date);

CREATE INDEX idx_availability_status
    ON availability.room_availability (availability_status);

CREATE INDEX idx_availability_date
    ON availability.room_availability (availability_date);

CREATE INDEX idx_availability_property_room_date
    ON availability.room_availability (property_id, room_type_id, availability_date);

CREATE INDEX idx_availability_stop_sell
    ON availability.room_availability (stop_sell) WHERE stop_sell = true;

-- =====================================================
-- ANALYTICS METRICS TABLE INDEXES
-- =====================================================

CREATE INDEX idx_metric_tenant_id
    ON analytics_metrics (tenant_id);

CREATE INDEX idx_metric_tenant_type_period
    ON analytics_metrics (tenant_id, metric_type, period_start, period_end);

CREATE INDEX idx_metric_type_period
    ON analytics_metrics (metric_type, period_start, period_end);

CREATE INDEX idx_metric_property_granularity
    ON analytics_metrics (property_id, time_granularity)
    WHERE property_id IS NOT NULL;

CREATE INDEX idx_metric_calculated_at
    ON analytics_metrics (calculated_at DESC);

CREATE INDEX idx_metric_status
    ON analytics_metrics (status);

CREATE INDEX idx_metric_period_range
    ON analytics_metrics (period_start, period_end);

CREATE INDEX idx_metric_expires_at
    ON analytics_metrics (expires_at) WHERE expires_at IS NOT NULL;

CREATE INDEX idx_metric_property_type
    ON analytics_metrics (property_id, metric_type)
    WHERE property_id IS NOT NULL;

-- =====================================================
-- ANALYTICS METRIC DIMENSIONS TABLE INDEXES
-- =====================================================

CREATE INDEX idx_metric_dimensions_metric_id
    ON analytics_metric_dimensions (metric_id);

CREATE INDEX idx_metric_dimensions_key
    ON analytics_metric_dimensions (dimension_key);

-- =====================================================
-- ANALYTICS REPORTS TABLE INDEXES
-- =====================================================

CREATE INDEX idx_report_tenant_id
    ON analytics_reports (tenant_id);

CREATE INDEX idx_report_tenant_type_status
    ON analytics_reports (tenant_id, report_type, status);

CREATE INDEX idx_report_type_status
    ON analytics_reports (report_type, status);

CREATE INDEX idx_report_property_period
    ON analytics_reports (property_id, period_start, period_end)
    WHERE property_id IS NOT NULL;

CREATE INDEX idx_report_generated_at
    ON analytics_reports (generation_completed_at DESC)
    WHERE generation_completed_at IS NOT NULL;

CREATE INDEX idx_report_scheduled_at
    ON analytics_reports (scheduled_at) WHERE scheduled_at IS NOT NULL;

CREATE INDEX idx_report_created_by
    ON analytics_reports (created_by) WHERE created_by IS NOT NULL;

CREATE INDEX idx_report_status
    ON analytics_reports (status);

-- =====================================================
-- REPORT PROPERTY IDS TABLE INDEXES
-- =====================================================

CREATE INDEX idx_report_properties_report_id
    ON report_property_ids (report_id);

CREATE INDEX idx_report_properties_property_id
    ON report_property_ids (property_id);

-- =====================================================
-- COMMENTS
-- =====================================================

COMMENT ON INDEX idx_rates_property_room_date IS 'Performance index for rate lookups by property, room type, and date';
COMMENT ON INDEX idx_reservations_property_dates IS 'Composite index for reservation date range queries';
COMMENT ON INDEX idx_payment_created_at IS 'Index for payment history queries';
COMMENT ON INDEX idx_availability_property_date IS 'Primary lookup index for availability queries';
COMMENT ON INDEX idx_metric_type_period IS 'Primary index for metric queries by type and period';
COMMENT ON INDEX idx_report_type_status IS 'Index for report filtering by type and status';
