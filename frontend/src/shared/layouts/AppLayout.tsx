import { Outlet } from 'react-router';

import { useAuth } from '../../features/auth/hooks/useAuth';
import { Button } from '../ui/Button';

export function AppLayout() {
  const { logout } = useAuth();

  return (
    <div className="min-h-screen bg-violet-50 text-slate-950">
      <header className="border-b border-fuchsia-100 bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-4">
          <strong className="font-serif text-xl text-slate-950">Zorysa Finance</strong>
          <nav className="flex items-center gap-4 text-sm text-slate-600">
            <span>Dashboard</span>
            <Button className="min-h-9 px-3 py-1.5" onClick={() => void logout()} type="button" variant="secondary">
              Sair
            </Button>
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-8">
        <Outlet />
      </main>
    </div>
  );
}
