import type { InputHTMLAttributes, ReactNode } from 'react';

type FormFieldProps = InputHTMLAttributes<HTMLInputElement> & {
  error?: string;
  label: string;
  leadingIcon?: ReactNode;
};

export function FormField({ error, id, label, leadingIcon, className = '', ...inputProps }: FormFieldProps) {
  return (
    <label className="block text-sm font-medium text-[#dcc3e8]" htmlFor={id}>
      {label}
      <span className="relative mt-2 block">
        {leadingIcon ? <span className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-[#c8a9d8]">{leadingIcon}</span> : null}
        <input
          aria-invalid={Boolean(error)}
          className={`min-h-11 w-full rounded-lg border bg-[#24112f] px-3 py-2 text-[#f7ecff] outline-none transition placeholder:text-[#9f82af] focus:border-fuchsia-400 focus:ring-4 focus:ring-fuchsia-500/20 ${leadingIcon ? 'pl-10' : ''} ${
            error ? 'border-rose-400' : 'border-[#5a3a6e]'
          } ${className}`}
          id={id}
          {...inputProps}
        />
      </span>
      {error ? <span className="mt-2 block text-xs font-medium text-rose-300">{error}</span> : null}
    </label>
  );
}
