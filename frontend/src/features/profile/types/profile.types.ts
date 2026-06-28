import type { AccountStatus } from '../../auth/types/auth.types';

export type UserProfile = {
  id: string;
  name: string;
  email: string;
  createdAt: string;
  status?: AccountStatus;
  message?: string;
};

export type UpdateProfileRequest = {
  name: string;
};

export type ChangePasswordRequest = {
  currentPassword: string;
  newPassword: string;
};
