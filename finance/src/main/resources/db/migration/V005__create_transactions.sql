CREATE TYPE transaction_type AS ENUM ('INCOME', 'EXPENSE');
CREATE TYPE transaction_status AS ENUM ('PENDING', 'PAID', 'RECEIVED', 'CANCELED');

CREATE TABLE transactions (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    account_id uuid NULL REFERENCES accounts (id) ON DELETE SET NULL,
    category_id uuid NOT NULL REFERENCES categories (id) ON DELETE RESTRICT,
    description varchar(180) NOT NULL,
    type transaction_type NOT NULL,
    amount numeric(15,2) NOT NULL,
    transaction_date date NOT NULL,
    status transaction_status NOT NULL,
    notes text NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ck_transactions_amount_positive CHECK (amount > 0)
);

CREATE INDEX ix_transactions_user_transaction_date ON transactions (user_id, transaction_date);
CREATE INDEX ix_transactions_user_category ON transactions (user_id, category_id);
CREATE INDEX ix_transactions_user_account ON transactions (user_id, account_id);
CREATE INDEX ix_transactions_user_status ON transactions (user_id, status);
