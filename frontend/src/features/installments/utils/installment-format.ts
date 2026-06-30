import type { InstallmentStatus, PurchaseStatus } from '../types/installments.types';

export const purchaseStatuses = ['ACTIVE', 'CANCELED'] as const;
export const installmentStatuses = ['OPEN', 'PAID', 'CANCELED'] as const;

export const purchaseStatusLabels: Record<PurchaseStatus, string> = {
  ACTIVE: 'Ativa',
  CANCELED: 'Cancelada',
};

export const installmentStatusLabels: Record<InstallmentStatus, string> = {
  CANCELED: 'Cancelada',
  OPEN: 'Aberta',
  PAID: 'Paga',
};

export const purchaseStatusTone: Record<PurchaseStatus, string> = {
  ACTIVE: 'border-fuchsia-300/25 bg-fuchsia-400/10 text-fuchsia-100',
  CANCELED: 'border-slate-300/25 bg-slate-400/10 text-slate-200',
};

export const installmentStatusTone: Record<InstallmentStatus, string> = {
  CANCELED: 'border-slate-300/25 bg-slate-400/10 text-slate-200',
  OPEN: 'border-fuchsia-300/25 bg-fuchsia-400/10 text-fuchsia-100',
  PAID: 'border-emerald-300/25 bg-emerald-400/10 text-emerald-200',
};

export function formatCurrency(value: number | string) {
  return new Intl.NumberFormat('pt-BR', {
    currency: 'BRL',
    style: 'currency',
  }).format(Number(value));
}

export function formatDate(date?: string | null) {
  if (!date) {
    return 'Sem data';
  }

  return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'medium' }).format(new Date(`${date}T00:00:00`));
}

export function formatMonthYear(month: number, year: number) {
  return new Intl.DateTimeFormat('pt-BR', {
    month: 'short',
    year: 'numeric',
  }).format(new Date(year, month - 1, 1));
}

export function installmentLabel(number: number, total: number) {
  return `${number}/${total}`;
}

export function hasPaidInstallment(statuses: Array<{ status: InstallmentStatus }>) {
  return statuses.some((installment) => installment.status === 'PAID');
}
