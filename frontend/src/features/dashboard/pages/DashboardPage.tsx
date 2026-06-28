export function DashboardPage() {
  return (
    <section className="space-y-6">
      <div className="app-page-hero">
        <div className="admin-flow-line" aria-hidden="true" />
        <div className="relative max-w-3xl">
          <p className="app-eyebrow">Dashboard</p>
          <h1 className="app-hero-title mt-4">Visao financeira</h1>
          <p className="app-hero-copy mt-4">Base inicial pronta para integrar os módulos financeiros.</p>
        </div>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {['Saldo atual', 'Receitas do mes', 'Despesas do mes', 'Saldo previsto'].map((label) => (
          <article key={label} className="app-panel p-4">
            <p className="text-sm text-[#c8a9d8]">{label}</p>
            <strong className="mt-2 block text-xl text-[#f7ecff]">R$ 0,00</strong>
          </article>
        ))}
      </div>
    </section>
  );
}
