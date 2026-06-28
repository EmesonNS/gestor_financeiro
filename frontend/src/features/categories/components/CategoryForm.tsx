import { Save, Tag } from 'lucide-react';
import { useEffect } from 'react';
import { useForm, useWatch } from 'react-hook-form';

import { Button } from '../../../shared/ui/Button';
import { FormField } from '../../../shared/ui/FormField';
import { categoryFormSchema, type CategoryFormData } from '../schemas/category.schemas';
import type { Category } from '../types/categories.types';
import { categoryColorOptions, categoryTypeLabels, categoryTypes } from '../utils/category-format';

type CategoryFormProps = {
  category?: Category;
  isLoading: boolean;
  mode: 'create' | 'edit';
  onSubmit: (data: CategoryFormData) => Promise<void>;
  submitError?: string | null;
  submitMessage?: string | null;
};

export function CategoryForm({ category, isLoading, mode, onSubmit, submitError, submitMessage }: CategoryFormProps) {
  const {
    formState: { errors },
    handleSubmit,
    register,
    reset,
    setError,
    setValue,
    control,
  } = useForm<CategoryFormData>({
    defaultValues: {
      color: '#D946EF',
      icon: '',
      name: '',
      type: 'EXPENSE',
    },
  });
  const selectedColor = useWatch({ control, name: 'color' }) || '#D946EF';

  useEffect(() => {
    if (category) {
      reset({
        color: category.color ?? '#D946EF',
        icon: category.icon ?? '',
        name: category.name,
        type: category.type,
      });
    }
  }, [category, reset]);

  async function submit(data: CategoryFormData) {
    const parsed = categoryFormSchema.safeParse(data);

    if (!parsed.success) {
      parsed.error.issues.forEach((issue) => {
        setError(issue.path[0] as keyof CategoryFormData, { message: issue.message });
      });
      return;
    }

    await onSubmit(parsed.data);
  }

  return (
    <section className="app-panel p-6">
      <div className="flex items-center gap-3">
        <span className="flex h-11 w-11 items-center justify-center rounded-lg border border-white/10 text-white" style={{ backgroundColor: `${selectedColor}33`, color: selectedColor }}>
          <Tag size={22} />
        </span>
        <div>
          <h2 className="font-serif text-2xl font-semibold text-[#f7ecff]">{mode === 'create' ? 'Nova categoria' : 'Editar categoria'}</h2>
          <p className="text-sm text-[#c8a9d8]">Classifique receitas e despesas com cor e identificador visual.</p>
        </div>
      </div>

      <form className="mt-6 grid gap-4 sm:grid-cols-2" onSubmit={handleSubmit(submit)}>
        <div className="sm:col-span-2">
          <FormField autoComplete="off" error={errors.name?.message} id="name" label="Nome da categoria" {...register('name')} />
        </div>

        <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor="type">
          Tipo
          <select
            className="mt-2 min-h-11 w-full rounded-lg border border-[#5a3a6e] bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20"
            id="type"
            {...register('type')}
          >
            {categoryTypes.map((type) => (
              <option key={type} value={type}>
                {categoryTypeLabels[type]}
              </option>
            ))}
          </select>
          {errors.type ? <span className="mt-2 block text-xs font-medium text-rose-300">{errors.type.message}</span> : null}
        </label>

        <FormField autoComplete="off" error={errors.icon?.message} id="icon" label="Icone ou palavra-chave" placeholder="ex: utensils, salary, home" {...register('icon')} />

        <div className="sm:col-span-2">
          <FormField autoComplete="off" error={errors.color?.message} id="color" label="Cor" {...register('color')} />
          <div className="mt-3 flex flex-wrap gap-2">
            {categoryColorOptions.map((color) => (
              <button
                aria-label={`Usar cor ${color}`}
                className={`h-9 w-9 rounded-lg border transition ${selectedColor === color ? 'border-white ring-2 ring-fuchsia-300' : 'border-white/20'}`}
                key={color}
                onClick={() => setValue('color', color, { shouldDirty: true, shouldValidate: true })}
                style={{ backgroundColor: color }}
                type="button"
              />
            ))}
          </div>
        </div>

        {submitError ? <p className="rounded-lg border border-rose-300/25 bg-rose-400/10 px-3 py-2 text-sm font-medium text-rose-200 sm:col-span-2">{submitError}</p> : null}
        {submitMessage ? <p className="rounded-lg border border-emerald-300/25 bg-emerald-400/10 px-3 py-2 text-sm font-medium text-emerald-200 sm:col-span-2">{submitMessage}</p> : null}

        <div className="sm:col-span-2">
          <Button isLoading={isLoading} type="submit">
            <Save size={17} /> {mode === 'create' ? 'Criar categoria' : 'Salvar categoria'}
          </Button>
        </div>
      </form>
    </section>
  );
}
