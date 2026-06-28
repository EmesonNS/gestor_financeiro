import { CalendarDays, Mail, UserRound } from 'lucide-react';

import type { UserProfile } from '../types/profile.types';

function formatDate(value?: string) {
  if (!value) {
    return 'Nao informado';
  }

  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'medium',
  }).format(new Date(value));
}

export function ProfileSummary({ profile }: { profile: UserProfile }) {
  return (
    <section className="app-panel relative min-h-80 overflow-hidden p-6">
      <div className="profile-flow-line" aria-hidden="true" />
      <div className="relative">
        <div className="flex h-14 w-14 items-center justify-center rounded-lg border border-white/10 bg-fuchsia-400/15 text-fuchsia-200">
          <UserRound size={28} />
        </div>
        <h1 className="mt-5 font-serif text-3xl font-semibold leading-tight text-slate-950">{profile.name}</h1>
        <div className="mt-5 grid gap-3 text-sm text-slate-600">
          <span className="inline-flex items-center gap-2">
            <Mail size={16} /> {profile.email}
          </span>
          <span className="inline-flex items-center gap-2">
            <CalendarDays size={16} /> Conta criada em {formatDate(profile.createdAt)}
          </span>
        </div>
      </div>
    </section>
  );
}
