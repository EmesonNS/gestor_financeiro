import { useMutation } from '@tanstack/react-query';
import { useCallback, useMemo, useState, type PropsWithChildren } from 'react';

import { clearAuthTokens, getAccessToken, getRefreshToken } from '../../../shared/lib/auth-token-store';
import { AuthContext, type AuthContextValue, type AuthStatus } from '../contexts/auth-context';
import { authService } from '../services/auth.service';
import type { ForgotPasswordRequest, LoginRequest, RegisterRequest } from '../types/auth.types';

export function AuthProvider({ children }: PropsWithChildren) {
  const [status, setStatus] = useState<AuthStatus>(() => (getAccessToken() ? 'authenticated' : getRefreshToken() ? 'checking' : 'anonymous'));

  const loginMutation = useMutation({ mutationFn: authService.login, onSuccess: () => setStatus('authenticated') });
  const registerMutation = useMutation({ mutationFn: authService.register });
  const forgotPasswordMutation = useMutation({ mutationFn: authService.forgotPassword });

  const forgotPassword = useCallback(async (payload: ForgotPasswordRequest) => {
    await forgotPasswordMutation.mutateAsync(payload);
  }, [forgotPasswordMutation]);

  const login = useCallback(async (payload: LoginRequest) => {
    await loginMutation.mutateAsync(payload);
  }, [loginMutation]);

  const logout = useCallback(async () => {
    try {
      await authService.logout();
    } finally {
      clearAuthTokens();
      setStatus('anonymous');
    }
  }, []);

  const register = useCallback(async (payload: RegisterRequest) => {
    await registerMutation.mutateAsync(payload);
  }, [registerMutation]);

  const restoreSession = useCallback(async () => {
    if (getAccessToken()) {
      setStatus('authenticated');
      return;
    }

    if (!getRefreshToken()) {
      setStatus('anonymous');
      return;
    }

    setStatus('checking');

    try {
      await authService.refreshSession();
      setStatus('authenticated');
    } catch {
      clearAuthTokens();
      setStatus('anonymous');
    }
  }, []);

  const value = useMemo<AuthContextValue>(() => ({
    forgotPassword,
    isAuthenticated: status === 'authenticated',
    login,
    logout,
    register,
    restoreSession,
    status,
  }), [forgotPassword, login, logout, register, restoreSession, status]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
