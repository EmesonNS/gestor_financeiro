# Backlog V2 pos-auditoria

Data: 2026-07-01

Fonte principal: `docs/auditoria-completa-projeto-2026-07-01.md`.

Esta versao substitui a ordem inicial do MVP para corrigir lacunas de seguranca, consistencia financeira e funcionalidades faltantes encontradas na auditoria. O objetivo agora nao e apenas adicionar telas, mas fechar invariantes de dominio: usuario bloqueado nao acessa, saldo nao fica inconsistente, historico financeiro nao se perde e relatorios/dashboard mostram numeros confiaveis.

## Escopo desta versao

Em escopo:

- Revalidacao de usuario aprovado em access token e refresh token.
- Regras administrativas server-side.
- Regras financeiras cruzando transacoes, contas a pagar, cartoes, faturas e contas arquivadas.
- Recorrencias mensais completas.
- Dashboard e relatorios com dados ja suportados pelos modulos atuais.
- Melhorias de validacao, erros, performance inicial e infraestrutura.
- Ajustes de frontend necessarios para refletir regras de backend.

Fora de escopo por decisao do usuario:

- Recuperacao/redefinicao de senha completa. Deve permanecer documentada como diferida.

## Prioridades

- P0: bloqueia confianca, seguranca ou integridade financeira.
- P1: funcionalidade obrigatoria para fechar o MVP corretamente.
- P2: melhoria importante, mas pode vir apos os fluxos P0/P1.

## Requisitos rastreaveis

| ID | Prioridade | Area | Requisito |
|---|---:|---|---|
| SEC-01 | P0 | Auth | Refresh token deve revalidar status administrativo antes de emitir novos tokens. |
| SEC-02 | P0 | Auth | Access token antigo nao pode acessar rotas privadas se o usuario deixou de estar aprovado. |
| SEC-03 | P0 | Auth/Admin | Suspender, rejeitar ou deletar usuario deve revogar refresh tokens ativos. |
| SEC-04 | P0 | Users | `isApproved()` deve refletir status efetivo, incluindo `approvedAt`. |
| SEC-05 | P1 | Config | CORS, JWT secret e admin inicial devem ser configuraveis por ambiente. |
| ADM-01 | P0 | Admin | Backend deve validar matriz de transicao de status de usuario. |
| ADM-02 | P0 | Admin | Motivo deve ser obrigatorio para rejeitar, suspender e deletar/desativar usuario. |
| ADM-03 | P0 | Admin | Admin nao pode suspender/deletar a propria conta nem remover o ultimo admin ativo. |
| ADM-04 | P1 | Admin | Listagem admin deve suportar filtros reais por status, busca e periodo. |
| ACC-01 | P0 | Accounts | Conta com historico financeiro nao pode ser deletada fisicamente. |
| ACC-02 | P0 | Accounts | Conta arquivada nao pode ser usada em novas transacoes, pagamentos de bills ou faturas. |
| ACC-03 | P1 | Accounts | Conta arquivada deve poder ser desarquivada quando o usuario decidir reativar. |
| CAT-01 | P0 | Categories | Deletar categoria em uso deve retornar conflito de dominio, nao erro tecnico. |
| CAT-02 | P1 | Categories | Nome de categoria deve ser unico por usuario/tipo ignorando caixa e espacos externos. |
| ERR-01 | P0 | Shared | API deve padronizar erros comuns de integridade, enum invalido, data invalida e tipo invalido. |
| TRX-01 | P0 | Transactions | Marcar transacao como paga/recebida so pode ocorrer quando ela esta pendente. |
| TRX-02 | P0 | Transactions/Bills | Transacao vinculada a bill paga nao pode ser editada, cancelada ou deletada isoladamente. |
| TRX-03 | P1 | Transactions | Estorno/cancelamento deve ser comando de dominio atomico quando houver vinculo com origem. |
| BILL-01 | P0 | Bills | Create/update nao podem criar bill `PAID` sem fluxo de pagamento dedicado. |
| BILL-02 | P0 | Bills | Pagamento de bill deve criar transacao, debitar saldo e vincular tudo atomicamente. |
| BILL-03 | P1 | Bills | Status vencida/cancelada deve ter regra unica: derivado ou persistido, mas nao ambiguo. |
| CARD-01 | P0 | Cards | Compra no cartao nao pode exceder limite disponivel. |
| CARD-02 | P1 | Cards | Valores de compras parceladas devem validar escala monetaria `numeric(15,2)`. |
| CARD-03 | P1 | Cards | Cartao arquivado nao aceita compra e deve ter fluxo claro de desarquivamento. |
| INV-01 | P0 | Invoices | Pagamento de fatura deve recalcular total pelas parcelas no momento do pagamento. |
| INV-02 | P1 | Invoices | Fatura atual deve ser calculada por competencia do ciclo, nao pela primeira fatura futura. |
| INV-03 | P0 | Invoices | Fatura nao pode ser paga usando conta arquivada. |
| REC-01 | P1 | Recurrences | Implementar CRUD de recorrencias mensais. |
| REC-02 | P1 | Recurrences | Geracao de bills por recorrencia deve ser idempotente por competencia. |
| REC-03 | P1 | Recurrences | Cancelar recorrencia nao apaga bills ja geradas. |
| BUD-01 | P1 | Budgets | Listagem de orcamentos deve exigir ou aplicar default seguro para mes/ano. |
| BUD-02 | P2 | Budgets | Consumo de orcamento deve migrar para agregacao no banco. |
| GOAL-01 | P1 | Goals | Meta deve ter fluxo de cancelamento sem delete fisico obrigatorio. |
| DASH-01 | P1 | Dashboard | Dashboard deve incluir faturas abertas, fatura atual, limite usado e limite disponivel. |
| DASH-02 | P1 | Dashboard | Remover ou atualizar doc/componente de pendencias futuras do dashboard. |
| REP-01 | P1 | Reports | Relatorio de saldo por conta deve respeitar `date` ou remover filtro do contrato/UI. |
| REP-02 | P1 | Reports | Relatorio de despesas de cartao deve ignorar compras canceladas. |
| REP-03 | P1 | Reports | Filtros de mes/ano devem validar intervalo e retornar 400 em entrada invalida. |
| REP-04 | P2 | Reports | Relatorios devem reduzir filtros em memoria, N+1 e paginacao pos-processada. |
| FE-01 | P0 | Frontend/Auth | Refresh de sessao deve manter usuario/role atualizados. |
| FE-02 | P0 | Frontend/Forms | Formularios devem ocultar ou bloquear acoes invalidas que o backend tambem bloqueia. |
| FE-03 | P1 | Frontend/Finance | UI deve mostrar arquivado/desarquivar e impedir uso de entidades arquivadas. |
| FE-04 | P1 | Frontend/Bills | Bill form nao deve permitir status pago; pagamento deve ocorrer por dialogo dedicado. |
| FE-05 | P1 | Frontend/Cards | Compra no cartao deve exibir limite disponivel e validar antes de enviar. |
| FE-06 | P1 | Frontend/Dashboard | Dashboard deve exibir indicadores reais de faturas/cartoes/limites. |
| FE-07 | P1 | Frontend/Reports | Filtros de relatorios devem espelhar validacoes do backend. |
| FE-08 | P2 | Frontend/Build | Aplicar lazy loading/code splitting para reduzir bundle inicial. |
| INF-01 | P1 | Infra | Docker/compose devem separar dev/prod e expor variaveis obrigatorias. |
| TEST-01 | P0 | Tests | Cada correcao P0/P1 deve nascer com teste que reproduz a falha atual. |

## Ordem recomendada

1. Seguranca de sessao e administracao.
2. Invariantes financeiras P0.
3. Historico/arquivamento e validacoes globais.
4. Recorrencias.
5. Dashboard e relatorios corretos.
6. Ajustes frontend integrados.
7. Infra, performance e refinamentos P2.

## Historias e tarefas

### 1. Seguranca de sessao e acesso administrativo

Prioridade: P0.

Requisitos: SEC-01, SEC-02, SEC-03, SEC-04, ADM-01, ADM-02, ADM-03, FE-01, TEST-01.

Historia:

```text
Como administrador,
quero que uma suspensao ou rejeicao tenha efeito imediato,
para impedir acesso financeiro de usuarios que nao estao aprovados.
```

Criterios de aceite:

```text
Dado que um usuario aprovado possui access token e refresh token validos,
quando um admin suspende esse usuario,
entao o refresh token passa a falhar e novas requisicoes privadas com access token antigo nao acessam dados financeiros.
```

```text
Dado que um admin tenta rejeitar, suspender ou deletar um usuario,
quando o motivo nao e informado,
entao a API retorna 400 e nao altera o status.
```

```text
Dado que existe apenas um admin ativo,
quando esse admin tenta deletar ou suspender a propria conta,
entao a API retorna 409 e preserva a conta administrativa.
```

Tarefas atomicas:

| Task | Requisitos | Onde | O que fazer | Verificacao |
|---|---|---|---|---|
| T1.1 | SEC-04 | `users/entity`, testes de dominio | Fazer `isApproved()` usar status efetivo e `approvedAt`; cobrir estado inconsistente. | Teste unitario de `User`. |
| T1.2 | SEC-01 | `auth/service` | Revalidar status no refresh antes de rotacionar token. | Teste de refresh apos suspensao/rejeicao/delecao. |
| T1.3 | SEC-02 | `auth/security` | Rejeitar usuario nao aprovado no filtro JWT ou no `UserDetailsService`. | Teste controller com token antigo apos suspensao. |
| T1.4 | SEC-03 | `admin/service`, `auth/repository` | Revogar refresh tokens ativos ao suspender/rejeitar/deletar. | Teste de revogacao por acao admin. |
| T1.5 | ADM-01/02/03 | `admin/service` | Criar policy de transicao, motivo obrigatorio, bloqueio de auto-acao e ultimo admin. | Testes de service e controller. |
| T1.6 | FE-01 | `frontend/src/features/auth`, `shared/lib` | Refresh deve atualizar usuario atual ou buscar `/users/me`; limpar sessao em 401/403. | Build frontend e teste manual guiado. |

### 2. Erros globais, configuracao e base de seguranca operacional

Prioridade: P0/P1.

Requisitos: ERR-01, SEC-05, INF-01.

Criterios de aceite:

```text
Dado que a API recebe enum invalido, data invalida ou violacao conhecida de integridade,
quando a requisicao falha,
entao a resposta segue o contrato de erro padrao com status correto e mensagem de dominio.
```

Tarefas atomicas:

| Task | Requisitos | Onde | O que fazer | Verificacao |
|---|---|---|---|---|
| T2.1 | ERR-01 | `shared/exception` | Adicionar handlers para integridade, tipo invalido, JSON invalido e data invalida. | Testes controller. |
| T2.2 | SEC-05 | `shared/security`, properties | Ler CORS de propriedade/env e validar segredo fora de dev/test. | Teste de config e smoke local. |
| T2.3 | INF-01 | `docker-compose.yml`, `.env.example`, Dockerfiles | Documentar variaveis obrigatorias e separar comportamento dev/prod. | `docker compose config` e build. |

### 3. Historico financeiro e entidades arquivadas

Prioridade: P0/P1.

Requisitos: ACC-01, ACC-02, ACC-03, CAT-01, CAT-02, FE-02, FE-03.

Historia:

```text
Como usuario,
quero arquivar entidades antigas sem quebrar historico,
para preservar meus relatorios e impedir uso acidental em novos lancamentos.
```

Criterios de aceite:

```text
Dado que uma conta possui transacoes ou bills vinculadas,
quando tento deleta-la,
entao a API retorna 409 ou arquiva conforme endpoint dedicado, sem apagar historico.
```

```text
Dado que uma conta esta arquivada,
quando tento criar transacao paga, pagar bill ou pagar fatura com ela,
entao a operacao e bloqueada.
```

Tarefas atomicas:

| Task | Requisitos | Onde | O que fazer | Verificacao |
|---|---|---|---|---|
| T3.1 | ACC-01 | `accounts/service`, repos | Bloquear delete de conta com historico; preservar `account_id`. | Teste com transacao/bill vinculada. |
| T3.2 | ACC-02 | `accounts/service`, consumers | Expor validacao de conta ativa para comandos financeiros. | Testes em transactions, bills, invoices. |
| T3.3 | ACC-03 | `accounts/controller/service`, frontend | Criar endpoint e UI de desarquivar conta. | Teste controller e build frontend. |
| T3.4 | CAT-01 | `categories/service`, repos | Checar uso antes de delete e retornar 409. | Teste de categoria usada. |
| T3.5 | CAT-02 | migration/repository/service | Normalizar unicidade por `lower(trim(name))` por usuario/tipo. | Teste repository/service. |
| T3.6 | FE-03 | telas de contas/categorias | Mostrar status arquivado, esconder itens arquivados em selects financeiros e tratar conflito. | Build frontend e verificacao manual. |

### 4. Transacoes e contas a pagar

Prioridade: P0/P1.

Requisitos: TRX-01, TRX-02, TRX-03, BILL-01, BILL-02, BILL-03, FE-04.

Historia:

```text
Como usuario,
quero que pagar uma conta crie uma despesa consistente,
para que saldo, historico e status da conta a pagar nunca fiquem divergentes.
```

Criterios de aceite:

```text
Dado que crio ou edito uma bill,
quando tento salvar status PAID pelo formulario ou API comum,
entao a operacao e bloqueada e o sistema orienta usar o endpoint de pagamento.
```

```text
Dado que uma bill foi paga e possui transacao vinculada,
quando tento cancelar ou excluir a transacao isoladamente,
entao a API bloqueia a operacao ou exige fluxo de estorno da bill.
```

Tarefas atomicas:

| Task | Requisitos | Onde | O que fazer | Verificacao |
|---|---|---|---|---|
| T4.1 | BILL-01 | `bills/service` | Bloquear `PAID` em create/update comum. | Teste create/update PAID. |
| T4.2 | BILL-02 | `bills/service` | Garantir pagamento atomico com transacao, saldo e vinculo. | Teste transacional e saldo insuficiente. |
| T4.3 | TRX-01 | `transactions/service` | `markAsPaid` apenas para `PENDING`. | Teste para CANCELED/PAID/RECEIVED. |
| T4.4 | TRX-02 | `transactions/service`, `bills/repository` | Bloquear alteracao isolada de transacao vinculada a bill paga. | Teste cruzado bill/transaction. |
| T4.5 | TRX-03 | `bills/controller/service` | Criar comando de estorno/cancelamento de pagamento de bill, se desejado no MVP. | Teste de estorno atomico. |
| T4.6 | BILL-03 | `bills/service`, frontend | Definir status vencida como derivado ou persistido e refletir na UI. | Teste de filtros vencidas/proximas. |
| T4.7 | FE-04 | `features/bills` | Remover status pago do form; pagamento somente por dialogo. | Build frontend. |

### 5. Cartoes, compras parceladas e faturas

Prioridade: P0/P1.

Requisitos: CARD-01, CARD-02, CARD-03, INV-01, INV-02, INV-03, FE-05.

Historia:

```text
Como usuario,
quero que compras no cartao respeitem limite e que faturas sejam pagas pelo valor real,
para evitar limite e saldo incorretos.
```

Criterios de aceite:

```text
Dado que meu cartao tem limite disponivel de 100,
quando tento criar compra de 101,
entao a API bloqueia a compra e nenhuma parcela/fatura e criada.
```

```text
Dado que o total persistido da fatura diverge da soma das parcelas,
quando pago a fatura,
entao o pagamento usa a soma recalculada das parcelas e corrige o total.
```

Tarefas atomicas:

| Task | Requisitos | Onde | O que fazer | Verificacao |
|---|---|---|---|---|
| T5.1 | CARD-01 | `installments/service`, `creditcards/service` | Validar limite disponivel em create/update de compra. | Testes acima do limite e edicao excedente. |
| T5.2 | CARD-02 | DTOs de installments | Aplicar `@Digits`/escala monetaria nos requests. | Teste validation com 3 casas. |
| T5.3 | INV-01 | `invoices/service` | Recalcular total por parcelas abertas no pagamento. | Teste de total divergente. |
| T5.4 | INV-02 | `invoices/repository/service` | Calcular fatura atual pela competencia do ciclo. | Teste com fatura futura existente. |
| T5.5 | INV-03 | `invoices/service` | Bloquear pagamento com conta arquivada. | Teste service/controller. |
| T5.6 | CARD-03 | `creditcards/controller/service`, frontend | Definir e implementar desarquivamento de cartao. | Teste controller e build frontend. |
| T5.7 | FE-05 | `features/installments` | Mostrar limite disponivel no formulario e validar antes de submit. | Build frontend e verificacao manual. |

### 6. Recorrencias mensais

Prioridade: P1.

Requisitos: REC-01, REC-02, REC-03.

Historia:

```text
Como usuario,
quero configurar despesas recorrentes mensais,
para gerar contas a pagar sem cadastrar a mesma despesa todo mes.
```

Criterios de aceite:

```text
Dado que tenho uma recorrencia mensal ativa iniciada em julho de 2026,
quando gero ocorrencias de julho a setembro,
entao o sistema cria uma bill pendente por competencia e nao duplica se o comando for repetido.
```

```text
Dado que cancelo uma recorrencia,
quando gero novas competencias futuras,
entao nenhuma nova bill e criada e as bills antigas permanecem no historico.
```

Tarefas atomicas:

| Task | Requisitos | Onde | O que fazer | Verificacao |
|---|---|---|---|---|
| T6.1 | REC-01 | migration/entity/repository | Criar tabela `recurrences` e FK real em bills. | Teste schema persistence. |
| T6.2 | REC-01 | DTO/controller/service | Criar CRUD de recorrencias com validacao de categoria/conta. | Testes service/controller. |
| T6.3 | REC-02 | `recurrences/service` | Criar gerador idempotente de bills por competencia. | Teste idempotencia e intervalo. |
| T6.4 | REC-03 | `recurrences/service` | Cancelar recorrencia sem apagar bills geradas. | Teste cancelamento. |
| T6.5 | REC-01/02 | frontend `features/recurrences` | Criar listagem, formulario e acao de gerar ocorrencias. | Build frontend. |

### 7. Budgets, metas e regras complementares

Prioridade: P1/P2.

Requisitos: BUD-01, BUD-02, GOAL-01.

Tarefas atomicas:

| Task | Requisitos | Onde | O que fazer | Verificacao |
|---|---|---|---|---|
| T7.1 | BUD-01 | `budgets/controller/service` | Aplicar default current month/year ou exigir par completo; validar mes. | Testes de parametros ausentes/parciais. |
| T7.2 | BUD-02 | `budgets/repository/service` | Mover consumo para query agregada por usuario/categoria/periodo. | Teste service com multiplas transacoes. |
| T7.3 | GOAL-01 | `goals/controller/service`, frontend | Criar cancelamento de meta e impedir reativacao acidental por update. | Testes de dominio/service e build. |

### 8. Dashboard e relatorios

Prioridade: P1/P2.

Requisitos: DASH-01, DASH-02, REP-01, REP-02, REP-03, REP-04, FE-06, FE-07.

Historia:

```text
Como usuario,
quero ver dashboard e relatorios com numeros confiaveis,
para tomar decisoes financeiras sem conferir modulo por modulo.
```

Criterios de aceite:

```text
Dado que tenho faturas abertas, fatura atual e limite usado,
quando acesso o dashboard,
entao os cards exibem valores reais e nao zero fixo.
```

```text
Dado que tenho compras canceladas,
quando gero relatorio de despesas de cartao,
entao compras canceladas nao entram nos totais.
```

Tarefas atomicas:

| Task | Requisitos | Onde | O que fazer | Verificacao |
|---|---|---|---|---|
| T8.1 | DASH-01 | `dashboard/service` | Integrar faturas/cartoes/compras aos indicadores. | Teste dashboard com dados de fatura/cartao. |
| T8.2 | DASH-02 | docs/frontend | Atualizar/remover `dashboard-dependencies` e painel vazio. | Build frontend. |
| T8.3 | REP-01 | `reports/service`, frontend | Implementar saldo por data ou remover filtro; recomendacao: implementar historico por transacoes ate a data. | Teste de saldo em data passada. |
| T8.4 | REP-02 | `reports/service` | Filtrar apenas compras ativas em despesas de cartao. | Teste com compra cancelada. |
| T8.5 | REP-03 | controllers/services | Validar mes/ano e intervalos em dashboard/reports/installments. | Teste 400 para mes invalido. |
| T8.6 | REP-04 | `reports/repository/service` | Substituir N+1 e filtros em memoria por queries/projections nas rotas mais pesadas. | Teste de paginacao e regressao. |
| T8.7 | FE-06/07 | `features/dashboard`, `features/reports` | Atualizar cards, filtros e mensagens de erro conforme backend. | Build frontend. |

### 9. Frontend transversal e experiencia

Prioridade: P1/P2.

Requisitos: FE-02, FE-03, FE-04, FE-05, FE-08.

Tarefas atomicas:

| Task | Requisitos | Onde | O que fazer | Verificacao |
|---|---|---|---|---|
| T9.1 | FE-02 | forms financeiros | Desabilitar/ocultar acoes invalidas por status, arquivamento e vinculos. | Build e revisao visual. |
| T9.2 | FE-03 | selects financeiros | Excluir contas/cartoes arquivados de selects de novos comandos. | Build frontend. |
| T9.3 | FE-08 | `app/routes` | Aplicar `React.lazy` por rotas principais e revisar chunking. | `npm run build` sem chunk principal excessivo ou com reducao registrada. |

### 10. Testes e gates finais

Prioridade: P0 continua.

Requisitos: TEST-01.

Tarefas atomicas:

| Task | Requisitos | Onde | O que fazer | Verificacao |
|---|---|---|---|---|
| T10.1 | TEST-01 | backend tests | Adicionar suites de regressao P0/P1 por modulo, sem apagar testes existentes. | `./mvnw test`. |
| T10.2 | TEST-01 | frontend | Garantir build e, se houver runner de teste, cobrir formularios criticos. | `npm run build`; testes se configurados. |
| T10.3 | TEST-01 | docs | Atualizar api/backend/frontend/database specs finais apos implementacao. | Revisao de rastreabilidade. |

## Mapa de dependencias

```text
T1.* seguranca/admin
  -> T3.* historico/arquivamento
  -> T4.* bills/transacoes
  -> T5.* cartoes/faturas

T2.* erros/config pode rodar em paralelo com T1.* apos baseline.

T6.* recorrencias depende de T3.* categorias/contas confiaveis e T4.* bills consistentes.

T8.* dashboard/relatorios depende de T4.*, T5.*, T6.* e T7.*.

T9.* frontend transversal acompanha cada fase, mas validacao final depende das APIs prontas.

T10.* roda continuamente e fecha cada fase.
```

## Gate de aceite por fase

- Fase 1: backend bloqueia usuario nao aprovado em login, refresh e rota privada; admin policy coberta por testes.
- Fase 2: erros globais padronizados e configuracao sem defaults perigosos em ambiente nao dev.
- Fase 3: nenhum fluxo financeiro aceita conta arquivada; delete de conta/categoria nao quebra historico.
- Fase 4: bills pagas, transacoes vinculadas e saldo permanecem consistentes.
- Fase 5: limite de cartao e pagamento de fatura usam valores corretos.
- Fase 6: recorrencias geram bills mensais idempotentes.
- Fase 8: dashboard e relatorios deixam de retornar zeros ou filtros falsos.
- Fase final: `./mvnw test` e `npm run build` passam.

## Itens explicitamente diferidos

- Recuperacao/redefinicao de senha ponta a ponta: email, tela `/reset-password`, token visivel em dev e testes especificos. Manter endpoints atuais, mas nao priorizar nesta rodada.
