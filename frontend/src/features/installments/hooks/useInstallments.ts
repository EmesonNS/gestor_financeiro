import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { creditCardKeys } from '../../credit-cards/hooks/useCreditCards';
import { dashboardKeys } from '../../dashboard/hooks/useDashboard';
import { invoiceKeys } from '../../invoices/hooks/useInvoices';
import { installmentsService } from '../services/installments.service';
import type { CardPurchasePayload, FutureInstallmentFilters, InstallmentFilters, PurchaseFilters } from '../types/installments.types';

export const installmentKeys = {
  all: ['installments'] as const,
  future: (filters: FutureInstallmentFilters) => [...installmentKeys.all, 'future', filters] as const,
  installments: (filters: InstallmentFilters) => [...installmentKeys.all, 'installments', filters] as const,
  purchaseDetail: (purchaseId: string) => [...installmentKeys.all, 'purchase-detail', purchaseId] as const,
  purchaseInstallments: (purchaseId: string, page: number) => [...installmentKeys.all, 'purchase-installments', purchaseId, page] as const,
  purchases: (cardId: string, filters: PurchaseFilters) => [...installmentKeys.all, 'purchases', cardId, filters] as const,
};

async function invalidateInstallmentState(queryClient: ReturnType<typeof useQueryClient>) {
  await queryClient.invalidateQueries({ queryKey: installmentKeys.all });
  await queryClient.invalidateQueries({ queryKey: invoiceKeys.all });
  await queryClient.invalidateQueries({ queryKey: creditCardKeys.all });
  await queryClient.invalidateQueries({ queryKey: dashboardKeys.all });
}

export function useCardPurchases(cardId: string | undefined, filters: PurchaseFilters) {
  return useQuery({
    enabled: Boolean(cardId),
    queryKey: installmentKeys.purchases(cardId ?? '', filters),
    queryFn: () => installmentsService.listPurchases(cardId ?? '', filters),
  });
}

export function useCardPurchase(purchaseId?: string) {
  return useQuery({
    enabled: Boolean(purchaseId),
    queryKey: installmentKeys.purchaseDetail(purchaseId ?? ''),
    queryFn: () => installmentsService.getPurchase(purchaseId ?? ''),
  });
}

export function useInstallments(filters: InstallmentFilters) {
  return useQuery({
    queryKey: installmentKeys.installments(filters),
    queryFn: () => installmentsService.listInstallments(filters),
  });
}

export function useFutureInstallments(filters: FutureInstallmentFilters) {
  return useQuery({
    queryKey: installmentKeys.future(filters),
    queryFn: () => installmentsService.listFutureInstallments(filters),
  });
}

export function usePurchaseInstallments(purchaseId: string | undefined, page: number) {
  return useQuery({
    enabled: Boolean(purchaseId),
    queryKey: installmentKeys.purchaseInstallments(purchaseId ?? '', page),
    queryFn: () => installmentsService.listPurchaseInstallments(purchaseId ?? '', page),
  });
}

export function useCreateCardPurchase(cardId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: CardPurchasePayload) => installmentsService.createPurchase(cardId, payload),
    onSuccess: async () => {
      await invalidateInstallmentState(queryClient);
    },
  });
}

export function useUpdateCardPurchase(purchaseId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: CardPurchasePayload) => installmentsService.updatePurchase(purchaseId, payload),
    onSuccess: async () => {
      await invalidateInstallmentState(queryClient);
      await queryClient.invalidateQueries({ queryKey: installmentKeys.purchaseDetail(purchaseId) });
    },
  });
}

export function useDeleteCardPurchase() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: installmentsService.deletePurchase,
    onSuccess: async () => {
      await invalidateInstallmentState(queryClient);
    },
  });
}
