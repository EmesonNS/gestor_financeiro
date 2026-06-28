import type { AccountStatus } from '../../auth/types/auth.types';
import { statusLabels } from '../utils/admin-format';

const statusClasses: Record<AccountStatus, string> = {
  APPROVED: 'border-emerald-300/25 bg-emerald-400/10 text-emerald-200',
  DELETED: 'border-slate-300/20 bg-slate-400/10 text-slate-200',
  PENDING_APPROVAL: 'border-fuchsia-300/25 bg-fuchsia-400/10 text-fuchsia-200',
  REJECTED: 'border-rose-300/25 bg-rose-400/10 text-rose-200',
  SUSPENDED: 'border-amber-300/25 bg-amber-400/10 text-amber-200',
};

export function StatusBadge({ status }: { status: AccountStatus }) {
  return <span className={`inline-flex rounded-full border px-2.5 py-1 text-xs font-bold ${statusClasses[status]}`}>{statusLabels[status]}</span>;
}
