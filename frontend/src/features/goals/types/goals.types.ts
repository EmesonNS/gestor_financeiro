export type GoalStatus = 'ACTIVE' | 'COMPLETED' | 'CANCELED';

export type Goal = {
  id: string;
  name: string;
  targetAmount: number;
  currentAmount: number;
  deadline?: string | null;
  description?: string | null;
  status: GoalStatus;
  completionPercentage: number;
  createdAt: string;
  updatedAt: string;
};

export type GoalPayload = {
  name: string;
  targetAmount: number;
  currentAmount: number;
  deadline?: string | null;
  description?: string | null;
};

export type GoalProgressPayload = {
  currentAmount: number;
};

export type GoalFilters = {
  page: number;
  status?: GoalStatus;
};

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};
