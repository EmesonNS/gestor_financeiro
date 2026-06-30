import { AxiosError } from 'axios';
import { ArrowLeft, ChevronLeft, ChevronRight, Plus, ReceiptText } from 'lucide-react';
import { useMemo, useState } from 'react';
import { Link, useParams } from 'react-router';

import { apiErrorMessage } from '../../../shared/lib/api-error';
import { Button } from '../../../shared/ui/Button';
import type { ApiErrorResponse } from '../../auth/types/auth.types';
import { useCategories } from '../../categories/hooks/useCategories';
import { useCreditCard } from '../../credit-cards/hooks/useCreditCards';
import { CardPurchaseCard } from '../components/CardPurchaseCard';
import { CardPurchaseDeleteDialog } from '../components/CardPurchaseDeleteDialog';
import { useCardPurchases, useDeleteCardPurchase } from '../hooks/useInstallments';
import type { CardPurchase, PurchaseStatus } from '../types/installments.types';
import { formatCurrency, purchaseStatuses, purchaseStatusLabels } from '../utils/installment-format';

type PurchaseStatusFilter = 'ALL' | PurchaseStatus;

const statusOptions: Array<{ label: string; value: PurchaseStatusFilter }> = [
  { label: 'Todas', value: 'ALL' },
  ...purchaseStatuses.map((status) => ({ label: purchaseStatusLabels[status], value: status })),
];

export function CardPurchasesPage() {
  const { cardId } = useParams();
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState<PurchaseStatusFilter>('ACTIVE');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [purchaseToDelete, setPurchaseToDelete] = useState<CardPurchase | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const filters = useMemo(
    () => ({
      endDate: endDate || undefined,
      page,
      startDate: startDate || undefined,
      status: statusFilter === 'ALL' ? undefined : statusFilter,
    }),
    [endDate, page, startDate, statusFilter],
  );
  const cardQuery = useCreditCard(cardId);
  const purchasesQuery = useCardPurchases(cardId, filters);
  const categoriesQuery = useCategories({ page: 0, type: 'EXPENSE' });
  const deleteMutation = useDeleteCardPurchase();
  const card = cardQuery.data;
  const purchases = purchasesQuery.data?.content ?? [];
  const categories = categoriesQuery.data?.content ?? [];
  const totalPages = purchasesQuery.data?.totalPages ?? 0;
  const totalAmount = purchases.reduce((total, purchase) => total + Number(purchase.totalAmount), 0);
  const totalInstallments = purchases.reduce((total, purchase) => total + purchase.installmentCount, 0);

  function resetPage() {
    setPage(0);
  }

  async function confirmDelete() {
    if (!purchaseToDelete) {
      return;
    }

    setActionError(null);

    try {
      await deleteMutation.mutateAsync(purchaseToDelete.id);
      setPurchaseToDelete(null);
    } catch (error) {
      const fallback = error instanceof AxiosError ? (error.response?.data as ApiErrorResponse | undefined)?.message : undefined;
      setActionError(apiErrorMessage(error, fallback ?? 'Nao foi possivel excluir esta compra.'));
    }
  }

  if (cardQuery.isLoading) {
    return <div className="app-panel-muted p-8 text-center text-sm font-medium text-fuchsia-50">Carregando cartao...</div>;
  }

  if (cardQuery.isError || !card) {
    return (
      <section className="rounded-lg border border-rose-300/25 bg-rose-400/10 p-6">
        <h1 className="font-serif text-2xl font-semibold text-rose-100">Cartao nao encontrado</h1>
        <p className="mt-2 text-sm text-rose-200">Nao foi possivel carregar as compras deste cartao.</p>
        <Link className="mt-4 inline-flex font-semibold text-rose-100" to="/credit-cards">
          Voltar para cartoes
        </Link>
      </section>
    );
  }

  return (
    <section className="space-y-6">
      <Link className="inline-flex items-center gap-2 text-sm font-semibold text-fuchsia-100 hover:text-white" to={`/credit-cards/${card.id}`}>
        <ArrowLeft size={16} /> Voltar para o cartao
      </Link>

      <div className="app-page-hero">
        <div className="admin-flow-line" aria-hidden="true" />
        <div className="relative flex flex-wrap items-start justify-between gap-4">
          <div>
            <p className="app-eyebrow">Compras parceladas</p>
            <h1 className="app-hero-title mt-4">{card.name}</h1>
            <p className="app-hero-copy mt-4 max-w-2xl">Registre compras no cartao e acompanhe as parcelas geradas por fatura.</p>
          </div>

          <div className="app-panel-muted grid min-w-56 gap-3 p-4">
            <span className="inline-flex items-center gap-2 text-sm font-medium text-fuchsia-50/85">
              <ReceiptText size={17} /> Total nesta pagina
            </span>
            <strong className="font-serif text-4xl text-[#f7ecff]">{formatCurrency(totalAmount)}</strong>
            <span className="text-sm text-[#c8a9d8]">{totalInstallments} parcelas listadas</span>
          </div>
        </div>
      </div>

      <div className="grid gap-3 rounded-lg border border-white/10 bg-white/10 p-3 backdrop-blur lg:grid-cols-[1fr_auto]">
        <div className="grid gap-3 md:grid-cols-[1fr_auto_auto]">
          <div className="flex flex-wrap gap-2">
            {statusOptions.map((option) => (
              <button
                className={`min-h-10 rounded-lg px-3 py-2 text-sm font-semibold transition ${
                  statusFilter === option.value ? 'bg-fuchsia-600 text-white shadow-lg shadow-fuchsia-950/30' : 'border border-white/10 bg-white/10 text-fuchsia-100 hover:bg-white/15'
                }`}
                key={option.value}
                onClick={() => {
                  setStatusFilter(option.value);
                  resetPage();
                }}
                type="button"
              >
                {option.label}
              </button>
            ))}
          </div>

          <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="purchaseStartDate">
            Inicio
            <input
              className="mt-2 min-h-10 rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20"
              id="purchaseStartDate"
              onChange={(event) => {
                setStartDate(event.target.value);
                resetPage();
              }}
              type="date"
              value={startDate}
            />
          </label>

          <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="purchaseEndDate">
            Fim
            <input
              className="mt-2 min-h-10 rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20"
              id="purchaseEndDate"
              onChange={(event) => {
                setEndDate(event.target.value);
                resetPage();
              }}
              type="date"
              value={endDate}
            />
          </label>
        </div>

        <Link className="inline-flex min-h-11 items-center justify-center gap-2 rounded-lg bg-fuchsia-600 px-4 py-2 text-sm font-semibold text-white shadow-lg shadow-fuchsia-950/30 hover:bg-fuchsia-500" to={`/credit-cards/${card.id}/purchases/new`}>
          <Plus size={17} /> Nova compra
        </Link>
      </div>

      {actionError ? <p className="rounded-lg border border-rose-300/25 bg-rose-400/10 px-4 py-3 text-sm font-medium text-rose-200">{actionError}</p> : null}
      {purchasesQuery.isLoading ? <div className="app-panel-muted p-8 text-center text-sm font-medium text-fuchsia-50">Carregando compras...</div> : null}
      {purchasesQuery.isError ? <div className="rounded-lg border border-rose-300/25 bg-rose-400/10 p-6 text-sm font-medium text-rose-200">Nao foi possivel carregar as compras.</div> : null}

      {!purchasesQuery.isLoading && !purchases.length ? (
        <div className="rounded-lg border border-dashed border-white/20 bg-white/10 p-10 text-center backdrop-blur">
          <ReceiptText className="mx-auto text-fuchsia-300" size={38} />
          <h2 className="mt-4 font-serif text-2xl font-semibold text-[#f7ecff]">Nenhuma compra encontrada</h2>
          <p className="mt-2 text-sm text-[#c8a9d8]">Registre uma compra para gerar parcelas nas faturas do cartao.</p>
          <Link className="mt-5 inline-flex min-h-11 items-center justify-center gap-2 rounded-lg bg-fuchsia-600 px-4 py-2 text-sm font-semibold text-white shadow-lg shadow-fuchsia-950/30 hover:bg-fuchsia-500" to={`/credit-cards/${card.id}/purchases/new`}>
            <Plus size={17} /> Criar compra
          </Link>
        </div>
      ) : null}

      <div className="grid gap-4 lg:grid-cols-2">
        {purchases.map((purchase) => (
          <CardPurchaseCard categories={categories} key={purchase.id} onDelete={setPurchaseToDelete} purchase={purchase} />
        ))}
      </div>

      {totalPages > 1 ? (
        <div className="flex items-center justify-end gap-2">
          <Button disabled={page === 0 || purchasesQuery.isFetching} onClick={() => setPage((current) => Math.max(0, current - 1))} type="button" variant="secondary">
            <ChevronLeft size={16} /> Anterior
          </Button>
          <span className="text-sm font-medium text-fuchsia-50">
            Pagina {page + 1} de {totalPages}
          </span>
          <Button disabled={page + 1 >= totalPages || purchasesQuery.isFetching} onClick={() => setPage((current) => current + 1)} type="button" variant="secondary">
            Proxima <ChevronRight size={16} />
          </Button>
        </div>
      ) : null}

      <CardPurchaseDeleteDialog isLoading={deleteMutation.isPending} onClose={() => setPurchaseToDelete(null)} onConfirm={confirmDelete} purchase={purchaseToDelete} />
    </section>
  );
}
