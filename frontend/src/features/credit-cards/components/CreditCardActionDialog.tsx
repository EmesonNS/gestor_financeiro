import { Button } from '../../../shared/ui/Button';
import type { CreditCard } from '../types/credit-cards.types';
import { formatCurrency } from '../utils/credit-card-format';

type CreditCardDialogAction = 'archive' | 'delete';

type CreditCardActionDialogProps = {
  action: CreditCardDialogAction | null;
  card: CreditCard | null;
  isLoading: boolean;
  onClose: () => void;
  onConfirm: () => void;
};

const copy = {
  archive: {
    button: 'Arquivar cartao',
    description: 'O cartao saira da lista ativa e nao aceitara novas compras.',
    title: 'Arquivar cartao',
  },
  delete: {
    button: 'Excluir cartao',
    description: 'A exclusao pode ser bloqueada se houver compras ou faturas relevantes vinculadas.',
    title: 'Excluir cartao',
  },
};

export function CreditCardActionDialog({ action, card, isLoading, onClose, onConfirm }: CreditCardActionDialogProps) {
  if (!action || !card) {
    return null;
  }

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-slate-950/70 px-4 backdrop-blur-sm">
      <section className="app-panel w-full max-w-lg p-6">
        <p className="text-xs font-bold uppercase tracking-[0.18em] text-fuchsia-300">Cartoes de credito</p>
        <h2 className="mt-3 font-serif text-2xl font-semibold text-[#f7ecff]">{copy[action].title}</h2>
        <p className="mt-3 text-sm leading-6 text-[#c8a9d8]">
          {copy[action].description} Cartao selecionado: <strong className="text-[#f7ecff]">{card.name}</strong>, limite de{' '}
          <strong className="text-[#f7ecff]">{formatCurrency(card.limitAmount)}</strong>.
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
