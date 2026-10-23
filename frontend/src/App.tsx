import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Navbar } from './components/layout/Navbar';
import { LoginPage } from './pages/LoginPage';
import { DashboardPage } from './pages/DashboardPage';
import { ClaimsListPage } from './pages/ClaimsListPage';
import { CreateClaimPage } from './pages/CreateClaimPage';
import { ClaimDetailPage } from './pages/ClaimDetailPage';
import { InvestigationPage } from './pages/InvestigationPage';
import { UsersPage } from './pages/UsersPage';

const ProtectedLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center text-slate-400 text-sm">
        Authenticating session...
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      <Navbar />
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-8">
        {children}
      </main>
      <footer className="bg-white border-t border-slate-200 py-4 text-center text-xs text-slate-400">
        ClaimGuard Benefits Fraud Detection System • SDE-1 Portfolio Project • Spring Boot 3 & React
      </footer>
    </div>
  );
};

const AdminRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAdmin, loading } = useAuth();

  if (loading) return null;

  if (!isAdmin) {
    return <Navigate to="/dashboard" replace />;
  }

  return <>{children}</>;
};

export const App: React.FC = () => {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />

          <Route
            path="/dashboard"
            element={
              <ProtectedLayout>
                <DashboardPage />
              </ProtectedLayout>
            }
          />

          <Route
            path="/claims"
            element={
              <ProtectedLayout>
                <ClaimsListPage />
              </ProtectedLayout>
            }
          />

          <Route
            path="/claims/new"
            element={
              <ProtectedLayout>
                <CreateClaimPage />
              </ProtectedLayout>
            }
          />

          <Route
            path="/claims/:id"
            element={
              <ProtectedLayout>
                <ClaimDetailPage />
              </ProtectedLayout>
            }
          />

          <Route
            path="/claims/:id/investigate"
            element={
              <ProtectedLayout>
                <InvestigationPage />
              </ProtectedLayout>
            }
          />

          <Route
            path="/users"
            element={
              <ProtectedLayout>
                <AdminRoute>
                  <UsersPage />
                </AdminRoute>
              </ProtectedLayout>
            }
          />

          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
};

export default App;
