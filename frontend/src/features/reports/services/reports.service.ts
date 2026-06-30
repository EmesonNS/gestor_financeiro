import { DEFAULT_PAGE_SIZE } from '../../../shared/config/pagination';
import { apiClient } from '../../../shared/lib/api-client';
import type { PageResponse, ReportFilters, ReportPage, ReportRow, ReportType } from '../types/reports.types';

function reportPath(reportType: ReportType) {
  return `/reports/${reportType}`;
}

function reportParams(filters: ReportFilters) {
  return {
    accountId: filters.accountId,
    cardId: filters.cardId,
    categoryId: filters.categoryId,
    date: filters.date,
    endDate: filters.endDate,
    fromMonth: filters.fromMonth,
    fromYear: filters.fromYear,
    month: filters.month,
    page: filters.page,
    size: DEFAULT_PAGE_SIZE,
    startDate: filters.startDate,
    type: filters.type,
    year: filters.year,
  };
}

export const reportsService = {
  async getReport(reportType: ReportType, filters: ReportFilters): Promise<ReportPage> {
    const { data } = await apiClient.get<PageResponse<ReportRow>>(reportPath(reportType), {
      params: reportParams(filters),
    });
    return data;
  },
};
