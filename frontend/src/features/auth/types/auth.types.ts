import type { AuthSessionUser } from '../../../shared/types/auth-session';

export type AccountStatus = 'PENDING_APPROVAL' | 'APPROVED' | 'REJECTED' | 'SUSPENDED' | 'DELETED';

export type UserRole = AuthSessionUser['role'];

export type AuthenticatedUser = AuthSessionUser;

export type AuthTokenResponse = {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  user?: AuthenticatedUser;
};

export type LoginRequest = {
  email: string;
  password: string;
};

export type RegisterRequest = {
  name: string;
  email: string;
  password: string;
};

export type ForgotPasswordRequest = {
  email: string;
};

export type UserResponse = {
  id: string;
  name: string;
  email: string;
  createdAt?: string;
  status: AccountStatus;
  message: string;
};

export type ApiErrorResponse = {
  timestamp?: string;
  status: number;
  error?: string;
  message?: string;
  path?: string;
  code?: string;
  userStatus?: AccountStatus;
  details?: string[];
};
