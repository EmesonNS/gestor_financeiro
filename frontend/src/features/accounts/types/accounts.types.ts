export type AccountType =
  | 'CHECKING_ACCOUNT'
  | 'SAVINGS_ACCOUNT'
  | 'CASH_WALLET'
  | 'DIGITAL_ACCOUNT'
  | 'INVESTMENT'
  | 'MEAL_VOUCHER'
  | 'OTHER';

export type Account = {
  id: string;
  name: string;
  type: AccountType;
  initialBalance: number;
  currentBalance: number;
  archived: boolean;
  createdAt: string;
  updatedAt: string;
};

export type CreateAccountRequest = {
  name: string;
  type: AccountType;
  initialBalance: number;
};

export type UpdateAccountRequest = {
  name: string;
  type: AccountType;
};

export type AccountFilters = {
  archived?: boolean;
  page: number;
  type?: AccountType;
};

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};
