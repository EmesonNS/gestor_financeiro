import { AxiosError } from 'axios';
import { ArrowLeft, ChevronLeft, ChevronRight, Pencil, ReceiptText, Trash2 } from 'lucide-react';
import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router';

import { apiErrorMessage } from '../../../shared/lib/api-error';
import { Button } from '../../../shared/ui/Button';
import type { ApiErrorResponse } from '../../auth/types/auth.types';
import { useCategory } from '../../categories/hooks/useCategories';
import { CardPurchaseDeleteDialog } from '../components/CardPurchaseDeleteDialog';
import { InstallmentMiniCard } from '../components/InstallmentMiniCard';
import { useCardPurchase, useDeleteCardPurchase, usePurchaseInstallments } from '../hooks/useInstallments';
import type { CardPurchase } from '../types/installments.types';
import { formatCurrency, formatDate, hasPaidInstallment, purchaseStatusLabels, purchaseStatusTone } from '../utils/installment-format';

export function CardPurchaseDetailsPage() {
  const { purchaseId } = useParams();
  const navigate = useNavigate();
  const [page, setPage] = useState(0);
  const [purchaseToDelete, setPurchaseToDelete] = useState<CardPurchase | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const purchaseQuery = useCardPurchase(purchaseId);
  const purchase = purchaseQuery.data;
  const categoryQuery = useCategory(purchase?.categoryId);
  const installmentsQuery = usePurchaseInstallments(purchaseId, page);
  const deleteMutation = useDeleteCardPurchase();
  const installments = installmentsQuery.data?.content ?? purchase?.installments ?? [];
  const totalPages = installmentsQuery.data?.totalPages ?? 0;
  const hasPaid = hasPaidInstallment(purchase?.installments ?? []);

  async function confirmDelete() {
    if (!purchaseToDelete) {
      return;
    }

    setActionError(null);

    try {
      await deleteMutation.mutateAsync(purchaseToDelete.id);
      navigate(`/credit-cards/${purchaseToDelete.creditCardId}/purchases`, { replace: true });
    } catch (error) {
      const fallback = error instanceof AxiosError ? (error.response?.data as ApiErrorResponse | undefined)?.message : undefined;
      setActionError(apiErrorMessage(error, fallback ?? 'Nao foi possivel excluir esta compra.'));
    }
  }

  if (purchaseQuery.isLoading) {
    return <div className="app-panel-muted p-8 text-center text-sm font-medium text-fuchsia-50">Carregando compra...</div>;
  }

  if (purchaseQuery.isError || !purchase) {
    return (
      <section className="rounded-lg border border-rose-300/25 bg-rose-400/10 p-6">
        <h1 className="font-serif text-2xl font-semibold text-rose-100">Compra nao encontrada</h1>
        <p className="mt-2 text-sm text-rose-200">Nao foi possivel carregar esta compra.</p>
        <Link className="mt-4 inline-flex font-semibold text-rose-100" to="/credit-cards">
          Voltar para cartoes
        </Link>
      </section>
    );
  }

  return (
    <section className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <Link className="inline-flex items-center gap-2 text-sm font-semibold text-fuchsia-100 hover:text-white" to={`/credit-cards/${purchase.creditCardId}/purchases`}>
          <ArrowLeft size={16} /> Voltar para compras
        </Link>
        <div className="flex flex-wrap gap-2">
          <Link className="inline-flex min-h-10 items-center justify-center gap-2 rounded-lg border border-white/15 bg-white/10 px-3 py-2 text-sm font-semibold text-fuchsia-50 hover:bg-white/15" to={`/card-purchases/${purchase.id}/edit`}>
            <Pencil size={16} /> Editar
          </Link>
          <Button className="min-h-10 border-rose-300/25 px-3 py-2 text-rose-200 hover:bg-rose-400/10" onClick={() => setPurchaseToDelete(purchase)} type="button" variant="secondary">
            <Trash2 size={16} /> Excluir
          </Button>
        </div>
      </div>

      <div className="app-page-hero">
        <div className="admin-flow-line" aria-hidden="true" />
        <div className="relative flex flex-wrap items-start justify-between gap-4">
          <div>
            <p className="app-eyebrow">Detalhes da compra</p>
            <h1 className="app-hero-title mt-4">{purchase.description}</h1>
            <p className="app-hero-copy mt-4 max-w-xl">Parcelas geradas, faturas vinculadas e regras de edicao quando ha fatura paga.</p>
          </div>
          <span className={`rounded-full border px-3 py-2 text-sm font-bold ${purchaseStatusTone[purchase.status]}`}>{purchaseStatusLabels[purchase.status]}</span>
        </div>
      </div>

      {actionError ? <p className="rounded-lg border border-rose-300/25 bg-rose-400/10 px-4 py-3 text-sm font-medium text-rose-200">{actionError}</p> : null}

      <section className="app-panel p-6">
        <div className="grid gap-5 lg:grid-cols-[1fr_0.8fr]">
          <div>
            <p className="app-eyebrow">Compra</p>
            <h2 className="mt-2 font-serif text-4xl font-semibold text-[#f7ecff]">{formatCurrency(purchase.totalAmount)}</h2>
            <p className="mt-3 text-sm leading-6 text-[#c8a9d8]">
              Compra em {formatDate(purchase.purchaseDate)}, categoria <strong className="text-[#f7ecff]">{categoryQuery.data?.name ?? 'Categoria'}</strong>, parcelada em{' '}
              <strong className="text-[#f7ecff]">{purchase.installmentCount}x</strong>.
            </p>
            {purchase.notes ? <p className="mt-4 rounded-lg border border-white/10 bg-white/10 px-3 py-2 text-sm text-[#c8a9d8]">{purchase.notes}</p> : null}
          </div>

          <div className="rounded-lg border border-white/10 bg-white/10 p-4">
            <div className="flex items-center gap-3">
              <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-lg border border-white/10 bg-fuchsia-400/15 text-fuchsia-200">
                <ReceiptText size={22} />
              </span>
              <div>
                <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]">Regra de edicao</p>
                <p className="mt-1 text-sm font-semibold text-[#f7ecff]">{hasPaid ? 'Bloqueio esperado pelo backend' : 'Sem fatura paga nesta compra'}</p>
              </div>
            </div>
            <p className="mt-4 text-sm leading-6 text-[#c8a9d8]">Se alguma parcela estiver em fatura paga, editar ou excluir deve ser recusado pela API.</p>
          </div>
        </div>
      </section>

      <section className="space-y-4">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <p className="app-eyebrow">Parcelas</p>
            <h2 className="mt-2 font-serif text-2xl font-semibold text-[#f7ecff]">Cronograma gerado</h2>
          </div>
        </div>

        {installmentsQuery.isLoading ? <div className="app-panel-muted p-8 text-center text-sm font-medium text-fuchsia-50">Carregando parcelas...</div> : null}
        {installmentsQuery.isError ? <div className="rounded-lg border border-rose-300/25 bg-rose-400/10 p-6 text-sm font-medium text-rose-200">Nao foi possivel carregar parcelas.</div> : null}

        <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
          {installments.map((installment) => (
            <InstallmentMiniCard installment={installment} key={installment.id} />
          ))}
        </div>

        {totalPages > 1 ? (
          <div className="flex items-center justify-end gap-2">
            <Button disabled={page === 0 || installmentsQuery.isFetching} onClick={() => setPage((current) => Math.max(0, current - 1))} type="button" variant="secondary">
              <ChevronLeft size={16} /> Anterior
            </Button>
            <span className="text-sm font-medium text-fuchsia-50">
              Pagina {page + 1} de {totalPages}
            </span>
            <Button disabled={page + 1 >= totalPages || installmentsQuery.isFetching} onClick={() => setPage((current) => current + 1)} type="button" variant="secondary">
              Proxima <ChevronRight size={16} />
            </Button>
          </div>
        ) : null}
      </section>

      <CardPurchaseDeleteDialog isLoading={deleteMutation.isPending} onClose={() => setPurchaseToDelete(null)} onConfirm={confirmDelete} purchase={purchaseToDelete} />
    </section>
  );
}
