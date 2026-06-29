export type BillStatus = 'PENDING' | 'PAID' | 'OVERDUE' | 'CANCELED';

export type Bill = {
  id: string;
  description: string;
  amount: number;
  dueDate: string;
  categoryId: string;
  accountId?: string | null;
  status: BillStatus;
  paidAt?: string | null;
  transactionId?: string | null;
  overdue: boolean;
  createdAt: string;
  updatedAt: string;
};

export type BillPayload = {
  description: string;
  amount: number;
  dueDate: string;
  categoryId: string;
  accountId?: string | null;
  status: BillStatus;
};

export type PayBillPayload = {
  accountId: string;
  paidAt: string;
};

export type BillFilters = {
  accountId?: string;
  categoryId?: string;
  endDueDate?: string;
  overdue?: boolean;
  page: number;
  startDueDate?: string;
  status?: BillStatus;
};

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};
