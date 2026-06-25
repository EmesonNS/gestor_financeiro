import { AxiosError } from 'axios';
import { Mail, UserRound, LockKeyhole } from 'lucide-react';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router';

import { Button } from '../../../shared/ui/Button';
import { FormField } from '../../../shared/ui/FormField';
import { AuthCard } from '../components/AuthCard';
import { registerSchema, type RegisterFormData } from '../schemas/auth.schemas';
import { useAuth } from '../hooks/useAuth';

export function RegisterPage() {
  const navigate = useNavigate();
  const { register: createAccount } = useAuth();
  const [submitError, setSubmitError] = useState<string | null>(null);
  const {
    formState: { errors, isSubmitting },
    handleSubmit,
    register,
    setError,
  } = useForm<RegisterFormData>();

  async function onSubmit(data: RegisterFormData) {
    setSubmitError(null);
    const parsed = registerSchema.safeParse(data);

    if (!parsed.success) {
      parsed.error.issues.forEach((issue) => {
        setError(issue.path[0] as keyof RegisterFormData, { message: issue.message });
      });
      return;
    }

    try {
      await createAccount(parsed.data);
      navigate('/login', { replace: true });
    } catch (error) {
      const message = error instanceof AxiosError ? error.response?.data?.message : null;
      setSubmitError(message ?? 'Nao foi possivel criar sua conta agora.');
    }
  }

  return (
    <AuthCard
      eyebrow="Conta protegida"
      footer={
        <>
          Ja tem conta?{' '}
          <Link className="font-semibold text-fuchsia-700 hover:text-fuchsia-600" to="/login">
            Entrar
          </Link>
        </>
      }
      title="Crie seu acesso financeiro"
    >
      <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
        <FormField autoComplete="name" error={errors.name?.message} id="name" label="Nome" leadingIcon={<UserRound size={18} />} {...register('name')} />
        <FormField autoComplete="email" error={errors.email?.message} id="email" label="E-mail" leadingIcon={<Mail size={18} />} type="email" {...register('email')} />
        <FormField autoComplete="new-password" error={errors.password?.message} id="password" label="Senha" leadingIcon={<LockKeyhole size={18} />} type="password" {...register('password')} />
        {submitError ? <p className="rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-sm font-medium text-rose-700">{submitError}</p> : null}
        <Button className="w-full" isLoading={isSubmitting} type="submit">
          Criar conta
        </Button>
      </form>
    </AuthCard>
  );
}
