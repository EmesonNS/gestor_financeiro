import { z } from 'zod';

export const loginSchema = z.object({
  email: z.string().email('Informe um e-mail valido.'),
  password: z.string().min(1, 'Informe sua senha.'),
});

export const registerSchema = z.object({
  name: z.string().min(2, 'Informe seu nome.'),
  email: z.string().email('Informe um e-mail valido.'),
  password: z.string().min(6, 'Use pelo menos 6 caracteres.'),
});

export const forgotPasswordSchema = z.object({
  email: z.string().email('Informe um e-mail valido.'),
});

export type LoginFormData = z.infer<typeof loginSchema>;
export type RegisterFormData = z.infer<typeof registerSchema>;
export type ForgotPasswordFormData = z.infer<typeof forgotPasswordSchema>;
