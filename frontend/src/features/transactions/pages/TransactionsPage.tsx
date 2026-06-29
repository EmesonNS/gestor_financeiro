import { ArrowDownUp, ChevronLeft, ChevronRight, Plus, ReceiptText } from 'lucide-react';
import { useMemo, useState } from 'react';
import { Link } from 'react-router';

import { Button } from '../../../shared/ui/Button';
import { apiErrorMessage } from '../../../shared/lib/api-error';
import { useAccounts } from '../../accounts/hooks/useAccounts';
import { useCategories } from '../../categories/hooks/useCategories';
import { TransactionActionDialog } from '../components/TransactionActionDialog';
import { TransactionCard } from '../components/TransactionCard';
import {
  useCancelTransaction,
  useDeleteTransaction,
  useMarkTransactionAsPaid,
  useTransactions,
} from '../hooks/useTransactions';
import type { MarkTransactionAsPaidPayload, Transaction, TransactionStatus, TransactionType } from '../types/transactions.types';
import {
  formatCurrency,
  signedAmount,
  transactionStatusLabels,
  transactionTypeLabels,
} from '../utils/transaction-format';

type TransactionAction = 'cancel' | 'delete' | 'mark-as-paid';
type TypeFilter = 'ALL' | TransactionType;
type StatusFilter = 'ALL' | TransactionStatus;

type DialogState = {
  action: TransactionAction;
  transaction: Transaction;
} | null;

export function TransactionsPage() {
  const [page, setPage] = useState(0);
  const [typeFilter, setTypeFilter] = useState<TypeFilter>('ALL');
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL');
  const [accountFilter, setAccountFilter] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [dialog, setDialog] = useState<DialogState>(null);
  const [actionError, setActionError] = useState<string | null>(null);

  const filters = useMemo(
    () => ({
      accountId: accountFilter || undefined,
      categoryId: categoryFilter || undefined,
      endDate: endDate || undefined,
      page,
      startDate: startDate || undefined,
      status: statusFilter === 'ALL' ? undefined : statusFilter,
      type: typeFilter === 'ALL' ? undefined : typeFilter,
    }),
    [accountFilter, categoryFilter, endDate, page, startDate, statusFilter, typeFilter],
  );
  const transactionsQuery = useTransactions(filters);
  const accountsQuery = useAccounts({ archived: false, page: 0 });
  const categoriesQuery = useCategories({ page: 0, type: typeFilter === 'ALL' ? undefined : typeFilter });
  const markAsPaidMutation = useMarkTransactionAsPaid();
  const cancelMutation = useCancelTransaction();
  const deleteMutation = useDeleteTransaction();

  const transactions = transactionsQuery.data?.content ?? [];
  const accounts = accountsQuery.data?.content ?? [];
  const categories = categoriesQuery.data?.content ?? [];
  const totalPages = transactionsQuery.data?.totalPages ?? 0;
  const pendingCount = transactions.filter((transaction) => transaction.status === 'PENDING').length;
  const pageNetAmount = transactions.reduce((total, transaction) => total + signedAmount(transaction.type, transaction.amount), 0);
  const isActionLoading = markAsPaidMutation.isPending || cancelMutation.isPending || deleteMutation.isPending;

  function resetPage() {
    setPage(0);
  }

  function updateTypeFilter(nextType: TypeFilter) {
    setTypeFilter(nextType);
    setCategoryFilter('');
    resetPage();
  }

  function updateStatusFilter(nextStatus: StatusFilter) {
    setStatusFilter(nextStatus);
    resetPage();
  }

  async function confirmAction(payload?: MarkTransactionAsPaidPayload) {
    if (!dialog) {
      return;
    }

    setActionError(null);

    try {
      if (dialog.action === 'mark-as-paid') {
        await markAsPaidMutation.mutateAsync({ payload, transactionId: dialog.transaction.id });
      }

      if (dialog.action === 'cancel') {
        await cancelMutation.mutateAsync(dialog.transaction.id);
      }

      if (dialog.action === 'delete') {
        await deleteMutation.mutateAsync(dialog.transaction.id);
      }

      setDialog(null);
    } catch (error) {
      setActionError(apiErrorMessage(error, 'Nao foi possivel concluir a acao da transacao.'));
    }
  }

  return (
    <section className="space-y-6">
      <div className="app-page-hero">
        <div className="admin-flow-line" aria-hidden="true" />
        <div className="relative flex flex-wrap items-start justify-between gap-4">
          <div>
            <p className="app-eyebrow">Transacoes</p>
            <h1 className="app-hero-title mt-4">Fluxo do dinheiro</h1>
            <p className="app-hero-copy mt-4 max-w-2xl">Registre receitas e despesas, acompanhe pendencias e confirme o impacto real no saldo das contas.</p>
          </div>

          <div className="app-panel-muted grid min-w-56 gap-3 p-4">
            <span className="inline-flex items-center gap-2 text-sm font-medium text-fuchsia-50/85">
              <ArrowDownUp size={17} /> Resultado nesta pagina
            </span>
            <strong className="font-serif text-4xl text-[#f7ecff]">{formatCurrency(pageNetAmount)}</strong>
            <span className="text-sm text-[#c8a9d8]">
              Pendentes: <strong className="text-[#f7ecff]">{pendingCount}</strong>
            </span>
          </div>
        </div>
      </div>

      <div className="grid gap-3 rounded-lg border border-white/10 bg-white/10 p-3 backdrop-blur lg:grid-cols-[1fr_auto]">
        <div className="flex flex-wrap gap-2">
          {[
            ['ALL', 'Todas'],
            ['EXPENSE', 'Despesas'],
            ['INCOME', 'Receitas'],
          ].map(([value, label]) => (
            <button
              className={`rounded-lg px-3 py-2 text-sm font-semibold ${typeFilter === value ? 'bg-fuchsia-500 text-white shadow-lg shadow-fuchsia-950/20' : 'text-fuchsia-50 hover:bg-white/10'}`}
              key={value}
              onClick={() => updateTypeFilter(value as TypeFilter)}
              type="button"
            >
              {label}
            </button>
          ))}
        </div>

        <Link className="inline-flex min-h-11 items-center justify-center gap-2 rounded-lg bg-fuchsia-600 px-4 py-2 text-sm font-semibold text-white shadow-lg shadow-fuchsia-950/30 hover:bg-fuchsia-500" to="/transactions/new">
          <Plus size={17} /> Nova transacao
        </Link>

        <div className="grid gap-3 sm:grid-cols-2 lg:col-span-2 lg:grid-cols-5">
          <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="statusFilter">
            Status
            <select
              className="mt-2 min-h-11 w-full rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20"
              id="statusFilter"
              onChange={(event) => updateStatusFilter(event.target.value as StatusFilter)}
              value={statusFilter}
            >
              <option value="ALL">Todos</option>
              {(['PENDING', 'PAID', 'RECEIVED', 'CANCELED'] as TransactionStatus[]).map((status) => (
                <option key={status} value={status}>
                  {transactionStatusLabels[status]}
                </option>
              ))}
            </select>
          </label>

          <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="accountFilter">
            Conta
            <select
              className="mt-2 min-h-11 w-full rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20"
              id="accountFilter"
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

          <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="categoryFilter">
            Categoria
            <select
              className="mt-2 min-h-11 w-full rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20"
              id="categoryFilter"
              onChange={(event) => {
                setCategoryFilter(event.target.value);
                resetPage();
              }}
              value={categoryFilter}
            >
              <option value="">Todas</option>
              {categories.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name} ({transactionTypeLabels[category.type]})
                </option>
              ))}
            </select>
          </label>

          <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="startDateFilter">
            Inicio
            <input
              className="mt-2 min-h-11 w-full rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20"
              id="startDateFilter"
              onChange={(event) => {
                setStartDate(event.target.value);
                resetPage();
              }}
              type="date"
              value={startDate}
            />
          </label>

          <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="endDateFilter">
            Fim
            <input
              className="mt-2 min-h-11 w-full rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20"
              id="endDateFilter"
              onChange={(event) => {
                setEndDate(event.target.value);
                resetPage();
              }}
              type="date"
              value={endDate}
            />
          </label>
        </div>
      </div>

      {actionError ? <p className="rounded-lg border border-rose-300/25 bg-rose-400/10 px-4 py-3 text-sm font-medium text-rose-200">{actionError}</p> : null}

      {transactionsQuery.isLoading ? <div className="app-panel-muted p-8 text-center text-sm font-medium text-fuchsia-50">Carregando transacoes...</div> : null}

      {transactionsQuery.isError ? (
        <div className="rounded-lg border border-rose-300/25 bg-rose-400/10 p-6 text-sm font-medium text-rose-200">Nao foi possivel carregar suas transacoes.</div>
      ) : null}

      {!transactionsQuery.isLoading && !transactions.length ? (
        <div className="rounded-lg border border-dashed border-white/20 bg-white/10 p-10 text-center backdrop-blur">
          <ReceiptText className="mx-auto text-fuchsia-300" size={38} />
          <h2 className="mt-4 font-serif text-2xl font-semibold text-[#f7ecff]">Nenhuma transacao encontrada</h2>
          <p className="mt-2 text-sm text-[#c8a9d8]">Crie uma receita ou despesa para iniciar o fluxo financeiro.</p>
          <Link className="mt-5 inline-flex min-h-11 items-center justify-center gap-2 rounded-lg bg-fuchsia-600 px-4 py-2 text-sm font-semibold text-white shadow-lg shadow-fuchsia-950/30 hover:bg-fuchsia-500" to="/transactions/new">
            <Plus size={17} /> Criar transacao
          </Link>
        </div>
      ) : null}

      <div className="grid gap-4">
        {transactions.map((transaction) => (
          <TransactionCard
            accounts={accounts}
            categories={categories}
            key={transaction.id}
            onCancel={(selected) => setDialog({ action: 'cancel', transaction: selected })}
            onDelete={(selected) => setDialog({ action: 'delete', transaction: selected })}
            onMarkAsPaid={(selected) => setDialog({ action: 'mark-as-paid', transaction: selected })}
            transaction={transaction}
          />
        ))}
      </div>

      {totalPages > 1 ? (
        <div className="flex items-center justify-end gap-2">
          <Button disabled={page === 0 || transactionsQuery.isFetching} onClick={() => setPage((current) => Math.max(0, current - 1))} type="button" variant="secondary">
            <ChevronLeft size={16} /> Anterior
          </Button>
          <span className="text-sm font-medium text-fuchsia-50">
            Pagina {page + 1} de {totalPages}
          </span>
          <Button disabled={page + 1 >= totalPages || transactionsQuery.isFetching} onClick={() => setPage((current) => current + 1)} type="button" variant="secondary">
            Proxima <ChevronRight size={16} />
          </Button>
        </div>
      ) : null}

      <TransactionActionDialog
        accounts={accounts}
        action={dialog?.action ?? null}
        isLoading={isActionLoading}
        onClose={() => setDialog(null)}
        onConfirm={confirmAction}
        transaction={dialog?.transaction ?? null}
      />
    </section>
  );
}
