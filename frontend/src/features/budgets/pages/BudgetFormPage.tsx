import { ArrowLeft } from 'lucide-react';
import { useMemo, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router';

import { apiErrorMessage } from '../../../shared/lib/api-error';
import { useCategories, useCategory } from '../../categories/hooks/useCategories';
import type { Category } from '../../categories/types/categories.types';
import { BudgetForm } from '../components/BudgetForm';
import { useBudget, useCreateBudget, useUpdateBudget } from '../hooks/useBudgets';
import type { BudgetFormData } from '../schemas/budget.schemas';

function mergeById<T extends { id: string }>(items: T[], item?: T | null) {
  if (!item || items.some((current) => current.id === item.id)) {
    return items;
  }

  return [item, ...items];
}

export function BudgetFormPage() {
  const { id } = useParams();
  const isEdit = Boolean(id);
  const navigate = useNavigate();
  const budgetQuery = useBudget(id);
  const budget = budgetQuery.data;
  const categoriesQuery = useCategories({ page: 0, type: 'EXPENSE' });
  const currentCategoryQuery = useCategory(budget?.categoryId);
  const createMutation = useCreateBudget();
  const updateMutation = useUpdateBudget(id ?? '');
  const [submitError, setSubmitError] = useState<string | null>(null);
  const categories = useMemo<Category[]>(
    () => mergeById(categoriesQuery.data?.content ?? [], currentCategoryQuery.data),
    [categoriesQuery.data?.content, currentCategoryQuery.data],
  );

  async function submit(data: BudgetFormData) {
    setSubmitError(null);
    const payload = {
      categoryId: data.categoryId,
      endMonth: data.durationMode === 'FOREVER' ? null : data.durationMode === 'SINGLE' ? data.startMonth : data.endMonth ?? null,
      endYear: data.durationMode === 'FOREVER' ? null : data.durationMode === 'SINGLE' ? data.startYear : data.endYear ?? null,
      limitAmount: data.limitAmount,
      startMonth: data.startMonth,
      startYear: data.startYear,
    };

    try {
      if (isEdit && id) {
        await updateMutation.mutateAsync(payload);
      } else {
        await createMutation.mutateAsync(payload);
      }
      navigate('/budgets', { replace: true });
    } catch (error) {
      setSubmitError(apiErrorMessage(error, 'Nao foi possivel salvar o orcamento.'));
    }
  }

  if (isEdit && budgetQuery.isLoading) {
    return <div className="app-panel-muted p-8 text-center text-sm font-medium text-fuchsia-50">Carregando orcamento...</div>;
  }

  if (isEdit && (budgetQuery.isError || !budget)) {
    return (
      <section className="rounded-lg border border-rose-300/25 bg-rose-400/10 p-6">
        <h1 className="font-serif text-2xl font-semibold text-rose-100">Orcamento nao encontrado</h1>
        <p className="mt-2 text-sm text-rose-200">Nao foi possivel carregar este orcamento.</p>
        <Link className="mt-4 inline-flex font-semibold text-rose-100" to="/budgets">
          Voltar para orcamentos
        </Link>
      </section>
    );
  }

  return (
    <section className="space-y-6">
      <Link className="inline-flex items-center gap-2 text-sm font-semibold text-fuchsia-100 hover:text-white" to="/budgets">
        <ArrowLeft size={16} /> Voltar para orcamentos
      </Link>

      <div className="app-page-hero">
        <div className="admin-flow-line" aria-hidden="true" />
        <div className="relative max-w-3xl">
          <p className="app-eyebrow">Orcamentos</p>
          <h1 className="app-hero-title mt-4">{isEdit ? 'Editar limite' : 'Novo limite'}</h1>
          <p className="app-hero-copy mt-4 max-w-xl">
            {isEdit ? 'Ajuste categoria, vigencia e limite mensal.' : 'Crie um teto mensal para uma categoria, por um mes, intervalo ou sem fim.'}
          </p>
        </div>
      </div>

      <BudgetForm
        budget={budget}
        categories={categories}
        isLoading={createMutation.isPending || updateMutation.isPending}
        mode={isEdit ? 'edit' : 'create'}
        onSubmit={submit}
        submitError={submitError}
      />
    </section>
  );
}
