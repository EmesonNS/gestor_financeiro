import { CalendarClock, Eye, Pencil, ReceiptText, Trash2 } from 'lucide-react';
import { Link } from 'react-router';

import { Button } from '../../../shared/ui/Button';
import type { Category } from '../../categories/types/categories.types';
import type { CardPurchase } from '../types/installments.types';
import { formatCurrency, formatDate, hasPaidInstallment, purchaseStatusLabels, purchaseStatusTone } from '../utils/installment-format';

type CardPurchaseCardProps = {
  categories: Category[];
  onDelete: (purchase: CardPurchase) => void;
  purchase: CardPurchase;
};

export function CardPurchaseCard({ categories, onDelete, purchase }: CardPurchaseCardProps) {
  const category = categories.find((item) => item.id === purchase.categoryId);
  const hasPaid = hasPaidInstallment(purchase.installments);

  return (
    <article className="app-panel p-5">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div className="flex gap-3">
          <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-lg border border-white/10 bg-fuchsia-400/15 text-fuchsia-200">
            <ReceiptText size={22} />
          </span>
          <div>
            <div className="flex flex-wrap items-center gap-2">
              <h2 className="font-serif text-xl font-semibold text-[#f7ecff]">{purchase.description}</h2>
              <span className={`rounded-full border px-2 py-1 text-xs font-bold ${purchaseStatusTone[purchase.status]}`}>{purchaseStatusLabels[purchase.status]}</span>
              {hasPaid ? <span className="rounded-full border border-amber-300/25 bg-amber-400/10 px-2 py-1 text-xs font-bold text-amber-100">Possui fatura paga</span> : null}
            </div>
            <p className="mt-1 flex items-center gap-2 text-sm text-[#c8a9d8]">
              <CalendarClock size={16} /> Compra em {formatDate(purchase.purchaseDate)}
            </p>
          </div>
        </div>
        <strong className="font-serif text-3xl text-fuchsia-100">{formatCurrency(purchase.totalAmount)}</strong>
      </div>

      <div className="mt-5 grid gap-3 sm:grid-cols-3">
        <div className="rounded-lg border border-white/10 bg-white/10 p-3">
          <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]">Parcelas</p>
          <p className="mt-2 text-sm font-semibold text-[#f7ecff]">{purchase.installmentCount}x</p>
        </div>
        <div className="rounded-lg border border-white/10 bg-white/10 p-3">
          <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]">Categoria</p>
          <p className="mt-2 text-sm font-semibold text-[#f7ecff]">{category?.name ?? 'Categoria'}</p>
        </div>
        <div className="rounded-lg border border-white/10 bg-white/10 p-3">
          <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]">Valor medio</p>
          <p className="mt-2 text-sm font-semibold text-[#f7ecff]">{formatCurrency(Number(purchase.totalAmount) / purchase.installmentCount)}</p>
        </div>
      </div>

      {purchase.notes ? <p className="mt-4 rounded-lg border border-white/10 bg-white/10 px-3 py-2 text-sm text-[#c8a9d8]">{purchase.notes}</p> : null}

      <div className="mt-5 flex flex-wrap gap-2 border-t border-white/10 pt-4">
        <Link className="inline-flex min-h-9 items-center justify-center gap-2 rounded-lg border border-white/15 bg-white/10 px-3 py-1.5 text-sm font-semibold text-fuchsia-50 hover:bg-white/15" to={`/card-purchases/${purchase.id}`}>
          <Eye size={16} /> Detalhes
        </Link>
        <Link className="inline-flex min-h-9 items-center justify-center gap-2 rounded-lg border border-white/15 bg-white/10 px-3 py-1.5 text-sm font-semibold text-fuchsia-50 hover:bg-white/15" to={`/card-purchases/${purchase.id}/edit`}>
          <Pencil size={16} /> Editar
        </Link>
        <Button className="min-h-9 border-rose-300/25 px-3 py-1.5 text-rose-200 hover:bg-rose-400/10" onClick={() => onDelete(purchase)} type="button" variant="secondary">
          <Trash2 size={16} /> Excluir
        </Button>
      </div>
    </article>
  );
}
