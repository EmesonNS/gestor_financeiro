import type { Bill, BillStatus } from '../types/bills.types';

export const billStatuses = ['PENDING', 'PAID', 'OVERDUE', 'CANCELED'] as const;

export const billStatusLabels: Record<BillStatus, string> = {
  CANCELED: 'Cancelada',
  OVERDUE: 'Vencida',
  PAID: 'Paga',
  PENDING: 'Pendente',
};

export const billStatusTone: Record<BillStatus, string> = {
  CANCELED: 'border-slate-300/25 bg-slate-400/10 text-slate-200',
  OVERDUE: 'border-rose-300/30 bg-rose-400/15 text-rose-100',
  PAID: 'border-emerald-300/25 bg-emerald-400/10 text-emerald-200',
  PENDING: 'border-amber-300/25 bg-amber-400/10 text-amber-100',
};

export function visualBillStatus(bill: Bill): BillStatus {
  if (bill.overdue && bill.status === 'PENDING') {
    return 'OVERDUE';
  }

  return bill.status;
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

export function daysUntilDue(dueDate: string) {
  const current = new Date();
  const today = new Date(current.getFullYear(), current.getMonth(), current.getDate()).getTime();
  const due = new Date(`${dueDate}T00:00:00`).getTime();
  return Math.round((due - today) / 86_400_000);
}

export function dueCopy(bill: Bill) {
  if (bill.status === 'PAID' && bill.paidAt) {
    return `Paga em ${formatDate(bill.paidAt)}`;
  }

  if (bill.status === 'CANCELED') {
    return 'Cancelada';
  }

  const days = daysUntilDue(bill.dueDate);
  if (days < 0) {
    return `${Math.abs(days)} dia${Math.abs(days) === 1 ? '' : 's'} em atraso`;
  }

  if (days === 0) {
    return 'Vence hoje';
  }

  return `Vence em ${days} dia${days === 1 ? '' : 's'}`;
}
