import type { InputHTMLAttributes, ReactNode } from 'react';

type FormFieldProps = InputHTMLAttributes<HTMLInputElement> & {
  error?: string;
  label: string;
  leadingIcon?: ReactNode;
};

export function FormField({ error, id, label, leadingIcon, className = '', ...inputProps }: FormFieldProps) {
  return (
    <label className="block text-sm font-medium text-slate-700" htmlFor={id}>
      {label}
      <span className="relative mt-2 block">
        {leadingIcon ? <span className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">{leadingIcon}</span> : null}
        <input
          aria-invalid={Boolean(error)}
          className={`min-h-11 w-full rounded-lg border bg-white px-3 py-2 text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-fuchsia-500 focus:ring-4 focus:ring-fuchsia-100 ${leadingIcon ? 'pl-10' : ''} ${
            error ? 'border-rose-400' : 'border-slate-200'
          } ${className}`}
          id={id}
          {...inputProps}
        />
      </span>
      {error ? <span className="mt-2 block text-xs font-medium text-rose-600">{error}</span> : null}
    </label>
  );
}
