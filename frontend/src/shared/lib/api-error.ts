import { AxiosError } from 'axios';

import type { ApiErrorResponse } from '../../features/auth/types/auth.types';

const apiErrorMessages: Record<string, string> = {
  INSUFFICIENT_ACCOUNT_BALANCE: 'Saldo insuficiente na conta financeira selecionada.',
};

export function apiErrorMessage(error: unknown, fallback: string) {
  if (!(error instanceof AxiosError)) {
    return fallback;
  }

  const data = error.response?.data as ApiErrorResponse | undefined;

  if (data?.code && apiErrorMessages[data.code]) {
    return apiErrorMessages[data.code];
  }

  return data?.message ?? fallback;
}
