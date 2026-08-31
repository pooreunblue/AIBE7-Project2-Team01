-- TalentPulse DB schema
-- 기준: docs/erd.md
-- 대상: PostgreSQL (Neon)

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =========================================================
-- USERS
-- =========================================================
CREATE TABLE users (
    user_id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email               VARCHAR(255) NOT NULL UNIQUE,
    nickname            VARCHAR(255) NOT NULL UNIQUE,
    password            VARCHAR(255),
    profile_image_path  VARCHAR(255),
    profile_image_url   VARCHAR(255),
    provider            VARCHAR(255) NOT NULL,
    role                VARCHAR(255) NOT NULL,
    created_at          TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT now()
);

-- =========================================================
-- CATEGORIES
-- =========================================================
CREATE TABLE categories (
    category_id  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name         VARCHAR(255) NOT NULL UNIQUE,
    description  VARCHAR(255),
    active       BOOLEAN
);

-- =========================================================
-- REQUEST_POSTS
-- =========================================================
CREATE TABLE request_posts (
    request_post_id  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID NOT NULL REFERENCES users (user_id),
    category_id      UUID NOT NULL REFERENCES categories (category_id),
    title            VARCHAR(255) NOT NULL,
    content          TEXT NOT NULL,
    budget_min       BIGINT NOT NULL,
    budget_max       BIGINT NOT NULL,
    due_date         DATE,
    status           VARCHAR(255) NOT NULL,
    ai_confidence    NUMERIC(38, 2),
    created_at       TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT now()
);

-- =========================================================
-- PORTFOLIOS
-- =========================================================
CREATE TABLE portfolios (
    portfolio_id  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL REFERENCES users (user_id),
    title         VARCHAR(100) NOT NULL,
    description   TEXT,
    created_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT now()
);

-- =========================================================
-- TALENT_POSTS
-- =========================================================
CREATE TABLE talent_posts (
    talent_post_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id              UUID NOT NULL REFERENCES users (user_id),
    category_id          UUID NOT NULL REFERENCES categories (category_id),
    portfolio_id         UUID NOT NULL REFERENCES portfolios (portfolio_id),
    title                VARCHAR(255) NOT NULL,
    content              TEXT NOT NULL,
    price                BIGINT NOT NULL,
    estimated_duration   INTEGER NOT NULL,
    duration_unit        VARCHAR(255) NOT NULL,
    status               VARCHAR(255) NOT NULL,
    ai_confidence        NUMERIC(38, 2),
    created_at           TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at           TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT now()
);

-- =========================================================
-- PORTFOLIO_FILES
-- =========================================================
CREATE TABLE portfolio_files (
    portfolio_file_id  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    portfolio_id       UUID NOT NULL REFERENCES portfolios (portfolio_id),
    original_file_name VARCHAR(255) NOT NULL,
    storage_path       VARCHAR(255) NOT NULL,
    file_url           VARCHAR(255) NOT NULL,
    content_type       VARCHAR(255) NOT NULL,
    file_size          BIGINT NOT NULL,
    thumbnail          BOOLEAN NOT NULL DEFAULT false,
    created_at         TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at         TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT now()
);

-- =========================================================
-- REQUEST_POST_FILES
-- =========================================================
CREATE TABLE request_post_files (
    request_post_file_id  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_post_id       UUID NOT NULL REFERENCES request_posts (request_post_id),
    original_file_name    VARCHAR(255) NOT NULL,
    storage_path          VARCHAR(255) NOT NULL,
    file_url              VARCHAR(255) NOT NULL,
    content_type          VARCHAR(255) NOT NULL,
    file_size             BIGINT NOT NULL,
    thumbnail             BOOLEAN NOT NULL DEFAULT false,
    created_at             TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at             TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT now()
);

-- =========================================================
-- TALENT_POST_FILES
-- =========================================================
CREATE TABLE talent_post_files (
    talent_post_file_id  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    talent_post_id       UUID NOT NULL REFERENCES talent_posts (talent_post_id),
    original_file_name   VARCHAR(255) NOT NULL,
    storage_path         VARCHAR(255) NOT NULL,
    file_url             VARCHAR(255) NOT NULL,
    content_type         VARCHAR(255) NOT NULL,
    file_size            BIGINT NOT NULL,
    thumbnail            BOOLEAN NOT NULL DEFAULT false,
    created_at            TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at            TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT now()
);

-- =========================================================
-- WALLETS
-- =========================================================
CREATE TABLE wallets (
    wallet_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL UNIQUE REFERENCES users (user_id),
    balance     NUMERIC(19, 2) NOT NULL DEFAULT 0,
    created_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT now()
);

-- =========================================================
-- CHAT_ROOMS
-- =========================================================
CREATE TABLE chat_rooms (
    chat_room_id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_post_id UUID,
    talent_post_id  UUID,
    created_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT now()
);

-- =========================================================
-- CHAT_PARTICIPANTS
-- =========================================================
CREATE TABLE chat_participants (
    chat_participant_id  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chat_room_id         UUID NOT NULL REFERENCES chat_rooms (chat_room_id),
    user_id              UUID NOT NULL REFERENCES users (user_id),
    joined_at            TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT uk_chat_participant_room_user UNIQUE (chat_room_id, user_id)
);

-- =========================================================
-- TRADES
-- =========================================================
CREATE TABLE trades (
    trade_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chat_room_id    UUID,
    request_post_id UUID,
    talent_post_id  UUID,
    payer_id        UUID NOT NULL REFERENCES users (user_id),
    payee_id        UUID NOT NULL REFERENCES users (user_id),
    amount          NUMERIC(19, 2) NOT NULL,
    status          VARCHAR(255) NOT NULL,
    paid_at         TIMESTAMP(6) WITH TIME ZONE,
    completed_at    TIMESTAMP(6) WITH TIME ZONE,
    cancelled_at    TIMESTAMP(6) WITH TIME ZONE,
    created_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT ck_trade_single_post CHECK (
        (request_post_id IS NOT NULL AND talent_post_id IS NULL)
        OR (request_post_id IS NULL AND talent_post_id IS NOT NULL)
    ),
    CONSTRAINT ck_trade_positive_amount CHECK (amount > 0),
    CONSTRAINT ck_trade_distinct_parties CHECK (payer_id <> payee_id)
);

-- 같은 채팅방에서는 PENDING 또는 PAID 거래를 동시에 하나만 유지한다.
CREATE UNIQUE INDEX uk_trade_active_chat_room
    ON trades (chat_room_id)
    WHERE chat_room_id IS NOT NULL AND status IN ('PENDING', 'PAID');

-- 1회성 요청글은 취소되지 않은 결제 거래를 하나만 가질 수 있다.
CREATE UNIQUE INDEX uk_trade_paid_request_post
    ON trades (request_post_id)
    WHERE request_post_id IS NOT NULL AND status IN ('PAID', 'COMPLETED');

-- =========================================================
-- CHAT_MESSAGES
-- =========================================================
CREATE TABLE chat_messages (
    chat_message_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chat_room_id    UUID NOT NULL REFERENCES chat_rooms (chat_room_id),
    user_id         UUID NOT NULL REFERENCES users (user_id),
    trade_id        UUID REFERENCES trades (trade_id),
    content         TEXT NOT NULL,
    message_type    VARCHAR(255) NOT NULL,
    attachment_path VARCHAR(255),
    created_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT now()
);

-- =========================================================
-- WALLET_TRANSACTIONS
-- =========================================================
CREATE TABLE wallet_transactions (
    wallet_transaction_id  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id              UUID NOT NULL REFERENCES wallets (wallet_id),
    trade_id               UUID REFERENCES trades (trade_id),
    transaction_type       VARCHAR(255) NOT NULL,
    amount                 NUMERIC(19, 2) NOT NULL,
    balance_after          NUMERIC(19, 2) NOT NULL,
    description            VARCHAR(255),
    created_at             TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT uk_wallet_transaction_trade_type UNIQUE (trade_id, transaction_type)
);
