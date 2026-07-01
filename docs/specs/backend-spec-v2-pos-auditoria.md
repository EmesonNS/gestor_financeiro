# Especificacao Tecnica do Back-end V2 pos-auditoria

Data: 2026-07-01

Esta especificacao detalha como implementar o backlog V2 no backend Spring Boot, mantendo o monolito modular atual e fechando as inconsistencias encontradas na auditoria.

Documento relacionado: `docs/specs/mvp-backlog-v2-pos-auditoria.md`.

## Decisoes de escopo

- Recuperacao/redefinicao de senha fica diferida. Endpoints existentes podem permanecer, mas nao entram nas tarefas desta rodada.
- Regras financeiras devem ficar no backend, em services transacionais.
- Frontend pode bloquear acoes invalidas, mas nunca substitui validacao server-side.
- Reads podem usar projections/repositories dedicados; commands devem passar por services de dominio.
- Operacoes que alteram saldo, status pago, limite ou historico devem ser atomicas.

## Principios obrigatorios

- Todo dado financeiro deve ser filtrado por `user_id`.
- Nenhum endpoint financeiro aceita `userId` pelo body/query.
- Valores monetarios usam `BigDecimal` e validacao compativel com `numeric(15,2)`.
- Datas financeiras usam `LocalDate`; timestamps tecnicos usam `Instant`.
- Services validam propriedade, status, arquivamento e transicao antes de alterar estado.
- FKs devem preservar historico financeiro; delete fisico so e permitido quando nao ha historico relevante.
- Erros de regra de negocio devem retornar 400/409 com codigo compreensivel, nao 500.

## Requisitos cobertos

| ID | Backend deve entregar |
|---|---|
| SEC-01 | Refresh revalida status e nao emite token para usuario bloqueado. |
| SEC-02 | JWT antigo nao autoriza usuario que deixou de ser aprovado. |
| SEC-03 | Admin status action revoga refresh tokens ativos quando bloqueia usuario. |
| SEC-04 | `User.isApproved()` reflete status efetivo. |
| SEC-05 | CORS/JWT/admin inicial configuraveis por ambiente. |
| ADM-01..03 | Policy administrativa server-side. |
| ACC-01..03 | Historico de contas e arquivamento/desarquivamento seguro. |
| CAT-01..02 | Delete seguro e unicidade normalizada de categoria. |
| ERR-01 | Erros globais padronizados. |
| TRX-01..03 | Transacoes nao quebram bills nem saldo. |
| BILL-01..03 | Bills pagas apenas via fluxo de pagamento/estorno. |
| CARD-01..03 | Limite de cartao e ciclo de vida de cartao. |
| INV-01..03 | Faturas pagas pelo total recalculado e conta ativa. |
| REC-01..03 | Recorrencias mensais completas e idempotentes. |
| BUD-01..02 | Parametros seguros e agregacao de consumo. |
| GOAL-01 | Cancelamento de metas. |
| DASH-01 | Indicadores reais de faturas/cartoes no dashboard. |
| REP-01..04 | Relatorios corretos, validados e menos dependentes de filtro em memoria. |
| INF-01 | Configuracao Docker/env segura. |
| TEST-01 | Testes junto das alteracoes. |

## Arquitetura e dependencias

Manter a estrutura modular existente:

```text
auth -> users
admin -> users, auth token repository/service
transactions -> accounts, categories, bills query port
bills -> accounts, categories, transactions
creditcards -> invoices, installments read port
invoices -> accounts, creditcards, installments
installments -> creditcards, invoices, categories
recurrences -> bills, accounts, categories
budgets -> transactions read projections
dashboard -> read projections/services
reports -> read projections/services
shared -> sem dependencia de negocio
```

Preferencia de implementacao:

- Commands usam services de dominio.
- Reports/dashboard podem usar repositories/projections especificas.
- Evitar repository de outro modulo diretamente em command service; se necessario, criar metodo publico no service do modulo dono.

## Seguranca e autenticacao

### Status efetivo do usuario

Regra:

- Usuario so esta aprovado quando `status == APPROVED`, `active == true` e `approvedAt != null`.
- `User.getStatus()` e `User.isApproved()` devem concordar.
- Se houver usuario `APPROVED` sem `approvedAt`, tratar como nao aprovado e bloquear login/refresh/request.

Implementacao:

- Ajustar `User.isApproved()`.
- Criar metodo de dominio, se util: `canAccessPrivateApplication()`.
- Cobrir com testes unitarios.

### Login, refresh e JWT

Login:

- Continua chamando `ensureUserCanLogin(user)`.
- Erros por status administrativo retornam 403 com `code` e `userStatus`.

Refresh:

- Buscar refresh token por hash.
- Verificar usabilidade.
- Revalidar usuario ativo/aprovado antes de revogar/rotacionar.
- Se bloqueado, revogar o refresh token atual e retornar 403/401 conforme contrato escolhido.
- Resposta deve incluir `user` atualizado ou o frontend deve buscar `/users/me`; recomendacao V2: incluir `user` no refresh para manter contrato simples.

JWT filter/UserDetails:

- Carregar usuario ativo e aprovado.
- Se usuario nao aprovado, limpar contexto e retornar 403 com erro de status administrativo quando possivel.
- Access token antigo deve falhar imediatamente apos suspensao/rejeicao/delecao.

Revogacao:

- Criar metodo em auth, por exemplo `revokeActiveRefreshTokensForUser(UUID userId, Instant now)`.
- Admin deve chamar esse metodo em `REJECTED`, `SUSPENDED` e `DELETED`.
- `APPROVED`/`REACTIVATED` nao revogam por padrao.

Testes obrigatorios:

- Login aprovado passa; pendente/rejeitado/suspenso/deletado falha.
- Refresh aprovado passa e retorna `user`.
- Refresh apos suspensao falha e nao cria novo token.
- Access token emitido antes da suspensao nao acessa endpoint privado.

## Administracao de usuarios

### Matriz de transicao

| Status atual | Acoes permitidas |
|---|---|
| PENDING_APPROVAL | APPROVED, REJECTED, DELETED |
| APPROVED | SUSPENDED, DELETED |
| REJECTED | APPROVED, DELETED |
| SUSPENDED | APPROVED, DELETED |
| DELETED | nenhuma no MVP V2 |

Regras:

- `reject`, `suspend` e `delete` exigem motivo nao vazio.
- `approve/reactivate` aceitam motivo opcional.
- Admin nao pode suspender/deletar a propria conta.
- Sistema nao pode ficar sem admin ativo aprovado.
- Usuario inexistente deve retornar 404.
- Toda transicao salva `user_status_history`.
- Acoes que bloqueiam acesso revogam refresh tokens ativos.

Servicos:

- Criar `UserStatusPolicy` para isolar a matriz e regras de auto-acao/ultimo admin.
- `AdminUserService` chama policy antes de alterar entidade.
- `UserRepository` precisa consultar contagem de admins aprovados ativos.

Testes obrigatorios:

- Cada transicao permitida.
- Transicoes invalidas retornam 409 ou 400.
- Motivo ausente falha para reject/suspend/delete.
- Auto-suspensao/delete falha.
- Delete/suspend do ultimo admin falha.

## Erros globais

Adicionar handlers em `GlobalExceptionHandler`:

- `DataIntegrityViolationException`: 409 quando violar FK/unique conhecida; 400/409 com codigo generico quando desconhecida.
- `MethodArgumentTypeMismatchException`: 400 para UUID/data/enum invalido.
- `HttpMessageNotReadableException`: 400 para JSON invalido ou enum invalido no body.
- `DateTimeException`: 400 para mes/ano/data invalida.
- `IllegalArgumentException` de conversao controlada: 400 somente quando for entrada do usuario.

Formato:

```json
{
  "timestamp": "2026-07-01T12:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Categoria esta em uso",
  "path": "/api/categories/{id}",
  "code": "CATEGORY_IN_USE",
  "details": []
}
```

Codigos sugeridos:

- `ACCOUNT_ARCHIVED`
- `ACCOUNT_HAS_HISTORY`
- `CATEGORY_IN_USE`
- `CATEGORY_DUPLICATED`
- `INSUFFICIENT_BALANCE`
- `CARD_LIMIT_EXCEEDED`
- `INVALID_STATUS_TRANSITION`
- `LINKED_TRANSACTION_LOCKED`
- `INVOICE_ALREADY_PAID`
- `INVALID_MONTH`
- `INVALID_PERIOD`

## Contas financeiras

### Delete e arquivamento

Regras:

- `DELETE /api/accounts/{id}`:
  - Se conta possui transacoes, bills, faturas pagas ou qualquer historico financeiro: retornar 409 `ACCOUNT_HAS_HISTORY`.
  - Se nao possui historico: pode deletar fisicamente.
- `PATCH /api/accounts/{id}/archive`: marca `archived=true`.
- `PATCH /api/accounts/{id}/unarchive`: marca `archived=false`.
- Conta arquivada continua visivel em historico e relatorios.
- Conta arquivada nao aparece em selects de novos comandos financeiros.

Service API interna:

- `findOwnedAccount(userId, accountId)` para leitura.
- `findActiveOwnedAccount(userId, accountId)` para comandos que alteram saldo.
- `adjustBalance(accountId, impact)` deve validar conta ativa quando chamado por comando novo.

Fluxos que devem usar conta ativa:

- Criar transacao paga/recebida.
- Marcar transacao pendente como paga/recebida.
- Pagar bill.
- Pagar fatura.
- Estornar pagamento quando exigir nova movimentacao.

Testes:

- Delete com historico retorna 409.
- Archive nao apaga historico.
- Unarchive reabilita uso.
- Pagamento/transacao com conta arquivada retorna 400/409.

## Categorias

Regras:

- Categoria padrao continua nao editavel pelo usuario comum.
- Delete de categoria usada por transactions, bills, budgets, recurrences ou purchases retorna 409.
- Nome unico por usuario/tipo deve ignorar caixa e espacos externos.
- Categorias padrao globais usam `user_id null`; categorias pessoais usam `user_id`.

Migrations:

- Criar indice unico funcional para pessoais, por exemplo `lower(trim(name)), type, user_id`.
- Criar indice equivalente para categorias padrao quando `user_id is null`.
- Antes da migration, tratar duplicatas existentes se houver. Se nao houver script automatico, documentar passo manual.

Testes:

- `Mercado` e `mercado` conflitam.
- Categoria usada em transaction/bill/purchase nao deleta.
- Categoria sem uso deleta.

## Transacoes

### Status e impacto

Regras:

- `PENDING` nao impacta saldo.
- `PAID`/`RECEIVED` impactam saldo conforme tipo.
- `CANCELED` nao impacta saldo.
- `markAsPaid` so aceita transacao `PENDING`.
- Update de transacao realizada deve reverter impacto antigo e aplicar novo impacto, respeitando saldo suficiente.
- Cancelamento de transacao realizada reverte impacto.

### Vinculo com origem

Regra:

- Transacao vinculada a bill paga nao pode ser editada, cancelada ou deletada pelo endpoint generico de transacoes.
- A operacao deve ser feita por endpoint da origem, por exemplo estorno da bill.

Implementacao:

- `TransactionService` consulta um port do modulo bills, por exemplo `BillLinkService.isPaidBillTransaction(userId, transactionId)`.
- Se houver vinculo, retornar 409 `LINKED_TRANSACTION_LOCKED`.

Endpoint opcional no MVP V2:

- `POST /api/bills/{id}/void-payment`
  - Reverte saldo da transacao vinculada.
  - Marca transacao como `CANCELED` ou cria uma transacao de estorno, conforme decisao tecnica.
  - Marca bill como `PENDING`, limpa `paidAt` e preserva historico.

Recomendacao: para menor impacto, bloquear alteracao isolada agora e implementar estorno dedicado em seguida na mesma fase.

Testes:

- `markAsPaid` falha para `PAID`, `RECEIVED` e `CANCELED`.
- Delete/cancel/update de transacao vinculada a bill paga retorna 409.
- Transacao nao vinculada segue comportamento atual.

## Contas a pagar

### Create/update

Regras:

- `POST /api/bills` e `PUT /api/bills/{id}` nao aceitam `status=PAID`.
- Se DTO hoje permite status, validar no service e preferencialmente restringir schema para `PENDING` ou `CANCELED`.
- `accountId` em bill pendente e opcional e nao altera saldo.
- `paidAt` e `transactionId` sao controlados pelo backend.

### Pagamento

`POST /api/bills/{id}/pay`:

- Exige bill `PENDING` ou vencida derivada.
- Exige conta ativa do usuario.
- Exige saldo suficiente.
- Cria transacao `EXPENSE` `PAID`.
- Debita saldo.
- Seta `status=PAID`, `paidAt`, `accountId`, `transactionId`.
- Tudo dentro da mesma transacao de banco.

### Vencida/cancelada

Decisao V2:

- `OVERDUE` deve ser status derivado na leitura quando `dueDate < today` e `status=PENDING`.
- Nao aceitar `OVERDUE` em create/update.
- `CANCELED` e status persistido e nao pode ser pago.

DTO response:

- Pode retornar `status` efetivo (`OVERDUE`) e `storedStatus` opcional se necessario.
- Alternativa mais simples: manter `status=PENDING` e adicionar `overdue=true`. Recomendacao: `overdue=true` para evitar mutar status por passagem de tempo.

Testes:

- Create/update `PAID` falha.
- Pay cria transacao e debita saldo.
- Falha de saldo nao cria transacao.
- Bill cancelada nao paga.
- Bill vencida pendente pode pagar.

## Cartoes, compras e limite

### Definicao de limite usado

V2 mantem a regra documentada:

- Limite usado = soma de parcelas `OPEN` de compras `ACTIVE` em faturas nao pagas.
- Limite disponivel = `limitAmount - usedLimit`, minimo zero para exibicao.
- Para validar compra nova, usar o valor real de `limitAmount - usedLimit`; se negativo, nenhuma nova compra e aceita.

### Criar compra

`POST /api/credit-cards/{id}/purchases`:

- Validar cartao ativo.
- Validar categoria de despesa.
- Validar `totalAmount` com `@Digits(integer=13, fraction=2)` e positivo.
- Calcular limite disponivel antes de criar parcelas.
- Se `totalAmount > availableLimit`, retornar 409 `CARD_LIMIT_EXCEEDED`.
- Criar compra, parcelas e atualizar faturas em uma transacao.

### Editar compra

`PUT /api/card-purchases/{id}`:

- Bloquear se qualquer parcela pertence a fatura paga.
- Recalcular impacto de limite considerando remocao das parcelas antigas abertas e inclusao das novas.
- Se novo total exceder limite disponivel ajustado, retornar 409.
- Cancelar parcelas antigas, criar novas e atualizar faturas afetadas em uma transacao.

### Arquivamento de cartao

- Cartao arquivado nao aceita compra.
- Criar endpoint `PATCH /api/credit-cards/{id}/unarchive`.
- Desarquivar nao altera faturas historicas.

Testes:

- Compra acima do limite falha e nao cria parcelas/faturas.
- Edicao acima do limite falha.
- Compra com 3 casas decimais falha.
- Cartao arquivado falha em compra.
- Desarquivar permite nova compra se houver limite.

## Faturas

### Fatura atual

Regra V2:

- Fatura atual e calculada pela competencia da data atual no ciclo do cartao.
- Nao buscar simplesmente a primeira fatura com `closingDate >= today`.
- Criar metodo em `InvoiceService`, por exemplo `referenceForDate(LocalDate date, closingDay)`.
- Buscar fatura por `userId`, `creditCardId`, `referenceMonth`, `referenceYear`.
- Se nao existir, criar fatura atual vazia somente se a API exigir resposta de fatura atual.

### Pagamento

`POST /api/invoices/{id}/pay`:

- Exige fatura nao paga.
- Exige conta ativa e saldo suficiente.
- Recalcula total pela soma de parcelas nao canceladas da fatura.
- Atualiza `invoice.totalAmount` se divergir.
- Debita saldo pelo total recalculado.
- Marca fatura `PAID`.
- Marca parcelas abertas da fatura como `PAID`.
- Tudo transacional.

Testes:

- Total persistido divergente e corrigido no pagamento.
- Conta arquivada falha.
- Fatura ja paga falha.
- Fatura futura nao e retornada como atual indevidamente.

## Recorrencias

### Modelo

Tabela `recurrences`:

| Campo | Tipo | Regra |
|---|---|---|
| id | uuid | PK |
| user_id | uuid | not null |
| description | varchar(180) | not null |
| amount | numeric(15,2) | > 0 |
| category_id | uuid | categoria de despesa do usuario ou padrao |
| account_id | uuid | nullable; se informado deve ser conta ativa na criacao |
| frequency | recurrence_frequency | MVP: `MONTHLY` |
| start_date | date | not null |
| end_date | date | nullable |
| active | boolean | default true |
| created_at/updated_at | timestamptz | not null |

Alterar `bills`:

- `recurrence_id uuid references recurrences(id)`.
- Adicionar `recurrence_month smallint nullable`.
- Adicionar `recurrence_year smallint nullable`.
- Unique parcial: `(recurrence_id, recurrence_month, recurrence_year)` quando `recurrence_id is not null`.

### Endpoints

- `GET /api/recurrences?page&size&active`
- `POST /api/recurrences`
- `GET /api/recurrences/{id}`
- `PUT /api/recurrences/{id}`
- `PATCH /api/recurrences/{id}/cancel`
- `POST /api/recurrences/{id}/generate`

Request de geracao:

```json
{
  "fromMonth": 7,
  "fromYear": 2026,
  "toMonth": 9,
  "toYear": 2026
}
```

Resposta de geracao:

```json
{
  "createdCount": 3,
  "skippedCount": 0,
  "billIds": ["uuid"]
}
```

### Regras

- Geracao e idempotente por recorrencia/competencia.
- Due date usa o dia de `start_date`, ajustado para ultimo dia do mes quando necessario.
- Geracao respeita `start_date`, `end_date` e `active`.
- Cancelar recorrencia seta `active=false` e nao apaga bills geradas.
- Editar recorrencia afeta novas geracoes. Bills ja geradas nao sao alteradas automaticamente no MVP V2.
- Categoria deve ser `EXPENSE`.
- Se `accountId` estiver arquivada no momento de gerar, criar bill pendente sem pagamento; conta pode ficar vinculada apenas se ainda ativa.

Testes:

- CRUD com isolamento por usuario.
- Geracao julho-setembro cria 3 bills.
- Segunda geracao do mesmo intervalo nao duplica.
- Recorrencia cancelada nao gera.
- End date limita geracao.
- Dia 31 ajusta fevereiro corretamente.

## Orcamentos

Regras:

- `GET /api/budgets` deve receber `month` e `year` juntos ou aplicar default atual para ambos.
- Mes deve estar entre 1 e 12.
- Ano deve estar em faixa razoavel, por exemplo 2000..2100.
- Consumo deve considerar despesas pagas no periodo.
- Migrar calculo de consumo para query agregada em vez de carregar todas as transacoes.

Testes:

- Sem mes/ano usa periodo atual.
- So mes ou so ano retorna 400.
- Mes 13 retorna 400.
- Despesas de outro usuario nao entram.

## Metas

Endpoints:

- `PATCH /api/goals/{id}/cancel`

Regras:

- Cancelar meta marca `status=CANCELED`.
- Meta cancelada nao pode receber update comum de detalhes/progresso, ou update nao pode reativar status.
- Delete fisico so permitido para meta sem relevancia historica; recomendacao: substituir delete por cancelamento no frontend.

Testes:

- Cancelar meta ativa.
- Update de meta cancelada falha ou preserva `CANCELED`.

## Dashboard

Indicadores obrigatorios V2:

- `totalBalance`: soma de contas nao arquivadas.
- `monthlyIncome`: receitas realizadas do mes.
- `monthlyExpense`: despesas realizadas do mes.
- `monthlyNet`: receitas - despesas.
- `expectedBalance`: saldo atual + pendencias do periodo, conforme regra existente.
- `openInvoicesTotal`: soma de faturas nao pagas.
- `currentInvoiceAmount`: soma/total da fatura atual por cartoes do usuario.
- `creditLimitUsed`: soma de limite usado em cartoes ativos.
- `creditLimitAvailable`: soma de limite disponivel em cartoes ativos, se DTO for ajustado.

Regras:

- Nao retornar zero fixo quando houver dados.
- Parametros `month/year` validados.
- Usar projections/agregacoes sempre que possivel.

Testes:

- Usuario com fatura aberta ve totais no dashboard.
- Dados de outro usuario nao entram.
- Mes invalido retorna 400.

## Relatorios

### Saldo por conta

Decisao V2:

- Manter filtro `date` e implementar saldo historico.
- Saldo na data = `initialBalance + impactos de transacoes/faturas pagas ate date`.
- Se `date` ausente, retornar `currentBalance`.

### Despesas de cartao

- Filtrar apenas compras `ACTIVE`.
- Periodo usa `purchaseDate`.
- Agrupar por cartao/categoria.

### Parcelas futuras

- `fromMonth/fromYear` devem vir juntos; default = mes atual.
- `toMonth/toYear` devem vir juntos ou ambos ausentes.
- Mes 1..12.
- Fim nao pode ser anterior ao inicio.
- Filtro aplicado no banco antes de paginar.

### Performance

- Evitar `Pageable.unpaged()` em relatorios com potencial de volume.
- Usar projections DTO e queries agregadas.
- Evitar `entityManager.find` por linha; carregar nomes em join/projection.

Testes:

- Saldo por data passada.
- Compra cancelada fora do relatorio.
- Intervalo invalido retorna 400.
- Paginacao ocorre apos filtro no banco.

## Infraestrutura e configuracao

Properties obrigatorias:

- `JWT_SECRET` sem default fraco fora de dev/test.
- `CORS_ALLOWED_ORIGINS` lido pelo backend.
- `FIRST_ADMIN_NAME`, `FIRST_ADMIN_EMAIL`, `FIRST_ADMIN_PASSWORD_HASH` documentados no `.env.example`.

Docker:

- Backend pode continuar com `-DskipTests` no Dockerfile somente se CI rodar testes antes.
- Frontend deve ter Dockerfile de producao com build estatico; compose de dev pode continuar usando Vite.

## Estrategia de testes

Gate minimo por fase:

- Backend: `./mvnw test`.
- Frontend: `npm run build`.

Matriz de teste por modulo:

| Modulo | Unitario | Controller | Repository/Testcontainers | Cenarios obrigatorios |
|---|---|---|---|---|
| auth/users | sim | sim | sim | refresh bloqueado, JWT antigo, status efetivo |
| admin | sim | sim | sim | matriz de transicao, ultimo admin, motivo |
| accounts | sim | sim | sim | delete com historico, conta arquivada |
| categories | sim | sim | sim | delete em uso, duplicidade normalizada |
| transactions | sim | sim | opcional | mark pending only, linked transaction locked |
| bills | sim | sim | sim | pay atomico, create/update PAID bloqueado |
| creditcards/installments | sim | sim | sim | limite excedido, escala monetaria |
| invoices | sim | sim | sim | total recalculado, conta arquivada |
| recurrences | sim | sim | sim | geracao idempotente, cancelamento |
| budgets | sim | sim | opcional | parametros mes/ano, consumo agregado |
| dashboard/reports | sim | sim | opcional | indicadores reais, filtros validos |

Regra de implementacao:

- Todo bug da auditoria deve primeiro ganhar um teste que falha no comportamento atual.
- Depois corrigir service/controller/migration.
- Nao apagar testes existentes para reduzir contagem.

## Checklist de pronto

- [ ] Todos os requisitos P0 cobertos por testes.
- [ ] `./mvnw test` passa.
- [ ] Migrations aplicam em banco limpo.
- [ ] Respostas de erro seguem contrato.
- [ ] Usuario suspenso perde acesso imediatamente.
- [ ] Compra acima do limite e pagamento sem saldo sao bloqueados.
- [ ] Bill/fatura/transacao nao ficam divergentes.
- [ ] Recorrencias geram bills idempotentes.
- [ ] Dashboard/relatorios nao retornam dados falsos/zerados quando ha dados.
