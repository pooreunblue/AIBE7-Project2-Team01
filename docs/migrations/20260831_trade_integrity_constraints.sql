-- Existing PostgreSQL databases: trade/wallet duplicate-prevention constraints.
-- Run duplicate checks first. Resolve any returned rows before applying constraints.

-- Duplicate participants in one chat room.
SELECT chat_room_id, user_id, COUNT(*)
FROM chat_participants
GROUP BY chat_room_id, user_id
HAVING COUNT(*) > 1;

-- Multiple active trades in one chat room.
SELECT chat_room_id, COUNT(*)
FROM trades
WHERE chat_room_id IS NOT NULL
  AND status IN ('PENDING', 'PAID')
GROUP BY chat_room_id
HAVING COUNT(*) > 1;

-- Multiple paid or completed trades for one-time request posts.
SELECT request_post_id, COUNT(*)
FROM trades
WHERE request_post_id IS NOT NULL
  AND status IN ('PAID', 'COMPLETED')
GROUP BY request_post_id
HAVING COUNT(*) > 1;

-- Duplicate wallet transaction types for one trade.
SELECT trade_id, transaction_type, COUNT(*)
FROM wallet_transactions
WHERE trade_id IS NOT NULL
GROUP BY trade_id, transaction_type
HAVING COUNT(*) > 1;

-- Stop here and clean up data when any duplicate-check query returns rows.

BEGIN;

CREATE UNIQUE INDEX IF NOT EXISTS uk_chat_participant_room_user
    ON chat_participants (chat_room_id, user_id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_trade_active_chat_room
    ON trades (chat_room_id)
    WHERE chat_room_id IS NOT NULL AND status IN ('PENDING', 'PAID');

CREATE UNIQUE INDEX IF NOT EXISTS uk_trade_paid_request_post
    ON trades (request_post_id)
    WHERE request_post_id IS NOT NULL AND status IN ('PAID', 'COMPLETED');

CREATE UNIQUE INDEX IF NOT EXISTS uk_wallet_transaction_trade_type
    ON wallet_transactions (trade_id, transaction_type)
    WHERE trade_id IS NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_trade_single_post'
    ) THEN
        ALTER TABLE trades
            ADD CONSTRAINT ck_trade_single_post CHECK (
                (request_post_id IS NOT NULL AND talent_post_id IS NULL)
                OR (request_post_id IS NULL AND talent_post_id IS NOT NULL)
            );
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_trade_positive_amount'
    ) THEN
        ALTER TABLE trades
            ADD CONSTRAINT ck_trade_positive_amount CHECK (amount > 0);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_trade_distinct_parties'
    ) THEN
        ALTER TABLE trades
            ADD CONSTRAINT ck_trade_distinct_parties CHECK (payer_id <> payee_id);
    END IF;
END
$$;

COMMIT;
