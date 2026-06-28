import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { accountKeys } from '../../accounts/hooks/useAccounts';
import { transactionsService } from '../services/transactions.service';
import type { MarkTransactionAsPaidPayload, TransactionFilters, TransactionPayload } from '../types/transactions.types';

export const transactionKeys = {
  all: ['transactions'] as const,
  detail: (transactionId: string) => [...transactionKeys.all, 'detail', transactionId] as const,
  list: (filters: TransactionFilters) => [...transactionKeys.all, 'list', filters] as const,
};

async function invalidateTransactionState(queryClient: ReturnType<typeof useQueryClient>) {
  await queryClient.invalidateQueries({ queryKey: transactionKeys.all });
  await queryClient.invalidateQueries({ queryKey: accountKeys.all });
}

export function useTransactions(filters: TransactionFilters) {
  return useQuery({
    queryKey: transactionKeys.list(filters),
    queryFn: () => transactionsService.listTransactions(filters),
  });
}

export function useTransaction(transactionId?: string) {
  return useQuery({
    enabled: Boolean(transactionId),
    queryKey: transactionKeys.detail(transactionId ?? ''),
    queryFn: () => transactionsService.getTransaction(transactionId ?? ''),
  });
}

export function useCreateTransaction() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: TransactionPayload) => transactionsService.createTransaction(payload),
    onSuccess: async () => {
      await invalidateTransactionState(queryClient);
    },
  });
}

export function useUpdateTransaction(transactionId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: TransactionPayload) => transactionsService.updateTransaction(transactionId, payload),
    onSuccess: async () => {
      await invalidateTransactionState(queryClient);
      await queryClient.invalidateQueries({ queryKey: transactionKeys.detail(transactionId) });
    },
  });
}

export function useDeleteTransaction() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: transactionsService.deleteTransaction,
    onSuccess: async () => {
      await invalidateTransactionState(queryClient);
    },
  });
}

export function useMarkTransactionAsPaid() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ payload, transactionId }: { payload?: MarkTransactionAsPaidPayload; transactionId: string }) => transactionsService.markAsPaid(transactionId, payload),
    onSuccess: async () => {
      await invalidateTransactionState(queryClient);
    },
  });
}

export function useCancelTransaction() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: transactionsService.cancelTransaction,
    onSuccess: async () => {
      await invalidateTransactionState(queryClient);
    },
  });
}
