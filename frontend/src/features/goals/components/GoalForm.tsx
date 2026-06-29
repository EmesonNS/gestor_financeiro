import { Save, Target } from 'lucide-react';
import { useEffect } from 'react';
import { useForm } from 'react-hook-form';

import { Button } from '../../../shared/ui/Button';
import { FormField } from '../../../shared/ui/FormField';
import { goalFormSchema, type GoalFormData } from '../schemas/goal.schemas';
import type { Goal } from '../types/goals.types';

type GoalFormProps = {
  goal?: Goal;
  isLoading: boolean;
  mode: 'create' | 'edit';
  onSubmit: (data: GoalFormData) => Promise<void>;
  submitError?: string | null;
};

const inputClass =
  'mt-2 min-h-11 w-full rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition placeholder:text-[#9f82af] focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20';

export function GoalForm({ goal, isLoading, mode, onSubmit, submitError }: GoalFormProps) {
  const {
    formState: { errors },
    handleSubmit,
    register,
    reset,
    setError,
  } = useForm<GoalFormData>({
    defaultValues: {
      currentAmount: 0,
      deadline: '',
      description: '',
      name: '',
      targetAmount: 0,
    },
  });

  useEffect(() => {
    if (goal) {
      reset({
        currentAmount: Number(goal.currentAmount),
        deadline: goal.deadline ?? '',
        description: goal.description ?? '',
        name: goal.name,
        targetAmount: Number(goal.targetAmount),
      });
    }
  }, [goal, reset]);

  async function submit(data: GoalFormData) {
    const parsed = goalFormSchema.safeParse(data);

    if (!parsed.success) {
      parsed.error.issues.forEach((issue) => {
        setError(issue.path[0] as keyof GoalFormData, { message: issue.message });
      });
      return;
    }

    await onSubmit(parsed.data);
  }

  return (
    <section className="app-panel p-6">
      <div className="flex items-center gap-3">
        <span className="flex h-11 w-11 items-center justify-center rounded-lg border border-white/10 bg-fuchsia-400/15 text-fuchsia-200">
          <Target size={22} />
        </span>
        <div>
          <h2 className="font-serif text-2xl font-semibold text-[#f7ecff]">{mode === 'create' ? 'Nova meta' : 'Editar meta'}</h2>
          <p className="text-sm text-[#c8a9d8]">Defina objetivo, valor atual e prazo opcional.</p>
        </div>
      </div>

      <form className="mt-6 grid gap-4 sm:grid-cols-2" onSubmit={handleSubmit(submit)}>
        <div className="sm:col-span-2">
          <FormField autoComplete="off" error={errors.name?.message} id="name" label="Nome da meta" {...register('name')} />
        </div>

        <FormField error={errors.targetAmount?.message} id="targetAmount" label="Valor alvo" min="0.01" step="0.01" type="number" {...register('targetAmount')} />

        <FormField error={errors.currentAmount?.message} id="currentAmount" label="Valor atual" min="0" step="0.01" type="number" {...register('currentAmount')} />

        <div className="sm:col-span-2">
          <FormField error={errors.deadline?.message} id="deadline" label="Prazo" type="date" {...register('deadline')} />
        </div>

        <label className="block text-sm font-medium text-[#dcc3e8] sm:col-span-2" htmlFor="description">
          Descricao
          <textarea className={`${inputClass} min-h-28 resize-y`} id="description" {...register('description')} />
          {errors.description ? <span className="mt-2 block text-xs font-medium text-rose-300">{errors.description.message}</span> : null}
        </label>

        {submitError ? <p className="rounded-lg border border-rose-300/25 bg-rose-400/10 px-3 py-2 text-sm font-medium text-rose-200 sm:col-span-2">{submitError}</p> : null}

        <div className="sm:col-span-2">
          <Button isLoading={isLoading} type="submit">
            <Save size={17} /> {mode === 'create' ? 'Criar meta' : 'Salvar meta'}
          </Button>
        </div>
      </form>
    </section>
  );
}
