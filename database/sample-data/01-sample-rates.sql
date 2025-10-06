-- Sample Data for Rates Table
-- Generates 500 realistic rate records with various strategies, statuses, and pricing

-- Insert 500 sample rates
INSERT INTO rates (
    id, property_id, room_type_id, rate_code, rate_name, description,
    rate_strategy, rate_status, season_type,
    base_rate, current_rate, minimum_rate, maximum_rate, currency,
    effective_date, expiry_date,
    minimum_stay, maximum_stay, advance_booking_days, maximum_booking_days,
    is_refundable, is_modifiable, cancellation_hours,
    tax_inclusive, service_fee_inclusive,
    occupancy_multiplier, demand_multiplier, competitive_adjustment,
    priority_order, is_active,
    created_by, updated_by, created_at, updated_at
)
SELECT
    gen_random_uuid(),
    -- Cycle through 10 properties
    (ARRAY[
        '11111111-1111-1111-1111-111111111111'::uuid,
        '22222222-2222-2222-2222-222222222222'::uuid,
        '33333333-3333-3333-3333-333333333333'::uuid,
        '44444444-4444-4444-4444-444444444444'::uuid,
        '55555555-5555-5555-5555-555555555555'::uuid,
        '66666666-6666-6666-6666-666666666666'::uuid,
        '77777777-7777-7777-7777-777777777777'::uuid,
        '88888888-8888-8888-8888-888888888888'::uuid,
        '99999999-9999-9999-9999-999999999999'::uuid,
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'::uuid
    ])[1 + (s % 10)] AS property_id,
    -- Cycle through 5 room types
    (ARRAY[
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'::uuid,
        'cccccccc-cccc-cccc-cccc-cccccccccccc'::uuid,
        'dddddddd-dddd-dddd-dddd-dddddddddddd'::uuid,
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee'::uuid,
        'ffffffff-ffff-ffff-ffff-ffffffffffff'::uuid
    ])[1 + (s % 5)] AS room_type_id,
    'RATE-' || LPAD(s::text, 5, '0') AS rate_code,
    (ARRAY['Standard Rate', 'Flexible Rate', 'Advanced Purchase', 'Last Minute Deal', 'Weekend Special',
           'Corporate Rate', 'Government Rate', 'AAA Rate', 'Senior Rate', 'Promo Rate'])[1 + (s % 10)] AS rate_name,
    (ARRAY['Best available rate with full flexibility', 'Non-refundable discounted rate', 'Book early and save',
           'Last minute discount for immediate bookings', 'Special weekend pricing',
           'Corporate discount rate', 'Government employee rate', 'AAA member discount',
           'Senior citizen discount', 'Promotional special offer'])[1 + (s % 10)] AS description,
    (ARRAY['FIXED', 'DYNAMIC', 'SEASONAL', 'LAST_MINUTE', 'EARLY_BIRD',
           'LENGTH_OF_STAY', 'OCCUPANCY_BASED', 'REVENUE_OPTIMIZATION', 'COMPETITIVE', 'PROMOTIONAL'])[1 + (s % 10)]::rate_strategy AS rate_strategy,
    (ARRAY['ACTIVE', 'ACTIVE', 'ACTIVE', 'ACTIVE', 'SUSPENDED', 'DRAFT', 'EXPIRED', 'ARCHIVED'])[1 + (s % 8)]::rate_status AS rate_status,
    (ARRAY['PEAK', 'HIGH', 'REGULAR', 'LOW', 'OFF_PEAK'])[1 + (s % 5)]::season_type AS season_type,
    -- Base rate between 100 and 500
    (100 + (s % 400))::numeric(10,2) AS base_rate,
    -- Current rate with some variance
    (100 + (s % 400) + ((s % 50) - 25))::numeric(10,2) AS current_rate,
    -- Min rate (80% of base)
    ((100 + (s % 400)) * 0.8)::numeric(10,2) AS minimum_rate,
    -- Max rate (150% of base)
    ((100 + (s % 400)) * 1.5)::numeric(10,2) AS maximum_rate,
    (ARRAY['USD', 'EUR', 'GBP'])[1 + (s % 3)] AS currency,
    -- Effective date spread over next year
    CURRENT_DATE + (s % 365) AS effective_date,
    -- Expiry date 30-180 days after effective
    CASE WHEN s % 3 = 0 THEN NULL ELSE CURRENT_DATE + (s % 365) + 30 + (s % 150) END AS expiry_date,
    -- Minimum stay 1-7 nights
    1 + (s % 7) AS minimum_stay,
    -- Maximum stay 7-30 nights
    CASE WHEN s % 5 = 0 THEN NULL ELSE 7 + (s % 24) END AS maximum_stay,
    -- Advance booking days
    CASE WHEN s % 4 = 0 THEN NULL ELSE (s % 90) END AS advance_booking_days,
    -- Maximum booking days
    CASE WHEN s % 4 = 0 THEN NULL ELSE 180 + (s % 180) END AS maximum_booking_days,
    -- Is refundable (70% yes)
    (s % 10) < 7 AS is_refundable,
    -- Is modifiable (80% yes)
    (s % 10) < 8 AS is_modifiable,
    -- Cancellation hours
    (ARRAY[24, 48, 72, 168])[1 + (s % 4)] AS cancellation_hours,
    -- Tax inclusive (50/50)
    (s % 2) = 0 AS tax_inclusive,
    -- Service fee inclusive (50/50)
    (s % 2) = 1 AS service_fee_inclusive,
    -- Occupancy multiplier 0.8 - 1.2
    (0.8 + (s % 40) * 0.01)::numeric(5,2) AS occupancy_multiplier,
    -- Demand multiplier 0.9 - 1.5
    (0.9 + (s % 60) * 0.01)::numeric(5,2) AS demand_multiplier,
    -- Competitive adjustment -0.2 to +0.2
    (-0.2 + (s % 40) * 0.01)::numeric(5,2) AS competitive_adjustment,
    -- Priority order
    1 + (s % 10) AS priority_order,
    -- Is active (80% yes)
    (s % 10) < 8 AS is_active,
    'system' AS created_by,
    CASE WHEN s % 5 = 0 THEN 'admin' ELSE NULL END AS updated_by,
    CURRENT_TIMESTAMP - (s || ' hours')::interval AS created_at,
    CASE WHEN s % 5 = 0 THEN CURRENT_TIMESTAMP - ((s / 2) || ' hours')::interval ELSE NULL END AS updated_at
FROM generate_series(1, 500) AS s;

-- Verify the insert
SELECT COUNT(*) as total_rates FROM rates;
SELECT rate_status, COUNT(*) FROM rates GROUP BY rate_status ORDER BY rate_status;
