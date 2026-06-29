CREATE TYPE goal_status AS ENUM ('ACTIVE', 'COMPLETED', 'CANCELED');

CREATE TABLE goals (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name varchar(120) NOT NULL,
    target_amount numeric(15,2) NOT NULL,
    current_amount numeric(15,2) NOT NULL,
    deadline date,
    description text,
    status goal_status NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ck_goals_target_amount_positive CHECK (target_amount > 0),
    CONSTRAINT ck_goals_current_amount_non_negative CHECK (current_amount >= 0)
);

CREATE INDEX ix_goals_user ON goals (user_id);
CREATE INDEX ix_goals_user_status ON goals (user_id, status);
CREATE INDEX ix_goals_user_deadline ON goals (user_id, deadline);
