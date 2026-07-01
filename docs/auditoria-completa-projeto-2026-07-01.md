# Auditoria completa do projeto - 2026-07-01

## Escopo

Esta varredura cobre backend Spring Boot, frontend React/Vite, migrations Flyway, contratos em `docs/specs`, infraestrutura Docker e testes existentes.

Verificacoes executadas:

- Backend: `./mvnw test` em `finance` - passou com 388 testes, 0 falhas, 0 erros, 0 ignorados.
- Frontend: `npm run build` em `frontend` - passou; gerou aviso de chunk acima de 500 kB.
- Busca estatica por `TODO`, `FIXME`, `debugger`, `console.log`, regras pendentes e divergencias entre codigo e specs.

Legenda de severidade:

- Critico: risco direto de acesso indevido, saldo incorreto grave ou falha de seguranca.
- Alto: regra financeira inconsistente, dado historico corrompido ou contrato publico quebrado.
- Medio: comportamento incompleto, erro ruim para usuario, risco operacional ou performance relevante.
- Baixo: ajuste de robustez, qualidade, UX ou manutencao.
- Sugestao: melhoria de produto, testes ou arquitetura.

## Resumo executivo

O projeto esta bem estruturado e possui uma base boa de testes, mas ainda ha lacunas importantes em regras financeiras que cruzam modulos. Os principais riscos encontrados sao:

- Sessao de usuario suspenso/rejeitado pode continuar viva via access token e refresh token.
- Compras no cartao nao validam limite disponivel.
- Contas a pagar podem nascer ou ser atualizadas como `PAID` sem criar transacao e sem abater saldo.
- Transacoes criadas por pagamento de contas podem ser canceladas ou excluidas isoladamente, deixando a conta a pagar como paga.
- Dashboard e relatorios ainda ignoram partes ja implementadas, principalmente faturas/cartoes, e alguns filtros aceitos pela API nao sao aplicados.
- Recorrencias estao especificadas, mas nao existem como modulo; ha apenas `bills.recurrence_id` sem FK/tabela.
- Deletes fisicos e FKs `ON DELETE SET NULL` podem perder contexto historico de contas em transacoes e contas a pagar.

## Achados criticos

### C-001 - Refresh token nao revalida status administrativo do usuario

Evidencia:

- `finance/src/main/java/com/zorysa/finance/auth/service/AuthService.java:73-80`
- `finance/src/main/java/com/zorysa/finance/auth/service/AuthService.java:112-127`
- `finance/src/main/java/com/zorysa/finance/admin/service/AdminUserService.java:93-110`

Impacto:

Um usuario que ja tinha refresh token valido pode continuar renovando sessao depois de ser suspenso, rejeitado ou marcado como deletado, porque `refresh()` chama `issueTokens(current.getUser())` sem passar por `ensureUserCanLogin()`. A troca de status no admin tambem nao revoga refresh tokens existentes.

Sugestao:

- Chamar `ensureUserCanLogin(current.getUser())` antes de emitir novos tokens no refresh.
- Revogar refresh tokens ativos quando admin suspender, rejeitar ou deletar um usuario.
- Adicionar testes cobrindo: usuario aprovado faz login, admin suspende, refresh deve retornar 403/401 e nao emitir novo access token.

### C-002 - Access token continua autenticando usuario que deixou de estar aprovado

Evidencia:

- `finance/src/main/java/com/zorysa/finance/auth/security/JwtAuthenticationFilter.java:35-45`
- `finance/src/main/java/com/zorysa/finance/auth/security/CustomUserDetailsService.java:19-21`
- `finance/src/main/java/com/zorysa/finance/shared/security/SecurityConfig.java:39-43`

Impacto:

Rotas financeiras aceitam qualquer usuario autenticado por JWT. O filtro valida o token e carrega usuario ativo, mas nao revalida `APPROVED`. Um usuario suspenso pode continuar acessando ate o access token expirar. Como o refresh tambem nao revalida status, o problema pode virar acesso continuo.

Sugestao:

- Fazer `CustomUserDetailsService` ou o filtro rejeitar usuarios que nao estejam efetivamente aprovados.
- Diferenciar claramente erro 401 de token invalido e 403 por status administrativo bloqueado.
- Cobrir endpoints financeiros com teste: token emitido antes da suspensao nao deve autorizar depois da suspensao.

### C-003 - Compra maior que limite disponivel do cartao e aceita

Evidencia:

- `finance/src/main/java/com/zorysa/finance/installments/service/InstallmentService.java:72-99`
- `finance/src/main/java/com/zorysa/finance/installments/service/InstallmentService.java:122-155`
- `finance/src/main/java/com/zorysa/finance/creditcards/service/CreditCardService.java:100-121`
- `finance/src/main/java/com/zorysa/finance/installments/repository/CreditCardInstallmentRepository.java:42-53`

Impacto:

O service valida se o cartao esta arquivado e se a categoria e de despesa, mas nao compara `totalAmount` ou impacto das novas parcelas contra `availableLimit`. Assim, uma compra acima do limite disponivel e gravada, faturas sao criadas e o limite pode ficar negativo internamente, mesmo que a resposta de cartao trunque disponivel para zero.

Sugestao:

- No `createPurchase`, validar `request.totalAmount()` contra limite disponivel calculado.
- No `updatePurchase`, calcular diferenca entre parcelas abertas antigas e novas para o cartao antes de recriar parcelas.
- Definir regra de negocio: limite considera apenas parcelas/faturas abertas ou tambem compras futuras ainda nao faturadas.
- Adicionar testes de service e controller para compra acima do limite e edicao que passa a exceder limite.

### C-004 - Conta a pagar pode ficar `PAID` sem transacao e sem impacto no saldo

Evidencia:

- `finance/src/main/java/com/zorysa/finance/bills/service/BillService.java:74-91`
- `finance/src/main/java/com/zorysa/finance/bills/service/BillService.java:99-116`
- `finance/src/main/java/com/zorysa/finance/bills/service/BillService.java:214-218`
- `finance/src/main/java/com/zorysa/finance/bills/service/BillService.java:128-149`

Impacto:

`createBill` e `updateBill` aceitam status `PAID` se `accountId` existir, mas so `payBill` cria a transacao de despesa, reduz saldo e seta `paidAt`/`transactionId`. Uma chamada direta na API pode deixar bill paga sem registro financeiro correspondente.

Sugestao:

- Bloquear `PAID` em create/update e exigir endpoint dedicado `pay`.
- Ou, se `PAID` for aceito em create/update, centralizar no mesmo fluxo de pagamento usado por `payBill`.
- Adicionar teste: criar/editar bill com `PAID` deve falhar ou deve criar transacao e ajustar saldo.

## Achados altos

### A-001 - Transacao vinculada a pagamento de conta pode ser cancelada, editada ou excluida isoladamente

Evidencia:

- `finance/src/main/java/com/zorysa/finance/transactions/service/TransactionService.java:124-128`
- `finance/src/main/java/com/zorysa/finance/transactions/service/TransactionService.java:131-163`
- `finance/src/main/java/com/zorysa/finance/transactions/service/TransactionService.java:166-173`
- `finance/src/main/java/com/zorysa/finance/bills/service/BillService.java:128-149`
- `finance/src/main/resources/db/migration/V006__create_bills.sql:8`

Impacto:

Ao pagar uma conta, o sistema cria uma transacao e armazena `bill.transaction_id`. Depois disso, a API de transacoes pode cancelar ou excluir essa transacao, revertendo saldo ou apagando historico, enquanto a bill continua `PAID`. Como a FK e `ON DELETE SET NULL`, o vinculo pode sumir silenciosamente.

Sugestao:

- Impedir update/delete/cancel de transacoes vinculadas a bills pagas, ou criar fluxo atomico para estornar a bill e a transacao juntas.
- Trocar `ON DELETE SET NULL` por uma estrategia que preserve integridade, ou bloquear delecao quando houver vinculo.
- Criar testes cruzados Bill + Transaction.

### A-002 - `markAsPaid` aceita transacao cancelada ou ja realizada

Evidencia:

- `finance/src/main/java/com/zorysa/finance/transactions/service/TransactionService.java:131-163`

Impacto:

O endpoint de marcar como pago nao valida que a transacao esta `PENDING`. Ele recalcula impacto e permite alterar data/conta de uma transacao `CANCELED`, `PAID` ou `RECEIVED`, o que abre caminho para reativar ou mover saldo por uma acao indevida.

Sugestao:

- Exigir status `PENDING` antes de `markAsPaid`.
- Separar comandos: pagar pendente, editar realizada, cancelar realizada.
- Testar status `CANCELED`, `PAID` e `RECEIVED` no endpoint.

### A-003 - Delete fisico de conta perde contexto historico

Evidencia:

- `finance/src/main/java/com/zorysa/finance/accounts/service/AccountService.java:74-78`
- `finance/src/main/resources/db/migration/V005__create_transactions.sql:7`
- `finance/src/main/resources/db/migration/V006__create_bills.sql:6`
- `docs/specs/api-contract.md:242-248`

Impacto:

O contrato diz para bloquear exclusao com historico financeiro ou usar arquivamento. Hoje o service deleta fisicamente a conta, e as FKs de transacoes/bills usam `ON DELETE SET NULL`. Isso preserva linhas, mas perde a informacao de qual conta originou a movimentacao.

Sugestao:

- Transformar delete em archive, ou retornar 409 quando existir qualquer historico financeiro.
- Criar endpoint de desarquivar, se arquivamento for o ciclo de vida real.
- Adicionar teste para conta com transacao/bill vinculada.

### A-004 - Contas arquivadas ainda podem ser usadas em fluxos financeiros

Evidencia:

- `finance/src/main/java/com/zorysa/finance/accounts/service/AccountService.java:80-82`
- `finance/src/main/java/com/zorysa/finance/transactions/service/TransactionService.java:253-255`
- `finance/src/main/java/com/zorysa/finance/invoices/service/InvoiceService.java:77-83`

Impacto:

`findOwnedAccount` valida propriedade, mas nao bloqueia `archived`. Transacoes novas, pagamento de bills e pagamento de faturas podem usar conta arquivada via API direta.

Sugestao:

- Criar metodo `findActiveOwnedAccount` para comandos que movimentam saldo.
- Manter leitura de conta arquivada para historico, mas bloquear novas operacoes.
- Testar transacao, bill payment e invoice payment com conta arquivada.

### A-005 - Categoria usada pode ser deletada e virar erro tecnico

Evidencia:

- `finance/src/main/java/com/zorysa/finance/categories/service/CategoryService.java:88-91`
- `finance/src/main/resources/db/migration/V005__create_transactions.sql:8`
- `docs/specs/api-contract.md:295-300`
- `finance/src/main/java/com/zorysa/finance/shared/exception/GlobalExceptionHandler.java:21-66`

Impacto:

O contrato espera 409 se a categoria estiver usada. O service chama delete direto; se existir transacao, o banco deve bloquear por FK `ON DELETE RESTRICT`, mas nao ha handler para `DataIntegrityViolationException`. O usuario tende a receber erro 500 ou resposta generica.

Sugestao:

- Consultar uso antes de deletar e retornar `ConflictException`.
- Adicionar handler global para violacoes de integridade conhecidas.
- Considerar arquivamento/inativacao de categoria em vez de delete.

### A-006 - Pagamento de fatura usa total persistido, nao recalculado no momento

Evidencia:

- `finance/src/main/java/com/zorysa/finance/invoices/service/InvoiceService.java:77-83`
- `finance/src/main/java/com/zorysa/finance/installments/service/InstallmentService.java:96-98`
- `finance/src/main/java/com/zorysa/finance/installments/service/InstallmentService.java:150-153`

Impacto:

`payInvoice` debita `invoice.getTotalAmount()`. Se o total persistido estiver stale por falha anterior, concorrencia ou ajuste manual, o saldo debitado sera incorreto. Como fatura e saldo sao dados financeiros, o pagamento deve recalcular ou travar a consistencia no momento da operacao.

Sugestao:

- Recalcular total pelas parcelas nao canceladas da fatura dentro da transacao de pagamento.
- Opcionalmente comparar total persistido com total recalculado e corrigir antes de debitar.
- Cobrir teste em que total da fatura diverge da soma das parcelas.

### A-007 - Dashboard ainda retorna zeros para indicadores ja suportados pelos modulos atuais

Evidencia:

- `finance/src/main/java/com/zorysa/finance/dashboard/service/DashboardService.java:67-75`
- `docs/dashboard-dependencies.md:15-32`

Impacto:

Campos relacionados a faturas/cartoes continuam como `ZERO`, embora os modulos de cartoes, faturas e compras parceladas existam. O dashboard passa uma visao financeira incompleta.

Sugestao:

- Atualizar `DashboardService` para integrar repositorios de faturas/cartoes/compras.
- Atualizar ou remover `docs/dashboard-dependencies.md`, pois ele ainda trata modulos implementados como pendentes.
- Criar testes do dashboard com fatura aberta, fatura atual e limite usado.

### A-008 - Relatorio de saldo por conta ignora o parametro `date`

Evidencia:

- `finance/src/main/java/com/zorysa/finance/reports/controller/ReportController.java:64-68`
- `finance/src/main/java/com/zorysa/finance/reports/service/ReportService.java:147-164`

Impacto:

A API aceita `date`, e o frontend mostra filtro de data, mas o service sempre retorna `currentBalance`. O relatorio "saldo na data" fica incorreto.

Sugestao:

- Definir se o relatorio e saldo atual ou saldo historico.
- Se for historico, calcular saldo base mais movimentos ate a data.
- Se for atual, remover `date` do contrato/UI.

### A-009 - Relatorio de despesas de cartao inclui compras canceladas

Evidencia:

- `finance/src/main/java/com/zorysa/finance/reports/service/ReportService.java:215-226`

Impacto:

O relatorio agrega todas as `CardPurchase` do usuario por cartao/categoria, sem filtrar `PurchaseStatus.ACTIVE`. Compras canceladas entram no total de despesas.

Sugestao:

- Filtrar compras ativas no service ou na query.
- Adicionar teste com uma compra ativa e outra cancelada no mesmo periodo.

### A-010 - Recorrencias estao especificadas, mas o modulo nao existe

Evidencia:

- `docs/specs/product-spec.md:145-149`
- `docs/specs/backend-spec.md:131-134`
- `docs/specs/database-model.md:171-183`
- `finance/src/main/resources/db/migration/V006__create_bills.sql:14`
- `finance/src/main/java/com/zorysa/finance/bills/entity/Bill.java:57-58`

Impacto:

O produto promete despesas recorrentes mensais, editar/cancelar recorrencias e o modelo documenta tabela `recurrences`. No codigo existe apenas `recurrence_id` em bills, sem tabela, FK, endpoints, service, frontend ou job de geracao.

Sugestao:

- Implementar modulo `recurrences` completo ou retirar do escopo MVP e ajustar specs.
- Criar migration da tabela `recurrences` com FK em `bills.recurrence_id`.
- Definir geracao: sob demanda ao listar meses, job agendado ou comando manual.

## Achados medios

### M-001 - Fluxo de redefinicao de senha esta incompleto no produto

Evidencia:

- `finance/src/main/java/com/zorysa/finance/auth/service/AuthService.java:89-109`
- `frontend/src/app/routes/AppRoutes.tsx:40-48`
- `frontend/src/features/auth/services/auth.service.ts:17-19`

Impacto:

O backend cria token de reset, mas nao envia e-mail nem retorna token. O frontend tem tela de "esqueci senha", mas nao tem rota/tela para `reset-password`. Para usuario real, o fluxo nao fecha.

Sugestao:

- Integrar envio de e-mail ou, em ambiente dev, expor token por canal controlado.
- Criar rota publica `/reset-password?token=...` no frontend.
- Testar pedido de reset, token expirado, token usado e senha alterada.

### M-002 - `isApproved()` diverge de `getStatus()` quando falta `approvedAt`

Evidencia:

- `finance/src/main/java/com/zorysa/finance/users/entity/User.java:107-112`
- `finance/src/main/java/com/zorysa/finance/users/entity/User.java:142-144`
- `finance/src/main/java/com/zorysa/finance/auth/service/AuthService.java:130-134`

Impacto:

`getStatus()` trata usuario `APPROVED` sem `approvedAt` como pendente, mas `isApproved()` retorna true olhando apenas o enum bruto. Como login usa `isApproved()`, um estado inconsistente no banco pode liberar acesso.

Sugestao:

- Fazer `isApproved()` usar `getStatus() == APPROVED` e/ou exigir `approvedAt != null`.
- Adicionar constraint ou teste de dominio para aprovado sem `approvedAt`.

### M-003 - Regras administrativas de transicao de status sao frouxas

Evidencia:

- `finance/src/main/java/com/zorysa/finance/admin/service/AdminUserService.java:93-110`
- `finance/src/main/java/com/zorysa/finance/admin/service/AdminUserService.java:113-115`

Impacto:

O backend permite aprovar, rejeitar, suspender, reativar ou deletar sem validar transicao, motivo obrigatorio, auto-acao, ultimo admin ou regra por status atual. A UI restringe algumas acoes, mas a API deve ser a fonte da verdade.

Sugestao:

- Definir matriz de transicoes permitidas no service.
- Exigir motivo server-side para reject/suspend/delete.
- Bloquear auto-suspensao/delecao e delecao do ultimo admin.
- Retornar 404 em usuario inexistente, em vez de 400.

### M-004 - CORS e variaveis de seguranca nao usam configuracao do compose

Evidencia:

- `finance/src/main/java/com/zorysa/finance/shared/security/SecurityConfig.java:55-60`
- `docker-compose.yml:24-31`
- `finance/src/main/resources/application.properties:16-25`

Impacto:

O compose define `CORS_ALLOWED_ORIGINS`, mas o backend ignora e usa apenas `http://localhost:5173`. Alem disso, `JWT_SECRET` e credenciais do admin inicial possuem defaults de desenvolvimento.

Sugestao:

- Ler origens permitidas de propriedade configuravel.
- Exigir `JWT_SECRET` forte fora de dev/test.
- Documentar `FIRST_ADMIN_*` no `.env.example` e compose.

### M-005 - Categorias duplicadas por caixa/espacamento podem passar

Evidencia:

- `finance/src/main/java/com/zorysa/finance/categories/service/CategoryService.java:55-58`
- `finance/src/main/java/com/zorysa/finance/categories/service/CategoryService.java:77-83`
- `finance/src/main/java/com/zorysa/finance/categories/repository/CategoryRepository.java:61`

Impacto:

A duplicidade usa igualdade exata. "Mercado" e "mercado" podem virar categorias distintas para o mesmo usuario e tipo. Isso prejudica relatorios e filtros.

Sugestao:

- Normalizar nome para comparacao (`lower(trim(name))`) com indice unico funcional no Postgres.
- Manter nome de exibicao como digitado, se desejado.

### M-006 - Listagem de budgets aceita mes/ano opcionais, mas service assume ambos

Evidencia:

- `finance/src/main/java/com/zorysa/finance/budgets/controller/BudgetController.java:37-43`
- `finance/src/main/java/com/zorysa/finance/budgets/service/BudgetService.java:51-56`
- `finance/src/main/java/com/zorysa/finance/budgets/repository/BudgetRepository.java:21-27`

Impacto:

O frontend sempre envia mes e ano atuais, mas a API publica aceita ambos opcionais. Chamada direta sem um dos parametros pode retornar resultado incoerente ou quebrar por autounboxing quando `calculateSpentAmount(..., month, year)` recebe `null`.

Sugestao:

- Tornar `month` e `year` obrigatorios ou aplicar default de mes/ano atual no controller/service.
- Validar mes entre 1 e 12.
- Testar chamadas com parametros ausentes e parciais.

### M-007 - Filtros de mes/ano podem gerar 500 por valores invalidos

Evidencia:

- `finance/src/main/java/com/zorysa/finance/dashboard/service/DashboardService.java:139`
- `finance/src/main/java/com/zorysa/finance/dashboard/service/DashboardService.java:153`
- `finance/src/main/java/com/zorysa/finance/reports/service/ReportService.java:257-258`
- `finance/src/main/java/com/zorysa/finance/reports/service/ReportService.java:270`

Impacto:

`YearMonth.of` e `LocalDate.of` lancam excecoes para mes 0/13. Sem validacao especifica, a API pode devolver erro tecnico em vez de 400 com mensagem clara.

Sugestao:

- Usar `@Min(1)`, `@Max(12)` e validacao de ano nos controllers.
- Adicionar handler para excecoes de conversao/data quando necessario.

### M-008 - `findCurrentByUserIdAndCreditCardId` pode retornar fatura futura

Evidencia:

- `finance/src/main/java/com/zorysa/finance/invoices/repository/InvoiceRepository.java:31-43`
- `finance/src/main/java/com/zorysa/finance/invoices/service/InvoiceService.java:64-65`

Impacto:

A fatura "atual" e a primeira com `closingDate >= LocalDate.now()`. Se compras futuras ja criaram faturas futuras e nao existe fatura do ciclo atual, a API pode retornar uma fatura futura como atual.

Sugestao:

- Calcular competencia atual com base em data de fechamento/vencimento do cartao.
- Buscar por mes/ano de referencia esperado, nao apenas proxima data de fechamento.

### M-009 - Compras parceladas nao validam escala monetaria

Evidencia:

- `finance/src/main/java/com/zorysa/finance/installments/dto/CreateCardPurchaseRequest.java:10-17`
- `finance/src/main/java/com/zorysa/finance/installments/dto/UpdateCardPurchaseRequest.java:10-17`

Impacto:

`totalAmount` aceita qualquer `BigDecimal` positivo, sem limite de casas decimais/tamanho. O restante do dominio grava `numeric(15,2)`, entao valores com muitas casas dependem do banco/arredondamento implicito.

Sugestao:

- Usar `@Digits(integer = 13, fraction = 2)` ou padrao equivalente usado nos outros DTOs monetarios.
- Testar valores com 3 casas decimais e valores acima do limite.

### M-010 - Filtros e relatorios fazem agregacao em memoria

Evidencia:

- `finance/src/main/java/com/zorysa/finance/dashboard/service/DashboardService.java:56-57`
- `finance/src/main/java/com/zorysa/finance/dashboard/service/DashboardService.java:194-202`
- `finance/src/main/java/com/zorysa/finance/budgets/service/BudgetService.java:109-112`
- `finance/src/main/java/com/zorysa/finance/reports/service/ReportService.java:147-164`
- `finance/src/main/java/com/zorysa/finance/reports/service/ReportService.java:174-203`
- `finance/src/main/java/com/zorysa/finance/reports/service/ReportService.java:215-244`
- `finance/src/main/java/com/zorysa/finance/reports/service/ReportService.java:259-293`

Impacto:

Para poucos dados funciona, mas com uso real pode carregar todas as transacoes, contas, categorias, compras e parcelas do usuario e depois paginar em memoria. Isso degrada performance e pode tornar paginacao incorreta para datasets grandes.

Sugestao:

- Migrar relatorios para queries agregadas/paginadas no banco.
- Criar testes de contrato para ordenacao/paginacao.
- Medir consultas em cenarios com volume.

### M-011 - N+1 queries em relatorios

Evidencia:

- `finance/src/main/java/com/zorysa/finance/reports/service/ReportService.java:184-187`
- `finance/src/main/java/com/zorysa/finance/reports/service/ReportService.java:274-282`
- `finance/src/main/java/com/zorysa/finance/reports/service/ReportService.java:315-327`

Impacto:

O service consulta despesas dentro de cada budget e usa `entityManager.find` para nomes de categoria/cartao/compra linha a linha. Em relatorios grandes, a latencia cresce rapidamente.

Sugestao:

- Precarregar nomes em mapas por ids ou usar joins/projections.
- Calcular despesas por categoria em uma query agrupada.

### M-012 - Status `OVERDUE` e `CANCELED` de bills precisam de regra clara

Evidencia:

- `finance/src/main/java/com/zorysa/finance/bills/service/BillService.java:74-116`
- `finance/src/main/java/com/zorysa/finance/bills/service/BillService.java:182-186`
- `finance/src/main/resources/db/migration/V006__create_bills.sql:1`

Impacto:

O filtro trata vencidas por `dueDate < now` e status diferente de `PAID`, mas o status persistido `OVERDUE` nao parece ser atualizado por job/comando. Tambem e possivel aceitar status fora do fluxo esperado se DTO permitir enum.

Sugestao:

- Decidir se `OVERDUE` e status persistido ou status derivado.
- Se persistido, criar job/comando transacional para atualizar.
- Se derivado, remover enum ou nao aceitar `OVERDUE` em create/update.

### M-013 - Metas tem status `CANCELED`, mas nao ha fluxo de cancelamento

Evidencia:

- `finance/src/main/java/com/zorysa/finance/goals/entity/Goal.java:138-157`
- `finance/src/main/java/com/zorysa/finance/goals/service/GoalService.java:71-75`

Impacto:

A entidade suporta cancelar, mas service/controller/frontend expoem delete fisico. Alem disso, `updateDetails` recalcula status e poderia reativar uma meta cancelada caso o fluxo apareca depois.

Sugestao:

- Criar endpoint `cancel` e listar metas canceladas como historico, ou remover status se nao for parte do produto.
- Garantir que meta cancelada nao volta a ativa em update comum.

### M-014 - Frontend de auth pode ficar com usuario nulo ou role antiga apos refresh

Evidencia:

- `frontend/src/features/auth/services/auth.service.ts:21-30`
- `frontend/src/shared/lib/auth-token-store.ts:36-43`

Impacto:

O refresh salva tokens, mas so atualiza usuario se `data.user` vier no response. Se `sessionStorage` perder `authUser`, a sessao pode ficar autenticada com access token e usuario nulo. Se role/status do usuario mudar, o frontend pode continuar usando dados antigos.

Sugestao:

- Fazer refresh retornar usuario atual ou chamar `/me` apos refresh.
- Atualizar role/dados do usuario a cada refresh bem-sucedido.

## Achados baixos

### B-001 - `forgotPassword` nao deixa rastro operacional do token em dev

Evidencia:

- `finance/src/main/java/com/zorysa/finance/auth/service/AuthService.java:89-99`

Impacto:

Em dev/test manual, o token gerado nao e acessivel sem consultar banco. Isso dificulta validar o fluxo ate a tela de reset.

Sugestao:

- Em profile dev, registrar token bruto de forma controlada ou retornar em resposta exclusiva de dev.
- Nunca fazer isso em producao.

### B-002 - `GlobalExceptionHandler` cobre poucos erros comuns de API

Evidencia:

- `finance/src/main/java/com/zorysa/finance/shared/exception/GlobalExceptionHandler.java:21-66`

Impacto:

Erros de integridade, enum invalido, parametro de data invalido, tipo incorreto e exceptions de `YearMonth` podem virar respostas tecnicas/inconsistentes.

Sugestao:

- Adicionar handlers para `DataIntegrityViolationException`, `MethodArgumentTypeMismatchException`, `HttpMessageNotReadableException` e `DateTimeException`.
- Padronizar `code`, `message`, `path` e detalhes de campo.

### B-003 - Docker do frontend roda servidor de desenvolvimento

Evidencia:

- `frontend/Dockerfile:1-10`

Impacto:

O container do frontend usa `npm run dev`, adequado para desenvolvimento, mas ruim para deploy. O build de producao passou, mas nao e usado pelo Dockerfile.

Sugestao:

- Criar Dockerfile multi-stage com `npm run build` e servir `dist` via nginx/caddy ou servidor estatico.
- Manter compose de dev separado se necessario.

### B-004 - Docker do backend empacota com `-DskipTests`

Evidencia:

- `finance/Dockerfile:6-9`

Impacto:

Imagem pode ser gerada sem rodar testes. Isso nao e bug funcional, mas reduz seguranca de pipeline se o Docker build for usado como validacao principal.

Sugestao:

- Rodar testes no CI antes do build de imagem.
- Ou remover `-DskipTests` do stage de build se o tempo for aceitavel.

### B-005 - Frontend gera bundle grande

Evidencia:

- `npm run build` gerou aviso: chunk JS minificado com 689.64 kB, acima de 500 kB.

Impacto:

Pode afetar carregamento inicial em conexoes lentas.

Sugestao:

- Aplicar code splitting por rotas.
- Avaliar `manualChunks` para vendor/react/lucide.

### B-006 - Painel de pendencias futuras do dashboard esta sempre oculto

Evidencia:

- `frontend/src/features/dashboard/components/FutureDependencyPanel.tsx:3-8`

Impacto:

O componente existe, mas `futureItems` e uma lista vazia constante. Ele nao aparece e nao comunica nada. Com modulos ja implementados, o painel parece ter perdido funcao.

Sugestao:

- Remover componente se nao for mais necessario, ou alimentar pendencias reais temporarias.

## Funcionalidades faltantes ou incompletas

### F-001 - Recorrencias mensais

Status: faltante.

Evidencia:

- Specs citam recorrencias em `docs/specs/product-spec.md:145-149`, `docs/specs/backend-spec.md:131-134` e `docs/specs/database-model.md:171-183`.
- Codigo possui apenas `bills.recurrence_id`.

Minimo esperado:

- CRUD de recorrencia.
- Geracao de bills por competencia.
- Cancelamento sem apagar historico.
- Edicao com regra para ocorrencias futuras vs ja geradas.
- Testes de geracao, idempotencia e isolamento por usuario.

### F-002 - Reset password completo no frontend

Status: incompleto.

Minimo esperado:

- Tela de nova senha com token.
- Tratamento de token expirado/usado.
- Fluxo de retorno ao login.

### F-003 - Dashboard financeiro completo

Status: parcialmente implementado.

Faltam ou estao zerados:

- Faturas abertas.
- Fatura atual.
- Total de faturas em aberto.
- Limite utilizado.
- Integracao com contas a pagar, orcamentos e metas, se isso ja fizer parte do dashboard esperado.

### F-004 - Estorno/cancelamento financeiro atomico

Status: incompleto.

Fluxos que precisam existir como comandos de dominio:

- Estornar pagamento de bill.
- Estornar pagamento de fatura.
- Cancelar compra parcelada com regras sobre faturas pagas.
- Cancelar transacao vinculada sem quebrar entidade origem.

### F-005 - Desarquivamento de conta/cartao

Status: ausente ou incompleto.

O sistema permite arquivar conta e cartao, mas nao foi encontrado fluxo simetrico claro para desarquivar. Se arquivamento e reversivel, faltam endpoint, UI e testes. Se nao for reversivel, a UI deve comunicar isso.

### F-006 - Politica de historico para entidades financeiras

Status: incompleta.

Entidades como conta, categoria e meta usam delete em alguns lugares. Para dominio financeiro, normalmente e melhor arquivar/cancelar e preservar historico.

## Divergencias entre specs e implementacao

- `DELETE /api/accounts/{id}`: spec pede bloquear exclusao com historico ou usar arquivamento; codigo deleta fisicamente.
- `DELETE /api/categories/{id}`: spec pede 409 se usada; codigo delega ao banco e nao trata integridade.
- Recorrencias: specs descrevem modulo; codigo nao implementa.
- Dashboard: doc diz para manter campos zerados enquanto modulos nao existem; modulos de cartao/fatura ja existem.
- Relatorio de saldo por conta: API aceita `date`; service ignora.
- Bloqueio de usuarios nao aprovados: specs indicam bloquear acesso financeiro, mas JWT/refresh nao revalidam status depois da emissao.

## Sugestoes de testes novos

Prioridade alta:

- Auth: refresh negado apos suspensao/rejeicao/delecao.
- Auth: access token antigo nao acessa rota financeira apos suspensao.
- Cartao: compra acima do limite disponivel deve falhar.
- Cartao: edicao de compra que excede limite deve falhar.
- Bill: create/update com `PAID` nao pode deixar bill paga sem transacao.
- Bill/Transaction: cancelar/deletar transacao de bill paga deve falhar ou estornar bill atomica.
- Invoice: pagamento recalcula total por parcelas.
- Account: conta arquivada nao aceita nova transacao/pagamento.
- Category: delete de categoria em uso retorna 409.

Prioridade media:

- Budget: listagem sem mes/ano e com mes invalido.
- Dashboard/Reports: mes 0/13 retorna 400.
- Reports: saldo por data aplica filtro ou campo e removido.
- Reports: compras canceladas nao entram em despesas de cartao.
- Goals: cancelamento preserva status.
- Admin: motivo obrigatorio e transicoes invalidas.

## Prioridades recomendadas

1. Fechar seguranca de sessao: revalidar status no JWT/refresh e revogar tokens em acoes administrativas.
2. Fechar regras financeiras de maior impacto: limite de cartao, bills pagas sem transacao e transacoes vinculadas a bills.
3. Proteger historico financeiro: delete de contas/categorias e uso de contas arquivadas.
4. Corrigir dashboard/relatorios que entregam numeros incorretos ou incompletos.
5. Implementar ou retirar do escopo as recorrencias.
6. Completar reset de senha ponta a ponta.
7. Mover agregacoes pesadas para queries no banco conforme o volume real crescer.

## Observacoes finais

A suite atual passar e um bom sinal de estabilidade, mas ela ainda valida principalmente contratos existentes. Os problemas mais importantes desta auditoria sao lacunas de regras ainda nao codificadas em testes. Recomendo transformar os achados criticos e altos em testes primeiro, reproduzindo o comportamento atual, e entao corrigir os services mantendo as regras financeiras concentradas no backend.
