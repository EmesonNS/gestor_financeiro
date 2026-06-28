import { z } from 'zod';

import { transactionStatuses, transactionTypes } from '../utils/transaction-format';

export const transactionFormSchema = z
  .object({
    accountId: z.string().optional(),
    amount: z.coerce.number({ error: 'Informe o valor.' }).positive('O valor deve ser maior que zero.'),
    categoryId: z.string().min(1, 'Selecione uma categoria.'),
    description: z.string().trim().min(1, 'Informe a descricao.').max(180, 'Use no maximo 180 caracteres.'),
    notes: z.string().trim().max(2000, 'Use no maximo 2000 caracteres.').optional(),
    status: z.enum(transactionStatuses, { error: 'Selecione o status.' }),
    transactionDate: z.string().min(1, 'Informe a data.'),
    type: z.enum(transactionTypes, { error: 'Selecione o tipo.' }),
  })
  .superRefine((data, ctx) => {
    if (data.type === 'EXPENSE' && data.status === 'RECEIVED') {
      ctx.addIssue({ code: 'custom', message: 'Despesa nao pode ser recebida.', path: ['status'] });
    }

    if (data.type === 'INCOME' && data.status === 'PAID') {
      ctx.addIssue({ code: 'custom', message: 'Receita nao pode ser paga.', path: ['status'] });
    }

    const isRealized = (data.type === 'EXPENSE' && data.status === 'PAID') || (data.type === 'INCOME' && data.status === 'RECEIVED');
    if (isRealized && !data.accountId) {
      ctx.addIssue({ code: 'custom', message: 'Selecione uma conta para lancamentos realizados.', path: ['accountId'] });
    }
  });

export type TransactionFormData = z.infer<typeof transactionFormSchema>;
