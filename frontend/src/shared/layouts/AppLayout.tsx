import { Link, Outlet, useLocation } from 'react-router';

import { useAuth } from '../../features/auth/hooks/useAuth';
import { Button } from '../ui/Button';

export function AppLayout() {
  const location = useLocation();
  const { isAdmin, logout } = useAuth();
  const isAdminRoute = location.pathname.startsWith('/admin');

  return (
    <div className={isAdminRoute ? 'admin-app-shell min-h-screen text-white' : 'min-h-screen bg-violet-50 text-slate-950'}>
      <header className={isAdminRoute ? 'border-b border-white/10 bg-white/5 backdrop-blur' : 'border-b border-fuchsia-100 bg-white'}>
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-4">
          <strong className={isAdminRoute ? 'font-serif text-xl text-white' : 'font-serif text-xl text-slate-950'}>Zorysa Finance</strong>
          <nav className={isAdminRoute ? 'flex items-center gap-4 text-sm text-fuchsia-100' : 'flex items-center gap-4 text-sm text-slate-600'}>
            <Link
              className={
                location.pathname === '/dashboard'
                  ? isAdminRoute
                    ? 'font-semibold text-fuchsia-300'
                    : 'font-semibold text-fuchsia-700'
                  : isAdminRoute
                    ? 'hover:text-fuchsia-300'
                    : 'hover:text-fuchsia-700'
              }
              to="/dashboard"
            >
              Dashboard
            </Link>
            {isAdmin ? (
              <Link className={isAdminRoute ? 'font-semibold text-white' : 'hover:text-fuchsia-700'} to="/admin/users/pending">
                Admin
              </Link>
            ) : null}
            <Button className={isAdminRoute ? 'min-h-9 border-white/20 bg-white/10 px-3 py-1.5 text-white hover:bg-white/20' : 'min-h-9 px-3 py-1.5'} onClick={() => void logout()} type="button" variant="secondary">
              Sair
            </Button>
          </nav>
        </div>
      </header>
      <main className={isAdminRoute ? 'relative mx-auto max-w-6xl px-4 py-8' : 'mx-auto max-w-6xl px-4 py-8'}>
        <Outlet />
      </main>
    </div>
  );
}
