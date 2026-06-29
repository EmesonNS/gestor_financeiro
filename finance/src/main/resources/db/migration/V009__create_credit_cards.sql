CREATE TABLE credit_cards (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name varchar(120) NOT NULL,
    limit_amount numeric(15,2) NOT NULL,
    closing_day smallint NOT NULL,
    due_day smallint NOT NULL,
    archived boolean NOT NULL DEFAULT false,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ck_credit_cards_limit_amount_non_negative CHECK (limit_amount >= 0),
    CONSTRAINT ck_credit_cards_closing_day_range CHECK (closing_day between 1 and 31),
    CONSTRAINT ck_credit_cards_due_day_range CHECK (due_day between 1 and 31)
);

CREATE INDEX ix_credit_cards_user ON credit_cards (user_id);
CREATE INDEX ix_credit_cards_user_archived ON credit_cards (user_id, archived);
