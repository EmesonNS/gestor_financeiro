import { Button } from '../../../shared/ui/Button';
import type { Category } from '../types/categories.types';

type CategoryDeleteDialogProps = {
  category: Category | null;
  isLoading: boolean;
  onClose: () => void;
  onConfirm: () => void;
};

export function CategoryDeleteDialog({ category, isLoading, onClose, onConfirm }: CategoryDeleteDialogProps) {
  if (!category) {
    return null;
  }

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-slate-950/70 px-4 backdrop-blur-sm">
      <section className="app-panel w-full max-w-lg p-6">
        <p className="text-xs font-bold uppercase tracking-[0.18em] text-fuchsia-300">Categorias</p>
        <h2 className="mt-3 font-serif text-2xl font-semibold text-[#f7ecff]">Excluir categoria</h2>
        <p className="mt-3 text-sm leading-6 text-[#c8a9d8]">
          A categoria <strong className="text-[#f7ecff]">{category.name}</strong> sera removida. Se ela estiver em uso, o backend pode bloquear a exclusao.
        </p>

        <div className="mt-6 flex flex-wrap justify-end gap-3">
          <Button disabled={isLoading} onClick={onClose} type="button" variant="secondary">
            Cancelar
          </Button>
          <Button className="bg-rose-600 hover:bg-rose-500" isLoading={isLoading} onClick={onConfirm} type="button">
            Excluir categoria
          </Button>
        </div>
      </section>
    </div>
  );
}
