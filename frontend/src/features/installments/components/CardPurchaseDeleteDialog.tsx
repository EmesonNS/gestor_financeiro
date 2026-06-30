import { Button } from '../../../shared/ui/Button';
import type { CardPurchase } from '../types/installments.types';
import { formatCurrency, hasPaidInstallment } from '../utils/installment-format';

type CardPurchaseDeleteDialogProps = {
  isLoading: boolean;
  onClose: () => void;
  onConfirm: () => void;
  purchase: CardPurchase | null;
};

export function CardPurchaseDeleteDialog({ isLoading, onClose, onConfirm, purchase }: CardPurchaseDeleteDialogProps) {
  if (!purchase) {
    return null;
  }

  const hasPaid = hasPaidInstallment(purchase.installments);

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-slate-950/70 px-4 backdrop-blur-sm">
      <section className="app-panel w-full max-w-lg p-6">
        <p className="text-xs font-bold uppercase tracking-[0.18em] text-fuchsia-300">Compras parceladas</p>
        <h2 className="mt-3 font-serif text-2xl font-semibold text-[#f7ecff]">Excluir compra</h2>
        <p className="mt-3 text-sm leading-6 text-[#c8a9d8]">
          Remover <strong className="text-[#f7ecff]">{purchase.description}</strong>, valor de <strong className="text-[#f7ecff]">{formatCurrency(purchase.totalAmount)}</strong>. Se houver
          fatura paga, o backend deve bloquear a exclusao.
        </p>
        {hasPaid ? <p className="mt-4 rounded-lg border border-amber-300/25 bg-amber-400/10 px-3 py-2 text-sm text-amber-100">Esta compra possui parcela em fatura paga.</p> : null}

        <div className="mt-6 flex flex-wrap justify-end gap-3">
          <Button disabled={isLoading} onClick={onClose} type="button" variant="secondary">
            Cancelar
          </Button>
          <Button className="bg-rose-600 hover:bg-rose-500" isLoading={isLoading} onClick={onConfirm} type="button">
            Excluir compra
          </Button>
        </div>
      </section>
    </div>
  );
}
