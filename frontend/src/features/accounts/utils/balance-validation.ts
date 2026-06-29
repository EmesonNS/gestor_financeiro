import type { Account } from '../types/accounts.types';

export type PreviousBalanceImpact = {
  accountId?: string | null;
  amount: number;
  direction: 'credit' | 'debit' | 'none';
};

export function accountAvailableBalance(account: Account, previousImpact?: PreviousBalanceImpact) {
  const currentBalance = Number(account.currentBalance);

  if (!previousImpact || previousImpact.accountId !== account.id || previousImpact.direction === 'none') {
    return currentBalance;
  }

  if (previousImpact.direction === 'debit') {
    return currentBalance + Number(previousImpact.amount);
  }

  return currentBalance - Number(previousImpact.amount);
}

export function hasSufficientBalance(account: Account, amount: number, previousImpact?: PreviousBalanceImpact) {
  return accountAvailableBalance(account, previousImpact) >= Number(amount);
}

export function insufficientBalanceMessage(account: Account) {
  return `Saldo insuficiente na conta ${account.name}. Selecione outra conta ou ajuste o valor.`;
}
