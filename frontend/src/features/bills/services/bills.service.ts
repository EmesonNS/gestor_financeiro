import { DEFAULT_PAGE_SIZE } from '../../../shared/config/pagination';
import { apiClient } from '../../../shared/lib/api-client';
import type { Bill, BillFilters, BillPayload, PageResponse, PayBillPayload } from '../types/bills.types';

export const billsService = {
  async listBills(filters: BillFilters) {
    const { data } = await apiClient.get<PageResponse<Bill>>('/bills', {
      params: {
        accountId: filters.accountId,
        categoryId: filters.categoryId,
        endDueDate: filters.endDueDate,
        overdue: filters.overdue,
        page: filters.page,
        size: DEFAULT_PAGE_SIZE,
        startDueDate: filters.startDueDate,
        status: filters.status,
      },
    });
    return data;
  },

  async getBill(billId: string) {
    const { data } = await apiClient.get<Bill>(`/bills/${billId}`);
    return data;
  },

  async createBill(payload: BillPayload) {
    const { data } = await apiClient.post<Bill>('/bills', payload);
    return data;
  },

  async updateBill(billId: string, payload: BillPayload) {
    const { data } = await apiClient.put<Bill>(`/bills/${billId}`, payload);
    return data;
  },

  async deleteBill(billId: string) {
    await apiClient.delete(`/bills/${billId}`);
  },

  async payBill(billId: string, payload: PayBillPayload) {
    const { data } = await apiClient.patch<Bill>(`/bills/${billId}/pay`, payload);
    return data;
  },
};
