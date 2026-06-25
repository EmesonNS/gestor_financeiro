import type { PropsWithChildren, ReactNode } from 'react';

type AuthCardProps = PropsWithChildren<{
  eyebrow: string;
  footer?: ReactNode;
  title: string;
}>;

export function AuthCard({ children, eyebrow, footer, title }: AuthCardProps) {
  return (
    <section className="w-full max-w-md rounded-lg border border-white/70 bg-white/95 p-6 shadow-2xl shadow-fuchsia-950/20 backdrop-blur md:p-8">
      <p className="text-xs font-bold uppercase tracking-[0.18em] text-fuchsia-600">{eyebrow}</p>
      <h1 className="mt-3 font-serif text-3xl font-semibold leading-tight text-slate-950">{title}</h1>
      <div className="mt-8">{children}</div>
      {footer ? <div className="mt-6 border-t border-slate-100 pt-5 text-sm text-slate-600">{footer}</div> : null}
    </section>
  );
}
