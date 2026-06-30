import { z } from 'zod';

export const payInvoiceSchema = z.object({
  paidAt: z.string().min(1, 'Informe a data do pagamento.'),
  paymentAccountId: z.string().min(1, 'Selecione uma conta financeira.'),
});

export type PayInvoiceData = z.infer<typeof payInvoiceSchema>;
