import { BarChart3 } from 'lucide-react';

import type { IncomeExpenseMonthly } from '../types/dashboard.types';
import { clampPercent, formatCompactCurrency, monthShortLabels } from '../utils/dashboard-format';

type IncomeExpenseChartProps = {
  isLoading: boolean;
  rows: IncomeExpenseMonthly[];
};

export function IncomeExpenseChart({ isLoading, rows }: IncomeExpenseChartProps) {
  const maxValue = Math.max(...rows.flatMap((row) => [Number(row.income), Number(row.expense)]), 0);

  return (
    <section className="app-panel p-5">
      <div className="flex items-center justify-between gap-4">
        <div>
          <p className="app-eyebrow">Ano</p>
          <h2 className="mt-2 font-serif text-2xl font-semibold text-[#f7ecff]">Receitas x despesas</h2>
        </div>
        <span className="flex h-11 w-11 items-center justify-center rounded-lg border border-white/10 bg-fuchsia-400/15 text-fuchsia-200">
          <BarChart3 size={21} />
        </span>
      </div>

      {isLoading ? <p className="mt-8 text-sm font-medium text-[#c8a9d8]">Carregando serie mensal...</p> : null}

      {!isLoading && !rows.length ? (
        <div className="mt-8 rounded-lg border border-dashed border-white/20 bg-white/10 p-6 text-sm text-[#c8a9d8]">Nenhum lancamento realizado neste ano.</div>
      ) : null}

      <div className="app-scrollbar mt-6 overflow-x-auto pb-3">
        <div className="grid min-h-72 min-w-[640px] grid-cols-12 gap-2 xl:min-w-0">
          {rows.map((row) => {
            const incomeHeight = maxValue > 0 ? clampPercent((Number(row.income) / maxValue) * 100) : 0;
            const expenseHeight = maxValue > 0 ? clampPercent((Number(row.expense) / maxValue) * 100) : 0;

            return (
              <div className="grid grid-rows-[1fr_auto] gap-3" key={row.month}>
                <div className="flex h-56 items-end justify-center gap-1 rounded-lg border border-white/10 bg-white/10 px-1.5 py-3">
                  <div className="w-3 rounded-t-md bg-emerald-400/80 sm:w-4" style={{ height: `${incomeHeight}%` }} title={`Receitas ${formatCompactCurrency(row.income)}`} />
                  <div className="w-3 rounded-t-md bg-rose-400/80 sm:w-4" style={{ height: `${expenseHeight}%` }} title={`Despesas ${formatCompactCurrency(row.expense)}`} />
                </div>
                <span className="text-center text-xs font-bold uppercase tracking-[0.12em] text-[#c8a9d8]">{monthShortLabels[row.month - 1]}</span>
              </div>
            );
          })}
        </div>
      </div>

      <div className="mt-5 flex flex-wrap gap-3 text-sm text-[#c8a9d8]">
        <span className="inline-flex items-center gap-2">
          <span className="h-3 w-3 rounded-sm bg-emerald-400/80" /> Receitas
        </span>
        <span className="inline-flex items-center gap-2">
          <span className="h-3 w-3 rounded-sm bg-rose-400/80" /> Despesas
        </span>
      </div>
    </section>
  );
}
