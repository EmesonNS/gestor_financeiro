import { Target } from 'lucide-react';
import { Link } from 'react-router';

import type { Goal } from '../types/goals.types';
import { clampProgress, formatCurrency } from '../utils/goal-format';

type DashboardGoalsPanelProps = {
  goals: Goal[];
  isLoading: boolean;
};

export function DashboardGoalsPanel({ goals, isLoading }: DashboardGoalsPanelProps) {
  return (
    <section className="app-panel p-5">
      <div className="flex items-center justify-between gap-4">
        <div>
          <p className="app-eyebrow">Metas</p>
          <h2 className="mt-2 font-serif text-2xl font-semibold text-[#f7ecff]">Objetivos ativos</h2>
        </div>
        <span className="flex h-11 w-11 items-center justify-center rounded-lg border border-white/10 bg-fuchsia-400/15 text-fuchsia-200">
          <Target size={21} />
        </span>
      </div>

      {isLoading ? <p className="mt-6 text-sm text-[#c8a9d8]">Carregando metas...</p> : null}

      {!isLoading && !goals.length ? <p className="mt-6 rounded-lg border border-dashed border-white/20 bg-white/10 p-4 text-sm text-[#c8a9d8]">Nenhuma meta ativa.</p> : null}

      <div className="mt-6 space-y-3">
        {goals.slice(0, 4).map((goal) => (
          <div className="rounded-lg border border-white/10 bg-white/10 p-3" key={goal.id}>
            <div className="flex items-center justify-between gap-3">
              <span className="truncate text-sm font-semibold text-[#f7ecff]">{goal.name}</span>
              <strong className="text-sm text-fuchsia-100">{formatCurrency(goal.currentAmount)}</strong>
            </div>
            <div className="mt-3 h-2 overflow-hidden rounded-full bg-[#24112f]">
              <div className="h-full rounded-full bg-gradient-to-r from-emerald-400 via-fuchsia-500 to-[#c800ff]" style={{ width: `${clampProgress(goal.completionPercentage)}%` }} />
            </div>
          </div>
        ))}
      </div>

      <Link className="mt-4 inline-flex text-sm font-semibold text-fuchsia-100 hover:text-white" to="/goals">
        Ver metas
      </Link>
    </section>
  );
}
