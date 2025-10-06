-- =====================================================
-- 01-extensions-and-types.sql
-- PostgreSQL Extensions and Enum Type Definitions
-- Generated from Entity Classes
-- Date: 2025-10-06
-- =====================================================

-- Enable required PostgreSQL extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =====================================================
-- RATE MANAGEMENT ENUMS
-- =====================================================

-- Rate Status Enum
CREATE TYPE rate_status AS ENUM (
    'DRAFT',
    'ACTIVE',
    'SUSPENDED',
    'EXPIRED',
    'ARCHIVED'
);

-- Rate Strategy Enum
CREATE TYPE rate_strategy AS ENUM (
    'FIXED',
    'DYNAMIC',
    'SEASONAL',
    'LAST_MINUTE',
    'EARLY_BIRD',
    'LENGTH_OF_STAY',
    'OCCUPANCY_BASED',
    'REVENUE_OPTIMIZATION',
    'COMPETITIVE',
    'PROMOTIONAL'
);

-- Season Type Enum
CREATE TYPE season_type AS ENUM (
    'PEAK',
    'HIGH',
    'REGULAR',
    'LOW',
    'OFF_PEAK'
);

-- =====================================================
-- RESERVATION ENGINE ENUMS
-- =====================================================

-- Reservation Status Enum
CREATE TYPE reservation_status AS ENUM (
    'PENDING',
    'CONFIRMED',
    'CHECKED_IN',
    'CHECKED_OUT',
    'CANCELLED',
    'NO_SHOW',
    'EXPIRED',
    'WAITLISTED',
    'MODIFIED',
    'ON_HOLD'
);

-- Reservation Source Enum
CREATE TYPE reservation_source AS ENUM (
    'DIRECT',
    'PHONE',
    'EMAIL',
    'WALK_IN',
    'BOOKING_COM',
    'EXPEDIA',
    'AIRBNB',
    'HOTELS_COM',
    'AGODA',
    'PRICELINE',
    'KAYAK',
    'TRIVAGO',
    'CORPORATE',
    'TRAVEL_AGENT',
    'GROUP_BOOKING',
    'EVENT_BOOKING',
    'MOBILE_APP',
    'SOCIAL_MEDIA',
    'API',
    'IMPORT',
    'ADMIN',
    'OTHER'
);

-- =====================================================
-- PAYMENT PROCESSOR ENUMS
-- =====================================================

-- Payment Method Enum
CREATE TYPE payment_method AS ENUM (
    'CREDIT_CARD',
    'DEBIT_CARD',
    'CASH',
    'BANK_TRANSFER',
    'PAYPAL',
    'STRIPE',
    'APPLE_PAY',
    'GOOGLE_PAY',
    'CRYPTOCURRENCY',
    'CHECK',
    'WIRE_TRANSFER',
    'OTHER'
);

-- Payment Status Enum
CREATE TYPE payment_status AS ENUM (
    'PENDING',
    'AUTHORIZED',
    'CAPTURED',
    'COMPLETED',
    'FAILED',
    'CANCELLED',
    'REFUNDED',
    'PARTIALLY_REFUNDED',
    'EXPIRED',
    'PROCESSING',
    'DECLINED'
);

-- Transaction Type Enum
CREATE TYPE transaction_type AS ENUM (
    'CHARGE',
    'REFUND',
    'AUTHORIZATION',
    'CAPTURE',
    'VOID',
    'ADJUSTMENT',
    'CHARGEBACK',
    'REVERSAL'
);

-- =====================================================
-- AVAILABILITY CALCULATOR ENUMS
-- =====================================================

-- Availability Status Enum
CREATE TYPE availability_status AS ENUM (
    'AVAILABLE',
    'OCCUPIED',
    'MAINTENANCE',
    'BLOCKED',
    'RESERVED'
);

-- Room Category Enum
CREATE TYPE room_category AS ENUM (
    'STANDARD',
    'DELUXE',
    'SUITE',
    'EXECUTIVE',
    'PRESIDENTIAL'
);

-- Pricing Method Enum
CREATE TYPE pricing_method AS ENUM (
    'FIXED',
    'DYNAMIC',
    'SEASONAL',
    'OCCUPANCY_BASED'
);

-- =====================================================
-- ANALYTICS ENGINE ENUMS
-- =====================================================

-- Time Granularity Enum
CREATE TYPE time_granularity AS ENUM (
    'HOURLY',
    'DAILY',
    'WEEKLY',
    'MONTHLY',
    'QUARTERLY',
    'YEARLY'
);

-- Analytics Status Enum
CREATE TYPE analytics_status AS ENUM (
    'PENDING',
    'CALCULATING',
    'COMPLETED',
    'FAILED',
    'EXPIRED',
    'CACHED'
);

-- Metric Type Enum (simplified - full list would be very long)
CREATE TYPE metric_type AS ENUM (
    'OCCUPANCY_RATE',
    'ADR',
    'REVPAR',
    'TOTAL_REVENUE',
    'ROOM_REVENUE',
    'ANCILLARY_REVENUE',
    'BOOKING_COUNT',
    'CANCELLATION_RATE',
    'NO_SHOW_RATE',
    'AVERAGE_LENGTH_OF_STAY',
    'LEAD_TIME',
    'CUSTOMER_ACQUISITION_COST',
    'CUSTOMER_LIFETIME_VALUE',
    'CONVERSION_RATE',
    'CHANNEL_CONTRIBUTION',
    'MARKET_SEGMENT_MIX',
    'CUSTOM'
);

COMMENT ON EXTENSION "uuid-ossp" IS 'UUID generation functions';
COMMENT ON EXTENSION "pgcrypto" IS 'Cryptographic functions for secure data handling';

COMMENT ON TYPE rate_status IS 'Rate lifecycle status';
COMMENT ON TYPE rate_strategy IS 'Pricing strategy types';
COMMENT ON TYPE season_type IS 'Seasonal pricing periods';
COMMENT ON TYPE reservation_status IS 'Reservation lifecycle status';
COMMENT ON TYPE reservation_source IS 'Booking channel/source';
COMMENT ON TYPE payment_method IS 'Payment method types';
COMMENT ON TYPE payment_status IS 'Payment transaction status';
COMMENT ON TYPE transaction_type IS 'Payment transaction types';
COMMENT ON TYPE availability_status IS 'Room availability status';
COMMENT ON TYPE room_category IS 'Room category/class';
COMMENT ON TYPE pricing_method IS 'Pricing calculation method';
COMMENT ON TYPE time_granularity IS 'Time period granularity for analytics';
COMMENT ON TYPE analytics_status IS 'Analytics calculation status';
COMMENT ON TYPE metric_type IS 'Business metric types';
