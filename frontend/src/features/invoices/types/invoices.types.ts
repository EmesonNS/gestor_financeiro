export type InvoiceStatus = 'OPEN' | 'CLOSED' | 'PAID' | 'OVERDUE';

export type Invoice = {
  closingDate: string;
  createdAt: string;
  creditCardId: string;
  dueDate: string;
  id: string;
  paidAt?: string | null;
  paymentAccountId?: string | null;
  referenceMonth: number;
  referenceYear: number;
  status: InvoiceStatus;
  totalAmount: number;
  updatedAt: string;
};

export type InvoiceFilters = {
  page: number;
  status?: InvoiceStatus;
  year?: number;
};

export type PayInvoicePayload = {
  paidAt: string;
  paymentAccountId: string;
};

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};
