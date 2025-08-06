import { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { 
  productApi, 
  companyApi, 
  redemptionApi, 
  orderApi, 
  inventoryApi,
  handleApiError 
} from '../services/api';
import { useUIStore } from '../store/useStore';
import toast from 'react-hot-toast';

// Generic API hook
export const useApi = (queryKey, queryFn, options = {}) => {
  const { addNotification } = useUIStore();
  
  return useQuery(queryKey, queryFn, {
    onError: (error) => {
      const errorInfo = handleApiError(error);
      addNotification({
        type: 'error',
        title: 'API Error',
        message: errorInfo.message
      });
      toast.error(errorInfo.message);
    },
    ...options
  });
};

// Product hooks
export const useProducts = (params = {}) => {
  return useApi(['products', params], () => productApi.getAll(params), {
    keepPreviousData: true,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

export const useProduct = (id) => {
  return useApi(['product', id], () => productApi.getById(id), {
    enabled: !!id,
  });
};

export const useProductSearch = (query, params = {}) => {
  return useApi(['products', 'search', query, params], 
    () => productApi.search(query, params), {
    enabled: !!query,
    keepPreviousData: true,
  });
};

export const useProductCategories = () => {
  return useApi(['products', 'categories'], () => productApi.getCategories(), {
    staleTime: 10 * 60 * 1000, // 10 minutes
  });
};

export const useProductBrands = () => {
  return useApi(['products', 'brands'], () => productApi.getBrands(), {
    staleTime: 10 * 60 * 1000, // 10 minutes
  });
};

// Company hooks
export const useCompanies = () => {
  return useApi(['companies'], () => companyApi.getAll(), {
    staleTime: 5 * 60 * 1000,
  });
};

export const useCompany = (id) => {
  return useApi(['company', id], () => companyApi.getById(id), {
    enabled: !!id,
  });
};

export const useLoyaltyBalance = (companyId) => {
  return useApi(['loyalty-balance', companyId], 
    () => redemptionApi.checkBalance(companyId), {
    enabled: !!companyId,
    refetchInterval: 30000, // Refetch every 30 seconds
  });
};

// Order hooks
export const useOrders = (params = {}) => {
  return useApi(['orders', params], () => orderApi.getByCompany(params.companyId, params), {
    keepPreviousData: true,
  });
};

export const useOrder = (orderId) => {
  return useApi(['order', orderId], () => orderApi.getById(orderId), {
    enabled: !!orderId,
  });
};

export const useOrderStats = () => {
  return useApi(['order-stats'], () => orderApi.getStatistics(), {
    staleTime: 2 * 60 * 1000, // 2 minutes
  });
};

// Inventory hooks
export const useInventory = (productId) => {
  return useApi(['inventory', productId], () => inventoryApi.getByProduct(productId), {
    enabled: !!productId,
  });
};

export const useInventoryStats = () => {
  return useApi(['inventory-stats'], () => inventoryApi.getStatistics(), {
    staleTime: 5 * 60 * 1000,
  });
};

// Mutation hooks
export const useCreateOrder = () => {
  const queryClient = useQueryClient();
  const { addNotification } = useUIStore();
  
  return useMutation(redemptionApi.createOrder, {
    onSuccess: (data) => {
      queryClient.invalidateQueries(['orders']);
      queryClient.invalidateQueries(['loyalty-balance']);
      addNotification({
        type: 'success',
        title: 'Order Created',
        message: `Order ${data.data.orderNumber} created successfully`
      });
      toast.success('Order created successfully!');
    },
    onError: (error) => {
      const errorInfo = handleApiError(error);
      addNotification({
        type: 'error',
        title: 'Order Creation Failed',
        message: errorInfo.message
      });
      toast.error(`Order creation failed: ${errorInfo.message}`);
    }
  });
};

export const useProcessOrder = () => {
  const queryClient = useQueryClient();
  const { addNotification } = useUIStore();
  
  return useMutation(redemptionApi.processOrder, {
    onSuccess: (data, orderId) => {
      queryClient.invalidateQueries(['orders']);
      queryClient.invalidateQueries(['order', orderId]);
      queryClient.invalidateQueries(['loyalty-balance']);
      addNotification({
        type: 'success',
        title: 'Order Processed',
        message: 'Order has been processed and points deducted'
      });
      toast.success('Order processed successfully!');
    },
    onError: (error) => {
      const errorInfo = handleApiError(error);
      addNotification({
        type: 'error',
        title: 'Order Processing Failed',
        message: errorInfo.message
      });
      toast.error(`Order processing failed: ${errorInfo.message}`);
    }
  });
};

export const useCancelOrder = () => {
  const queryClient = useQueryClient();
  const { addNotification } = useUIStore();
  
  return useMutation(({ orderId, reason }) => redemptionApi.cancelOrder(orderId, reason), {
    onSuccess: (data, { orderId }) => {
      queryClient.invalidateQueries(['orders']);
      queryClient.invalidateQueries(['order', orderId]);
      queryClient.invalidateQueries(['loyalty-balance']);
      addNotification({
        type: 'success',
        title: 'Order Cancelled',
        message: 'Order has been cancelled and points refunded'
      });
      toast.success('Order cancelled successfully!');
    },
    onError: (error) => {
      const errorInfo = handleApiError(error);
      addNotification({
        type: 'error',
        title: 'Order Cancellation Failed',
        message: errorInfo.message
      });
      toast.error(`Order cancellation failed: ${errorInfo.message}`);
    }
  });
};

// Loading state hook
export const useLoading = (key) => {
  const { loading, setLoading } = useUIStore();
  
  return [
    loading[key] || false,
    (isLoading) => setLoading(key, isLoading)
  ];
};

// Generic mutation hook with loading state
export const useMutationWithLoading = (mutationFn, options = {}) => {
  const [isLoading, setLoading] = useState(false);
  const { addNotification } = useUIStore();
  
  const mutation = useMutation(mutationFn, {
    onMutate: () => {
      setLoading(true);
    },
    onSettled: () => {
      setLoading(false);
    },
    onError: (error) => {
      const errorInfo = handleApiError(error);
      addNotification({
        type: 'error',
        title: 'Operation Failed',
        message: errorInfo.message
      });
      toast.error(errorInfo.message);
    },
    ...options
  });
  
  return {
    ...mutation,
    isLoading
  };
};

// Pagination hook
export const usePagination = (initialPage = 1, initialPageSize = 10) => {
  const [page, setPage] = useState(initialPage);
  const [pageSize, setPageSize] = useState(initialPageSize);
  
  const resetPagination = () => {
    setPage(initialPage);
  };
  
  return {
    page,
    pageSize,
    setPage,
    setPageSize,
    resetPagination,
    params: { page: page - 1, size: pageSize } // Convert to 0-based for backend
  };
};

// Debounced search hook
export const useDebounce = (value, delay) => {
  const [debouncedValue, setDebouncedValue] = useState(value);
  
  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);
    
    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);
  
  return debouncedValue;
};