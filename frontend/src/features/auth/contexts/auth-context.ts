import { createContext } from 'react';

import type { AuthenticatedUser, ForgotPasswordRequest, LoginRequest, RegisterRequest, UserResponse } from '../types/auth.types';

export type AuthStatus = 'checking' | 'authenticated' | 'anonymous';

export type AuthContextValue = {
  forgotPassword: (payload: ForgotPasswordRequest) => Promise<void>;
  isAdmin: boolean;
  isAuthenticated: boolean;
  login: (payload: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
  register: (payload: RegisterRequest) => Promise<UserResponse>;
  restoreSession: () => Promise<void>;
  status: AuthStatus;
  user: AuthenticatedUser | null;
};

export const AuthContext = createContext<AuthContextValue | null>(null);
