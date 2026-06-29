import { CalendarClock, Pencil, Target, Trash2, TrendingUp } from 'lucide-react';
import { Link } from 'react-router';

import { Button } from '../../../shared/ui/Button';
import type { Goal } from '../types/goals.types';
import { clampProgress, deadlineCopy, formatCurrency, formatDate, formatPercent, goalStatusLabels, goalStatusTone } from '../utils/goal-format';

type GoalProgressCardProps = {
  goal: Goal;
  onDelete: (goal: Goal) => void;
  onProgress: (goal: Goal) => void;
};

export function GoalProgressCard({ goal, onDelete, onProgress }: GoalProgressCardProps) {
  const progress = clampProgress(goal.completionPercentage);
  const remaining = Math.max(0, Number(goal.targetAmount) - Number(goal.currentAmount));

  return (
    <article className="app-panel p-5">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div className="flex gap-3">
          <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-lg border border-white/10 bg-fuchsia-400/15 text-fuchsia-200">
            <Target size={22} />
          </span>
          <div>
            <div className="flex flex-wrap items-center gap-2">
              <h2 className="font-serif text-xl font-semibold text-[#f7ecff]">{goal.name}</h2>
              <span className={`rounded-full border px-2 py-1 text-xs font-bold ${goalStatusTone[goal.status]}`}>{goalStatusLabels[goal.status]}</span>
            </div>
            <p className="mt-1 flex items-center gap-2 text-sm text-[#c8a9d8]">
              <CalendarClock size={16} /> {deadlineCopy(goal.deadline)}
            </p>
          </div>
        </div>

        <strong className="font-serif text-3xl text-fuchsia-100">{formatPercent(goal.completionPercentage)}</strong>
      </div>

      <div className="mt-6 h-4 overflow-hidden rounded-full border border-white/10 bg-[#24112f]">
        <div className="h-full rounded-full bg-gradient-to-r from-emerald-400 via-fuchsia-500 to-[#c800ff]" style={{ width: `${progress}%` }} />
      </div>

      <div className="mt-5 grid gap-3 sm:grid-cols-3">
        <div className="rounded-lg border border-white/10 bg-white/10 p-3">
          <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]">Atual</p>
          <p className="mt-2 text-sm font-semibold text-[#f7ecff]">{formatCurrency(goal.currentAmount)}</p>
        </div>
        <div className="rounded-lg border border-white/10 bg-white/10 p-3">
          <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]">Alvo</p>
          <p className="mt-2 text-sm font-semibold text-[#f7ecff]">{formatCurrency(goal.targetAmount)}</p>
        </div>
        <div className="rounded-lg border border-white/10 bg-white/10 p-3">
          <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]">Falta</p>
          <p className="mt-2 text-sm font-semibold text-[#f7ecff]">{formatCurrency(remaining)}</p>
        </div>
      </div>

      {goal.description ? <p className="mt-4 rounded-lg border border-white/10 bg-white/10 px-3 py-2 text-sm text-[#c8a9d8]">{goal.description}</p> : null}

      <p className="mt-4 text-sm text-[#c8a9d8]">Prazo: {formatDate(goal.deadline)}</p>

      <div className="mt-5 flex flex-wrap gap-2 border-t border-white/10 pt-4">
        <Button className="min-h-9 px-3 py-1.5" onClick={() => onProgress(goal)} type="button">
          <TrendingUp size={16} /> Atualizar valor
        </Button>
        <Link className="inline-flex min-h-9 items-center justify-center gap-2 rounded-lg border border-white/15 bg-white/10 px-3 py-1.5 text-sm font-semibold text-fuchsia-50 hover:bg-white/15" to={`/goals/${goal.id}/edit`}>
          <Pencil size={16} /> Editar
        </Link>
        <Button className="min-h-9 border-rose-300/25 px-3 py-1.5 text-rose-200 hover:bg-rose-400/10" onClick={() => onDelete(goal)} type="button" variant="secondary">
          <Trash2 size={16} /> Excluir
        </Button>
      </div>
    </article>
  );
}
