import { ArrowDownCircle, ArrowUpCircle, ChevronLeft, ChevronRight, Landmark, TrendingUp, WalletCards } from 'lucide-react';
import { useMemo, useState } from 'react';

import { Button } from '../../../shared/ui/Button';
import { DashboardMetricCard } from '../components/DashboardMetricCard';
import { ExpenseCategoryChart } from '../components/ExpenseCategoryChart';
import { FutureDependencyPanel } from '../components/FutureDependencyPanel';
import { IncomeExpenseChart } from '../components/IncomeExpenseChart';
import { PeriodSelector } from '../components/PeriodSelector';
import { useDashboardMonthly, useDashboardSummary, useExpensesByCategory, useIncomeVsExpense } from '../hooks/useDashboard';
import type { DashboardPeriod } from '../types/dashboard.types';
import { currentDashboardPeriod, formatCurrency, formatPeriod } from '../utils/dashboard-format';

function PaginationControls({
  isFetching,
  onPageChange,
  page,
  totalPages,
}: {
  isFetching: boolean;
  onPageChange: (page: number) => void;
  page: number;
  totalPages: number;
}) {
  if (totalPages <= 1) {
    return null;
  }

  return (
    <div className="mt-5 flex items-center justify-end gap-2">
      <Button disabled={page === 0 || isFetching} onClick={() => onPageChange(Math.max(0, page - 1))} type="button" variant="secondary">
        <ChevronLeft size={16} /> Anterior
      </Button>
      <span className="text-sm font-medium text-fuchsia-50">
        Pagina {page + 1} de {totalPages}
      </span>
      <Button disabled={page + 1 >= totalPages || isFetching} onClick={() => onPageChange(page + 1)} type="button" variant="secondary">
        Proxima <ChevronRight size={16} />
      </Button>
    </div>
  );
}

export function DashboardPage() {
  const [period, setPeriod] = useState<DashboardPeriod>(() => currentDashboardPeriod());
  const [expensePage, setExpensePage] = useState(0);
  const summaryQuery = useDashboardSummary(period);
  const monthlyQuery = useDashboardMonthly(period);
  const expensesQuery = useExpensesByCategory({ page: expensePage, period });
  const incomeExpenseFirstPageQuery = useIncomeVsExpense({ page: 0, year: period.year });
  const incomeExpenseSecondPageQuery = useIncomeVsExpense({ page: 1, year: period.year });
  const summary = summaryQuery.data;
  const monthly = monthlyQuery.data;
  const expenses = expensesQuery.data?.content ?? [];
  const incomeExpenseRows = useMemo(
    () => [...(incomeExpenseFirstPageQuery.data?.content ?? []), ...(incomeExpenseSecondPageQuery.data?.content ?? [])].sort((first, second) => first.month - second.month),
    [incomeExpenseFirstPageQuery.data?.content, incomeExpenseSecondPageQuery.data?.content],
  );

  const metrics = useMemo(
    () => [
      {
        description: 'Soma das contas financeiras ativas.',
        icon: WalletCards,
        label: 'Saldo atual',
        tone: 'default' as const,
        value: summary?.totalBalance ?? 0,
      },
      {
        description: 'Receitas recebidas no periodo.',
        icon: ArrowUpCircle,
        label: 'Receitas do mes',
        tone: 'income' as const,
        value: summary?.monthlyIncome ?? 0,
      },
      {
        description: 'Despesas pagas no periodo.',
        icon: ArrowDownCircle,
        label: 'Despesas do mes',
        tone: 'expense' as const,
        value: summary?.monthlyExpense ?? 0,
      },
      {
        description: 'Saldo atual somado aos lancamentos pendentes do mes.',
        icon: TrendingUp,
        label: 'Saldo previsto',
        tone: 'warning' as const,
        value: summary?.expectedBalance ?? 0,
      },
    ],
    [summary],
  );

  function updatePeriod(nextPeriod: DashboardPeriod) {
    setPeriod(nextPeriod);
    setExpensePage(0);
  }

  return (
    <section className="space-y-6">
      <div className="app-page-hero">
        <div className="admin-flow-line" aria-hidden="true" />
        <div className="relative flex flex-wrap items-start justify-between gap-4">
          <div>
            <p className="app-eyebrow">Dashboard</p>
            <h1 className="app-hero-title mt-4">Painel financeiro</h1>
            <p className="app-hero-copy mt-4 max-w-2xl">
              Leitura rapida de saldo, receitas, despesas e tendencia de lancamentos realizados.
            </p>
          </div>

          <PeriodSelector onChange={updatePeriod} period={period} />
        </div>
      </div>

      {summaryQuery.isError || monthlyQuery.isError ? (
        <div className="rounded-lg border border-rose-300/25 bg-rose-400/10 p-6 text-sm font-medium text-rose-200">Nao foi possivel carregar o resumo do dashboard.</div>
      ) : null}

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {metrics.map((metric) => (
          <DashboardMetricCard key={metric.label} {...metric} />
        ))}
      </div>

      <div className="grid gap-4 lg:grid-cols-[1.25fr_0.75fr]">
        <section className="app-panel p-5">
          <div className="flex items-center justify-between gap-4">
            <div>
              <p className="app-eyebrow">{formatPeriod(period.month, period.year)}</p>
              <h2 className="mt-2 font-serif text-2xl font-semibold text-[#f7ecff]">Fechamento mensal</h2>
            </div>
            <span className="flex h-11 w-11 items-center justify-center rounded-lg border border-white/10 bg-fuchsia-400/15 text-fuchsia-200">
              <Landmark size={21} />
            </span>
          </div>

          {monthlyQuery.isLoading ? <p className="mt-8 text-sm font-medium text-[#c8a9d8]">Carregando fechamento...</p> : null}

          <div className="mt-6 grid gap-3 sm:grid-cols-2">
            <div className="rounded-lg border border-white/10 bg-white/10 p-4">
              <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]">Saldo mensal</p>
              <strong className="mt-2 block font-serif text-3xl text-[#f7ecff]">{formatCurrency(monthly?.balance ?? 0)}</strong>
            </div>
            <div className="rounded-lg border border-white/10 bg-white/10 p-4">
              <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]">Previsto</p>
              <strong className="mt-2 block font-serif text-3xl text-[#f7ecff]">{formatCurrency(monthly?.expectedBalance ?? 0)}</strong>
            </div>
            <div className="rounded-lg border border-emerald-300/20 bg-emerald-400/10 p-4">
              <p className="text-xs font-bold uppercase tracking-[0.16em] text-emerald-100">Entradas</p>
              <strong className="mt-2 block font-serif text-2xl text-emerald-100">{formatCurrency(monthly?.income ?? 0)}</strong>
            </div>
            <div className="rounded-lg border border-rose-300/20 bg-rose-400/10 p-4">
              <p className="text-xs font-bold uppercase tracking-[0.16em] text-rose-100">Saidas</p>
              <strong className="mt-2 block font-serif text-2xl text-rose-100">{formatCurrency(monthly?.expense ?? 0)}</strong>
            </div>
          </div>
        </section>

        <FutureDependencyPanel />
      </div>

      <div className="grid gap-4 xl:grid-cols-2">
        <div>
          {expensesQuery.isError ? (
            <div className="rounded-lg border border-rose-300/25 bg-rose-400/10 p-6 text-sm font-medium text-rose-200">Nao foi possivel carregar despesas por categoria.</div>
          ) : null}
          <ExpenseCategoryChart expenses={expenses} isLoading={expensesQuery.isLoading} />
          <PaginationControls
            isFetching={expensesQuery.isFetching}
            onPageChange={setExpensePage}
            page={expensePage}
            totalPages={expensesQuery.data?.totalPages ?? 0}
          />
        </div>

        <div>
          {incomeExpenseFirstPageQuery.isError || incomeExpenseSecondPageQuery.isError ? (
            <div className="rounded-lg border border-rose-300/25 bg-rose-400/10 p-6 text-sm font-medium text-rose-200">Nao foi possivel carregar receitas x despesas.</div>
          ) : null}
          <IncomeExpenseChart isLoading={incomeExpenseFirstPageQuery.isLoading || incomeExpenseSecondPageQuery.isLoading} rows={incomeExpenseRows} />
        </div>
      </div>
    </section>
  );
}
