import type { AccountType } from '../../accounts/types/accounts.types';
import type { InstallmentStatus } from '../../installments/types/installments.types';
import type { TransactionStatus, TransactionType } from '../../transactions/types/transactions.types';

export type ReportType =
  | 'transactions'
  | 'expenses-by-category'
  | 'monthly-evolution'
  | 'accounts-balance'
  | 'budget-vs-actual'
  | 'credit-card-expenses'
  | 'future-installments';

export type ReportFilters = {
  accountId?: string;
  cardId?: string;
  categoryId?: string;
  date?: string;
  endDate?: string;
  fromMonth?: number;
  fromYear?: number;
  month?: number;
  page: number;
  startDate?: string;
  toMonth?: number;
  toYear?: number;
  type?: TransactionType;
  year?: number;
};

export type TransactionReportRow = {
  accountId: string;
  accountName: string;
  amount: number;
  categoryId: string;
  categoryName: string;
  description: string;
  status: TransactionStatus;
  transactionDate: string;
  transactionId: string;
  type: TransactionType;
};

export type ExpenseByCategoryReportRow = {
  categoryId: string;
  categoryName: string;
  percentage: number;
  totalAmount: number;
};

export type MonthlyEvolutionReportRow = {
  balance: number;
  expense: number;
  income: number;
  month: number;
};

export type AccountBalanceReportRow = {
  accountId: string;
  accountName: string;
  accountType: AccountType;
  balance: number;
};

export type BudgetVsActualReportRow = {
  actualAmount: number;
  budgetId: string;
  categoryId: string;
  categoryName: string;
  exceeded: boolean;
  percentageUsed: number;
  plannedAmount: number;
  remainingAmount: number;
};

export type CreditCardExpenseReportRow = {
  cardId: string;
  cardName: string;
  categoryId: string;
  categoryName: string;
  totalAmount: number;
};

export type FutureInstallmentReportRow = {
  amount: number;
  cardId: string;
  cardName: string;
  competenceMonth: number;
  competenceYear: number;
  description: string;
  installmentId: string;
  installmentNumber: number;
  purchaseId: string;
  status: InstallmentStatus;
  totalInstallments: number;
};

export type ReportRow =
  | TransactionReportRow
  | ExpenseByCategoryReportRow
  | MonthlyEvolutionReportRow
  | AccountBalanceReportRow
  | BudgetVsActualReportRow
  | CreditCardExpenseReportRow
  | FutureInstallmentReportRow;

export type ReportPage = PageResponse<ReportRow>;

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};
