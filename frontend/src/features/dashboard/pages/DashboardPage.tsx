export function DashboardPage() {
  return (
    <section className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold tracking-normal">Dashboard</h1>
        <p className="mt-2 text-slate-600">Base inicial pronta para integrar os módulos financeiros.</p>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {['Saldo atual', 'Receitas do mes', 'Despesas do mes', 'Saldo previsto'].map((label) => (
          <article key={label} className="rounded-lg border border-slate-200 bg-white p-4">
            <p className="text-sm text-slate-500">{label}</p>
            <strong className="mt-2 block text-xl">R$ 0,00</strong>
          </article>
        ))}
      </div>
    </section>
  );
}
