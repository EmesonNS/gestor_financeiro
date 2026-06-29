import { z } from 'zod';

export const creditCardFormSchema = z.object({
  closingDay: z.coerce.number({ error: 'Informe o dia de fechamento.' }).int('Use um dia inteiro.').min(1, 'Use um dia entre 1 e 31.').max(31, 'Use um dia entre 1 e 31.'),
  dueDay: z.coerce.number({ error: 'Informe o dia de vencimento.' }).int('Use um dia inteiro.').min(1, 'Use um dia entre 1 e 31.').max(31, 'Use um dia entre 1 e 31.'),
  limitAmount: z.coerce.number({ error: 'Informe o limite.' }).min(0, 'O limite deve ser maior ou igual a zero.'),
  name: z.string().trim().min(1, 'Informe o nome do cartao.').max(120, 'Use no maximo 120 caracteres.'),
});

export type CreditCardFormData = z.infer<typeof creditCardFormSchema>;
