# Especificação de Produto

## 1. Visão geral

Sistema web de organização financeira pessoal, multiusuário com isolamento total de dados por usuário. Cada pessoa terá cadastro, autenticação, contas, categorias, transações, contas a pagar, cartões, faturas, parcelamentos, orçamentos, metas, dashboard e relatórios próprios.

O sistema também deve ter uma visão administrativa. Novos cadastros não liberam acesso imediatamente: eles ficam com status pendente até que um administrador aprove a conta. O administrador pode aprovar, negar, suspender, reativar ou excluir/desativar usuários.

Na primeira versão não existe grupo financeiro, convite, compartilhamento, divisão de despesas, reembolso ou visualização de dados de outro usuário.

## 2. Objetivo

Permitir que cada usuário registre, acompanhe e analise sua vida financeira pessoal em um único sistema, com visão clara de saldo, receitas, despesas, vencimentos, cartão de crédito, parcelamentos, metas e orçamento mensal.

## 3. Público-alvo

- Pessoas físicas que desejam organizar finanças pessoais.
- Usuários que precisam controlar contas bancárias, carteira física, cartão de crédito e compras parceladas.
- Inicialmente duas pessoas usando o mesmo sistema, porém com dados completamente isolados.

## 4. Problema que resolve

O sistema reduz a dispersão de informações financeiras em planilhas, aplicativos bancários e anotações manuais. Ele centraliza lançamentos, vencimentos, limites de cartão, faturas, metas e relatórios para facilitar decisões financeiras.

## 5. Escopo do MVP

- Autenticação, recuperação e alteração de senha.
- Aprovação administrativa de novos usuários.
- Painel administrativo básico de usuários.
- Perfil básico do usuário.
- Contas financeiras.
- Categorias de receitas e despesas.
- Transações de receita e despesa.
- Contas a pagar.
- Despesas recorrentes mensais.
- Orçamentos mensais por categoria.
- Metas financeiras.
- Cartões de crédito.
- Faturas de cartão.
- Compras à vista e parceladas no cartão.
- Dashboard financeiro.
- Relatórios básicos.

## 6. Fora do escopo do MVP

- Exportação PDF/Excel.
- Importação de extrato bancário.
- Integração bancária.
- Notificações por e-mail.
- Aplicativo mobile.
- IA para categorização automática.
- Compartilhamento financeiro entre usuários.
- Divisão de despesas, grupos, convites e reembolsos.

## 7. Principais jornadas do usuário

1. Solicitar cadastro e aguardar aprovação do administrador.
2. Administrador revisa usuários pendentes e aprova ou nega a criação da conta.
3. Usuário aprovado faz login e acessa área privada.
4. Cadastrar contas financeiras com saldo inicial.
5. Criar categorias personalizadas ou usar categorias padrão sugeridas.
6. Registrar receitas e despesas pagas ou pendentes.
7. Cadastrar contas a pagar e marcar pagamentos.
8. Criar orçamento mensal por categoria e acompanhar consumo.
9. Criar metas financeiras e atualizar progresso.
10. Cadastrar cartão, registrar compras e acompanhar faturas.
11. Pagar fatura usando uma conta financeira.
12. Consultar dashboard e relatórios por período.

## 8. Requisitos funcionais por módulo

### Autenticação e perfil

- Permitir solicitação de cadastro com nome, e-mail e senha.
- Criar novos cadastros com status `PENDING_APPROVAL`.
- Impedir acesso normal de usuários pendentes, negados ou suspensos.
- Permitir login, logout, refresh token, recuperação e redefinição de senha apenas para usuários aptos conforme status.
- Gerar e validar JWT para rotas privadas.
- Permitir visualizar e editar perfil básico.
- Permitir alteração de senha mediante senha atual ou fluxo seguro equivalente.


### Administração de usuários

- Exibir painel administrativo separado da área financeira comum.
- Listar usuários por status: pendente, aprovado, negado, suspenso e excluído/inativo.
- Exibir dados básicos da solicitação: nome, e-mail, data da solicitação, status atual e datas de decisão.
- Aprovar criação de conta pendente.
- Negar criação de conta pendente.
- Aprovar posteriormente um usuário que havia sido negado.
- Suspender usuário já aprovado.
- Reativar usuário suspenso.
- Excluir ou desativar usuário, conforme política segura do sistema.
- Alterar livremente o status administrativo do usuário, respeitando regras de auditoria.
- Registrar qual admin executou a decisão e quando.
- Bloquear acesso financeiro de usuários pendentes, negados, suspensos ou excluídos.
- Impedir que administradores acessem dados financeiros pessoais dos usuários no MVP, salvo se uma funcionalidade futura de suporte/auditoria for aprovada explicitamente.

Funcionalidades administrativas interessantes para evolução:

- Histórico de decisões administrativas por usuário.
- Filtros e busca por nome, e-mail, status e data.
- Motivo obrigatório ao negar, suspender ou excluir.
- Tela de métricas operacionais: usuários pendentes, aprovados, suspensos e cadastros por mês.
- Registro de último login.
- Notificação visual de novos cadastros pendentes.
- Exportação de lista de usuários para auditoria.

### Contas financeiras

- Criar, listar, editar, arquivar e excluir contas do usuário autenticado.
- Tipos: conta corrente, poupança, carteira física, conta digital, investimento, vale-alimentação/refeição e outros.
- Registrar saldo inicial e manter saldo atual.
- Calcular saldo atual considerando transações realizadas.
- Toda conta pertence exclusivamente ao usuário autenticado.

### Categorias

- Criar, listar, editar e excluir categorias do usuário.
- Suportar categorias de receita e despesa.
- Sugerir categorias padrão.
- Exemplos de despesa: moradia, alimentação, transporte, saúde, educação, lazer, assinaturas, compras, dívidas, outros.
- Exemplos de receita: salário, freelance, reembolso, venda, rendimento, outros.

### Transações

- Criar, listar, editar e excluir receitas e despesas.
- Filtrar por período, tipo, categoria, conta e status.
- Campos: descrição, valor, tipo, data, categoria, conta, status e observação.
- Status: pendente, pago/recebido e cancelado.
- Receita recebida aumenta saldo da conta.
- Despesa paga diminui saldo da conta.
- Despesa paga deve ser bloqueada quando a conta financeira nao tiver saldo suficiente.
- Transações pendentes não alteram saldo realizado, mas compõem saldo previsto.

### Contas a pagar

- Cadastrar conta com valor, vencimento, categoria, conta associada e status.
- Listar próximas contas e contas vencidas.
- Marcar como paga.
- Ao pagar, registrar ou atualizar despesa correspondente.
- Bloquear pagamento quando a conta financeira nao tiver saldo suficiente.
- Destacar visualmente vencimentos próximos e atrasados.

### Despesas recorrentes

- Criar recorrência mensal com valor, categoria, conta e período.
- Gerar automaticamente ou sugerir lançamento no mês correspondente.
- Editar e cancelar recorrências.
- Exemplos: aluguel, internet, energia e assinaturas.

### Orçamentos

- Criar orçamento por categoria para um mês, vários meses ou sem data final.
- Definir limite de gasto mensal.
- Calcular gasto realizado por categoria.
- Exibir restante, percentual utilizado e alerta de estouro.
- Permitir consulta de meses anteriores.
- Impedir orçamentos sobrepostos para a mesma categoria.

### Metas financeiras

- Criar, editar e excluir metas.
- Campos: nome, valor alvo, valor atual, prazo e descrição.
- Atualizar progresso manualmente.
- Exibir percentual de conclusão.

### Cartão de crédito e faturas

- Cadastrar, editar e arquivar cartões.
- Definir nome, limite total, dia de fechamento e dia de vencimento.
- Ao editar fechamento ou vencimento do cartão, faturas abertas e futuras devem refletir as novas datas; faturas pagas mantêm o histórico.
- Listar cartões com limite total, utilizado e disponível.
- Criar faturas por competência conforme compras.
- Agrupar compras por mês considerando fechamento e vencimento.
- Marcar fatura como paga.
- Compra no cartão não altera saldo de conta; pagamento de fatura altera.

### Compras parceladas

- Registrar compra à vista ou parcelada no cartão.
- Informar valor total, quantidade de parcelas e data da compra.
- Gerar parcelas automaticamente e associá-las às faturas corretas.
- Ajustar diferença de arredondamento na última parcela.
- Bloquear edição quando houver fatura paga relacionada.
- Cancelar compra sem alterar faturas pagas.

### Dashboard

- Exibir saldo total atual, receitas do mês, despesas do mês, saldo mensal e saldo previsto.
- Exibir contas próximas, atrasadas, faturas abertas, fatura atual e limite utilizado.
- Exibir maiores gastos por categoria, progresso de orçamentos e metas.
- Exibir gráficos de receitas x despesas e despesas por categoria.

### Relatórios

- Relatórios de receitas, despesas, despesas por categoria, evolução mensal, saldo por conta, orçamento planejado x realizado, gastos no cartão e parcelas futuras.
- Filtros por mês, ano, categoria, conta e cartão.
- Exibição em gráficos e tabelas.

## 9. Requisitos não funcionais

- Segurança: todas as rotas privadas exigem JWT válido.
- Privacidade: isolamento obrigatório por `user_id`.
- Auditoria técnica: timestamps de criação e atualização.
- Consistência financeira: usar valores decimais exatos.
- Usabilidade: interface responsiva e feedback claro de erro, loading e vazio.
- Performance: listagens paginadas e índices em filtros principais.
- Manutenibilidade: back-end em monólito modular e front-end feature-based.
- Portabilidade: execução local com Docker Compose.
- Testabilidade: testes unitários, integração e isolamento de dados.

## 10. Regras de negócio gerais

- O usuário autenticado é sempre o dono dos dados criados.
- O cadastro de usuário não libera acesso automaticamente.
- O usuário só acessa o sistema financeiro quando seu status for `APPROVED`.
- Admin pode alterar status do usuário entre pendente, aprovado, negado, suspenso e excluído/inativo.
- Usuário suspenso perde acesso às rotas privadas até reativação.
- Ações administrativas devem ser auditáveis.
- Admin não deve visualizar dados financeiros pessoais dos usuários no MVP.
- O sistema nunca deve aceitar `userId` livre no corpo de criação de dados financeiros.
- Nenhuma consulta financeira pode retornar dados de outro usuário.
- Exclusões de entidades usadas por histórico financeiro devem ser restritas, arquivadas ou validadas.
- Valores monetários devem ser positivos, exceto ajustes explicitamente modelados.
- Saldo realizado considera apenas transações pagas/recebidas e faturas pagas.
- Saldo previsto pode considerar pendências futuras.
- Cartão arquivado não aceita novas compras.

## 11. Critérios de aceite gerais

- Dado que dois usuários existem, quando um consulta qualquer listagem privada, então somente seus próprios dados são retornados.
- Dado que um novo usuário solicita cadastro, quando o cadastro é enviado, então a conta fica pendente e não permite acesso financeiro.
- Dado que um admin aprova uma conta pendente, quando o usuário faz login, então ele acessa normalmente o sistema.
- Dado que um admin nega ou suspende uma conta, quando o usuário tenta acessar, então o sistema bloqueia o acesso e exibe uma tela explicando o status da conta.
- Dado que um admin altera uma conta negada para aprovada, quando o usuário faz login, então o acesso passa a ser permitido.
- Dado que estou autenticado, quando crio uma despesa paga vinculada a uma conta, então o saldo da conta é reduzido.
- Dado que registro compra no cartão, quando consulto saldo da conta, então o saldo não muda até a fatura ser paga.
- Dado que pago uma fatura com uma conta, quando consulto a conta, então o saldo é reduzido pelo total da fatura.
- Dado que uma fatura já foi paga, quando tento pagá-la novamente, então o sistema rejeita a operação.
- Dado que uma compra tem parcela em fatura paga, quando tento editá-la, então o sistema bloqueia a edição.

## 12. Glossário

- Conta financeira: origem ou destino de dinheiro, como banco, carteira ou conta digital.
- Categoria: classificação de receita ou despesa.
- Transação: lançamento financeiro de receita ou despesa.
- Conta a pagar: obrigação futura ou pendente com vencimento.
- Recorrência: regra para gerar despesa repetida mensalmente.
- Orçamento: limite mensal planejado para uma categoria.
- Meta: objetivo financeiro com valor alvo.
- Cartão: meio de compra com limite e faturas.
- Fatura: agrupamento de compras do cartão em uma competência.
- Parcela: parte de uma compra parcelada associada a uma fatura.
- Saldo realizado: saldo considerando apenas movimentações efetivadas.
- Saldo previsto: saldo considerando pendências futuras.
- Admin: usuário com permissão administrativa para controlar acesso de outros usuários ao sistema.
- Status do usuário: estado administrativo que define se a conta pode acessar o sistema.
