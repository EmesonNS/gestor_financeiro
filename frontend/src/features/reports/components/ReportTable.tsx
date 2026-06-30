import type { ReportRow, ReportType } from '../types/reports.types';
import { reportHeaders, rowCells } from '../utils/report-format';

type ReportTableProps = {
  rows: ReportRow[];
  reportType: ReportType;
};

export function ReportTable({ reportType, rows }: ReportTableProps) {
  const headers = reportHeaders(reportType);

  if (!rows.length) {
    return null;
  }

  return (
    <section className="app-panel overflow-hidden p-0">
      <div className="app-scrollbar overflow-x-auto">
        <table className="min-w-full border-separate border-spacing-0 text-left text-sm">
          <thead>
            <tr>
              {headers.map((header) => (
                <th className="border-b border-white/10 bg-white/10 px-4 py-3 text-xs font-bold uppercase tracking-[0.16em] text-[#c8a9d8]" key={header}>
                  {header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {rows.map((row, rowIndex) => (
              <tr className="hover:bg-white/5" key={`${reportType}-${rowIndex}`}>
                {rowCells(reportType, row).map((cell, cellIndex) => (
                  <td className="border-b border-white/10 px-4 py-4 text-[#f7ecff]" key={`${reportType}-${rowIndex}-${cellIndex}`}>
                    {typeof cell === 'object' && cell !== null ? <span className={`rounded-full border px-2 py-1 text-xs font-bold ${cell.tone}`}>{cell.label}</span> : cell}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
