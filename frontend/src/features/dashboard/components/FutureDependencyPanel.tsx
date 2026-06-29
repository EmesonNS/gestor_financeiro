import { Clock3 } from 'lucide-react';

const futureItems = [
  'Faturas e parcelas: apos etapas 12 e 13',
];

export function FutureDependencyPanel() {
  return (
    <section className="app-panel-muted p-5">
      <div className="flex items-start gap-3">
        <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg border border-white/10 bg-white/10 text-fuchsia-200">
          <Clock3 size={19} />
        </span>
        <div>
          <p className="app-eyebrow">Pendencias planejadas</p>
          <h2 className="mt-2 font-serif text-2xl font-semibold text-[#f7ecff]">Indicadores futuros</h2>
          <div className="mt-4 grid gap-2 sm:grid-cols-2">
            {futureItems.map((item) => (
              <span className="rounded-lg border border-white/10 bg-[#24112f]/70 px-3 py-2 text-sm text-[#c8a9d8]" key={item}>
                {item}
              </span>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}
