import { monthOptions } from '../../dashboard/utils/dashboard-format';

export { monthOptions };

export function formatCurrency(value: number | string) {
  return new Intl.NumberFormat('pt-BR', {
    currency: 'BRL',
    style: 'currency',
  }).format(Number(value));
}

export function formatPercent(value: number | string) {
  return `${Number(value).toFixed(0)}%`;
}

export function clampUsage(value: number | string) {
  if (!Number.isFinite(Number(value))) {
    return 0;
  }

  return Math.max(0, Math.min(100, Number(value)));
}

export function budgetTone(usagePercentage: number | string, exceeded: boolean) {
  if (exceeded) {
    return {
      bar: 'from-rose-500 via-fuchsia-500 to-[#c800ff]',
      border: 'border-rose-300/25',
      text: 'text-rose-100',
    };
  }

  if (Number(usagePercentage) >= 80) {
    return {
      bar: 'from-amber-400 via-fuchsia-500 to-[#c800ff]',
      border: 'border-amber-300/25',
      text: 'text-amber-100',
    };
  }

  return {
    bar: 'from-emerald-400 via-fuchsia-500 to-[#c800ff]',
    border: 'border-emerald-300/25',
    text: 'text-emerald-100',
  };
}

export function periodLabel(month: number, year: number) {
  return `${monthOptions.find((option) => option.value === month)?.label ?? 'Mes'} de ${year}`;
}

export function periodRangeLabel(startMonth: number, startYear: number, endMonth?: number | null, endYear?: number | null) {
  const start = periodLabel(startMonth, startYear);

  if (!endMonth || !endYear) {
    return `${start} em diante`;
  }

  if (startMonth === endMonth && startYear === endYear) {
    return start;
  }

  return `${start} ate ${periodLabel(endMonth, endYear)}`;
}

export function periodValue(month: number, year: number) {
  return year * 12 + month;
}
