export type Budget = {
  id: string;
  categoryId: string;
  startMonth: number;
  startYear: number;
  endMonth?: number | null;
  endYear?: number | null;
  limitAmount: number;
  spentAmount: number;
  remainingAmount: number;
  usagePercentage: number;
  exceeded: boolean;
  createdAt: string;
  updatedAt: string;
};

export type BudgetPayload = {
  categoryId: string;
  startMonth: number;
  startYear: number;
  endMonth?: number | null;
  endYear?: number | null;
  limitAmount: number;
};

export type BudgetFilters = {
  categoryId?: string;
  month?: number;
  page: number;
  year?: number;
};

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};
