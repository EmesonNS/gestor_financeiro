import { AxiosError } from 'axios';
import { ChevronLeft, ChevronRight, Plus, WalletCards } from 'lucide-react';
import { useMemo, useState } from 'react';
import { Link } from 'react-router';

import { Button } from '../../../shared/ui/Button';
import { AccountActionDialog } from '../components/AccountActionDialog';
import { AccountCard } from '../components/AccountCard';
import { useAccounts, useArchiveAccount, useDeleteAccount } from '../hooks/useAccounts';
import type { Account, AccountType } from '../types/accounts.types';
import { accountTypeLabels, accountTypes, formatCurrency, sumCurrentBalance } from '../utils/account-format';

type ArchiveFilter = 'active' | 'archived' | 'all';

type DialogState = {
  account: Account;
  action: 'archive' | 'delete';
} | null;

function archivedParam(filter: ArchiveFilter) {
  if (filter === 'active') {
    return false;
  }

  if (filter === 'archived') {
    return true;
  }

  return undefined;
}

export function AccountsPage() {
  const [page, setPage] = useState(0);
  const [archivedFilter, setArchivedFilter] = useState<ArchiveFilter>('active');
  const [typeFilter, setTypeFilter] = useState<'ALL' | AccountType>('ALL');
  const [dialog, setDialog] = useState<DialogState>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const filters = useMemo(
    () => ({
      archived: archivedParam(archivedFilter),
      page,
      type: typeFilter === 'ALL' ? undefined : typeFilter,
    }),
    [archivedFilter, page, typeFilter],
  );
  const accountsQuery = useAccounts(filters);
  const archiveMutation = useArchiveAccount();
  const deleteMutation = useDeleteAccount();
  const accounts = accountsQuery.data?.content ?? [];
  const activeAccounts = accounts.filter((account) => !account.archived);
  const totalBalance = sumCurrentBalance(activeAccounts);
  const totalPages = accountsQuery.data?.totalPages ?? 0;
  const isActionLoading = archiveMutation.isPending || deleteMutation.isPending;

  function updateArchiveFilter(nextFilter: ArchiveFilter) {
    setArchivedFilter(nextFilter);
    setPage(0);
  }

  function updateTypeFilter(nextType: 'ALL' | AccountType) {
    setTypeFilter(nextType);
    setPage(0);
  }

  async function confirmAction() {
    if (!dialog) {
      return;
    }

    setActionError(null);

    try {
      if (dialog.action === 'archive') {
        await archiveMutation.mutateAsync(dialog.account.id);
      } else {
        await deleteMutation.mutateAsync(dialog.account.id);
      }
      setDialog(null);
    } catch (error) {
      const message = error instanceof AxiosError ? error.response?.data?.message : null;
      setActionError(message ?? 'Nao foi possivel concluir a acao da conta.');
    }
  }

  return (
    <section className="space-y-6">
      <div className="app-page-hero">
        <div className="admin-flow-line" aria-hidden="true" />
        <div className="relative flex flex-wrap items-start justify-between gap-4">
          <div>
            <p className="app-eyebrow">Contas financeiras</p>
            <h1 className="app-hero-title mt-4">Origens do dinheiro</h1>
            <p className="app-hero-copy mt-4 max-w-2xl">Cadastre bancos, carteiras, investimentos e acompanhe o saldo por fonte.</p>
          </div>

          <div className="app-panel-muted grid min-w-56 p-4">
            <span className="inline-flex items-center gap-2 text-sm font-medium text-fuchsia-50/85">
              <WalletCards size={17} /> Saldo ativo nesta pagina
            </span>
            <strong className="mt-2 font-serif text-4xl text-[#f7ecff]">{formatCurrency(totalBalance)}</strong>
          </div>
        </div>
      </div>

      <div className="flex flex-wrap items-center justify-between gap-3 rounded-lg border border-white/10 bg-white/10 p-3 backdrop-blur">
        <div className="flex flex-wrap gap-2">
          {[
            ['active', 'Ativas'],
            ['archived', 'Arquivadas'],
            ['all', 'Todas'],
          ].map(([value, label]) => (
            <button
              className={`rounded-lg px-3 py-2 text-sm font-semibold ${archivedFilter === value ? 'bg-fuchsia-500 text-white shadow-lg shadow-fuchsia-950/20' : 'text-fuchsia-50 hover:bg-white/10'}`}
              key={value}
              onClick={() => updateArchiveFilter(value as ArchiveFilter)}
              type="button"
            >
              {label}
            </button>
          ))}
        </div>

        <div className="flex flex-wrap items-center gap-3">
          <label className="flex items-center gap-2 text-sm font-medium text-fuchsia-50">
            Tipo
            <select
              className="rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-sm text-[#f7ecff] outline-none focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20"
              onChange={(event) => updateTypeFilter(event.target.value as 'ALL' | AccountType)}
              value={typeFilter}
            >
              <option value="ALL">Todos</option>
              {accountTypes.map((type) => (
                <option key={type} value={type}>
                  {accountTypeLabels[type]}
                </option>
              ))}
            </select>
          </label>

          <Link className="inline-flex min-h-11 items-center justify-center gap-2 rounded-lg bg-fuchsia-600 px-4 py-2 text-sm font-semibold text-white shadow-lg shadow-fuchsia-950/30 hover:bg-fuchsia-500" to="/accounts/new">
            <Plus size={17} /> Nova conta
          </Link>
        </div>
      </div>

      {actionError ? <p className="rounded-lg border border-rose-300/25 bg-rose-400/10 px-4 py-3 text-sm font-medium text-rose-200">{actionError}</p> : null}

      {accountsQuery.isLoading ? <div className="app-panel-muted p-8 text-center text-sm font-medium text-fuchsia-50">Carregando contas...</div> : null}

      {accountsQuery.isError ? (
        <div className="rounded-lg border border-rose-300/25 bg-rose-400/10 p-6 text-sm font-medium text-rose-200">Nao foi possivel carregar suas contas.</div>
      ) : null}

      {!accountsQuery.isLoading && !accounts.length ? (
        <div className="rounded-lg border border-dashed border-white/20 bg-white/10 p-10 text-center backdrop-blur">
          <WalletCards className="mx-auto text-fuchsia-300" size={38} />
          <h2 className="mt-4 font-serif text-2xl font-semibold text-[#f7ecff]">Nenhuma conta encontrada</h2>
          <p className="mt-2 text-sm text-[#c8a9d8]">Crie uma conta financeira para acompanhar seu saldo por origem.</p>
          <Link className="mt-5 inline-flex min-h-11 items-center justify-center gap-2 rounded-lg bg-fuchsia-600 px-4 py-2 text-sm font-semibold text-white shadow-lg shadow-fuchsia-950/30 hover:bg-fuchsia-500" to="/accounts/new">
            <Plus size={17} /> Criar primeira conta
          </Link>
        </div>
      ) : null}

      <div className="grid gap-4 lg:grid-cols-2">
        {accounts.map((account) => (
          <AccountCard key={account.id} account={account} onArchive={(selected) => setDialog({ account: selected, action: 'archive' })} onDelete={(selected) => setDialog({ account: selected, action: 'delete' })} />
        ))}
      </div>

      {totalPages > 1 ? (
        <div className="flex items-center justify-end gap-2">
          <Button disabled={page === 0 || accountsQuery.isFetching} onClick={() => setPage((current) => Math.max(0, current - 1))} type="button" variant="secondary">
            <ChevronLeft size={16} /> Anterior
          </Button>
          <span className="text-sm font-medium text-fuchsia-50">
            Pagina {page + 1} de {totalPages}
          </span>
          <Button disabled={page + 1 >= totalPages || accountsQuery.isFetching} onClick={() => setPage((current) => current + 1)} type="button" variant="secondary">
            Proxima <ChevronRight size={16} />
          </Button>
        </div>
      ) : null}

      <AccountActionDialog action={dialog?.action ?? null} account={dialog?.account ?? null} isLoading={isActionLoading} onClose={() => setDialog(null)} onConfirm={confirmAction} />
    </section>
  );
}
