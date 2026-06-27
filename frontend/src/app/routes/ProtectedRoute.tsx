import { useEffect } from 'react';
import { Navigate, Outlet, useLocation } from 'react-router';

import { useAuth } from '../../features/auth/hooks/useAuth';
import type { UserRole } from '../../features/auth/types/auth.types';

type ProtectedRouteProps = {
  requiredRole?: UserRole;
};

export function ProtectedRoute({ requiredRole }: ProtectedRouteProps) {
  const location = useLocation();
  const { isAuthenticated, restoreSession, status, user } = useAuth();

  useEffect(() => {
    if (status === 'checking') {
      void restoreSession();
    }
  }, [restoreSession, status]);

  if (status === 'checking') {
    return (
      <main className="grid min-h-screen place-items-center bg-slate-950 text-fuchsia-100">
        <div className="rounded-lg border border-white/10 bg-white/10 px-5 py-4 text-sm font-medium">Validando sessao...</div>
      </main>
    );
  }

  if (!isAuthenticated) {
    return <Navigate replace state={{ from: location }} to="/login" />;
  }

  if (requiredRole && user?.role !== requiredRole) {
    return <Navigate replace to="/dashboard" />;
  }

  return <Outlet />;
}
