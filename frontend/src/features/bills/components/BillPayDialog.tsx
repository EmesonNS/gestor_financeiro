import { useState } from 'react';

import { Button } from '../../../shared/ui/Button';
import type { Account } from '../../accounts/types/accounts.types';
import { hasSufficientBalance, insufficientBalanceMessage } from '../../accounts/utils/balance-validation';
import { payBillSchema, type PayBillData } from '../schemas/bill.schemas';
import type { Bill } from '../types/bills.types';
import { formatCurrency, formatDate } from '../utils/bill-format';

type BillPayDialogProps = {
  accounts: Account[];
  bill: Bill | null;
  isLoading: boolean;
  onClose: () => void;
  onConfirm: (data: PayBillData) => void;
};

export function BillPayDialog({ accounts, bill, isLoading, onClose, onConfirm }: BillPayDialogProps) {
  const [accountId, setAccountId] = useState('');
  const [paidAt, setPaidAt] = useState(new Date().toISOString().slice(0, 10));
  const [error, setError] = useState<string | null>(null);

  if (!bill) {
    return null;
  }
  const activeBill = bill;

  function confirm() {
    const parsed = payBillSchema.safeParse({
      accountId: accountId || activeBill.accountId || '',
      paidAt,
    });

    if (!parsed.success) {
      setError(parsed.error.issues[0]?.message ?? 'Confira os dados do pagamento.');
      return;
    }

    const selectedAccount = accounts.find((account) => account.id === parsed.data.accountId);
    if (selectedAccount && !hasSufficientBalance(selectedAccount, Number(activeBill.amount))) {
      setError(insufficientBalanceMessage(selectedAccount));
      return;
    }

    setError(null);
    onConfirm(parsed.data);
  }

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-slate-950/70 px-4 backdrop-blur-sm">
      <section className="app-panel w-full max-w-lg p-6">
        <p className="text-xs font-bold uppercase tracking-[0.18em] text-fuchsia-300">Contas a pagar</p>
        <h2 className="mt-3 font-serif text-2xl font-semibold text-[#f7ecff]">Pagar conta</h2>
        <p className="mt-3 text-sm leading-6 text-[#c8a9d8]">
          O pagamento cria uma despesa paga e reduz o saldo da conta selecionada. Conta:{' '}
          <strong className="text-[#f7ecff]">{activeBill.description}</strong>, vencimento <strong className="text-[#f7ecff]">{formatDate(activeBill.dueDate)}</strong>, valor{' '}
          <strong className="text-[#f7ecff]">{formatCurrency(activeBill.amount)}</strong>.
        </p>

        <div className="mt-5 grid gap-4 sm:grid-cols-2">
          <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="payBillAccountId">
            Conta
            <select
              className="mt-2 min-h-11 w-full rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20"
              id="payBillAccountId"
              onChange={(event) => setAccountId(event.target.value)}
              value={accountId || activeBill.accountId || ''}
            >
              <option value="">Selecione uma conta</option>
                {accounts.map((account) => (
                  <option key={account.id} value={account.id}>
                    {account.name} - saldo {formatCurrency(account.currentBalance)}
                  </option>
                ))}
            </select>
          </label>

          <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="payBillPaidAt">
            Data do pagamento
            <input
              className="mt-2 min-h-11 w-full rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20"
              id="payBillPaidAt"
              onChange={(event) => setPaidAt(event.target.value)}
              type="date"
              value={paidAt}
            />
          </label>
        </div>

        {error ? <p className="mt-4 rounded-lg border border-rose-300/25 bg-rose-400/10 px-3 py-2 text-sm font-medium text-rose-200">{error}</p> : null}

        <div className="mt-6 flex flex-wrap justify-end gap-3">
          <Button disabled={isLoading} onClick={onClose} type="button" variant="secondary">
            Fechar
          </Button>
          <Button isLoading={isLoading} onClick={confirm} type="button">
            Confirmar pagamento
          </Button>
        </div>
      </section>
    </div>
  );
}
