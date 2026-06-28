import type { PropsWithChildren, ReactNode } from 'react';

type AuthCardProps = PropsWithChildren<{
  eyebrow: string;
  footer?: ReactNode;
  title: string;
}>;

export function AuthCard({ children, eyebrow, footer, title }: AuthCardProps) {
  return (
    <section className="app-panel w-full max-w-md p-6 backdrop-blur md:p-8">
      <p className="text-xs font-bold uppercase tracking-[0.18em] text-fuchsia-300">{eyebrow}</p>
      <h1 className="mt-3 font-serif text-3xl font-semibold leading-tight text-[#f7ecff]">{title}</h1>
      <div className="mt-8">{children}</div>
      {footer ? <div className="mt-6 border-t border-white/10 pt-5 text-sm text-[#c8a9d8]">{footer}</div> : null}
    </section>
  );
}
