import { CreditCard, Save } from 'lucide-react';
import { useEffect } from 'react';
import { useForm } from 'react-hook-form';

import { Button } from '../../../shared/ui/Button';
import { FormField } from '../../../shared/ui/FormField';
import { creditCardFormSchema, type CreditCardFormData } from '../schemas/credit-card.schemas';
import type { CreditCard as CreditCardType } from '../types/credit-cards.types';

type CreditCardFormProps = {
  card?: CreditCardType;
  isLoading: boolean;
  mode: 'create' | 'edit';
  onSubmit: (data: CreditCardFormData) => Promise<void>;
  submitError?: string | null;
};

export function CreditCardForm({ card, isLoading, mode, onSubmit, submitError }: CreditCardFormProps) {
  const {
    formState: { errors },
    handleSubmit,
    register,
    reset,
    setError,
  } = useForm<CreditCardFormData>({
    defaultValues: {
      closingDay: 1,
      dueDay: 10,
      limitAmount: 0,
      name: '',
    },
  });

  useEffect(() => {
    if (card) {
      reset({
        closingDay: card.closingDay,
        dueDay: card.dueDay,
        limitAmount: Number(card.limitAmount),
        name: card.name,
      });
    }
  }, [card, reset]);

  async function submit(data: CreditCardFormData) {
    const parsed = creditCardFormSchema.safeParse(data);

    if (!parsed.success) {
      parsed.error.issues.forEach((issue) => {
        setError(issue.path[0] as keyof CreditCardFormData, { message: issue.message });
      });
      return;
    }

    await onSubmit(parsed.data);
  }

  return (
    <section className="app-panel p-6">
      <div className="flex items-center gap-3">
        <span className="flex h-11 w-11 items-center justify-center rounded-lg border border-white/10 bg-fuchsia-400/15 text-fuchsia-200">
          <CreditCard size={22} />
        </span>
        <div>
          <h2 className="font-serif text-2xl font-semibold text-[#f7ecff]">{mode === 'create' ? 'Novo cartao' : 'Editar cartao'}</h2>
          <p className="text-sm text-[#c8a9d8]">Defina limite, fechamento e vencimento.</p>
        </div>
      </div>

      <form className="mt-6 grid gap-4 sm:grid-cols-2" onSubmit={handleSubmit(submit)}>
        <div className="sm:col-span-2">
          <FormField autoComplete="off" error={errors.name?.message} id="name" label="Nome do cartao" {...register('name')} />
        </div>

        <FormField error={errors.limitAmount?.message} id="limitAmount" label="Limite total" min="0" step="0.01" type="number" {...register('limitAmount')} />
        <FormField error={errors.closingDay?.message} id="closingDay" label="Dia de fechamento" max="31" min="1" step="1" type="number" {...register('closingDay')} />
        <FormField error={errors.dueDay?.message} id="dueDay" label="Dia de vencimento" max="31" min="1" step="1" type="number" {...register('dueDay')} />

        {submitError ? <p className="rounded-lg border border-rose-300/25 bg-rose-400/10 px-3 py-2 text-sm font-medium text-rose-200 sm:col-span-2">{submitError}</p> : null}

        <div className="sm:col-span-2">
          <Button isLoading={isLoading} type="submit">
            <Save size={17} /> {mode === 'create' ? 'Criar cartao' : 'Salvar cartao'}
          </Button>
        </div>
      </form>
    </section>
  );
}
