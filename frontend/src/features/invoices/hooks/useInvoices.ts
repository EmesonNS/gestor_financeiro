import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { accountKeys } from '../../accounts/hooks/useAccounts';
import { creditCardKeys } from '../../credit-cards/hooks/useCreditCards';
import { dashboardKeys } from '../../dashboard/hooks/useDashboard';
import { invoicesService } from '../services/invoices.service';
import type { InvoiceFilters, PayInvoicePayload } from '../types/invoices.types';

export const invoiceKeys = {
  all: ['invoices'] as const,
  current: (cardId: string) => [...invoiceKeys.all, 'current', cardId] as const,
  detail: (invoiceId: string) => [...invoiceKeys.all, 'detail', invoiceId] as const,
  list: (cardId: string, filters: InvoiceFilters) => [...invoiceKeys.all, 'list', cardId, filters] as const,
};

async function invalidateInvoiceState(queryClient: ReturnType<typeof useQueryClient>) {
  await queryClient.invalidateQueries({ queryKey: invoiceKeys.all });
  await queryClient.invalidateQueries({ queryKey: creditCardKeys.all });
  await queryClient.invalidateQueries({ queryKey: accountKeys.all });
  await queryClient.invalidateQueries({ queryKey: dashboardKeys.all });
}

export function useCurrentInvoice(cardId?: string) {
  return useQuery({
    enabled: Boolean(cardId),
    queryKey: invoiceKeys.current(cardId ?? ''),
    queryFn: () => invoicesService.getCurrentInvoice(cardId ?? ''),
  });
}

export function useInvoice(invoiceId?: string) {
  return useQuery({
    enabled: Boolean(invoiceId),
    queryKey: invoiceKeys.detail(invoiceId ?? ''),
    queryFn: () => invoicesService.getInvoice(invoiceId ?? ''),
  });
}

export function useInvoices(cardId: string | undefined, filters: InvoiceFilters) {
  return useQuery({
    enabled: Boolean(cardId),
    queryKey: invoiceKeys.list(cardId ?? '', filters),
    queryFn: () => invoicesService.listInvoices(cardId ?? '', filters),
  });
}

export function usePayInvoice() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ invoiceId, payload }: { invoiceId: string; payload: PayInvoicePayload }) => invoicesService.payInvoice(invoiceId, payload),
    onSuccess: async (_invoice, variables) => {
      await invalidateInvoiceState(queryClient);
      await queryClient.invalidateQueries({ queryKey: invoiceKeys.detail(variables.invoiceId) });
    },
  });
}
