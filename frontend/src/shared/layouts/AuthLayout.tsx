import { Outlet } from 'react-router';

export function AuthLayout() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
      <Outlet />
    </main>
  );
}
