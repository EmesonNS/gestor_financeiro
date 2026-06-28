import { DEFAULT_PAGE_SIZE } from '../../../shared/config/pagination';
import { apiClient } from '../../../shared/lib/api-client';
import type {
  MarkTransactionAsPaidPayload,
  PageResponse,
  Transaction,
  TransactionFilters,
  TransactionPayload,
} from '../types/transactions.types';

export const transactionsService = {
  async listTransactions(filters: TransactionFilters) {
    const { data } = await apiClient.get<PageResponse<Transaction>>('/transactions', {
      params: {
        accountId: filters.accountId,
        categoryId: filters.categoryId,
        endDate: filters.endDate,
        page: filters.page,
        size: DEFAULT_PAGE_SIZE,
        startDate: filters.startDate,
        status: filters.status,
        type: filters.type,
      },
    });
    return data;
  },

  async getTransaction(transactionId: string) {
    const { data } = await apiClient.get<Transaction>(`/transactions/${transactionId}`);
    return data;
  },

  async createTransaction(payload: TransactionPayload) {
    const { data } = await apiClient.post<Transaction>('/transactions', payload);
    return data;
  },

  async updateTransaction(transactionId: string, payload: TransactionPayload) {
    const { data } = await apiClient.put<Transaction>(`/transactions/${transactionId}`, payload);
    return data;
  },

  async deleteTransaction(transactionId: string) {
    await apiClient.delete(`/transactions/${transactionId}`);
  },

  async markAsPaid(transactionId: string, payload?: MarkTransactionAsPaidPayload) {
    const { data } = await apiClient.patch<Transaction>(`/transactions/${transactionId}/mark-as-paid`, payload ?? {});
    return data;
  },

  async cancelTransaction(transactionId: string) {
    const { data } = await apiClient.patch<Transaction>(`/transactions/${transactionId}/cancel`);
    return data;
  },
};
