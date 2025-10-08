-- =====================================================
-- 04-constraints.sql
-- Foreign Key Constraints and Additional Constraints
-- Date: 2025-10-06
-- Updated: 2025-10-08 (Multi-tenancy constraints added)
-- =====================================================

-- =====================================================
-- MULTI-TENANCY CONSTRAINTS
-- =====================================================

-- User-Tenant Association Constraints
ALTER TABLE user_tenant_associations
    ADD CONSTRAINT fk_user_tenant_tenant
    FOREIGN KEY (tenant_id)
    REFERENCES tenants(id)
    ON DELETE CASCADE;

-- Only one primary tenant per user
CREATE UNIQUE INDEX idx_user_tenant_one_primary
    ON user_tenant_associations (user_id)
    WHERE is_primary = true;

-- Tenant Foreign Key Constraints for all tables
ALTER TABLE rates
    ADD CONSTRAINT fk_rates_tenant
    FOREIGN KEY (tenant_id)
    REFERENCES tenants(id)
    ON DELETE RESTRICT;

ALTER TABLE reservations
    ADD CONSTRAINT fk_reservations_tenant
    FOREIGN KEY (tenant_id)
    REFERENCES tenants(id)
    ON DELETE RESTRICT;

ALTER TABLE reservation_status_history
    ADD CONSTRAINT fk_res_history_tenant
    FOREIGN KEY (tenant_id)
    REFERENCES tenants(id)
    ON DELETE RESTRICT;

ALTER TABLE payments
    ADD CONSTRAINT fk_payments_tenant
    FOREIGN KEY (tenant_id)
    REFERENCES tenants(id)
    ON DELETE RESTRICT;

ALTER TABLE availability.room_availability
    ADD CONSTRAINT fk_availability_tenant
    FOREIGN KEY (tenant_id)
    REFERENCES tenants(id)
    ON DELETE RESTRICT;

ALTER TABLE analytics_metrics
    ADD CONSTRAINT fk_metrics_tenant
    FOREIGN KEY (tenant_id)
    REFERENCES tenants(id)
    ON DELETE RESTRICT;

ALTER TABLE analytics_reports
    ADD CONSTRAINT fk_reports_tenant
    FOREIGN KEY (tenant_id)
    REFERENCES tenants(id)
    ON DELETE RESTRICT;

-- =====================================================
-- RESERVATION STATUS HISTORY CONSTRAINTS
-- =====================================================

ALTER TABLE reservation_status_history
    ADD CONSTRAINT fk_res_history_reservation
    FOREIGN KEY (reservation_id)
    REFERENCES reservations(id)
    ON DELETE CASCADE;

-- =====================================================
-- ANALYTICS METRIC DIMENSIONS CONSTRAINTS
-- =====================================================

ALTER TABLE analytics_metric_dimensions
    ADD CONSTRAINT fk_metric_dimensions_metric
    FOREIGN KEY (metric_id)
    REFERENCES analytics_metrics(metric_id)
    ON DELETE CASCADE;

-- =====================================================
-- REPORT PROPERTY IDS CONSTRAINTS
-- =====================================================

ALTER TABLE report_property_ids
    ADD CONSTRAINT fk_report_properties_report
    FOREIGN KEY (report_id)
    REFERENCES analytics_reports(report_id)
    ON DELETE CASCADE;

-- =====================================================
-- ADDITIONAL CHECK CONSTRAINTS
-- =====================================================

-- Rates Table Constraints
ALTER TABLE rates
    ADD CONSTRAINT chk_rates_date_range
    CHECK (expiry_date IS NULL OR expiry_date >= effective_date);

ALTER TABLE rates
    ADD CONSTRAINT chk_rates_min_max
    CHECK (minimum_rate IS NULL OR maximum_rate IS NULL OR minimum_rate <= maximum_rate);

ALTER TABLE rates
    ADD CONSTRAINT chk_rates_current
    CHECK (current_rate >= 0);

ALTER TABLE rates
    ADD CONSTRAINT chk_rates_multipliers
    CHECK (
        (occupancy_multiplier IS NULL OR occupancy_multiplier >= 0) AND
        (demand_multiplier IS NULL OR demand_multiplier >= 0)
    );

-- Reservations Table Constraints
ALTER TABLE reservations
    ADD CONSTRAINT chk_reservations_dates
    CHECK (check_out_date > check_in_date);

ALTER TABLE reservations
    ADD CONSTRAINT chk_reservations_nights
    CHECK (nights > 0);

ALTER TABLE reservations
    ADD CONSTRAINT chk_reservations_amounts
    CHECK (
        room_rate >= 0 AND
        (taxes IS NULL OR taxes >= 0) AND
        (fees IS NULL OR fees >= 0) AND
        total_amount >= 0
    );

ALTER TABLE reservations
    ADD CONSTRAINT chk_reservations_guests
    CHECK (
        adults > 0 AND
        (children IS NULL OR children >= 0) AND
        (infants IS NULL OR infants >= 0)
    );

-- Payments Table Constraints
ALTER TABLE payments
    ADD CONSTRAINT chk_payments_refund
    CHECK (refunded_amount <= amount);

-- Room Availability Table Constraints
ALTER TABLE availability.room_availability
    ADD CONSTRAINT chk_availability_rooms
    CHECK (
        available_rooms <= total_rooms AND
        occupied_rooms >= 0 AND
        maintenance_rooms >= 0 AND
        blocked_rooms >= 0 AND
        (occupied_rooms + maintenance_rooms + blocked_rooms) <= total_rooms
    );

ALTER TABLE availability.room_availability
    ADD CONSTRAINT chk_availability_stay
    CHECK (
        minimum_stay >= 1 AND
        (maximum_stay IS NULL OR maximum_stay >= minimum_stay)
    );

ALTER TABLE availability.room_availability
    ADD CONSTRAINT chk_availability_rates
    CHECK (
        (base_rate IS NULL OR base_rate >= 0) AND
        (current_rate IS NULL OR current_rate >= 0) AND
        (min_rate IS NULL OR min_rate >= 0) AND
        (max_rate IS NULL OR max_rate >= 0) AND
        (min_rate IS NULL OR max_rate IS NULL OR min_rate <= max_rate)
    );

-- Analytics Metrics Table Constraints
ALTER TABLE analytics_metrics
    ADD CONSTRAINT chk_metrics_period
    CHECK (period_end >= period_start);

ALTER TABLE analytics_metrics
    ADD CONSTRAINT chk_metrics_percentage
    CHECK (percentage_value IS NULL OR (percentage_value >= 0 AND percentage_value <= 100));

-- Analytics Reports Table Constraints
ALTER TABLE analytics_reports
    ADD CONSTRAINT chk_reports_period
    CHECK (period_end >= period_start);

ALTER TABLE analytics_reports
    ADD CONSTRAINT chk_reports_generation
    CHECK (
        generation_completed_at IS NULL OR
        generation_started_at IS NULL OR
        generation_completed_at >= generation_started_at
    );

-- =====================================================
-- UNIQUE CONSTRAINTS
-- =====================================================

-- Rates: Unique rate code per property and room type
ALTER TABLE rates
    ADD CONSTRAINT uq_rates_code
    UNIQUE (property_id, room_type_id, rate_code);

-- Room Availability: Unique availability per property, room type, and date
ALTER TABLE availability.room_availability
    ADD CONSTRAINT uq_availability_property_room_date
    UNIQUE (property_id, room_type_id, availability_date);

-- =====================================================
-- COMMENTS
-- =====================================================

COMMENT ON CONSTRAINT fk_res_history_reservation ON reservation_status_history IS
    'Links status history to parent reservation, cascade delete on reservation removal';

COMMENT ON CONSTRAINT chk_rates_date_range ON rates IS
    'Ensures expiry date is not before effective date';

COMMENT ON CONSTRAINT chk_reservations_dates ON reservations IS
    'Ensures check-out date is after check-in date';

COMMENT ON CONSTRAINT chk_availability_rooms ON availability.room_availability IS
    'Ensures room counts are logical and do not exceed total inventory';

COMMENT ON CONSTRAINT chk_metrics_period ON analytics_metrics IS
    'Ensures metric period end is not before period start';
