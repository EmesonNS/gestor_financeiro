CREATE TYPE purchase_status AS ENUM ('ACTIVE', 'CANCELED');
CREATE TYPE installment_status AS ENUM ('OPEN', 'PAID', 'CANCELED');

CREATE TABLE credit_card_purchases (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    credit_card_id uuid NOT NULL REFERENCES credit_cards (id) ON DELETE CASCADE,
    category_id uuid NOT NULL REFERENCES categories (id),
    description varchar(180) NOT NULL,
    total_amount numeric(15,2) NOT NULL,
    purchase_date date NOT NULL,
    installment_count integer NOT NULL,
    status purchase_status NOT NULL,
    notes text,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ck_credit_card_purchases_total_amount_positive CHECK (total_amount > 0),
    CONSTRAINT ck_credit_card_purchases_installment_count_positive CHECK (installment_count > 0)
);

CREATE INDEX ix_credit_card_purchases_user_card ON credit_card_purchases (user_id, credit_card_id);
CREATE INDEX ix_credit_card_purchases_user_purchase_date ON credit_card_purchases (user_id, purchase_date);
CREATE INDEX ix_credit_card_purchases_user_category ON credit_card_purchases (user_id, category_id);

CREATE TABLE credit_card_installments (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    purchase_id uuid NOT NULL REFERENCES credit_card_purchases (id) ON DELETE CASCADE,
    invoice_id uuid NOT NULL REFERENCES credit_card_invoices (id),
    installment_number integer NOT NULL,
    total_installments integer NOT NULL,
    amount numeric(15,2) NOT NULL,
    competence_month smallint NOT NULL,
    competence_year smallint NOT NULL,
    status installment_status NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ck_credit_card_installments_installment_number_positive CHECK (installment_number > 0),
    CONSTRAINT ck_credit_card_installments_total_installments_positive CHECK (total_installments > 0),
    CONSTRAINT ck_credit_card_installments_amount_positive CHECK (amount > 0),
    CONSTRAINT ck_credit_card_installments_competence_month_range CHECK (competence_month between 1 and 12),
    CONSTRAINT ck_credit_card_installments_number_lte_total CHECK (installment_number <= total_installments),
    CONSTRAINT uq_credit_card_installments_purchase_number UNIQUE (purchase_id, installment_number)
);

CREATE INDEX ix_credit_card_installments_user_invoice ON credit_card_installments (user_id, invoice_id);
CREATE INDEX ix_credit_card_installments_user_purchase ON credit_card_installments (user_id, purchase_id);
CREATE INDEX ix_credit_card_installments_user_competence ON credit_card_installments (user_id, competence_year, competence_month);
CREATE INDEX ix_credit_card_installments_user_status ON credit_card_installments (user_id, status);

CREATE TABLE credit_card_installment_constraint_usage (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    installment_id uuid,
    CONSTRAINT ck_credit_card_installments_number_lte_total
        FOREIGN KEY (installment_id) REFERENCES credit_card_installments (id)
);
