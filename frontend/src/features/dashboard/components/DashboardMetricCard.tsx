import type { LucideIcon } from 'lucide-react';

import { formatCurrency } from '../utils/dashboard-format';

type DashboardMetricCardProps = {
  description: string;
  icon: LucideIcon;
  label: string;
  tone?: 'default' | 'income' | 'expense' | 'warning';
  value: number;
};

const toneClasses = {
  default: 'text-fuchsia-200 bg-fuchsia-400/15',
  expense: 'text-rose-200 bg-rose-400/15',
  income: 'text-emerald-200 bg-emerald-400/15',
  warning: 'text-amber-100 bg-amber-400/15',
};

export function DashboardMetricCard({ description, icon: Icon, label, tone = 'default', value }: DashboardMetricCardProps) {
  return (
    <article className="app-panel p-5">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-sm font-medium text-[#c8a9d8]">{label}</p>
          <strong className="mt-3 block font-serif text-3xl text-[#f7ecff]">{formatCurrency(value)}</strong>
        </div>
        <span className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-lg border border-white/10 ${toneClasses[tone]}`}>
          <Icon size={21} />
        </span>
      </div>
      <p className="mt-4 text-sm leading-6 text-[#c8a9d8]">{description}</p>
    </article>
  );
}
