import { AxiosError } from 'axios';
import { LockKeyhole, Mail } from 'lucide-react';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useLocation, useNavigate } from 'react-router';

import { Button } from '../../../shared/ui/Button';
import { FormField } from '../../../shared/ui/FormField';
import { AuthCard } from '../components/AuthCard';
import { useAuth } from '../hooks/useAuth';
import { loginSchema, type LoginFormData } from '../schemas/auth.schemas';
import type { ApiErrorResponse } from '../types/auth.types';
import { accountStatusPath } from '../utils/account-status';

type LoginLocationState = {
  from?: {
    pathname?: string;
  };
};

export function LoginPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const { login } = useAuth();
  const [submitError, setSubmitError] = useState<string | null>(null);
  const {
    formState: { errors, isSubmitting },
    handleSubmit,
    register,
    setError,
  } = useForm<LoginFormData>();

  async function onSubmit(data: LoginFormData) {
    setSubmitError(null);
    const parsed = loginSchema.safeParse(data);

    if (!parsed.success) {
      parsed.error.issues.forEach((issue) => {
        setError(issue.path[0] as keyof LoginFormData, { message: issue.message });
      });
      return;
    }

    try {
      await login(parsed.data);
      const state = location.state as LoginLocationState | null;
      navigate(state?.from?.pathname ?? '/dashboard', { replace: true });
    } catch (error) {
      if (error instanceof AxiosError && error.response?.status === 403) {
        const data = error.response.data as ApiErrorResponse;
        const statusPath = accountStatusPath(data.userStatus);

        if (statusPath) {
          navigate(statusPath, {
            replace: true,
            state: {
              email: parsed.data.email,
              message: data.message,
            },
          });
          return;
        }
      }

      const message = error instanceof AxiosError ? (error.response?.data as ApiErrorResponse | undefined)?.message : null;
      setSubmitError(message ?? 'E-mail ou senha invalidos.');
    }
  }

  return (
    <AuthCard
      eyebrow="Sessao segura"
      footer={
        <div className="flex flex-wrap items-center justify-between gap-3">
          <Link className="font-semibold text-fuchsia-300 hover:text-fuchsia-200" to="/forgot-password">
            Esqueci minha senha
          </Link>
          <Link className="font-semibold text-fuchsia-300 hover:text-fuchsia-200" to="/register">
            Solicitar acesso
          </Link>
        </div>
      }
      title="Entre para organizar seu dinheiro"
    >
      <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
        <FormField autoComplete="email" error={errors.email?.message} id="email" label="E-mail" leadingIcon={<Mail size={18} />} type="email" {...register('email')} />
        <FormField autoComplete="current-password" error={errors.password?.message} id="password" label="Senha" leadingIcon={<LockKeyhole size={18} />} type="password" {...register('password')} />
        {submitError ? <p className="rounded-lg border border-rose-300/25 bg-rose-400/10 px-3 py-2 text-sm font-medium text-rose-200">{submitError}</p> : null}
        <Button className="w-full" isLoading={isSubmitting} type="submit">
          Entrar
        </Button>
      </form>
    </AuthCard>
  );
}
