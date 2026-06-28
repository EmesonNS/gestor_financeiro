import type { AccountStatus, UserRole } from '../../auth/types/auth.types';

export type AdminUser = {
  id: string;
  name: string;
  email: string;
  role: UserRole;
  status: AccountStatus;
  createdAt: string;
  approvedAt?: string | null;
  rejectedAt?: string | null;
  suspendedAt?: string | null;
};

export type UserStatusHistory = {
  id: string;
  adminUserId?: string | null;
  previousStatus?: AccountStatus | null;
  newStatus: AccountStatus;
  action: AdminUserAction;
  reason?: string | null;
  createdAt: string;
};

export type AdminUserDetails = AdminUser & {
  deletedAt?: string | null;
  statusHistory: UserStatusHistory[];
};

export type AdminUserAction = 'REGISTERED' | 'APPROVED' | 'REJECTED' | 'SUSPENDED' | 'REACTIVATED' | 'DELETED';

export type AdminUserCommand = 'approve' | 'reject' | 'suspend' | 'reactivate' | 'delete';

export type PageResponse<T> = {
  content: T[];
  number?: number;
  page?: number;
  size: number;
  totalElements: number;
  totalPages: number;
};
