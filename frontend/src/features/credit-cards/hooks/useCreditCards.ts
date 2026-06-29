import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { dashboardKeys } from '../../dashboard/hooks/useDashboard';
import { creditCardsService } from '../services/credit-cards.service';
import type { CreditCardFilters, CreditCardPayload } from '../types/credit-cards.types';

export const creditCardKeys = {
  all: ['credit-cards'] as const,
  detail: (cardId: string) => [...creditCardKeys.all, 'detail', cardId] as const,
  list: (filters: CreditCardFilters) => [...creditCardKeys.all, 'list', filters] as const,
};

async function invalidateCreditCardState(queryClient: ReturnType<typeof useQueryClient>) {
  await queryClient.invalidateQueries({ queryKey: creditCardKeys.all });
  await queryClient.invalidateQueries({ queryKey: dashboardKeys.all });
}

export function useCreditCards(filters: CreditCardFilters) {
  return useQuery({
    queryKey: creditCardKeys.list(filters),
    queryFn: () => creditCardsService.listCreditCards(filters),
  });
}

export function useCreditCard(cardId?: string) {
  return useQuery({
    enabled: Boolean(cardId),
    queryKey: creditCardKeys.detail(cardId ?? ''),
    queryFn: () => creditCardsService.getCreditCard(cardId ?? ''),
  });
}

export function useCreateCreditCard() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: CreditCardPayload) => creditCardsService.createCreditCard(payload),
    onSuccess: async () => {
      await invalidateCreditCardState(queryClient);
    },
  });
}

export function useUpdateCreditCard(cardId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: CreditCardPayload) => creditCardsService.updateCreditCard(cardId, payload),
    onSuccess: async () => {
      await invalidateCreditCardState(queryClient);
      await queryClient.invalidateQueries({ queryKey: creditCardKeys.detail(cardId) });
    },
  });
}

export function useArchiveCreditCard() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: creditCardsService.archiveCreditCard,
    onSuccess: async () => {
      await invalidateCreditCardState(queryClient);
    },
  });
}

export function useDeleteCreditCard() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: creditCardsService.deleteCreditCard,
    onSuccess: async () => {
      await invalidateCreditCardState(queryClient);
    },
  });
}
