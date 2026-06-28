import { Tags } from 'lucide-react';

import type { ExpenseByCategory } from '../types/dashboard.types';
import { clampPercent, formatCompactCurrency, formatCurrency } from '../utils/dashboard-format';

type ExpenseCategoryChartProps = {
  expenses: ExpenseByCategory[];
  isLoading: boolean;
};

export function ExpenseCategoryChart({ expenses, isLoading }: ExpenseCategoryChartProps) {
  const maxAmount = Math.max(...expenses.map((expense) => Number(expense.amount)), 0);

  return (
    <section className="app-panel p-5">
      <div className="flex items-center justify-between gap-4">
        <div>
          <p className="app-eyebrow">Categorias</p>
          <h2 className="mt-2 font-serif text-2xl font-semibold text-[#f7ecff]">Despesas do mes</h2>
        </div>
        <span className="flex h-11 w-11 items-center justify-center rounded-lg border border-white/10 bg-rose-400/15 text-rose-200">
          <Tags size={21} />
        </span>
      </div>

      {isLoading ? <p className="mt-8 text-sm font-medium text-[#c8a9d8]">Carregando categorias...</p> : null}

      {!isLoading && !expenses.length ? (
        <div className="mt-8 rounded-lg border border-dashed border-white/20 bg-white/10 p-6 text-sm text-[#c8a9d8]">Nenhuma despesa paga encontrada neste periodo.</div>
      ) : null}

      <div className="mt-6 space-y-4">
        {expenses.map((expense) => {
          const amount = Number(expense.amount);
          const width = maxAmount > 0 ? clampPercent((amount / maxAmount) * 100) : 0;

          return (
            <div key={expense.categoryId}>
              <div className="mb-2 flex items-center justify-between gap-3 text-sm">
                <span className="font-semibold text-[#f7ecff]">{expense.categoryName}</span>
                <span className="text-[#c8a9d8]">{formatCurrency(amount)}</span>
              </div>
              <div className="h-10 overflow-hidden rounded-lg border border-white/10 bg-[#24112f]">
                <div
                  className="flex h-full items-center justify-end rounded-r-lg bg-gradient-to-r from-rose-500 via-fuchsia-500 to-[#c800ff] px-3 text-xs font-bold text-white shadow-lg shadow-fuchsia-950/30"
                  style={{ width: `${width}%` }}
                >
                  {formatCompactCurrency(amount)}
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </section>
  );
}
