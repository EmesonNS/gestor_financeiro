import { AxiosError } from 'axios';
import { Mail } from 'lucide-react';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link } from 'react-router';

import { Button } from '../../../shared/ui/Button';
import { FormField } from '../../../shared/ui/FormField';
import { AuthCard } from '../components/AuthCard';
import { forgotPasswordSchema, type ForgotPasswordFormData } from '../schemas/auth.schemas';
import { useAuth } from '../hooks/useAuth';

export function ForgotPasswordPage() {
  const { forgotPassword } = useAuth();
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [sent, setSent] = useState(false);
  const {
    formState: { errors, isSubmitting },
    handleSubmit,
    register,
    setError,
  } = useForm<ForgotPasswordFormData>();

  async function onSubmit(data: ForgotPasswordFormData) {
    setSubmitError(null);
    const parsed = forgotPasswordSchema.safeParse(data);

    if (!parsed.success) {
      parsed.error.issues.forEach((issue) => {
        setError(issue.path[0] as keyof ForgotPasswordFormData, { message: issue.message });
      });
      return;
    }

    try {
      await forgotPassword(parsed.data);
      setSent(true);
    } catch (error) {
      const message = error instanceof AxiosError ? error.response?.data?.message : null;
      setSubmitError(message ?? 'Nao foi possivel enviar a recuperacao agora.');
    }
  }

  return (
    <AuthCard
      eyebrow="Recuperacao"
      footer={
        <Link className="font-semibold text-fuchsia-700 hover:text-fuchsia-600" to="/login">
          Voltar para login
        </Link>
      }
      title="Recupere seu acesso"
    >
      {sent ? (
        <div className="rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-800">
          Se o e-mail estiver cadastrado, enviaremos as instrucoes de recuperacao.
        </div>
      ) : (
        <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
          <FormField autoComplete="email" error={errors.email?.message} id="email" label="E-mail" leadingIcon={<Mail size={18} />} type="email" {...register('email')} />
          {submitError ? <p className="rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-sm font-medium text-rose-700">{submitError}</p> : null}
          <Button className="w-full" isLoading={isSubmitting} type="submit">
            Enviar instrucoes
          </Button>
        </form>
      )}
    </AuthCard>
  );
}
