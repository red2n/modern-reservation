-- Sample Payments Data
-- This file contains sample data for payments, invoices, refunds, and payment gateways

-- Sample Payment Gateways (5 gateways)
INSERT INTO payment_gateways (id, gateway_name, gateway_type, api_endpoint, is_active, supported_currencies, configuration, created_at, updated_at) VALUES
(gen_random_uuid(), 'Stripe', 'CREDIT_CARD', 'https://api.stripe.com', true, ARRAY['USD', 'EUR', 'GBP'], '{"api_version": "2023-10-16"}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'PayPal', 'PAYPAL', 'https://api.paypal.com', true, ARRAY['USD', 'EUR', 'CAD'], '{"mode": "live"}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Square', 'CREDIT_CARD', 'https://connect.squareup.com', true, ARRAY['USD', 'CAD'], '{"location_id": "main"}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Authorize.Net', 'CREDIT_CARD', 'https://api.authorize.net', true, ARRAY['USD'], '{"transaction_type": "auth_capture"}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Bank Transfer', 'BANK_TRANSFER', NULL, true, ARRAY['USD', 'EUR'], '{"account_number": "****1234"}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Sample Payments (120 payments)
DO $$
DECLARE
    reservation_rec RECORD;
    gateway_ids UUID[];
    gateway_id UUID;
    payment_id UUID;
    counter INTEGER := 0;
BEGIN
    SELECT ARRAY_AGG(id) INTO gateway_ids FROM payment_gateways;

    FOR reservation_rec IN (SELECT id, total_amount, guest_id FROM reservations ORDER BY id LIMIT 120) LOOP
        counter := counter + 1;
        gateway_id := gateway_ids[((counter - 1) % ARRAY_LENGTH(gateway_ids, 1)) + 1];
        payment_id := gen_random_uuid();

        INSERT INTO payments (
            id, reservation_id, guest_id, payment_gateway_id,
            payment_method, payment_type, amount, currency,
            payment_status, transaction_id, authorization_code,
            card_last_four, card_type, payment_date, processed_at,
            created_at, updated_at
        ) VALUES (
            payment_id,
            reservation_rec.id,
            reservation_rec.guest_id,
            gateway_id,
            CASE (counter % 5)
                WHEN 0 THEN 'CREDIT_CARD'
                WHEN 1 THEN 'DEBIT_CARD'
                WHEN 2 THEN 'PAYPAL'
                WHEN 3 THEN 'BANK_TRANSFER'
                ELSE 'CASH'
            END,
            CASE (counter % 3)
                WHEN 0 THEN 'FULL_PAYMENT'
                WHEN 1 THEN 'DEPOSIT'
                ELSE 'PARTIAL'
            END,
            reservation_rec.total_amount,
            'USD',
            CASE (counter % 10)
                WHEN 0 THEN 'PENDING'
                WHEN 1 THEN 'FAILED'
                ELSE 'COMPLETED'
            END,
            'TXN' || LPAD(counter::TEXT, 12, '0'),
            'AUTH' || LPAD(counter::TEXT, 8, '0'),
            CASE (counter % 5)
                WHEN 0 THEN '4532'
                WHEN 1 THEN '5425'
                WHEN 2 THEN '3782'
                WHEN 3 THEN '6011'
                ELSE NULL
            END,
            CASE (counter % 5)
                WHEN 0 THEN 'VISA'
                WHEN 1 THEN 'MASTERCARD'
                WHEN 2 THEN 'AMEX'
                WHEN 3 THEN 'DISCOVER'
                ELSE NULL
            END,
            CURRENT_TIMESTAMP - ((counter % 50) || ' days')::INTERVAL,
            CURRENT_TIMESTAMP - ((counter % 50) || ' days')::INTERVAL,
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        );

        -- Add payment attempts (1-2 per payment)
        FOR i IN 1..(1 + (counter % 2)) LOOP
            INSERT INTO payment_attempts (
                id, payment_id, attempt_number, attempted_at,
                payment_gateway_id, amount, currency, attempt_status,
                error_code, error_message, gateway_response, created_at
            ) VALUES (
                gen_random_uuid(),
                payment_id,
                i,
                CURRENT_TIMESTAMP - ((counter % 50) || ' days ' || (i * 5) || ' minutes')::INTERVAL,
                gateway_id,
                reservation_rec.total_amount,
                'USD',
                CASE
                    WHEN i = 1 AND counter % 10 = 1 THEN 'FAILED'
                    WHEN i = 1 THEN 'SUCCESS'
                    ELSE 'SUCCESS'
                END,
                CASE WHEN i = 1 AND counter % 10 = 1 THEN 'CARD_DECLINED' ELSE NULL END,
                CASE WHEN i = 1 AND counter % 10 = 1 THEN 'Insufficient funds' ELSE NULL END,
                '{"status": "success", "transaction_id": "' || counter || '"}',
                CURRENT_TIMESTAMP
            );
        END LOOP;
    END LOOP;
END $$;

-- Sample Invoices (120 invoices)
DO $$
DECLARE
    reservation_rec RECORD;
    property_rec RECORD;
    invoice_id UUID;
    counter INTEGER := 0;
    tax_rate DECIMAL(5,2) := 0.10;
BEGIN
    FOR reservation_rec IN (
        SELECT r.id as reservation_id, r.guest_id, r.total_amount, r.property_id
        FROM reservations r
        ORDER BY r.id
        LIMIT 120
    ) LOOP
        counter := counter + 1;
        invoice_id := gen_random_uuid();

        SELECT * INTO property_rec FROM properties WHERE id = reservation_rec.property_id;

        INSERT INTO invoices (
            id, invoice_number, reservation_id, guest_id, property_id,
            invoice_date, due_date, subtotal, tax_amount, total_amount,
            currency, invoice_status, payment_status, payment_terms,
            billing_address, notes, created_at, updated_at
        ) VALUES (
            invoice_id,
            'INV' || TO_CHAR(CURRENT_DATE, 'YYYY') || LPAD(counter::TEXT, 6, '0'),
            reservation_rec.reservation_id,
            reservation_rec.guest_id,
            reservation_rec.property_id,
            CURRENT_DATE - ((counter % 40) || ' days')::INTERVAL,
            CURRENT_DATE - ((counter % 40) || ' days')::INTERVAL + INTERVAL '30 days',
            reservation_rec.total_amount,
            reservation_rec.total_amount * tax_rate,
            reservation_rec.total_amount * (1 + tax_rate),
            'USD',
            CASE (counter % 4)
                WHEN 0 THEN 'DRAFT'
                WHEN 1 THEN 'SENT'
                WHEN 2 THEN 'PAID'
                ELSE 'OVERDUE'
            END,
            CASE (counter % 3)
                WHEN 0 THEN 'PAID'
                WHEN 1 THEN 'PENDING'
                ELSE 'PARTIAL'
            END,
            'Net 30',
            property_rec.address || ', ' || property_rec.city || ', ' || property_rec.state,
            'Thank you for your business',
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        );

        -- Add invoice line items (2-4 per invoice)
        FOR i IN 1..(2 + (counter % 3)) LOOP
            INSERT INTO invoice_line_items (
                id, invoice_id, item_type, description, quantity,
                unit_price, total_price, tax_rate, tax_amount, created_at
            ) VALUES (
                gen_random_uuid(),
                invoice_id,
                CASE (i % 4)
                    WHEN 0 THEN 'ROOM'
                    WHEN 1 THEN 'SERVICE'
                    WHEN 2 THEN 'TAX'
                    ELSE 'FEE'
                END,
                CASE (i % 4)
                    WHEN 0 THEN 'Room Charge'
                    WHEN 1 THEN 'Housekeeping Service'
                    WHEN 2 THEN 'City Tax'
                    ELSE 'Resort Fee'
                END,
                i,
                (50.00 + (counter * 2)),
                (50.00 + (counter * 2)) * i,
                tax_rate,
                (50.00 + (counter * 2)) * i * tax_rate,
                CURRENT_TIMESTAMP
            );
        END LOOP;
    END LOOP;
END $$;

-- Sample Refunds (30 refunds)
DO $$
DECLARE
    payment_rec RECORD;
    counter INTEGER := 0;
BEGIN
    FOR payment_rec IN (
        SELECT id, reservation_id, guest_id, amount, currency
        FROM payments
        WHERE payment_status = 'COMPLETED'
        ORDER BY id
        LIMIT 30
    ) LOOP
        counter := counter + 1;

        INSERT INTO refunds (
            id, payment_id, reservation_id, guest_id,
            refund_amount, refund_reason, refund_status,
            refund_method, transaction_id, processed_at,
            notes, created_at, updated_at
        ) VALUES (
            gen_random_uuid(),
            payment_rec.id,
            payment_rec.reservation_id,
            payment_rec.guest_id,
            payment_rec.amount * (0.5 + (counter % 5) * 0.1),
            CASE (counter % 4)
                WHEN 0 THEN 'CANCELLATION'
                WHEN 1 THEN 'OVERBOOKING'
                WHEN 2 THEN 'GUEST_REQUEST'
                ELSE 'ERROR'
            END,
            CASE (counter % 3)
                WHEN 0 THEN 'PENDING'
                WHEN 1 THEN 'COMPLETED'
                ELSE 'FAILED'
            END,
            'ORIGINAL_METHOD',
            'REFUND' || LPAD(counter::TEXT, 12, '0'),
            CURRENT_TIMESTAMP - ((counter % 30) || ' days')::INTERVAL,
            'Refund processed successfully',
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        );
    END LOOP;
END $$;

-- Sample Financial Periods (24 periods - 2 years of monthly periods)
DO $$
DECLARE
    period_start DATE;
    period_end DATE;
    i INTEGER;
BEGIN
    FOR i IN 0..23 LOOP
        period_start := DATE_TRUNC('month', CURRENT_DATE - ((23 - i) || ' months')::INTERVAL);
        period_end := period_start + INTERVAL '1 month' - INTERVAL '1 day';

        INSERT INTO financial_periods (
            id, period_name, period_type, start_date, end_date,
            is_closed, closed_at, created_at, updated_at
        ) VALUES (
            gen_random_uuid(),
            TO_CHAR(period_start, 'YYYY-MM'),
            'MONTHLY',
            period_start,
            period_end,
            (i < 20),  -- Close older periods
            CASE WHEN i < 20 THEN period_end + INTERVAL '7 days' ELSE NULL END,
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        );
    END LOOP;
END $$;
