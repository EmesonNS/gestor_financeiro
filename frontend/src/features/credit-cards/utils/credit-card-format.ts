export function formatCurrency(value: number | string) {
  return new Intl.NumberFormat('pt-BR', {
    currency: 'BRL',
    style: 'currency',
  }).format(Number(value));
}

export function formatDateTime(value: string) {
  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value));
}

export function limitUsagePercent(usedLimit: number | string, limitAmount: number | string) {
  const limit = Number(limitAmount);

  if (!Number.isFinite(limit) || limit <= 0) {
    return 0;
  }

  return Math.max(0, Math.min(100, (Number(usedLimit) / limit) * 100));
}

export function formatPercent(value: number | string) {
  return `${Number(value).toFixed(0)}%`;
}

export function statementDayCopy(closingDay: number, dueDay: number) {
  return `Fecha dia ${closingDay}, vence dia ${dueDay}`;
}
