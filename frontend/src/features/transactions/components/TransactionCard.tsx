import { Ban, CalendarDays, CheckCircle2, Pencil, Trash2 } from 'lucide-react';
import { Link } from 'react-router';

import { Button } from '../../../shared/ui/Button';
import type { Account } from '../../accounts/types/accounts.types';
import type { Category } from '../../categories/types/categories.types';
import type { Transaction } from '../types/transactions.types';
import {
  formatCurrency,
  formatDate,
  signedAmount,
  transactionStatusLabels,
  transactionStatusTone,
  transactionTypeLabels,
} from '../utils/transaction-format';

type TransactionCardProps = {
  accounts: Account[];
  categories: Category[];
  onCancel: (transaction: Transaction) => void;
  onDelete: (transaction: Transaction) => void;
  onMarkAsPaid: (transaction: Transaction) => void;
  transaction: Transaction;
};

function findName(items: Array<{ id: string; name: string }>, id?: string | null) {
  if (!id) {
    return 'Nao vinculada';
  }

  return items.find((item) => item.id === id)?.name ?? `${id.slice(0, 8)}...`;
}

export function TransactionCard({ accounts, categories, onCancel, onDelete, onMarkAsPaid, transaction }: TransactionCardProps) {
  const value = signedAmount(transaction.type, transaction.amount);
  const valueColor = value < 0 ? 'text-rose-200' : 'text-emerald-200';
  const canMarkAsPaid = transaction.status === 'PENDING';
  const canCancel = transaction.status !== 'CANCELED';

  return (
    <article className="app-panel p-5">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <div className="flex flex-wrap items-center gap-2">
            <h2 className="font-serif text-xl font-semibold text-[#f7ecff]">{transaction.description}</h2>
            <span className={`rounded-full border px-2 py-1 text-xs font-bold ${transactionStatusTone[transaction.status]}`}>
              {transactionStatusLabels[transaction.status]}
            </span>
          </div>
          <p className="mt-1 text-sm text-[#c8a9d8]">{transactionTypeLabels[transaction.type]}</p>
        </div>

        <strong className={`font-serif text-3xl ${valueColor}`}>{formatCurrency(value)}</strong>
      </div>

      <div className="mt-5 grid gap-3 sm:grid-cols-3">
        <div className="rounded-lg border border-white/10 bg-white/10 p-3">
          <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]">Data</p>
          <p className="mt-2 flex items-center gap-2 text-sm font-semibold text-[#f7ecff]">
            <CalendarDays size={15} /> {formatDate(transaction.transactionDate)}
          </p>
        </div>
        <div className="rounded-lg border border-white/10 bg-white/10 p-3">
          <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]">Categoria</p>
          <p className="mt-2 text-sm font-semibold text-[#f7ecff]">{findName(categories, transaction.categoryId)}</p>
        </div>
        <div className="rounded-lg border border-white/10 bg-white/10 p-3">
          <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]">Conta</p>
          <p className="mt-2 text-sm font-semibold text-[#f7ecff]">{findName(accounts, transaction.accountId)}</p>
        </div>
      </div>

      {transaction.notes ? <p className="mt-4 rounded-lg border border-white/10 bg-white/10 px-3 py-2 text-sm text-[#c8a9d8]">{transaction.notes}</p> : null}

      <div className="mt-5 flex flex-wrap gap-2 border-t border-white/10 pt-4">
        <Link className="inline-flex min-h-9 items-center justify-center gap-2 rounded-lg border border-white/15 bg-white/10 px-3 py-1.5 text-sm font-semibold text-fuchsia-50 hover:bg-white/15" to={`/transactions/${transaction.id}/edit`}>
          <Pencil size={16} /> Editar
        </Link>
        {canMarkAsPaid ? (
          <Button className="min-h-9 px-3 py-1.5" onClick={() => onMarkAsPaid(transaction)} type="button">
            <CheckCircle2 size={16} /> {transaction.type === 'EXPENSE' ? 'Marcar paga' : 'Marcar recebida'}
          </Button>
        ) : null}
        {canCancel ? (
          <Button className="min-h-9 px-3 py-1.5" onClick={() => onCancel(transaction)} type="button" variant="secondary">
            <Ban size={16} /> Cancelar
          </Button>
        ) : null}
        <Button className="min-h-9 border-rose-300/25 px-3 py-1.5 text-rose-200 hover:bg-rose-400/10" onClick={() => onDelete(transaction)} type="button" variant="secondary">
          <Trash2 size={16} /> Excluir
        </Button>
      </div>
    </article>
  );
}
