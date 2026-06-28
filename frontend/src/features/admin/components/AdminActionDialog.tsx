import { useState } from 'react';

import { Button } from '../../../shared/ui/Button';
import type { AdminUserCommand } from '../types/admin.types';
import { commandLabels, isReasonRequired } from '../utils/admin-format';

type AdminActionDialogProps = {
  command: AdminUserCommand | null;
  isLoading: boolean;
  onClose: () => void;
  onConfirm: (reason?: string) => void;
  userName: string;
};

const commandCopy: Record<AdminUserCommand, string> = {
  approve: 'A conta sera aprovada e podera acessar o sistema.',
  delete: 'A conta ficara indisponivel para acesso.',
  reactivate: 'A conta voltara ao status aprovado.',
  reject: 'O cadastro sera negado e nao recebera acesso.',
  suspend: 'A conta aprovada perdera acesso as rotas privadas.',
};

export function AdminActionDialog({ command, isLoading, onClose, onConfirm, userName }: AdminActionDialogProps) {
  const [reason, setReason] = useState('');

  if (!command) {
    return null;
  }

  const reasonRequired = isReasonRequired(command);
  const canConfirm = !reasonRequired || reason.trim().length > 0;

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-slate-950/50 px-4 backdrop-blur-sm">
      <section className="w-full max-w-lg rounded-lg border border-fuchsia-100 bg-white p-6 shadow-2xl shadow-slate-950/20">
        <p className="text-xs font-bold uppercase tracking-[0.18em] text-fuchsia-600">Confirmacao administrativa</p>
        <h2 className="mt-3 font-serif text-2xl font-semibold text-slate-950">
          {commandLabels[command]} {userName}
        </h2>
        <p className="mt-3 text-sm leading-6 text-slate-600">{commandCopy[command]}</p>

        <label className="mt-5 block text-sm font-medium text-slate-700" htmlFor="admin-reason">
          Motivo {reasonRequired ? '' : '(opcional)'}
          <textarea
            className="mt-2 min-h-28 w-full resize-none rounded-lg border border-slate-200 bg-white px-3 py-2 text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-fuchsia-500 focus:ring-4 focus:ring-fuchsia-100"
            id="admin-reason"
            onChange={(event) => setReason(event.target.value)}
            placeholder="Registre o contexto da decisao"
            value={reason}
          />
        </label>

        <div className="mt-6 flex flex-wrap justify-end gap-3">
          <Button disabled={isLoading} onClick={onClose} type="button" variant="secondary">
            Cancelar
          </Button>
          <Button
            className={command === 'delete' || command === 'reject' || command === 'suspend' ? 'bg-rose-600 hover:bg-rose-500' : ''}
            disabled={!canConfirm}
            isLoading={isLoading}
            onClick={() => onConfirm(reason.trim() || undefined)}
            type="button"
          >
            {commandLabels[command]}
          </Button>
        </div>
      </section>
    </div>
  );
}
