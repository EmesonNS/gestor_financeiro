import { AxiosError } from 'axios';
import { ArrowLeft } from 'lucide-react';
import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router';

import { AccountForm } from '../components/AccountForm';
import { useAccount, useCreateAccount, useUpdateAccount } from '../hooks/useAccounts';
import type { ApiErrorResponse } from '../../auth/types/auth.types';
import type { AccountFormData } from '../schemas/account.schemas';

export function AccountFormPage() {
  const { id } = useParams();
  const isEdit = Boolean(id);
  const navigate = useNavigate();
  const accountQuery = useAccount(id);
  const createMutation = useCreateAccount();
  const updateMutation = useUpdateAccount(id ?? '');
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [submitMessage, setSubmitMessage] = useState<string | null>(null);

  async function submit(data: AccountFormData) {
    setSubmitError(null);
    setSubmitMessage(null);

    try {
      if (isEdit && id) {
        await updateMutation.mutateAsync({
          name: data.name,
          type: data.type,
        });
        navigate('/accounts', { replace: true });
      } else {
        await createMutation.mutateAsync({
          initialBalance: data.initialBalance,
          name: data.name,
          type: data.type,
        });
        navigate('/accounts', { replace: true });
      }
    } catch (error) {
      const message = error instanceof AxiosError ? (error.response?.data as ApiErrorResponse | undefined)?.message : null;
      setSubmitError(message ?? 'Nao foi possivel salvar a conta.');
    }
  }

  if (isEdit && accountQuery.isLoading) {
    return <div className="app-panel-muted p-8 text-center text-sm font-medium text-fuchsia-50">Carregando conta...</div>;
  }

  if (isEdit && (accountQuery.isError || !accountQuery.data)) {
    return (
      <section className="rounded-lg border border-rose-300/25 bg-rose-400/10 p-6">
        <h1 className="font-serif text-2xl font-semibold text-rose-100">Conta nao encontrada</h1>
        <p className="mt-2 text-sm text-rose-200">Nao foi possivel carregar esta conta financeira.</p>
        <Link className="mt-4 inline-flex font-semibold text-rose-100" to="/accounts">
          Voltar para contas
        </Link>
      </section>
    );
  }

  return (
    <section className="space-y-6">
      <Link className="inline-flex items-center gap-2 text-sm font-semibold text-fuchsia-100 hover:text-white" to="/accounts">
        <ArrowLeft size={16} /> Voltar para contas
      </Link>

      <div className="app-page-hero">
        <div className="admin-flow-line" aria-hidden="true" />
        <div className="relative max-w-3xl">
          <p className="app-eyebrow">Contas financeiras</p>
          <h1 className="app-hero-title mt-4">{isEdit ? 'Editar origem' : 'Nova origem'}</h1>
          <p className="app-hero-copy mt-4 max-w-xl">
            {isEdit ? 'Atualize nome e tipo da conta sem alterar o saldo inicial.' : 'Cadastre uma fonte de saldo para organizar seu dinheiro desde o primeiro lançamento.'}
          </p>
        </div>
      </div>

      <AccountForm
        account={accountQuery.data}
        isLoading={createMutation.isPending || updateMutation.isPending}
        mode={isEdit ? 'edit' : 'create'}
        onSubmit={submit}
        submitError={submitError}
        submitMessage={submitMessage}
      />
    </section>
  );
}
