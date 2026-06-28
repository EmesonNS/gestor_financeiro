import { Archive, CalendarClock, Pencil, Trash2, WalletCards } from 'lucide-react';
import { Link } from 'react-router';

import { Button } from '../../../shared/ui/Button';
import type { Account } from '../types/accounts.types';
import { accountTypeLabels, formatCurrency, formatDateTime } from '../utils/account-format';

type AccountCardProps = {
  account: Account;
  onArchive: (account: Account) => void;
  onDelete: (account: Account) => void;
};

export function AccountCard({ account, onArchive, onDelete }: AccountCardProps) {
  return (
    <article className="app-panel p-5">
      <div className="flex items-start justify-between gap-4">
        <div className="flex gap-3">
          <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-lg border border-white/10 bg-fuchsia-400/15 text-fuchsia-200">
            <WalletCards size={22} />
          </span>
          <div>
            <div className="flex flex-wrap items-center gap-2">
              <h2 className="font-serif text-xl font-semibold text-[#f7ecff]">{account.name}</h2>
              {account.archived ? <span className="rounded-full border border-slate-300/20 bg-slate-400/10 px-2 py-1 text-xs font-bold text-slate-200">Arquivada</span> : null}
            </div>
            <p className="mt-1 text-sm text-[#c8a9d8]">{accountTypeLabels[account.type]}</p>
          </div>
        </div>

        <Link className="rounded-lg border border-white/15 bg-white/10 px-3 py-2 text-sm font-semibold text-fuchsia-50 hover:bg-white/15" to={`/accounts/${account.id}/edit`}>
          <Pencil size={15} />
          <span className="sr-only">Editar</span>
        </Link>
      </div>

      <div className="mt-6 grid gap-4 sm:grid-cols-2">
        <div>
          <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]">Saldo atual</p>
          <strong className="mt-2 block font-serif text-3xl text-[#f7ecff]">{formatCurrency(account.currentBalance)}</strong>
        </div>
        <div>
          <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]">Saldo inicial</p>
          <strong className="mt-2 block font-serif text-2xl text-[#f7ecff]">{formatCurrency(account.initialBalance)}</strong>
        </div>
      </div>

      <div className="mt-5 flex items-center gap-2 border-t border-white/10 pt-4 text-sm text-[#c8a9d8]">
        <CalendarClock size={16} /> Atualizada em {formatDateTime(account.updatedAt)}
      </div>

      <div className="mt-5 flex flex-wrap gap-2">
        {!account.archived ? (
          <Button className="min-h-9 px-3 py-1.5" onClick={() => onArchive(account)} type="button" variant="secondary">
            <Archive size={16} /> Arquivar
          </Button>
        ) : null}
        <Button className="min-h-9 border-rose-300/25 px-3 py-1.5 text-rose-200 hover:bg-rose-400/10" onClick={() => onDelete(account)} type="button" variant="secondary">
          <Trash2 size={16} /> Excluir
        </Button>
      </div>
    </article>
  );
}
