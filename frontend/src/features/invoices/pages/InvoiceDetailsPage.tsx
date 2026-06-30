import { AxiosError } from 'axios';
import { ArrowLeft, ReceiptText } from 'lucide-react';
import { useState } from 'react';
import { Link, useParams } from 'react-router';

import { apiErrorMessage } from '../../../shared/lib/api-error';
import type { ApiErrorResponse } from '../../auth/types/auth.types';
import { useAccounts } from '../../accounts/hooks/useAccounts';
import { InvoiceSummary } from '../components/InvoiceSummary';
import { PayInvoiceDialog } from '../components/PayInvoiceDialog';
import { useInvoice, usePayInvoice } from '../hooks/useInvoices';
import type { Invoice } from '../types/invoices.types';

export function InvoiceDetailsPage() {
  const { invoiceId } = useParams();
  const invoiceQuery = useInvoice(invoiceId);
  const accountsQuery = useAccounts({ archived: false, page: 0 });
  const payMutation = usePayInvoice();
  const [invoiceToPay, setInvoiceToPay] = useState<Invoice | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const invoice = invoiceQuery.data;

  async function confirmPay(data: { paidAt: string; paymentAccountId: string }) {
    if (!invoiceToPay) {
      return;
    }

    setActionError(null);

    try {
      await payMutation.mutateAsync({
        invoiceId: invoiceToPay.id,
        payload: data,
      });
      setInvoiceToPay(null);
    } catch (error) {
      const fallback = error instanceof AxiosError ? (error.response?.data as ApiErrorResponse | undefined)?.message : undefined;
      setActionError(apiErrorMessage(error, fallback ?? 'Nao foi possivel pagar esta fatura.'));
    }
  }

  if (invoiceQuery.isLoading) {
    return <div className="app-panel-muted p-8 text-center text-sm font-medium text-fuchsia-50">Carregando fatura...</div>;
  }

  if (invoiceQuery.isError || !invoice) {
    return (
      <section className="rounded-lg border border-rose-300/25 bg-rose-400/10 p-6">
        <h1 className="font-serif text-2xl font-semibold text-rose-100">Fatura nao encontrada</h1>
        <p className="mt-2 text-sm text-rose-200">Nao foi possivel carregar esta fatura.</p>
        <Link className="mt-4 inline-flex font-semibold text-rose-100" to="/credit-cards">
          Voltar para cartoes
        </Link>
      </section>
    );
  }

  return (
    <section className="space-y-6">
      <Link className="inline-flex items-center gap-2 text-sm font-semibold text-fuchsia-100 hover:text-white" to={`/credit-cards/${invoice.creditCardId}/invoices`}>
        <ArrowLeft size={16} /> Voltar para faturas
      </Link>

      <div className="app-page-hero">
        <div className="admin-flow-line" aria-hidden="true" />
        <div className="relative max-w-3xl">
          <p className="app-eyebrow">Detalhes da fatura</p>
          <h1 className="app-hero-title mt-4">Conferencia da fatura</h1>
          <p className="app-hero-copy mt-4 max-w-xl">Confira competencia, datas e pagamento registrado para esta fatura.</p>
        </div>
      </div>

      {actionError ? <p className="rounded-lg border border-rose-300/25 bg-rose-400/10 px-4 py-3 text-sm font-medium text-rose-200">{actionError}</p> : null}

      <InvoiceSummary invoice={invoice} onPay={setInvoiceToPay} />

      <section className="app-panel-muted p-5">
        <div className="flex items-start gap-3">
          <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg border border-white/10 bg-white/10 text-fuchsia-200">
            <ReceiptText size={19} />
          </span>
          <div>
            <p className="app-eyebrow">Compras e parcelas</p>
            <h2 className="mt-2 font-serif text-2xl font-semibold text-[#f7ecff]">Itens detalhados entram na etapa 13</h2>
            <p className="mt-2 max-w-2xl text-sm leading-6 text-[#c8a9d8]">
              O contrato atual da fatura retorna o total consolidado. A listagem de compras/parcelas sera ligada quando o modulo de compras parceladas for implementado.
            </p>
          </div>
        </div>
      </section>

      <PayInvoiceDialog accounts={accountsQuery.data?.content ?? []} invoice={invoiceToPay} isLoading={payMutation.isPending || accountsQuery.isLoading} onClose={() => setInvoiceToPay(null)} onConfirm={confirmPay} />
    </section>
  );
}
