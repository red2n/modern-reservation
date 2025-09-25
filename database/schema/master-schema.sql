-- Master Database Schema File
-- This file orchestrates the creation of all database objects in the correct order
-- Modern Reservation Management System - Ultra-Scale Architecture

-- File execution order is critical due to foreign key dependencies
-- Each file is focused on a specific domain for better maintainability

-- =============================================================================
-- PHASE 1: Foundation and Core Setup
-- =============================================================================

-- Set up extensions, custom types, and utility functions
\i 00-extensions-and-types.sql

-- =============================================================================
-- PHASE 2: Core Domain Tables (No dependencies)
-- =============================================================================

-- Property and room management (foundation for all other entities)
\i 01-property-management.sql

-- Guest profiles and preferences (independent domain)
\i 02-guest-management.sql

-- =============================================================================
-- PHASE 3: Business Logic Tables (Depend on core domains)
-- =============================================================================

-- Reservation system (depends on properties, rooms, guests)
\i 03-reservation-management.sql

-- Payment and financial management (depends on reservations, guests)
\i 04-payment-management.sql

-- Availability and rate management (depends on properties, rooms)
\i 05-availability-rate-management.sql

-- =============================================================================
-- PHASE 4: Supporting Systems
-- =============================================================================

-- Audit and event tracking system
\i 02-audit-system.sql

-- User management and authentication
\i 06-user-management.sql

-- Channel management and integrations
\i 07-channel-management.sql

-- Housekeeping and operations
\i 08-housekeeping-operations.sql

-- Analytics and reporting
\i 09-analytics-reporting.sql

-- =============================================================================
-- PHASE 5: Performance and Optimization
-- =============================================================================

-- Additional indexes for cross-table queries
\i 10-cross-domain-indexes.sql

-- Views for common queries
\i 11-views-and-functions.sql

-- Materialized views for analytics
\i 12-materialized-views.sql

-- =============================================================================
-- PHASE 6: Sample Data and Testing
-- =============================================================================

-- Insert reference data (amenities, rate plans, etc.)
\i 90-reference-data.sql

-- Sample data for development and testing (optional)
-- \i 91-sample-data.sql

-- =============================================================================
-- Post-Creation Verification
-- =============================================================================

-- Verify all tables were created successfully
SELECT
    schemaname,
    tablename,
    hasindexes,
    hasrules,
    hastriggers
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY tablename;

-- Verify foreign key constraints
SELECT
    tc.table_name,
    tc.constraint_name,
    tc.constraint_type,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
    AND tc.table_schema = 'public'
ORDER BY tc.table_name, tc.constraint_name;

-- Check partition status for partitioned tables
SELECT
    t.tablename,
    p.schemaname as partition_schema,
    p.tablename as partition_name
FROM pg_tables t
LEFT JOIN pg_inherits i ON t.oid = i.inhparent
LEFT JOIN pg_tables p ON i.inhrelid = p.oid
WHERE t.schemaname = 'public'
    AND t.tablename IN ('reservations')
ORDER BY t.tablename, p.tablename;

-- Summary statistics
SELECT
    'Tables' as object_type,
    count(*) as count
FROM pg_tables
WHERE schemaname = 'public'
UNION ALL
SELECT
    'Indexes' as object_type,
    count(*) as count
FROM pg_indexes
WHERE schemaname = 'public'
UNION ALL
SELECT
    'Functions' as object_type,
    count(*) as count
FROM pg_proc p
JOIN pg_namespace n ON p.pronamespace = n.oid
WHERE n.nspname = 'public'
UNION ALL
SELECT
    'Triggers' as object_type,
    count(*) as count
FROM pg_trigger t
JOIN pg_class c ON t.tgrelid = c.oid
JOIN pg_namespace n ON c.relnamespace = n.oid
WHERE n.nspname = 'public'
    AND NOT t.tgisinternal;

-- Performance recommendations
ANALYZE;

-- Create comments for documentation
COMMENT ON SCHEMA public IS 'Modern Reservation Management System - Ultra-Scale Database Schema';

-- Log completion
SELECT
    'Schema installation completed successfully at: ' || CURRENT_TIMESTAMP as status;
