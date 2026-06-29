import { DEFAULT_PAGE_SIZE } from '../../../shared/config/pagination';
import { apiClient } from '../../../shared/lib/api-client';
import type { Goal, GoalFilters, GoalPayload, GoalProgressPayload, PageResponse } from '../types/goals.types';

export const goalsService = {
  async listGoals(filters: GoalFilters) {
    const { data } = await apiClient.get<PageResponse<Goal>>('/goals', {
      params: {
        page: filters.page,
        size: DEFAULT_PAGE_SIZE,
        status: filters.status,
      },
    });
    return data;
  },

  async getGoal(goalId: string) {
    const { data } = await apiClient.get<Goal>(`/goals/${goalId}`);
    return data;
  },

  async createGoal(payload: GoalPayload) {
    const { data } = await apiClient.post<Goal>('/goals', payload);
    return data;
  },

  async updateGoal(goalId: string, payload: GoalPayload) {
    const { data } = await apiClient.put<Goal>(`/goals/${goalId}`, payload);
    return data;
  },

  async deleteGoal(goalId: string) {
    await apiClient.delete(`/goals/${goalId}`);
  },

  async updateProgress(goalId: string, payload: GoalProgressPayload) {
    const { data } = await apiClient.patch<Goal>(`/goals/${goalId}/progress`, payload);
    return data;
  },
};
