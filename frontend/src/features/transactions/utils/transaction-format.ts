import type { TransactionStatus, TransactionType } from '../types/transactions.types';

export const transactionTypes = ['EXPENSE', 'INCOME'] as const;
export const transactionStatuses = ['PENDING', 'PAID', 'RECEIVED', 'CANCELED'] as const;

export const transactionTypeLabels: Record<TransactionType, string> = {
  EXPENSE: 'Despesa',
  INCOME: 'Receita',
};

export const transactionStatusLabels: Record<TransactionStatus, string> = {
  CANCELED: 'Cancelada',
  PAID: 'Paga',
  PENDING: 'Pendente',
  RECEIVED: 'Recebida',
};

export const transactionStatusTone: Record<TransactionStatus, string> = {
  CANCELED: 'border-slate-300/25 bg-slate-400/10 text-slate-200',
  PAID: 'border-rose-300/25 bg-rose-400/10 text-rose-200',
  PENDING: 'border-amber-300/25 bg-amber-400/10 text-amber-100',
  RECEIVED: 'border-emerald-300/25 bg-emerald-400/10 text-emerald-200',
};

export function realizedStatusFor(type: TransactionType) {
  return type === 'EXPENSE' ? 'PAID' : 'RECEIVED';
}

export function formatCurrency(value: number | string) {
  return new Intl.NumberFormat('pt-BR', {
    currency: 'BRL',
    style: 'currency',
  }).format(Number(value));
}

export function formatDate(date: string) {
  return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'medium' }).format(new Date(`${date}T00:00:00`));
}

export function signedAmount(type: TransactionType, amount: number | string) {
  const value = Number(amount);
  return type === 'EXPENSE' ? value * -1 : value;
}
