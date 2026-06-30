import { AxiosError } from 'axios';
import { ArrowLeft, ChevronLeft, ChevronRight, ReceiptText } from 'lucide-react';
import { useMemo, useState } from 'react';
import { Link, useParams } from 'react-router';

import { apiErrorMessage } from '../../../shared/lib/api-error';
import { Button } from '../../../shared/ui/Button';
import { useAccounts } from '../../accounts/hooks/useAccounts';
import type { ApiErrorResponse } from '../../auth/types/auth.types';
import { useCreditCard } from '../../credit-cards/hooks/useCreditCards';
import { InvoiceCard } from '../components/InvoiceCard';
import { InvoiceSummary } from '../components/InvoiceSummary';
import { PayInvoiceDialog } from '../components/PayInvoiceDialog';
import { useCurrentInvoice, useInvoices, usePayInvoice } from '../hooks/useInvoices';
import type { Invoice, InvoiceStatus } from '../types/invoices.types';
import { formatCurrency, invoiceStatuses, invoiceStatusLabels } from '../utils/invoice-format';

type InvoiceStatusFilter = 'ALL' | InvoiceStatus;

const statusOptions: Array<{ label: string; value: InvoiceStatusFilter }> = [
  { label: 'Todas', value: 'ALL' },
  ...invoiceStatuses.map((status) => ({ label: invoiceStatusLabels[status], value: status })),
];

export function CardInvoicesPage() {
  const { cardId } = useParams();
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState<InvoiceStatusFilter>('ALL');
  const [currentDate] = useState(() => new Date());
  const [year, setYear] = useState(currentDate.getFullYear());
  const [invoiceToPay, setInvoiceToPay] = useState<Invoice | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const filters = useMemo(
    () => ({
      page,
      status: statusFilter === 'ALL' ? undefined : statusFilter,
      year,
    }),
    [page, statusFilter, year],
  );
  const cardQuery = useCreditCard(cardId);
  const currentInvoiceQuery = useCurrentInvoice(cardId);
  const invoicesQuery = useInvoices(cardId, filters);
  const accountsQuery = useAccounts({ archived: false, page: 0 });
  const payMutation = usePayInvoice();
  const card = cardQuery.data;
  const invoices = invoicesQuery.data?.content ?? [];
  const accounts = accountsQuery.data?.content ?? [];
  const totalPages = invoicesQuery.data?.totalPages ?? 0;
  const openTotal = invoices.filter((invoice) => invoice.status !== 'PAID').reduce((total, invoice) => total + Number(invoice.totalAmount), 0);

  function resetPage() {
    setPage(0);
  }

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

  if (cardQuery.isLoading) {
    return <div className="app-panel-muted p-8 text-center text-sm font-medium text-fuchsia-50">Carregando cartao...</div>;
  }

  if (cardQuery.isError || !card) {
    return (
      <section className="rounded-lg border border-rose-300/25 bg-rose-400/10 p-6">
        <h1 className="font-serif text-2xl font-semibold text-rose-100">Cartao nao encontrado</h1>
        <p className="mt-2 text-sm text-rose-200">Nao foi possivel carregar as faturas deste cartao.</p>
        <Link className="mt-4 inline-flex font-semibold text-rose-100" to="/credit-cards">
          Voltar para cartoes
        </Link>
      </section>
    );
  }

  return (
    <section className="space-y-6">
      <Link className="inline-flex items-center gap-2 text-sm font-semibold text-fuchsia-100 hover:text-white" to={`/credit-cards/${card.id}`}>
        <ArrowLeft size={16} /> Voltar para o cartao
      </Link>

      <div className="app-page-hero">
        <div className="admin-flow-line" aria-hidden="true" />
        <div className="relative flex flex-wrap items-start justify-between gap-4">
          <div>
            <p className="app-eyebrow">Faturas de cartao</p>
            <h1 className="app-hero-title mt-4">{card.name}</h1>
            <p className="app-hero-copy mt-4 max-w-2xl">Veja fatura atual, historico por ano e registre pagamentos usando uma conta financeira.</p>
          </div>

          <div className="app-panel-muted grid min-w-56 gap-3 p-4">
            <span className="inline-flex items-center gap-2 text-sm font-medium text-fuchsia-50/85">
              <ReceiptText size={17} /> Em aberto nesta pagina
            </span>
            <strong className="font-serif text-4xl text-[#f7ecff]">{formatCurrency(openTotal)}</strong>
            <span className="text-sm text-[#c8a9d8]">Ano {year}</span>
          </div>
        </div>
      </div>

      {currentInvoiceQuery.isLoading ? <div className="app-panel-muted p-6 text-sm font-medium text-fuchsia-50">Carregando fatura atual...</div> : null}
      {currentInvoiceQuery.isError ? <div className="rounded-lg border border-rose-300/25 bg-rose-400/10 p-6 text-sm font-medium text-rose-200">Nao foi possivel carregar a fatura atual.</div> : null}
      {currentInvoiceQuery.data ? <InvoiceSummary invoice={currentInvoiceQuery.data} onPay={setInvoiceToPay} /> : null}

      <div className="grid gap-3 rounded-lg border border-white/10 bg-white/10 p-3 backdrop-blur lg:grid-cols-[1fr_auto]">
        <div className="flex flex-wrap gap-2">
          {statusOptions.map((option) => (
            <button
              className={`min-h-10 rounded-lg px-3 py-2 text-sm font-semibold transition ${
                statusFilter === option.value ? 'bg-fuchsia-600 text-white shadow-lg shadow-fuchsia-950/30' : 'border border-white/10 bg-white/10 text-fuchsia-100 hover:bg-white/15'
              }`}
              key={option.value}
              onClick={() => {
                setStatusFilter(option.value);
                resetPage();
              }}
              type="button"
            >
              {option.label}
            </button>
          ))}
        </div>

        <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="invoiceYear">
          Ano
          <input
            className="mt-2 min-h-10 w-36 rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20"
            id="invoiceYear"
            max="2100"
            min="2000"
            onChange={(event) => {
              setYear(Number(event.target.value));
              resetPage();
            }}
            type="number"
            value={year}
          />
        </label>
      </div>

      {actionError ? <p className="rounded-lg border border-rose-300/25 bg-rose-400/10 px-4 py-3 text-sm font-medium text-rose-200">{actionError}</p> : null}
      {invoicesQuery.isLoading ? <div className="app-panel-muted p-8 text-center text-sm font-medium text-fuchsia-50">Carregando faturas...</div> : null}
      {invoicesQuery.isError ? <div className="rounded-lg border border-rose-300/25 bg-rose-400/10 p-6 text-sm font-medium text-rose-200">Nao foi possivel carregar o historico de faturas.</div> : null}

      {!invoicesQuery.isLoading && !invoices.length ? (
        <div className="rounded-lg border border-dashed border-white/20 bg-white/10 p-10 text-center backdrop-blur">
          <ReceiptText className="mx-auto text-fuchsia-300" size={38} />
          <h2 className="mt-4 font-serif text-2xl font-semibold text-[#f7ecff]">Nenhuma fatura encontrada</h2>
          <p className="mt-2 text-sm text-[#c8a9d8]">A fatura atual aparece automaticamente quando o backend cria a competencia do cartao.</p>
        </div>
      ) : null}

      <div className="grid gap-4 lg:grid-cols-2">
        {invoices.map((invoice) => (
          <InvoiceCard invoice={invoice} key={invoice.id} onPay={setInvoiceToPay} />
        ))}
      </div>

      {totalPages > 1 ? (
        <div className="flex items-center justify-end gap-2">
          <Button disabled={page === 0 || invoicesQuery.isFetching} onClick={() => setPage((current) => Math.max(0, current - 1))} type="button" variant="secondary">
            <ChevronLeft size={16} /> Anterior
          </Button>
          <span className="text-sm font-medium text-fuchsia-50">
            Pagina {page + 1} de {totalPages}
          </span>
          <Button disabled={page + 1 >= totalPages || invoicesQuery.isFetching} onClick={() => setPage((current) => current + 1)} type="button" variant="secondary">
            Proxima <ChevronRight size={16} />
          </Button>
        </div>
      ) : null}

      <PayInvoiceDialog accounts={accounts} invoice={invoiceToPay} isLoading={payMutation.isPending || accountsQuery.isLoading} onClose={() => setInvoiceToPay(null)} onConfirm={confirmPay} />
    </section>
  );
}
