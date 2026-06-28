export type DashboardPeriod = {
  month: number;
  year: number;
};

export type DashboardSummary = {
  totalBalance: number;
  monthlyIncome: number;
  monthlyExpense: number;
  monthlyBalance: number;
  expectedBalance: number;
  openInvoicesTotal: number;
  currentInvoiceAmount: number;
  creditLimitUsed: number;
};

export type DashboardMonthly = {
  month: number;
  year: number;
  income: number;
  expense: number;
  balance: number;
  expectedBalance: number;
};

export type ExpenseByCategory = {
  categoryId: string;
  categoryName: string;
  amount: number;
};

export type IncomeExpenseMonthly = {
  month: number;
  income: number;
  expense: number;
  balance: number;
};

export type DashboardChartFilters = {
  page: number;
  period: DashboardPeriod;
};

export type IncomeExpenseFilters = {
  page: number;
  year: number;
};

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};
