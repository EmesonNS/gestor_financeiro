export type PurchaseStatus = 'ACTIVE' | 'CANCELED';
export type InstallmentStatus = 'OPEN' | 'PAID' | 'CANCELED';

export type Installment = {
  amount: number;
  competenceMonth: number;
  competenceYear: number;
  createdAt: string;
  id: string;
  installmentNumber: number;
  invoiceId: string;
  purchaseId: string;
  status: InstallmentStatus;
  totalInstallments: number;
  updatedAt: string;
};

export type CardPurchase = {
  categoryId: string;
  createdAt: string;
  creditCardId: string;
  description: string;
  id: string;
  installmentCount: number;
  installments: Installment[];
  notes?: string | null;
  purchaseDate: string;
  status: PurchaseStatus;
  totalAmount: number;
  updatedAt: string;
};

export type CardPurchasePayload = {
  categoryId: string;
  description: string;
  installmentCount: number;
  notes?: string | null;
  purchaseDate: string;
  totalAmount: number;
};

export type PurchaseFilters = {
  endDate?: string;
  page: number;
  startDate?: string;
  status?: PurchaseStatus;
};

export type InstallmentFilters = {
  cardId?: string;
  month?: number;
  page: number;
  status?: InstallmentStatus;
  year?: number;
};

export type FutureInstallmentFilters = {
  cardId?: string;
  fromMonth?: number;
  fromYear?: number;
  page: number;
};

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};
