import { AxiosError } from 'axios';
import { KeyRound, Save, UserRound } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';

import { useAuth } from '../../auth/hooks/useAuth';
import type { ApiErrorResponse } from '../../auth/types/auth.types';
import { Button } from '../../../shared/ui/Button';
import { FormField } from '../../../shared/ui/FormField';
import { ProfileSummary } from '../components/ProfileSummary';
import { useChangePassword, useProfile, useUpdateProfile } from '../hooks/useProfile';
import { changePasswordSchema, updateProfileSchema, type ChangePasswordFormData, type UpdateProfileFormData } from '../schemas/profile.schemas';

export function ProfilePage() {
  const { updateAuthenticatedUser } = useAuth();
  const profileQuery = useProfile();
  const updateProfileMutation = useUpdateProfile();
  const changePasswordMutation = useChangePassword();
  const [profileMessage, setProfileMessage] = useState<string | null>(null);
  const [profileError, setProfileError] = useState<string | null>(null);
  const [passwordMessage, setPasswordMessage] = useState<string | null>(null);
  const [passwordError, setPasswordError] = useState<string | null>(null);

  const {
    formState: { errors: profileErrors },
    handleSubmit: handleProfileSubmit,
    register: registerProfile,
    reset: resetProfile,
    setError: setProfileFieldError,
  } = useForm<UpdateProfileFormData>();

  const {
    formState: { errors: passwordErrors },
    handleSubmit: handlePasswordSubmit,
    register: registerPassword,
    reset: resetPassword,
    setError: setPasswordFieldError,
  } = useForm<ChangePasswordFormData>();

  useEffect(() => {
    if (profileQuery.data) {
      resetProfile({ name: profileQuery.data.name });
    }
  }, [profileQuery.data, resetProfile]);

  async function onUpdateProfile(data: UpdateProfileFormData) {
    setProfileError(null);
    setProfileMessage(null);
    const parsed = updateProfileSchema.safeParse(data);

    if (!parsed.success) {
      parsed.error.issues.forEach((issue) => {
        setProfileFieldError(issue.path[0] as keyof UpdateProfileFormData, { message: issue.message });
      });
      return;
    }

    try {
      const profile = await updateProfileMutation.mutateAsync(parsed.data);
      updateAuthenticatedUser({ name: profile.name });
      setProfileMessage('Perfil atualizado.');
    } catch (error) {
      const message = error instanceof AxiosError ? (error.response?.data as ApiErrorResponse | undefined)?.message : null;
      setProfileError(message ?? 'Nao foi possivel atualizar seu perfil.');
    }
  }

  async function onChangePassword(data: ChangePasswordFormData) {
    setPasswordError(null);
    setPasswordMessage(null);
    const parsed = changePasswordSchema.safeParse(data);

    if (!parsed.success) {
      parsed.error.issues.forEach((issue) => {
        setPasswordFieldError(issue.path[0] as keyof ChangePasswordFormData, { message: issue.message });
      });
      return;
    }

    try {
      await changePasswordMutation.mutateAsync({
        currentPassword: parsed.data.currentPassword,
        newPassword: parsed.data.newPassword,
      });
      resetPassword({ confirmPassword: '', currentPassword: '', newPassword: '' });
      setPasswordMessage('Senha alterada.');
    } catch (error) {
      const message = error instanceof AxiosError ? (error.response?.data as ApiErrorResponse | undefined)?.message : null;
      setPasswordError(message ?? 'Nao foi possivel alterar sua senha.');
    }
  }

  if (profileQuery.isLoading) {
    return <div className="app-panel-muted p-8 text-center text-sm font-medium text-fuchsia-50">Carregando perfil...</div>;
  }

  if (profileQuery.isError || !profileQuery.data) {
    return (
      <section className="rounded-lg border border-rose-300/25 bg-rose-400/10 p-6">
        <h1 className="font-serif text-2xl font-semibold text-rose-100">Perfil indisponivel</h1>
        <p className="mt-2 text-sm text-rose-200">Nao foi possivel carregar seus dados agora.</p>
      </section>
    );
  }

  return (
    <section className="space-y-6">
      <div className="app-page-hero">
        <div className="admin-flow-line" aria-hidden="true" />
        <div className="relative max-w-3xl">
          <p className="app-eyebrow">Perfil</p>
          <h1 className="app-hero-title mt-4">Dados da conta</h1>
          <p className="app-hero-copy mt-4 max-w-xl">Atualize seu nome de exibicao e mantenha sua senha em dia.</p>
        </div>
      </div>

      <div className="grid gap-5 lg:grid-cols-[minmax(0,0.8fr)_minmax(0,1.2fr)]">
        <ProfileSummary profile={profileQuery.data} />

        <div className="grid gap-5">
          <section className="app-panel p-6">
            <div className="flex items-center gap-3">
              <span className="flex h-10 w-10 items-center justify-center rounded-lg border border-white/10 bg-fuchsia-400/15 text-fuchsia-200">
                <UserRound size={20} />
              </span>
              <div>
                <h2 className="font-serif text-2xl font-semibold text-slate-950">Identidade</h2>
                <p className="text-sm text-slate-600">Este nome aparece nas areas autenticadas.</p>
              </div>
            </div>

            <form className="mt-5 space-y-4" onSubmit={handleProfileSubmit(onUpdateProfile)}>
              <FormField autoComplete="name" error={profileErrors.name?.message} id="name" label="Nome" {...registerProfile('name')} />
              {profileError ? <p className="rounded-lg border border-rose-300/25 bg-rose-400/10 px-3 py-2 text-sm font-medium text-rose-200">{profileError}</p> : null}
              {profileMessage ? <p className="rounded-lg border border-emerald-300/25 bg-emerald-400/10 px-3 py-2 text-sm font-medium text-emerald-200">{profileMessage}</p> : null}
              <Button isLoading={updateProfileMutation.isPending} type="submit">
                <Save size={17} /> Salvar perfil
              </Button>
            </form>
          </section>

          <section className="app-panel p-6">
            <div className="flex items-center gap-3">
              <span className="flex h-10 w-10 items-center justify-center rounded-lg border border-white/10 bg-fuchsia-400/15 text-fuchsia-200">
                <KeyRound size={20} />
              </span>
              <div>
                <h2 className="font-serif text-2xl font-semibold text-slate-950">Seguranca</h2>
                <p className="text-sm text-slate-600">Altere a senha usando sua senha atual.</p>
              </div>
            </div>

            <form className="mt-5 grid gap-4 sm:grid-cols-2" onSubmit={handlePasswordSubmit(onChangePassword)}>
              <div className="sm:col-span-2">
                <FormField autoComplete="current-password" error={passwordErrors.currentPassword?.message} id="currentPassword" label="Senha atual" type="password" {...registerPassword('currentPassword')} />
              </div>
              <FormField autoComplete="new-password" error={passwordErrors.newPassword?.message} id="newPassword" label="Nova senha" type="password" {...registerPassword('newPassword')} />
              <FormField autoComplete="new-password" error={passwordErrors.confirmPassword?.message} id="confirmPassword" label="Confirmar senha" type="password" {...registerPassword('confirmPassword')} />
              {passwordError ? <p className="rounded-lg border border-rose-300/25 bg-rose-400/10 px-3 py-2 text-sm font-medium text-rose-200 sm:col-span-2">{passwordError}</p> : null}
              {passwordMessage ? <p className="rounded-lg border border-emerald-300/25 bg-emerald-400/10 px-3 py-2 text-sm font-medium text-emerald-200 sm:col-span-2">{passwordMessage}</p> : null}
              <div className="sm:col-span-2">
                <Button isLoading={changePasswordMutation.isPending} type="submit">
                  <KeyRound size={17} /> Alterar senha
                </Button>
              </div>
            </form>
          </section>
        </div>
      </div>
    </section>
  );
}
