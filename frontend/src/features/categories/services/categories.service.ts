import { apiClient } from '../../../shared/lib/api-client';
import { DEFAULT_PAGE_SIZE } from '../../../shared/config/pagination';
import type { Category, CategoryCountResponse, CategoryFilters, CategoryPayload, CategoryTypeCountsResponse, PageResponse } from '../types/categories.types';

export const categoriesService = {
  async listCategories(filters: CategoryFilters) {
    const { data } = await apiClient.get<PageResponse<Category>>('/categories', {
      params: {
        page: filters.page,
        size: DEFAULT_PAGE_SIZE,
        type: filters.type,
      },
    });
    return data;
  },

  async getCategory(categoryId: string) {
    const { data } = await apiClient.get<Category>(`/categories/${categoryId}`);
    return data;
  },

  async countCustomCategories() {
    const { data } = await apiClient.get<CategoryCountResponse>('/categories/custom/count');
    return data;
  },

  async countCategoriesByType() {
    const { data } = await apiClient.get<CategoryTypeCountsResponse>('/categories/type-counts');
    return data;
  },

  async createCategory(payload: CategoryPayload) {
    const { data } = await apiClient.post<Category>('/categories', payload);
    return data;
  },

  async updateCategory(categoryId: string, payload: CategoryPayload) {
    const { data } = await apiClient.put<Category>(`/categories/${categoryId}`, payload);
    return data;
  },

  async deleteCategory(categoryId: string) {
    await apiClient.delete(`/categories/${categoryId}`);
  },
};
