import { Navigate, Route, Routes } from 'react-router';

import { AdminUserDetailsPage } from '../../features/admin/pages/AdminUserDetailsPage';
import { AdminUsersPage } from '../../features/admin/pages/AdminUsersPage';
import { AccountFormPage } from '../../features/accounts/pages/AccountFormPage';
import { AccountsPage } from '../../features/accounts/pages/AccountsPage';
import { AccountStatusPage } from '../../features/auth/pages/AccountStatusPage';
import { ForgotPasswordPage } from '../../features/auth/pages/ForgotPasswordPage';
import { LoginPage } from '../../features/auth/pages/LoginPage';
import { RegisterPage } from '../../features/auth/pages/RegisterPage';
import { CategoriesPage } from '../../features/categories/pages/CategoriesPage';
import { CategoryFormPage } from '../../features/categories/pages/CategoryFormPage';
import { DashboardPage } from '../../features/dashboard/pages/DashboardPage';
import { ProfilePage } from '../../features/profile/pages/ProfilePage';
import { TransactionFormPage } from '../../features/transactions/pages/TransactionFormPage';
import { TransactionsPage } from '../../features/transactions/pages/TransactionsPage';
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
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/accounts" element={<AccountsPage />} />
          <Route path="/accounts/new" element={<AccountFormPage />} />
          <Route path="/accounts/:id/edit" element={<AccountFormPage />} />
          <Route path="/categories" element={<CategoriesPage />} />
          <Route path="/categories/new" element={<CategoryFormPage />} />
          <Route path="/categories/:id/edit" element={<CategoryFormPage />} />
          <Route path="/transactions" element={<TransactionsPage />} />
          <Route path="/transactions/new" element={<TransactionFormPage />} />
          <Route path="/transactions/:id/edit" element={<TransactionFormPage />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute requiredRole="ADMIN" />}>
        <Route element={<AppLayout />}>
          <Route path="/admin/users" element={<AdminUsersPage />} />
          <Route path="/admin/users/pending" element={<AdminUsersPage />} />
          <Route path="/admin/users/:id" element={<AdminUserDetailsPage />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}
