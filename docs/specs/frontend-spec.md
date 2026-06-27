# Especificação Técnica do Front-end

## 1. Stack técnica

- React.
- TypeScript.
- Tailwind CSS.
- Axios ou Fetch API, com preferência por Axios configurado em `shared/lib`.
- TanStack Query para estado assíncrono.
- React Hook Form.
- Zod para validação.
- Recharts para gráficos.
- React Router.
- Docker para execução local.

## 2. Arquitetura feature-based

O front-end deve ser organizado por domínio funcional, não por camadas técnicas globais. Cada feature contém suas páginas, componentes, hooks, serviços, schemas e tipos quando fizer sentido.

Componentes genéricos e utilitários compartilhados ficam em `shared`. Código específico de uma feature não deve ser promovido para `shared` antes de haver reutilização real.

## 3. Composition patterns

- Páginas compõem componentes menores.
- Separar componentes de apresentação e componentes que carregam dados quando fizer sentido.
- Usar `children`, slots e subcomponentes para compor layouts, modais, cards, tabelas e formulários.
- Evitar prop drilling excessivo usando composição, hooks de feature e contexto apenas quando necessário.
- Não colocar regra de negócio complexa diretamente no JSX.

Exemplos:

```tsx
<PageHeader title="Transações" description="Gerencie suas receitas e despesas">
  <Button>Nova transação</Button>
</PageHeader>
```

```tsx
<Modal>
  <Modal.Header>Nova despesa</Modal.Header>
  <Modal.Body>
    <TransactionForm />
  </Modal.Body>
  <Modal.Footer>
    <Button variant="secondary">Cancelar</Button>
    <Button>Salvar</Button>
  </Modal.Footer>
</Modal>
```

## 4. Estrutura de pastas sugerida

```text
src
├── app
│   ├── routes
│   ├── providers
│   ├── config
│   └── query-client.ts
├── features
│   ├── auth
│   │   ├── pages
│   │   ├── components
│   │   ├── hooks
│   │   ├── services
│   │   ├── schemas
│   │   └── types
│   ├── admin
│   │   ├── pages
│   │   ├── components
│   │   ├── hooks
│   │   ├── services
│   │   ├── schemas
│   │   └── types
│   ├── dashboard
│   ├── accounts
│   ├── categories
│   ├── transactions
│   ├── bills
│   ├── budgets
│   ├── goals
│   ├── credit-cards
│   ├── installments
│   ├── reports
│   └── profile
├── shared
│   ├── components
│   ├── ui
│   ├── layouts
│   ├── hooks
│   ├── lib
│   ├── types
│   └── utils
└── main.tsx
```

## 5. Estratégia de rotas

- Rotas públicas usam `AuthLayout`: login, cadastro e recuperação de senha.
- Rotas privadas usam `AppLayout`: dashboard e módulos financeiros.
- `ProtectedRoute` valida token e redireciona para login quando necessário.
- Após login, redirecionar para dashboard.
- Rotas principais:
  - `/login`, `/register`, `/forgot-password`.
  - `/account-status/pending`, `/account-status/suspended`, `/account-status/rejected`, `/account-status/unavailable`.
  - `/admin/users`, `/admin/users/pending`, `/admin/users/:id`.
  - `/dashboard`.
  - `/accounts`, `/categories`, `/transactions`, `/bills`, `/budgets`, `/goals`.
  - `/credit-cards`, `/credit-cards/:id`, `/credit-cards/:id/invoices/current`, `/credit-cards/:id/invoices`.
  - `/installments/future`, `/reports`, `/profile`.

## 6. Autenticação no front-end

- Guardar access token em estratégia definida pelo projeto. Para MVP, pode ser memória + refresh token em cookie httpOnly se suportado pelo back-end.
- Interceptor adiciona `Authorization: Bearer`.
- Interceptor trata 401 tentando refresh uma vez.
- Se refresh falhar, limpar sessão e redirecionar para login.
- Nunca armazenar dados financeiros sensíveis em localStorage.
- Após cadastro, exibir mensagem de conta pendente de aprovação e não autenticar automaticamente.
- Usuários pendentes, negados ou suspensos devem ver mensagem objetiva no login.
- Quando o login retornar `403` com `userStatus`, redirecionar para a tela pública correspondente:
  - `PENDING_APPROVAL` para `/account-status/pending`.
  - `SUSPENDED` para `/account-status/suspended`.
  - `REJECTED` para `/account-status/rejected`.
  - `DELETED` para `/account-status/unavailable`.
- Rotas administrativas devem exigir role `ADMIN`.
- Usuários comuns não devem enxergar links ou rotas do painel admin.

## 7. Consumo da API

- Cliente HTTP central em `shared/lib/api-client.ts`.
- Services por feature encapsulam endpoints.
- Hooks por feature usam TanStack Query.
- Chaves de cache padronizadas por domínio, por exemplo `['transactions', filters]`.
- Mutations invalidam queries relacionadas.

## 8. Formulários

- React Hook Form para todos os formulários.
- Zod para schemas.
- Componentes de formulário reutilizáveis: `FormField`, `Input`, `Select`, `MoneyInput`, `DatePicker`.
- Feedback de validação no campo.
- Botões com estado `loading` durante submit.

## 9. Validação

- Valor positivo.
- Datas obrigatórias.
- Categoria obrigatória em transações.
- Conta obrigatória em transações pagas.
- Cartão obrigatório em compras no cartão.
- Quantidade de parcelas maior que zero.
- Limite do cartão maior ou igual a zero.
- Dias de fechamento e vencimento entre 1 e 31.
- E-mail válido.
- Senha obrigatória.

## 10. Estado assíncrono

- TanStack Query gerencia dados vindos da API, loading, erro, refetch e cache.
- Estado local fica restrito a UI: modal aberto, filtro temporário, aba ativa.
- Evitar duplicar no estado local dados já gerenciados pela query.

## 11. Componentes reutilizáveis

- `AppLayout`, `AuthLayout`, `Sidebar`, `Header`.
- `PageHeader`, `SummaryCard`, `ChartCard`.
- `DataTable`, `FilterBar`, `EmptyState`, `LoadingState`, `ErrorState`.
- `ConfirmDialog`, `Modal`.
- `Button`, `Input`, `Select`, `DatePicker`, `MoneyInput`, `FormField`.
- `Badge`, `StatusBadge`.

Composition patterns:

- `PageHeader` recebe ações via `children`.
- `DataTable` recebe colunas configuráveis.
- `Modal` expõe `Header`, `Body` e `Footer`.
- `FormField` compõe label, controle e erro.
- `SummaryCard` recebe ícone, título, valor e variação.
- `ChartCard` compõe título, filtros e gráfico.
- `FilterBar` compõe filtros específicos por feature.
- `ConfirmDialog` atende exclusões, arquivamentos e pagamentos críticos.

## 12. Loading, empty e error state

- Toda tela com query deve exibir loading inicial.
- Toda listagem vazia deve exibir `EmptyState` com ação primária quando aplicável.
- Erros devem mostrar mensagem compreensível e opção de tentar novamente.
- Mutations devem exibir feedback por toast ou alerta.

## 13. Responsividade

- Layout responsivo para desktop e mobile.
- Sidebar pode virar drawer em telas pequenas.
- Tabelas devem ter alternativa responsiva, como cards compactos ou scroll controlado.
- Cards de resumo usam grid adaptável.
- Gráficos devem respeitar largura do container.

## 14. Telas obrigatórias do MVP

- Login, cadastro e recuperação de senha.
- Tela de cadastro pendente aguardando aprovação.
- Tela de conta suspensa.
- Tela de cadastro negado.
- Tela de conta indisponível/excluída.
- Admin: listagem de usuários pendentes.
- Admin: listagem geral de usuários por status.
- Admin: detalhes básicos do usuário e ações de aprovar, negar, suspender, reativar e excluir/desativar.
- Dashboard.
- Contas financeiras e criar/editar conta.
- Categorias e criar/editar categoria.
- Transações e criar/editar transação.
- Contas a pagar e criar/editar conta.
- Orçamentos e criar/editar orçamento.
- Metas e criar/editar meta.
- Cartões de crédito, criar/editar cartão e detalhes do cartão.
- Fatura atual, faturas futuras e histórico de faturas.
- Criar compra no cartão e criar compra parcelada.
- Parcelas futuras.
- Relatórios.
- Perfil/configurações.

## 15. Componentes por feature

- `auth`: `LoginForm`, `RegisterForm`, `ForgotPasswordForm`, `AccountStatusPage`, `PendingApprovalMessage`, `SuspendedAccountMessage`, `RejectedAccountMessage`.
- `admin`: `AdminUserTable`, `PendingUsersTable`, `AdminUserDetails`, `UserStatusBadge`, `ApproveUserDialog`, `RejectUserDialog`, `SuspendUserDialog`, `ReactivateUserDialog`, `DeleteUserDialog`.
- `dashboard`: `DashboardSummary`, `IncomeExpenseChart`, `ExpenseCategoryChart`, `DueBillsList`, `BudgetProgressList`, `GoalProgressList`.
- `accounts`: `AccountList`, `AccountForm`, `AccountCard`, `ArchiveAccountDialog`.
- `categories`: `CategoryList`, `CategoryForm`, `CategoryBadge`.
- `transactions`: `TransactionTable`, `TransactionForm`, `TransactionFilters`, `MarkAsPaidButton`.
- `bills`: `BillList`, `BillForm`, `PayBillDialog`, `OverdueBillBadge`.
- `budgets`: `BudgetList`, `BudgetForm`, `BudgetProgressCard`.
- `goals`: `GoalList`, `GoalForm`, `GoalProgressCard`.
- `credit-cards`: `CreditCardList`, `CreditCardForm`, `CardLimitSummary`, `InvoiceList`, `InvoiceDetails`, `PayInvoiceDialog`, `CardPurchaseForm`.
- `installments`: `InstallmentPurchaseForm`, `FutureInstallmentsTable`, `PurchaseDetails`.
- `reports`: `ReportFilters`, `ReportChart`, `ReportTable`.
- `profile`: `ProfileForm`, `ChangePasswordForm`.

## 16. Hooks por feature

- `useLogin`, `useRegister`.
- `useAdminUsers`, `usePendingUsers`, `useApproveUser`, `useRejectUser`, `useSuspendUser`, `useReactivateUser`, `useDeleteUser`.
- `useAccounts`, `useCreateAccount`, `useUpdateAccount`.
- `useCategories`.
- `useTransactions`, `useCreateTransaction`.
- `useBills`.
- `useBudgets`.
- `useGoals`.
- `useCreditCards`, `useCreateCreditCard`.
- `useCardInvoices`, `usePayInvoice`.
- `useCreateCardPurchase`, `useCreateInstallmentPurchase`.
- `useDashboard`.
- `useReports`.

## 17. Serviços por feature

Cada feature terá um arquivo `services/*.service.ts` chamando o cliente HTTP:

- `authService`.
- `adminUsersService`.
- `accountsService`.
- `categoriesService`.
- `transactionsService`.
- `billsService`.
- `budgetsService`.
- `goalsService`.
- `creditCardsService`.
- `invoicesService`.
- `installmentsService`.
- `dashboardService`.
- `reportsService`.

## 18. Tipos TypeScript por feature

- Tipos devem refletir DTOs do contrato da API.
- Enums de UI devem acompanhar enums do back-end.
- Tipos de filtros devem ser separados dos tipos de resposta.
- Valores monetários trafegam como number ou string conforme contrato final; se string, a UI converte apenas para exibição e input.

## 19. Regras de interface para cartão

- Mostrar limite total, utilizado e disponível.
- Mostrar valor da fatura atual.
- Mostrar fechamento e vencimento.
- Destacar faturas vencidas e pagas.
- Bloquear visualmente edição de faturas pagas.
- Confirmar pagamento de fatura.
- Solicitar conta financeira para pagamento.
- Não indicar que compra no cartão reduziu saldo de conta.

## 20. Critérios de aceite visuais e funcionais

- Usuário não autenticado não acessa rotas privadas.
- Usuário recém-cadastrado vê estado pendente e não acessa dashboard até aprovação.
- Usuário pendente, suspenso, negado ou excluído é redirecionado para uma tela pública de status após tentativa de login.
- Admin consegue aprovar, negar, suspender, reativar e excluir/desativar usuários.
- Usuário comum não acessa rotas administrativas.
- Listagens mostram loading, vazio e erro.
- Formulários exibem validação antes de enviar dados inválidos.
- Dashboard carrega resumo, gráficos e pendências.
- Cartão exibe limites e faturas corretamente.
- Pagamento de fatura pede confirmação e conta financeira.
- Layout é utilizável em desktop e mobile.
