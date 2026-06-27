import { useMutation } from '@tanstack/react-query';
import { useCallback, useMemo, useState, type PropsWithChildren } from 'react';

import { clearAuthTokens, getAccessToken, getAuthUser, getRefreshToken } from '../../../shared/lib/auth-token-store';
import { AuthContext, type AuthContextValue, type AuthStatus } from '../contexts/auth-context';
import { authService } from '../services/auth.service';
import type { AuthenticatedUser, ForgotPasswordRequest, LoginRequest, RegisterRequest } from '../types/auth.types';

export function AuthProvider({ children }: PropsWithChildren) {
  const [user, setUser] = useState<AuthenticatedUser | null>(() => getAuthUser());
  const [status, setStatus] = useState<AuthStatus>(() => (getAccessToken() || getRefreshToken() ? 'checking' : 'anonymous'));

  const loginMutation = useMutation({ mutationFn: authService.login });
  const registerMutation = useMutation({ mutationFn: authService.register });
  const forgotPasswordMutation = useMutation({ mutationFn: authService.forgotPassword });

  const forgotPassword = useCallback(async (payload: ForgotPasswordRequest) => {
    await forgotPasswordMutation.mutateAsync(payload);
  }, [forgotPasswordMutation]);

  const login = useCallback(async (payload: LoginRequest) => {
    const response = await loginMutation.mutateAsync(payload);
    setUser(response.user ?? getAuthUser());
    setStatus('authenticated');
  }, [loginMutation]);

  const logout = useCallback(async () => {
    try {
      await authService.logout();
    } finally {
      clearAuthTokens();
      setUser(null);
      setStatus('anonymous');
    }
  }, []);

  const register = useCallback(async (payload: RegisterRequest) => {
    const response = await registerMutation.mutateAsync(payload);
    clearAuthTokens();
    setUser(null);
    setStatus('anonymous');
    return response;
  }, [registerMutation]);

  const restoreSession = useCallback(async () => {
    if (!getRefreshToken()) {
      clearAuthTokens();
      setUser(null);
      setStatus('anonymous');
      return;
    }

    setStatus('checking');

    try {
      await authService.refreshSession();
      setUser(getAuthUser());
      setStatus('authenticated');
    } catch {
      clearAuthTokens();
      setUser(null);
      setStatus('anonymous');
    }
  }, []);

  const value = useMemo<AuthContextValue>(() => ({
    forgotPassword,
    isAdmin: user?.role === 'ADMIN',
    isAuthenticated: status === 'authenticated',
    login,
    logout,
    register,
    restoreSession,
    status,
    user,
  }), [forgotPassword, login, logout, register, restoreSession, status, user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
