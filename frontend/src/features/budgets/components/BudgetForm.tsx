import { PiggyBank, Save } from 'lucide-react';
import { useEffect } from 'react';
import { useForm, useWatch } from 'react-hook-form';

import { Button } from '../../../shared/ui/Button';
import { FormField } from '../../../shared/ui/FormField';
import type { Category } from '../../categories/types/categories.types';
import { budgetFormSchema, type BudgetFormData } from '../schemas/budget.schemas';
import type { Budget } from '../types/budgets.types';
import { monthOptions } from '../utils/budget-format';

type BudgetFormProps = {
  budget?: Budget;
  categories: Category[];
  isLoading: boolean;
  mode: 'create' | 'edit';
  onSubmit: (data: BudgetFormData) => Promise<void>;
  submitError?: string | null;
};

const inputClass =
  'mt-2 min-h-11 w-full rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition placeholder:text-[#9f82af] focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20';

export function BudgetForm({ budget, categories, isLoading, mode, onSubmit, submitError }: BudgetFormProps) {
  const {
    formState: { errors },
    handleSubmit,
    register,
    reset,
    setError,
    control,
  } = useForm<BudgetFormData>({
    defaultValues: {
      categoryId: '',
      durationMode: 'SINGLE',
      endMonth: new Date().getMonth() + 1,
      endYear: new Date().getFullYear(),
      limitAmount: 0,
      startMonth: new Date().getMonth() + 1,
      startYear: new Date().getFullYear(),
    },
  });
  const durationMode = useWatch({ control, name: 'durationMode' }) ?? 'SINGLE';

  useEffect(() => {
    if (budget) {
      const isSingleMonth = budget.endMonth === budget.startMonth && budget.endYear === budget.startYear;
      reset({
        categoryId: budget.categoryId,
        durationMode: !budget.endMonth || !budget.endYear ? 'FOREVER' : isSingleMonth ? 'SINGLE' : 'RANGE',
        endMonth: budget.endMonth ?? budget.startMonth,
        endYear: budget.endYear ?? budget.startYear,
        limitAmount: Number(budget.limitAmount),
        startMonth: budget.startMonth,
        startYear: budget.startYear,
      });
    }
  }, [budget, reset]);

  async function submit(data: BudgetFormData) {
    const parsed = budgetFormSchema.safeParse(data);

    if (!parsed.success) {
      parsed.error.issues.forEach((issue) => {
        setError(issue.path[0] as keyof BudgetFormData, { message: issue.message });
      });
      return;
    }

    await onSubmit(parsed.data);
  }

  return (
    <section className="app-panel p-6">
      <div className="flex items-center gap-3">
        <span className="flex h-11 w-11 items-center justify-center rounded-lg border border-white/10 bg-fuchsia-400/15 text-fuchsia-200">
          <PiggyBank size={22} />
        </span>
        <div>
          <h2 className="font-serif text-2xl font-semibold text-[#f7ecff]">{mode === 'create' ? 'Novo orcamento' : 'Editar orcamento'}</h2>
          <p className="text-sm text-[#c8a9d8]">Defina um limite mensal por categoria e periodo de vigencia.</p>
        </div>
      </div>

      <form className="mt-6 grid gap-4 sm:grid-cols-2" onSubmit={handleSubmit(submit)}>
        <label className="block text-sm font-medium text-[#dcc3e8] sm:col-span-2" htmlFor="categoryId">
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

        <div className="sm:col-span-2">
          <p className="mb-2 text-sm font-medium text-[#dcc3e8]">Vigencia</p>
          <div className="grid gap-2 sm:grid-cols-3">
            {[
              ['SINGLE', 'Um mes'],
              ['RANGE', 'Intervalo'],
              ['FOREVER', 'Para sempre'],
            ].map(([value, label]) => (
              <label
                className={`flex min-h-11 items-center justify-center rounded-lg border px-3 py-2 text-sm font-semibold transition ${
                  durationMode === value ? 'border-fuchsia-300 bg-fuchsia-500 text-white shadow-lg shadow-fuchsia-950/20' : 'border-white/10 bg-white/10 text-fuchsia-50'
                }`}
                key={value}
              >
                <input className="sr-only" type="radio" value={value} {...register('durationMode')} />
                {label}
              </label>
            ))}
          </div>
        </div>

        <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="startMonth">
          Mes inicial
          <select className={inputClass} id="startMonth" {...register('startMonth')}>
            {monthOptions.map((month) => (
              <option key={month.value} value={month.value}>
                {month.label}
              </option>
            ))}
          </select>
          {errors.startMonth ? <span className="mt-2 block text-xs font-medium text-rose-300">{errors.startMonth.message}</span> : null}
        </label>

        <FormField error={errors.startYear?.message} id="startYear" label="Ano inicial" min="2000" max="2100" type="number" {...register('startYear')} />

        {durationMode === 'RANGE' ? (
          <>
            <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="endMonth">
              Mes final
              <select className={inputClass} id="endMonth" {...register('endMonth')}>
                {monthOptions.map((month) => (
                  <option key={month.value} value={month.value}>
                    {month.label}
                  </option>
                ))}
              </select>
              {errors.endMonth ? <span className="mt-2 block text-xs font-medium text-rose-300">{errors.endMonth.message}</span> : null}
            </label>

            <FormField error={errors.endYear?.message} id="endYear" label="Ano final" min="2000" max="2100" type="number" {...register('endYear')} />
          </>
        ) : null}

        {durationMode === 'FOREVER' ? (
          <p className="rounded-lg border border-white/10 bg-white/10 px-3 py-2 text-sm text-[#c8a9d8] sm:col-span-2">
            Este limite ficara ativo a partir do periodo inicial ate ser editado ou excluido.
          </p>
        ) : null}

        <div className="sm:col-span-2">
          <FormField error={errors.limitAmount?.message} id="limitAmount" label="Limite mensal" min="0.01" step="0.01" type="number" {...register('limitAmount')} />
        </div>

        {submitError ? <p className="rounded-lg border border-rose-300/25 bg-rose-400/10 px-3 py-2 text-sm font-medium text-rose-200 sm:col-span-2">{submitError}</p> : null}

        <div className="sm:col-span-2">
          <Button isLoading={isLoading} type="submit">
            <Save size={17} /> {mode === 'create' ? 'Criar orcamento' : 'Salvar orcamento'}
          </Button>
        </div>
      </form>
    </section>
  );
}
