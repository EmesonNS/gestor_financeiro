import { Navigate, Route, Routes } from 'react-router';

import { AccountStatusPage } from '../../features/auth/pages/AccountStatusPage';
import { ForgotPasswordPage } from '../../features/auth/pages/ForgotPasswordPage';
import { LoginPage } from '../../features/auth/pages/LoginPage';
import { RegisterPage } from '../../features/auth/pages/RegisterPage';
import { DashboardPage } from '../../features/dashboard/pages/DashboardPage';
import { AppLayout } from '../../shared/layouts/AppLayout';
import { AuthLayout } from '../../shared/layouts/AuthLayout';
import { ProtectedRoute } from './ProtectedRoute';

export function AppRoutes() {
  return (
    <Routes>
      <Route element={<AuthLayout />}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/account-status/pending" element={<AccountStatusPage status="PENDING_APPROVAL" />} />
        <Route path="/account-status/suspended" element={<AccountStatusPage status="SUSPENDED" />} />
        <Route path="/account-status/rejected" element={<AccountStatusPage status="REJECTED" />} />
        <Route path="/account-status/unavailable" element={<AccountStatusPage status="DELETED" />} />
      </Route>

      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route path="/dashboard" element={<DashboardPage />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}
