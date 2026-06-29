import { PiggyBank } from 'lucide-react';
import { Link } from 'react-router';

import type { Category } from '../../categories/types/categories.types';
import type { Budget } from '../types/budgets.types';
import { budgetTone, clampUsage, formatCurrency, periodRangeLabel } from '../utils/budget-format';

type DashboardBudgetPanelProps = {
  budgets: Budget[];
  categories: Category[];
  isLoading: boolean;
};

function categoryName(categories: Category[], categoryId: string) {
  return categories.find((category) => category.id === categoryId)?.name ?? `${categoryId.slice(0, 8)}...`;
}

export function DashboardBudgetPanel({ budgets, categories, isLoading }: DashboardBudgetPanelProps) {
  return (
    <section className="app-panel p-5">
      <div className="flex items-center justify-between gap-4">
        <div>
          <p className="app-eyebrow">Orcamentos</p>
          <h2 className="mt-2 font-serif text-2xl font-semibold text-[#f7ecff]">Progresso do mes</h2>
        </div>
        <span className="flex h-11 w-11 items-center justify-center rounded-lg border border-white/10 bg-fuchsia-400/15 text-fuchsia-200">
          <PiggyBank size={21} />
        </span>
      </div>

      {isLoading ? <p className="mt-6 text-sm text-[#c8a9d8]">Carregando orcamentos...</p> : null}

      {!isLoading && !budgets.length ? (
        <p className="mt-6 rounded-lg border border-dashed border-white/20 bg-white/10 p-4 text-sm text-[#c8a9d8]">Nenhum orcamento para este periodo.</p>
      ) : null}

      <div className="mt-6 space-y-3">
        {budgets.slice(0, 4).map((budget) => {
          const tone = budgetTone(budget.usagePercentage, budget.exceeded);
          return (
            <div className="rounded-lg border border-white/10 bg-white/10 p-3" key={budget.id}>
              <div className="flex items-center justify-between gap-3">
                <span className="truncate text-sm font-semibold text-[#f7ecff]">{categoryName(categories, budget.categoryId)}</span>
                <strong className={`text-sm ${tone.text}`}>{formatCurrency(budget.remainingAmount)}</strong>
              </div>
              <p className="mt-1 text-xs text-[#c8a9d8]">{periodRangeLabel(budget.startMonth, budget.startYear, budget.endMonth, budget.endYear)}</p>
              <div className="mt-3 h-2 overflow-hidden rounded-full bg-[#24112f]">
                <div className={`h-full rounded-full bg-gradient-to-r ${tone.bar}`} style={{ width: `${clampUsage(budget.usagePercentage)}%` }} />
              </div>
            </div>
          );
        })}
      </div>

      <Link className="mt-4 inline-flex text-sm font-semibold text-fuchsia-100 hover:text-white" to="/budgets">
        Ver orcamentos
      </Link>
    </section>
  );
}
