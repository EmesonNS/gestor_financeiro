import { ReceiptText, Save } from 'lucide-react';
import { useEffect } from 'react';
import { useForm } from 'react-hook-form';

import { Button } from '../../../shared/ui/Button';
import { FormField } from '../../../shared/ui/FormField';
import type { Category } from '../../categories/types/categories.types';
import { cardPurchaseFormSchema, type CardPurchaseFormData } from '../schemas/installment.schemas';
import type { CardPurchase } from '../types/installments.types';

type CardPurchaseFormProps = {
  categories: Category[];
  isLoading: boolean;
  mode: 'create' | 'edit';
  onSubmit: (data: CardPurchaseFormData) => Promise<void>;
  purchase?: CardPurchase;
  submitError?: string | null;
};

const inputClass =
  'mt-2 min-h-11 w-full rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition placeholder:text-[#9f82af] focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20';

export function CardPurchaseForm({ categories, isLoading, mode, onSubmit, purchase, submitError }: CardPurchaseFormProps) {
  const {
    formState: { errors },
    handleSubmit,
    register,
    reset,
    setError,
  } = useForm<CardPurchaseFormData>({
    defaultValues: {
      categoryId: '',
      description: '',
      installmentCount: 1,
      notes: '',
      purchaseDate: '',
      totalAmount: 0,
    },
  });

  useEffect(() => {
    if (purchase) {
      reset({
        categoryId: purchase.categoryId,
        description: purchase.description,
        installmentCount: purchase.installmentCount,
        notes: purchase.notes ?? '',
        purchaseDate: purchase.purchaseDate,
        totalAmount: Number(purchase.totalAmount),
      });
    }
  }, [purchase, reset]);

  async function submit(data: CardPurchaseFormData) {
    const parsed = cardPurchaseFormSchema.safeParse(data);

    if (!parsed.success) {
      parsed.error.issues.forEach((issue) => {
        setError(issue.path[0] as keyof CardPurchaseFormData, { message: issue.message });
      });
      return;
    }

    await onSubmit(parsed.data);
  }

  return (
    <section className="app-panel p-6">
      <div className="flex items-center gap-3">
        <span className="flex h-11 w-11 items-center justify-center rounded-lg border border-white/10 bg-fuchsia-400/15 text-fuchsia-200">
          <ReceiptText size={22} />
        </span>
        <div>
          <h2 className="font-serif text-2xl font-semibold text-[#f7ecff]">{mode === 'create' ? 'Nova compra' : 'Editar compra'}</h2>
          <p className="text-sm text-[#c8a9d8]">O backend gera as parcelas e associa cada uma a uma fatura.</p>
        </div>
      </div>

      <form className="mt-6 grid gap-4 sm:grid-cols-2" onSubmit={handleSubmit(submit)}>
        <div className="sm:col-span-2">
          <FormField autoComplete="off" error={errors.description?.message} id="description" label="Descricao" {...register('description')} />
        </div>

        <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="categoryId">
          Categoria
          <select className={inputClass} id="categoryId" {...register('categoryId')}>
            <option value="">Selecione uma categoria</option>
            {categories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
          {errors.categoryId ? <span className="mt-2 block text-xs font-medium text-rose-300">{errors.categoryId.message}</span> : null}
        </label>

        <FormField error={errors.totalAmount?.message} id="totalAmount" label="Valor total" min="0.01" step="0.01" type="number" {...register('totalAmount')} />
        <FormField error={errors.purchaseDate?.message} id="purchaseDate" label="Data da compra" type="date" {...register('purchaseDate')} />
        <FormField error={errors.installmentCount?.message} id="installmentCount" label="Quantidade de parcelas" min="1" step="1" type="number" {...register('installmentCount')} />

        <label className="block text-sm font-medium text-[#dcc3e8] sm:col-span-2" htmlFor="notes">
          Observacoes
          <textarea className={`${inputClass} min-h-28 resize-y`} id="notes" {...register('notes')} />
          {errors.notes ? <span className="mt-2 block text-xs font-medium text-rose-300">{errors.notes.message}</span> : null}
        </label>

        {submitError ? <p className="rounded-lg border border-rose-300/25 bg-rose-400/10 px-3 py-2 text-sm font-medium text-rose-200 sm:col-span-2">{submitError}</p> : null}

        <div className="sm:col-span-2">
          <Button isLoading={isLoading} type="submit">
            <Save size={17} /> {mode === 'create' ? 'Criar compra' : 'Salvar compra'}
          </Button>
        </div>
      </form>
    </section>
  );
}
