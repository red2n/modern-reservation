-- Sample Payments, Invoices, and Refunds
-- Creates payment records for reservations

DO $$
DECLARE
    v_system_tenant_id UUID := '00000000-0000-0000-0000-000000000001';
    v_system_user_id UUID := '00000000-0000-0000-0000-000000000002';
    v_property_ids UUID[];
    v_reservation_data RECORD;
    v_payment_id UUID;
    v_invoice_id UUID;
    v_gateway_ids UUID[];
    i INT := 0;
    payment_methods TEXT[] := ARRAY['credit_card', 'debit_card', 'digital_wallet', 'bank_transfer'];
    card_types TEXT[] := ARRAY['Visa', 'Mastercard', 'American Express', 'Discover'];
BEGIN
    -- Get property IDs
    SELECT ARRAY_AGG(id) INTO v_property_ids FROM properties WHERE tenant_id = v_system_tenant_id;

    -- Create payment gateways for each property
    FOR j IN 1..array_length(v_property_ids, 1) LOOP
        INSERT INTO payment_gateways (
            property_id, provider_name, is_active, is_primary,
            configuration, supports_credit_cards, supports_debit_cards,
            supports_ach, supports_digital_wallets, supported_currencies,
            transaction_fee_percent, transaction_fee_fixed,
            created_by, updated_by
        ) VALUES (
            v_property_ids[j],
            'Stripe',
            true, true,
            '{"api_key": "encrypted_key_here", "webhook_secret": "encrypted_secret"}'::jsonb,
            true, true, true, true,
            '["USD", "EUR", "GBP"]'::jsonb,
            0.0290, 0.30,
            v_system_user_id, v_system_user_id
        );
    END LOOP;

    RAISE NOTICE '% payment gateways created', (SELECT COUNT(*) FROM payment_gateways);

    -- Create payments and invoices for non-cancelled reservations
    FOR v_reservation_data IN
        SELECT r.id, r.confirmation_number, r.property_id, r.guest_id, r.total_amount,
               r.check_in_date, r.status, r.created_at
        FROM reservations r
        WHERE r.status != 'cancelled'
        ORDER BY r.created_at
        LIMIT 120
    LOOP
        i := i + 1;

        -- Create invoice
        INSERT INTO invoices (
            invoice_number, reservation_id, guest_id, property_id,
            invoice_date, due_date,
            subtotal, tax_amount, total_amount, currency,
            status, paid_amount, balance_due,
            created_by, updated_by
        ) VALUES (
            'INV-' || EXTRACT(YEAR FROM v_reservation_data.check_in_date) || '-' || LPAD(i::TEXT, 6, '0'),
            v_reservation_data.id,
            v_reservation_data.guest_id,
            v_reservation_data.property_id,
            v_reservation_data.check_in_date - INTERVAL '1 day',
            v_reservation_data.check_in_date,
            (v_reservation_data.total_amount / 1.0875)::DECIMAL(12,2),
            (v_reservation_data.total_amount - (v_reservation_data.total_amount / 1.0875))::DECIMAL(12,2),
            v_reservation_data.total_amount,
            'USD',
            CASE
                WHEN v_reservation_data.status IN ('checked_out') THEN 'paid'
                WHEN v_reservation_data.status = 'checked_in' THEN 'paid'
                WHEN v_reservation_data.status = 'confirmed' THEN
                    CASE WHEN i % 5 = 0 THEN 'sent' ELSE 'paid' END
                ELSE 'draft'
            END,
            CASE
                WHEN v_reservation_data.status IN ('checked_out', 'checked_in') THEN v_reservation_data.total_amount
                WHEN v_reservation_data.status = 'confirmed' AND i % 5 != 0 THEN v_reservation_data.total_amount
                ELSE 0.00
            END,
            CASE
                WHEN v_reservation_data.status IN ('checked_out', 'checked_in') THEN 0.00
                WHEN v_reservation_data.status = 'confirmed' AND i % 5 != 0 THEN 0.00
                ELSE v_reservation_data.total_amount
            END,
            v_system_user_id,
            v_system_user_id
        ) RETURNING id INTO v_invoice_id;

        -- Create invoice line items from reservation charges
        INSERT INTO invoice_line_items (
            invoice_id, line_number, description, quantity, unit_price,
            line_total, tax_rate, tax_amount, item_type, item_date
        )
        SELECT
            v_invoice_id,
            ROW_NUMBER() OVER (ORDER BY rc.charge_date, rc.created_at),
            rc.description,
            rc.quantity,
            rc.unit_price,
            rc.amount,
            rc.tax_rate,
            rc.tax_amount,
            rc.charge_type,
            rc.charge_date
        FROM reservation_charges rc
        WHERE rc.reservation_id = v_reservation_data.id;

        -- Create payment record if invoice is paid
        IF v_reservation_data.status IN ('checked_out', 'checked_in') OR
           (v_reservation_data.status = 'confirmed' AND i % 5 != 0) THEN

            INSERT INTO payments (
                reservation_id, guest_id, amount, currency,
                payment_method, payment_provider, transaction_id, status,
                description, reference,
                last_four_digits, card_type,
                card_holder_name, processed_at,
                billing_address, reconciled,
                created_by, updated_by
            ) VALUES (
                v_reservation_data.id,
                v_reservation_data.guest_id,
                v_reservation_data.total_amount,
                'USD',
                payment_methods[1 + (i % 4)]::payment_method,
                'Stripe',
                'ch_' || MD5(v_reservation_data.id::TEXT || random()::TEXT),
                'completed'::payment_status,
                'Payment for reservation ' || v_reservation_data.confirmation_number,
                v_reservation_data.confirmation_number,
                LPAD((1000 + i % 9000)::TEXT, 4, '0'),
                card_types[1 + (i % 4)],
                'Cardholder ' || i,
                v_reservation_data.check_in_date - INTERVAL '1 day' + (i % 24 || ' hours')::INTERVAL,
                FORMAT('{"street": "%s Main St", "city": "New York", "state": "NY", "postal_code": "10001"}', i)::jsonb,
                (v_reservation_data.status = 'checked_out'),
                v_system_user_id,
                v_system_user_id
            ) RETURNING id INTO v_payment_id;

            -- Create payment attempt record
            INSERT INTO payment_attempts (
                reservation_id, guest_id, amount, currency,
                payment_method, payment_provider,
                attempt_number, status, payment_id
            ) VALUES (
                v_reservation_data.id,
                v_reservation_data.guest_id,
                v_reservation_data.total_amount,
                'USD',
                payment_methods[1 + (i % 4)]::payment_method,
                'Stripe',
                1,
                'success',
                v_payment_id
            );
        END IF;

        -- Create refund for some cancelled-then-refunded or modified reservations
        IF i % 15 = 0 AND v_payment_id IS NOT NULL THEN
            INSERT INTO refunds (
                payment_id, amount, currency, reason,
                refund_method, status, processed_at,
                reference_number, reconciled,
                created_by, updated_by
            ) VALUES (
                v_payment_id,
                (v_reservation_data.total_amount * 0.50)::DECIMAL(12,2),
                'USD',
                'Partial refund due to service issue',
                'original_payment_method',
                'completed',
                CURRENT_TIMESTAMP - ((i % 30) || ' days')::INTERVAL,
                'REF-' || LPAD(i::TEXT, 8, '0'),
                true,
                v_system_user_id,
                v_system_user_id
            );
        END IF;
    END LOOP;

    -- Create financial periods for each property
    FOR j IN 1..array_length(v_property_ids, 1) LOOP
        -- Create last 12 months of financial periods
        FOR k IN 0..11 LOOP
            INSERT INTO financial_periods (
                property_id, period_name,
                start_date, end_date, is_closed,
                total_revenue, total_payments, total_refunds,
                created_by, updated_by
            ) VALUES (
                v_property_ids[j],
                TO_CHAR(CURRENT_DATE - (k || ' months')::INTERVAL, 'YYYY-MM'),
                DATE_TRUNC('month', CURRENT_DATE - (k || ' months')::INTERVAL)::DATE,
                (DATE_TRUNC('month', CURRENT_DATE - (k || ' months')::INTERVAL) + INTERVAL '1 month' - INTERVAL '1 day')::DATE,
                (k > 1),  -- Close periods older than 2 months
                (50000 + (j * 5000) - (k * 1000))::DECIMAL(12,2),
                (48000 + (j * 5000) - (k * 1000))::DECIMAL(12,2),
                (1000 + (k * 100))::DECIMAL(12,2),
                v_system_user_id,
                v_system_user_id
            );
        END LOOP;
    END LOOP;

    RAISE NOTICE 'Created % invoices', (SELECT COUNT(*) FROM invoices);
    RAISE NOTICE 'Created % payments', (SELECT COUNT(*) FROM payments);
    RAISE NOTICE 'Created % refunds', (SELECT COUNT(*) FROM refunds);
    RAISE NOTICE 'Created % financial periods', (SELECT COUNT(*) FROM financial_periods);
END $$;
