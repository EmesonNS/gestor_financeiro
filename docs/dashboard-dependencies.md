# Pendencias do Dashboard por Dependencias Futuras

Este documento registra partes do dashboard que ainda nao devem ser implementadas integralmente porque dependem de modulos que aparecem depois da etapa atual do backlog.

## Pode ser implementado agora

- Saldo total atual: depende de contas financeiras.
- Receitas do mes: depende de transacoes `INCOME` realizadas no mes.
- Despesas do mes: depende de transacoes `EXPENSE` realizadas no mes.
- Saldo mensal: `monthlyIncome - monthlyExpense`.
- Saldo previsto basico: pode considerar saldo total atual e transacoes pendentes quando a regra for definida.
- Grafico de despesas por categoria: depende de transacoes e categorias.
- Grafico receitas x despesas: depende de transacoes.

## Deve ficar pendente

- Contas proximas e atrasadas: depende do modulo **8. Contas a pagar**.
- Recorrencias: depende do modulo **9. Recorrencias**.
- Orcamentos e progresso de orcamento: depende do modulo **10. Orcamentos**.
- Metas e progresso de metas: depende do modulo **11. Metas**.
- Faturas abertas, fatura atual, total de faturas em aberto e limite utilizado: dependem dos modulos **12. Cartoes de credito e faturas** e **13. Compras parceladas**.

## Quando implementar

Implemente cada indicador pendente imediatamente apos concluir e validar os testes da etapa correspondente do backlog:

- Apos a etapa 8: adicionar cards/listas de contas a pagar proximas e atrasadas.
- Apos a etapa 10: adicionar progresso de orcamentos ao dashboard.
- Apos a etapa 11: adicionar progresso de metas ao dashboard.
- Apos as etapas 12 e 13: adicionar faturas, limite utilizado e parcelas futuras.

Enquanto esses modulos nao existirem, os campos de resumo relacionados devem permanecer no contrato com valor zero ou sem dados, e os testes devem validar apenas que o dashboard nao mistura dados de outros usuarios e nao persiste regras de negocio proprias.
