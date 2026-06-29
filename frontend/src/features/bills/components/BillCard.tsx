import { CalendarClock, CheckCircle2, Pencil, Trash2 } from 'lucide-react';
import { Link } from 'react-router';

import { Button } from '../../../shared/ui/Button';
import type { Account } from '../../accounts/types/accounts.types';
import type { Category } from '../../categories/types/categories.types';
import type { Bill } from '../types/bills.types';
import { billStatusLabels, billStatusTone, dueCopy, formatCurrency, formatDate, visualBillStatus } from '../utils/bill-format';

type BillCardProps = {
  accounts: Account[];
  bill: Bill;
  categories: Category[];
  onDelete: (bill: Bill) => void;
  onPay: (bill: Bill) => void;
};

function findName(items: Array<{ id: string; name: string }>, id?: string | null) {
  if (!id) {
    return 'Nao definida';
  }

  return items.find((item) => item.id === id)?.name ?? `${id.slice(0, 8)}...`;
}

export function BillCard({ accounts, bill, categories, onDelete, onPay }: BillCardProps) {
  const status = visualBillStatus(bill);
  const canChange = bill.status !== 'PAID';

  return (
    <article className="app-panel p-5">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <div className="flex flex-wrap items-center gap-2">
            <h2 className="font-serif text-xl font-semibold text-[#f7ecff]">{bill.description}</h2>
            <span className={`rounded-full border px-2 py-1 text-xs font-bold ${billStatusTone[status]}`}>{billStatusLabels[status]}</span>
          </div>
          <p className="mt-1 flex items-center gap-2 text-sm text-[#c8a9d8]">
            <CalendarClock size={16} /> {dueCopy(bill)}
          </p>
        </div>

        <strong className="font-serif text-3xl text-[#f7ecff]">{formatCurrency(bill.amount)}</strong>
      </div>

      <div className="mt-5 grid gap-3 sm:grid-cols-3">
        <div className="rounded-lg border border-white/10 bg-white/10 p-3">
          <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]">Vencimento</p>
          <p className="mt-2 text-sm font-semibold text-[#f7ecff]">{formatDate(bill.dueDate)}</p>
        </div>
        <div className="rounded-lg border border-white/10 bg-white/10 p-3">
          <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]">Categoria</p>
          <p className="mt-2 text-sm font-semibold text-[#f7ecff]">{findName(categories, bill.categoryId)}</p>
        </div>
        <div className="rounded-lg border border-white/10 bg-white/10 p-3">
          <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]">Conta</p>
          <p className="mt-2 text-sm font-semibold text-[#f7ecff]">{findName(accounts, bill.accountId)}</p>
        </div>
      </div>

      <div className="mt-5 flex flex-wrap gap-2 border-t border-white/10 pt-4">
        {canChange ? (
          <>
            <Button className="min-h-9 px-3 py-1.5" onClick={() => onPay(bill)} type="button">
              <CheckCircle2 size={16} /> Pagar
            </Button>
            <Link className="inline-flex min-h-9 items-center justify-center gap-2 rounded-lg border border-white/15 bg-white/10 px-3 py-1.5 text-sm font-semibold text-fuchsia-50 hover:bg-white/15" to={`/bills/${bill.id}/edit`}>
              <Pencil size={16} /> Editar
            </Link>
            <Button className="min-h-9 border-rose-300/25 px-3 py-1.5 text-rose-200 hover:bg-rose-400/10" onClick={() => onDelete(bill)} type="button" variant="secondary">
              <Trash2 size={16} /> Excluir
            </Button>
          </>
        ) : (
          <span className="rounded-lg border border-emerald-300/20 bg-emerald-400/10 px-3 py-2 text-sm font-semibold text-emerald-100">
            Transacao vinculada {bill.transactionId ? bill.transactionId.slice(0, 8) : ''}
          </span>
        )}
      </div>
    </article>
  );
}
