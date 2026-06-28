import type { AccountStatus } from '../../auth/types/auth.types';
import type { AdminUserAction, AdminUserCommand } from '../types/admin.types';

export const statusLabels: Record<AccountStatus, string> = {
  APPROVED: 'Aprovado',
  DELETED: 'Indisponivel',
  PENDING_APPROVAL: 'Pendente',
  REJECTED: 'Negado',
  SUSPENDED: 'Suspenso',
};

export const actionLabels: Record<AdminUserAction, string> = {
  APPROVED: 'Aprovou',
  DELETED: 'Desativou',
  REGISTERED: 'Registrou',
  REACTIVATED: 'Reativou',
  REJECTED: 'Negou',
  SUSPENDED: 'Suspendeu',
};

export const commandLabels: Record<AdminUserCommand, string> = {
  approve: 'Aprovar',
  delete: 'Desativar',
  reactivate: 'Reativar',
  reject: 'Negar',
  suspend: 'Suspender',
};

export function formatDateTime(value?: string | null) {
  if (!value) {
    return 'Nao registrado';
  }

  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(new Date(value));
}

export function isReasonRequired(command: AdminUserCommand) {
  return command === 'reject' || command === 'suspend' || command === 'delete';
}

export function availableCommands(status: AccountStatus): AdminUserCommand[] {
  switch (status) {
    case 'PENDING_APPROVAL':
      return ['approve', 'reject', 'delete'];
    case 'APPROVED':
      return ['suspend', 'delete'];
    case 'REJECTED':
    case 'SUSPENDED':
      return ['reactivate', 'delete'];
    case 'DELETED':
      return [];
    default:
      return [];
  }
}
