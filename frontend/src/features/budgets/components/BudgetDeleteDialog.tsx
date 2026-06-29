import { Button } from '../../../shared/ui/Button';
import type { Category } from '../../categories/types/categories.types';
import type { Budget } from '../types/budgets.types';
import { formatCurrency, periodRangeLabel } from '../utils/budget-format';

type BudgetDeleteDialogProps = {
  budget: Budget | null;
  categories: Category[];
  isLoading: boolean;
  onClose: () => void;
  onConfirm: () => void;
};

function categoryName(categories: Category[], categoryId: string) {
  return categories.find((category) => category.id === categoryId)?.name ?? `${categoryId.slice(0, 8)}...`;
}

export function BudgetDeleteDialog({ budget, categories, isLoading, onClose, onConfirm }: BudgetDeleteDialogProps) {
  if (!budget) {
    return null;
  }

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-slate-950/70 px-4 backdrop-blur-sm">
      <section className="app-panel w-full max-w-lg p-6">
        <p className="text-xs font-bold uppercase tracking-[0.18em] text-fuchsia-300">Orcamentos</p>
        <h2 className="mt-3 font-serif text-2xl font-semibold text-[#f7ecff]">Excluir orcamento</h2>
        <p className="mt-3 text-sm leading-6 text-[#c8a9d8]">
          Remover limite de <strong className="text-[#f7ecff]">{formatCurrency(budget.limitAmount)}</strong> para{' '}
          <strong className="text-[#f7ecff]">{categoryName(categories, budget.categoryId)}</strong> em {periodRangeLabel(budget.startMonth, budget.startYear, budget.endMonth, budget.endYear)}.
        </p>

        <div className="mt-6 flex flex-wrap justify-end gap-3">
          <Button disabled={isLoading} onClick={onClose} type="button" variant="secondary">
            Cancelar
          </Button>
          <Button className="bg-rose-600 hover:bg-rose-500" isLoading={isLoading} onClick={onConfirm} type="button">
            Excluir orcamento
          </Button>
        </div>
      </section>
    </div>
  );
}
