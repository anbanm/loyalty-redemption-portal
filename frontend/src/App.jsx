import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from 'react-query';
import { ThemeProvider } from '@mui/material/styles';
import { CssBaseline, Box } from '@mui/material';
import { Toaster } from 'react-hot-toast';

// Theme
import theme from './themes';

// Layout Components
import MainLayout from './layout/MainLayout';

// Pages
import ProductCatalog from './pages/products/ProductCatalog';
import ShoppingCart from './pages/cart/ShoppingCart';
import OrderManagement from './pages/orders/OrderManagement';

// Store initialization
import { useAuthStore } from './store/useStore';
import { useEffect } from 'react';

// Create React Query client
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
      staleTime: 5 * 60 * 1000, // 5 minutes
    },
  },
});

// Protected Route Component
const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, user, initialize } = useAuthStore();
  
  useEffect(() => {
    initialize();
  }, [initialize]);
  
  if (!isAuthenticated || !user) {
    // For development, auto-login with mock user
    const mockUser = {
      id: 'am-001',
      firstName: 'John',
      lastName: 'Doe',
      email: 'john.doe@acme.com',
      role: 'ACCOUNT_MANAGER',
      company: {
        id: 'company-001',
        name: 'ACME Corporation',
        loyaltyAccountId: 'ACME-123456',
        tier: 'GOLD'
      }
    };
    
    useAuthStore.getState().mockLogin(mockUser);
    return <div>Logging in...</div>;
  }
  
  return children;
};

// Main App Component
function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <Router>
          <ProtectedRoute>
            <MainLayout>
              <Routes>
                <Route path="/" element={<Navigate to="/products" replace />} />
                <Route path="/products" element={<ProductCatalog />} />
                <Route path="/cart" element={<ShoppingCart />} />
                <Route path="/orders" element={<OrderManagement />} />
                <Route path="*" element={<Navigate to="/products" replace />} />
              </Routes>
            </MainLayout>
          </ProtectedRoute>
        </Router>
        
        {/* Toast Notifications */}
        <Toaster
          position="top-right"
          toastOptions={{
            duration: 4000,
            style: {
              background: '#363636',
              color: '#fff',
            },
            success: {
              duration: 3000,
              style: {
                background: '#4caf50',
              },
            },
            error: {
              duration: 5000,
              style: {
                background: '#f44336',
              },
            },
          }}
        />
      </ThemeProvider>
    </QueryClientProvider>
  );
}

export default App;