import { createContext } from 'react';

import type { ForgotPasswordRequest, LoginRequest, RegisterRequest } from '../types/auth.types';

export type AuthStatus = 'checking' | 'authenticated' | 'anonymous';

export type AuthContextValue = {
  forgotPassword: (payload: ForgotPasswordRequest) => Promise<void>;
  isAuthenticated: boolean;
  login: (payload: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
  register: (payload: RegisterRequest) => Promise<void>;
  restoreSession: () => Promise<void>;
  status: AuthStatus;
};

export const AuthContext = createContext<AuthContextValue | null>(null);
