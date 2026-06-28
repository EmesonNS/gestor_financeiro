import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { accountsService } from '../services/accounts.service';
import type { AccountFilters, CreateAccountRequest, UpdateAccountRequest } from '../types/accounts.types';

export const accountKeys = {
  all: ['accounts'] as const,
  detail: (accountId: string) => [...accountKeys.all, 'detail', accountId] as const,
  list: (filters: AccountFilters) => [...accountKeys.all, 'list', filters] as const,
};

export function useAccounts(filters: AccountFilters) {
  return useQuery({
    queryKey: accountKeys.list(filters),
    queryFn: () => accountsService.listAccounts(filters),
  });
}

export function useAccount(accountId?: string) {
  return useQuery({
    enabled: Boolean(accountId),
    queryKey: accountKeys.detail(accountId ?? ''),
    queryFn: () => accountsService.getAccount(accountId ?? ''),
  });
}

export function useCreateAccount() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: CreateAccountRequest) => accountsService.createAccount(payload),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: accountKeys.all });
    },
  });
}

export function useUpdateAccount(accountId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: UpdateAccountRequest) => accountsService.updateAccount(accountId, payload),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: accountKeys.all });
      await queryClient.invalidateQueries({ queryKey: accountKeys.detail(accountId) });
    },
  });
}

export function useArchiveAccount() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: accountsService.archiveAccount,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: accountKeys.all });
    },
  });
}

export function useDeleteAccount() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: accountsService.deleteAccount,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: accountKeys.all });
    },
  });
}
