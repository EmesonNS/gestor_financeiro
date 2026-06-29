import { ArrowLeft } from 'lucide-react';
import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router';

import { apiErrorMessage } from '../../../shared/lib/api-error';
import { GoalForm } from '../components/GoalForm';
import { useCreateGoal, useGoal, useUpdateGoal } from '../hooks/useGoals';
import type { GoalFormData } from '../schemas/goal.schemas';

export function GoalFormPage() {
  const { id } = useParams();
  const isEdit = Boolean(id);
  const navigate = useNavigate();
  const goalQuery = useGoal(id);
  const goal = goalQuery.data;
  const createMutation = useCreateGoal();
  const updateMutation = useUpdateGoal(id ?? '');
  const [submitError, setSubmitError] = useState<string | null>(null);

  async function submit(data: GoalFormData) {
    setSubmitError(null);
    const payload = {
      currentAmount: data.currentAmount,
      deadline: data.deadline || null,
      description: data.description || null,
      name: data.name,
      targetAmount: data.targetAmount,
    };

    try {
      if (isEdit && id) {
        await updateMutation.mutateAsync(payload);
      } else {
        await createMutation.mutateAsync(payload);
      }
      navigate('/goals', { replace: true });
    } catch (error) {
      setSubmitError(apiErrorMessage(error, 'Nao foi possivel salvar a meta.'));
    }
  }

  if (isEdit && goalQuery.isLoading) {
    return <div className="app-panel-muted p-8 text-center text-sm font-medium text-fuchsia-50">Carregando meta...</div>;
  }

  if (isEdit && (goalQuery.isError || !goal)) {
    return (
      <section className="rounded-lg border border-rose-300/25 bg-rose-400/10 p-6">
        <h1 className="font-serif text-2xl font-semibold text-rose-100">Meta nao encontrada</h1>
        <p className="mt-2 text-sm text-rose-200">Nao foi possivel carregar esta meta.</p>
        <Link className="mt-4 inline-flex font-semibold text-rose-100" to="/goals">
          Voltar para metas
        </Link>
      </section>
    );
  }

  return (
    <section className="space-y-6">
      <Link className="inline-flex items-center gap-2 text-sm font-semibold text-fuchsia-100 hover:text-white" to="/goals">
        <ArrowLeft size={16} /> Voltar para metas
      </Link>

      <div className="app-page-hero">
        <div className="admin-flow-line" aria-hidden="true" />
        <div className="relative max-w-3xl">
          <p className="app-eyebrow">Metas financeiras</p>
          <h1 className="app-hero-title mt-4">{isEdit ? 'Editar objetivo' : 'Nova meta'}</h1>
          <p className="app-hero-copy mt-4 max-w-xl">
            {isEdit ? 'Ajuste valores, prazo e descricao da meta.' : 'Crie uma meta com valor alvo, progresso atual e prazo opcional.'}
          </p>
        </div>
      </div>

      <GoalForm
        goal={goal}
        isLoading={createMutation.isPending || updateMutation.isPending}
        mode={isEdit ? 'edit' : 'create'}
        onSubmit={submit}
        submitError={submitError}
      />
    </section>
  );
}
