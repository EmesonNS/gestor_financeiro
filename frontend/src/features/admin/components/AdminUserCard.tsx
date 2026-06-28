import { CalendarClock, Mail, Shield } from 'lucide-react';
import { Link } from 'react-router';

import { Button } from '../../../shared/ui/Button';
import type { AdminUser, AdminUserCommand } from '../types/admin.types';
import { availableCommands, commandLabels, formatDateTime } from '../utils/admin-format';
import { StatusBadge } from './StatusBadge';

type AdminUserCardProps = {
  onAction: (user: AdminUser, command: AdminUserCommand) => void;
  user: AdminUser;
};

export function AdminUserCard({ onAction, user }: AdminUserCardProps) {
  const commands = availableCommands(user.status);

  return (
    <article className="app-panel p-5">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <div className="flex flex-wrap items-center gap-3">
            <h2 className="font-serif text-xl font-semibold text-slate-950">{user.name}</h2>
            <StatusBadge status={user.status} />
          </div>
          <div className="mt-3 grid gap-2 text-sm text-slate-600">
            <span className="inline-flex items-center gap-2">
              <Mail size={16} /> {user.email}
            </span>
            <span className="inline-flex items-center gap-2">
              <Shield size={16} /> {user.role}
            </span>
            <span className="inline-flex items-center gap-2">
              <CalendarClock size={16} /> Criado em {formatDateTime(user.createdAt)}
            </span>
          </div>
        </div>

        <Link className="rounded-lg border border-white/15 bg-white/10 px-3 py-2 text-sm font-semibold text-fuchsia-50 hover:bg-white/15" to={`/admin/users/${user.id}`}>
          Ver detalhes
        </Link>
      </div>

      {commands.length ? (
        <div className="mt-5 flex flex-wrap gap-2 border-t border-white/10 pt-4">
          {commands.map((command) => (
            <Button
              className={`min-h-9 px-3 py-1.5 ${command === 'reject' || command === 'suspend' || command === 'delete' ? 'border-rose-300/25 text-rose-200 hover:bg-rose-400/10' : ''}`}
              key={command}
              onClick={() => onAction(user, command)}
              type="button"
              variant="secondary"
            >
              {commandLabels[command]}
            </Button>
          ))}
        </div>
      ) : null}
    </article>
  );
}
