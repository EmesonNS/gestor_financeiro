import { BarChart3, ChevronLeft, ChevronRight, FileText } from 'lucide-react';
import { useState } from 'react';

import { Button } from '../../../shared/ui/Button';
import { useAccounts } from '../../accounts/hooks/useAccounts';
import { useCategories } from '../../categories/hooks/useCategories';
import { useCreditCards } from '../../credit-cards/hooks/useCreditCards';
import { ReportFilters } from '../components/ReportFilters';
import { ReportTable } from '../components/ReportTable';
import { useReport } from '../hooks/useReports';
import type { ReportFilters as ReportFiltersType, ReportType } from '../types/reports.types';
import { currentReportOption, defaultReportFilters, formatCurrency, reportSummaryValue, reportTypes } from '../utils/report-format';

export function ReportsPage() {
  const [reportType, setReportType] = useState<ReportType>('expenses-by-category');
  const [filters, setFilters] = useState<ReportFiltersType>(() => defaultReportFilters());
  const reportQuery = useReport(reportType, filters);
  const accountsQuery = useAccounts({ archived: false, page: 0 });
  const categoriesQuery = useCategories({ page: 0 });
  const cardsQuery = useCreditCards({ archived: false, page: 0 });
  const rows = reportQuery.data?.content ?? [];
  const totalPages = reportQuery.data?.totalPages ?? 0;
  const totalElements = reportQuery.data?.totalElements ?? 0;
  const reportOption = currentReportOption(reportType);
  const summaryValue = reportSummaryValue(reportType, rows);

  function updateReportType(nextReportType: ReportType) {
    setReportType(nextReportType);
    setFilters({ ...defaultReportFilters(), page: 0 });
  }

  function setPage(page: number) {
    setFilters((current) => ({ ...current, page }));
  }

  return (
    <section className="space-y-6">
      <div className="app-page-hero">
        <div className="admin-flow-line" aria-hidden="true" />
        <div className="relative flex flex-wrap items-start justify-between gap-4">
          <div>
            <p className="app-eyebrow">Relatorios</p>
            <h1 className="app-hero-title mt-4">Leitura financeira</h1>
            <p className="app-hero-copy mt-4 max-w-2xl">Analise transacoes, categorias, saldos, orcamentos, cartoes e parcelas futuras por periodo.</p>
          </div>

          <div className="app-panel-muted grid min-w-56 gap-3 p-4">
            <span className="inline-flex items-center gap-2 text-sm font-medium text-fuchsia-50/85">
              <BarChart3 size={17} /> Total nesta pagina
            </span>
            <strong className="font-serif text-4xl text-[#f7ecff]">{formatCurrency(summaryValue)}</strong>
            <span className="text-sm text-[#c8a9d8]">{totalElements} linha(s) encontradas</span>
          </div>
        </div>
      </div>

      <section className="grid gap-3 lg:grid-cols-7">
        {reportTypes.map((report) => (
          <button
            className={`rounded-lg border p-4 text-left transition ${
              reportType === report.value
                ? 'border-fuchsia-300/35 bg-fuchsia-400/15 shadow-lg shadow-fuchsia-950/20'
                : 'border-white/10 bg-white/10 hover:border-white/20 hover:bg-white/15'
            }`}
            key={report.value}
            onClick={() => updateReportType(report.value)}
            type="button"
          >
            <span className="flex h-9 w-9 items-center justify-center rounded-lg border border-white/10 bg-[#24112f]/80 text-fuchsia-200">
              <FileText size={17} />
            </span>
            <strong className="mt-3 block text-sm text-[#f7ecff]">{report.label}</strong>
            <span className="mt-2 block text-xs leading-5 text-[#c8a9d8]">{report.description}</span>
          </button>
        ))}
      </section>

      <div className="app-panel-muted p-5">
        <p className="app-eyebrow">Filtro ativo</p>
        <h2 className="mt-2 font-serif text-2xl font-semibold text-[#f7ecff]">{reportOption.label}</h2>
        <p className="mt-2 text-sm leading-6 text-[#c8a9d8]">{reportOption.description}</p>
      </div>

      <ReportFilters
        accounts={accountsQuery.data?.content ?? []}
        cards={cardsQuery.data?.content ?? []}
        categories={categoriesQuery.data?.content ?? []}
        filters={filters}
        onChange={setFilters}
        reportType={reportType}
      />

      {reportQuery.isLoading ? <div className="app-panel-muted p-8 text-center text-sm font-medium text-fuchsia-50">Gerando relatorio...</div> : null}
      {reportQuery.isError ? <div className="rounded-lg border border-rose-300/25 bg-rose-400/10 p-6 text-sm font-medium text-rose-200">Nao foi possivel gerar este relatorio.</div> : null}

      {!reportQuery.isLoading && !rows.length ? (
        <div className="rounded-lg border border-dashed border-white/20 bg-white/10 p-10 text-center backdrop-blur">
          <BarChart3 className="mx-auto text-fuchsia-300" size={38} />
          <h2 className="mt-4 font-serif text-2xl font-semibold text-[#f7ecff]">Nenhuma linha encontrada</h2>
          <p className="mt-2 text-sm text-[#c8a9d8]">Ajuste os filtros ou selecione outro relatorio.</p>
        </div>
      ) : null}

      <ReportTable reportType={reportType} rows={rows} />

      {totalPages > 1 ? (
        <div className="flex items-center justify-end gap-2">
          <Button disabled={filters.page === 0 || reportQuery.isFetching} onClick={() => setPage(Math.max(0, filters.page - 1))} type="button" variant="secondary">
            <ChevronLeft size={16} /> Anterior
          </Button>
          <span className="text-sm font-medium text-fuchsia-50">
            Pagina {filters.page + 1} de {totalPages}
          </span>
          <Button disabled={filters.page + 1 >= totalPages || reportQuery.isFetching} onClick={() => setPage(filters.page + 1)} type="button" variant="secondary">
            Proxima <ChevronRight size={16} />
          </Button>
        </div>
      ) : null}
    </section>
  );
}
