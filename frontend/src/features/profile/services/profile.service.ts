import { apiClient } from '../../../shared/lib/api-client';
import type { ChangePasswordRequest, UpdateProfileRequest, UserProfile } from '../types/profile.types';

export const profileService = {
  async getProfile() {
    const { data } = await apiClient.get<UserProfile>('/users/me');
    return data;
  },

  async updateProfile(payload: UpdateProfileRequest) {
    const { data } = await apiClient.put<UserProfile>('/users/me', payload);
    return data;
  },

  async changePassword(payload: ChangePasswordRequest) {
    await apiClient.put('/users/me/password', payload);
  },
};
