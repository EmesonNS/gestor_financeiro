CREATE TABLE budgets (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    category_id uuid NOT NULL REFERENCES categories (id) ON DELETE RESTRICT,
    start_month smallint NOT NULL,
    start_year smallint NOT NULL,
    end_month smallint,
    end_year smallint,
    limit_amount numeric(15,2) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ck_budgets_start_month_range CHECK (start_month between 1 and 12),
    CONSTRAINT ck_budgets_end_month_range CHECK (end_month IS NULL OR end_month between 1 and 12),
    CONSTRAINT ck_budgets_complete_end_period CHECK ((end_month IS NULL AND end_year IS NULL) OR (end_month IS NOT NULL AND end_year IS NOT NULL)),
    CONSTRAINT ck_budgets_ordered_period CHECK (end_year IS NULL OR (end_year > start_year OR (end_year = start_year AND end_month >= start_month))),
    CONSTRAINT ck_budgets_limit_amount_positive CHECK (limit_amount > 0)
);

CREATE INDEX ix_budgets_user_start_period ON budgets (user_id, start_year, start_month);
CREATE INDEX ix_budgets_user_category ON budgets (user_id, category_id);
CREATE INDEX ix_budgets_user_category_period ON budgets (user_id, category_id, start_year, start_month, end_year, end_month);
