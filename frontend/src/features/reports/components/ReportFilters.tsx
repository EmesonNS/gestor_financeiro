import type { Account } from '../../accounts/types/accounts.types';
import type { Category } from '../../categories/types/categories.types';
import type { CreditCard } from '../../credit-cards/types/credit-cards.types';
import { transactionTypes } from '../../transactions/utils/transaction-format';
import type { ReportFilters as ReportFiltersType, ReportType } from '../types/reports.types';

type ReportFiltersProps = {
  accounts: Account[];
  cards: CreditCard[];
  categories: Category[];
  filters: ReportFiltersType;
  onChange: (filters: ReportFiltersType) => void;
  reportType: ReportType;
};

const fieldClass =
  'mt-2 min-h-11 w-full rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20';

function filterValue(value?: string | number) {
  return value ?? '';
}

export function ReportFilters({ accounts, cards, categories, filters, onChange, reportType }: ReportFiltersProps) {
  function update(next: Partial<ReportFiltersType>) {
    onChange({ ...filters, ...next, page: 0 });
  }

  const usesDateRange = reportType === 'transactions' || reportType === 'expenses-by-category' || reportType === 'credit-card-expenses';
  const usesYear = reportType === 'monthly-evolution';
  const usesDate = reportType === 'accounts-balance';
  const usesMonthYear = reportType === 'budget-vs-actual';
  const usesFutureFrom = reportType === 'future-installments';
  const usesCard = reportType === 'credit-card-expenses' || reportType === 'future-installments';

  return (
    <section className="grid gap-3 rounded-lg border border-white/10 bg-white/10 p-3 backdrop-blur md:grid-cols-2 xl:grid-cols-4">
      {usesDateRange ? (
        <>
          <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="reportStartDate">
            Inicio
            <input className={fieldClass} id="reportStartDate" onChange={(event) => update({ startDate: event.target.value || undefined })} type="date" value={filters.startDate ?? ''} />
          </label>
          <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="reportEndDate">
            Fim
            <input className={fieldClass} id="reportEndDate" onChange={(event) => update({ endDate: event.target.value || undefined })} type="date" value={filters.endDate ?? ''} />
          </label>
        </>
      ) : null}

      {reportType === 'transactions' ? (
        <>
          <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="reportTransactionType">
            Tipo
            <select className={fieldClass} id="reportTransactionType" onChange={(event) => update({ type: event.target.value ? (event.target.value as ReportFiltersType['type']) : undefined })} value={filters.type ?? ''}>
              <option value="">Todos</option>
              {transactionTypes.map((type) => (
                <option key={type} value={type}>
                  {type === 'EXPENSE' ? 'Despesa' : 'Receita'}
                </option>
              ))}
            </select>
          </label>

          <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="reportCategoryId">
            Categoria
            <select className={fieldClass} id="reportCategoryId" onChange={(event) => update({ categoryId: event.target.value || undefined })} value={filters.categoryId ?? ''}>
              <option value="">Todas</option>
              {categories.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
          </label>

          <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="reportAccountId">
            Conta
            <select className={fieldClass} id="reportAccountId" onChange={(event) => update({ accountId: event.target.value || undefined })} value={filters.accountId ?? ''}>
              <option value="">Todas</option>
              {accounts.map((account) => (
                <option key={account.id} value={account.id}>
                  {account.name}
                </option>
              ))}
            </select>
          </label>
        </>
      ) : null}

      {usesCard ? (
        <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="reportCardId">
          Cartao
          <select className={fieldClass} id="reportCardId" onChange={(event) => update({ cardId: event.target.value || undefined })} value={filters.cardId ?? ''}>
            <option value="">Todos</option>
            {cards.map((card) => (
              <option key={card.id} value={card.id}>
                {card.name}
              </option>
            ))}
          </select>
        </label>
      ) : null}

      {usesYear ? (
        <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="reportYear">
          Ano
          <input className={fieldClass} id="reportYear" max="2100" min="2000" onChange={(event) => update({ year: Number(event.target.value) || undefined })} type="number" value={filterValue(filters.year)} />
        </label>
      ) : null}

      {usesDate ? (
        <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="reportDate">
          Data
          <input className={fieldClass} id="reportDate" onChange={(event) => update({ date: event.target.value || undefined })} type="date" value={filters.date ?? ''} />
        </label>
      ) : null}

      {usesMonthYear ? (
        <>
          <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="reportMonth">
            Mes
            <input className={fieldClass} id="reportMonth" max="12" min="1" onChange={(event) => update({ month: Number(event.target.value) || undefined })} type="number" value={filterValue(filters.month)} />
          </label>
          <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="reportBudgetYear">
            Ano
            <input className={fieldClass} id="reportBudgetYear" max="2100" min="2000" onChange={(event) => update({ year: Number(event.target.value) || undefined })} type="number" value={filterValue(filters.year)} />
          </label>
        </>
      ) : null}

      {usesFutureFrom ? (
        <>
          <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="reportFromMonth">
            Mes inicial
            <input className={fieldClass} id="reportFromMonth" max="12" min="1" onChange={(event) => update({ fromMonth: Number(event.target.value) || undefined })} type="number" value={filterValue(filters.fromMonth)} />
          </label>
          <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="reportFromYear">
            Ano inicial
            <input className={fieldClass} id="reportFromYear" max="2100" min="2000" onChange={(event) => update({ fromYear: Number(event.target.value) || undefined })} type="number" value={filterValue(filters.fromYear)} />
          </label>
        </>
      ) : null}
    </section>
  );
}
