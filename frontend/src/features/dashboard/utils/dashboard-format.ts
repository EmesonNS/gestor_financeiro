export const monthOptions = [
  { label: 'Janeiro', value: 1 },
  { label: 'Fevereiro', value: 2 },
  { label: 'Marco', value: 3 },
  { label: 'Abril', value: 4 },
  { label: 'Maio', value: 5 },
  { label: 'Junho', value: 6 },
  { label: 'Julho', value: 7 },
  { label: 'Agosto', value: 8 },
  { label: 'Setembro', value: 9 },
  { label: 'Outubro', value: 10 },
  { label: 'Novembro', value: 11 },
  { label: 'Dezembro', value: 12 },
];

export const monthShortLabels = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];

export function currentDashboardPeriod() {
  const current = new Date();
  return {
    month: current.getMonth() + 1,
    year: current.getFullYear(),
  };
}

export function formatCurrency(value: number | string) {
  return new Intl.NumberFormat('pt-BR', {
    currency: 'BRL',
    style: 'currency',
  }).format(Number(value));
}

export function formatCompactCurrency(value: number | string) {
  return new Intl.NumberFormat('pt-BR', {
    compactDisplay: 'short',
    maximumFractionDigits: 1,
    notation: 'compact',
    style: 'currency',
    currency: 'BRL',
  }).format(Number(value));
}

export function formatPeriod(month: number, year: number) {
  return `${monthOptions.find((option) => option.value === month)?.label ?? 'Mes'} de ${year}`;
}

export function clampPercent(value: number) {
  if (!Number.isFinite(value)) {
    return 0;
  }

  return Math.max(3, Math.min(100, value));
}
