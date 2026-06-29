import { Button } from '../../../shared/ui/Button';
import type { Bill } from '../types/bills.types';
import { formatCurrency } from '../utils/bill-format';

type BillDeleteDialogProps = {
  bill: Bill | null;
  isLoading: boolean;
  onClose: () => void;
  onConfirm: () => void;
};

export function BillDeleteDialog({ bill, isLoading, onClose, onConfirm }: BillDeleteDialogProps) {
  if (!bill) {
    return null;
  }

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-slate-950/70 px-4 backdrop-blur-sm">
      <section className="app-panel w-full max-w-lg p-6">
        <p className="text-xs font-bold uppercase tracking-[0.18em] text-fuchsia-300">Contas a pagar</p>
        <h2 className="mt-3 font-serif text-2xl font-semibold text-[#f7ecff]">Excluir conta</h2>
        <p className="mt-3 text-sm leading-6 text-[#c8a9d8]">
          Esta conta sera removida se ainda nao tiver pagamento vinculado. Conta selecionada:{' '}
          <strong className="text-[#f7ecff]">{bill.description}</strong>, valor <strong className="text-[#f7ecff]">{formatCurrency(bill.amount)}</strong>.
        </p>

        <div className="mt-6 flex flex-wrap justify-end gap-3">
          <Button disabled={isLoading} onClick={onClose} type="button" variant="secondary">
            Cancelar
          </Button>
          <Button className="bg-rose-600 hover:bg-rose-500" isLoading={isLoading} onClick={onConfirm} type="button">
            Excluir conta
          </Button>
        </div>
      </section>
    </div>
  );
}
