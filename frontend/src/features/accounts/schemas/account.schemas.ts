import { z } from 'zod';

import { accountTypes } from '../utils/account-format';

export const accountFormSchema = z.object({
  initialBalance: z.coerce.number({ error: 'Informe o saldo inicial.' }).min(0, 'O saldo inicial deve ser maior ou igual a zero.'),
  name: z.string().trim().min(1, 'Informe o nome da conta.').max(120, 'Use no maximo 120 caracteres.'),
  type: z.enum(accountTypes, { error: 'Selecione o tipo da conta.' }),
});

export type AccountFormData = z.infer<typeof accountFormSchema>;
