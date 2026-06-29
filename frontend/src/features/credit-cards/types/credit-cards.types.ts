export type CreditCard = {
  archived: boolean;
  availableLimit: number;
  closingDay: number;
  createdAt: string;
  dueDay: number;
  id: string;
  limitAmount: number;
  name: string;
  updatedAt: string;
  usedLimit: number;
};

export type CreditCardPayload = {
  closingDay: number;
  dueDay: number;
  limitAmount: number;
  name: string;
};

export type CreditCardFilters = {
  archived?: boolean;
  page: number;
};

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};
