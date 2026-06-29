import { CreditCard } from 'lucide-react';

import type { CreditCard as CreditCardType } from '../types/credit-cards.types';
import { formatCurrency, formatPercent, limitUsagePercent, statementDayCopy } from '../utils/credit-card-format';

type CardLimitSummaryProps = {
  card: CreditCardType;
};

export function CardLimitSummary({ card }: CardLimitSummaryProps) {
  const usagePercent = limitUsagePercent(card.usedLimit, card.limitAmount);

  return (
    <section className="app-panel p-6">
      <div className="grid gap-6 lg:grid-cols-[1fr_1.1fr]">
        <div className="rounded-xl border border-white/10 bg-gradient-to-br from-[#4b1f64] via-[#2a1638] to-[#160722] p-6 shadow-2xl shadow-fuchsia-950/30">
          <div className="flex items-start justify-between gap-4">
            <div>
              <p className="text-xs font-bold uppercase tracking-[0.2em] text-fuchsia-200">Cartao de credito</p>
              <h2 className="mt-4 font-serif text-4xl font-semibold text-[#f7ecff]">{card.name}</h2>
            </div>
            <span className="flex h-12 w-12 shrink-0 items-center justify-center rounded-lg border border-white/15 bg-white/10 text-fuchsia-100">
              <CreditCard size={24} />
            </span>
          </div>
          <p className="mt-12 text-sm text-[#c8a9d8]">{statementDayCopy(card.closingDay, card.dueDay)}</p>
        </div>

        <div>
          <p className="app-eyebrow">Limite</p>
          <h3 className="mt-2 font-serif text-3xl font-semibold text-[#f7ecff]">{formatCurrency(card.availableLimit)} disponivel</h3>
          <div className="mt-6 h-4 overflow-hidden rounded-full border border-white/10 bg-[#24112f]">
            <div className="h-full rounded-full bg-gradient-to-r from-emerald-400 via-fuchsia-500 to-[#ff6bb5]" style={{ width: `${usagePercent}%` }} />
          </div>
          <p className="mt-3 text-sm text-[#c8a9d8]">{formatPercent(usagePercent)} do limite usado.</p>

          <div className="mt-6 grid gap-3 sm:grid-cols-3">
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
        </div>
      </div>
    </section>
  );
}
