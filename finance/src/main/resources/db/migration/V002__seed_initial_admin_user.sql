INSERT INTO users (
    name,
    email,
    password_hash,
    role,
    status,
    approved_at,
    created_at,
    updated_at,
    active
)
SELECT
    '${firstAdminName}',
    '${firstAdminEmail}',
    '${firstAdminPasswordHash}',
    'ADMIN',
    'APPROVED',
    now(),
    now(),
    now(),
    true
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE role = 'ADMIN'
)
ON CONFLICT (email) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    role = 'ADMIN',
    status = 'APPROVED',
    approved_at = COALESCE(users.approved_at, now()),
    deleted_at = NULL,
    active = true,
    updated_at = now();
