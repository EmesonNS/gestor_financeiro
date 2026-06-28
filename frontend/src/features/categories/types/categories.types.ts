export type CategoryType = 'INCOME' | 'EXPENSE';

export type Category = {
  id: string;
  name: string;
  type: CategoryType;
  color?: string | null;
  icon?: string | null;
  defaultCategory: boolean;
  createdAt: string;
  updatedAt: string;
};

export type CategoryPayload = {
  name: string;
  type: CategoryType;
  color?: string | null;
  icon?: string | null;
};

export type CategoryFilters = {
  page: number;
  type?: CategoryType;
};

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type CategoryCountResponse = {
  count: number;
};

export type CategoryTypeCountsResponse = {
  incomeCount: number;
  expenseCount: number;
};
