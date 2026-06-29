import { AxiosError } from 'axios';
import { ChevronLeft, ChevronRight, Plus, Target } from 'lucide-react';
import { useMemo, useState } from 'react';
import { Link } from 'react-router';

import { apiErrorMessage } from '../../../shared/lib/api-error';
import { Button } from '../../../shared/ui/Button';
import type { ApiErrorResponse } from '../../auth/types/auth.types';
import { GoalDeleteDialog } from '../components/GoalDeleteDialog';
import { GoalProgressCard } from '../components/GoalProgressCard';
import { GoalProgressDialog } from '../components/GoalProgressDialog';
import { useDeleteGoal, useGoals, useUpdateGoalProgress } from '../hooks/useGoals';
import type { Goal, GoalStatus } from '../types/goals.types';
import { formatCurrency, goalStatusLabels, goalStatuses } from '../utils/goal-format';

type GoalStatusFilter = 'ALL' | GoalStatus;

const statusFilterOptions: Array<{ label: string; value: GoalStatusFilter }> = [
  { label: 'Todas', value: 'ALL' },
  ...goalStatuses.map((status) => ({ label: goalStatusLabels[status], value: status })),
];

export function GoalsPage() {
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState<GoalStatusFilter>('ACTIVE');
  const [goalToUpdate, setGoalToUpdate] = useState<Goal | null>(null);
  const [goalToDelete, setGoalToDelete] = useState<Goal | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const filters = useMemo(
    () => ({
      page,
      status: statusFilter === 'ALL' ? undefined : statusFilter,
    }),
    [page, statusFilter],
  );
  const goalsQuery = useGoals(filters);
  const updateProgressMutation = useUpdateGoalProgress();
  const deleteMutation = useDeleteGoal();
  const goals = goalsQuery.data?.content ?? [];
  const totalPages = goalsQuery.data?.totalPages ?? 0;
  const currentAmountTotal = goals.reduce((total, goal) => total + Number(goal.currentAmount), 0);
  const targetAmountTotal = goals.reduce((total, goal) => total + Number(goal.targetAmount), 0);
  const remainingAmountTotal = Math.max(0, targetAmountTotal - currentAmountTotal);

  function selectStatus(nextStatus: GoalStatusFilter) {
    setStatusFilter(nextStatus);
    setPage(0);
  }

  async function confirmProgress(data: { currentAmount: number }) {
    if (!goalToUpdate) {
      return;
    }

    setActionError(null);

    try {
      await updateProgressMutation.mutateAsync({
        goalId: goalToUpdate.id,
        payload: data,
      });
      setGoalToUpdate(null);
    } catch (error) {
      const fallback = error instanceof AxiosError ? (error.response?.data as ApiErrorResponse | undefined)?.message : undefined;
      setActionError(apiErrorMessage(error, fallback ?? 'Nao foi possivel atualizar o progresso da meta.'));
    }
  }

  async function confirmDelete() {
    if (!goalToDelete) {
      return;
    }

    setActionError(null);

    try {
      await deleteMutation.mutateAsync(goalToDelete.id);
      setGoalToDelete(null);
    } catch (error) {
      const fallback = error instanceof AxiosError ? (error.response?.data as ApiErrorResponse | undefined)?.message : undefined;
      setActionError(apiErrorMessage(error, fallback ?? 'Nao foi possivel excluir esta meta.'));
    }
  }

  return (
    <section className="space-y-6">
      <div className="app-page-hero">
        <div className="admin-flow-line" aria-hidden="true" />
        <div className="relative flex flex-wrap items-start justify-between gap-4">
          <div>
            <p className="app-eyebrow">Metas financeiras</p>
            <h1 className="app-hero-title mt-4">Objetivos em andamento</h1>
            <p className="app-hero-copy mt-4 max-w-2xl">Acompanhe cada reserva, atualize o valor acumulado e veja quando uma meta chega ao alvo.</p>
          </div>

          <div className="app-panel-muted grid min-w-56 gap-3 p-4">
            <span className="inline-flex items-center gap-2 text-sm font-medium text-fuchsia-50/85">
              <Target size={17} /> Falta nesta pagina
            </span>
            <strong className="font-serif text-4xl text-[#f7ecff]">{formatCurrency(remainingAmountTotal)}</strong>
            <span className="text-sm text-[#c8a9d8]">
              Guardado: <strong className="text-[#f7ecff]">{formatCurrency(currentAmountTotal)}</strong>
            </span>
          </div>
        </div>
      </div>

      <div className="grid gap-3 rounded-lg border border-white/10 bg-white/10 p-3 backdrop-blur lg:grid-cols-[1fr_auto]">
        <div className="flex flex-wrap gap-2">
          {statusFilterOptions.map((option) => (
            <button
              className={`min-h-10 rounded-lg px-3 py-2 text-sm font-semibold transition ${
                statusFilter === option.value ? 'bg-fuchsia-600 text-white shadow-lg shadow-fuchsia-950/30' : 'border border-white/10 bg-white/10 text-fuchsia-100 hover:bg-white/15'
              }`}
              key={option.value}
              onClick={() => selectStatus(option.value)}
              type="button"
            >
              {option.label}
            </button>
          ))}
        </div>

        <Link className="inline-flex min-h-11 items-center justify-center gap-2 rounded-lg bg-fuchsia-600 px-4 py-2 text-sm font-semibold text-white shadow-lg shadow-fuchsia-950/30 hover:bg-fuchsia-500" to="/goals/new">
          <Plus size={17} /> Nova meta
        </Link>
      </div>

      {actionError ? <p className="rounded-lg border border-rose-300/25 bg-rose-400/10 px-4 py-3 text-sm font-medium text-rose-200">{actionError}</p> : null}

      {goalsQuery.isLoading ? <div className="app-panel-muted p-8 text-center text-sm font-medium text-fuchsia-50">Carregando metas...</div> : null}
      {goalsQuery.isError ? <div className="rounded-lg border border-rose-300/25 bg-rose-400/10 p-6 text-sm font-medium text-rose-200">Nao foi possivel carregar suas metas.</div> : null}

      {!goalsQuery.isLoading && !goals.length ? (
        <div className="rounded-lg border border-dashed border-white/20 bg-white/10 p-10 text-center backdrop-blur">
          <Target className="mx-auto text-fuchsia-300" size={38} />
          <h2 className="mt-4 font-serif text-2xl font-semibold text-[#f7ecff]">Nenhuma meta encontrada</h2>
          <p className="mt-2 text-sm text-[#c8a9d8]">Crie um objetivo financeiro e registre o quanto ja foi guardado.</p>
          <Link className="mt-5 inline-flex min-h-11 items-center justify-center gap-2 rounded-lg bg-fuchsia-600 px-4 py-2 text-sm font-semibold text-white shadow-lg shadow-fuchsia-950/30 hover:bg-fuchsia-500" to="/goals/new">
            <Plus size={17} /> Criar meta
          </Link>
        </div>
      ) : null}

      <div className="grid gap-4 lg:grid-cols-2">
        {goals.map((goal) => (
          <GoalProgressCard goal={goal} key={goal.id} onDelete={setGoalToDelete} onProgress={setGoalToUpdate} />
        ))}
      </div>

      {totalPages > 1 ? (
        <div className="flex items-center justify-end gap-2">
          <Button disabled={page === 0 || goalsQuery.isFetching} onClick={() => setPage((current) => Math.max(0, current - 1))} type="button" variant="secondary">
            <ChevronLeft size={16} /> Anterior
          </Button>
          <span className="text-sm font-medium text-fuchsia-50">
            Pagina {page + 1} de {totalPages}
          </span>
          <Button disabled={page + 1 >= totalPages || goalsQuery.isFetching} onClick={() => setPage((current) => current + 1)} type="button" variant="secondary">
            Proxima <ChevronRight size={16} />
          </Button>
        </div>
      ) : null}

      <GoalProgressDialog goal={goalToUpdate} isLoading={updateProgressMutation.isPending} onClose={() => setGoalToUpdate(null)} onConfirm={confirmProgress} />
      <GoalDeleteDialog goal={goalToDelete} isLoading={deleteMutation.isPending} onClose={() => setGoalToDelete(null)} onConfirm={confirmDelete} />
    </section>
  );
}
