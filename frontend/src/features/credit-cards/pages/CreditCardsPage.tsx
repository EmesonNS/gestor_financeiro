import { AxiosError } from 'axios';
import { ChevronLeft, ChevronRight, CreditCard, Plus } from 'lucide-react';
import { useMemo, useState } from 'react';
import { Link } from 'react-router';

import { Button } from '../../../shared/ui/Button';
import type { ApiErrorResponse } from '../../auth/types/auth.types';
import { CreditCardActionDialog } from '../components/CreditCardActionDialog';
import { CreditCardLimitCard } from '../components/CreditCardLimitCard';
import { useArchiveCreditCard, useCreditCards, useDeleteCreditCard } from '../hooks/useCreditCards';
import type { CreditCard as CreditCardType } from '../types/credit-cards.types';
import { formatCurrency } from '../utils/credit-card-format';

type ArchiveFilter = 'active' | 'archived' | 'all';

type DialogState = {
  action: 'archive' | 'delete';
  card: CreditCardType;
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

export function CreditCardsPage() {
  const [page, setPage] = useState(0);
  const [archiveFilter, setArchiveFilter] = useState<ArchiveFilter>('active');
  const [dialog, setDialog] = useState<DialogState>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const filters = useMemo(
    () => ({
      archived: archivedParam(archiveFilter),
      page,
    }),
    [archiveFilter, page],
  );
  const cardsQuery = useCreditCards(filters);
  const archiveMutation = useArchiveCreditCard();
  const deleteMutation = useDeleteCreditCard();
  const cards = cardsQuery.data?.content ?? [];
  const activeCards = cards.filter((card) => !card.archived);
  const totalLimit = activeCards.reduce((total, card) => total + Number(card.limitAmount), 0);
  const totalUsed = activeCards.reduce((total, card) => total + Number(card.usedLimit), 0);
  const totalAvailable = activeCards.reduce((total, card) => total + Number(card.availableLimit), 0);
  const totalPages = cardsQuery.data?.totalPages ?? 0;
  const isActionLoading = archiveMutation.isPending || deleteMutation.isPending;

  function updateArchiveFilter(nextFilter: ArchiveFilter) {
    setArchiveFilter(nextFilter);
    setPage(0);
  }

  async function confirmAction() {
    if (!dialog) {
      return;
    }

    setActionError(null);

    try {
      if (dialog.action === 'archive') {
        await archiveMutation.mutateAsync(dialog.card.id);
      } else {
        await deleteMutation.mutateAsync(dialog.card.id);
      }
      setDialog(null);
    } catch (error) {
      const message = error instanceof AxiosError ? (error.response?.data as ApiErrorResponse | undefined)?.message : null;
      setActionError(message ?? 'Nao foi possivel concluir a acao do cartao.');
    }
  }

  return (
    <section className="space-y-6">
      <div className="app-page-hero">
        <div className="admin-flow-line" aria-hidden="true" />
        <div className="relative flex flex-wrap items-start justify-between gap-4">
          <div>
            <p className="app-eyebrow">Cartoes de credito</p>
            <h1 className="app-hero-title mt-4">Limites sob controle</h1>
            <p className="app-hero-copy mt-4 max-w-2xl">Cadastre cartoes, acompanhe limite usado e mantenha fechamento e vencimento organizados.</p>
          </div>

          <div className="app-panel-muted grid min-w-56 gap-3 p-4">
            <span className="inline-flex items-center gap-2 text-sm font-medium text-fuchsia-50/85">
              <CreditCard size={17} /> Disponivel nesta pagina
            </span>
            <strong className="font-serif text-4xl text-[#f7ecff]">{formatCurrency(totalAvailable)}</strong>
            <span className="text-sm text-[#c8a9d8]">
              Usado: <strong className="text-[#f7ecff]">{formatCurrency(totalUsed)}</strong> de {formatCurrency(totalLimit)}
            </span>
          </div>
        </div>
      </div>

      <div className="flex flex-wrap items-center justify-between gap-3 rounded-lg border border-white/10 bg-white/10 p-3 backdrop-blur">
        <div className="flex flex-wrap gap-2">
          {[
            ['active', 'Ativos'],
            ['archived', 'Arquivados'],
            ['all', 'Todos'],
          ].map(([value, label]) => (
            <button
              className={`rounded-lg px-3 py-2 text-sm font-semibold ${archiveFilter === value ? 'bg-fuchsia-500 text-white shadow-lg shadow-fuchsia-950/20' : 'text-fuchsia-50 hover:bg-white/10'}`}
              key={value}
              onClick={() => updateArchiveFilter(value as ArchiveFilter)}
              type="button"
            >
              {label}
            </button>
          ))}
        </div>

        <Link className="inline-flex min-h-11 items-center justify-center gap-2 rounded-lg bg-fuchsia-600 px-4 py-2 text-sm font-semibold text-white shadow-lg shadow-fuchsia-950/30 hover:bg-fuchsia-500" to="/credit-cards/new">
          <Plus size={17} /> Novo cartao
        </Link>
      </div>

      {actionError ? <p className="rounded-lg border border-rose-300/25 bg-rose-400/10 px-4 py-3 text-sm font-medium text-rose-200">{actionError}</p> : null}

      {cardsQuery.isLoading ? <div className="app-panel-muted p-8 text-center text-sm font-medium text-fuchsia-50">Carregando cartoes...</div> : null}

      {cardsQuery.isError ? <div className="rounded-lg border border-rose-300/25 bg-rose-400/10 p-6 text-sm font-medium text-rose-200">Nao foi possivel carregar seus cartoes.</div> : null}

      {!cardsQuery.isLoading && !cards.length ? (
        <div className="rounded-lg border border-dashed border-white/20 bg-white/10 p-10 text-center backdrop-blur">
          <CreditCard className="mx-auto text-fuchsia-300" size={38} />
          <h2 className="mt-4 font-serif text-2xl font-semibold text-[#f7ecff]">Nenhum cartao encontrado</h2>
          <p className="mt-2 text-sm text-[#c8a9d8]">Crie um cartao para acompanhar limite total, usado e disponivel.</p>
          <Link className="mt-5 inline-flex min-h-11 items-center justify-center gap-2 rounded-lg bg-fuchsia-600 px-4 py-2 text-sm font-semibold text-white shadow-lg shadow-fuchsia-950/30 hover:bg-fuchsia-500" to="/credit-cards/new">
            <Plus size={17} /> Criar cartao
          </Link>
        </div>
      ) : null}

      <div className="grid gap-4 lg:grid-cols-2">
        {cards.map((card) => (
          <CreditCardLimitCard key={card.id} card={card} onArchive={(selected) => setDialog({ action: 'archive', card: selected })} onDelete={(selected) => setDialog({ action: 'delete', card: selected })} />
        ))}
      </div>

      {totalPages > 1 ? (
        <div className="flex items-center justify-end gap-2">
          <Button disabled={page === 0 || cardsQuery.isFetching} onClick={() => setPage((current) => Math.max(0, current - 1))} type="button" variant="secondary">
            <ChevronLeft size={16} /> Anterior
          </Button>
          <span className="text-sm font-medium text-fuchsia-50">
            Pagina {page + 1} de {totalPages}
          </span>
          <Button disabled={page + 1 >= totalPages || cardsQuery.isFetching} onClick={() => setPage((current) => current + 1)} type="button" variant="secondary">
            Proxima <ChevronRight size={16} />
          </Button>
        </div>
      ) : null}

      <CreditCardActionDialog action={dialog?.action ?? null} card={dialog?.card ?? null} isLoading={isActionLoading} onClose={() => setDialog(null)} onConfirm={confirmAction} />
    </section>
  );
}
