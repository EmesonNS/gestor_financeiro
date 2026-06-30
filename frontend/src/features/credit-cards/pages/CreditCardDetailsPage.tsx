import { ArrowLeft, Archive, Pencil, ReceiptText, ShoppingBag } from 'lucide-react';
import { Link, useParams } from 'react-router';

import { CardLimitSummary } from '../components/CardLimitSummary';
import { useCreditCard } from '../hooks/useCreditCards';
import { formatDateTime } from '../utils/credit-card-format';

export function CreditCardDetailsPage() {
  const { id } = useParams();
  const cardQuery = useCreditCard(id);
  const card = cardQuery.data;

  if (cardQuery.isLoading) {
    return <div className="app-panel-muted p-8 text-center text-sm font-medium text-fuchsia-50">Carregando cartao...</div>;
  }

  if (cardQuery.isError || !card) {
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
      <div className="flex flex-wrap items-center justify-between gap-3">
        <Link className="inline-flex items-center gap-2 text-sm font-semibold text-fuchsia-100 hover:text-white" to="/credit-cards">
          <ArrowLeft size={16} /> Voltar para cartoes
        </Link>
        <Link className="inline-flex min-h-10 items-center justify-center gap-2 rounded-lg border border-white/15 bg-white/10 px-3 py-2 text-sm font-semibold text-fuchsia-50 hover:bg-white/15" to={`/credit-cards/${card.id}/edit`}>
          <Pencil size={16} /> Editar
        </Link>
        <Link className="inline-flex min-h-10 items-center justify-center gap-2 rounded-lg bg-fuchsia-600 px-3 py-2 text-sm font-semibold text-white shadow-lg shadow-fuchsia-950/30 hover:bg-fuchsia-500" to={`/credit-cards/${card.id}/invoices`}>
          <ReceiptText size={16} /> Faturas
        </Link>
        <Link className="inline-flex min-h-10 items-center justify-center gap-2 rounded-lg bg-fuchsia-600 px-3 py-2 text-sm font-semibold text-white shadow-lg shadow-fuchsia-950/30 hover:bg-fuchsia-500" to={`/credit-cards/${card.id}/purchases`}>
          <ShoppingBag size={16} /> Compras
        </Link>
      </div>

      <div className="app-page-hero">
        <div className="admin-flow-line" aria-hidden="true" />
        <div className="relative flex flex-wrap items-start justify-between gap-4">
          <div>
            <p className="app-eyebrow">Detalhes do cartao</p>
            <h1 className="app-hero-title mt-4">{card.name}</h1>
            <p className="app-hero-copy mt-4 max-w-xl">Visao consolidada de limite total, utilizado, disponivel e calendario da fatura.</p>
          </div>
          {card.archived ? (
            <span className="inline-flex min-h-10 items-center gap-2 rounded-lg border border-slate-300/20 bg-slate-400/10 px-3 py-2 text-sm font-bold text-slate-200">
              <Archive size={16} /> Arquivado
            </span>
          ) : null}
        </div>
      </div>

      <CardLimitSummary card={card} />

      <section className="app-panel-muted p-5">
        <p className="app-eyebrow">Historico</p>
        <div className="mt-4 grid gap-3 sm:grid-cols-2">
          <div className="rounded-lg border border-white/10 bg-[#24112f]/70 p-4">
            <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]">Criado em</p>
            <p className="mt-2 text-sm font-semibold text-[#f7ecff]">{formatDateTime(card.createdAt)}</p>
          </div>
          <div className="rounded-lg border border-white/10 bg-[#24112f]/70 p-4">
            <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]">Atualizado em</p>
            <p className="mt-2 text-sm font-semibold text-[#f7ecff]">{formatDateTime(card.updatedAt)}</p>
          </div>
        </div>
      </section>

      <section className="app-panel-muted p-5">
        <p className="app-eyebrow">Operacao</p>
        <h2 className="mt-2 font-serif text-2xl font-semibold text-[#f7ecff]">Faturas e compras conectadas</h2>
        <p className="mt-2 max-w-2xl text-sm leading-6 text-[#c8a9d8]">Compras no cartao geram parcelas, alimentam faturas e atualizam o limite usado sem movimentar saldo de conta ate o pagamento da fatura.</p>
      </section>
    </section>
  );
}
