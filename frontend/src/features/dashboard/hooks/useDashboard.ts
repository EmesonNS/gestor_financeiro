import { useQuery } from '@tanstack/react-query';

import { dashboardService } from '../services/dashboard.service';
import type { DashboardChartFilters, DashboardPeriod, IncomeExpenseFilters } from '../types/dashboard.types';

export const dashboardKeys = {
  all: ['dashboard'] as const,
  expensesByCategory: (filters: DashboardChartFilters) => [...dashboardKeys.all, 'expenses-by-category', filters] as const,
  incomeVsExpense: (filters: IncomeExpenseFilters) => [...dashboardKeys.all, 'income-vs-expense', filters] as const,
  monthly: (period: DashboardPeriod) => [...dashboardKeys.all, 'monthly', period] as const,
  summary: (period: DashboardPeriod) => [...dashboardKeys.all, 'summary', period] as const,
};

export function useDashboardSummary(period: DashboardPeriod) {
  return useQuery({
    queryKey: dashboardKeys.summary(period),
    queryFn: () => dashboardService.getSummary(period),
  });
}

export function useDashboardMonthly(period: DashboardPeriod) {
  return useQuery({
    queryKey: dashboardKeys.monthly(period),
    queryFn: () => dashboardService.getMonthly(period),
  });
}

export function useExpensesByCategory(filters: DashboardChartFilters) {
  return useQuery({
    queryKey: dashboardKeys.expensesByCategory(filters),
    queryFn: () => dashboardService.getExpensesByCategory(filters),
  });
}

export function useIncomeVsExpense(filters: IncomeExpenseFilters) {
  return useQuery({
    queryKey: dashboardKeys.incomeVsExpense(filters),
    queryFn: () => dashboardService.getIncomeVsExpense(filters),
  });
}
