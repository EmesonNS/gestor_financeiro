import { CalendarClock, Eye, ReceiptText, WalletCards } from 'lucide-react';
import { Link } from 'react-router';

import { Button } from '../../../shared/ui/Button';
import type { Invoice } from '../types/invoices.types';
import { canPayInvoice, dueDateCopy, formatCurrency, formatDate, formatMonthYear, invoiceStatusLabels, invoiceStatusTone } from '../utils/invoice-format';

type InvoiceCardProps = {
  invoice: Invoice;
  onPay: (invoice: Invoice) => void;
};

export function InvoiceCard({ invoice, onPay }: InvoiceCardProps) {
  return (
    <article className="app-panel p-5">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div className="flex gap-3">
          <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-lg border border-white/10 bg-fuchsia-400/15 text-fuchsia-200">
            <ReceiptText size={22} />
          </span>
          <div>
            <div className="flex flex-wrap items-center gap-2">
              <h2 className="font-serif text-xl font-semibold capitalize text-[#f7ecff]">{formatMonthYear(invoice.referenceMonth, invoice.referenceYear)}</h2>
              <span className={`rounded-full border px-2 py-1 text-xs font-bold ${invoiceStatusTone[invoice.status]}`}>{invoiceStatusLabels[invoice.status]}</span>
            </div>
            <p className="mt-1 flex items-center gap-2 text-sm text-[#c8a9d8]">
              <CalendarClock size={16} /> {dueDateCopy(invoice.dueDate, invoice.status)}
            </p>
          </div>
        </div>

        <strong className="font-serif text-3xl text-fuchsia-100">{formatCurrency(invoice.totalAmount)}</strong>
      </div>

      <div className="mt-5 grid gap-3 sm:grid-cols-2">
        <div className="rounded-lg border border-white/10 bg-white/10 p-3">
          <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]">Fechamento</p>
          <p className="mt-2 text-sm font-semibold text-[#f7ecff]">{formatDate(invoice.closingDate)}</p>
        </div>
        <div className="rounded-lg border border-white/10 bg-white/10 p-3">
          <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]">Vencimento</p>
          <p className="mt-2 text-sm font-semibold text-[#f7ecff]">{formatDate(invoice.dueDate)}</p>
        </div>
      </div>

      {invoice.status === 'PAID' ? (
        <p className="mt-4 rounded-lg border border-emerald-300/20 bg-emerald-400/10 px-3 py-2 text-sm text-emerald-100">Paga em {formatDate(invoice.paidAt)}.</p>
      ) : null}

      <div className="mt-5 flex flex-wrap gap-2 border-t border-white/10 pt-4">
        <Link className="inline-flex min-h-9 items-center justify-center gap-2 rounded-lg border border-white/15 bg-white/10 px-3 py-1.5 text-sm font-semibold text-fuchsia-50 hover:bg-white/15" to={`/invoices/${invoice.id}`}>
          <Eye size={16} /> Detalhes
        </Link>
        {canPayInvoice(invoice.status) ? (
          <Button className="min-h-9 px-3 py-1.5" onClick={() => onPay(invoice)} type="button">
            <WalletCards size={16} /> Pagar fatura
          </Button>
        ) : null}
      </div>
    </article>
  );
}
