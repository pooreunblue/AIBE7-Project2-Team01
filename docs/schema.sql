-- TalentPulse DB schema
-- ERD(mermaid)에 정의된 테이블/컬럼/관계 그대로 반영. 임의로 컬럼을 추가/삭제/변경하지 않음.
-- 대상: PostgreSQL (Neon)

-- =========================================================
-- USERS
-- =========================================================
CREATE TABLE users (
    user_id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email               VARCHAR NOT NULL UNIQUE,
    password            VARCHAR NOT NULL,
    nickname            VARCHAR NOT NULL,
    profile_image_url  VARCHAR,
    status              VARCHAR,
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP NOT NULL DEFAULT now()
);

-- =========================================================
-- CATEGORIES
-- =========================================================
CREATE TABLE categories (
    category_id  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name          VARCHAR NOT NULL UNIQUE,
    description   VARCHAR,
    active        BOOLEAN,
    created_at    TIMESTAMP NOT NULL DEFAULT now()
);

-- =========================================================
-- REQUEST_POSTS
-- =========================================================
CREATE TABLE request_posts (
    request_post_id  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id           BIGINT NOT NULL REFERENCES users (user_id),
    category_id       BIGINT NOT NULL REFERENCES categories (category_id),
    title             VARCHAR NOT NULL,
    content           TEXT,
    budget_min        NUMERIC,
    budget_max        NUMERIC,
    status            VARCHAR,
    category_source   VARCHAR,
    ai_confidence     NUMERIC,
    created_at        TIMESTAMP NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP NOT NULL DEFAULT now()
);

-- =========================================================
-- TALENT_POSTS
-- =========================================================
CREATE TABLE talent_posts (
    talent_post_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id           BIGINT NOT NULL REFERENCES users (user_id),
    category_id       BIGINT NOT NULL REFERENCES categories (category_id),
    title             VARCHAR NOT NULL,
    content           TEXT,
    price             NUMERIC,
    status            VARCHAR,
    category_source   VARCHAR,
    ai_confidence     NUMERIC,
    created_at        TIMESTAMP NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP NOT NULL DEFAULT now()
);

-- =========================================================
-- PORTFOLIOS
-- =========================================================
CREATE TABLE portfolios (
    portfolio_id  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id        BIGINT NOT NULL REFERENCES users (user_id),
    title          VARCHAR NOT NULL,
    description    TEXT,
    created_at     TIMESTAMP NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP NOT NULL DEFAULT now()
);

-- =========================================================
-- PORTFOLIO_FILES
-- =========================================================
CREATE TABLE portfolio_files (
    portfolio_file_id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    portfolio_id          BIGINT NOT NULL REFERENCES portfolios (portfolio_id),
    original_file_name    VARCHAR NOT NULL,
    stored_file_name      VARCHAR NOT NULL,
    file_url              VARCHAR NOT NULL,
    content_type          VARCHAR,
    file_size             BIGINT,
    created_at             TIMESTAMP NOT NULL DEFAULT now()
);

-- =========================================================
-- WALLETS
-- =========================================================
CREATE TABLE wallets (
    wallet_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id      BIGINT NOT NULL UNIQUE REFERENCES users (user_id),
    balance      NUMERIC NOT NULL DEFAULT 0,
    created_at   TIMESTAMP NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP NOT NULL DEFAULT now()
);

-- =========================================================
-- CHAT_ROOMS
-- =========================================================
CREATE TABLE chat_rooms (
    chat_room_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    request_post_id   BIGINT REFERENCES request_posts (request_post_id),
    talent_post_id    BIGINT REFERENCES talent_posts (talent_post_id),
    created_at        TIMESTAMP NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP NOT NULL DEFAULT now()
);

-- =========================================================
-- CHAT_PARTICIPANTS
-- =========================================================
CREATE TABLE chat_participants (
    chat_participant_id  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    chat_room_id          BIGINT NOT NULL REFERENCES chat_rooms (chat_room_id),
    user_id                BIGINT NOT NULL REFERENCES users (user_id),
    joined_at              TIMESTAMP NOT NULL DEFAULT now()
);

-- =========================================================
-- CHAT_MESSAGES
-- =========================================================
CREATE TABLE chat_messages (
    chat_message_id  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    chat_room_id      BIGINT NOT NULL REFERENCES chat_rooms (chat_room_id),
    user_id            BIGINT NOT NULL REFERENCES users (user_id),
    content            TEXT NOT NULL,
    message_type       VARCHAR NOT NULL,
    created_at         TIMESTAMP NOT NULL DEFAULT now()
);

-- =========================================================
-- TRADES
-- =========================================================
CREATE TABLE trades (
    trade_id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    chat_room_id       BIGINT REFERENCES chat_rooms (chat_room_id),
    request_post_id    BIGINT REFERENCES request_posts (request_post_id),
    talent_post_id     BIGINT REFERENCES talent_posts (talent_post_id),
    payer_id           BIGINT NOT NULL REFERENCES users (user_id),
    payee_id           BIGINT NOT NULL REFERENCES users (user_id),
    amount             NUMERIC NOT NULL,
    status             VARCHAR,
    paid_at            TIMESTAMP,
    completed_at       TIMESTAMP,
    cancelled_at       TIMESTAMP,
    created_at         TIMESTAMP NOT NULL DEFAULT now()
);

-- =========================================================
-- WALLET_TRANSACTIONS
-- =========================================================
CREATE TABLE wallet_transactions (
    wallet_transaction_id  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    wallet_id               BIGINT NOT NULL REFERENCES wallets (wallet_id),
    trade_id                 BIGINT REFERENCES trades (trade_id),
    transaction_type        VARCHAR NOT NULL,
    amount                   NUMERIC NOT NULL,
    balance_after            NUMERIC,
    description              VARCHAR,
    created_at                TIMESTAMP NOT NULL DEFAULT now()
);

-- =========================================================
-- REVIEWS
-- =========================================================
CREATE TABLE reviews (
    review_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    trade_id       BIGINT NOT NULL REFERENCES trades (trade_id),
    reviewer_id    BIGINT NOT NULL REFERENCES users (user_id),
    reviewee_id    BIGINT NOT NULL REFERENCES users (user_id),
    rating         INTEGER NOT NULL,
    content        TEXT,
    created_at     TIMESTAMP NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP NOT NULL DEFAULT now()
);
