import { z } from 'zod';

import { billStatuses } from '../utils/bill-format';

export const billFormSchema = z
  .object({
    accountId: z.string().optional(),
    amount: z.coerce.number({ error: 'Informe o valor.' }).positive('O valor deve ser maior que zero.'),
    categoryId: z.string().min(1, 'Selecione uma categoria de despesa.'),
    description: z.string().trim().min(1, 'Informe a descricao.').max(180, 'Use no maximo 180 caracteres.'),
    dueDate: z.string().min(1, 'Informe a data de vencimento.'),
    status: z.enum(billStatuses, { error: 'Selecione o status.' }),
  })
  .superRefine((data, ctx) => {
    if (data.status === 'PAID' && !data.accountId) {
      ctx.addIssue({ code: 'custom', message: 'Selecione uma conta para contas pagas.', path: ['accountId'] });
    }
  });

export const payBillSchema = z.object({
  accountId: z.string().min(1, 'Selecione a conta do pagamento.'),
  paidAt: z.string().min(1, 'Informe a data de pagamento.'),
});

export type BillFormData = z.infer<typeof billFormSchema>;
export type PayBillData = z.infer<typeof payBillSchema>;
