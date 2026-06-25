export type AuthTokenResponse = {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
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
};
