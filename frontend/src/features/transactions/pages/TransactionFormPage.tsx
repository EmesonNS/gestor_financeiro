import { ArrowLeft } from 'lucide-react';
import { useMemo, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router';

import { useAccount, useAccounts } from '../../accounts/hooks/useAccounts';
import type { Account } from '../../accounts/types/accounts.types';
import { hasSufficientBalance, insufficientBalanceMessage, type PreviousBalanceImpact } from '../../accounts/utils/balance-validation';
import { useCategories, useCategory } from '../../categories/hooks/useCategories';
import type { Category } from '../../categories/types/categories.types';
import { apiErrorMessage } from '../../../shared/lib/api-error';
import { TransactionForm } from '../components/TransactionForm';
import { useCreateTransaction, useTransaction, useUpdateTransaction } from '../hooks/useTransactions';
import type { TransactionFormData } from '../schemas/transaction.schemas';

function mergeById<T extends { id: string }>(items: T[], item?: T | null) {
  if (!item || items.some((current) => current.id === item.id)) {
    return items;
  }

  return [item, ...items];
}

export function TransactionFormPage() {
  const { id } = useParams();
  const isEdit = Boolean(id);
  const navigate = useNavigate();
  const transactionQuery = useTransaction(id);
  const transaction = transactionQuery.data;
  const accountsQuery = useAccounts({ archived: false, page: 0 });
  const currentAccountQuery = useAccount(transaction?.accountId ?? undefined);
  const categoriesQuery = useCategories({ page: 0 });
  const currentCategoryQuery = useCategory(transaction?.categoryId);
  const createMutation = useCreateTransaction();
  const updateMutation = useUpdateTransaction(id ?? '');
  const [submitError, setSubmitError] = useState<string | null>(null);

  const accounts = useMemo<Account[]>(
    () => mergeById(accountsQuery.data?.content ?? [], currentAccountQuery.data),
    [accountsQuery.data?.content, currentAccountQuery.data],
  );
  const categories = useMemo<Category[]>(
    () => mergeById(categoriesQuery.data?.content ?? [], currentCategoryQuery.data),
    [categoriesQuery.data?.content, currentCategoryQuery.data],
  );

  async function submit(data: TransactionFormData) {
    setSubmitError(null);
    const selectedAccount = accounts.find((account) => account.id === data.accountId);
    const previousImpact: PreviousBalanceImpact | undefined = transaction
      ? {
          accountId: transaction.accountId,
          amount: Number(transaction.amount),
          direction: transaction.type === 'EXPENSE' && transaction.status === 'PAID' ? 'debit' : transaction.type === 'INCOME' && transaction.status === 'RECEIVED' ? 'credit' : 'none',
        }
      : undefined;

    if (data.type === 'EXPENSE' && data.status === 'PAID' && selectedAccount && !hasSufficientBalance(selectedAccount, data.amount, previousImpact)) {
      setSubmitError(insufficientBalanceMessage(selectedAccount));
      return;
    }

    const payload = {
      accountId: data.accountId || null,
      amount: data.amount,
      categoryId: data.categoryId,
      description: data.description,
      notes: data.notes?.trim() || null,
      status: data.status,
      transactionDate: data.transactionDate,
      type: data.type,
    };

    try {
      if (isEdit && id) {
        await updateMutation.mutateAsync(payload);
      } else {
        await createMutation.mutateAsync(payload);
      }
      navigate('/transactions', { replace: true });
    } catch (error) {
      setSubmitError(apiErrorMessage(error, 'Nao foi possivel salvar a transacao.'));
    }
  }

  if (isEdit && transactionQuery.isLoading) {
    return <div className="app-panel-muted p-8 text-center text-sm font-medium text-fuchsia-50">Carregando transacao...</div>;
  }

  if (isEdit && (transactionQuery.isError || !transaction)) {
    return (
      <section className="rounded-lg border border-rose-300/25 bg-rose-400/10 p-6">
        <h1 className="font-serif text-2xl font-semibold text-rose-100">Transacao nao encontrada</h1>
        <p className="mt-2 text-sm text-rose-200">Nao foi possivel carregar este lancamento.</p>
        <Link className="mt-4 inline-flex font-semibold text-rose-100" to="/transactions">
          Voltar para transacoes
        </Link>
      </section>
    );
  }

  return (
    <section className="space-y-6">
      <Link className="inline-flex items-center gap-2 text-sm font-semibold text-fuchsia-100 hover:text-white" to="/transactions">
        <ArrowLeft size={16} /> Voltar para transacoes
      </Link>

      <div className="app-page-hero">
        <div className="admin-flow-line" aria-hidden="true" />
        <div className="relative max-w-3xl">
          <p className="app-eyebrow">Transacoes</p>
          <h1 className="app-hero-title mt-4">{isEdit ? 'Editar lancamento' : 'Novo lancamento'}</h1>
          <p className="app-hero-copy mt-4 max-w-xl">
            {isEdit ? 'Ajuste dados, status e conta vinculada mantendo o saldo coerente.' : 'Cadastre uma receita ou despesa e escolha quando ela deve impactar o saldo.'}
          </p>
        </div>
      </div>

      <TransactionForm
        accounts={accounts}
        categories={categories}
        isLoading={createMutation.isPending || updateMutation.isPending}
        mode={isEdit ? 'edit' : 'create'}
        onSubmit={submit}
        submitError={submitError}
        transaction={transaction}
      />
    </section>
  );
}
