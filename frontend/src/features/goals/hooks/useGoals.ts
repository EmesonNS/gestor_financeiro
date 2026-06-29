import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { dashboardKeys } from '../../dashboard/hooks/useDashboard';
import { goalsService } from '../services/goals.service';
import type { GoalFilters, GoalPayload, GoalProgressPayload } from '../types/goals.types';

export const goalKeys = {
  all: ['goals'] as const,
  detail: (goalId: string) => [...goalKeys.all, 'detail', goalId] as const,
  list: (filters: GoalFilters) => [...goalKeys.all, 'list', filters] as const,
};

async function invalidateGoalState(queryClient: ReturnType<typeof useQueryClient>) {
  await queryClient.invalidateQueries({ queryKey: goalKeys.all });
  await queryClient.invalidateQueries({ queryKey: dashboardKeys.all });
}

export function useGoals(filters: GoalFilters) {
  return useQuery({
    queryKey: goalKeys.list(filters),
    queryFn: () => goalsService.listGoals(filters),
  });
}

export function useGoal(goalId?: string) {
  return useQuery({
    enabled: Boolean(goalId),
    queryKey: goalKeys.detail(goalId ?? ''),
    queryFn: () => goalsService.getGoal(goalId ?? ''),
  });
}

export function useCreateGoal() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: GoalPayload) => goalsService.createGoal(payload),
    onSuccess: async () => {
      await invalidateGoalState(queryClient);
    },
  });
}

export function useUpdateGoal(goalId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: GoalPayload) => goalsService.updateGoal(goalId, payload),
    onSuccess: async () => {
      await invalidateGoalState(queryClient);
      await queryClient.invalidateQueries({ queryKey: goalKeys.detail(goalId) });
    },
  });
}

export function useDeleteGoal() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: goalsService.deleteGoal,
    onSuccess: async () => {
      await invalidateGoalState(queryClient);
    },
  });
}

export function useUpdateGoalProgress() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ goalId, payload }: { goalId: string; payload: GoalProgressPayload }) => goalsService.updateProgress(goalId, payload),
    onSuccess: async () => {
      await invalidateGoalState(queryClient);
    },
  });
}
