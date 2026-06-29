import { z } from 'zod';

export const budgetDurationModes = ['SINGLE', 'RANGE', 'FOREVER'] as const;

export const budgetFormSchema = z
  .object({
    categoryId: z.string().min(1, 'Selecione uma categoria de despesa.'),
    durationMode: z.enum(budgetDurationModes),
    endMonth: z.coerce.number().min(1, 'Selecione o mes final.').max(12, 'Selecione um mes final valido.').optional(),
    endYear: z.coerce.number().min(2000, 'Informe um ano final valido.').max(2100, 'Informe um ano final valido.').optional(),
    limitAmount: z.coerce.number({ error: 'Informe o limite.' }).positive('O limite deve ser maior que zero.'),
    startMonth: z.coerce.number({ error: 'Informe o mes inicial.' }).min(1, 'Selecione o mes inicial.').max(12, 'Selecione um mes inicial valido.'),
    startYear: z.coerce.number({ error: 'Informe o ano inicial.' }).min(2000, 'Informe um ano inicial valido.').max(2100, 'Informe um ano inicial valido.'),
  })
  .superRefine((data, ctx) => {
    if (data.durationMode !== 'RANGE') {
      return;
    }

    if (!data.endMonth) {
      ctx.addIssue({ code: 'custom', message: 'Selecione o mes final.', path: ['endMonth'] });
    }

    if (!data.endYear) {
      ctx.addIssue({ code: 'custom', message: 'Informe o ano final.', path: ['endYear'] });
    }

    if (data.endMonth && data.endYear && data.endYear * 12 + data.endMonth < data.startYear * 12 + data.startMonth) {
      ctx.addIssue({ code: 'custom', message: 'O fim deve ser igual ou posterior ao inicio.', path: ['endMonth'] });
    }
  });

export type BudgetFormData = z.infer<typeof budgetFormSchema>;
