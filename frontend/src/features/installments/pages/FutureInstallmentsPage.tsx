import { ChevronLeft, ChevronRight, ReceiptText } from 'lucide-react';
import { useState } from 'react';

import { Button } from '../../../shared/ui/Button';
import { useCreditCards } from '../../credit-cards/hooks/useCreditCards';
import { InstallmentMiniCard } from '../components/InstallmentMiniCard';
import { useFutureInstallments } from '../hooks/useInstallments';
import { formatCurrency } from '../utils/installment-format';

export function FutureInstallmentsPage() {
  const [currentDate] = useState(() => new Date());
  const [page, setPage] = useState(0);
  const [cardId, setCardId] = useState('');
  const [fromMonth, setFromMonth] = useState(currentDate.getMonth() + 1);
  const [fromYear, setFromYear] = useState(currentDate.getFullYear());
  const [toMonth, setToMonth] = useState<number | ''>('');
  const [toYear, setToYear] = useState<number | ''>('');
  const installmentsQuery = useFutureInstallments({
    cardId: cardId || undefined,
    fromMonth,
    fromYear,
    page,
    toMonth: toMonth || undefined,
    toYear: toYear || undefined,
  });
  const cardsQuery = useCreditCards({ archived: false, page: 0 });
  const installments = installmentsQuery.data?.content ?? [];
  const totalPages = installmentsQuery.data?.totalPages ?? 0;
  const totalAmount = installments.reduce((total, installment) => total + Number(installment.amount), 0);

  function resetPage() {
    setPage(0);
  }

  return (
    <section className="space-y-6">
      <div className="app-page-hero">
        <div className="admin-flow-line" aria-hidden="true" />
        <div className="relative flex flex-wrap items-start justify-between gap-4">
          <div>
            <p className="app-eyebrow">Parcelas futuras</p>
            <h1 className="app-hero-title mt-4">Compromissos no cartao</h1>
            <p className="app-hero-copy mt-4 max-w-2xl">Veja parcelas abertas dentro de um intervalo de competencias e acompanhe faturas futuras.</p>
          </div>

          <div className="app-panel-muted grid min-w-56 gap-3 p-4">
            <span className="inline-flex items-center gap-2 text-sm font-medium text-fuchsia-50/85">
              <ReceiptText size={17} /> Total nesta pagina
            </span>
            <strong className="font-serif text-4xl text-[#f7ecff]">{formatCurrency(totalAmount)}</strong>
          </div>
        </div>
      </div>

      <div className="grid gap-3 rounded-lg border border-white/10 bg-white/10 p-3 backdrop-blur md:grid-cols-5">
        <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="futureCardId">
          Cartao
          <select
            className="mt-2 min-h-11 w-full rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20"
            id="futureCardId"
            onChange={(event) => {
              setCardId(event.target.value);
              resetPage();
            }}
            value={cardId}
          >
            <option value="">Todos</option>
            {(cardsQuery.data?.content ?? []).map((card) => (
              <option key={card.id} value={card.id}>
                {card.name}
              </option>
            ))}
          </select>
        </label>

        <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="futureMonth">
          Mes inicial
          <input
            className="mt-2 min-h-11 w-full rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20"
            id="futureMonth"
            max="12"
            min="1"
            onChange={(event) => {
              setFromMonth(Number(event.target.value));
              resetPage();
            }}
            type="number"
            value={fromMonth}
          />
        </label>

        <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="futureYear">
          Ano inicial
          <input
            className="mt-2 min-h-11 w-full rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20"
            id="futureYear"
            max="2100"
            min="2000"
            onChange={(event) => {
              const nextFromYear = Number(event.target.value);
              setFromYear(nextFromYear);
              if (toMonth && !toYear) {
                setToYear(nextFromYear);
              }
              resetPage();
            }}
            type="number"
            value={fromYear}
          />
        </label>

        <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="futureToMonth">
          Mes final
          <input
            className="mt-2 min-h-11 w-full rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20"
            id="futureToMonth"
            max="12"
            min="1"
            onChange={(event) => {
              const nextToMonth = event.target.value ? Number(event.target.value) : '';
              setToMonth(nextToMonth);
              if (nextToMonth && !toYear) {
                setToYear(fromYear);
              }
              resetPage();
            }}
            placeholder="Opcional"
            type="number"
            value={toMonth}
          />
        </label>

        <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="futureToYear">
          Ano final
          <input
            className="mt-2 min-h-11 w-full rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20"
            id="futureToYear"
            max="2100"
            min="2000"
            onChange={(event) => {
              setToYear(event.target.value ? Number(event.target.value) : toMonth ? fromYear : '');
              resetPage();
            }}
            placeholder="Opcional"
            type="number"
            value={toYear}
          />
        </label>
      </div>

      {installmentsQuery.isLoading ? <div className="app-panel-muted p-8 text-center text-sm font-medium text-fuchsia-50">Carregando parcelas futuras...</div> : null}
      {installmentsQuery.isError ? <div className="rounded-lg border border-rose-300/25 bg-rose-400/10 p-6 text-sm font-medium text-rose-200">Nao foi possivel carregar parcelas futuras.</div> : null}

      {!installmentsQuery.isLoading && !installments.length ? (
        <div className="rounded-lg border border-dashed border-white/20 bg-white/10 p-10 text-center backdrop-blur">
          <ReceiptText className="mx-auto text-fuchsia-300" size={38} />
          <h2 className="mt-4 font-serif text-2xl font-semibold text-[#f7ecff]">Nenhuma parcela futura</h2>
          <p className="mt-2 text-sm text-[#c8a9d8]">Ajuste filtros ou registre uma compra parcelada.</p>
        </div>
      ) : null}

      <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
        {installments.map((installment) => (
          <InstallmentMiniCard installment={installment} key={installment.id} />
        ))}
      </div>

      {totalPages > 1 ? (
        <div className="flex items-center justify-end gap-2">
          <Button disabled={page === 0 || installmentsQuery.isFetching} onClick={() => setPage((current) => Math.max(0, current - 1))} type="button" variant="secondary">
            <ChevronLeft size={16} /> Anterior
          </Button>
          <span className="text-sm font-medium text-fuchsia-50">
            Pagina {page + 1} de {totalPages}
          </span>
          <Button disabled={page + 1 >= totalPages || installmentsQuery.isFetching} onClick={() => setPage((current) => current + 1)} type="button" variant="secondary">
            Proxima <ChevronRight size={16} />
          </Button>
        </div>
      ) : null}
    </section>
  );
}
