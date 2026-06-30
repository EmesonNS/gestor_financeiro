import { useQuery } from '@tanstack/react-query';

import { reportsService } from '../services/reports.service';
import type { ReportFilters, ReportType } from '../types/reports.types';

export const reportKeys = {
  all: ['reports'] as const,
  detail: (reportType: ReportType, filters: ReportFilters) => [...reportKeys.all, reportType, filters] as const,
};

export function useReport(reportType: ReportType, filters: ReportFilters) {
  return useQuery({
    queryKey: reportKeys.detail(reportType, filters),
    queryFn: () => reportsService.getReport(reportType, filters),
  });
}
