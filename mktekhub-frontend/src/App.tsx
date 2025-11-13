import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { AuthProvider } from "./contexts/AuthContext";
import { ToastProvider } from "./contexts/ToastContext";
import { ProtectedRoute } from "./components/common/ProtectedRoute";
import { ToastContainer } from "./components/common";
import { Layout } from "./components/layout/Layout";
import { LoginPage } from "./pages/LoginPage";
import { SignupPage } from "./pages/SignupPage";
import { DashboardPage } from "./pages/DashboardPage";
import { WarehousesPage } from "./pages/WarehousesPage";
import { InventoryPage } from "./pages/InventoryPage";
import { StockTransferPage } from "./pages/StockTransferPage";
import { StockActivityPage } from "./pages/StockActivityPage";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
    },
  },
});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <ToastProvider>
          <BrowserRouter>
            <Routes>
              <Route path="/login" element={<LoginPage />} />
              <Route path="/signup" element={<SignupPage />} />
              <Route
                path="/dashboard"
                element={
                  <ProtectedRoute>
                    <Layout>
                      <DashboardPage />
                    </Layout>
                  </ProtectedRoute>
                }
              />
              <Route
                path="/warehouses"
                element={
                  <ProtectedRoute>
                    <Layout>
                      <WarehousesPage />
                    </Layout>
                  </ProtectedRoute>
                }
              />
              <Route
                path="/inventory"
                element={
                  <ProtectedRoute>
                    <Layout>
                      <InventoryPage />
                    </Layout>
                  </ProtectedRoute>
                }
              />
              <Route
                path="/stock-transfer"
                element={
                  <ProtectedRoute>
                    <Layout>
                      <StockTransferPage />
                    </Layout>
                  </ProtectedRoute>
                }
              />
              <Route
                path="/stock-activity"
                element={
                  <ProtectedRoute>
                    <Layout>
                      <StockActivityPage />
                    </Layout>
                  </ProtectedRoute>
                }
              />
              <Route path="/" element={<Navigate to="/dashboard" replace />} />
            </Routes>
            <ToastContainer />
          </BrowserRouter>
        </ToastProvider>
      </AuthProvider>
    </QueryClientProvider>
  );
}

export default App;
