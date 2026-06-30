import { Link, Outlet, useLocation } from 'react-router';

import { useAuth } from '../../features/auth/hooks/useAuth';
import { Button } from '../ui/Button';

export function AppLayout() {
  const location = useLocation();
  const { isAdmin, logout } = useAuth();
  const isAdminRoute = location.pathname.startsWith('/admin');
  const isCreditCardRoute = location.pathname.startsWith('/credit-cards') || location.pathname.startsWith('/card-purchases');

  return (
    <div className="app-shell min-h-screen text-white">
      <header className="border-b border-white/10 bg-white/5 backdrop-blur">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-4">
          <strong className="font-serif text-xl text-white">Zorysa Finance</strong>
          <nav className="flex items-center gap-4 text-sm text-fuchsia-100">
            <Link
              className={
                location.pathname === '/dashboard'
                  ? 'font-semibold text-white'
                  : 'hover:text-white'
              }
              to="/dashboard"
            >
              Dashboard
            </Link>
            <Link
              className={
                location.pathname.startsWith('/accounts')
                  ? 'font-semibold text-white'
                  : 'hover:text-white'
              }
              to="/accounts"
            >
              Contas
            </Link>
            <Link
              className={
                location.pathname.startsWith('/categories')
                  ? 'font-semibold text-white'
                  : 'hover:text-white'
              }
              to="/categories"
            >
              Categorias
            </Link>
            <Link
              className={
                location.pathname.startsWith('/transactions')
                  ? 'font-semibold text-white'
                  : 'hover:text-white'
              }
              to="/transactions"
            >
              Transacoes
            </Link>
            <Link
              className={
                location.pathname.startsWith('/bills')
                  ? 'font-semibold text-white'
                  : 'hover:text-white'
              }
              to="/bills"
            >
              A pagar
            </Link>
            <Link
              className={
                location.pathname.startsWith('/budgets')
                  ? 'font-semibold text-white'
                  : 'hover:text-white'
              }
              to="/budgets"
            >
              Orcamentos
            </Link>
            <Link
              className={
                location.pathname.startsWith('/goals')
                  ? 'font-semibold text-white'
                  : 'hover:text-white'
              }
              to="/goals"
            >
              Metas
            </Link>
            <Link
              className={
                isCreditCardRoute
                  ? 'font-semibold text-white'
                  : 'hover:text-white'
              }
              to="/credit-cards"
            >
              Cartoes
            </Link>
            <Link
              className={
                location.pathname.startsWith('/installments')
                  ? 'font-semibold text-white'
                  : 'hover:text-white'
              }
              to="/installments/future"
            >
              Parcelas
            </Link>
            <Link
              className={
                location.pathname.startsWith('/reports')
                  ? 'font-semibold text-white'
                  : 'hover:text-white'
              }
              to="/reports"
            >
              Relatorios
            </Link>
            <Link
              className={
                location.pathname === '/profile'
                  ? 'font-semibold text-white'
                  : 'hover:text-white'
              }
              to="/profile"
            >
              Perfil
            </Link>
            {isAdmin ? (
              <Link className={isAdminRoute ? 'font-semibold text-white' : 'hover:text-white'} to="/admin/users/pending">
                Admin
              </Link>
            ) : null}
            <Button className="min-h-9 border-white/20 bg-white/10 px-3 py-1.5 text-white hover:bg-white/20" onClick={() => void logout()} type="button" variant="secondary">
              Sair
            </Button>
          </nav>
        </div>
      </header>
      <main className="relative mx-auto max-w-6xl px-4 py-8">
        <Outlet />
      </main>
    </div>
  );
}
