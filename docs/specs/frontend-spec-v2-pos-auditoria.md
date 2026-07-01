# Especificacao Tecnica do Front-end V2 pos-auditoria

Data: 2026-07-01

Esta versao detalha as alteracoes de frontend necessarias para acompanhar o backlog V2 e o backend V2. A UI deve orientar o usuario, mas todas as regras criticas continuam sendo validadas pelo backend.

Documentos relacionados:

- `docs/specs/mvp-backlog-v2-pos-auditoria.md`
- `docs/specs/backend-spec-v2-pos-auditoria.md`

## Decisoes de escopo

- Recuperacao/redefinicao de senha fica diferida. Manter a tela atual de esqueci senha se existir, mas nao criar tela `/reset-password` nesta rodada.
- Frontend nao deve simular regra financeira que o backend nao valida; ele deve apenas antecipar feedback e lidar com erro de API.
- Quando backend retornar conflito/regra de negocio, a tela deve mostrar mensagem clara e manter dados do formulario para ajuste.
- Rotas e modulos existentes devem ser preservados; novas telas entram por feature.

## Requisitos cobertos

| ID | Frontend deve entregar |
|---|---|
| FE-01 | Sessao e usuario atualizados apos refresh; 401/403 tratados corretamente. |
| FE-02 | Acoes invalidas por status/vinculo/arquivamento bloqueadas ou escondidas. |
| FE-03 | UI de arquivar/desarquivar e selects sem entidades arquivadas em comandos novos. |
| FE-04 | Bills pagas apenas por dialogo de pagamento; form comum nao salva `PAID`. |
| FE-05 | Compra no cartao mostra limite disponivel e valida antes do submit. |
| FE-06 | Dashboard mostra indicadores reais de faturas/cartoes/limites. |
| FE-07 | Filtros de relatorios seguem validacoes do backend. |
| FE-08 | Code splitting/lazy loading para reduzir bundle inicial. |
| REC-01..03 | Telas de recorrencias mensais. |
| ADM-01..04 | UI admin respeita matriz de transicao e motivo obrigatorio. |

## Arquitetura mantida

Manter estrutura feature-based:

```text
src/features
|-- auth
|-- admin
|-- accounts
|-- categories
|-- transactions
|-- bills
|-- recurrences
|-- budgets
|-- goals
|-- credit-cards
|-- installments
|-- invoices
|-- dashboard
|-- reports
`-- profile
```

Padroes:

- Services por feature chamam `apiClient`.
- Hooks por feature encapsulam TanStack Query.
- Schemas por feature usam Zod.
- Components de formulario nao devem carregar regra de API diretamente; usar props e schemas.
- Mutations invalidam queries relacionadas.

## Sessao e autenticacao

### Refresh e usuario atual

Problema a corrigir:

- O refresh atual salva tokens, mas pode deixar `authUser` nulo ou com role antiga.

Regra V2:

- Se `/auth/refresh` retornar `user`, chamar `setAuthTokens(data)` e atualizar `authUser`.
- Se refresh nao retornar `user`, chamar `/users/me` imediatamente apos refresh e atualizar store.
- Se refresh falhar com 401 ou status administrativo 403, limpar sessao e redirecionar:
  - `PENDING_APPROVAL` -> `/account-status/pending`
  - `SUSPENDED` -> `/account-status/suspended`
  - `REJECTED` -> `/account-status/rejected`
  - `DELETED` -> `/account-status/unavailable`
  - sem status -> `/login`

Arquivos esperados:

- `frontend/src/features/auth/services/auth.service.ts`
- `frontend/src/shared/lib/auth-token-store.ts`
- `frontend/src/shared/lib/api-client.ts`
- `frontend/src/app/routes/ProtectedRoute.tsx`

Criterios de aceite:

- Usuario suspenso apos login perde sessao no proximo refresh ou request privado.
- Role atualizada apos refresh reflete mudanca feita por admin.
- Usuario nulo nao fica autenticado em tela privada.

### Recuperacao de senha

Status: diferido.

Nao implementar nesta rodada:

- Tela `/reset-password`.
- Envio de email/token em dev.
- Fluxo completo de redefinicao.

## Administracao

### Listagem e filtros

Atualizar admin para suportar:

- Filtro por status.
- Busca por nome/e-mail.
- Periodo de cadastro.
- Indicadores de pendentes/suspensos quando backend expuser.

Arquivos esperados:

- `features/admin/pages/AdminUsersPage.tsx`
- `features/admin/services/admin-users.service.ts`
- `features/admin/hooks/useAdminUsers.ts`
- `features/admin/types`

### Acoes e matriz de status

UI deve seguir a matriz:

| Status | Acoes visiveis |
|---|---|
| PENDING_APPROVAL | Aprovar, Rejeitar, Deletar |
| APPROVED | Suspender, Deletar |
| REJECTED | Aprovar/Reativar, Deletar |
| SUSPENDED | Reativar, Deletar |
| DELETED | nenhuma acao de mudanca no MVP V2 |

Regras de UI:

- Motivo obrigatorio para rejeitar, suspender e deletar.
- Se backend retornar `LAST_ADMIN` ou `SELF_ACTION_NOT_ALLOWED`, mostrar mensagem clara.
- Nao depender apenas da UI: se a API rejeitar, exibir erro no dialogo.
- Historico administrativo deve continuar visivel em detalhes.

Criterios de aceite:

- Dialogo de suspensao sem motivo nao envia request.
- Acao invalida por status nao aparece.
- Erro de backend permanece legivel no dialogo.

## Contas financeiras

### Arquivar/desarquivar

Telas:

- Listagem de contas deve permitir filtrar arquivadas.
- Conta arquivada mostra badge `Arquivada`.
- Acoes:
  - Conta ativa: `Arquivar`.
  - Conta arquivada: `Desarquivar`.
  - Delete so deve aparecer quando backend permitir ou deve exibir confirmacao avisando que contas com historico nao podem ser excluidas.

Selects financeiros:

- Transacao paga/recebida: listar apenas contas ativas.
- Pagamento de bill: listar apenas contas ativas.
- Pagamento de fatura: listar apenas contas ativas.
- Relatorios/filtros historicos podem listar contas arquivadas.

Erros:

- `ACCOUNT_ARCHIVED`: "Esta conta esta arquivada. Desarquive para usa-la em novos pagamentos."
- `ACCOUNT_HAS_HISTORY`: "Esta conta possui historico financeiro e nao pode ser excluida."

Arquivos esperados:

- `features/accounts`
- `features/transactions`
- `features/bills`
- `features/invoices`

## Categorias

Regras de UI:

- Mostrar erro de duplicidade ignorando caixa quando backend retornar `CATEGORY_DUPLICATED`.
- Ao deletar categoria em uso, mostrar `CATEGORY_IN_USE` e orientar manter a categoria para historico.
- Categoria padrao continua sem edicao/delete.

Selects:

- Formularios de despesa mostram categorias `EXPENSE`.
- Formularios de receita mostram categorias `INCOME`.
- Recorrencias e compras no cartao usam apenas `EXPENSE`.

Arquivos esperados:

- `features/categories`
- Schemas Zod devem aplicar trim e required, mas backend decide unicidade.

## Transacoes

Regras de UI:

- Botao "marcar como pago/recebido" aparece apenas para status `PENDING`.
- Transacao `CANCELED`, `PAID` ou `RECEIVED` nao mostra acao de marcar como paga.
- Transacao vinculada a bill paga deve mostrar badge/aviso "Gerada por pagamento de conta" e nao permitir editar/cancelar/delete generico.
- Se backend retornar `LINKED_TRANSACTION_LOCKED`, mostrar link ou orientacao para abrir a bill de origem quando a API fornecer esse id.

Formulario:

- Conta obrigatoria quando status selecionado impacta saldo.
- Conta arquivada nao aparece.
- Saldo insuficiente continua tratado por erro de API.

Arquivos esperados:

- `features/transactions/pages`
- `features/transactions/components/TransactionForm.tsx`
- `features/transactions/components/TransactionTable.tsx`

## Contas a pagar

### Formulario

Regra V2:

- Form comum nao permite criar/editar bill como `PAID`.
- Status permitidos no formulario:
  - Criacao: `PENDING` por padrao.
  - Edicao: `PENDING` ou `CANCELED`, conforme backend permitir.
- `paidAt` e `transactionId` nao aparecem em formulario comum.

### Pagamento

Dialogo dedicado:

- Abre apenas para bill pendente/vencida.
- Seleciona conta ativa.
- Mostra valor, vencimento, categoria e saldo da conta escolhida quando disponivel.
- Ao confirmar, chama endpoint de pagamento.
- Em sucesso, invalida queries de bills, transactions, accounts e dashboard.

### Vencidas

Se backend retornar `overdue=true`, UI mostra badge `Vencida` sem depender de status persistido `OVERDUE`.

Arquivos esperados:

- `features/bills/components/BillForm.tsx`
- `features/bills/components/PayBillDialog.tsx`
- `features/bills/pages/BillsPage.tsx`

## Cartoes e compras parceladas

### Cartao

Listagem/detalhes:

- Mostrar limite total, usado e disponivel.
- Cartao arquivado mostra badge e nao exibe CTA de nova compra.
- Cartao arquivado deve ter acao `Desarquivar` se backend expuser.

### Formulario de compra

Regra V2:

- Mostrar resumo do cartao no topo do formulario: limite total, usado e disponivel.
- Validar `totalAmount <= availableLimit` antes de submit.
- Validar duas casas decimais.
- Validar parcelas > 0.
- Ainda tratar `CARD_LIMIT_EXCEEDED` vindo do backend, pois limite pode mudar entre tela e submit.
- Se edicao de compra possuir parcela em fatura paga, form deve abrir em modo leitura ou bloquear submit com mensagem.

UX sugerida:

- Campo valor total mostra ajuda curta quando exceder limite.
- Botao submit fica desabilitado enquanto valor excede limite conhecido.
- Em edicao, mostrar aviso "Nao e possivel editar compras com parcelas em faturas pagas".

Arquivos esperados:

- `features/credit-cards`
- `features/installments/components/CardPurchaseForm.tsx`
- `features/installments/pages/CardPurchaseFormPage.tsx`
- `features/installments/schemas`

## Faturas

Regras de UI:

- Fatura atual deve consumir endpoint corrigido pelo backend.
- Pagamento de fatura exige conta ativa.
- Contas arquivadas nao aparecem no dialogo.
- Se backend corrigir total antes de pagar, UI deve refetch da fatura e contas apos sucesso.
- Fatura paga nao mostra botao de pagar.
- Erro `INVOICE_ALREADY_PAID` deve mostrar mensagem "Esta fatura ja foi paga."

Arquivos esperados:

- `features/invoices/pages/CardInvoicesPage.tsx`
- `features/invoices/pages/InvoiceDetailsPage.tsx`
- `features/invoices/components/PayInvoiceDialog.tsx`

## Recorrencias

Nova feature obrigatoria:

```text
frontend/src/features/recurrences
|-- pages
|   |-- RecurrencesPage.tsx
|   `-- RecurrenceFormPage.tsx
|-- components
|   |-- RecurrenceForm.tsx
|   |-- RecurrenceList.tsx
|   `-- GenerateRecurrenceDialog.tsx
|-- hooks
|-- services
|-- schemas
`-- types
```

Rotas:

- `/recurrences`
- `/recurrences/new`
- `/recurrences/:id/edit`

Menu:

- Adicionar item "Recorrencias" proximo de "Contas a pagar".

Formulario:

- Descricao.
- Valor.
- Categoria de despesa.
- Conta opcional ativa.
- Frequencia: somente `MONTHLY` no MVP V2, pode ser campo desabilitado ou omitido.
- Data inicial.
- Data final opcional.
- Ativa/inativa conforme backend.

Listagem:

- Filtro por ativa/inativa.
- Acoes:
  - Editar.
  - Cancelar.
  - Gerar ocorrencias.

Dialogo de geracao:

- `fromMonth`, `fromYear`, `toMonth`, `toYear`.
- Validar meses 1..12.
- Validar fim >= inicio.
- Mostrar resultado: `createdCount`, `skippedCount`.
- Invalidar queries de bills, dashboard e recorrencias.

Criterios de aceite:

- Usuario cria recorrencia mensal.
- Gera bills de intervalo sem duplicar.
- Cancela recorrencia e nao gera novas ocorrencias.

## Orcamentos

Regras de UI:

- Sempre enviar `month` e `year` juntos.
- Se filtros ficarem vazios, aplicar mes/ano atual no frontend.
- Validar mes 1..12.
- Exibir erro amigavel em conflito de periodo.

Arquivos esperados:

- `features/budgets/pages/BudgetsPage.tsx`
- `features/budgets/components/BudgetForm.tsx`
- `features/budgets/hooks`

## Metas

Regras de UI:

- Trocar delete como acao primaria por `Cancelar meta`, se backend implementar cancelamento.
- Meta cancelada aparece com badge `Cancelada` e sem acao de editar progresso.
- Se delete fisico permanecer para metas sem historico, deixar como acao secundaria/destrutiva com confirmacao.

Arquivos esperados:

- `features/goals/pages/GoalsPage.tsx`
- `features/goals/components/GoalForm.tsx`
- `features/goals/components/GoalProgressCard.tsx`

## Dashboard

Remover dependencia obsoleta:

- `FutureDependencyPanel` deve ser removido ou receber pendencias reais. Recomendacao: remover do dashboard se nao houver itens.
- `docs/dashboard-dependencies.md` deve ser atualizado pelo backend/docs, nao exibido como fonte de verdade na UI.

Cards obrigatorios V2:

- Saldo total.
- Receitas do mes.
- Despesas do mes.
- Resultado do mes.
- Saldo previsto.
- Faturas abertas.
- Fatura atual.
- Limite usado.
- Limite disponivel, se backend retornar.

Componentes:

- `DashboardSummary`
- `CreditCardSummaryCards`
- `IncomeExpenseChart`
- `ExpenseCategoryChart`
- `DueBillsList`
- `BudgetProgressList`
- `GoalProgressList`

Estados:

- Loading inicial.
- Empty state para usuario sem dados.
- Error state com retry.

Criterios de aceite:

- Quando existem faturas abertas, o card nao fica zero.
- Quando nao existem cartoes, cards de cartao mostram zero com texto neutro, nao erro.
- Dados refazem fetch apos pagamento de bill/fatura e criacao de compra.

## Relatorios

### Filtros

Todos os filtros devem espelhar backend:

- Mes: 1..12.
- Ano: 2000..2100.
- Periodo final nao pode ser anterior ao inicial.
- `fromMonth/fromYear` sempre juntos.
- `toMonth/toYear` sempre juntos quando usados.

### Saldo por conta

Como backend V2 deve implementar `date`, manter campo "Data" e explicar no label:

- Label: "Saldo em"
- Se vazio, backend retorna saldo atual.

### Despesas de cartao

- UI nao precisa filtrar canceladas; backend faz.
- Pode mostrar aviso em detalhes se houver compras canceladas no periodo, apenas se API fornecer dado.

### Parcelas futuras

- Filtros de competencia devem validar antes de enviar.
- Resultado paginado deve vir do backend; frontend nao deve filtrar a pagina atual para simular intervalo.

Arquivos esperados:

- `features/reports/components/ReportFilters.tsx`
- `features/reports/services/reports.service.ts`
- `features/reports/types`

## Tratamento de erros na UI

Mapear codigos de backend para mensagens:

| Code | Mensagem sugerida |
|---|---|
| `ACCOUNT_ARCHIVED` | Esta conta esta arquivada. Desarquive para usa-la em novos pagamentos. |
| `ACCOUNT_HAS_HISTORY` | Esta conta possui historico financeiro e nao pode ser excluida. |
| `CATEGORY_IN_USE` | Esta categoria esta em uso e precisa ser mantida para preservar historico. |
| `CATEGORY_DUPLICATED` | Ja existe uma categoria com esse nome e tipo. |
| `INSUFFICIENT_BALANCE` | Saldo insuficiente para concluir a operacao. |
| `CARD_LIMIT_EXCEEDED` | O valor excede o limite disponivel do cartao. |
| `INVALID_STATUS_TRANSITION` | Esta acao nao e permitida para o status atual. |
| `LINKED_TRANSACTION_LOCKED` | Esta transacao foi criada por outro fluxo e nao pode ser alterada aqui. |
| `INVOICE_ALREADY_PAID` | Esta fatura ja foi paga. |
| `INVALID_MONTH` | Informe um mes entre 1 e 12. |
| `INVALID_PERIOD` | O periodo final deve ser igual ou posterior ao inicial. |

Implementacao:

- Criar helper em `shared/lib/api-error.ts` ou reaproveitar util existente.
- Formularios devem exibir erro proximo ao submit.
- Listagens podem exibir `ErrorState`.

## Code splitting e build

Problema:

- Build atual passou, mas avisou chunk JS acima de 500 kB.

V2:

- Aplicar `React.lazy` nas paginas principais.
- Usar `Suspense` em `AppRoutes`.
- Opcional: separar chunks de vendor com `build.rollupOptions.output.manualChunks`.

Criterio:

- `npm run build` passa.
- Registrar tamanho do bundle antes/depois. Ideal: chunk principal abaixo de 500 kB; se nao atingir, documentar o motivo.

## Testes e verificacao

Gate minimo:

- `npm run build`.

Se o projeto adicionar runner de testes frontend:

- Testar `ProtectedRoute` e refresh falho.
- Testar `AdminActionDialog` com motivo obrigatorio.
- Testar schemas de compra no cartao, recorrencia e filtros de relatorios.
- Testar que selects de conta nao mostram arquivadas em comandos financeiros.

Checklist manual por fase:

- [ ] Usuario suspenso e redirecionado corretamente.
- [ ] Admin nao envia rejeicao/suspensao/delete sem motivo.
- [ ] Conta arquivada nao aparece em pagamento.
- [ ] Bill form nao permite status pago.
- [ ] Compra acima do limite mostra erro antes do submit e trata erro da API.
- [ ] Recorrencia cria, gera bills e cancela.
- [ ] Dashboard mostra faturas/cartoes reais.
- [ ] Relatorios validam mes/periodo.
- [ ] Build final passa.

## Ordem de implementacao frontend

1. Auth/session e helper de erro.
2. Admin actions e filtros.
3. Contas/categorias arquivamento e conflitos.
4. Bills/transacoes com acoes por status/vinculo.
5. Cartoes/compras/faturas.
6. Recorrencias.
7. Dashboard/relatorios.
8. Lazy loading e refinamentos.

## Itens diferidos

- Tela `/reset-password`.
- Envio/recebimento de token de recuperacao.
- Testes especificos de redefinicao de senha.
