import { apiClient } from '../../../shared/lib/api-client';
import { DEFAULT_PAGE_SIZE } from '../../../shared/config/pagination';
import type { Account, AccountFilters, CreateAccountRequest, PageResponse, UpdateAccountRequest } from '../types/accounts.types';

export const accountsService = {
  async listAccounts(filters: AccountFilters) {
    const { data } = await apiClient.get<PageResponse<Account>>('/accounts', {
      params: {
        archived: filters.archived,
        page: filters.page,
        size: DEFAULT_PAGE_SIZE,
        type: filters.type,
      },
    });
    return data;
  },

  async getAccount(accountId: string) {
    const { data } = await apiClient.get<Account>(`/accounts/${accountId}`);
    return data;
  },

  async createAccount(payload: CreateAccountRequest) {
    const { data } = await apiClient.post<Account>('/accounts', payload);
    return data;
  },

  async updateAccount(accountId: string, payload: UpdateAccountRequest) {
    const { data } = await apiClient.put<Account>(`/accounts/${accountId}`, payload);
    return data;
  },

  async archiveAccount(accountId: string) {
    const { data } = await apiClient.patch<Account>(`/accounts/${accountId}/archive`);
    return data;
  },

  async deleteAccount(accountId: string) {
    await apiClient.delete(`/accounts/${accountId}`);
  },
};
