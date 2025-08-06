import axios from 'axios';

// Base API configuration
const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';

// Create axios instance with default configuration
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor for adding auth tokens (if needed)
api.interceptors.request.use(
  (config) => {
    // Add auth token if available
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor for handling errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Handle unauthorized - redirect to login
      localStorage.removeItem('authToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Product API
export const productApi = {
  getAll: (params = {}) => api.get('/products', { params }),
  getById: (id) => api.get(`/products/${id}`),
  search: (query, params = {}) => api.get('/products/search', { params: { q: query, ...params } }),
  getByType: (type, params = {}) => api.get(`/products/type/${type}`, { params }),
  getByCategory: (category) => api.get(`/products/category/${category}`),
  getByBrand: (brand) => api.get(`/products/brand/${brand}`),
  getCategories: () => api.get('/products/categories'),
  getBrands: () => api.get('/products/brands'),
  getByPointsRange: (minPoints, maxPoints) => 
    api.get('/products/points-range', { params: { minPoints, maxPoints } }),
  getInStock: () => api.get('/products/in-stock'),
};

// Company API  
export const companyApi = {
  getAll: () => api.get('/companies'),
  getById: (id) => api.get(`/companies/${id}`),
  getWithManagers: (id) => api.get(`/companies/${id}/with-managers`),
  getByLoyaltyAccountId: (loyaltyAccountId) => 
    api.get(`/companies/loyalty-account/${loyaltyAccountId}`),
  getByTier: (tierLevel) => api.get(`/companies/tier/${tierLevel}`),
  search: (name) => api.get('/companies/search', { params: { name } }),
  getCount: () => api.get('/companies/count'),
};

// Redemption API
export const redemptionApi = {
  checkBalance: (companyId) => api.get(`/redemption/balance/${companyId}`),
  createOrder: (orderData) => api.post('/redemption/orders', orderData),
  processOrder: (orderId) => api.post(`/redemption/orders/${orderId}/process`),
  cancelOrder: (orderId, reason) => 
    api.post(`/redemption/orders/${orderId}/cancel`, null, { params: { reason } }),
};

// Order API
export const orderApi = {
  getById: (orderId) => api.get(`/orders/${orderId}`),
  getByNumber: (orderNumber) => api.get(`/orders/number/${orderNumber}`),
  getByCompany: (companyId, params = {}) => api.get(`/orders/company/${companyId}`, { params }),
  getByAccountManager: (accountManagerId, params = {}) => 
    api.get(`/orders/account-manager/${accountManagerId}`, { params }),
  getByStatus: (status, params = {}) => api.get(`/orders/status/${status}`, { params }),
  getByDateRange: (startDate, endDate) => 
    api.get('/orders/date-range', { params: { startDate, endDate } }),
  markItemAsShipped: (orderId, itemId, trackingNumber) =>
    api.post(`/orders/${orderId}/items/${itemId}/ship`, null, 
      { params: { trackingNumber } }),
  markItemAsDelivered: (orderId, itemId) =>
    api.post(`/orders/${orderId}/items/${itemId}/deliver`),
  getProcessingPhysical: () => api.get('/orders/processing/physical'),
  getProcessingVirtual: () => api.get('/orders/processing/virtual'),
  getStatistics: () => api.get('/orders/statistics'),
};

// Inventory API
export const inventoryApi = {
  getByProduct: (productId) => api.get(`/inventory/product/${productId}`),
  checkAvailability: (productId, quantity) => 
    api.get(`/inventory/product/${productId}/availability`, { params: { quantity } }),
  getLowStock: () => api.get('/inventory/low-stock'),
  getOutOfStock: () => api.get('/inventory/out-of-stock'),
  addStock: (productId, quantity) => 
    api.post(`/inventory/product/${productId}/add-stock`, null, { params: { quantity } }),
  initialize: (productId, data) => 
    api.post(`/inventory/product/${productId}/initialize`, data),
  updateReorderPoint: (productId, reorderPoint) =>
    api.put(`/inventory/product/${productId}/reorder-point`, null, 
      { params: { reorderPoint } }),
  batchUpdate: (updates) => api.post('/inventory/batch-update', updates),
  getStatistics: () => api.get('/inventory/statistics'),
};

// Mock API (for testing)
export const mockApi = {
  getStatus: () => api.get('/mock/status'),
  setLoyaltyBalance: (accountId, balance) => 
    api.post(`/mock/loyalty/balance/${accountId}`, null, { params: { balance } }),
  setLoyaltyTier: (accountId, tier) =>
    api.post(`/mock/loyalty/tier/${accountId}`, null, { params: { tier } }),
  getLoyaltyBalance: (accountId) => api.get(`/mock/loyalty/balance/${accountId}`),
  resetLoyalty: () => api.post('/mock/loyalty/reset'),
  
  // Notification mocks
  getSentEmails: () => api.get('/mock/notifications/emails'),
  getEmailsByRecipient: (email) => api.get(`/mock/notifications/emails/recipient/${email}`),
  getEmailsByType: (type) => api.get(`/mock/notifications/emails/type/${type}`),
  getNotificationStats: () => api.get('/mock/notifications/statistics'),
  clearEmails: () => api.post('/mock/notifications/clear'),
  
  // Virtual fulfillment mocks
  getFulfillmentStatus: (fulfillmentId) => 
    api.get(`/mock/fulfillment/virtual/status/${fulfillmentId}`),
  getFulfillmentsByCustomer: (email) => 
    api.get(`/mock/fulfillment/virtual/customer/${email}`),
  getFulfillmentStats: () => api.get('/mock/fulfillment/virtual/statistics'),
  resetFulfillment: () => api.post('/mock/fulfillment/virtual/reset'),
  
  // General
  resetAll: () => api.post('/mock/reset-all'),
};

// Utility functions
export const handleApiError = (error) => {
  if (error.response) {
    // Server responded with error status
    const { status, data } = error.response;
    return {
      status,
      message: data?.message || data || `HTTP ${status} Error`,
      details: data?.details || null,
    };
  } else if (error.request) {
    // Network error
    return {
      status: 0,
      message: 'Network error - please check your connection',
      details: 'Unable to connect to server',
    };
  } else {
    // Other error
    return {
      status: -1,
      message: error.message || 'An unexpected error occurred',
      details: null,
    };
  }
};

export default api;