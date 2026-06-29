import { useState } from 'react';

import { Button } from '../../../shared/ui/Button';
import { goalProgressSchema, type GoalProgressData } from '../schemas/goal.schemas';
import type { Goal } from '../types/goals.types';
import { formatCurrency } from '../utils/goal-format';

type GoalProgressDialogProps = {
  goal: Goal | null;
  isLoading: boolean;
  onClose: () => void;
  onConfirm: (data: GoalProgressData) => void;
};

export function GoalProgressDialog({ goal, isLoading, onClose, onConfirm }: GoalProgressDialogProps) {
  const [formState, setFormState] = useState({ currentAmount: '', goalId: '' });
  const [error, setError] = useState<string | null>(null);

  if (!goal) {
    return null;
  }

  const currentFormState = formState.goalId === goal.id ? formState : { currentAmount: String(goal.currentAmount), goalId: goal.id };

  function confirm() {
    const parsed = goalProgressSchema.safeParse({ currentAmount: currentFormState.currentAmount });

    if (!parsed.success) {
      setError(parsed.error.issues[0]?.message ?? 'Confira o valor atual.');
      return;
    }

    setError(null);
    onConfirm(parsed.data);
  }

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-slate-950/70 px-4 backdrop-blur-sm">
      <section className="app-panel w-full max-w-lg p-6">
        <p className="text-xs font-bold uppercase tracking-[0.18em] text-fuchsia-300">Metas financeiras</p>
        <h2 className="mt-3 font-serif text-2xl font-semibold text-[#f7ecff]">Atualizar progresso</h2>
        <p className="mt-3 text-sm leading-6 text-[#c8a9d8]">
          Meta <strong className="text-[#f7ecff]">{goal.name}</strong>, alvo <strong className="text-[#f7ecff]">{formatCurrency(goal.targetAmount)}</strong>.
        </p>

        <label className="mt-5 block text-sm font-medium text-[#dcc3e8]" htmlFor="goalCurrentAmount">
          Valor atual
          <input
            className="mt-2 min-h-11 w-full rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20"
            id="goalCurrentAmount"
            min="0"
            onChange={(event) => setFormState({ currentAmount: event.target.value, goalId: goal.id })}
            step="0.01"
            type="number"
            value={currentFormState.currentAmount}
          />
        </label>

        {error ? <p className="mt-4 rounded-lg border border-rose-300/25 bg-rose-400/10 px-3 py-2 text-sm font-medium text-rose-200">{error}</p> : null}

        <div className="mt-6 flex flex-wrap justify-end gap-3">
          <Button disabled={isLoading} onClick={onClose} type="button" variant="secondary">
            Fechar
          </Button>
          <Button isLoading={isLoading} onClick={confirm} type="button">
            Atualizar progresso
          </Button>
        </div>
      </section>
    </div>
  );
}
