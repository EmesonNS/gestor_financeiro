import { z } from 'zod';

export const goalFormSchema = z.object({
  currentAmount: z.coerce.number({ error: 'Informe o valor atual.' }).min(0, 'O valor atual deve ser maior ou igual a zero.'),
  deadline: z.string().optional(),
  description: z.string().trim().max(2000, 'Use no maximo 2000 caracteres.').optional(),
  name: z.string().trim().min(1, 'Informe o nome da meta.').max(120, 'Use no maximo 120 caracteres.'),
  targetAmount: z.coerce.number({ error: 'Informe o valor alvo.' }).positive('O valor alvo deve ser maior que zero.'),
});

export const goalProgressSchema = z.object({
  currentAmount: z.coerce.number({ error: 'Informe o valor atual.' }).min(0, 'O valor atual deve ser maior ou igual a zero.'),
});

export type GoalFormData = z.infer<typeof goalFormSchema>;
export type GoalProgressData = z.infer<typeof goalProgressSchema>;
