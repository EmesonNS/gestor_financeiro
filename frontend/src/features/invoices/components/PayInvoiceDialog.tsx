import { useState } from 'react';

import { Button } from '../../../shared/ui/Button';
import type { Account } from '../../accounts/types/accounts.types';
import { hasSufficientBalance, insufficientBalanceMessage } from '../../accounts/utils/balance-validation';
import { payInvoiceSchema, type PayInvoiceData } from '../schemas/invoice.schemas';
import type { Invoice } from '../types/invoices.types';
import { formatCurrency, formatDate, formatMonthYear } from '../utils/invoice-format';

type PayInvoiceDialogProps = {
  accounts: Account[];
  invoice: Invoice | null;
  isLoading: boolean;
  onClose: () => void;
  onConfirm: (data: PayInvoiceData) => void;
};

export function PayInvoiceDialog({ accounts, invoice, isLoading, onClose, onConfirm }: PayInvoiceDialogProps) {
  const [formState, setFormState] = useState({
    invoiceId: '',
    paidAt: new Date().toISOString().slice(0, 10),
    paymentAccountId: '',
  });
  const [error, setError] = useState<string | null>(null);

  if (!invoice) {
    return null;
  }
  const activeInvoice = invoice;

  const activeState = formState.invoiceId === activeInvoice.id ? formState : { invoiceId: activeInvoice.id, paidAt: new Date().toISOString().slice(0, 10), paymentAccountId: '' };

  function updateField(field: 'paidAt' | 'paymentAccountId', value: string) {
    setFormState({
      ...activeState,
      [field]: value,
    });
  }

  function confirm() {
    const parsed = payInvoiceSchema.safeParse({
      paidAt: activeState.paidAt,
      paymentAccountId: activeState.paymentAccountId,
    });

    if (!parsed.success) {
      setError(parsed.error.issues[0]?.message ?? 'Confira os dados do pagamento.');
      return;
    }

    const selectedAccount = accounts.find((account) => account.id === parsed.data.paymentAccountId);
    if (selectedAccount && !hasSufficientBalance(selectedAccount, Number(activeInvoice.totalAmount))) {
      setError(insufficientBalanceMessage(selectedAccount));
      return;
    }

    setError(null);
    onConfirm(parsed.data);
  }

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-slate-950/70 px-4 backdrop-blur-sm">
      <section className="app-panel w-full max-w-lg p-6">
        <p className="text-xs font-bold uppercase tracking-[0.18em] text-fuchsia-300">Faturas de cartao</p>
        <h2 className="mt-3 font-serif text-2xl font-semibold text-[#f7ecff]">Pagar fatura</h2>
        <p className="mt-3 text-sm leading-6 text-[#c8a9d8]">
          O pagamento reduz o saldo da conta selecionada apenas agora. Fatura{' '}
          <strong className="capitalize text-[#f7ecff]">{formatMonthYear(activeInvoice.referenceMonth, activeInvoice.referenceYear)}</strong>, vencimento{' '}
          <strong className="text-[#f7ecff]">{formatDate(activeInvoice.dueDate)}</strong>, valor <strong className="text-[#f7ecff]">{formatCurrency(activeInvoice.totalAmount)}</strong>.
        </p>

        <div className="mt-5 grid gap-4 sm:grid-cols-2">
          <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="payInvoiceAccountId">
            Conta
            <select
              className="mt-2 min-h-11 w-full rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20"
              id="payInvoiceAccountId"
              onChange={(event) => updateField('paymentAccountId', event.target.value)}
              value={activeState.paymentAccountId}
            >
              <option value="">Selecione uma conta</option>
              {accounts.map((account) => (
                <option key={account.id} value={account.id}>
                  {account.name} - saldo {formatCurrency(account.currentBalance)}
                </option>
              ))}
            </select>
          </label>

          <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="payInvoicePaidAt">
            Data do pagamento
            <input
              className="mt-2 min-h-11 w-full rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20"
              id="payInvoicePaidAt"
              onChange={(event) => updateField('paidAt', event.target.value)}
              type="date"
              value={activeState.paidAt}
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
