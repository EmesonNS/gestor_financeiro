import { Pencil, PiggyBank, Trash2 } from 'lucide-react';
import { Link } from 'react-router';

import { Button } from '../../../shared/ui/Button';
import type { Category } from '../../categories/types/categories.types';
import type { Budget } from '../types/budgets.types';
import { budgetTone, clampUsage, formatCurrency, formatPercent, periodRangeLabel } from '../utils/budget-format';

type BudgetProgressCardProps = {
  budget: Budget;
  categories: Category[];
  onDelete: (budget: Budget) => void;
};

function categoryName(categories: Category[], categoryId: string) {
  return categories.find((category) => category.id === categoryId)?.name ?? `${categoryId.slice(0, 8)}...`;
}

export function BudgetProgressCard({ budget, categories, onDelete }: BudgetProgressCardProps) {
  const tone = budgetTone(budget.usagePercentage, budget.exceeded);
  const progress = clampUsage(budget.usagePercentage);

  return (
    <article className={`app-panel p-5 ${tone.border}`}>
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div className="flex gap-3">
          <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-lg border border-white/10 bg-fuchsia-400/15 text-fuchsia-200">
            <PiggyBank size={22} />
          </span>
          <div>
            <div className="flex flex-wrap items-center gap-2">
              <h2 className="font-serif text-xl font-semibold text-[#f7ecff]">{categoryName(categories, budget.categoryId)}</h2>
              {budget.exceeded ? <span className="rounded-full border border-rose-300/25 bg-rose-400/10 px-2 py-1 text-xs font-bold text-rose-100">Excedido</span> : null}
            </div>
            <p className="mt-1 text-sm text-[#c8a9d8]">{periodRangeLabel(budget.startMonth, budget.startYear, budget.endMonth, budget.endYear)}</p>
          </div>
        </div>

        <strong className={`font-serif text-3xl ${tone.text}`}>{formatPercent(budget.usagePercentage)}</strong>
      </div>

      <div className="mt-6 h-4 overflow-hidden rounded-full border border-white/10 bg-[#24112f]">
        <div className={`h-full rounded-full bg-gradient-to-r ${tone.bar}`} style={{ width: `${progress}%` }} />
      </div>

      <div className="mt-5 grid gap-3 sm:grid-cols-3">
        <div className="rounded-lg border border-white/10 bg-white/10 p-3">
          <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]">Limite</p>
          <p className="mt-2 text-sm font-semibold text-[#f7ecff]">{formatCurrency(budget.limitAmount)}</p>
        </div>
        <div className="rounded-lg border border-white/10 bg-white/10 p-3">
          <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]">Gasto</p>
          <p className="mt-2 text-sm font-semibold text-[#f7ecff]">{formatCurrency(budget.spentAmount)}</p>
        </div>
        <div className="rounded-lg border border-white/10 bg-white/10 p-3">
          <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]">Restante</p>
          <p className={`mt-2 text-sm font-semibold ${budget.remainingAmount < 0 ? 'text-rose-100' : 'text-[#f7ecff]'}`}>{formatCurrency(budget.remainingAmount)}</p>
        </div>
      </div>

      <div className="mt-5 flex flex-wrap gap-2 border-t border-white/10 pt-4">
        <Link className="inline-flex min-h-9 items-center justify-center gap-2 rounded-lg border border-white/15 bg-white/10 px-3 py-1.5 text-sm font-semibold text-fuchsia-50 hover:bg-white/15" to={`/budgets/${budget.id}/edit`}>
          <Pencil size={16} /> Editar
        </Link>
        <Button className="min-h-9 border-rose-300/25 px-3 py-1.5 text-rose-200 hover:bg-rose-400/10" onClick={() => onDelete(budget)} type="button" variant="secondary">
          <Trash2 size={16} /> Excluir
        </Button>
      </div>
    </article>
  );
}
