import { Archive, CalendarClock, CreditCard, Eye, Pencil, Trash2 } from 'lucide-react';
import { Link } from 'react-router';

import { Button } from '../../../shared/ui/Button';
import type { CreditCard as CreditCardType } from '../types/credit-cards.types';
import { formatCurrency, formatDateTime, formatPercent, limitUsagePercent, statementDayCopy } from '../utils/credit-card-format';

type CreditCardLimitCardProps = {
  card: CreditCardType;
  onArchive: (card: CreditCardType) => void;
  onDelete: (card: CreditCardType) => void;
};

export function CreditCardLimitCard({ card, onArchive, onDelete }: CreditCardLimitCardProps) {
  const usagePercent = limitUsagePercent(card.usedLimit, card.limitAmount);

  return (
    <article className="app-panel overflow-hidden p-5">
      <div className="rounded-xl border border-white/10 bg-gradient-to-br from-[#4b1f64] via-[#2a1638] to-[#160722] p-5 shadow-2xl shadow-fuchsia-950/30">
        <div className="flex items-start justify-between gap-4">
          <div>
            <p className="text-xs font-bold uppercase tracking-[0.2em] text-fuchsia-200">Cartao de credito</p>
            <h2 className="mt-3 font-serif text-2xl font-semibold text-[#f7ecff]">{card.name}</h2>
          </div>
          <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-lg border border-white/15 bg-white/10 text-fuchsia-100">
            <CreditCard size={22} />
          </span>
        </div>

        <div className="mt-8 grid grid-cols-[1fr_auto] items-end gap-4">
          <div>
            <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]">Limite disponivel</p>
            <strong className="mt-2 block font-serif text-3xl text-[#f7ecff]">{formatCurrency(card.availableLimit)}</strong>
          </div>
          {card.archived ? <span className="rounded-full border border-slate-300/20 bg-slate-400/10 px-2 py-1 text-xs font-bold text-slate-200">Arquivado</span> : null}
        </div>
      </div>

      <div className="mt-5">
        <div className="flex items-center justify-between gap-3 text-sm">
          <span className="font-medium text-[#c8a9d8]">Uso do limite</span>
          <strong className="text-fuchsia-100">{formatPercent(usagePercent)}</strong>
        </div>
        <div className="mt-3 h-3 overflow-hidden rounded-full border border-white/10 bg-[#24112f]">
          <div className="h-full rounded-full bg-gradient-to-r from-emerald-400 via-fuchsia-500 to-[#ff6bb5]" style={{ width: `${usagePercent}%` }} />
        </div>
      </div>

      <div className="mt-5 grid gap-3 sm:grid-cols-3">
        <div className="rounded-lg border border-white/10 bg-white/10 p-3">
          <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]">Total</p>
          <p className="mt-2 text-sm font-semibold text-[#f7ecff]">{formatCurrency(card.limitAmount)}</p>
        </div>
        <div className="rounded-lg border border-white/10 bg-white/10 p-3">
          <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]">Usado</p>
          <p className="mt-2 text-sm font-semibold text-[#f7ecff]">{formatCurrency(card.usedLimit)}</p>
        </div>
        <div className="rounded-lg border border-white/10 bg-white/10 p-3">
          <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]">Disponivel</p>
          <p className="mt-2 text-sm font-semibold text-[#f7ecff]">{formatCurrency(card.availableLimit)}</p>
        </div>
      </div>

      <div className="mt-5 flex flex-wrap gap-3 border-t border-white/10 pt-4 text-sm text-[#c8a9d8]">
        <span className="inline-flex items-center gap-2">
          <CalendarClock size={16} /> {statementDayCopy(card.closingDay, card.dueDay)}
        </span>
        <span>Atualizado em {formatDateTime(card.updatedAt)}</span>
      </div>

      <div className="mt-5 flex flex-wrap gap-2">
        <Link className="inline-flex min-h-9 items-center justify-center gap-2 rounded-lg border border-white/15 bg-white/10 px-3 py-1.5 text-sm font-semibold text-fuchsia-50 hover:bg-white/15" to={`/credit-cards/${card.id}`}>
          <Eye size={16} /> Detalhes
        </Link>
        <Link className="inline-flex min-h-9 items-center justify-center gap-2 rounded-lg border border-white/15 bg-white/10 px-3 py-1.5 text-sm font-semibold text-fuchsia-50 hover:bg-white/15" to={`/credit-cards/${card.id}/edit`}>
          <Pencil size={16} /> Editar
        </Link>
        {!card.archived ? (
          <Button className="min-h-9 px-3 py-1.5" onClick={() => onArchive(card)} type="button" variant="secondary">
            <Archive size={16} /> Arquivar
          </Button>
        ) : null}
        <Button className="min-h-9 border-rose-300/25 px-3 py-1.5 text-rose-200 hover:bg-rose-400/10" onClick={() => onDelete(card)} type="button" variant="secondary">
          <Trash2 size={16} /> Excluir
        </Button>
      </div>
    </article>
  );
}
