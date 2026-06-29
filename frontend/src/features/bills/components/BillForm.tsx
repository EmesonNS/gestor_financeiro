import { CalendarDays, FileClock, Save } from 'lucide-react';
import { useEffect } from 'react';
import { useForm } from 'react-hook-form';

import { Button } from '../../../shared/ui/Button';
import { FormField } from '../../../shared/ui/FormField';
import type { Account } from '../../accounts/types/accounts.types';
import type { Category } from '../../categories/types/categories.types';
import { billFormSchema, type BillFormData } from '../schemas/bill.schemas';
import type { Bill, BillStatus } from '../types/bills.types';
import { billStatusLabels } from '../utils/bill-format';

type BillFormProps = {
  accounts: Account[];
  bill?: Bill;
  categories: Category[];
  isLoading: boolean;
  mode: 'create' | 'edit';
  onSubmit: (data: BillFormData) => Promise<void>;
  submitError?: string | null;
};

const editableStatuses: BillStatus[] = ['PENDING', 'CANCELED'];
const inputClass =
  'mt-2 min-h-11 w-full rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition placeholder:text-[#9f82af] focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20';

export function BillForm({ accounts, bill, categories, isLoading, mode, onSubmit, submitError }: BillFormProps) {
  const {
    formState: { errors },
    handleSubmit,
    register,
    reset,
    setError,
  } = useForm<BillFormData>({
    defaultValues: {
      accountId: '',
      amount: 0,
      categoryId: '',
      description: '',
      dueDate: new Date().toISOString().slice(0, 10),
      status: 'PENDING',
    },
  });

  useEffect(() => {
    if (bill) {
      reset({
        accountId: bill.accountId ?? '',
        amount: Number(bill.amount),
        categoryId: bill.categoryId,
        description: bill.description,
        dueDate: bill.dueDate,
        status: bill.status === 'PAID' ? 'PENDING' : bill.status,
      });
    }
  }, [bill, reset]);

  async function submit(data: BillFormData) {
    const parsed = billFormSchema.safeParse(data);

    if (!parsed.success) {
      parsed.error.issues.forEach((issue) => {
        setError(issue.path[0] as keyof BillFormData, { message: issue.message });
      });
      return;
    }

    await onSubmit(parsed.data);
  }

  return (
    <section className="app-panel p-6">
      <div className="flex items-center gap-3">
        <span className="flex h-11 w-11 items-center justify-center rounded-lg border border-white/10 bg-fuchsia-400/15 text-fuchsia-200">
          <FileClock size={22} />
        </span>
        <div>
          <h2 className="font-serif text-2xl font-semibold text-[#f7ecff]">{mode === 'create' ? 'Nova conta a pagar' : 'Editar conta a pagar'}</h2>
          <p className="text-sm text-[#c8a9d8]">Controle vencimento, categoria e conta sugerida para pagamento.</p>
        </div>
      </div>

      <form className="mt-6 grid gap-4 sm:grid-cols-2" onSubmit={handleSubmit(submit)}>
        <div className="sm:col-span-2">
          <FormField autoComplete="off" error={errors.description?.message} id="description" label="Descricao" {...register('description')} />
        </div>

        <FormField error={errors.amount?.message} id="amount" label="Valor" min="0.01" step="0.01" type="number" {...register('amount')} />

        <FormField error={errors.dueDate?.message} id="dueDate" label="Vencimento" leadingIcon={<CalendarDays size={17} />} type="date" {...register('dueDate')} />

        <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="categoryId">
          Categoria de despesa
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

        <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="accountId">
          Conta sugerida
          <select className={inputClass} id="accountId" {...register('accountId')}>
            <option value="">Definir ao pagar</option>
            {accounts.map((account) => (
              <option key={account.id} value={account.id}>
                {account.name} - saldo {new Intl.NumberFormat('pt-BR', { currency: 'BRL', style: 'currency' }).format(Number(account.currentBalance))}
              </option>
            ))}
          </select>
          {errors.accountId ? <span className="mt-2 block text-xs font-medium text-rose-300">{errors.accountId.message}</span> : null}
        </label>

        <label className="block text-sm font-medium text-[#dcc3e8] sm:col-span-2" htmlFor="status">
          Status
          <select className={inputClass} id="status" {...register('status')}>
            {editableStatuses.map((status) => (
              <option key={status} value={status}>
                {billStatusLabels[status]}
              </option>
            ))}
          </select>
          {errors.status ? <span className="mt-2 block text-xs font-medium text-rose-300">{errors.status.message}</span> : null}
        </label>

        {submitError ? <p className="rounded-lg border border-rose-300/25 bg-rose-400/10 px-3 py-2 text-sm font-medium text-rose-200 sm:col-span-2">{submitError}</p> : null}

        <div className="sm:col-span-2">
          <Button isLoading={isLoading} type="submit">
            <Save size={17} /> {mode === 'create' ? 'Criar conta' : 'Salvar conta'}
          </Button>
        </div>
      </form>
    </section>
  );
}
