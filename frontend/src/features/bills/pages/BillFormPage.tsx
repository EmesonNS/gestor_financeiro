import { ArrowLeft } from 'lucide-react';
import { useMemo, useState } from 'react';
import { Link, Navigate, useNavigate, useParams } from 'react-router';

import { useAccount, useAccounts } from '../../accounts/hooks/useAccounts';
import type { Account } from '../../accounts/types/accounts.types';
import { useCategories, useCategory } from '../../categories/hooks/useCategories';
import type { Category } from '../../categories/types/categories.types';
import { apiErrorMessage } from '../../../shared/lib/api-error';
import { BillForm } from '../components/BillForm';
import { useBill, useCreateBill, useUpdateBill } from '../hooks/useBills';
import type { BillFormData } from '../schemas/bill.schemas';

function mergeById<T extends { id: string }>(items: T[], item?: T | null) {
  if (!item || items.some((current) => current.id === item.id)) {
    return items;
  }

  return [item, ...items];
}

export function BillFormPage() {
  const { id } = useParams();
  const isEdit = Boolean(id);
  const navigate = useNavigate();
  const billQuery = useBill(id);
  const bill = billQuery.data;
  const accountsQuery = useAccounts({ archived: false, page: 0 });
  const currentAccountQuery = useAccount(bill?.accountId ?? undefined);
  const categoriesQuery = useCategories({ page: 0, type: 'EXPENSE' });
  const currentCategoryQuery = useCategory(bill?.categoryId);
  const createMutation = useCreateBill();
  const updateMutation = useUpdateBill(id ?? '');
  const [submitError, setSubmitError] = useState<string | null>(null);

  const accounts = useMemo<Account[]>(
    () => mergeById(accountsQuery.data?.content ?? [], currentAccountQuery.data),
    [accountsQuery.data?.content, currentAccountQuery.data],
  );
  const categories = useMemo<Category[]>(
    () => mergeById(categoriesQuery.data?.content ?? [], currentCategoryQuery.data),
    [categoriesQuery.data?.content, currentCategoryQuery.data],
  );

  async function submit(data: BillFormData) {
    setSubmitError(null);
    const payload = {
      accountId: data.accountId || null,
      amount: data.amount,
      categoryId: data.categoryId,
      description: data.description,
      dueDate: data.dueDate,
      status: data.status,
    };

    try {
      if (isEdit && id) {
        await updateMutation.mutateAsync(payload);
      } else {
        await createMutation.mutateAsync(payload);
      }
      navigate('/bills', { replace: true });
    } catch (error) {
      setSubmitError(apiErrorMessage(error, 'Nao foi possivel salvar a conta a pagar.'));
    }
  }

  if (isEdit && billQuery.isLoading) {
    return <div className="app-panel-muted p-8 text-center text-sm font-medium text-fuchsia-50">Carregando conta...</div>;
  }

  if (isEdit && (billQuery.isError || !bill)) {
    return (
      <section className="rounded-lg border border-rose-300/25 bg-rose-400/10 p-6">
        <h1 className="font-serif text-2xl font-semibold text-rose-100">Conta nao encontrada</h1>
        <p className="mt-2 text-sm text-rose-200">Nao foi possivel carregar esta conta a pagar.</p>
        <Link className="mt-4 inline-flex font-semibold text-rose-100" to="/bills">
          Voltar para contas
        </Link>
      </section>
    );
  }

  if (bill?.status === 'PAID') {
    return <Navigate replace to="/bills" />;
  }

  return (
    <section className="space-y-6">
      <Link className="inline-flex items-center gap-2 text-sm font-semibold text-fuchsia-100 hover:text-white" to="/bills">
        <ArrowLeft size={16} /> Voltar para contas
      </Link>

      <div className="app-page-hero">
        <div className="admin-flow-line" aria-hidden="true" />
        <div className="relative max-w-3xl">
          <p className="app-eyebrow">Contas a pagar</p>
          <h1 className="app-hero-title mt-4">{isEdit ? 'Editar vencimento' : 'Novo vencimento'}</h1>
          <p className="app-hero-copy mt-4 max-w-xl">
            {isEdit ? 'Atualize dados da conta antes do pagamento.' : 'Cadastre uma obrigacao futura e transforme em despesa quando ela for paga.'}
          </p>
        </div>
      </div>

      <BillForm
        accounts={accounts}
        bill={bill}
        categories={categories}
        isLoading={createMutation.isPending || updateMutation.isPending}
        mode={isEdit ? 'edit' : 'create'}
        onSubmit={submit}
        submitError={submitError}
      />
    </section>
  );
}
