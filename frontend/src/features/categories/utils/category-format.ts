import type { Category, CategoryType } from '../types/categories.types';

export const categoryTypeLabels: Record<CategoryType, string> = {
  EXPENSE: 'Despesa',
  INCOME: 'Receita',
};

export const categoryTypes = Object.keys(categoryTypeLabels) as CategoryType[];

export const categoryColorOptions = ['#D946EF', '#FF6BB5', '#C084FC', '#22C55E', '#F59E0B', '#38BDF8'];

export function countByType(categories: Category[], type: CategoryType) {
  return categories.filter((category) => category.type === type).length;
}

export function formatDateTime(value?: string | null) {
  if (!value) {
    return 'Nao registrado';
  }

  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(new Date(value));
}
