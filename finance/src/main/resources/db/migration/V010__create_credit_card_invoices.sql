CREATE TYPE invoice_status AS ENUM ('OPEN', 'CLOSED', 'PAID', 'OVERDUE');

CREATE TABLE credit_card_invoices (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    credit_card_id uuid NOT NULL REFERENCES credit_cards (id) ON DELETE CASCADE,
    reference_month smallint NOT NULL,
    reference_year smallint NOT NULL,
    closing_date date NOT NULL,
    due_date date NOT NULL,
    total_amount numeric(15,2) NOT NULL DEFAULT 0,
    status invoice_status NOT NULL,
    paid_at date,
    payment_account_id uuid REFERENCES accounts (id),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ck_credit_card_invoices_reference_month_range CHECK (reference_month between 1 and 12),
    CONSTRAINT ck_credit_card_invoices_total_amount_non_negative CHECK (total_amount >= 0),
    CONSTRAINT uq_credit_card_invoices_card_reference UNIQUE (credit_card_id, reference_month, reference_year)
);

CREATE INDEX ix_credit_card_invoices_user_card ON credit_card_invoices (user_id, credit_card_id);
CREATE INDEX ix_credit_card_invoices_user_period ON credit_card_invoices (user_id, reference_year, reference_month);
CREATE INDEX ix_credit_card_invoices_user_status ON credit_card_invoices (user_id, status);
CREATE INDEX ix_credit_card_invoices_user_due_date ON credit_card_invoices (user_id, due_date);
