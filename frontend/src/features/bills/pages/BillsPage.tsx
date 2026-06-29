import { AlertTriangle, CalendarClock, ChevronLeft, ChevronRight, FileClock, Plus } from 'lucide-react';
import { useMemo, useState } from 'react';
import { Link } from 'react-router';

import { Button } from '../../../shared/ui/Button';
import { apiErrorMessage } from '../../../shared/lib/api-error';
import { useAccounts } from '../../accounts/hooks/useAccounts';
import { useCategories } from '../../categories/hooks/useCategories';
import { BillCard } from '../components/BillCard';
import { BillDeleteDialog } from '../components/BillDeleteDialog';
import { BillPayDialog } from '../components/BillPayDialog';
import { useBills, useDeleteBill, usePayBill } from '../hooks/useBills';
import type { Bill, BillStatus, PayBillPayload } from '../types/bills.types';
import { formatCurrency } from '../utils/bill-format';

type BillListFilter = 'ALL' | 'OVERDUE' | BillStatus;

function statusForFilter(filter: BillListFilter) {
  if (filter === 'ALL' || filter === 'OVERDUE') {
    return undefined;
  }

  return filter;
}

export function BillsPage() {
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState<BillListFilter>('PENDING');
  const [accountFilter, setAccountFilter] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('');
  const [startDueDate, setStartDueDate] = useState('');
  const [endDueDate, setEndDueDate] = useState('');
  const [billToPay, setBillToPay] = useState<Bill | null>(null);
  const [billToDelete, setBillToDelete] = useState<Bill | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const filters = useMemo(
    () => ({
      accountId: accountFilter || undefined,
      categoryId: categoryFilter || undefined,
      endDueDate: endDueDate || undefined,
      overdue: statusFilter === 'OVERDUE' ? true : undefined,
      page,
      startDueDate: startDueDate || undefined,
      status: statusForFilter(statusFilter),
    }),
    [accountFilter, categoryFilter, endDueDate, page, startDueDate, statusFilter],
  );
  const billsQuery = useBills(filters);
  const accountsQuery = useAccounts({ archived: false, page: 0 });
  const categoriesQuery = useCategories({ page: 0, type: 'EXPENSE' });
  const payMutation = usePayBill();
  const deleteMutation = useDeleteBill();
  const bills = billsQuery.data?.content ?? [];
  const accounts = accountsQuery.data?.content ?? [];
  const categories = categoriesQuery.data?.content ?? [];
  const totalPages = billsQuery.data?.totalPages ?? 0;
  const pendingTotal = bills.filter((bill) => bill.status === 'PENDING').reduce((total, bill) => total + Number(bill.amount), 0);
  const overdueCount = bills.filter((bill) => bill.overdue && bill.status === 'PENDING').length;
  const isActionLoading = payMutation.isPending || deleteMutation.isPending;

  function resetPage() {
    setPage(0);
  }

  function updateStatusFilter(nextFilter: BillListFilter) {
    setStatusFilter(nextFilter);
    resetPage();
  }

  async function confirmPay(payload: PayBillPayload) {
    if (!billToPay) {
      return;
    }

    setActionError(null);

    try {
      await payMutation.mutateAsync({ billId: billToPay.id, payload });
      setBillToPay(null);
    } catch (error) {
      setActionError(apiErrorMessage(error, 'Nao foi possivel pagar esta conta.'));
    }
  }

  async function confirmDelete() {
    if (!billToDelete) {
      return;
    }

    setActionError(null);

    try {
      await deleteMutation.mutateAsync(billToDelete.id);
      setBillToDelete(null);
    } catch (error) {
      setActionError(apiErrorMessage(error, 'Nao foi possivel excluir esta conta.'));
    }
  }

  return (
    <section className="space-y-6">
      <div className="app-page-hero">
        <div className="admin-flow-line" aria-hidden="true" />
        <div className="relative flex flex-wrap items-start justify-between gap-4">
          <div>
            <p className="app-eyebrow">Contas a pagar</p>
            <h1 className="app-hero-title mt-4">Agenda de vencimentos</h1>
            <p className="app-hero-copy mt-4 max-w-2xl">Controle boletos, despesas recorrentes manuais e pagamentos que viram transacoes realizadas.</p>
          </div>

          <div className="app-panel-muted grid min-w-56 gap-3 p-4">
            <span className="inline-flex items-center gap-2 text-sm font-medium text-fuchsia-50/85">
              <CalendarClock size={17} /> Pendentes nesta pagina
            </span>
            <strong className="font-serif text-4xl text-[#f7ecff]">{formatCurrency(pendingTotal)}</strong>
            <span className="text-sm text-[#c8a9d8]">
              Atrasadas: <strong className="text-rose-100">{overdueCount}</strong>
            </span>
          </div>
        </div>
      </div>

      <div className="grid gap-3 rounded-lg border border-white/10 bg-white/10 p-3 backdrop-blur lg:grid-cols-[1fr_auto]">
        <div className="flex flex-wrap gap-2">
          {[
            ['PENDING', 'Pendentes'],
            ['OVERDUE', 'Atrasadas'],
            ['PAID', 'Pagas'],
            ['CANCELED', 'Canceladas'],
            ['ALL', 'Todas'],
          ].map(([value, label]) => (
            <button
              className={`rounded-lg px-3 py-2 text-sm font-semibold ${statusFilter === value ? 'bg-fuchsia-500 text-white shadow-lg shadow-fuchsia-950/20' : 'text-fuchsia-50 hover:bg-white/10'}`}
              key={value}
              onClick={() => updateStatusFilter(value as BillListFilter)}
              type="button"
            >
              {label}
            </button>
          ))}
        </div>

        <Link className="inline-flex min-h-11 items-center justify-center gap-2 rounded-lg bg-fuchsia-600 px-4 py-2 text-sm font-semibold text-white shadow-lg shadow-fuchsia-950/30 hover:bg-fuchsia-500" to="/bills/new">
          <Plus size={17} /> Nova conta
        </Link>

        <div className="grid gap-3 sm:grid-cols-2 lg:col-span-2 lg:grid-cols-4">
          <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="billAccountFilter">
            Conta
            <select
              className="mt-2 min-h-11 w-full rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20"
              id="billAccountFilter"
              onChange={(event) => {
                setAccountFilter(event.target.value);
                resetPage();
              }}
              value={accountFilter}
            >
              <option value="">Todas</option>
              {accounts.map((account) => (
                <option key={account.id} value={account.id}>
                  {account.name}
                </option>
              ))}
            </select>
          </label>

          <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="billCategoryFilter">
            Categoria
            <select
              className="mt-2 min-h-11 w-full rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20"
              id="billCategoryFilter"
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

          <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="billStartDueDate">
            Inicio
            <input
              className="mt-2 min-h-11 w-full rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20"
              id="billStartDueDate"
              onChange={(event) => {
                setStartDueDate(event.target.value);
                resetPage();
              }}
              type="date"
              value={startDueDate}
            />
          </label>

          <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="billEndDueDate">
            Fim
            <input
              className="mt-2 min-h-11 w-full rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20"
              id="billEndDueDate"
              onChange={(event) => {
                setEndDueDate(event.target.value);
                resetPage();
              }}
              type="date"
              value={endDueDate}
            />
          </label>
        </div>
      </div>

      {actionError ? <p className="rounded-lg border border-rose-300/25 bg-rose-400/10 px-4 py-3 text-sm font-medium text-rose-200">{actionError}</p> : null}

      {billsQuery.isLoading ? <div className="app-panel-muted p-8 text-center text-sm font-medium text-fuchsia-50">Carregando contas...</div> : null}

      {billsQuery.isError ? (
        <div className="rounded-lg border border-rose-300/25 bg-rose-400/10 p-6 text-sm font-medium text-rose-200">Nao foi possivel carregar suas contas a pagar.</div>
      ) : null}

      {!billsQuery.isLoading && !bills.length ? (
        <div className="rounded-lg border border-dashed border-white/20 bg-white/10 p-10 text-center backdrop-blur">
          <FileClock className="mx-auto text-fuchsia-300" size={38} />
          <h2 className="mt-4 font-serif text-2xl font-semibold text-[#f7ecff]">Nenhuma conta encontrada</h2>
          <p className="mt-2 text-sm text-[#c8a9d8]">Cadastre contas a pagar para acompanhar vencimentos e gerar despesas ao pagar.</p>
          <Link className="mt-5 inline-flex min-h-11 items-center justify-center gap-2 rounded-lg bg-fuchsia-600 px-4 py-2 text-sm font-semibold text-white shadow-lg shadow-fuchsia-950/30 hover:bg-fuchsia-500" to="/bills/new">
            <Plus size={17} /> Criar conta
          </Link>
        </div>
      ) : null}

      {overdueCount > 0 ? (
        <p className="flex items-center gap-2 rounded-lg border border-rose-300/25 bg-rose-400/10 px-4 py-3 text-sm font-semibold text-rose-100">
          <AlertTriangle size={17} /> Existem contas atrasadas nesta pagina.
        </p>
      ) : null}

      <div className="grid gap-4">
        {bills.map((bill) => (
          <BillCard accounts={accounts} bill={bill} categories={categories} key={bill.id} onDelete={setBillToDelete} onPay={setBillToPay} />
        ))}
      </div>

      {totalPages > 1 ? (
        <div className="flex items-center justify-end gap-2">
          <Button disabled={page === 0 || billsQuery.isFetching} onClick={() => setPage((current) => Math.max(0, current - 1))} type="button" variant="secondary">
            <ChevronLeft size={16} /> Anterior
          </Button>
          <span className="text-sm font-medium text-fuchsia-50">
            Pagina {page + 1} de {totalPages}
          </span>
          <Button disabled={page + 1 >= totalPages || billsQuery.isFetching} onClick={() => setPage((current) => current + 1)} type="button" variant="secondary">
            Proxima <ChevronRight size={16} />
          </Button>
        </div>
      ) : null}

      <BillPayDialog accounts={accounts} bill={billToPay} isLoading={isActionLoading} onClose={() => setBillToPay(null)} onConfirm={confirmPay} />
      <BillDeleteDialog bill={billToDelete} isLoading={isActionLoading} onClose={() => setBillToDelete(null)} onConfirm={confirmDelete} />
    </section>
  );
}
