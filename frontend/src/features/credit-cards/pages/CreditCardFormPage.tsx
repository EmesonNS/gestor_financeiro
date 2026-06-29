import { ArrowLeft } from 'lucide-react';
import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router';

import { apiErrorMessage } from '../../../shared/lib/api-error';
import { CreditCardForm } from '../components/CreditCardForm';
import { useCreateCreditCard, useCreditCard, useUpdateCreditCard } from '../hooks/useCreditCards';
import type { CreditCardFormData } from '../schemas/credit-card.schemas';

export function CreditCardFormPage() {
  const { id } = useParams();
  const isEdit = Boolean(id);
  const navigate = useNavigate();
  const cardQuery = useCreditCard(id);
  const card = cardQuery.data;
  const createMutation = useCreateCreditCard();
  const updateMutation = useUpdateCreditCard(id ?? '');
  const [submitError, setSubmitError] = useState<string | null>(null);

  async function submit(data: CreditCardFormData) {
    setSubmitError(null);

    try {
      if (isEdit && id) {
        await updateMutation.mutateAsync(data);
      } else {
        await createMutation.mutateAsync(data);
      }
      navigate('/credit-cards', { replace: true });
    } catch (error) {
      setSubmitError(apiErrorMessage(error, 'Nao foi possivel salvar o cartao.'));
    }
  }

  if (isEdit && cardQuery.isLoading) {
    return <div className="app-panel-muted p-8 text-center text-sm font-medium text-fuchsia-50">Carregando cartao...</div>;
  }

  if (isEdit && (cardQuery.isError || !card)) {
    return (
      <section className="rounded-lg border border-rose-300/25 bg-rose-400/10 p-6">
        <h1 className="font-serif text-2xl font-semibold text-rose-100">Cartao nao encontrado</h1>
        <p className="mt-2 text-sm text-rose-200">Nao foi possivel carregar este cartao.</p>
        <Link className="mt-4 inline-flex font-semibold text-rose-100" to="/credit-cards">
          Voltar para cartoes
        </Link>
      </section>
    );
  }

  return (
    <section className="space-y-6">
      <Link className="inline-flex items-center gap-2 text-sm font-semibold text-fuchsia-100 hover:text-white" to="/credit-cards">
        <ArrowLeft size={16} /> Voltar para cartoes
      </Link>

      <div className="app-page-hero">
        <div className="admin-flow-line" aria-hidden="true" />
        <div className="relative max-w-3xl">
          <p className="app-eyebrow">Cartoes de credito</p>
          <h1 className="app-hero-title mt-4">{isEdit ? 'Editar cartao' : 'Novo cartao'}</h1>
          <p className="app-hero-copy mt-4 max-w-xl">
            {isEdit ? 'Atualize limite, fechamento e vencimento do cartao.' : 'Cadastre um cartao para controlar limite usado e disponivel.'}
          </p>
        </div>
      </div>

      <CreditCardForm
        card={card}
        isLoading={createMutation.isPending || updateMutation.isPending}
        mode={isEdit ? 'edit' : 'create'}
        onSubmit={submit}
        submitError={submitError}
      />
    </section>
  );
}
