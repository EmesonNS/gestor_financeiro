import type { Account } from '../../accounts/types/accounts.types';
import type { Category } from '../../categories/types/categories.types';

export type TransactionType = 'INCOME' | 'EXPENSE';

export type TransactionStatus = 'PENDING' | 'PAID' | 'RECEIVED' | 'CANCELED';

export type Transaction = {
  id: string;
  description: string;
  amount: number;
  type: TransactionType;
  transactionDate: string;
  categoryId: string;
  accountId?: string | null;
  status: TransactionStatus;
  notes?: string | null;
  createdAt: string;
  updatedAt: string;
};

export type TransactionPayload = {
  description: string;
  amount: number;
  type: TransactionType;
  transactionDate: string;
  categoryId: string;
  accountId?: string | null;
  status: TransactionStatus;
  notes?: string | null;
};

export type MarkTransactionAsPaidPayload = {
  accountId?: string | null;
  paidDate?: string | null;
};

export type TransactionFilters = {
  accountId?: string;
  categoryId?: string;
  endDate?: string;
  page: number;
  startDate?: string;
  status?: TransactionStatus;
  type?: TransactionType;
};

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type TransactionLookup = {
  accounts: Account[];
  categories: Category[];
};
