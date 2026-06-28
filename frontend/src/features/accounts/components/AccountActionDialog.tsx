import { Button } from '../../../shared/ui/Button';
import type { Account } from '../types/accounts.types';

type AccountDialogAction = 'archive' | 'delete';

type AccountActionDialogProps = {
  action: AccountDialogAction | null;
  account: Account | null;
  isLoading: boolean;
  onClose: () => void;
  onConfirm: () => void;
};

const copy = {
  archive: {
    button: 'Arquivar conta',
    description: 'A conta sairá da lista ativa, mas continuará disponível nos filtros de arquivadas.',
    title: 'Arquivar conta',
  },
  delete: {
    button: 'Excluir conta',
    description: 'A exclusão remove a conta. Se houver histórico financeiro vinculado, o backend pode bloquear esta ação.',
    title: 'Excluir conta',
  },
};

export function AccountActionDialog({ account, action, isLoading, onClose, onConfirm }: AccountActionDialogProps) {
  if (!action || !account) {
    return null;
  }

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-slate-950/70 px-4 backdrop-blur-sm">
      <section className="app-panel w-full max-w-lg p-6">
        <p className="text-xs font-bold uppercase tracking-[0.18em] text-fuchsia-300">Contas financeiras</p>
        <h2 className="mt-3 font-serif text-2xl font-semibold text-[#f7ecff]">{copy[action].title}</h2>
        <p className="mt-3 text-sm leading-6 text-[#c8a9d8]">
          {copy[action].description} Conta selecionada: <strong className="text-[#f7ecff]">{account.name}</strong>.
        </p>

        <div className="mt-6 flex flex-wrap justify-end gap-3">
          <Button disabled={isLoading} onClick={onClose} type="button" variant="secondary">
            Cancelar
          </Button>
          <Button className={action === 'delete' ? 'bg-rose-600 hover:bg-rose-500' : ''} isLoading={isLoading} onClick={onConfirm} type="button">
            {copy[action].button}
          </Button>
        </div>
      </section>
    </div>
  );
}
