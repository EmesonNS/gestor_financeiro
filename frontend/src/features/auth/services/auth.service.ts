import { apiClient } from '../../../shared/lib/api-client';
import { clearAuthTokens, getRefreshToken, setAuthTokens } from '../../../shared/lib/auth-token-store';
import type { AuthTokenResponse, ForgotPasswordRequest, LoginRequest, RegisterRequest, UserResponse } from '../types/auth.types';

export const authService = {
  async login(payload: LoginRequest) {
    const { data } = await apiClient.post<AuthTokenResponse>('/auth/login', payload);
    setAuthTokens(data);
    return data;
  },

  async register(payload: RegisterRequest) {
    const { data } = await apiClient.post<UserResponse>('/auth/register', payload);
    return data;
  },

  async forgotPassword(payload: ForgotPasswordRequest) {
    await apiClient.post('/auth/forgot-password', payload);
  },

  async refreshSession() {
    const refreshToken = getRefreshToken();

    if (!refreshToken) {
      return null;
    }

    const { data } = await apiClient.post<AuthTokenResponse>('/auth/refresh', { refreshToken });
    setAuthTokens(data);
    return data;
  },

  async logout() {
    const refreshToken = getRefreshToken();

    if (refreshToken) {
      await apiClient.post('/auth/logout', { refreshToken });
    }

    clearAuthTokens();
  },
};
