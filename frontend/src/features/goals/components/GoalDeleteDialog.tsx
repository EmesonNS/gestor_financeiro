import { Button } from '../../../shared/ui/Button';
import type { Goal } from '../types/goals.types';
import { formatCurrency } from '../utils/goal-format';

type GoalDeleteDialogProps = {
  goal: Goal | null;
  isLoading: boolean;
  onClose: () => void;
  onConfirm: () => void;
};

export function GoalDeleteDialog({ goal, isLoading, onClose, onConfirm }: GoalDeleteDialogProps) {
  if (!goal) {
    return null;
  }

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-slate-950/70 px-4 backdrop-blur-sm">
      <section className="app-panel w-full max-w-lg p-6">
        <p className="text-xs font-bold uppercase tracking-[0.18em] text-fuchsia-300">Metas financeiras</p>
        <h2 className="mt-3 font-serif text-2xl font-semibold text-[#f7ecff]">Excluir meta</h2>
        <p className="mt-3 text-sm leading-6 text-[#c8a9d8]">
          Remover a meta <strong className="text-[#f7ecff]">{goal.name}</strong> com alvo de <strong className="text-[#f7ecff]">{formatCurrency(goal.targetAmount)}</strong>.
        </p>

        <div className="mt-6 flex flex-wrap justify-end gap-3">
          <Button disabled={isLoading} onClick={onClose} type="button" variant="secondary">
            Cancelar
          </Button>
          <Button className="bg-rose-600 hover:bg-rose-500" isLoading={isLoading} onClick={onConfirm} type="button">
            Excluir meta
          </Button>
        </div>
      </section>
    </div>
  );
}
