import { DEFAULT_PAGE_SIZE } from '../../../shared/config/pagination';
import { apiClient } from '../../../shared/lib/api-client';
import type {
  DashboardChartFilters,
  DashboardMonthly,
  DashboardPeriod,
  DashboardSummary,
  ExpenseByCategory,
  IncomeExpenseFilters,
  IncomeExpenseMonthly,
  PageResponse,
} from '../types/dashboard.types';

export const dashboardService = {
  async getSummary(period: DashboardPeriod) {
    const { data } = await apiClient.get<DashboardSummary>('/dashboard/summary', {
      params: period,
    });
    return data;
  },

  async getMonthly(period: DashboardPeriod) {
    const { data } = await apiClient.get<DashboardMonthly>('/dashboard/monthly', {
      params: period,
    });
    return data;
  },

  async getExpensesByCategory(filters: DashboardChartFilters) {
    const { data } = await apiClient.get<PageResponse<ExpenseByCategory>>('/dashboard/charts/expenses-by-category', {
      params: {
        month: filters.period.month,
        page: filters.page,
        size: DEFAULT_PAGE_SIZE,
        sort: 'amount,desc',
        year: filters.period.year,
      },
    });
    return data;
  },

  async getIncomeVsExpense(filters: IncomeExpenseFilters) {
    const { data } = await apiClient.get<PageResponse<IncomeExpenseMonthly>>('/dashboard/charts/income-vs-expense', {
      params: {
        page: filters.page,
        size: DEFAULT_PAGE_SIZE,
        year: filters.year,
      },
    });
    return data;
  },
};
