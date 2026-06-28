import { CalendarDays, ReceiptText, Save } from 'lucide-react';
import { useEffect, useMemo } from 'react';
import { useForm, useWatch } from 'react-hook-form';

import { Button } from '../../../shared/ui/Button';
import { FormField } from '../../../shared/ui/FormField';
import type { Account } from '../../accounts/types/accounts.types';
import type { Category } from '../../categories/types/categories.types';
import { transactionFormSchema, type TransactionFormData } from '../schemas/transaction.schemas';
import type { Transaction, TransactionStatus } from '../types/transactions.types';
import { realizedStatusFor, transactionStatusLabels, transactionTypeLabels, transactionTypes } from '../utils/transaction-format';

type TransactionFormProps = {
  accounts: Account[];
  categories: Category[];
  isLoading: boolean;
  mode: 'create' | 'edit';
  onSubmit: (data: TransactionFormData) => Promise<void>;
  submitError?: string | null;
  transaction?: Transaction;
};

const inputClass =
  'mt-2 min-h-11 w-full rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition placeholder:text-[#9f82af] focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20';

export function TransactionForm({ accounts, categories, isLoading, mode, onSubmit, submitError, transaction }: TransactionFormProps) {
  const {
    control,
    formState: { errors },
    handleSubmit,
    register,
    reset,
    setError,
    setValue,
  } = useForm<TransactionFormData>({
    defaultValues: {
      accountId: '',
      amount: 0,
      categoryId: '',
      description: '',
      notes: '',
      status: 'PENDING',
      transactionDate: new Date().toISOString().slice(0, 10),
      type: 'EXPENSE',
    },
  });
  const selectedType = useWatch({ control, name: 'type' }) ?? 'EXPENSE';
  const selectedStatus = useWatch({ control, name: 'status' }) ?? 'PENDING';
  const selectedCategoryId = useWatch({ control, name: 'categoryId' });
  const filteredCategories = useMemo(() => categories.filter((category) => category.type === selectedType), [categories, selectedType]);
  const allowedStatuses: TransactionStatus[] = useMemo(
    () => (selectedType === 'EXPENSE' ? ['PENDING', 'PAID', 'CANCELED'] : ['PENDING', 'RECEIVED', 'CANCELED']),
    [selectedType],
  );

  useEffect(() => {
    if (transaction) {
      reset({
        accountId: transaction.accountId ?? '',
        amount: Number(transaction.amount),
        categoryId: transaction.categoryId,
        description: transaction.description,
        notes: transaction.notes ?? '',
        status: transaction.status,
        transactionDate: transaction.transactionDate,
        type: transaction.type,
      });
    }
  }, [reset, transaction]);

  useEffect(() => {
    if (!allowedStatuses.includes(selectedStatus)) {
      setValue('status', 'PENDING', { shouldDirty: true, shouldValidate: true });
    }
  }, [allowedStatuses, selectedStatus, setValue]);

  useEffect(() => {
    if (!selectedCategoryId) {
      return;
    }

    const categoryStillMatches = filteredCategories.some((category) => category.id === selectedCategoryId);
    if (!categoryStillMatches) {
      setValue('categoryId', '', { shouldDirty: true, shouldValidate: true });
    }
  }, [filteredCategories, selectedCategoryId, setValue]);

  async function submit(data: TransactionFormData) {
    const parsed = transactionFormSchema.safeParse(data);

    if (!parsed.success) {
      parsed.error.issues.forEach((issue) => {
        setError(issue.path[0] as keyof TransactionFormData, { message: issue.message });
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
          <h2 className="font-serif text-2xl font-semibold text-[#f7ecff]">{mode === 'create' ? 'Nova transacao' : 'Editar transacao'}</h2>
          <p className="text-sm text-[#c8a9d8]">Registre receitas, despesas e o impacto nas contas financeiras.</p>
        </div>
      </div>

      <form className="mt-6 grid gap-4 sm:grid-cols-2" onSubmit={handleSubmit(submit)}>
        <div className="sm:col-span-2">
          <FormField autoComplete="off" error={errors.description?.message} id="description" label="Descricao" {...register('description')} />
        </div>

        <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="type">
          Tipo
          <select className={inputClass} id="type" {...register('type')}>
            {transactionTypes.map((type) => (
              <option key={type} value={type}>
                {transactionTypeLabels[type]}
              </option>
            ))}
          </select>
          {errors.type ? <span className="mt-2 block text-xs font-medium text-rose-300">{errors.type.message}</span> : null}
        </label>

        <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="status">
          Status
          <select className={inputClass} id="status" {...register('status')}>
            {allowedStatuses.map((status) => (
              <option key={status} value={status}>
                {transactionStatusLabels[status]}
              </option>
            ))}
          </select>
          {errors.status ? <span className="mt-2 block text-xs font-medium text-rose-300">{errors.status.message}</span> : null}
        </label>

        <FormField error={errors.amount?.message} id="amount" label="Valor" min="0.01" step="0.01" type="number" {...register('amount')} />

        <FormField error={errors.transactionDate?.message} id="transactionDate" label="Data" leadingIcon={<CalendarDays size={17} />} type="date" {...register('transactionDate')} />

        <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="categoryId">
          Categoria
          <select className={inputClass} id="categoryId" {...register('categoryId')}>
            <option value="">Selecione uma categoria</option>
            {filteredCategories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
          {errors.categoryId ? <span className="mt-2 block text-xs font-medium text-rose-300">{errors.categoryId.message}</span> : null}
        </label>

        <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="accountId">
          Conta
          <select className={inputClass} id="accountId" {...register('accountId')}>
            <option value="">{selectedStatus === realizedStatusFor(selectedType) ? 'Selecione uma conta' : 'Sem conta por enquanto'}</option>
            {accounts.map((account) => (
              <option key={account.id} value={account.id}>
                {account.name}
              </option>
            ))}
          </select>
          {errors.accountId ? <span className="mt-2 block text-xs font-medium text-rose-300">{errors.accountId.message}</span> : null}
        </label>

        <label className="block text-sm font-medium text-[#dcc3e8] sm:col-span-2" htmlFor="notes">
          Observacoes
          <textarea className={`${inputClass} min-h-28 resize-y`} id="notes" {...register('notes')} />
          {errors.notes ? <span className="mt-2 block text-xs font-medium text-rose-300">{errors.notes.message}</span> : null}
        </label>

        {submitError ? <p className="rounded-lg border border-rose-300/25 bg-rose-400/10 px-3 py-2 text-sm font-medium text-rose-200 sm:col-span-2">{submitError}</p> : null}

        <div className="sm:col-span-2">
          <Button isLoading={isLoading} type="submit">
            <Save size={17} /> {mode === 'create' ? 'Criar transacao' : 'Salvar transacao'}
          </Button>
        </div>
      </form>
    </section>
  );
}
