import { CalendarClock, Pencil, Tag, Trash2 } from 'lucide-react';
import { Link } from 'react-router';

import { Button } from '../../../shared/ui/Button';
import type { Category } from '../types/categories.types';
import { categoryTypeLabels, formatDateTime } from '../utils/category-format';

type CategoryCardProps = {
  category: Category;
  onDelete: (category: Category) => void;
};

export function CategoryCard({ category, onDelete }: CategoryCardProps) {
  const color = category.color ?? '#D946EF';

  return (
    <article className="app-panel p-5">
      <div className="flex items-start justify-between gap-4">
        <div className="flex gap-3">
          <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-lg border border-white/10 text-white" style={{ backgroundColor: `${color}33`, color }}>
            <Tag size={22} />
          </span>
          <div>
            <div className="flex flex-wrap items-center gap-2">
              <h2 className="font-serif text-xl font-semibold text-[#f7ecff]">{category.name}</h2>
              {category.defaultCategory ? <span className="rounded-full border border-fuchsia-300/25 bg-fuchsia-400/10 px-2 py-1 text-xs font-bold text-fuchsia-200">Padrao</span> : null}
            </div>
            <p className="mt-1 text-sm text-[#c8a9d8]">{categoryTypeLabels[category.type]}</p>
          </div>
        </div>

        {!category.defaultCategory ? (
          <Link className="rounded-lg border border-white/15 bg-white/10 px-3 py-2 text-sm font-semibold text-fuchsia-50 hover:bg-white/15" to={`/categories/${category.id}/edit`}>
            <Pencil size={15} />
            <span className="sr-only">Editar</span>
          </Link>
        ) : null}
      </div>

      <div className="mt-5 grid gap-3 text-sm text-[#c8a9d8]">
        <span className="inline-flex items-center gap-2">
          <span className="h-3 w-3 rounded-full" style={{ backgroundColor: color }} /> {category.color ?? 'Cor padrao'}
        </span>
        <span className="inline-flex items-center gap-2">
          <Tag size={16} /> {category.icon || 'Sem icone personalizado'}
        </span>
        <span className="inline-flex items-center gap-2">
          <CalendarClock size={16} /> Atualizada em {formatDateTime(category.updatedAt)}
        </span>
      </div>

      {!category.defaultCategory ? (
        <div className="mt-5 flex flex-wrap gap-2 border-t border-white/10 pt-4">
          <Button className="min-h-9 border-rose-300/25 px-3 py-1.5 text-rose-200 hover:bg-rose-400/10" onClick={() => onDelete(category)} type="button" variant="secondary">
            <Trash2 size={16} /> Excluir
          </Button>
        </div>
      ) : null}
    </article>
  );
}
