import { CalendarDays } from 'lucide-react';

import type { DashboardPeriod } from '../types/dashboard.types';
import { monthOptions } from '../utils/dashboard-format';

type PeriodSelectorProps = {
  onChange: (period: DashboardPeriod) => void;
  period: DashboardPeriod;
};

export function PeriodSelector({ onChange, period }: PeriodSelectorProps) {
  const years = Array.from({ length: 5 }, (_, index) => new Date().getFullYear() - 2 + index);

  return (
    <div className="app-panel-muted flex flex-wrap items-end gap-3 p-4">
      <span className="flex h-11 w-11 items-center justify-center rounded-lg border border-white/10 bg-fuchsia-400/15 text-fuchsia-200">
        <CalendarDays size={20} />
      </span>
      <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="dashboardMonth">
        Mes
        <select
          className="mt-2 min-h-11 rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20"
          id="dashboardMonth"
          onChange={(event) => onChange({ ...period, month: Number(event.target.value) })}
          value={period.month}
        >
          {monthOptions.map((month) => (
            <option key={month.value} value={month.value}>
              {month.label}
            </option>
          ))}
        </select>
      </label>
      <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="dashboardYear">
        Ano
        <select
          className="mt-2 min-h-11 rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20"
          id="dashboardYear"
          onChange={(event) => onChange({ ...period, year: Number(event.target.value) })}
          value={period.year}
        >
          {years.map((year) => (
            <option key={year} value={year}>
              {year}
            </option>
          ))}
        </select>
      </label>
    </div>
  );
}
