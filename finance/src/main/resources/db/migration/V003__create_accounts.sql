CREATE TYPE account_type AS ENUM (
    'CHECKING_ACCOUNT',
    'SAVINGS_ACCOUNT',
    'CASH_WALLET',
    'DIGITAL_ACCOUNT',
    'INVESTMENT',
    'MEAL_VOUCHER',
    'OTHER'
);

CREATE TABLE accounts (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name varchar(120) NOT NULL,
    type account_type NOT NULL,
    initial_balance numeric(15,2) NOT NULL DEFAULT 0,
    current_balance numeric(15,2) NOT NULL DEFAULT 0,
    archived boolean NOT NULL DEFAULT false,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX ix_accounts_user_id ON accounts (user_id);
CREATE INDEX ix_accounts_user_archived ON accounts (user_id, archived);
CREATE INDEX ix_accounts_user_type ON accounts (user_id, type);
