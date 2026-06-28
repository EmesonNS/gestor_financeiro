import { Save, WalletCards } from 'lucide-react';
import { useEffect } from 'react';
import { useForm } from 'react-hook-form';

import { Button } from '../../../shared/ui/Button';
import { FormField } from '../../../shared/ui/FormField';
import { accountFormSchema, type AccountFormData } from '../schemas/account.schemas';
import type { Account } from '../types/accounts.types';
import { accountTypeLabels, accountTypes } from '../utils/account-format';

type AccountFormProps = {
  account?: Account;
  isLoading: boolean;
  mode: 'create' | 'edit';
  onSubmit: (data: AccountFormData) => Promise<void>;
  submitError?: string | null;
  submitMessage?: string | null;
};

export function AccountForm({ account, isLoading, mode, onSubmit, submitError, submitMessage }: AccountFormProps) {
  const {
    formState: { errors },
    handleSubmit,
    register,
    reset,
    setError,
  } = useForm<AccountFormData>({
    defaultValues: {
      initialBalance: 0,
      name: '',
      type: 'DIGITAL_ACCOUNT',
    },
  });

  useEffect(() => {
    if (account) {
      reset({
        initialBalance: Number(account.initialBalance),
        name: account.name,
        type: account.type,
      });
    }
  }, [account, reset]);

  async function submit(data: AccountFormData) {
    const parsed = accountFormSchema.safeParse(data);

    if (!parsed.success) {
      parsed.error.issues.forEach((issue) => {
        setError(issue.path[0] as keyof AccountFormData, { message: issue.message });
      });
      return;
    }

    await onSubmit(parsed.data);
  }

  return (
    <section className="app-panel p-6">
      <div className="flex items-center gap-3">
        <span className="flex h-11 w-11 items-center justify-center rounded-lg border border-white/10 bg-fuchsia-400/15 text-fuchsia-200">
          <WalletCards size={22} />
        </span>
        <div>
          <h2 className="font-serif text-2xl font-semibold text-[#f7ecff]">{mode === 'create' ? 'Nova conta' : 'Editar conta'}</h2>
          <p className="text-sm text-[#c8a9d8]">Defina a origem do dinheiro e o saldo de partida.</p>
        </div>
      </div>

      <form className="mt-6 grid gap-4 sm:grid-cols-2" onSubmit={handleSubmit(submit)}>
        <div className="sm:col-span-2">
          <FormField autoComplete="off" error={errors.name?.message} id="name" label="Nome da conta" {...register('name')} />
        </div>

        <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="type">
          Tipo
          <select
            className="mt-2 min-h-11 w-full rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20"
            id="type"
            {...register('type')}
          >
            {accountTypes.map((type) => (
              <option key={type} value={type}>
                {accountTypeLabels[type]}
              </option>
            ))}
          </select>
          {errors.type ? <span className="mt-2 block text-xs font-medium text-rose-300">{errors.type.message}</span> : null}
        </label>

        <FormField
          disabled={mode === 'edit'}
          error={errors.initialBalance?.message}
          id="initialBalance"
          label="Saldo inicial"
          min="0"
          step="0.01"
          type="number"
          {...register('initialBalance')}
        />

        {mode === 'edit' ? <p className="sm:col-span-2 rounded-lg border border-white/10 bg-white/10 px-3 py-2 text-sm text-[#c8a9d8]">O saldo inicial nao pode ser alterado depois da criacao.</p> : null}
        {submitError ? <p className="rounded-lg border border-rose-300/25 bg-rose-400/10 px-3 py-2 text-sm font-medium text-rose-200 sm:col-span-2">{submitError}</p> : null}
        {submitMessage ? <p className="rounded-lg border border-emerald-300/25 bg-emerald-400/10 px-3 py-2 text-sm font-medium text-emerald-200 sm:col-span-2">{submitMessage}</p> : null}

        <div className="sm:col-span-2">
          <Button isLoading={isLoading} type="submit">
            <Save size={17} /> {mode === 'create' ? 'Criar conta' : 'Salvar conta'}
          </Button>
        </div>
      </form>
    </section>
  );
}
