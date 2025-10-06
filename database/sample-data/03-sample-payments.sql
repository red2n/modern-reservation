-- Sample Data for Payments Table
-- Generates 500 realistic payment records linked to reservations

-- First, let's get some reservation IDs to reference
WITH reservation_ids AS (
    SELECT id, total_amount FROM reservations ORDER BY created_at LIMIT 500
)
INSERT INTO payments (
    id, payment_reference, reservation_id, customer_id,
    amount, currency, processing_fee,
    payment_method, transaction_type, status,
    gateway_provider, gateway_transaction_id, authorization_code,
    card_last_four, card_brand,
    billing_name, billing_email, billing_address,
    description, failure_reason, refunded_amount,
    authorized_at, captured_at, settled_at
)
SELECT
    gen_random_uuid() AS id,
    'PAY-' || TO_CHAR(CURRENT_DATE, 'YYYYMMDD') || '-' || LPAD(s::text, 8, '0') AS payment_reference,
    -- Use actual reservation IDs
    (SELECT id FROM reservation_ids OFFSET (s-1) LIMIT 1) AS reservation_id,
    -- Customer ID (UUID for consistency)
    gen_random_uuid() AS customer_id,
    -- Amount (varies, use reservation total or partial amounts)
    (50 + (s % 950))::numeric(10,2) AS amount,
    -- Currency
    (ARRAY['USD', 'EUR', 'GBP'])[1 + (s % 3)] AS currency,
    -- Processing fee (2-3.5% of amount)
    ((50 + (s % 950)) * (0.02 + (s % 15) * 0.001))::numeric(10,2) AS processing_fee,
    -- Payment method distribution
    (ARRAY['CREDIT_CARD', 'CREDIT_CARD', 'CREDIT_CARD', 'DEBIT_CARD', 'DEBIT_CARD',
           'PAYPAL', 'STRIPE', 'CASH', 'BANK_TRANSFER', 'APPLE_PAY', 'GOOGLE_PAY'])[1 + (s % 11)]::payment_method AS payment_method,
    -- Transaction type distribution
    (ARRAY['CHARGE', 'CHARGE', 'CHARGE', 'CHARGE', 'AUTHORIZATION',
           'CAPTURE', 'REFUND', 'VOID', 'ADJUSTMENT'])[1 + (s % 9)]::transaction_type AS transaction_type,
    -- Status distribution (mostly completed)
    (ARRAY['COMPLETED', 'COMPLETED', 'COMPLETED', 'COMPLETED', 'COMPLETED',
           'PENDING', 'AUTHORIZED', 'FAILED', 'REFUNDED', 'CANCELLED'])[1 + (s % 10)]::payment_status AS status,
    -- Gateway provider
    CASE
        WHEN (ARRAY['CREDIT_CARD', 'DEBIT_CARD'])[1] = ANY(ARRAY['CREDIT_CARD', 'DEBIT_CARD'])
        THEN (ARRAY['Stripe', 'Square', 'Braintree', 'Authorize.Net'])[1 + (s % 4)]
        WHEN (ARRAY['PAYPAL'])[1] = 'PAYPAL' THEN 'PayPal'
        ELSE (ARRAY['Stripe', 'Square'])[1 + (s % 2)]
    END AS gateway_provider,
    -- Gateway transaction ID
    'TXN-' || UPPER(MD5(s::text || CURRENT_TIMESTAMP::text)) AS gateway_transaction_id,
    -- Authorization code (for successful payments)
    CASE WHEN s % 10 < 8 THEN 'AUTH-' || LPAD((100000 + s)::text, 6, '0') ELSE NULL END AS authorization_code,
    -- Card last four (for card payments)
    CASE WHEN s % 3 < 2 THEN LPAD(((s % 9000) + 1000)::text, 4, '0') ELSE NULL END AS card_last_four,
    -- Card brand
    CASE WHEN s % 3 < 2 THEN
        (ARRAY['Visa', 'Mastercard', 'American Express', 'Discover'])[1 + (s % 4)]
    ELSE NULL END AS card_brand,
    -- Billing name
    (ARRAY['John Smith', 'Jane Doe', 'Michael Johnson', 'Emily Brown', 'David Wilson',
           'Sarah Davis', 'Robert Miller', 'Lisa Garcia', 'James Martinez', 'Mary Rodriguez'])[1 + (s % 10)] AS billing_name,
    -- Billing email
    LOWER((ARRAY['john.smith', 'jane.doe', 'michael.johnson', 'emily.brown', 'david.wilson',
           'sarah.davis', 'robert.miller', 'lisa.garcia', 'james.martinez', 'mary.rodriguez'])[1 + (s % 10)]) ||
    s || '@email.com' AS billing_email,
    -- Billing address
    (100 + (s % 900)) || ' ' ||
    (ARRAY['Main St', 'Oak Ave', 'Maple Dr', 'Pine Ln', 'Cedar Blvd', 'Elm St', 'Park Ave'])[1 + (s % 7)] || ', ' ||
    (ARRAY['New York', 'Los Angeles', 'Chicago', 'Houston', 'Phoenix', 'Philadelphia', 'San Antonio'])[1 + (s % 7)] || ', ' ||
    (ARRAY['NY', 'CA', 'IL', 'TX', 'AZ', 'PA', 'TX'])[1 + (s % 7)] || ' ' ||
    LPAD((10000 + (s % 89999))::text, 5, '0') AS billing_address,
    -- Description
    'Payment for reservation ' || 'RES-' || TO_CHAR(CURRENT_DATE, 'YYYYMMDD') || '-' || LPAD(s::text, 6, '0') AS description,
    -- Failure reason (only for failed payments)
    CASE WHEN s % 10 = 7 THEN
        (ARRAY['Insufficient funds', 'Card declined', 'Invalid card number', 'Expired card',
               'Card security code invalid', 'Payment gateway timeout', 'Fraud detected'])[1 + (s % 7)]
    ELSE NULL END AS failure_reason,
    -- Refunded amount (0 for most, some amount for refunded)
    CASE WHEN s % 10 = 8 THEN (20 + (s % 480))::numeric(10,2) ELSE 0.00 END AS refunded_amount,
    -- Authorized at (for successful payments)
    CASE WHEN s % 10 < 8 THEN CURRENT_TIMESTAMP - ((s * 2) || ' hours')::interval ELSE NULL END AS authorized_at,
    -- Captured at (slightly after authorized)
    CASE WHEN s % 10 < 8 THEN CURRENT_TIMESTAMP - ((s * 2 - 1) || ' hours')::interval ELSE NULL END AS captured_at,
    -- Settled at (1-3 days after capture for completed)
    CASE WHEN s % 10 < 5 THEN CURRENT_TIMESTAMP - ((s * 2 - 48 - (s % 48)) || ' hours')::interval ELSE NULL END AS settled_at
FROM generate_series(1, 500) AS s;

-- Verify the insert
SELECT COUNT(*) as total_payments FROM payments;
SELECT status, COUNT(*) FROM payments GROUP BY status ORDER BY status;
SELECT payment_method, COUNT(*) FROM payments GROUP BY payment_method ORDER BY COUNT(*) DESC;
SELECT transaction_type, COUNT(*) FROM payments GROUP BY transaction_type ORDER BY COUNT(*) DESC;
