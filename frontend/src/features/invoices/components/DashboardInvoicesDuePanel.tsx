import { CalendarClock, ReceiptText } from 'lucide-react';
import { Link } from 'react-router';

import type { CreditCard } from '../../credit-cards/types/credit-cards.types';
import type { Invoice } from '../types/invoices.types';
import { formatCurrency, formatDate } from '../utils/invoice-format';

type DashboardInvoicesDuePanelProps = {
  cards: CreditCard[];
  invoices: Invoice[];
  isLoading: boolean;
};

function daysUntil(date: string) {
  const current = new Date();
  const today = new Date(current.getFullYear(), current.getMonth(), current.getDate()).getTime();
  const target = new Date(`${date}T00:00:00`).getTime();
  return Math.round((target - today) / 86_400_000);
}

function dueCopy(date: string) {
  const days = daysUntil(date);

  if (days < 0) {
    return `${Math.abs(days)} dia${Math.abs(days) === 1 ? '' : 's'} em atraso`;
  }

  if (days === 0) {
    return 'Vence hoje';
  }

  return `Vence em ${days} dia${days === 1 ? '' : 's'}`;
}

export function DashboardInvoicesDuePanel({ cards, invoices, isLoading }: DashboardInvoicesDuePanelProps) {
  const dueInvoices = invoices
    .filter((invoice) => invoice.status !== 'PAID')
    .filter((invoice) => daysUntil(invoice.dueDate) <= 7)
    .sort((first, second) => new Date(`${first.dueDate}T00:00:00`).getTime() - new Date(`${second.dueDate}T00:00:00`).getTime());
  const dueTotal = dueInvoices.reduce((total, invoice) => total + Number(invoice.totalAmount), 0);

  return (
    <section className="app-panel p-5">
      <div className="flex items-center justify-between gap-4">
        <div>
          <p className="app-eyebrow">Faturas</p>
          <h2 className="mt-2 font-serif text-2xl font-semibold text-[#f7ecff]">Vencimentos proximos</h2>
        </div>
        <span className="flex h-11 w-11 items-center justify-center rounded-lg border border-white/10 bg-fuchsia-400/15 text-fuchsia-200">
          <ReceiptText size={21} />
        </span>
      </div>

      {isLoading ? <p className="mt-6 text-sm text-[#c8a9d8]">Carregando faturas...</p> : null}

      {!isLoading ? (
        <div className="mt-5 rounded-lg border border-white/10 bg-white/10 p-4">
          <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]">Total em alerta</p>
          <strong className="mt-2 block font-serif text-3xl text-[#f7ecff]">{formatCurrency(dueTotal)}</strong>
          <p className="mt-1 text-sm text-[#c8a9d8]">{dueInvoices.length} fatura(s) vencidas ou vencendo em ate 7 dias.</p>
        </div>
      ) : null}

      {!isLoading && !dueInvoices.length ? <p className="mt-6 rounded-lg border border-dashed border-white/20 bg-white/10 p-4 text-sm text-[#c8a9d8]">Nenhuma fatura vencendo nos proximos 7 dias.</p> : null}

      <div className="mt-4 space-y-3">
        {dueInvoices.slice(0, 4).map((invoice) => {
          const card = cards.find((item) => item.id === invoice.creditCardId);

          return (
            <Link className="block rounded-lg border border-white/10 bg-white/10 p-3 hover:bg-white/15" key={invoice.id} to={`/invoices/${invoice.id}`}>
              <div className="flex items-start justify-between gap-3">
                <div>
                  <p className="text-sm font-semibold text-[#f7ecff]">{card?.name ?? 'Cartao'}</p>
                  <p className="mt-1 flex items-center gap-2 text-xs text-[#c8a9d8]">
                    <CalendarClock size={14} /> {dueCopy(invoice.dueDate)} · {formatDate(invoice.dueDate)}
                  </p>
                </div>
                <strong className="text-sm text-fuchsia-100">{formatCurrency(invoice.totalAmount)}</strong>
              </div>
            </Link>
          );
        })}
      </div>
    </section>
  );
}
