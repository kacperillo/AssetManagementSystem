import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { CssBaseline, ThemeProvider, createTheme } from '@mui/material';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/routing/ProtectedRoute';
import AdminRoute from './components/routing/AdminRoute';
import Layout from './components/layout/Layout';
import LoginPage from './pages/LoginPage';
import EmployeesPage from './pages/EmployeesPage';
import AssetsPage from './pages/AssetsPage';
import AssignmentsPage from './pages/AssignmentsPage';
import MyAssetsPage from './pages/MyAssetsPage';
import MyHistoryPage from './pages/MyHistoryPage';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
    },
  },
});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <AuthProvider>
          <BrowserRouter>
            <Routes>
              {/* Trasa publiczna */}
              <Route path="/login" element={<LoginPage />} />

              {/* Trasy chronione */}
              <Route element={<ProtectedRoute />}>
                <Route element={<Layout />}>
                  {/* Trasy ADMIN */}
                  <Route element={<AdminRoute />}>
                    <Route path="/employees" element={<EmployeesPage />} />
                    <Route path="/assets" element={<AssetsPage />} />
                    <Route path="/assignments" element={<AssignmentsPage />} />
                  </Route>

                  {/* Trasy ADMIN + EMPLOYEE */}
                  <Route path="/my-assets" element={<MyAssetsPage />} />
                  <Route path="/my-history" element={<MyHistoryPage />} />
                </Route>
              </Route>

              {/* Domyślne przekierowanie */}
              <Route path="*" element={<Navigate to="/login" replace />} />
            </Routes>
          </BrowserRouter>
        </AuthProvider>
      </ThemeProvider>
    </QueryClientProvider>
  );
}

export default App;
