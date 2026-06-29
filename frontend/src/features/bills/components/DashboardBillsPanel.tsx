import { AlertTriangle, CalendarClock } from 'lucide-react';
import { Link } from 'react-router';

import type { Bill } from '../types/bills.types';
import { dueCopy, formatCurrency } from '../utils/bill-format';

type DashboardBillsPanelProps = {
  isLoading: boolean;
  overdueBills: Bill[];
  upcomingBills: Bill[];
};

function BillMiniRow({ bill }: { bill: Bill }) {
  return (
    <li className="rounded-lg border border-white/10 bg-[#24112f]/70 px-3 py-2">
      <div className="flex items-center justify-between gap-3">
        <span className="truncate text-sm font-semibold text-[#f7ecff]">{bill.description}</span>
        <strong className="text-sm text-[#f7ecff]">{formatCurrency(bill.amount)}</strong>
      </div>
      <p className="mt-1 text-xs text-[#c8a9d8]">{dueCopy(bill)}</p>
    </li>
  );
}

export function DashboardBillsPanel({ isLoading, overdueBills, upcomingBills }: DashboardBillsPanelProps) {
  return (
    <section className="app-panel-muted p-5">
      <div className="flex items-start gap-3">
        <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg border border-white/10 bg-rose-400/15 text-rose-200">
          <AlertTriangle size={19} />
        </span>
        <div className="min-w-0 flex-1">
          <p className="app-eyebrow">Vencimentos</p>
          <h2 className="mt-2 font-serif text-2xl font-semibold text-[#f7ecff]">Contas proximas</h2>
          {isLoading ? <p className="mt-4 text-sm text-[#c8a9d8]">Carregando vencimentos...</p> : null}

          {!isLoading ? (
            <div className="mt-4 grid gap-4">
              <div>
                <p className="mb-2 flex items-center gap-2 text-sm font-semibold text-rose-100">
                  <AlertTriangle size={16} /> Atrasadas
                </p>
                {overdueBills.length ? (
                  <ul className="space-y-2">
                    {overdueBills.slice(0, 3).map((bill) => (
                      <BillMiniRow bill={bill} key={bill.id} />
                    ))}
                  </ul>
                ) : (
                  <p className="rounded-lg border border-white/10 bg-white/10 px-3 py-2 text-sm text-[#c8a9d8]">Nenhuma conta atrasada.</p>
                )}
              </div>

              <div>
                <p className="mb-2 flex items-center gap-2 text-sm font-semibold text-fuchsia-100">
                  <CalendarClock size={16} /> Proximos 7 dias
                </p>
                {upcomingBills.length ? (
                  <ul className="space-y-2">
                    {upcomingBills.slice(0, 3).map((bill) => (
                      <BillMiniRow bill={bill} key={bill.id} />
                    ))}
                  </ul>
                ) : (
                  <p className="rounded-lg border border-white/10 bg-white/10 px-3 py-2 text-sm text-[#c8a9d8]">Sem vencimentos proximos.</p>
                )}
              </div>
            </div>
          ) : null}

          <Link className="mt-4 inline-flex text-sm font-semibold text-fuchsia-100 hover:text-white" to="/bills">
            Ver contas a pagar
          </Link>
        </div>
      </div>
    </section>
  );
}
