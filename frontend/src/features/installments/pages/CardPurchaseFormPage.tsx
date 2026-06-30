import { ArrowLeft } from 'lucide-react';
import { useMemo, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router';

import { apiErrorMessage } from '../../../shared/lib/api-error';
import { useCategories, useCategory } from '../../categories/hooks/useCategories';
import type { Category } from '../../categories/types/categories.types';
import { useCreditCard } from '../../credit-cards/hooks/useCreditCards';
import { CardPurchaseForm } from '../components/CardPurchaseForm';
import { useCardPurchase, useCreateCardPurchase, useUpdateCardPurchase } from '../hooks/useInstallments';
import type { CardPurchaseFormData } from '../schemas/installment.schemas';

function mergeById<T extends { id: string }>(items: T[], item?: T | null) {
  if (!item || items.some((current) => current.id === item.id)) {
    return items;
  }

  return [item, ...items];
}

export function CardPurchaseFormPage() {
  const { cardId, purchaseId } = useParams();
  const isEdit = Boolean(purchaseId);
  const navigate = useNavigate();
  const cardQuery = useCreditCard(cardId);
  const purchaseQuery = useCardPurchase(purchaseId);
  const purchase = purchaseQuery.data;
  const categoriesQuery = useCategories({ page: 0, type: 'EXPENSE' });
  const currentCategoryQuery = useCategory(purchase?.categoryId);
  const createMutation = useCreateCardPurchase(cardId ?? '');
  const updateMutation = useUpdateCardPurchase(purchaseId ?? '');
  const [submitError, setSubmitError] = useState<string | null>(null);
  const categories = useMemo<Category[]>(
    () => mergeById(categoriesQuery.data?.content ?? [], currentCategoryQuery.data),
    [categoriesQuery.data?.content, currentCategoryQuery.data],
  );
  const effectiveCardId = cardId ?? purchase?.creditCardId;

  async function submit(data: CardPurchaseFormData) {
    setSubmitError(null);
    const payload = {
      categoryId: data.categoryId,
      description: data.description,
      installmentCount: data.installmentCount,
      notes: data.notes || null,
      purchaseDate: data.purchaseDate,
      totalAmount: data.totalAmount,
    };

    try {
      if (isEdit && purchaseId) {
        await updateMutation.mutateAsync(payload);
        navigate(`/card-purchases/${purchaseId}`, { replace: true });
      } else {
        await createMutation.mutateAsync(payload);
        navigate(`/credit-cards/${cardId}/purchases`, { replace: true });
      }
    } catch (error) {
      setSubmitError(apiErrorMessage(error, 'Nao foi possivel salvar a compra.'));
    }
  }

  if ((cardId && cardQuery.isLoading) || (isEdit && purchaseQuery.isLoading)) {
    return <div className="app-panel-muted p-8 text-center text-sm font-medium text-fuchsia-50">Carregando compra...</div>;
  }

  if ((cardId && (cardQuery.isError || !cardQuery.data)) || (isEdit && (purchaseQuery.isError || !purchase))) {
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
      <Link className="inline-flex items-center gap-2 text-sm font-semibold text-fuchsia-100 hover:text-white" to={isEdit && purchase ? `/card-purchases/${purchase.id}` : `/credit-cards/${cardId}/purchases`}>
        <ArrowLeft size={16} /> Voltar
      </Link>

      <div className="app-page-hero">
        <div className="admin-flow-line" aria-hidden="true" />
        <div className="relative max-w-3xl">
          <p className="app-eyebrow">Compras parceladas</p>
          <h1 className="app-hero-title mt-4">{isEdit ? 'Editar compra' : 'Nova compra'}</h1>
          <p className="app-hero-copy mt-4 max-w-xl">
            {isEdit ? 'Ajuste a compra e regenere parcelas quando permitido pelo backend.' : 'Informe valor total e quantidade de parcelas para gerar faturas automaticamente.'}
          </p>
        </div>
      </div>

      <CardPurchaseForm
        categories={categories}
        isLoading={createMutation.isPending || updateMutation.isPending}
        mode={isEdit ? 'edit' : 'create'}
        onSubmit={submit}
        purchase={purchase}
        submitError={submitError || (!effectiveCardId ? 'Cartao nao informado para esta compra.' : null)}
      />
    </section>
  );
}
