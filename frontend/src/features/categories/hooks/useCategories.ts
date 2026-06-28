import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { categoriesService } from '../services/categories.service';
import type { CategoryFilters, CategoryPayload } from '../types/categories.types';

export const categoryKeys = {
  all: ['categories'] as const,
  customCount: () => [...categoryKeys.all, 'custom-count'] as const,
  detail: (categoryId: string) => [...categoryKeys.all, 'detail', categoryId] as const,
  list: (filters: CategoryFilters) => [...categoryKeys.all, 'list', filters] as const,
  typeCounts: () => [...categoryKeys.all, 'type-counts'] as const,
};

export function useCategories(filters: CategoryFilters) {
  return useQuery({
    queryKey: categoryKeys.list(filters),
    queryFn: () => categoriesService.listCategories(filters),
  });
}

export function useCategory(categoryId?: string) {
  return useQuery({
    enabled: Boolean(categoryId),
    queryKey: categoryKeys.detail(categoryId ?? ''),
    queryFn: () => categoriesService.getCategory(categoryId ?? ''),
  });
}

export function useCustomCategoryCount() {
  return useQuery({
    queryKey: categoryKeys.customCount(),
    queryFn: categoriesService.countCustomCategories,
  });
}

export function useCategoryTypeCounts() {
  return useQuery({
    queryKey: categoryKeys.typeCounts(),
    queryFn: categoriesService.countCategoriesByType,
  });
}

export function useCreateCategory() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: CategoryPayload) => categoriesService.createCategory(payload),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: categoryKeys.all });
    },
  });
}

export function useUpdateCategory(categoryId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: CategoryPayload) => categoriesService.updateCategory(categoryId, payload),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: categoryKeys.all });
      await queryClient.invalidateQueries({ queryKey: categoryKeys.detail(categoryId) });
    },
  });
}

export function useDeleteCategory() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: categoriesService.deleteCategory,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: categoryKeys.all });
    },
  });
}
