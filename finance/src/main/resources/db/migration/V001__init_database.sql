CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TYPE user_role AS ENUM ('USER', 'ADMIN');
CREATE TYPE user_status AS ENUM ('PENDING_APPROVAL', 'APPROVED', 'REJECTED', 'SUSPENDED', 'DELETED');
CREATE TYPE user_status_action AS ENUM ('REGISTERED', 'APPROVED', 'REJECTED', 'SUSPENDED', 'REACTIVATED', 'DELETED');

CREATE TABLE users (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name varchar(120) NOT NULL,
    email varchar(180) NOT NULL,
    password_hash varchar(255) NOT NULL,
    role user_role NOT NULL DEFAULT 'USER',
    status user_status NOT NULL DEFAULT 'PENDING_APPROVAL',
    approved_at timestamptz NULL,
    rejected_at timestamptz NULL,
    suspended_at timestamptz NULL,
    deleted_at timestamptz NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    active boolean NOT NULL DEFAULT true
);

CREATE UNIQUE INDEX ux_users_email ON users (email);
CREATE INDEX ix_users_status ON users (status);
CREATE INDEX ix_users_role ON users (role);

CREATE TABLE user_status_history (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    admin_user_id uuid NULL REFERENCES users (id) ON DELETE SET NULL,
    previous_status user_status NULL,
    new_status user_status NOT NULL,
    action user_status_action NOT NULL,
    reason text NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX ix_user_status_history_user_created_at ON user_status_history (user_id, created_at);
CREATE INDEX ix_user_status_history_admin_created_at ON user_status_history (admin_user_id, created_at);
CREATE INDEX ix_user_status_history_new_status ON user_status_history (new_status);

CREATE TABLE refresh_tokens (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash varchar(64) NOT NULL,
    expires_at timestamptz NOT NULL,
    revoked_at timestamptz NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX ux_refresh_tokens_token_hash ON refresh_tokens (token_hash);
CREATE INDEX ix_refresh_tokens_user_id ON refresh_tokens (user_id);

CREATE TABLE password_reset_tokens (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash varchar(64) NOT NULL,
    expires_at timestamptz NOT NULL,
    used_at timestamptz NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX ux_password_reset_tokens_token_hash ON password_reset_tokens (token_hash);
CREATE INDEX ix_password_reset_tokens_user_id ON password_reset_tokens (user_id);
