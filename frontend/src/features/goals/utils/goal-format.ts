import type { GoalStatus } from '../types/goals.types';

export const goalStatuses = ['ACTIVE', 'COMPLETED', 'CANCELED'] as const;

export const goalStatusLabels: Record<GoalStatus, string> = {
  ACTIVE: 'Ativa',
  CANCELED: 'Cancelada',
  COMPLETED: 'Concluida',
};

export const goalStatusTone: Record<GoalStatus, string> = {
  ACTIVE: 'border-fuchsia-300/25 bg-fuchsia-400/10 text-fuchsia-100',
  CANCELED: 'border-slate-300/25 bg-slate-400/10 text-slate-200',
  COMPLETED: 'border-emerald-300/25 bg-emerald-400/10 text-emerald-200',
};

export function formatCurrency(value: number | string) {
  return new Intl.NumberFormat('pt-BR', {
    currency: 'BRL',
    style: 'currency',
  }).format(Number(value));
}

export function formatPercent(value: number | string) {
  return `${Number(value).toFixed(0)}%`;
}

export function clampProgress(value: number | string) {
  if (!Number.isFinite(Number(value))) {
    return 0;
  }

  return Math.max(0, Math.min(100, Number(value)));
}

export function formatDate(date?: string | null) {
  if (!date) {
    return 'Sem prazo';
  }

  return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'medium' }).format(new Date(`${date}T00:00:00`));
}

export function deadlineCopy(date?: string | null) {
  if (!date) {
    return 'Sem prazo definido';
  }

  const current = new Date();
  const today = new Date(current.getFullYear(), current.getMonth(), current.getDate()).getTime();
  const deadline = new Date(`${date}T00:00:00`).getTime();
  const days = Math.round((deadline - today) / 86_400_000);

  if (days < 0) {
    return `${Math.abs(days)} dia${Math.abs(days) === 1 ? '' : 's'} apos o prazo`;
  }

  if (days === 0) {
    return 'Prazo hoje';
  }

  return `${days} dia${days === 1 ? '' : 's'} restantes`;
}
