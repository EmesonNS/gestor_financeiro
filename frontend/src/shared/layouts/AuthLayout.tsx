import { Outlet } from 'react-router';

export function AuthLayout() {
  return (
    <main className="auth-shell grid min-h-screen overflow-hidden bg-slate-950 text-white lg:grid-cols-[minmax(0,1fr)_minmax(420px,520px)]">
      <section className="relative hidden min-h-screen px-10 py-12 lg:flex lg:flex-col lg:justify-between">
        <div>
          <p className="text-sm font-bold uppercase tracking-[0.24em] text-fuchsia-200">Zorysa Finance</p>
          <h2 className="mt-8 max-w-xl font-serif text-6xl font-semibold leading-[0.95] text-white">
            Controle financeiro
          </h2>
        </div>

        <div className="relative h-64 max-w-2xl">
          <div className="cashflow-line" aria-hidden="true" />
          <div className="absolute left-0 top-6 rounded-lg border border-white/10 bg-white/10 px-4 py-3 backdrop-blur">
            <p className="text-xs uppercase tracking-[0.18em] text-fuchsia-200">Receitas</p>
            <strong className="mt-1 block text-2xl">R$ 8.420</strong>
          </div>
          <div className="absolute bottom-8 left-56 rounded-lg border border-white/10 bg-white/10 px-4 py-3 backdrop-blur">
            <p className="text-xs uppercase tracking-[0.18em] text-rose-200">Despesas</p>
            <strong className="mt-1 block text-2xl">R$ 4.180</strong>
          </div>
          <div className="absolute right-0 top-16 rounded-lg border border-white/10 bg-white/10 px-4 py-3 backdrop-blur">
            <p className="text-xs uppercase tracking-[0.18em] text-violet-200">Saldo previsto</p>
            <strong className="mt-1 block text-2xl">R$ 4.240</strong>
          </div>
        </div>
      </section>

      <section className="flex min-h-screen items-center justify-center px-4 py-8 sm:px-6">
        <Outlet />
      </section>
    </main>
  );
}
