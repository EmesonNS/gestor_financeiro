import { AxiosError } from 'axios';
import { ChevronLeft, ChevronRight, Plus, Tags } from 'lucide-react';
import { useMemo, useState } from 'react';
import { Link } from 'react-router';

import { CategoryCard } from '../components/CategoryCard';
import { CategoryDeleteDialog } from '../components/CategoryDeleteDialog';
import { useCategories, useCategoryTypeCounts, useCustomCategoryCount, useDeleteCategory } from '../hooks/useCategories';
import type { ApiErrorResponse } from '../../auth/types/auth.types';
import type { Category, CategoryType } from '../types/categories.types';
import { categoryTypeLabels } from '../utils/category-format';

type CategoryFilter = 'ALL' | CategoryType;

export function CategoriesPage() {
  const [typeFilter, setTypeFilter] = useState<CategoryFilter>('ALL');
  const [page, setPage] = useState(0);
  const [categoryToDelete, setCategoryToDelete] = useState<Category | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const filters = useMemo(() => ({ page, type: typeFilter === 'ALL' ? undefined : typeFilter }), [page, typeFilter]);
  const categoriesQuery = useCategories(filters);
  const customCountQuery = useCustomCategoryCount();
  const typeCountsQuery = useCategoryTypeCounts();
  const deleteMutation = useDeleteCategory();
  const categories = categoriesQuery.data?.content ?? [];
  const incomeCount = typeCountsQuery.data?.incomeCount ?? 0;
  const expenseCount = typeCountsQuery.data?.expenseCount ?? 0;
  const customCount = customCountQuery.data?.count ?? 0;
  const totalPages = categoriesQuery.data?.totalPages ?? 0;

  function updateTypeFilter(nextType: CategoryFilter) {
    setTypeFilter(nextType);
    setPage(0);
  }

  async function confirmDelete() {
    if (!categoryToDelete) {
      return;
    }

    setActionError(null);

    try {
      await deleteMutation.mutateAsync(categoryToDelete.id);
      setCategoryToDelete(null);
    } catch (error) {
      const message = error instanceof AxiosError ? (error.response?.data as ApiErrorResponse | undefined)?.message : null;
      setActionError(message ?? 'Nao foi possivel excluir a categoria.');
    }
  }

  return (
    <section className="space-y-6">
      <div className="app-page-hero">
        <div className="admin-flow-line" aria-hidden="true" />
        <div className="relative flex flex-wrap items-start justify-between gap-4">
          <div>
            <p className="app-eyebrow">Categorias</p>
            <h1 className="app-hero-title mt-4">Mapa dos lançamentos</h1>
            <p className="app-hero-copy mt-4 max-w-2xl">Organize receitas e despesas com cores, nomes e filtros prontos para os próximos lançamentos.</p>
          </div>

          <div className="app-panel-muted grid min-w-56 p-4">
            <span className="inline-flex items-center gap-2 text-sm font-medium text-fuchsia-50/85">
              <Tags size={17} /> Personalizadas
            </span>
            <strong className="mt-2 font-serif text-4xl text-[#f7ecff]">{customCount}</strong>
          </div>
        </div>
      </div>

      <div className="flex flex-wrap items-center justify-between gap-3 rounded-lg border border-white/10 bg-white/10 p-3 backdrop-blur">
        <div className="flex flex-wrap gap-2">
          {[
            ['ALL', 'Todas'],
            ['EXPENSE', 'Despesas'],
            ['INCOME', 'Receitas'],
          ].map(([value, label]) => (
            <button
              className={`rounded-lg px-3 py-2 text-sm font-semibold ${typeFilter === value ? 'bg-fuchsia-500 text-white shadow-lg shadow-fuchsia-950/20' : 'text-fuchsia-50 hover:bg-white/10'}`}
              key={value}
              onClick={() => updateTypeFilter(value as CategoryFilter)}
              type="button"
            >
              {label}
            </button>
          ))}
        </div>

        <div className="flex flex-wrap items-center gap-3">
          <span className="rounded-lg border border-white/10 bg-white/10 px-3 py-2 text-sm text-[#c8a9d8]">
            {categoryTypeLabels.EXPENSE}: <strong className="text-[#f7ecff]">{expenseCount}</strong>
          </span>
          <span className="rounded-lg border border-white/10 bg-white/10 px-3 py-2 text-sm text-[#c8a9d8]">
            {categoryTypeLabels.INCOME}: <strong className="text-[#f7ecff]">{incomeCount}</strong>
          </span>
          <Link className="inline-flex min-h-11 items-center justify-center gap-2 rounded-lg bg-fuchsia-600 px-4 py-2 text-sm font-semibold text-white shadow-lg shadow-fuchsia-950/30 hover:bg-fuchsia-500" to="/categories/new">
            <Plus size={17} /> Nova categoria
          </Link>
        </div>
      </div>

      {actionError ? <p className="rounded-lg border border-rose-300/25 bg-rose-400/10 px-4 py-3 text-sm font-medium text-rose-200">{actionError}</p> : null}

      {categoriesQuery.isLoading ? <div className="app-panel-muted p-8 text-center text-sm font-medium text-fuchsia-50">Carregando categorias...</div> : null}

      {categoriesQuery.isError ? (
        <div className="rounded-lg border border-rose-300/25 bg-rose-400/10 p-6 text-sm font-medium text-rose-200">Nao foi possivel carregar suas categorias.</div>
      ) : null}

      {!categoriesQuery.isLoading && !categories.length ? (
        <div className="rounded-lg border border-dashed border-white/20 bg-white/10 p-10 text-center backdrop-blur">
          <Tags className="mx-auto text-fuchsia-300" size={38} />
          <h2 className="mt-4 font-serif text-2xl font-semibold text-[#f7ecff]">Nenhuma categoria encontrada</h2>
          <p className="mt-2 text-sm text-[#c8a9d8]">Crie uma categoria para classificar seus lançamentos.</p>
          <Link className="mt-5 inline-flex min-h-11 items-center justify-center gap-2 rounded-lg bg-fuchsia-600 px-4 py-2 text-sm font-semibold text-white shadow-lg shadow-fuchsia-950/30 hover:bg-fuchsia-500" to="/categories/new">
            <Plus size={17} /> Criar categoria
          </Link>
        </div>
      ) : null}

      <div className="grid gap-4 lg:grid-cols-2">
        {categories.map((category) => (
          <CategoryCard category={category} key={category.id} onDelete={setCategoryToDelete} />
        ))}
      </div>

      {totalPages > 1 ? (
        <div className="flex items-center justify-end gap-2">
          <button
            className="inline-flex min-h-11 items-center justify-center gap-2 rounded-lg border border-white/15 bg-white/10 px-4 py-2 text-sm font-semibold text-fuchsia-50 transition hover:bg-white/15 disabled:cursor-not-allowed disabled:opacity-60"
            disabled={page === 0 || categoriesQuery.isFetching}
            onClick={() => setPage((current) => Math.max(0, current - 1))}
            type="button"
          >
            <ChevronLeft size={16} /> Anterior
          </button>
          <span className="text-sm font-medium text-fuchsia-50">
            Pagina {page + 1} de {totalPages}
          </span>
          <button
            className="inline-flex min-h-11 items-center justify-center gap-2 rounded-lg border border-white/15 bg-white/10 px-4 py-2 text-sm font-semibold text-fuchsia-50 transition hover:bg-white/15 disabled:cursor-not-allowed disabled:opacity-60"
            disabled={page + 1 >= totalPages || categoriesQuery.isFetching}
            onClick={() => setPage((current) => current + 1)}
            type="button"
          >
            Proxima <ChevronRight size={16} />
          </button>
        </div>
      ) : null}

      <CategoryDeleteDialog category={categoryToDelete} isLoading={deleteMutation.isPending} onClose={() => setCategoryToDelete(null)} onConfirm={confirmDelete} />
    </section>
  );
}
