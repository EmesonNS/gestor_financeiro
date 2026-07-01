import { DEFAULT_PAGE_SIZE } from '../../../shared/config/pagination';
import { apiClient } from '../../../shared/lib/api-client';
import type {
  CardPurchase,
  CardPurchasePayload,
  FutureInstallmentFilters,
  Installment,
  InstallmentFilters,
  PageResponse,
  PurchaseFilters,
} from '../types/installments.types';

export const installmentsService = {
  async createPurchase(cardId: string, payload: CardPurchasePayload) {
    const { data } = await apiClient.post<CardPurchase>(`/credit-cards/${cardId}/purchases`, payload);
    return data;
  },

  async deletePurchase(purchaseId: string) {
    await apiClient.delete(`/card-purchases/${purchaseId}`);
  },

  async getPurchase(purchaseId: string) {
    const { data } = await apiClient.get<CardPurchase>(`/card-purchases/${purchaseId}`);
    return data;
  },

  async listFutureInstallments(filters: FutureInstallmentFilters) {
    const { data } = await apiClient.get<PageResponse<Installment>>('/installments/future', {
      params: {
        cardId: filters.cardId,
        fromMonth: filters.fromMonth,
        fromYear: filters.fromYear,
        page: filters.page,
        size: DEFAULT_PAGE_SIZE,
        toMonth: filters.toMonth,
        toYear: filters.toYear,
      },
    });
    return data;
  },

  async listInstallments(filters: InstallmentFilters) {
    const { data } = await apiClient.get<PageResponse<Installment>>('/installments', {
      params: {
        cardId: filters.cardId,
        month: filters.month,
        page: filters.page,
        size: DEFAULT_PAGE_SIZE,
        status: filters.status,
        year: filters.year,
      },
    });
    return data;
  },

  async listPurchaseInstallments(purchaseId: string, page: number) {
    const { data } = await apiClient.get<PageResponse<Installment>>(`/card-purchases/${purchaseId}/installments`, {
      params: {
        page,
        size: DEFAULT_PAGE_SIZE,
      },
    });
    return data;
  },

  async listPurchases(cardId: string, filters: PurchaseFilters) {
    const { data } = await apiClient.get<PageResponse<CardPurchase>>(`/credit-cards/${cardId}/purchases`, {
      params: {
        endDate: filters.endDate,
        page: filters.page,
        size: DEFAULT_PAGE_SIZE,
        startDate: filters.startDate,
        status: filters.status,
      },
    });
    return data;
  },

  async updatePurchase(purchaseId: string, payload: CardPurchasePayload) {
    const { data } = await apiClient.put<CardPurchase>(`/card-purchases/${purchaseId}`, payload);
    return data;
  },
};
