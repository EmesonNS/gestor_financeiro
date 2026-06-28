import { z } from 'zod';

import { categoryTypes } from '../utils/category-format';

export const categoryFormSchema = z.object({
  color: z.string().regex(/^#[0-9A-Fa-f]{6}$/, 'Use uma cor hexadecimal valida.').optional().or(z.literal('')),
  icon: z.string().trim().max(80, 'Use no maximo 80 caracteres.').optional(),
  name: z.string().trim().min(1, 'Informe o nome da categoria.').max(100, 'Use no maximo 100 caracteres.'),
  type: z.enum(categoryTypes, { error: 'Selecione o tipo da categoria.' }),
});

export type CategoryFormData = z.infer<typeof categoryFormSchema>;
