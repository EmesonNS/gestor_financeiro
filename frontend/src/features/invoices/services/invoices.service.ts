import { DEFAULT_PAGE_SIZE } from '../../../shared/config/pagination';
import { apiClient } from '../../../shared/lib/api-client';
import type { Invoice, InvoiceFilters, PageResponse, PayInvoicePayload } from '../types/invoices.types';

export const invoicesService = {
  async getCurrentInvoice(cardId: string) {
    const { data } = await apiClient.get<Invoice>(`/credit-cards/${cardId}/invoices/current`);
    return data;
  },

  async getInvoice(invoiceId: string) {
    const { data } = await apiClient.get<Invoice>(`/invoices/${invoiceId}`);
    return data;
  },

  async listInvoices(cardId: string, filters: InvoiceFilters) {
    const { data } = await apiClient.get<PageResponse<Invoice>>(`/credit-cards/${cardId}/invoices`, {
      params: {
        page: filters.page,
        size: DEFAULT_PAGE_SIZE,
        status: filters.status,
        year: filters.year,
      },
    });
    return data;
  },

  async payInvoice(invoiceId: string, payload: PayInvoicePayload) {
    const { data } = await apiClient.patch<Invoice>(`/invoices/${invoiceId}/pay`, payload);
    return data;
  },
};
