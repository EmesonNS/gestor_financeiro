const REFRESH_TOKEN_KEY = 'finance.refreshToken';

let accessToken: string | null = null;

export function getAccessToken() {
  return accessToken;
}

export function getRefreshToken() {
  return window.sessionStorage.getItem(REFRESH_TOKEN_KEY);
}

export function setAuthTokens(tokens: { accessToken: string; refreshToken: string }) {
  accessToken = tokens.accessToken;
  window.sessionStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken);
}

export function clearAuthTokens() {
  accessToken = null;
  window.sessionStorage.removeItem(REFRESH_TOKEN_KEY);
}
