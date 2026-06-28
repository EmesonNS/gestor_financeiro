import { CircleAlert, CircleCheck, CircleOff, ShieldX } from 'lucide-react';
import { Link, useLocation } from 'react-router';

import { Button } from '../../../shared/ui/Button';
import { AuthCard } from '../components/AuthCard';
import type { AccountStatus } from '../types/auth.types';

type AccountStatusPageProps = {
  status: Exclude<AccountStatus, 'APPROVED'>;
};

type AccountStatusState = {
  email?: string;
  message?: string;
};

const statusContent = {
  PENDING_APPROVAL: {
    eyebrow: 'Cadastro em analise',
    title: 'Sua conta aguarda aprovacao',
    description: 'A solicitacao foi recebida. O acesso ao dashboard sera liberado depois da aprovacao administrativa.',
    icon: CircleCheck,
  },
  SUSPENDED: {
    eyebrow: 'Acesso bloqueado',
    title: 'Sua conta esta suspensa',
    description: 'O acesso ao sistema foi pausado por uma decisao administrativa. Entre em contato com o administrador para revisar a situacao.',
    icon: CircleAlert,
  },
  REJECTED: {
    eyebrow: 'Cadastro negado',
    title: 'Seu cadastro nao foi aprovado',
    description: 'A solicitacao de acesso foi negada. Revise os dados com o administrador antes de tentar novamente.',
    icon: ShieldX,
  },
  DELETED: {
    eyebrow: 'Conta indisponivel',
    title: 'Esta conta nao esta disponivel',
    description: 'A conta foi desativada ou excluida administrativamente e nao pode acessar o sistema.',
    icon: CircleOff,
  },
} satisfies Record<Exclude<AccountStatus, 'APPROVED'>, { description: string; eyebrow: string; icon: typeof CircleAlert; title: string }>;

export function AccountStatusPage({ status }: AccountStatusPageProps) {
  const location = useLocation();
  const state = location.state as AccountStatusState | null;
  const content = statusContent[status];
  const Icon = content.icon;

  return (
    <AuthCard
      eyebrow={content.eyebrow}
      footer={
        <div className="flex flex-wrap items-center justify-between gap-3">
          <Link className="font-semibold text-fuchsia-300 hover:text-fuchsia-200" to="/login">
            Voltar para login
          </Link>
          <Link className="font-semibold text-fuchsia-300 hover:text-fuchsia-200" to="/register">
            Solicitar outro cadastro
          </Link>
        </div>
      }
      title={content.title}
    >
      <div className="space-y-5">
        <div className="flex h-12 w-12 items-center justify-center rounded-lg border border-white/10 bg-fuchsia-400/15 text-fuchsia-200">
          <Icon size={24} />
        </div>
        <p className="text-sm leading-6 text-[#c8a9d8]">{state?.message ?? content.description}</p>
        {state?.email ? (
          <div className="rounded-lg border border-white/10 bg-white/10 px-4 py-3 text-sm text-[#c8a9d8]">
            Solicitacao vinculada a <strong className="text-[#f7ecff]">{state.email}</strong>.
          </div>
        ) : null}
        <Button className="w-full" type="button" variant="secondary">
          <Link className="flex w-full justify-center" to="/login">
            Entendi
          </Link>
        </Button>
      </div>
    </AuthCard>
  );
}
