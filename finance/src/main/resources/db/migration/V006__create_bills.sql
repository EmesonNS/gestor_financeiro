CREATE TYPE bill_status AS ENUM ('PENDING', 'PAID', 'OVERDUE', 'CANCELED');

CREATE TABLE bills (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    account_id uuid NULL REFERENCES accounts (id) ON DELETE SET NULL,
    category_id uuid NOT NULL REFERENCES categories (id) ON DELETE RESTRICT,
    transaction_id uuid NULL REFERENCES transactions (id) ON DELETE SET NULL,
    description varchar(180) NOT NULL,
    amount numeric(15,2) NOT NULL,
    due_date date NOT NULL,
    paid_at date NULL,
    status bill_status NOT NULL,
    recurrence_id uuid NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ck_bills_amount_positive CHECK (amount > 0)
);

CREATE INDEX ix_bills_user_due_date ON bills (user_id, due_date);
CREATE INDEX ix_bills_user_status ON bills (user_id, status);
CREATE INDEX ix_bills_user_category ON bills (user_id, category_id);
