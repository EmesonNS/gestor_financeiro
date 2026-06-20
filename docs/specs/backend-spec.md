# Especificação Técnica do Back-end

## 1. Stack técnica

- Java 21.
- Spring Boot.
- Spring Security.
- Spring Data JPA.
- PostgreSQL.
- Flyway como ferramenta recomendada de migrations.
- JWT com access token e refresh token.
- Bean Validation.
- JUnit 5, Mockito e Testcontainers quando houver integração com PostgreSQL.
- Docker e Docker Compose para ambiente local.

## 2. Arquitetura monólito modular

O back-end deve ser um monólito modular. O deploy é único, mas o código é dividido por domínios funcionais, com baixo acoplamento e alta coesão.

Cada módulo contém suas camadas internas quando necessário: `controller`, `service`, `repository`, `dto`, `entity`, `mapper`, `exception` e `validation`.

Regras centrais:

- Controllers recebem requisições, validam DTOs e delegam para services.
- Services concentram regras de negócio e transações.
- Repositories encapsulam persistência do próprio módulo.
- DTOs representam entrada e saída da API.
- Entidades JPA nunca são expostas diretamente.
- Mappers convertem DTOs e entidades.
- Comunicação entre módulos ocorre por services ou interfaces públicas do módulo.
- Um módulo não acessa diretamente repository de outro módulo sem justificativa técnica forte.

## 3. Estrutura de pastas sugerida

```text
src/main/java/com/zorysa/finance
├── FinanceApplication.java
├── shared
│   ├── config
│   ├── security
│   ├── exception
│   ├── response
│   ├── validation
│   └── util
├── auth
│   ├── controller
│   ├── service
│   ├── dto
│   └── security
├── users
│   ├── controller
│   ├── service
│   ├── repository
│   ├── entity
│   ├── dto
│   └── mapper
├── accounts
├── categories
├── transactions
├── bills
├── recurrences
├── budgets
├── goals
├── creditcards
├── invoices
├── installments
├── dashboard
└── reports
```

## 4. Módulos internos e responsabilidades

### auth

- Registro, login, logout lógico, geração e validação de JWT.
- Refresh token.
- Recuperação e redefinição de senha.
- Integração com `users` para criação e autenticação.

### users

- Dados do usuário, perfil, alteração de dados básicos e senha.
- Entidade `User`, repository próprio e services de consulta do usuário autenticado.

### accounts

- Contas financeiras, saldo inicial, saldo atual, arquivamento e recálculo.
- Deve expor service para ajuste de saldo usado por transações e faturas.

### categories

- Categorias de receita/despesa.
- Categorias padrão e personalizadas por usuário.
- Validação de categoria pertencente ao usuário.

### transactions

- Receitas e despesas pagas, pendentes e canceladas.
- Impacto e reversão de saldo em criação, edição, pagamento, cancelamento e exclusão.

### bills

- Contas a pagar, vencimentos e status.
- Integração com transações ao marcar conta como paga.

### recurrences

- Despesas recorrentes mensais.
- Controle de recorrências ativas/canceladas.
- Geração ou sugestão de lançamentos mensais.

### budgets

- Orçamento mensal por categoria.
- Cálculo de consumo com base em despesas realizadas.
- Alertas de limite ultrapassado.

### goals

- Metas financeiras, valor alvo, valor atual, prazo, status e progresso.

### creditcards

- Cadastro de cartões, limite, fechamento, vencimento, arquivamento.
- Cálculo de limite utilizado e disponível.

### invoices

- Faturas abertas, fechadas e pagas.
- Cálculo de valor total.
- Pagamento de fatura usando conta financeira.
- Impacto no saldo apenas no pagamento.

### installments

- Compras à vista e parceladas.
- Geração de parcelas.
- Associação de parcelas a faturas.
- Edição e cancelamento seguros.

### dashboard

- Agregações do mês atual e indicadores financeiros.
- Não deve persistir regra de negócio própria quando puder consultar services de domínio.

### reports

- Consultas analíticas por período, categoria, conta, cartão e parcelas futuras.
- Consultas otimizadas e paginadas quando necessário.

### shared

- Configurações, segurança comum, exceções globais, responses, utilitários e classes comuns.
- Não deve conter regras de negócio de módulos específicos.

## 5. Regras de dependência entre módulos

- `auth` pode depender de `users`.
- `transactions` pode depender de `accounts` e `categories` via services.
- `bills` pode depender de `transactions`, `accounts` e `categories` via services.
- `budgets` pode consultar agregações de `transactions`.
- `invoices` depende de `creditcards`, `accounts` e `installments` via contracts/services.
- `installments` depende de `creditcards`, `invoices` e `categories`.
- `dashboard` e `reports` podem consultar services ou projections de leitura, sem modificar estado.
- `shared` não depende de módulos de negócio.

## 6. Autenticação e autorização

- Access token JWT enviado em `Authorization: Bearer <token>`.
- Refresh token persistido com rotação e expiração.
- Senhas armazenadas com BCrypt.
- Rotas públicas: cadastro, login, refresh, forgot-password e reset-password.
- Rotas privadas: todas as demais.
- O usuário autenticado deve ser obtido do `SecurityContext`.
- Dados financeiros nunca aceitam `userId` pelo body.
- Services recebem `authenticatedUserId` ou resolvem via componente `CurrentUser`.

## 7. Tratamento de erros

Usar `@RestControllerAdvice` global no módulo `shared.exception`.

Formato padrão:

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

Erros principais:

- 400: validação ou regra de negócio inválida.
- 401: token ausente, inválido ou expirado.
- 403: tentativa de acessar recurso de outro usuário.
- 404: recurso inexistente ou não pertencente ao usuário.
- 409: conflito de estado, como fatura já paga.
- 422: operação semanticamente inválida.
- 500: erro inesperado.

## 8. Validação

- Bean Validation em DTOs de entrada.
- Valores monetários com `@DecimalMin(value = "0.01")` quando positivos.
- Limite de cartão com mínimo zero.
- Dias de fechamento/vencimento entre 1 e 31.
- Número de parcelas maior que zero.
- Datas obrigatórias.
- E-mail válido.
- Validações de propriedade do usuário no service.

## 9. Migrations

- Usar Flyway com arquivos `V001__create_users.sql`, `V002__create_accounts.sql` etc.
- Toda alteração de schema deve ser versionada.
- Migrations devem criar constraints, índices e enums/check constraints.
- Dados iniciais de categorias padrão podem ser criados por migration ou seed controlado.

## 10. Estratégia de testes

- Unitários para services com JUnit 5 e Mockito.
- Integração para repositories e fluxos críticos com Testcontainers/PostgreSQL.
- Testes de controller para autenticação, validação e autorização.
- Testes obrigatórios de isolamento entre usuários.

Testes mínimos:

- Autenticação e refresh token.
- Isolamento de dados entre usuários.
- Criação, edição e exclusão de transação paga com ajuste de saldo.
- Orçamento e cálculo de consumo.
- Conta a pagar e pagamento com transação vinculada.
- Criação de cartão.
- Compra à vista no cartão.
- Compra parcelada, geração de parcelas e associação com faturas.
- Pagamento de fatura e bloqueio de pagamento duplicado.
- Tentativa de acesso a recurso de outro usuário.

## 11. Padrões de resposta da API

Listagens paginadas:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5
}
```

Resposta simples:

```json
{
  "id": "uuid",
  "createdAt": "2026-01-01T10:00:00Z",
  "updatedAt": "2026-01-01T10:00:00Z"
}
```

## 12. DTOs principais

- `RegisterRequest`, `LoginRequest`, `AuthResponse`, `RefreshTokenRequest`.
- `UserProfileResponse`, `UpdateUserRequest`, `ChangePasswordRequest`.
- `AccountRequest`, `AccountResponse`.
- `CategoryRequest`, `CategoryResponse`.
- `TransactionRequest`, `TransactionResponse`, `TransactionFilter`.
- `BillRequest`, `BillResponse`, `PayBillRequest`.
- `RecurrenceRequest`, `RecurrenceResponse`.
- `BudgetRequest`, `BudgetResponse`.
- `GoalRequest`, `GoalResponse`, `UpdateGoalProgressRequest`.
- `CreditCardRequest`, `CreditCardResponse`.
- `InvoiceResponse`, `PayInvoiceRequest`.
- `CardPurchaseRequest`, `CardPurchaseResponse`.
- `InstallmentResponse`.
- `DashboardSummaryResponse`.
- `ReportFilter`, `ReportResponse`.

## 13. Services principais

- `AuthService`, `JwtService`, `RefreshTokenService`, `PasswordRecoveryService`.
- `UserService`.
- `AccountService` com métodos de ajuste/reversão de saldo.
- `CategoryService`.
- `TransactionService`.
- `BillService`.
- `RecurrenceService`.
- `BudgetService`.
- `GoalService`.
- `CreditCardService`.
- `InvoiceService`.
- `InstallmentPurchaseService`.
- `DashboardService`.
- `ReportService`.

## 14. Repositories principais

- `UserRepository`.
- `AccountRepository`.
- `CategoryRepository`.
- `TransactionRepository`.
- `BillRepository`.
- `RecurrenceRepository`.
- `BudgetRepository`.
- `GoalRepository`.
- `CreditCardRepository`.
- `CreditCardInvoiceRepository`.
- `CreditCardPurchaseRepository`.
- `CreditCardInstallmentRepository`.

Repositories financeiros devem oferecer métodos filtrando por `id` e `userId`, por exemplo `findByIdAndUserId`.

## 15. Policies de autorização e isolamento

- Toda tabela financeira contém `user_id`.
- Toda consulta sensível filtra por `user_id`.
- Endpoints usam usuário autenticado do token.
- `userId` nunca é aceito livremente no body para dados financeiros.
- IDs públicos podem ser UUID.
- Ao referenciar conta, categoria, cartão, fatura ou compra, o service deve validar que pertence ao usuário autenticado.
- Para evitar enumeração, recursos inexistentes ou de outro usuário podem retornar 404.

## 16. Regras técnicas importantes

- Usar `BigDecimal` para valores monetários.
- Nunca usar `double` ou `float` para dinheiro.
- Usar `LocalDate` para datas financeiras sem horário.
- Usar `OffsetDateTime` ou `Instant` para timestamps técnicos.
- Usar paginação em listagens.
- Ordenação padrão de transações por data decrescente.
- Validar valores positivos, datas obrigatórias, limite de cartão >= 0, parcelas > 0 e dias entre 1 e 31.

## 17. Regras de negócio de cartão

- Compra no cartão não altera saldo da conta financeira.
- Compra no cartão aumenta limite utilizado.
- Compra à vista entra na fatura conforme data da compra e dia de fechamento.
- Compra parcelada gera N parcelas.
- Cada parcela tem valor, número, competência e fatura.
- Soma das parcelas deve bater com valor total; arredondamento ajustado na última parcela.
- Pagamento da fatura reduz saldo da conta escolhida e marca fatura como paga.
- Não permitir pagar a mesma fatura duas vezes.
- Não permitir alterar compras de faturas pagas, salvo regra futura de estorno.
- Cartões arquivados não aceitam novas compras.
- Faturas pagas não recebem novas parcelas.
- Limite disponível considera compras em faturas abertas e futuras ainda não pagas.

## 18. Regras de negócio de parcelamentos

- Usuário informa valor total, quantidade de parcelas, data da compra e cartão.
- Sistema calcula parcelas e associa cada uma à fatura correta.
- Parcelas futuras devem ser listáveis.
- Detalhes da compra original devem ser consultáveis.
- Cancelamento não pode alterar faturas pagas.
- Edição é bloqueada se qualquer parcela pertencer a fatura paga.
