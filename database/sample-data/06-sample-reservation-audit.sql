-- Sample Data for Reservation Audit History
-- Generates 200 audit records for status changes

INSERT INTO reservation_status_history (
    id, reservation_id, old_status, new_status,
    reason, notes, changed_by, changed_at
)
SELECT
    gen_random_uuid(),
    -- Reference actual reservations (get first 100)
    (SELECT id FROM reservations ORDER BY booking_date LIMIT 1 OFFSET (s % 100)) AS reservation_id,
    -- Old status (previous state)
    (ARRAY['PENDING', 'PENDING', 'CONFIRMED', 'CONFIRMED', 'CONFIRMED',
           'CHECKED_IN', 'ON_HOLD', 'MODIFIED'])[1 + (s % 8)]::reservation_status AS old_status,
    -- New status (next state in typical flow)
    CASE (ARRAY['PENDING', 'PENDING', 'CONFIRMED', 'CONFIRMED', 'CONFIRMED',
           'CHECKED_IN', 'ON_HOLD', 'MODIFIED'])[1 + (s % 8)]::reservation_status
        WHEN 'PENDING' THEN (ARRAY['CONFIRMED', 'CANCELLED', 'ON_HOLD'])[1 + (s % 3)]
        WHEN 'CONFIRMED' THEN (ARRAY['CHECKED_IN', 'CANCELLED', 'MODIFIED', 'NO_SHOW'])[1 + (s % 4)]
        WHEN 'CHECKED_IN' THEN 'CHECKED_OUT'
        WHEN 'ON_HOLD' THEN (ARRAY['CONFIRMED', 'CANCELLED'])[1 + (s % 2)]
        WHEN 'MODIFIED' THEN 'CONFIRMED'
        ELSE 'CONFIRMED'
    END::reservation_status AS new_status,
    -- Reason for change
    CASE
        WHEN (ARRAY['PENDING', 'PENDING', 'CONFIRMED', 'CONFIRMED', 'CONFIRMED',
           'CHECKED_IN', 'ON_HOLD', 'MODIFIED'])[1 + (s % 8)] = 'PENDING' THEN
            (ARRAY['Payment received', 'Customer cancelled', 'Awaiting payment', 'Credit card authorized'])[1 + (s % 4)]
        WHEN (ARRAY['PENDING', 'PENDING', 'CONFIRMED', 'CONFIRMED', 'CONFIRMED',
           'CHECKED_IN', 'ON_HOLD', 'MODIFIED'])[1 + (s % 8)] = 'CONFIRMED' THEN
            (ARRAY['Guest arrived', 'Customer requested cancellation', 'Dates changed', 'Guest no-show'])[1 + (s % 4)]
        WHEN (ARRAY['PENDING', 'PENDING', 'CONFIRMED', 'CONFIRMED', 'CONFIRMED',
           'CHECKED_IN', 'ON_HOLD', 'MODIFIED'])[1 + (s % 8)] = 'CHECKED_IN' THEN
            'Guest completed stay'
        ELSE 'Status updated by system'
    END AS reason,
    -- Additional notes
    CASE WHEN s % 3 = 0 THEN
        (ARRAY['VIP guest - special handling', 'Late arrival noted', 'Early departure arranged',
               'Room upgrade provided', 'Complaint resolved', 'Special request fulfilled',
               'Payment plan arranged', 'Group booking adjustment', 'Weather-related change',
               'System migration update'])[1 + (s % 10)]
    ELSE NULL END AS notes,
    -- Changed by (user ID)
    CASE WHEN s % 4 = 0 THEN NULL
    ELSE gen_random_uuid()
    END AS changed_by,
    -- Changed at (historical)
    CURRENT_TIMESTAMP - ((s * 12) || ' hours')::interval AS changed_at
FROM generate_series(1, 200) AS s;

-- Verify the insert
SELECT COUNT(*) as total_audit_records FROM reservation_status_history;
SELECT old_status, new_status, COUNT(*)
FROM reservation_status_history
GROUP BY old_status, new_status
ORDER BY COUNT(*) DESC
LIMIT 10;
