import type { Account, AccountType } from '../types/accounts.types';

export const accountTypeLabels: Record<AccountType, string> = {
  CASH_WALLET: 'Carteira',
  CHECKING_ACCOUNT: 'Conta corrente',
  DIGITAL_ACCOUNT: 'Conta digital',
  INVESTMENT: 'Investimento',
  MEAL_VOUCHER: 'Vale refeicao',
  OTHER: 'Outra',
  SAVINGS_ACCOUNT: 'Poupanca',
};

export const accountTypes = Object.keys(accountTypeLabels) as AccountType[];

export function formatCurrency(value: number | string) {
  return new Intl.NumberFormat('pt-BR', {
    currency: 'BRL',
    style: 'currency',
  }).format(Number(value));
}

export function formatDateTime(value?: string | null) {
  if (!value) {
    return 'Nao registrado';
  }

  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(new Date(value));
}

export function sumCurrentBalance(accounts: Account[]) {
  return accounts.reduce((total, account) => total + Number(account.currentBalance), 0);
}
