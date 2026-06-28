import { useState } from 'react';

import { Button } from '../../../shared/ui/Button';
import type { Account } from '../../accounts/types/accounts.types';
import type { MarkTransactionAsPaidPayload, Transaction } from '../types/transactions.types';
import { formatCurrency, realizedStatusFor } from '../utils/transaction-format';

type TransactionAction = 'cancel' | 'delete' | 'mark-as-paid';

type TransactionActionDialogProps = {
  accounts: Account[];
  action: TransactionAction | null;
  isLoading: boolean;
  onClose: () => void;
  onConfirm: (payload?: MarkTransactionAsPaidPayload) => void;
  transaction: Transaction | null;
};

export function TransactionActionDialog({ accounts, action, isLoading, onClose, onConfirm, transaction }: TransactionActionDialogProps) {
  const [formState, setFormState] = useState({ accountId: '', paidDate: '', transactionId: '' });

  if (!action || !transaction) {
    return null;
  }

  const currentFormState =
    formState.transactionId === transaction.id
      ? formState
      : {
          accountId: transaction.accountId ?? '',
          paidDate: transaction.transactionDate,
          transactionId: transaction.id,
        };
  const isMarkAsPaid = action === 'mark-as-paid';
  const realizedLabel = transaction.type === 'EXPENSE' ? 'paga' : 'recebida';
  const titles = {
    cancel: 'Cancelar transacao',
    delete: 'Excluir transacao',
    'mark-as-paid': `Marcar como ${realizedLabel}`,
  };
  const descriptions = {
    cancel: 'O backend reverte o impacto de saldo quando esta transacao ja estiver realizada.',
    delete: 'A exclusao remove a transacao e reverte o saldo quando houver impacto realizado.',
    'mark-as-paid': `A transacao mudara para ${realizedStatusFor(transaction.type).toLowerCase()} e o saldo da conta selecionada sera atualizado.`,
  };

  function confirm() {
    if (isMarkAsPaid) {
      onConfirm({ accountId: currentFormState.accountId || null, paidDate: currentFormState.paidDate || null });
      return;
    }

    onConfirm();
  }

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-slate-950/70 px-4 backdrop-blur-sm">
      <section className="app-panel w-full max-w-lg p-6">
        <p className="text-xs font-bold uppercase tracking-[0.18em] text-fuchsia-300">Transacoes</p>
        <h2 className="mt-3 font-serif text-2xl font-semibold text-[#f7ecff]">{titles[action]}</h2>
        <p className="mt-3 text-sm leading-6 text-[#c8a9d8]">
          {descriptions[action]} Lancamento: <strong className="text-[#f7ecff]">{transaction.description}</strong>, valor{' '}
          <strong className="text-[#f7ecff]">{formatCurrency(transaction.amount)}</strong>.
        </p>

        {isMarkAsPaid ? (
          <div className="mt-5 grid gap-4 sm:grid-cols-2">
            <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="markPaidAccountId">
              Conta
              <select
                className="mt-2 min-h-11 w-full rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20"
                id="markPaidAccountId"
                onChange={(event) => setFormState({ ...currentFormState, accountId: event.target.value })}
                value={currentFormState.accountId}
              >
                <option value="">Selecione uma conta</option>
                {accounts.map((account) => (
                  <option key={account.id} value={account.id}>
                    {account.name}
                  </option>
                ))}
              </select>
            </label>
            <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="markPaidDate">
              Data
              <input
                className="mt-2 min-h-11 w-full rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20"
                id="markPaidDate"
                onChange={(event) => setFormState({ ...currentFormState, paidDate: event.target.value })}
                type="date"
                value={currentFormState.paidDate}
              />
            </label>
          </div>
        ) : null}

        <div className="mt-6 flex flex-wrap justify-end gap-3">
          <Button disabled={isLoading} onClick={onClose} type="button" variant="secondary">
            Fechar
          </Button>
          <Button className={action === 'delete' ? 'bg-rose-600 hover:bg-rose-500' : ''} isLoading={isLoading} onClick={confirm} type="button">
            {isMarkAsPaid ? `Marcar ${realizedLabel}` : titles[action]}
          </Button>
        </div>
      </section>
    </div>
  );
}
