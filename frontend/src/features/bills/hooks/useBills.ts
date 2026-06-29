import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { accountKeys } from '../../accounts/hooks/useAccounts';
import { dashboardKeys } from '../../dashboard/hooks/useDashboard';
import { transactionKeys } from '../../transactions/hooks/useTransactions';
import { billsService } from '../services/bills.service';
import type { BillFilters, BillPayload, PayBillPayload } from '../types/bills.types';

export const billKeys = {
  all: ['bills'] as const,
  detail: (billId: string) => [...billKeys.all, 'detail', billId] as const,
  list: (filters: BillFilters) => [...billKeys.all, 'list', filters] as const,
};

async function invalidateBillState(queryClient: ReturnType<typeof useQueryClient>) {
  await queryClient.invalidateQueries({ queryKey: billKeys.all });
  await queryClient.invalidateQueries({ queryKey: accountKeys.all });
  await queryClient.invalidateQueries({ queryKey: transactionKeys.all });
  await queryClient.invalidateQueries({ queryKey: dashboardKeys.all });
}

export function useBills(filters: BillFilters) {
  return useQuery({
    queryKey: billKeys.list(filters),
    queryFn: () => billsService.listBills(filters),
  });
}

export function useBill(billId?: string) {
  return useQuery({
    enabled: Boolean(billId),
    queryKey: billKeys.detail(billId ?? ''),
    queryFn: () => billsService.getBill(billId ?? ''),
  });
}

export function useCreateBill() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: BillPayload) => billsService.createBill(payload),
    onSuccess: async () => {
      await invalidateBillState(queryClient);
    },
  });
}

export function useUpdateBill(billId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: BillPayload) => billsService.updateBill(billId, payload),
    onSuccess: async () => {
      await invalidateBillState(queryClient);
      await queryClient.invalidateQueries({ queryKey: billKeys.detail(billId) });
    },
  });
}

export function useDeleteBill() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: billsService.deleteBill,
    onSuccess: async () => {
      await invalidateBillState(queryClient);
    },
  });
}

export function usePayBill() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ billId, payload }: { billId: string; payload: PayBillPayload }) => billsService.payBill(billId, payload),
    onSuccess: async () => {
      await invalidateBillState(queryClient);
    },
  });
}
