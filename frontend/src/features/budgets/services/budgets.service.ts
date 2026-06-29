import { DEFAULT_PAGE_SIZE } from '../../../shared/config/pagination';
import { apiClient } from '../../../shared/lib/api-client';
import type { Budget, BudgetFilters, BudgetPayload, PageResponse } from '../types/budgets.types';

export const budgetsService = {
  async listBudgets(filters: BudgetFilters) {
    const { data } = await apiClient.get<PageResponse<Budget>>('/budgets', {
      params: {
        categoryId: filters.categoryId,
        month: filters.month,
        page: filters.page,
        size: DEFAULT_PAGE_SIZE,
        year: filters.year,
      },
    });
    return data;
  },

  async getBudget(budgetId: string) {
    const { data } = await apiClient.get<Budget>(`/budgets/${budgetId}`);
    return data;
  },

  async createBudget(payload: BudgetPayload) {
    const { data } = await apiClient.post<Budget>('/budgets', payload);
    return data;
  },

  async updateBudget(budgetId: string, payload: BudgetPayload) {
    const { data } = await apiClient.put<Budget>(`/budgets/${budgetId}`, payload);
    return data;
  },

  async deleteBudget(budgetId: string) {
    await apiClient.delete(`/budgets/${budgetId}`);
  },
};
