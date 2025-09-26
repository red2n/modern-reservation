-- Core Extensions and Base Configuration
-- This file sets up PostgreSQL extensions and base configurations needed for the entire system

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "btree_gin";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";

-- Create custom types
CREATE TYPE property_type AS ENUM ('hotel', 'resort', 'apartment', 'villa', 'hostel');
CREATE TYPE room_status AS ENUM ('available', 'occupied', 'maintenance', 'out_of_order');
CREATE TYPE reservation_status AS ENUM ('pending', 'confirmed', 'checked_in', 'checked_out', 'cancelled', 'no_show');
CREATE TYPE reservation_source AS ENUM ('direct', 'booking_dot_com', 'expedia', 'airbnb', 'phone', 'walk_in');
CREATE TYPE payment_method AS ENUM ('credit_card', 'debit_card', 'cash', 'bank_transfer', 'digital_wallet');
CREATE TYPE payment_status AS ENUM ('pending', 'completed', 'failed', 'refunded', 'cancelled');
CREATE TYPE task_type AS ENUM ('cleaning', 'maintenance', 'inspection', 'deep_clean');
CREATE TYPE priority_level AS ENUM ('low', 'normal', 'high', 'urgent');
CREATE TYPE room_condition AS ENUM ('good', 'needs_cleaning', 'maintenance_required', 'damaged');
CREATE TYPE user_role AS ENUM ('super_admin', 'property_manager', 'front_desk', 'housekeeping', 'maintenance', 'guest', 'channel_manager', 'finance');

-- Create common functions
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create partition management functions
CREATE OR REPLACE FUNCTION create_monthly_partition(table_name TEXT, start_date DATE)
RETURNS VOID AS $$
DECLARE
    partition_name TEXT;
    end_date DATE;
BEGIN
    partition_name := table_name || '_' || to_char(start_date, 'YYYY_MM');
    end_date := start_date + INTERVAL '1 month';

    EXECUTE format('CREATE TABLE IF NOT EXISTS %I PARTITION OF %I
                    FOR VALUES FROM (%L) TO (%L)',
                   partition_name, table_name, start_date, end_date);
END;
$$ LANGUAGE plpgsql;
