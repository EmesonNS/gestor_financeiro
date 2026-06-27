# Modelo de Dados PostgreSQL

## Regras gerais

- Usar UUID como chave primária.
- Usar `numeric(15,2)` para dinheiro.
- Usar `date` para datas financeiras.
- Usar `timestamp with time zone` para timestamps técnicos.
- Todas as tabelas financeiras possuem `user_id`.
- Criar índice para `user_id` e datas filtráveis.
- Criar índices compostos como `(user_id, transaction_date)`, `(user_id, category_id)`, `(user_id, account_id)`.
- Usar `check constraints` para tipos e status ou enums PostgreSQL versionados.
- Não permitir dados financeiros sem usuário dono.
- Usar arquivamento ou soft delete para entidades importantes.

## Enums sugeridos

- `account_type`: `CHECKING_ACCOUNT`, `SAVINGS_ACCOUNT`, `CASH_WALLET`, `DIGITAL_ACCOUNT`, `INVESTMENT`, `MEAL_VOUCHER`, `OTHER`.
- `category_type`: `INCOME`, `EXPENSE`.
- `transaction_type`: `INCOME`, `EXPENSE`.
- `transaction_status`: `PENDING`, `PAID`, `RECEIVED`, `CANCELED`.
- `bill_status`: `PENDING`, `PAID`, `OVERDUE`, `CANCELED`.
- `recurrence_frequency`: `MONTHLY`.
- `goal_status`: `ACTIVE`, `COMPLETED`, `CANCELED`.
- `invoice_status`: `OPEN`, `CLOSED`, `PAID`, `OVERDUE`.
- `purchase_status`: `ACTIVE`, `CANCELED`.
- `installment_status`: `OPEN`, `PAID`, `CANCELED`.
- `user_role`: `USER`, `ADMIN`.
- `user_status`: `PENDING_APPROVAL`, `APPROVED`, `REJECTED`, `SUSPENDED`, `DELETED`.
- `user_status_action`: `REGISTERED`, `APPROVED`, `REJECTED`, `SUSPENDED`, `REACTIVATED`, `DELETED`.

## users

Descrição: usuários do sistema.

| Campo | Tipo | Regras |
|---|---|---|
| id | uuid | PK |
| name | varchar(120) | not null |
| email | varchar(180) | not null, unique |
| password_hash | varchar(255) | not null |
| role | user_role | not null default USER |
| status | user_status | not null default PENDING_APPROVAL |
| approved_at | timestamptz | nullable |
| rejected_at | timestamptz | nullable |
| suspended_at | timestamptz | nullable |
| deleted_at | timestamptz | nullable |
| created_at | timestamptz | not null |
| updated_at | timestamptz | not null |
| active | boolean | not null default true |

Índices:

- `ux_users_email` unique em `email`.
- `ix_users_status` em `status`.
- `ix_users_role` em `role`.

Observações:

- Cadastro público cria usuário com `status = PENDING_APPROVAL`.
- Apenas usuários `APPROVED` podem acessar o sistema financeiro.
- Admins devem possuir `role = ADMIN`.
- Suspensão, negação e exclusão/desativação bloqueiam login normal.

## user_status_history

Descrição: auditoria de decisões administrativas sobre usuários.

| Campo | Tipo | Regras |
|---|---|---|
| id | uuid | PK |
| user_id | uuid | FK users(id), not null |
| admin_user_id | uuid | FK users(id), nullable para evento automático de cadastro |
| previous_status | user_status | nullable |
| new_status | user_status | not null |
| action | user_status_action | not null |
| reason | text | nullable |
| created_at | timestamptz | not null |

Índices: `(user_id, created_at)`, `(admin_user_id, created_at)`, `(new_status)`.

Observações: toda aprovação, negação, suspensão, reativação e exclusão/desativação deve gerar registro.

## accounts

Descrição: contas financeiras do usuário.

| Campo | Tipo | Regras |
|---|---|---|
| id | uuid | PK |
| user_id | uuid | FK users(id), not null |
| name | varchar(120) | not null |
| type | account_type | not null |
| initial_balance | numeric(15,2) | not null default 0 |
| current_balance | numeric(15,2) | not null default 0 |
| archived | boolean | not null default false |
| created_at | timestamptz | not null |
| updated_at | timestamptz | not null |

Índices: `(user_id)`, `(user_id, archived)`, `(user_id, type)`.

Observações: saldo atual deve ser atualizado por transações realizadas e pagamento de faturas.

## categories

Descrição: categorias padrão ou personalizadas por usuário.

| Campo | Tipo | Regras |
|---|---|---|
| id | uuid | PK |
| user_id | uuid | FK users(id), nullable para padrão global |
| name | varchar(100) | not null |
| type | category_type | not null |
| color | varchar(20) | nullable |
| icon | varchar(80) | nullable |
| is_default | boolean | not null default false |
| created_at | timestamptz | not null |
| updated_at | timestamptz | not null |

Índices: `(user_id)`, `(user_id, type)`, `(is_default, type)`.

Constraints: nome único por usuário e tipo; categorias padrão podem ter `user_id` nulo.

## transactions

Descrição: receitas e despesas.

| Campo | Tipo | Regras |
|---|---|---|
| id | uuid | PK |
| user_id | uuid | FK users(id), not null |
| account_id | uuid | FK accounts(id), nullable quando pendente sem conta |
| category_id | uuid | FK categories(id), not null |
| description | varchar(180) | not null |
| type | transaction_type | not null |
| amount | numeric(15,2) | not null, > 0 |
| transaction_date | date | not null |
| status | transaction_status | not null |
| notes | text | nullable |
| created_at | timestamptz | not null |
| updated_at | timestamptz | not null |

Índices: `(user_id, transaction_date)`, `(user_id, category_id)`, `(user_id, account_id)`, `(user_id, status)`.

Observações: transações pagas/recebidas exigem conta e impactam saldo.

## bills

Descrição: contas a pagar.

| Campo | Tipo | Regras |
|---|---|---|
| id | uuid | PK |
| user_id | uuid | FK users(id), not null |
| account_id | uuid | FK accounts(id), nullable até pagamento |
| category_id | uuid | FK categories(id), not null |
| transaction_id | uuid | FK transactions(id), nullable |
| description | varchar(180) | not null |
| amount | numeric(15,2) | not null, > 0 |
| due_date | date | not null |
| paid_at | date | nullable |
| status | bill_status | not null |
| recurrence_id | uuid | FK recurrences(id), nullable |
| created_at | timestamptz | not null |
| updated_at | timestamptz | not null |

Índices: `(user_id, due_date)`, `(user_id, status)`, `(user_id, category_id)`.

Observações: ao pagar, criar ou vincular despesa em `transactions`.

## recurrences

Descrição: regras de despesas recorrentes.

| Campo | Tipo | Regras |
|---|---|---|
| id | uuid | PK |
| user_id | uuid | FK users(id), not null |
| description | varchar(180) | not null |
| amount | numeric(15,2) | not null, > 0 |
| category_id | uuid | FK categories(id), not null |
| account_id | uuid | FK accounts(id), nullable |
| frequency | recurrence_frequency | not null |
| start_date | date | not null |
| end_date | date | nullable |
| active | boolean | not null default true |
| created_at | timestamptz | not null |
| updated_at | timestamptz | not null |

Índices: `(user_id, active)`, `(user_id, start_date)`.

## budgets

Descrição: orçamento mensal por categoria.

| Campo | Tipo | Regras |
|---|---|---|
| id | uuid | PK |
| user_id | uuid | FK users(id), not null |
| category_id | uuid | FK categories(id), not null |
| month | smallint | not null, 1..12 |
| year | smallint | not null |
| limit_amount | numeric(15,2) | not null, > 0 |
| created_at | timestamptz | not null |
| updated_at | timestamptz | not null |

Índices: `(user_id, year, month)`, `(user_id, category_id)`.

Constraint: único por usuário, categoria, mês e ano.

## goals

Descrição: metas financeiras.

| Campo | Tipo | Regras |
|---|---|---|
| id | uuid | PK |
| user_id | uuid | FK users(id), not null |
| name | varchar(120) | not null |
| target_amount | numeric(15,2) | not null, > 0 |
| current_amount | numeric(15,2) | not null, >= 0 |
| deadline | date | nullable |
| description | text | nullable |
| status | goal_status | not null |
| created_at | timestamptz | not null |
| updated_at | timestamptz | not null |

Índices: `(user_id, status)`, `(user_id, deadline)`.

## credit_cards

Descrição: cartões de crédito do usuário.

| Campo | Tipo | Regras |
|---|---|---|
| id | uuid | PK |
| user_id | uuid | FK users(id), not null |
| name | varchar(120) | not null |
| limit_amount | numeric(15,2) | not null, >= 0 |
| closing_day | smallint | not null, 1..31 |
| due_day | smallint | not null, 1..31 |
| archived | boolean | not null default false |
| created_at | timestamptz | not null |
| updated_at | timestamptz | not null |

Índices: `(user_id)`, `(user_id, archived)`.

## credit_card_invoices

Descrição: faturas de cartão por competência.

| Campo | Tipo | Regras |
|---|---|---|
| id | uuid | PK |
| user_id | uuid | FK users(id), not null |
| credit_card_id | uuid | FK credit_cards(id), not null |
| reference_month | smallint | not null, 1..12 |
| reference_year | smallint | not null |
| closing_date | date | not null |
| due_date | date | not null |
| total_amount | numeric(15,2) | not null default 0 |
| status | invoice_status | not null |
| paid_at | date | nullable |
| payment_account_id | uuid | FK accounts(id), nullable |
| created_at | timestamptz | not null |
| updated_at | timestamptz | not null |

Índices: `(user_id, credit_card_id)`, `(user_id, reference_year, reference_month)`, `(user_id, status)`, `(user_id, due_date)`.

Constraint: única por cartão, mês e ano.

## credit_card_purchases

Descrição: compra original no cartão, à vista ou parcelada.

| Campo | Tipo | Regras |
|---|---|---|
| id | uuid | PK |
| user_id | uuid | FK users(id), not null |
| credit_card_id | uuid | FK credit_cards(id), not null |
| category_id | uuid | FK categories(id), not null |
| description | varchar(180) | not null |
| total_amount | numeric(15,2) | not null, > 0 |
| purchase_date | date | not null |
| installment_count | integer | not null, > 0 |
| status | purchase_status | not null |
| notes | text | nullable |
| created_at | timestamptz | not null |
| updated_at | timestamptz | not null |

Índices: `(user_id, credit_card_id)`, `(user_id, purchase_date)`, `(user_id, category_id)`.

## credit_card_installments

Descrição: parcelas geradas para compras no cartão.

| Campo | Tipo | Regras |
|---|---|---|
| id | uuid | PK |
| user_id | uuid | FK users(id), not null |
| purchase_id | uuid | FK credit_card_purchases(id), not null |
| invoice_id | uuid | FK credit_card_invoices(id), not null |
| installment_number | integer | not null, > 0 |
| total_installments | integer | not null, > 0 |
| amount | numeric(15,2) | not null, > 0 |
| competence_month | smallint | not null, 1..12 |
| competence_year | smallint | not null |
| status | installment_status | not null |
| created_at | timestamptz | not null |
| updated_at | timestamptz | not null |

Índices: `(user_id, invoice_id)`, `(user_id, purchase_id)`, `(user_id, competence_year, competence_month)`, `(user_id, status)`.

Constraints:

- Única por `purchase_id` e `installment_number`.
- `installment_number <= total_installments`.

Observações: parcelas de fatura paga não podem ser alteradas por edição/cancelamento de compra.
