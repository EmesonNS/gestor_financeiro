import type { ReportFilters, ReportRow, ReportType } from '../types/reports.types';
import { accountTypeLabels } from '../../accounts/utils/account-format';
import { installmentLabel, installmentStatusLabels, installmentStatusTone } from '../../installments/utils/installment-format';
import { transactionStatusLabels, transactionStatusTone, transactionTypeLabels } from '../../transactions/utils/transaction-format';

export const reportTypes: Array<{ description: string; label: string; value: ReportType }> = [
  { description: 'Lancamentos por periodo, tipo, categoria e conta.', label: 'Transacoes', value: 'transactions' },
  { description: 'Totais de despesas agrupados por categoria.', label: 'Despesas por categoria', value: 'expenses-by-category' },
  { description: 'Receitas, despesas e saldo por mes do ano.', label: 'Evolucao mensal', value: 'monthly-evolution' },
  { description: 'Saldos das contas em uma data de referencia.', label: 'Saldo por conta', value: 'accounts-balance' },
  { description: 'Limite planejado contra gasto realizado.', label: 'Orcado x realizado', value: 'budget-vs-actual' },
  { description: 'Gastos no cartao agrupados por categoria.', label: 'Gastos no cartao', value: 'credit-card-expenses' },
  { description: 'Parcelas futuras por cartao e competencia.', label: 'Parcelas futuras', value: 'future-installments' },
];

export function formatCurrency(value: number | string) {
  return new Intl.NumberFormat('pt-BR', {
    currency: 'BRL',
    style: 'currency',
  }).format(Number(value));
}

export function formatPercent(value: number | string) {
  return `${Number(value).toFixed(0)}%`;
}

export function formatDate(date?: string | null) {
  if (!date) {
    return '-';
  }

  return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'medium' }).format(new Date(`${date}T00:00:00`));
}

export function formatMonthYear(month: number, year?: number) {
  return new Intl.DateTimeFormat('pt-BR', {
    month: 'short',
    year: year ? 'numeric' : undefined,
  }).format(new Date(year ?? 2026, month - 1, 1));
}

export function currentReportOption(reportType: ReportType) {
  return reportTypes.find((report) => report.value === reportType) ?? reportTypes[0];
}

export function defaultReportFilters(): ReportFilters {
  const current = new Date();
  return {
    fromMonth: current.getMonth() + 1,
    fromYear: current.getFullYear(),
    month: current.getMonth() + 1,
    page: 0,
    toMonth: undefined,
    toYear: undefined,
    year: current.getFullYear(),
  };
}

export function reportSummaryValue(reportType: ReportType, rows: ReportRow[]) {
  switch (reportType) {
    case 'transactions':
      return rows.reduce((total, row) => total + ('amount' in row ? Number(row.amount) : 0), 0);
    case 'expenses-by-category':
    case 'credit-card-expenses':
      return rows.reduce((total, row) => total + ('totalAmount' in row ? Number(row.totalAmount) : 0), 0);
    case 'monthly-evolution':
      return rows.reduce((total, row) => total + ('balance' in row ? Number(row.balance) : 0), 0);
    case 'accounts-balance':
      return rows.reduce((total, row) => total + ('balance' in row ? Number(row.balance) : 0), 0);
    case 'budget-vs-actual':
      return rows.reduce((total, row) => total + ('actualAmount' in row ? Number(row.actualAmount) : 0), 0);
    case 'future-installments':
      return rows.reduce((total, row) => total + ('amount' in row ? Number(row.amount) : 0), 0);
  }
}

export function rowCells(reportType: ReportType, row: ReportRow) {
  switch (reportType) {
    case 'transactions':
      if (!('transactionId' in row)) return [];
      return [
        row.description,
        transactionTypeLabels[row.type],
        row.categoryName,
        row.accountName,
        formatCurrency(row.amount),
        formatDate(row.transactionDate),
        { label: transactionStatusLabels[row.status], tone: transactionStatusTone[row.status] },
      ];
    case 'expenses-by-category':
      if (!('percentage' in row)) return [];
      return [row.categoryName, formatCurrency(row.totalAmount), formatPercent(row.percentage)];
    case 'monthly-evolution':
      if (!('income' in row)) return [];
      return [formatMonthYear(row.month), formatCurrency(row.income), formatCurrency(row.expense), formatCurrency(row.balance)];
    case 'accounts-balance':
      if (!('accountType' in row)) return [];
      return [row.accountName, accountTypeLabels[row.accountType], formatCurrency(row.balance)];
    case 'budget-vs-actual':
      if (!('plannedAmount' in row)) return [];
      return [row.categoryName, formatCurrency(row.plannedAmount), formatCurrency(row.actualAmount), formatCurrency(row.remainingAmount), formatPercent(row.percentageUsed), row.exceeded ? 'Excedido' : 'Dentro'];
    case 'credit-card-expenses':
      if (!('cardName' in row) || !('totalAmount' in row)) return [];
      return [row.cardName, row.categoryName, formatCurrency(row.totalAmount)];
    case 'future-installments':
      if (!('installmentId' in row)) return [];
      return [
        row.cardName,
        row.description,
        installmentLabel(row.installmentNumber, row.totalInstallments),
        formatCurrency(row.amount),
        formatMonthYear(row.competenceMonth, row.competenceYear),
        { label: installmentStatusLabels[row.status], tone: installmentStatusTone[row.status] },
      ];
  }
}

export function reportHeaders(reportType: ReportType) {
  switch (reportType) {
    case 'transactions':
      return ['Descricao', 'Tipo', 'Categoria', 'Conta', 'Valor', 'Data', 'Status'];
    case 'expenses-by-category':
      return ['Categoria', 'Total', 'Participacao'];
    case 'monthly-evolution':
      return ['Mes', 'Receitas', 'Despesas', 'Saldo'];
    case 'accounts-balance':
      return ['Conta', 'Tipo', 'Saldo'];
    case 'budget-vs-actual':
      return ['Categoria', 'Planejado', 'Realizado', 'Restante', 'Uso', 'Estado'];
    case 'credit-card-expenses':
      return ['Cartao', 'Categoria', 'Total'];
    case 'future-installments':
      return ['Cartao', 'Compra', 'Parcela', 'Valor', 'Competencia', 'Status'];
  }
}
