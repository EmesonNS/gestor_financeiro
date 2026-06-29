import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { dashboardKeys } from '../../dashboard/hooks/useDashboard';
import { budgetsService } from '../services/budgets.service';
import type { BudgetFilters, BudgetPayload } from '../types/budgets.types';

export const budgetKeys = {
  all: ['budgets'] as const,
  detail: (budgetId: string) => [...budgetKeys.all, 'detail', budgetId] as const,
  list: (filters: BudgetFilters) => [...budgetKeys.all, 'list', filters] as const,
};

async function invalidateBudgetState(queryClient: ReturnType<typeof useQueryClient>) {
  await queryClient.invalidateQueries({ queryKey: budgetKeys.all });
  await queryClient.invalidateQueries({ queryKey: dashboardKeys.all });
}

export function useBudgets(filters: BudgetFilters) {
  return useQuery({
    queryKey: budgetKeys.list(filters),
    queryFn: () => budgetsService.listBudgets(filters),
  });
}

export function useBudget(budgetId?: string) {
  return useQuery({
    enabled: Boolean(budgetId),
    queryKey: budgetKeys.detail(budgetId ?? ''),
    queryFn: () => budgetsService.getBudget(budgetId ?? ''),
  });
}

export function useCreateBudget() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: BudgetPayload) => budgetsService.createBudget(payload),
    onSuccess: async () => {
      await invalidateBudgetState(queryClient);
    },
  });
}

export function useUpdateBudget(budgetId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: BudgetPayload) => budgetsService.updateBudget(budgetId, payload),
    onSuccess: async () => {
      await invalidateBudgetState(queryClient);
      await queryClient.invalidateQueries({ queryKey: budgetKeys.detail(budgetId) });
    },
  });
}

export function useDeleteBudget() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: budgetsService.deleteBudget,
    onSuccess: async () => {
      await invalidateBudgetState(queryClient);
    },
  });
}
