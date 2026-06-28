import type { AuthSessionUser } from '../types/auth-session';

const REFRESH_TOKEN_KEY = 'finance.refreshToken';
const AUTH_USER_KEY = 'finance.authUser';

let accessToken: string | null = null;
let authUser: AuthSessionUser | null = readStoredUser();

function readStoredUser() {
  const stored = window.sessionStorage.getItem(AUTH_USER_KEY);

  if (!stored) {
    return null;
  }

  try {
    return JSON.parse(stored) as AuthSessionUser;
  } catch {
    window.sessionStorage.removeItem(AUTH_USER_KEY);
    return null;
  }
}

export function getAccessToken() {
  return accessToken;
}

export function getAuthUser() {
  return authUser;
}

export function getRefreshToken() {
  return window.sessionStorage.getItem(REFRESH_TOKEN_KEY);
}

export function setAuthTokens(tokens: { accessToken: string; refreshToken: string; user?: AuthSessionUser }) {
  accessToken = tokens.accessToken;
  window.sessionStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken);

  if (tokens.user) {
    setAuthUser(tokens.user);
  }
}

export function setAuthUser(user: AuthSessionUser) {
  authUser = user;
  window.sessionStorage.setItem(AUTH_USER_KEY, JSON.stringify(user));
}

export function clearAuthTokens() {
  accessToken = null;
  authUser = null;
  window.sessionStorage.removeItem(REFRESH_TOKEN_KEY);
  window.sessionStorage.removeItem(AUTH_USER_KEY);
}
