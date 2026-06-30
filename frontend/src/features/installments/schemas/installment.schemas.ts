import { z } from 'zod';

export const cardPurchaseFormSchema = z.object({
  categoryId: z.string().min(1, 'Selecione uma categoria.'),
  description: z.string().trim().min(1, 'Informe a descricao da compra.'),
  installmentCount: z.coerce.number({ error: 'Informe a quantidade de parcelas.' }).int('Use um numero inteiro.').positive('Use pelo menos uma parcela.'),
  notes: z.string().trim().max(2000, 'Use no maximo 2000 caracteres.').optional(),
  purchaseDate: z.string().min(1, 'Informe a data da compra.'),
  totalAmount: z.coerce.number({ error: 'Informe o valor total.' }).positive('O valor deve ser maior que zero.'),
});

export type CardPurchaseFormData = z.infer<typeof cardPurchaseFormSchema>;
