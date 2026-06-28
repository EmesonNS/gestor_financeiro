import { AxiosError } from 'axios';
import { ChevronLeft, ChevronRight, SearchCheck, UsersRound } from 'lucide-react';
import { useMemo, useState } from 'react';
import { Link, useLocation } from 'react-router';

import { Button } from '../../../shared/ui/Button';
import { useAdminUserAction, useAdminUsers } from '../hooks/useAdminUsers';
import type { AdminUser, AdminUserCommand } from '../types/admin.types';
import { statusLabels } from '../utils/admin-format';
import { AdminActionDialog } from '../components/AdminActionDialog';
import { AdminUserCard } from '../components/AdminUserCard';

type DialogState = {
  command: AdminUserCommand;
  user: AdminUser;
} | null;

export function AdminUsersPage() {
  const location = useLocation();
  const scope = location.pathname.endsWith('/pending') ? 'pending' : 'all';
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState<'ALL' | AdminUser['status']>('ALL');
  const [dialog, setDialog] = useState<DialogState>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const usersQuery = useAdminUsers(scope, page);
  const actionMutation = useAdminUserAction();

  const users = useMemo(() => usersQuery.data?.content ?? [], [usersQuery.data?.content]);
  const visibleUsers = useMemo(() => (statusFilter === 'ALL' ? users : users.filter((user) => user.status === statusFilter)), [statusFilter, users]);
  const pendingCount = users.filter((user) => user.status === 'PENDING_APPROVAL').length;
  const totalPages = usersQuery.data?.totalPages ?? 0;

  async function confirmAction(reason?: string) {
    if (!dialog) {
      return;
    }

    setActionError(null);

    try {
      await actionMutation.mutateAsync({
        command: dialog.command,
        reason,
        userId: dialog.user.id,
      });
      setDialog(null);
    } catch (error) {
      const message = error instanceof AxiosError ? error.response?.data?.message : null;
      setActionError(message ?? 'Nao foi possivel concluir a acao administrativa.');
    }
  }

  return (
    <section className="space-y-6">
      <div className="relative overflow-hidden rounded-lg border border-white/10 bg-white/10 p-6 shadow-2xl shadow-fuchsia-950/20 backdrop-blur">
        <div className="admin-flow-line" aria-hidden="true" />
        <div className="relative flex flex-wrap items-start justify-between gap-4">
          <div>
            <p className="text-xs font-bold uppercase tracking-[0.24em] text-fuchsia-200">Administracao</p>
            <h1 className="mt-3 font-serif text-4xl font-semibold leading-tight text-white">Usuarios e aprovacoes</h1>
            <p className="mt-3 max-w-2xl text-sm leading-6 text-fuchsia-50/80">
            Controle quem pode entrar no sistema, aprove cadastros pendentes e registre decisoes administrativas.
            </p>
          </div>

          <div className="grid min-w-48 rounded-lg border border-white/15 bg-white/10 p-4 text-white shadow-sm shadow-fuchsia-950/20 backdrop-blur">
            <span className="inline-flex items-center gap-2 text-sm font-medium text-fuchsia-50/85">
              <SearchCheck size={17} /> Pendentes nesta pagina
            </span>
            <strong className="mt-2 font-serif text-4xl text-white">{pendingCount}</strong>
          </div>
        </div>
      </div>

      <div className="flex flex-wrap items-center justify-between gap-3 rounded-lg border border-white/10 bg-white/10 p-3 backdrop-blur">
        <div className="flex flex-wrap gap-2">
          <Link className={`rounded-lg px-3 py-2 text-sm font-semibold ${scope === 'all' ? 'bg-fuchsia-500 text-white shadow-lg shadow-fuchsia-950/20' : 'text-fuchsia-50 hover:bg-white/10'}`} to="/admin/users">
            Todos
          </Link>
          <Link
            className={`rounded-lg px-3 py-2 text-sm font-semibold ${scope === 'pending' ? 'bg-fuchsia-500 text-white shadow-lg shadow-fuchsia-950/20' : 'text-fuchsia-50 hover:bg-white/10'}`}
            to="/admin/users/pending"
          >
            Pendentes
          </Link>
        </div>

        <label className="flex items-center gap-2 text-sm font-medium text-fuchsia-50">
          Status
          <select
            className="rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-950 outline-none focus:border-fuchsia-500 focus:ring-4 focus:ring-fuchsia-100"
            onChange={(event) => setStatusFilter(event.target.value as 'ALL' | AdminUser['status'])}
            value={statusFilter}
          >
            <option value="ALL">Todos da pagina</option>
            {Object.entries(statusLabels).map(([status, label]) => (
              <option key={status} value={status}>
                {label}
              </option>
            ))}
          </select>
        </label>
      </div>

      {actionError ? <p className="rounded-lg border border-rose-200 bg-rose-50 px-4 py-3 text-sm font-medium text-rose-700">{actionError}</p> : null}

      {usersQuery.isLoading ? (
        <div className="rounded-lg border border-white/10 bg-white/10 p-8 text-center text-sm font-medium text-fuchsia-50 backdrop-blur">Carregando usuarios...</div>
      ) : null}

      {usersQuery.isError ? (
        <div className="rounded-lg border border-rose-200 bg-rose-50 p-6 text-sm font-medium text-rose-700">
          Nao foi possivel carregar usuarios. Tente novamente em alguns instantes.
        </div>
      ) : null}

      {!usersQuery.isLoading && !visibleUsers.length ? (
        <div className="rounded-lg border border-dashed border-white/20 bg-white/10 p-10 text-center backdrop-blur">
          <UsersRound className="mx-auto text-fuchsia-500" size={36} />
          <h2 className="mt-4 font-serif text-2xl font-semibold text-white">Nenhum usuario encontrado</h2>
          <p className="mt-2 text-sm text-fuchsia-50/75">Nao ha cadastros para esta visualizacao.</p>
        </div>
      ) : null}

      <div className="grid gap-4">
        {visibleUsers.map((user) => (
          <AdminUserCard key={user.id} onAction={(selectedUser, command) => setDialog({ command, user: selectedUser })} user={user} />
        ))}
      </div>

      {totalPages > 1 ? (
        <div className="flex items-center justify-end gap-2">
          <Button disabled={page === 0 || usersQuery.isFetching} onClick={() => setPage((current) => Math.max(0, current - 1))} type="button" variant="secondary">
            <ChevronLeft size={16} /> Anterior
          </Button>
          <span className="text-sm font-medium text-fuchsia-50">
            Pagina {page + 1} de {totalPages}
          </span>
          <Button disabled={page + 1 >= totalPages || usersQuery.isFetching} onClick={() => setPage((current) => current + 1)} type="button" variant="secondary">
            Proxima <ChevronRight size={16} />
          </Button>
        </div>
      ) : null}

      <AdminActionDialog
        command={dialog?.command ?? null}
        isLoading={actionMutation.isPending}
        onClose={() => setDialog(null)}
        onConfirm={confirmAction}
        userName={dialog?.user.name ?? ''}
      />
    </section>
  );
}
