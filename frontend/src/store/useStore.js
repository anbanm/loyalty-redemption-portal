import { create } from 'zustand';
import { devtools } from 'zustand/middleware';

// Auth Store
export const useAuthStore = create(
  devtools((set, get) => ({
    user: null,
    company: null,
    isAuthenticated: false,
    isLoading: false,
    
    // Actions
    setUser: (user) => set({ user, isAuthenticated: !!user }),
    setCompany: (company) => set({ company }),
    setLoading: (isLoading) => set({ isLoading }),
    logout: () => set({ user: null, company: null, isAuthenticated: false }),
    
    // Mock login for development
    mockLogin: (userData) => {
      set({ 
        user: userData,
        company: userData.company,
        isAuthenticated: true 
      });
      localStorage.setItem('mockUser', JSON.stringify(userData));
    },
    
    // Initialize from localStorage
    initialize: () => {
      const mockUser = localStorage.getItem('mockUser');
      if (mockUser) {
        const userData = JSON.parse(mockUser);
        set({
          user: userData,
          company: userData.company,
          isAuthenticated: true
        });
      }
    },
  }), { name: 'auth-store' })
);

// Cart Store
export const useCartStore = create(
  devtools((set, get) => ({
    items: [],
    totalItems: 0,
    totalPoints: 0,
    
    // Actions
    addItem: (product, quantity = 1) => {
      const { items } = get();
      const existingItem = items.find(item => item.id === product.id);
      
      if (existingItem) {
        set({
          items: items.map(item =>
            item.id === product.id
              ? { ...item, quantity: item.quantity + quantity }
              : item
          )
        });
      } else {
        set({
          items: [...items, { ...product, quantity }]
        });
      }
      
      get().updateTotals();
    },
    
    removeItem: (productId) => {
      set({
        items: get().items.filter(item => item.id !== productId)
      });
      get().updateTotals();
    },
    
    updateQuantity: (productId, quantity) => {
      if (quantity <= 0) {
        get().removeItem(productId);
        return;
      }
      
      set({
        items: get().items.map(item =>
          item.id === productId
            ? { ...item, quantity }
            : item
        )
      });
      get().updateTotals();
    },
    
    clearCart: () => {
      set({ items: [], totalItems: 0, totalPoints: 0 });
    },
    
    updateTotals: () => {
      const { items } = get();
      const totalItems = items.reduce((sum, item) => sum + item.quantity, 0);
      const totalPoints = items.reduce((sum, item) => sum + (item.pointsCost * item.quantity), 0);
      set({ totalItems, totalPoints });
    },
  }), { name: 'cart-store' })
);

// UI Store
export const useUIStore = create(
  devtools((set) => ({
    sidebarOpen: true,
    loading: {},
    notifications: [],
    
    // Actions
    toggleSidebar: () => set((state) => ({ sidebarOpen: !state.sidebarOpen })),
    setSidebarOpen: (open) => set({ sidebarOpen: open }),
    
    setLoading: (key, isLoading) => set((state) => ({
      loading: { ...state.loading, [key]: isLoading }
    })),
    
    addNotification: (notification) => set((state) => ({
      notifications: [...state.notifications, {
        id: Date.now(),
        timestamp: new Date(),
        ...notification
      }]
    })),
    
    removeNotification: (id) => set((state) => ({
      notifications: state.notifications.filter(n => n.id !== id)
    })),
    
    clearNotifications: () => set({ notifications: [] }),
  }), { name: 'ui-store' })
);

// Product Store
export const useProductStore = create(
  devtools((set, get) => ({
    products: [],
    categories: [],
    brands: [],
    selectedProduct: null,
    filters: {
      category: '',
      brand: '',
      productType: '',
      minPoints: '',
      maxPoints: '',
      search: ''
    },
    
    // Actions
    setProducts: (products) => set({ products }),
    setCategories: (categories) => set({ categories }),
    setBrands: (brands) => set({ brands }),
    setSelectedProduct: (product) => set({ selectedProduct: product }),
    
    updateFilters: (newFilters) => set((state) => ({
      filters: { ...state.filters, ...newFilters }
    })),
    
    clearFilters: () => set({
      filters: {
        category: '',
        brand: '',
        productType: '',
        minPoints: '',
        maxPoints: '',
        search: ''
      }
    }),
    
    getFilteredProducts: () => {
      const { products, filters } = get();
      
      return products.filter(product => {
        // Search filter
        if (filters.search && 
            !product.name.toLowerCase().includes(filters.search.toLowerCase()) &&
            !product.description?.toLowerCase().includes(filters.search.toLowerCase())) {
          return false;
        }
        
        // Category filter
        if (filters.category && product.category !== filters.category) {
          return false;
        }
        
        // Brand filter
        if (filters.brand && product.brand !== filters.brand) {
          return false;
        }
        
        // Product type filter
        if (filters.productType && product.productType !== filters.productType) {
          return false;
        }
        
        // Points range filter
        if (filters.minPoints && product.pointsCost < parseInt(filters.minPoints)) {
          return false;
        }
        
        if (filters.maxPoints && product.pointsCost > parseInt(filters.maxPoints)) {
          return false;
        }
        
        return true;
      });
    },
  }), { name: 'product-store' })
);

// Order Store
export const useOrderStore = create(
  devtools((set, get) => ({
    orders: [],
    selectedOrder: null,
    orderStats: null,
    
    // Actions
    setOrders: (orders) => set({ orders }),
    setSelectedOrder: (order) => set({ selectedOrder: order }),
    setOrderStats: (stats) => set({ orderStats: stats }),
    
    addOrder: (order) => set((state) => ({
      orders: [order, ...state.orders]
    })),
    
    updateOrder: (orderId, updates) => set((state) => ({
      orders: state.orders.map(order =>
        order.id === orderId ? { ...order, ...updates } : order
      ),
      selectedOrder: state.selectedOrder?.id === orderId 
        ? { ...state.selectedOrder, ...updates }
        : state.selectedOrder
    })),
    
    getOrdersByStatus: (status) => {
      return get().orders.filter(order => order.status === status);
    },
    
    getRecentOrders: (limit = 5) => {
      return get().orders
        .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
        .slice(0, limit);
    },
  }), { name: 'order-store' })
);

// Company Store  
export const useCompanyStore = create(
  devtools((set, get) => ({
    companies: [],
    selectedCompany: null,
    loyaltyBalance: null,
    
    // Actions
    setCompanies: (companies) => set({ companies }),
    setSelectedCompany: (company) => set({ selectedCompany: company }),
    setLoyaltyBalance: (balance) => set({ loyaltyBalance: balance }),
    
    getCompanyById: (id) => {
      return get().companies.find(company => company.id === id);
    },
  }), { name: 'company-store' })
);