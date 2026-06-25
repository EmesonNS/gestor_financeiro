import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';

import { clearAuthTokens, getAccessToken, getRefreshToken, setAuthTokens } from './auth-token-store';

type RetriableRequestConfig = InternalAxiosRequestConfig & {
  _retry?: boolean;
};

type RefreshTokenResponse = {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
};

const baseURL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api';

const refreshClient = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const apiClient = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use((config) => {
  const token = getAccessToken();

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as RetriableRequestConfig | undefined;
    const refreshToken = getRefreshToken();

    if (error.response?.status !== 401 || !originalRequest || originalRequest._retry || !refreshToken) {
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    try {
      const { data } = await refreshClient.post<RefreshTokenResponse>('/auth/refresh', { refreshToken });
      setAuthTokens(data);
      originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;
      return apiClient(originalRequest);
    } catch (refreshError) {
      clearAuthTokens();

      if (!window.location.pathname.startsWith('/login')) {
        window.location.assign('/login');
      }

      return Promise.reject(refreshError);
    }
  },
);
