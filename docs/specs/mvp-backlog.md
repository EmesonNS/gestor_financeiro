# Backlog Inicial do MVP

## Ordem recomendada de implementação

1. Setup do projeto back-end e front-end.
2. Banco de dados e migrations iniciais.
3. Autenticação e segurança.
4. Perfil do usuário.
5. Contas financeiras.
6. Categorias.
7. Transações.
8. Dashboard básico.
9. Contas a pagar.
10. Orçamentos.
11. Metas financeiras.
12. Cartões de crédito.
13. Faturas de cartão.
14. Compras parceladas.
15. Relatórios básicos.
16. Testes.
17. Deploy.

## 1. Setup e infraestrutura

Objetivo: criar base executável para back-end, front-end, banco e containers.

Features:

- Projeto Spring Boot Java 21.
- Projeto React TypeScript com Tailwind.
- Docker Compose com PostgreSQL.
- Configuração de ambientes.

Tarefas back-end:

- Criar projeto Spring Boot com dependências.
- Configurar PostgreSQL, JPA, Flyway e profiles.
- Criar health check básico.

Tarefas front-end:

- Criar projeto React TypeScript.
- Configurar Tailwind, Router, Query Client e cliente HTTP.
- Criar layouts base.

Critérios de aceite:

- Dado que o ambiente local está configurado, quando executo Docker Compose, então PostgreSQL sobe corretamente.
- Dado que inicio back-end e front-end, quando acesso as URLs locais, então ambos respondem.

Prioridade: P0.

## 2. Autenticação e segurança

Objetivo: proteger rotas e identificar o usuário autenticado.

Features:

- Cadastro, login, logout, refresh token e recuperação de senha.
- JWT e proteção de rotas.

Tarefas back-end:

- Implementar `users` e `auth`.
- Configurar Spring Security e JWT.
- Criar endpoints de auth.
- Implementar refresh token.

Tarefas front-end:

- Criar telas de login, cadastro e recuperação.
- Implementar `ProtectedRoute`.
- Configurar interceptor de token.

História:

```text
Como usuário,
quero criar conta e fazer login,
para acessar meus dados financeiros com segurança.
```

Critérios de aceite:

```text
Dado que informo credenciais válidas,
quando faço login,
então recebo uma sessão autenticada e sou redirecionado ao dashboard.
```

Prioridade: P0.

## 3. Perfil do usuário

Objetivo: permitir consulta e edição básica dos dados do usuário.

Features: visualizar perfil, editar nome, alterar senha.

Tarefas back-end: endpoints `/api/users/me` e alteração de senha.

Tarefas front-end: tela de perfil e formulários.

Critérios de aceite:

```text
Dado que estou autenticado,
quando atualizo meu nome,
então o perfil passa a exibir o novo nome.
```

Prioridade: P0.

## 4. Contas financeiras

Objetivo: manter contas e saldos por usuário.

Features: criar, listar, editar, arquivar e excluir conta.

Tarefas back-end: entidade, migration, CRUD, validação de dono e ajuste de saldo.

Tarefas front-end: listagem, formulário, cards de saldo, arquivamento.

História:

```text
Como usuário autenticado,
quero cadastrar minhas contas financeiras,
para acompanhar meu saldo por origem de dinheiro.
```

Critérios de aceite:

```text
Dado que estou autenticado,
quando cadastro uma conta com saldo inicial,
então a conta aparece na minha listagem com saldo atual igual ao saldo inicial.
```

Prioridade: P0.

## 5. Categorias

Objetivo: classificar receitas e despesas.

Features: categorias padrão e personalizadas.

Tarefas back-end: CRUD, categorias padrão, validação por tipo.

Tarefas front-end: listagem, formulário e filtros por tipo.

Critérios de aceite:

```text
Dado que estou autenticado,
quando crio uma categoria de despesa,
então posso usá-la em despesas futuras.
```

Prioridade: P0.

## 6. Transações

Objetivo: registrar receitas e despesas e refletir saldo.

Features: CRUD, filtros, marcar como paga, cancelar.

Tarefas back-end:

- Implementar regras de impacto no saldo.
- Reverter saldo ao editar, cancelar ou excluir.
- Filtrar sempre por usuário autenticado.

Tarefas front-end: tabela, filtros, formulário, ações de pagamento/cancelamento.

História:

```text
Como usuário autenticado,
quero cadastrar uma despesa,
para acompanhar meus gastos mensais.
```

Critérios de aceite:

```text
Dado que estou autenticado,
quando cadastro uma despesa paga vinculada a uma conta,
então o saldo da conta deve ser reduzido pelo valor da despesa.
```

Prioridade: P0.

## 7. Contas a pagar

Objetivo: controlar vencimentos e transformar pagamentos em despesas.

Features: cadastro, listagem, vencidas, próximas e pagamento.

Tarefas back-end: CRUD, status, pagamento com transação vinculada.

Tarefas front-end: listagem com alertas visuais, formulário e diálogo de pagamento.

Critérios de aceite:

```text
Dado que tenho uma conta a pagar pendente,
quando marco como paga,
então uma despesa paga é criada ou atualizada e o saldo da conta é reduzido.
```

Prioridade: P1.

## 8. Orçamentos

Objetivo: controlar limites mensais por categoria.

Features: orçamento mensal, consumo, restante e alerta de limite.

Tarefas back-end: CRUD e cálculo planejado x realizado.

Tarefas front-end: cards de progresso e formulário.

Critérios de aceite:

```text
Dado que tenho orçamento para alimentação,
quando minhas despesas pagas ultrapassam o limite,
então o orçamento deve indicar limite excedido.
```

Prioridade: P1.

## 9. Metas financeiras

Objetivo: acompanhar objetivos financeiros.

Features: CRUD e atualização de progresso.

Tarefas back-end: entidade, endpoints e cálculo percentual.

Tarefas front-end: cards de metas, formulário e atualização de valor atual.

Critérios de aceite:

```text
Dado que tenho uma meta ativa,
quando atualizo o valor atual,
então o percentual de conclusão é recalculado.
```

Prioridade: P1.

## 10. Cartões de crédito

Objetivo: controlar cartões, limites e compras.

Features: cadastrar, editar, arquivar, listar limites.

Tarefas back-end: entidade, CRUD, cálculo de limite usado e disponível.

Tarefas front-end: listagem, formulário, detalhes do cartão.

Critérios de aceite:

```text
Dado que estou autenticado,
quando crio um cartão com limite e vencimento,
então ele aparece na minha lista com limite total, utilizado e disponível.
```

Prioridade: P1.

## 11. Faturas de cartão

Objetivo: agrupar compras por competência e permitir pagamento.

Features: fatura atual, futuras, histórico, pagamento.

Tarefas back-end:

- Criar/obter faturas conforme fechamento.
- Calcular total da fatura.
- Pagar fatura usando conta financeira.
- Bloquear pagamento duplicado.

Tarefas front-end: telas de fatura, confirmação de pagamento e escolha de conta.

Critérios de aceite:

```text
Dado que tenho uma fatura aberta,
quando pago a fatura usando uma conta financeira,
então o saldo da conta deve ser reduzido apenas nesse momento.
```

```text
Dado que uma fatura já está paga,
quando tento pagá-la novamente,
então o sistema bloqueia o pagamento duplicado.
```

Prioridade: P1.

## 12. Compras parceladas

Objetivo: registrar compras no cartão e gerar parcelas automaticamente.

Features: compra à vista, compra parcelada, parcelas futuras, edição/cancelamento seguro.

Tarefas back-end:

- Gerar parcelas.
- Associar parcelas às faturas corretas.
- Ajustar arredondamento na última parcela.
- Bloquear edição quando houver fatura paga.

Tarefas front-end: formulários de compra, detalhes e listagem de parcelas futuras.

Critérios de aceite:

```text
Dado que cadastro uma compra parcelada em 10 vezes,
quando salvo a compra,
então o sistema deve gerar 10 parcelas e associá-las às faturas corretas.
```

```text
Dado que uma compra possui parcela em fatura paga,
quando tento editar a compra,
então o sistema deve bloquear a edição.
```

Prioridade: P1.

## 13. Dashboard

Objetivo: apresentar visão financeira inicial.

Features: resumo mensal, saldos, contas, faturas, orçamentos, metas e gráficos.

Tarefas back-end: endpoints agregados e consultas otimizadas.

Tarefas front-end: cards, gráficos e listas compactas.

Critérios de aceite:

```text
Dado que tenho receitas, despesas e faturas no mês,
quando acesso o dashboard,
então vejo saldo total, receitas, despesas, faturas e gráficos do período.
```

Prioridade: P1.

## 14. Relatórios

Objetivo: permitir análise financeira básica.

Features: relatórios por período, categoria, conta, orçamento, cartão e parcelas futuras.

Tarefas back-end: endpoints analíticos com filtros.

Tarefas front-end: filtros, tabelas e gráficos.

Critérios de aceite:

```text
Dado que seleciono um período,
quando gero relatório de despesas por categoria,
então vejo os totais agrupados por categoria apenas com meus dados.
```

Prioridade: P2.

## 15. Testes

Objetivo: garantir regras críticas e isolamento de dados.

Features: testes unitários, integração e contrato básico.

Tarefas back-end:

- Testar autenticação.
- Testar isolamento entre usuários.
- Testar saldo em transações.
- Testar cartão, faturas e parcelamentos.

Tarefas front-end:

- Testar formulários críticos.
- Testar rotas protegidas.
- Testar hooks principais com mocks.

Critérios de aceite:

```text
Dado que existem dados de dois usuários,
quando executo os testes de autorização,
então nenhum endpoint retorna dados de outro usuário.
```

Prioridade: P0 contínua.

## 16. Deploy

Objetivo: disponibilizar ambiente executável.

Features: build, Dockerfiles, variáveis e documentação de execução.

Tarefas back-end: Dockerfile, profile produtivo e configuração segura.

Tarefas front-end: build estático e configuração de API base URL.

Critérios de aceite:

```text
Dado que executo o processo de deploy,
quando os containers sobem,
então a aplicação fica acessível e conectada ao banco.
```

Prioridade: P2.
