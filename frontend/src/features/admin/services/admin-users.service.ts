import { apiClient } from '../../../shared/lib/api-client';
import type { AdminUser, AdminUserCommand, AdminUserDetails, PageResponse } from '../types/admin.types';

const actionPaths: Record<AdminUserCommand, string> = {
  approve: 'approve',
  delete: '',
  reactivate: 'reactivate',
  reject: 'reject',
  suspend: 'suspend',
};

export const adminUsersService = {
  async listUsers(page = 0, size = 20) {
    const { data } = await apiClient.get<PageResponse<AdminUser>>('/admin/users', {
      params: { page, size },
    });
    return data;
  },

  async listPendingUsers(page = 0, size = 20) {
    const { data } = await apiClient.get<PageResponse<AdminUser>>('/admin/users/pending', {
      params: { page, size },
    });
    return data;
  },

  async getUser(userId: string) {
    const { data } = await apiClient.get<AdminUserDetails>(`/admin/users/${userId}`);
    return data;
  },

  async changeStatus(userId: string, command: AdminUserCommand, reason?: string) {
    if (command === 'delete') {
      await apiClient.delete(`/admin/users/${userId}`, {
        data: reason ? { reason } : undefined,
      });
      return null;
    }

    const { data } = await apiClient.patch<AdminUser>(`/admin/users/${userId}/${actionPaths[command]}`, reason ? { reason } : undefined);
    return data;
  },
};
