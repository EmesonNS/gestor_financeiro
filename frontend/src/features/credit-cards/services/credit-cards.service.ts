import { DEFAULT_PAGE_SIZE } from '../../../shared/config/pagination';
import { apiClient } from '../../../shared/lib/api-client';
import type { CreditCard, CreditCardFilters, CreditCardPayload, PageResponse } from '../types/credit-cards.types';

export const creditCardsService = {
  async archiveCreditCard(cardId: string) {
    const { data } = await apiClient.patch<CreditCard>(`/credit-cards/${cardId}/archive`);
    return data;
  },

  async createCreditCard(payload: CreditCardPayload) {
    const { data } = await apiClient.post<CreditCard>('/credit-cards', payload);
    return data;
  },

  async deleteCreditCard(cardId: string) {
    await apiClient.delete(`/credit-cards/${cardId}`);
  },

  async getCreditCard(cardId: string) {
    const { data } = await apiClient.get<CreditCard>(`/credit-cards/${cardId}`);
    return data;
  },

  async listCreditCards(filters: CreditCardFilters) {
    const { data } = await apiClient.get<PageResponse<CreditCard>>('/credit-cards', {
      params: {
        archived: filters.archived,
        page: filters.page,
        size: DEFAULT_PAGE_SIZE,
      },
    });
    return data;
  },

  async updateCreditCard(cardId: string, payload: CreditCardPayload) {
    const { data } = await apiClient.put<CreditCard>(`/credit-cards/${cardId}`, payload);
    return data;
  },
};
