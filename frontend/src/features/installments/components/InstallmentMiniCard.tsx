import { CalendarClock, ReceiptText } from 'lucide-react';
import { Link } from 'react-router';

import type { Installment } from '../types/installments.types';
import { formatCurrency, formatMonthYear, installmentLabel, installmentStatusLabels, installmentStatusTone } from '../utils/installment-format';

type InstallmentMiniCardProps = {
  installment: Installment;
};

export function InstallmentMiniCard({ installment }: InstallmentMiniCardProps) {
  return (
    <article className="rounded-lg border border-white/10 bg-white/10 p-4">
      <div className="flex items-start justify-between gap-3">
        <div className="flex gap-3">
          <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg border border-white/10 bg-fuchsia-400/15 text-fuchsia-200">
            <ReceiptText size={18} />
          </span>
          <div>
            <p className="font-serif text-xl font-semibold text-[#f7ecff]">{installmentLabel(installment.installmentNumber, installment.totalInstallments)}</p>
            <p className="mt-1 flex items-center gap-2 text-sm text-[#c8a9d8]">
              <CalendarClock size={15} /> {formatMonthYear(installment.competenceMonth, installment.competenceYear)}
            </p>
          </div>
        </div>
        <span className={`rounded-full border px-2 py-1 text-xs font-bold ${installmentStatusTone[installment.status]}`}>{installmentStatusLabels[installment.status]}</span>
      </div>
      <div className="mt-4 flex items-center justify-between gap-3">
        <strong className="font-serif text-2xl text-fuchsia-100">{formatCurrency(installment.amount)}</strong>
        <Link className="text-sm font-semibold text-fuchsia-100 hover:text-white" to={`/invoices/${installment.invoiceId}`}>
          Ver fatura
        </Link>
      </div>
    </article>
  );
}
