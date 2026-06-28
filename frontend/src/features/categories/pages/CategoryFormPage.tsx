import { AxiosError } from 'axios';
import { ArrowLeft } from 'lucide-react';
import { useState } from 'react';
import { Link, Navigate, useNavigate, useParams } from 'react-router';

import type { ApiErrorResponse } from '../../auth/types/auth.types';
import { CategoryForm } from '../components/CategoryForm';
import { useCategory, useCreateCategory, useUpdateCategory } from '../hooks/useCategories';
import type { CategoryFormData } from '../schemas/category.schemas';

export function CategoryFormPage() {
  const { id } = useParams();
  const isEdit = Boolean(id);
  const navigate = useNavigate();
  const categoryQuery = useCategory(id);
  const createMutation = useCreateCategory();
  const updateMutation = useUpdateCategory(id ?? '');
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [submitMessage, setSubmitMessage] = useState<string | null>(null);
  const category = categoryQuery.data;

  async function submit(data: CategoryFormData) {
    setSubmitError(null);
    setSubmitMessage(null);
    const payload = {
      color: data.color || null,
      icon: data.icon?.trim() || null,
      name: data.name,
      type: data.type,
    };

    try {
      if (isEdit && id) {
        await updateMutation.mutateAsync(payload);
        navigate('/categories', { replace: true });
      } else {
        await createMutation.mutateAsync(payload);
        navigate('/categories', { replace: true });
      }
    } catch (error) {
      const message = error instanceof AxiosError ? (error.response?.data as ApiErrorResponse | undefined)?.message : null;
      setSubmitError(message ?? 'Nao foi possivel salvar a categoria.');
    }
  }

  if (isEdit && categoryQuery.isLoading) {
    return <div className="app-panel-muted p-8 text-center text-sm font-medium text-fuchsia-50">Carregando categoria...</div>;
  }

  if (isEdit && (categoryQuery.isError || !category)) {
    return (
      <section className="rounded-lg border border-rose-300/25 bg-rose-400/10 p-6">
        <h1 className="font-serif text-2xl font-semibold text-rose-100">Categoria nao encontrada</h1>
        <p className="mt-2 text-sm text-rose-200">Nao foi possivel carregar esta categoria.</p>
        <Link className="mt-4 inline-flex font-semibold text-rose-100" to="/categories">
          Voltar para categorias
        </Link>
      </section>
    );
  }

  if (category?.defaultCategory) {
    return <Navigate replace to="/categories" />;
  }

  return (
    <section className="space-y-6">
      <Link className="inline-flex items-center gap-2 text-sm font-semibold text-fuchsia-100 hover:text-white" to="/categories">
        <ArrowLeft size={16} /> Voltar para categorias
      </Link>

      <div className="app-page-hero">
        <div className="admin-flow-line" aria-hidden="true" />
        <div className="relative max-w-3xl">
          <p className="app-eyebrow">Categorias</p>
          <h1 className="app-hero-title mt-4">{isEdit ? 'Editar categoria' : 'Nova categoria'}</h1>
          <p className="app-hero-copy mt-4 max-w-xl">
            {isEdit ? 'Ajuste nome, tipo, cor e identificador visual da categoria.' : 'Crie uma classificacao para receitas ou despesas futuras.'}
          </p>
        </div>
      </div>

      <CategoryForm
        category={category}
        isLoading={createMutation.isPending || updateMutation.isPending}
        mode={isEdit ? 'edit' : 'create'}
        onSubmit={submit}
        submitError={submitError}
        submitMessage={submitMessage}
      />
    </section>
  );
}
