import { AxiosError } from 'axios';
import { ChevronLeft, ChevronRight, PiggyBank, Plus } from 'lucide-react';
import { useMemo, useState } from 'react';
import { Link } from 'react-router';

import { apiErrorMessage } from '../../../shared/lib/api-error';
import { Button } from '../../../shared/ui/Button';
import type { ApiErrorResponse } from '../../auth/types/auth.types';
import { useCategories } from '../../categories/hooks/useCategories';
import { BudgetDeleteDialog } from '../components/BudgetDeleteDialog';
import { BudgetProgressCard } from '../components/BudgetProgressCard';
import { useBudgets, useDeleteBudget } from '../hooks/useBudgets';
import type { Budget } from '../types/budgets.types';
import { formatCurrency, monthOptions } from '../utils/budget-format';

export function BudgetsPage() {
  const [page, setPage] = useState(0);
  const [currentPeriod] = useState(() => {
    const currentDate = new Date();
    return {
      month: currentDate.getMonth() + 1,
      year: currentDate.getFullYear(),
    };
  });
  const [month, setMonth] = useState(currentPeriod.month);
  const [year, setYear] = useState(currentPeriod.year);
  const [categoryFilter, setCategoryFilter] = useState('');
  const [budgetToDelete, setBudgetToDelete] = useState<Budget | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const filters = useMemo(
    () => ({
      categoryId: categoryFilter || undefined,
      month,
      page,
      year,
    }),
    [categoryFilter, month, page, year],
  );
  const budgetsQuery = useBudgets(filters);
  const categoriesQuery = useCategories({ page: 0, type: 'EXPENSE' });
  const deleteMutation = useDeleteBudget();
  const budgets = budgetsQuery.data?.content ?? [];
  const categories = categoriesQuery.data?.content ?? [];
  const totalPages = budgetsQuery.data?.totalPages ?? 0;
  const totalLimit = budgets.reduce((total, budget) => total + Number(budget.limitAmount), 0);
  const totalSpent = budgets.reduce((total, budget) => total + Number(budget.spentAmount), 0);
  const exceededCount = budgets.filter((budget) => budget.exceeded).length;

  function resetPage() {
    setPage(0);
  }

  async function confirmDelete() {
    if (!budgetToDelete) {
      return;
    }

    setActionError(null);

    try {
      await deleteMutation.mutateAsync(budgetToDelete.id);
      setBudgetToDelete(null);
    } catch (error) {
      const fallback = error instanceof AxiosError ? (error.response?.data as ApiErrorResponse | undefined)?.message : undefined;
      setActionError(apiErrorMessage(error, fallback ?? 'Nao foi possivel excluir este orcamento.'));
    }
  }

  return (
    <section className="space-y-6">
      <div className="app-page-hero">
        <div className="admin-flow-line" aria-hidden="true" />
        <div className="relative flex flex-wrap items-start justify-between gap-4">
          <div>
            <p className="app-eyebrow">Orcamentos</p>
            <h1 className="app-hero-title mt-4">Limites do mes</h1>
            <p className="app-hero-copy mt-4 max-w-2xl">Defina tetos por categoria, com vigencia mensal, por intervalo ou sem fim, e acompanhe o gasto realizado.</p>
          </div>

          <div className="app-panel-muted grid min-w-56 gap-3 p-4">
            <span className="inline-flex items-center gap-2 text-sm font-medium text-fuchsia-50/85">
              <PiggyBank size={17} /> Gasto nesta pagina
            </span>
            <strong className="font-serif text-4xl text-[#f7ecff]">{formatCurrency(totalSpent)}</strong>
            <span className="text-sm text-[#c8a9d8]">
              Limite: <strong className="text-[#f7ecff]">{formatCurrency(totalLimit)}</strong>
            </span>
          </div>
        </div>
      </div>

      <div className="grid gap-3 rounded-lg border border-white/10 bg-white/10 p-3 backdrop-blur lg:grid-cols-[1fr_auto]">
        <div className="grid gap-3 sm:grid-cols-3">
          <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="budgetMonth">
            Mes
            <select
              className="mt-2 min-h-11 w-full rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20"
              id="budgetMonth"
              onChange={(event) => {
                setMonth(Number(event.target.value));
                resetPage();
              }}
              value={month}
            >
              {monthOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </label>

          <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="budgetYear">
            Ano
            <input
              className="mt-2 min-h-11 w-full rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20"
              id="budgetYear"
              max="2100"
              min="2000"
              onChange={(event) => {
                setYear(Number(event.target.value));
                resetPage();
              }}
              type="number"
              value={year}
            />
          </label>

          <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="budgetCategory">
            Categoria
            <select
              className="mt-2 min-h-11 w-full rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20"
              id="budgetCategory"
              onChange={(event) => {
                setCategoryFilter(event.target.value);
                resetPage();
              }}
              value={categoryFilter}
            >
              <option value="">Todas</option>
              {categories.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
          </label>
        </div>

        <Link className="inline-flex min-h-11 items-center justify-center gap-2 rounded-lg bg-fuchsia-600 px-4 py-2 text-sm font-semibold text-white shadow-lg shadow-fuchsia-950/30 hover:bg-fuchsia-500" to="/budgets/new">
          <Plus size={17} /> Novo orcamento
        </Link>
      </div>

      {actionError ? <p className="rounded-lg border border-rose-300/25 bg-rose-400/10 px-4 py-3 text-sm font-medium text-rose-200">{actionError}</p> : null}
      {exceededCount > 0 ? <p className="rounded-lg border border-rose-300/25 bg-rose-400/10 px-4 py-3 text-sm font-semibold text-rose-100">{exceededCount} orcamento(s) excedido(s) nesta pagina.</p> : null}

      {budgetsQuery.isLoading ? <div className="app-panel-muted p-8 text-center text-sm font-medium text-fuchsia-50">Carregando orcamentos...</div> : null}
      {budgetsQuery.isError ? <div className="rounded-lg border border-rose-300/25 bg-rose-400/10 p-6 text-sm font-medium text-rose-200">Nao foi possivel carregar seus orcamentos.</div> : null}

      {!budgetsQuery.isLoading && !budgets.length ? (
        <div className="rounded-lg border border-dashed border-white/20 bg-white/10 p-10 text-center backdrop-blur">
          <PiggyBank className="mx-auto text-fuchsia-300" size={38} />
          <h2 className="mt-4 font-serif text-2xl font-semibold text-[#f7ecff]">Nenhum orcamento encontrado</h2>
          <p className="mt-2 text-sm text-[#c8a9d8]">Crie um limite mensal para acompanhar gastos por categoria.</p>
          <Link className="mt-5 inline-flex min-h-11 items-center justify-center gap-2 rounded-lg bg-fuchsia-600 px-4 py-2 text-sm font-semibold text-white shadow-lg shadow-fuchsia-950/30 hover:bg-fuchsia-500" to="/budgets/new">
            <Plus size={17} /> Criar orcamento
          </Link>
        </div>
      ) : null}

      <div className="grid gap-4 lg:grid-cols-2">
        {budgets.map((budget) => (
          <BudgetProgressCard budget={budget} categories={categories} key={budget.id} onDelete={setBudgetToDelete} />
        ))}
      </div>

      {totalPages > 1 ? (
        <div className="flex items-center justify-end gap-2">
          <Button disabled={page === 0 || budgetsQuery.isFetching} onClick={() => setPage((current) => Math.max(0, current - 1))} type="button" variant="secondary">
            <ChevronLeft size={16} /> Anterior
          </Button>
          <span className="text-sm font-medium text-fuchsia-50">
            Pagina {page + 1} de {totalPages}
          </span>
          <Button disabled={page + 1 >= totalPages || budgetsQuery.isFetching} onClick={() => setPage((current) => current + 1)} type="button" variant="secondary">
            Proxima <ChevronRight size={16} />
          </Button>
        </div>
      ) : null}

      <BudgetDeleteDialog budget={budgetToDelete} categories={categories} isLoading={deleteMutation.isPending} onClose={() => setBudgetToDelete(null)} onConfirm={confirmDelete} />
    </section>
  );
}
