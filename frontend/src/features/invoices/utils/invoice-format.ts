import type { InvoiceStatus } from '../types/invoices.types';

export const invoiceStatuses = ['OPEN', 'CLOSED', 'OVERDUE', 'PAID'] as const;

export const invoiceStatusLabels: Record<InvoiceStatus, string> = {
  CLOSED: 'Fechada',
  OPEN: 'Aberta',
  OVERDUE: 'Atrasada',
  PAID: 'Paga',
};

export const invoiceStatusTone: Record<InvoiceStatus, string> = {
  CLOSED: 'border-amber-300/25 bg-amber-400/10 text-amber-100',
  OPEN: 'border-fuchsia-300/25 bg-fuchsia-400/10 text-fuchsia-100',
  OVERDUE: 'border-rose-300/25 bg-rose-400/10 text-rose-100',
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
    month: 'long',
    year: 'numeric',
  }).format(new Date(year, month - 1, 1));
}

export function dueDateCopy(date: string, status: InvoiceStatus) {
  if (status === 'PAID') {
    return 'Pagamento registrado';
  }

  const current = new Date();
  const today = new Date(current.getFullYear(), current.getMonth(), current.getDate()).getTime();
  const dueDate = new Date(`${date}T00:00:00`).getTime();
  const days = Math.round((dueDate - today) / 86_400_000);

  if (days < 0) {
    return `${Math.abs(days)} dia${Math.abs(days) === 1 ? '' : 's'} em atraso`;
  }

  if (days === 0) {
    return 'Vence hoje';
  }

  return `${days} dia${days === 1 ? '' : 's'} ate o vencimento`;
}

export function canPayInvoice(status: InvoiceStatus) {
  return status !== 'PAID';
}
