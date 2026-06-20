# Contrato REST da API

## Convenções gerais

- Base path: `/api`.
- Autenticação privada: `Authorization: Bearer <accessToken>`.
- Content-Type: `application/json`.
- IDs públicos: UUID.
- Datas financeiras: `YYYY-MM-DD`.
- Timestamps: ISO 8601 UTC/offset.
- Dinheiro: decimal com duas casas.
- Listagens devem suportar `page`, `size` e `sort` quando aplicável.
- Todas as regras de autorização usam o usuário autenticado do token.
- Recurso inexistente ou pertencente a outro usuário retorna 404 ou 403 conforme política final, com preferência por 404 para reduzir enumeração.

## Erro padrão

```json
{
  "timestamp": "2026-01-01T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Valor deve ser maior que zero",
  "path": "/api/transactions",
  "details": []
}
```

## Modelo paginado

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0
}
```

## Auth

### POST `/api/auth/register`

- Descrição: cadastra usuário.
- Auth: não.
- Body: `{ "name": "Maria", "email": "maria@email.com", "password": "secret" }`.
- Response 201: `{ "id": "uuid", "name": "Maria", "email": "maria@email.com" }`.
- Erros: 400, 409.
- Autorização: e-mail deve ser único.

### POST `/api/auth/login`

- Descrição: autentica usuário.
- Auth: não.
- Body: `{ "email": "maria@email.com", "password": "secret" }`.
- Response 200: `{ "accessToken": "jwt", "refreshToken": "token", "expiresIn": 900 }`.
- Erros: 400, 401.

### POST `/api/auth/logout`

- Descrição: invalida refresh token.
- Auth: sim.
- Body: `{ "refreshToken": "token" }`.
- Response 204.
- Erros: 401.

### POST `/api/auth/refresh`

- Descrição: renova access token.
- Auth: não.
- Body: `{ "refreshToken": "token" }`.
- Response 200: `{ "accessToken": "jwt", "refreshToken": "new-token", "expiresIn": 900 }`.
- Erros: 401.

### POST `/api/auth/forgot-password`

- Descrição: solicita recuperação de senha.
- Auth: não.
- Body: `{ "email": "maria@email.com" }`.
- Response 204.
- Erros: 400.

### POST `/api/auth/reset-password`

- Descrição: redefine senha com token.
- Auth: não.
- Body: `{ "token": "reset-token", "newPassword": "secret" }`.
- Response 204.
- Erros: 400, 401.

## Users

### GET `/api/users/me`

- Auth: sim.
- Response 200: `{ "id": "uuid", "name": "Maria", "email": "maria@email.com", "createdAt": "..." }`.
- Erros: 401.
- Autorização: retorna apenas o usuário autenticado.

### PUT `/api/users/me`

- Auth: sim.
- Body: `{ "name": "Maria Silva" }`.
- Response 200: perfil atualizado.
- Erros: 400, 401.

### PUT `/api/users/me/password`

- Auth: sim.
- Body: `{ "currentPassword": "old", "newPassword": "new" }`.
- Response 204.
- Erros: 400, 401.

## Accounts

### GET `/api/accounts`

- Auth: sim.
- Query: `page`, `size`, `archived`, `type`.
- Response 200: página de contas.
- Autorização: filtra por `user_id`.

### POST `/api/accounts`

- Auth: sim.
- Body: `{ "name": "Nubank", "type": "DIGITAL_ACCOUNT", "initialBalance": 1000.00 }`.
- Response 201: account response.
- Erros: 400, 401.
- Autorização: `userId` derivado do token.

### GET `/api/accounts/{id}`

- Auth: sim.
- Response 200: account response.
- Erros: 401, 404.
- Autorização: conta deve pertencer ao usuário.

### PUT `/api/accounts/{id}`

- Auth: sim.
- Body: `{ "name": "Conta principal", "type": "CHECKING_ACCOUNT" }`.
- Response 200.
- Erros: 400, 401, 404.

### DELETE `/api/accounts/{id}`

- Auth: sim.
- Response 204.
- Erros: 401, 404, 409.
- Regra: bloquear exclusão com histórico financeiro relevante ou usar arquivamento.

### PATCH `/api/accounts/{id}/archive`

- Auth: sim.
- Response 200: conta arquivada.
- Erros: 401, 404.

## Categories

### GET `/api/categories`

- Auth: sim.
- Query: `type`.
- Response 200: lista ou página de categorias do usuário e padrão aplicáveis.

### POST `/api/categories`

- Auth: sim.
- Body: `{ "name": "Alimentação", "type": "EXPENSE", "color": "#16a34a", "icon": "utensils" }`.
- Response 201.

### GET `/api/categories/{id}`

- Auth: sim.
- Response 200.
- Autorização: categoria padrão ou do usuário.

### PUT `/api/categories/{id}`

- Auth: sim.
- Response 200.
- Regra: categorias padrão globais não devem ser editadas pelo usuário, salvo cópia pessoal.

### DELETE `/api/categories/{id}`

- Auth: sim.
- Response 204.
- Erros: 404, 409.
- Regra: bloquear se usada em lançamentos, ou exigir migração futura.

## Transactions

### GET `/api/transactions`

- Auth: sim.
- Query: `page`, `size`, `startDate`, `endDate`, `type`, `status`, `categoryId`, `accountId`.
- Response 200: página por data decrescente.
- Autorização: filtra por `user_id`.

### POST `/api/transactions`

- Auth: sim.
- Body: `{ "description": "Mercado", "amount": 150.25, "type": "EXPENSE", "transactionDate": "2026-06-20", "categoryId": "uuid", "accountId": "uuid", "status": "PAID", "notes": "" }`.
- Response 201.
- Regra: se paga/recebida, ajustar saldo.

### GET `/api/transactions/{id}`

- Auth: sim.
- Response 200.
- Autorização: transação deve pertencer ao usuário.

### PUT `/api/transactions/{id}`

- Auth: sim.
- Body: igual criação.
- Response 200.
- Regra: reverter impacto anterior e aplicar novo impacto quando necessário.

### DELETE `/api/transactions/{id}`

- Auth: sim.
- Response 204.
- Regra: reverter saldo se transação realizada.

### PATCH `/api/transactions/{id}/mark-as-paid`

- Auth: sim.
- Body opcional: `{ "accountId": "uuid", "paidDate": "2026-06-20" }`.
- Response 200.
- Regra: aplica impacto no saldo.

### PATCH `/api/transactions/{id}/cancel`

- Auth: sim.
- Response 200.
- Regra: reverte saldo se estava paga/recebida.

## Bills

### GET `/api/bills`

- Auth: sim.
- Query: `page`, `size`, `status`, `startDueDate`, `endDueDate`, `categoryId`, `accountId`, `overdue`.
- Response 200.

### POST `/api/bills`

- Auth: sim.
- Body: `{ "description": "Energia", "amount": 180.00, "dueDate": "2026-06-25", "categoryId": "uuid", "accountId": "uuid", "status": "PENDING" }`.
- Response 201.

### GET `/api/bills/{id}`

- Auth: sim.
- Response 200.

### PUT `/api/bills/{id}`

- Auth: sim.
- Response 200.
- Regra: validar efeitos se já paga.

### DELETE `/api/bills/{id}`

- Auth: sim.
- Response 204.
- Regra: bloquear ou reverter transação vinculada conforme política.

### PATCH `/api/bills/{id}/pay`

- Auth: sim.
- Body: `{ "accountId": "uuid", "paidAt": "2026-06-20" }`.
- Response 200.
- Regra: cria ou atualiza despesa correspondente.

## Budgets

### GET `/api/budgets`

- Auth: sim.
- Query: `month`, `year`, `categoryId`.
- Response 200: orçamentos com gasto, restante e percentual.

### POST `/api/budgets`

- Auth: sim.
- Body: `{ "categoryId": "uuid", "month": 6, "year": 2026, "limitAmount": 1000.00 }`.
- Response 201.

### GET `/api/budgets/{id}`

- Auth: sim.
- Response 200.

### PUT `/api/budgets/{id}`

- Auth: sim.
- Response 200.

### DELETE `/api/budgets/{id}`

- Auth: sim.
- Response 204.

## Goals

### GET `/api/goals`

- Auth: sim.
- Query: `status`.
- Response 200.

### POST `/api/goals`

- Auth: sim.
- Body: `{ "name": "Reserva", "targetAmount": 10000.00, "currentAmount": 1000.00, "deadline": "2026-12-31", "description": "" }`.
- Response 201.

### GET `/api/goals/{id}`

- Auth: sim.
- Response 200.

### PUT `/api/goals/{id}`

- Auth: sim.
- Response 200.

### DELETE `/api/goals/{id}`

- Auth: sim.
- Response 204.

### PATCH `/api/goals/{id}/progress`

- Auth: sim.
- Body: `{ "currentAmount": 2500.00 }`.
- Response 200.

## Credit Cards

### GET `/api/credit-cards`

- Auth: sim.
- Query: `archived`.
- Response 200: cartões com limite total, usado e disponível.

### POST `/api/credit-cards`

- Auth: sim.
- Body: `{ "name": "Visa", "limitAmount": 5000.00, "closingDay": 10, "dueDay": 17 }`.
- Response 201.

### GET `/api/credit-cards/{id}`

- Auth: sim.
- Response 200.

### PUT `/api/credit-cards/{id}`

- Auth: sim.
- Response 200.

### DELETE `/api/credit-cards/{id}`

- Auth: sim.
- Response 204.
- Regra: bloquear se houver faturas/compras relevantes ou arquivar.

### PATCH `/api/credit-cards/{id}/archive`

- Auth: sim.
- Response 200.
- Regra: cartão arquivado não aceita compras.

## Invoices

### GET `/api/credit-cards/{cardId}/invoices`

- Auth: sim.
- Query: `status`, `year`.
- Response 200.
- Autorização: cartão deve pertencer ao usuário.

### GET `/api/credit-cards/{cardId}/invoices/current`

- Auth: sim.
- Response 200: fatura atual.

### GET `/api/invoices/{invoiceId}`

- Auth: sim.
- Response 200: detalhes com parcelas/compras.

### PATCH `/api/invoices/{invoiceId}/pay`

- Auth: sim.
- Body: `{ "paymentAccountId": "uuid", "paidAt": "2026-06-20" }`.
- Response 200.
- Regras: reduz saldo da conta, marca fatura como paga e bloqueia pagamento duplicado.

## Card Purchases

### POST `/api/credit-cards/{cardId}/purchases`

- Auth: sim.
- Body: `{ "description": "Notebook", "categoryId": "uuid", "totalAmount": 3000.00, "purchaseDate": "2026-06-20", "installmentCount": 10, "notes": "" }`.
- Response 201: compra com parcelas geradas.
- Regras: cartão não arquivado, parcelas associadas às faturas corretas.

### GET `/api/credit-cards/{cardId}/purchases`

- Auth: sim.
- Query: `page`, `size`, `status`, `startDate`, `endDate`.
- Response 200.

### GET `/api/card-purchases/{purchaseId}`

- Auth: sim.
- Response 200.

### PUT `/api/card-purchases/{purchaseId}`

- Auth: sim.
- Response 200.
- Regra: bloquear se alguma parcela estiver em fatura paga.

### DELETE `/api/card-purchases/{purchaseId}`

- Auth: sim.
- Response 204.
- Regra: não pode alterar faturas pagas.

## Installments

### GET `/api/installments`

- Auth: sim.
- Query: `page`, `size`, `status`, `cardId`, `month`, `year`.
- Response 200.

### GET `/api/installments/future`

- Auth: sim.
- Query: `cardId`, `fromMonth`, `fromYear`.
- Response 200.

### GET `/api/card-purchases/{purchaseId}/installments`

- Auth: sim.
- Response 200.

## Dashboard

### GET `/api/dashboard/summary`

- Auth: sim.
- Query: `month`, `year`.
- Response 200: `{ "totalBalance": 0, "monthlyIncome": 0, "monthlyExpense": 0, "monthlyBalance": 0, "expectedBalance": 0, "openInvoicesTotal": 0, "currentInvoiceAmount": 0, "creditLimitUsed": 0 }`.

### GET `/api/dashboard/monthly`

- Auth: sim.
- Query: `month`, `year`.
- Response 200: resumo mensal detalhado.

### GET `/api/dashboard/charts/expenses-by-category`

- Auth: sim.
- Query: `month`, `year`.
- Response 200: lista de `{ "categoryId": "uuid", "categoryName": "Alimentação", "amount": 100 }`.

### GET `/api/dashboard/charts/income-vs-expense`

- Auth: sim.
- Query: `year`.
- Response 200: séries mensais.

## Reports

Todos exigem autenticação e filtram por `user_id`.

- GET `/api/reports/transactions`: query `startDate`, `endDate`, `type`, `categoryId`, `accountId`.
- GET `/api/reports/expenses-by-category`: query `startDate`, `endDate`.
- GET `/api/reports/monthly-evolution`: query `year`.
- GET `/api/reports/accounts-balance`: query `date`.
- GET `/api/reports/budget-vs-actual`: query `month`, `year`.
- GET `/api/reports/credit-card-expenses`: query `cardId`, `startDate`, `endDate`.
- GET `/api/reports/future-installments`: query `cardId`, `fromMonth`, `fromYear`.

Responses 200 variam por relatório, sempre em formato de tabela e dados agregados para gráfico. Erros comuns: 400, 401.
