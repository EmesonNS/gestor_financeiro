import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { adminUsersService } from '../services/admin-users.service';
import type { AdminUserCommand } from '../types/admin.types';

export const adminUserKeys = {
  all: ['admin-users'] as const,
  detail: (userId: string) => [...adminUserKeys.all, 'detail', userId] as const,
  list: (scope: 'all' | 'pending', page: number) => [...adminUserKeys.all, scope, page] as const,
};

export function useAdminUsers(scope: 'all' | 'pending', page: number) {
  return useQuery({
    queryKey: adminUserKeys.list(scope, page),
    queryFn: () => (scope === 'pending' ? adminUsersService.listPendingUsers(page) : adminUsersService.listUsers(page)),
  });
}

export function useAdminUser(userId: string) {
  return useQuery({
    enabled: Boolean(userId),
    queryKey: adminUserKeys.detail(userId),
    queryFn: () => adminUsersService.getUser(userId),
  });
}

export function useAdminUserAction(defaultUserId?: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ command, reason, userId }: { command: AdminUserCommand; reason?: string; userId: string }) =>
      adminUsersService.changeStatus(userId, command, reason),
    onSuccess: async (_data, variables) => {
      await queryClient.invalidateQueries({ queryKey: adminUserKeys.all });

      if (defaultUserId || variables.userId) {
        await queryClient.invalidateQueries({ queryKey: adminUserKeys.detail(defaultUserId ?? variables.userId) });
      }
    },
  });
}
