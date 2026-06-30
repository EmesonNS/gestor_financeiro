import { CalendarClock, ReceiptText, WalletCards } from 'lucide-react';

import { Button } from '../../../shared/ui/Button';
import type { Invoice } from '../types/invoices.types';
import { canPayInvoice, dueDateCopy, formatCurrency, formatDate, formatMonthYear, invoiceStatusLabels, invoiceStatusTone } from '../utils/invoice-format';

type InvoiceSummaryProps = {
  invoice: Invoice;
  onPay?: (invoice: Invoice) => void;
};

export function InvoiceSummary({ invoice, onPay }: InvoiceSummaryProps) {
  return (
    <section className="app-panel p-6">
      <div className="grid gap-6 lg:grid-cols-[1fr_1.1fr]">
        <div className="rounded-xl border border-white/10 bg-gradient-to-br from-[#4b1f64] via-[#2a1638] to-[#160722] p-6 shadow-2xl shadow-fuchsia-950/30">
          <div className="flex items-start justify-between gap-4">
            <div>
              <p className="text-xs font-bold uppercase tracking-[0.2em] text-fuchsia-200">Fatura</p>
              <h2 className="mt-4 font-serif text-4xl font-semibold capitalize text-[#f7ecff]">{formatMonthYear(invoice.referenceMonth, invoice.referenceYear)}</h2>
            </div>
            <span className="flex h-12 w-12 shrink-0 items-center justify-center rounded-lg border border-white/15 bg-white/10 text-fuchsia-100">
              <ReceiptText size={24} />
            </span>
          </div>
          <p className="mt-12 flex items-center gap-2 text-sm text-[#c8a9d8]">
            <CalendarClock size={16} /> {dueDateCopy(invoice.dueDate, invoice.status)}
          </p>
        </div>

        <div>
          <div className="flex flex-wrap items-start justify-between gap-3">
            <div>
              <p className="app-eyebrow">Total da fatura</p>
              <h3 className="mt-2 font-serif text-4xl font-semibold text-[#f7ecff]">{formatCurrency(invoice.totalAmount)}</h3>
            </div>
            <span className={`rounded-full border px-2 py-1 text-xs font-bold ${invoiceStatusTone[invoice.status]}`}>{invoiceStatusLabels[invoice.status]}</span>
          </div>

          <div className="mt-6 grid gap-3 sm:grid-cols-3">
            <div className="rounded-lg border border-white/10 bg-white/10 p-3">
              <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]">Fechamento</p>
              <p className="mt-2 text-sm font-semibold text-[#f7ecff]">{formatDate(invoice.closingDate)}</p>
            </div>
            <div className="rounded-lg border border-white/10 bg-white/10 p-3">
              <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]">Vencimento</p>
              <p className="mt-2 text-sm font-semibold text-[#f7ecff]">{formatDate(invoice.dueDate)}</p>
            </div>
            <div className="rounded-lg border border-white/10 bg-white/10 p-3">
              <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]">Pagamento</p>
              <p className="mt-2 text-sm font-semibold text-[#f7ecff]">{invoice.paidAt ? formatDate(invoice.paidAt) : 'Pendente'}</p>
            </div>
          </div>

          {onPay && canPayInvoice(invoice.status) ? (
            <Button className="mt-6" onClick={() => onPay(invoice)} type="button">
              <WalletCards size={17} /> Pagar fatura
            </Button>
          ) : null}
        </div>
      </div>
    </section>
  );
}
