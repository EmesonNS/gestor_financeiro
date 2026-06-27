import type { AccountStatus } from '../types/auth.types';

export function accountStatusPath(status?: AccountStatus) {
  switch (status) {
    case 'PENDING_APPROVAL':
      return '/account-status/pending';
    case 'SUSPENDED':
      return '/account-status/suspended';
    case 'REJECTED':
      return '/account-status/rejected';
    case 'DELETED':
      return '/account-status/unavailable';
    default:
      return null;
  }
}
