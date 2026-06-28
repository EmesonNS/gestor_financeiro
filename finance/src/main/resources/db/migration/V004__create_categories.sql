CREATE TYPE category_type AS ENUM ('INCOME', 'EXPENSE');

CREATE TABLE categories (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NULL REFERENCES users (id) ON DELETE CASCADE,
    name varchar(100) NOT NULL,
    type category_type NOT NULL,
    color varchar(20) NULL,
    icon varchar(80) NULL,
    is_default boolean NOT NULL DEFAULT false,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX ix_categories_user_id ON categories (user_id);
CREATE INDEX ix_categories_user_type ON categories (user_id, type);
CREATE INDEX ix_categories_default_type ON categories (is_default, type);
CREATE UNIQUE INDEX ux_categories_user_name_type ON categories (user_id, name, type);
CREATE UNIQUE INDEX ux_categories_default_name_type ON categories (name, type) WHERE user_id IS NULL;

INSERT INTO categories (name, type, color, icon, is_default) VALUES
    ('Salario', 'INCOME', '#16a34a', 'wallet', true),
    ('Freelance', 'INCOME', '#0ea5e9', 'briefcase', true),
    ('Investimentos', 'INCOME', '#6366f1', 'trending-up', true),
    ('Alimentacao', 'EXPENSE', '#f97316', 'utensils', true),
    ('Moradia', 'EXPENSE', '#8b5cf6', 'home', true),
    ('Transporte', 'EXPENSE', '#0f766e', 'car', true),
    ('Saude', 'EXPENSE', '#dc2626', 'heart-pulse', true),
    ('Lazer', 'EXPENSE', '#db2777', 'party-popper', true);
