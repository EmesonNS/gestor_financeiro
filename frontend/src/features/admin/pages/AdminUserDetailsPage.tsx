import { AxiosError } from 'axios';
import { ArrowLeft, CalendarDays, Mail, ShieldCheck } from 'lucide-react';
import { useState } from 'react';
import { Link, useParams } from 'react-router';

import { Button } from '../../../shared/ui/Button';
import { AdminActionDialog } from '../components/AdminActionDialog';
import { StatusBadge } from '../components/StatusBadge';
import { useAdminUser, useAdminUserAction } from '../hooks/useAdminUsers';
import type { AdminUserCommand } from '../types/admin.types';
import { actionLabels, availableCommands, commandLabels, formatDateTime, statusLabels } from '../utils/admin-format';

export function AdminUserDetailsPage() {
  const { id } = useParams();
  const userId = id ?? '';
  const userQuery = useAdminUser(userId);
  const actionMutation = useAdminUserAction(userId);
  const [command, setCommand] = useState<AdminUserCommand | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const user = userQuery.data;
  const commands = user ? availableCommands(user.status) : [];

  async function confirmAction(reason?: string) {
    if (!command || !user) {
      return;
    }

    setActionError(null);

    try {
      await actionMutation.mutateAsync({ command, reason, userId: user.id });
      setCommand(null);
    } catch (error) {
      const message = error instanceof AxiosError ? error.response?.data?.message : null;
      setActionError(message ?? 'Nao foi possivel concluir a acao administrativa.');
    }
  }

  if (userQuery.isLoading) {
    return <div className="app-panel-muted p-8 text-center text-sm font-medium text-fuchsia-50">Carregando detalhes...</div>;
  }

  if (userQuery.isError || !user) {
    return (
      <section className="rounded-lg border border-rose-300/25 bg-rose-400/10 p-6">
        <h1 className="font-serif text-2xl font-semibold text-rose-100">Usuario nao encontrado</h1>
        <p className="mt-2 text-sm text-rose-200">Nao foi possivel carregar os detalhes administrativos deste usuario.</p>
        <Link className="mt-4 inline-flex font-semibold text-rose-100" to="/admin/users">
          Voltar para usuarios
        </Link>
      </section>
    );
  }

  return (
    <section className="space-y-6">
      <Link className="inline-flex items-center gap-2 text-sm font-semibold text-fuchsia-100 hover:text-white" to="/admin/users">
        <ArrowLeft size={16} /> Voltar para usuarios
      </Link>

      <div className="app-panel p-6">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div>
            <div className="flex flex-wrap items-center gap-3">
              <h1 className="font-serif text-3xl font-semibold text-slate-950">{user.name}</h1>
              <StatusBadge status={user.status} />
            </div>
            <div className="mt-4 grid gap-2 text-sm text-slate-600 sm:grid-cols-2">
              <span className="inline-flex items-center gap-2">
                <Mail size={16} /> {user.email}
              </span>
              <span className="inline-flex items-center gap-2">
                <ShieldCheck size={16} /> {user.role}
              </span>
              <span className="inline-flex items-center gap-2">
                <CalendarDays size={16} /> Criado em {formatDateTime(user.createdAt)}
              </span>
            </div>
          </div>

          {commands.length ? (
            <div className="flex flex-wrap justify-end gap-2">
              {commands.map((item) => (
                <Button
                  className={`min-h-9 px-3 py-1.5 ${item === 'reject' || item === 'suspend' || item === 'delete' ? 'border-rose-300/25 text-rose-200 hover:bg-rose-400/10' : ''}`}
                  key={item}
                  onClick={() => setCommand(item)}
                  type="button"
                  variant="secondary"
                >
                  {commandLabels[item]}
                </Button>
              ))}
            </div>
          ) : null}
        </div>

        {actionError ? <p className="mt-5 rounded-lg border border-rose-300/25 bg-rose-400/10 px-4 py-3 text-sm font-medium text-rose-200">{actionError}</p> : null}
      </div>

      <div className="grid gap-4 lg:grid-cols-[minmax(0,0.8fr)_minmax(0,1.2fr)]">
        <section className="app-panel p-5">
          <h2 className="font-serif text-2xl font-semibold text-slate-950">Datas de decisao</h2>
          <dl className="mt-5 grid gap-4 text-sm">
            <div>
              <dt className="font-semibold text-slate-500">Aprovado</dt>
              <dd className="mt-1 text-slate-950">{formatDateTime(user.approvedAt)}</dd>
            </div>
            <div>
              <dt className="font-semibold text-slate-500">Negado</dt>
              <dd className="mt-1 text-slate-950">{formatDateTime(user.rejectedAt)}</dd>
            </div>
            <div>
              <dt className="font-semibold text-slate-500">Suspenso</dt>
              <dd className="mt-1 text-slate-950">{formatDateTime(user.suspendedAt)}</dd>
            </div>
            <div>
              <dt className="font-semibold text-slate-500">Desativado</dt>
              <dd className="mt-1 text-slate-950">{formatDateTime(user.deletedAt)}</dd>
            </div>
          </dl>
        </section>

        <section className="app-panel p-5">
          <h2 className="font-serif text-2xl font-semibold text-slate-950">Historico administrativo</h2>
          {user.statusHistory.length ? (
            <ol className="admin-timeline mt-5 space-y-4">
              {user.statusHistory.map((history) => (
                <li className="relative rounded-lg border border-slate-100 bg-slate-50 p-4" key={history.id}>
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <strong className="text-sm text-slate-950">{actionLabels[history.action]}</strong>
                    <span className="text-xs font-medium text-slate-500">{formatDateTime(history.createdAt)}</span>
                  </div>
                  <p className="mt-2 text-sm text-slate-600">
                    {history.previousStatus ? statusLabels[history.previousStatus] : 'Sem status anterior'} {'->'} {statusLabels[history.newStatus]}
                  </p>
                  {history.reason ? <p className="mt-2 rounded-lg bg-white px-3 py-2 text-sm text-slate-700">{history.reason}</p> : null}
                </li>
              ))}
            </ol>
          ) : (
            <p className="mt-4 rounded-lg border border-dashed border-white/15 bg-white/10 px-4 py-5 text-sm text-[#c8a9d8]">
              Ainda nao ha decisoes registradas para este usuario.
            </p>
          )}
        </section>
      </div>

      <AdminActionDialog command={command} isLoading={actionMutation.isPending} onClose={() => setCommand(null)} onConfirm={confirmAction} userName={user.name} />
    </section>
  );
}
