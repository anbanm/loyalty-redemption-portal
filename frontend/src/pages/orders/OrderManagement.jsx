import { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Grid,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  Paper,
  Chip,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  List,
  ListItem,
  ListItemText,
  Divider,
  Alert,
  CircularProgress,
  Tooltip
} from '@mui/material';
import {
  Visibility as ViewIcon,
  Receipt as ReceiptIcon,
  Cancel as CancelIcon,
  CheckCircle as CompleteIcon,
  LocalShipping as ShippingIcon,
  Search as SearchIcon,
  FilterList as FilterIcon,
  Download as DownloadIcon,
  Refresh as RefreshIcon
} from '@mui/icons-material';
import MainCard from '../../components/MainCard';
import { useOrders, useOrder, useProcessOrder, useCancelOrder } from '../../hooks/useApi';
import { useAuthStore, useOrderStore } from '../../store/useStore';
import { usePagination, useDebounce } from '../../hooks/useApi';
import toast from 'react-hot-toast';

// Order Status Chip Component
const OrderStatusChip = ({ status }) => {
  const getStatusConfig = (status) => {
    switch (status) {
      case 'PENDING':
        return { color: 'warning', label: 'Pending' };
      case 'PROCESSING':
        return { color: 'info', label: 'Processing' };
      case 'COMPLETED':
        return { color: 'success', label: 'Completed' };
      case 'CANCELLED':
        return { color: 'error', label: 'Cancelled' };
      case 'SHIPPED':
        return { color: 'primary', label: 'Shipped' };
      case 'DELIVERED':
        return { color: 'success', label: 'Delivered' };
      default:
        return { color: 'default', label: status };
    }
  };

  const { color, label } = getStatusConfig(status);
  return <Chip label={label} color={color} size="small" />;
};

// Order Actions Component
const OrderActions = ({ order, onView, onProcess, onCancel, isProcessing, isCancelling }) => {
  const canProcess = order.status === 'PENDING';
  const canCancel = ['PENDING', 'PROCESSING'].includes(order.status);

  return (
    <Box sx={{ display: 'flex', gap: 1 }}>
      <Tooltip title="View Details">
        <IconButton size="small" onClick={() => onView(order.id)}>
          <ViewIcon />
        </IconButton>
      </Tooltip>
      
      {canProcess && (
        <Tooltip title="Process Order">
          <IconButton 
            size="small" 
            color="primary"
            onClick={() => onProcess(order.id)}
            disabled={isProcessing}
          >
            {isProcessing ? <CircularProgress size={16} /> : <CompleteIcon />}
          </IconButton>
        </Tooltip>
      )}
      
      {canCancel && (
        <Tooltip title="Cancel Order">
          <IconButton 
            size="small" 
            color="error"
            onClick={() => onCancel(order.id)}
            disabled={isCancelling}
          >
            {isCancelling ? <CircularProgress size={16} /> : <CancelIcon />}
          </IconButton>
        </Tooltip>
      )}
    </Box>
  );
};

// Order Details Dialog Component
const OrderDetailsDialog = ({ open, onClose, orderId }) => {
  const { data: orderData, isLoading } = useOrder(orderId);
  const order = orderData?.data;

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        Order Details {order && `- ${order.orderNumber}`}
      </DialogTitle>
      <DialogContent>
        {isLoading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
            <CircularProgress />
          </Box>
        ) : order ? (
          <Grid container spacing={3}>
            {/* Order Summary */}
            <Grid item xs={12} md={6}>
              <Card variant="outlined">
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Order Summary
                  </Typography>
                  <Box sx={{ mb: 2 }}>
                    <Typography variant="body2" color="text.secondary">
                      Order Number: <strong>{order.orderNumber}</strong>
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Status: <OrderStatusChip status={order.status} />
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Created: <strong>{formatDate(order.createdAt)}</strong>
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Total Points: <strong>{order.totalPoints.toLocaleString()}</strong>
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Total Items: <strong>{order.totalItems}</strong>
                    </Typography>
                  </Box>
                </CardContent>
              </Card>
            </Grid>

            {/* Company & Account Manager */}
            <Grid item xs={12} md={6}>
              <Card variant="outlined">
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Account Information
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Company: <strong>{order.company?.name}</strong>
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Account Manager: <strong>{order.accountManager?.firstName} {order.accountManager?.lastName}</strong>
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Email: <strong>{order.accountManager?.email}</strong>
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Loyalty Account: <strong>{order.company?.loyaltyAccountId}</strong>
                  </Typography>
                </CardContent>
              </Card>
            </Grid>

            {/* Order Items */}
            <Grid item xs={12}>
              <Card variant="outlined">
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Order Items
                  </Typography>
                  <TableContainer>
                    <Table size="small">
                      <TableHead>
                        <TableRow>
                          <TableCell>Product</TableCell>
                          <TableCell>Type</TableCell>
                          <TableCell align="right">Points Each</TableCell>
                          <TableCell align="right">Quantity</TableCell>
                          <TableCell align="right">Total Points</TableCell>
                          <TableCell>Status</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {order.items?.map((item) => (
                          <TableRow key={item.id}>
                            <TableCell>
                              <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                                {item.product?.name}
                              </Typography>
                              <Typography variant="caption" color="text.secondary">
                                {item.product?.description}
                              </Typography>
                            </TableCell>
                            <TableCell>
                              <Chip 
                                label={item.product?.productType} 
                                color={item.product?.productType === 'VIRTUAL' ? 'primary' : 'secondary'}
                                size="small"
                              />
                            </TableCell>
                            <TableCell align="right">
                              {item.pointsPerItem?.toLocaleString()}
                            </TableCell>
                            <TableCell align="right">{item.quantity}</TableCell>
                            <TableCell align="right">
                              <strong>{item.totalPoints?.toLocaleString()}</strong>
                            </TableCell>
                            <TableCell>
                              <OrderStatusChip status={item.status} />
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </TableContainer>
                </CardContent>
              </Card>
            </Grid>

            {/* Shipping Information */}
            {order.shippingAddress && (
              <Grid item xs={12}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      Shipping Information
                    </Typography>
                    <Typography variant="body2" sx={{ whiteSpace: 'pre-line' }}>
                      {order.shippingAddress}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            )}

            {/* Special Instructions */}
            {order.specialInstructions && (
              <Grid item xs={12}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      Special Instructions
                    </Typography>
                    <Typography variant="body2">
                      {order.specialInstructions}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            )}
          </Grid>
        ) : (
          <Alert severity="error">Failed to load order details</Alert>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Close</Button>
      </DialogActions>
    </Dialog>
  );
};

// Cancel Order Dialog Component
const CancelOrderDialog = ({ open, onClose, onConfirm, orderId, isLoading }) => {
  const [reason, setReason] = useState('');

  const handleConfirm = () => {
    if (!reason.trim()) {
      toast.error('Please provide a cancellation reason');
      return;
    }
    onConfirm(orderId, reason);
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Cancel Order</DialogTitle>
      <DialogContent>
        <Typography variant="body2" sx={{ mb: 2 }}>
          Are you sure you want to cancel this order? This action cannot be undone.
        </Typography>
        <TextField
          fullWidth
          label="Cancellation Reason"
          multiline
          rows={3}
          value={reason}
          onChange={(e) => setReason(e.target.value)}
          required
          helperText="Please provide a reason for cancelling this order"
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isLoading}>
          Cancel
        </Button>
        <Button 
          onClick={handleConfirm} 
          color="error"
          variant="contained"
          disabled={isLoading || !reason.trim()}
          startIcon={isLoading ? <CircularProgress size={16} /> : null}
        >
          {isLoading ? 'Cancelling...' : 'Confirm Cancellation'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

// Main Order Management Component
const OrderManagement = () => {
  const [filters, setFilters] = useState({
    status: '',
    search: '',
    startDate: '',
    endDate: ''
  });
  const [selectedOrderId, setSelectedOrderId] = useState(null);
  const [cancelDialogOpen, setCancelDialogOpen] = useState(false);
  const [orderToCancel, setOrderToCancel] = useState(null);
  const [processingOrderId, setProcessingOrderId] = useState(null);
  const [cancellingOrderId, setCancellingOrderId] = useState(null);

  // Store hooks
  const { user, company } = useAuthStore();
  const { orders, setOrders } = useOrderStore();

  // Pagination hook
  const { page, pageSize, setPage, setPageSize, params } = usePagination(1, 10);

  // Debounced search
  const debouncedSearch = useDebounce(filters.search, 500);

  // API hooks
  const { 
    data: ordersData, 
    isLoading: ordersLoading, 
    refetch: refetchOrders 
  } = useOrders({ 
    companyId: company?.id, 
    ...filters, 
    search: debouncedSearch,
    ...params 
  });

  const { mutate: processOrder } = useProcessOrder();
  const { mutate: cancelOrder } = useCancelOrder();

  // Extract orders data
  const ordersResponse = ordersData?.data;
  const ordersList = ordersResponse?.content || ordersResponse || [];
  const totalElements = ordersResponse?.totalElements || 0;

  useEffect(() => {
    if (ordersList.length > 0) {
      setOrders(ordersList);
    }
  }, [ordersList, setOrders]);

  const handleFilterChange = (field, value) => {
    setFilters(prev => ({ ...prev, [field]: value }));
    setPage(1); // Reset to first page when filtering
  };

  const handleViewOrder = (orderId) => {
    setSelectedOrderId(orderId);
  };

  const handleProcessOrder = (orderId) => {
    setProcessingOrderId(orderId);
    processOrder(orderId, {
      onSuccess: () => {
        setProcessingOrderId(null);
        refetchOrders();
      },
      onError: () => {
        setProcessingOrderId(null);
      }
    });
  };

  const handleCancelOrderClick = (orderId) => {
    setOrderToCancel(orderId);
    setCancelDialogOpen(true);
  };

  const handleConfirmCancel = (orderId, reason) => {
    setCancellingOrderId(orderId);
    cancelOrder({ orderId, reason }, {
      onSuccess: () => {
        setCancellingOrderId(null);
        setCancelDialogOpen(false);
        setOrderToCancel(null);
        refetchOrders();
      },
      onError: () => {
        setCancellingOrderId(null);
      }
    });
  };

  const handleExportOrders = () => {
    // TODO: Implement export functionality
    toast.info('Export functionality coming soon');
  };

  if (!user || !company) {
    return (
      <MainCard title="Order Management">
        <Alert severity="warning">
          Please log in to view order management.
        </Alert>
      </MainCard>
    );
  }

  return (
    <>
      <MainCard
        title="Order Management"
        secondary={
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Button
              startIcon={<RefreshIcon />}
              onClick={() => refetchOrders()}
              size="small"
            >
              Refresh
            </Button>
            <Button
              startIcon={<DownloadIcon />}
              onClick={handleExportOrders}
              size="small"
              variant="outlined"
            >
              Export
            </Button>
          </Box>
        }
      >
        {/* Filters */}
        <Card variant="outlined" sx={{ mb: 3 }}>
          <CardContent>
            <Grid container spacing={2} alignItems="center">
              <Grid item xs={12} sm={6} md={3}>
                <TextField
                  fullWidth
                  placeholder="Search orders..."
                  value={filters.search}
                  onChange={(e) => handleFilterChange('search', e.target.value)}
                  size="small"
                  InputProps={{
                    startAdornment: <SearchIcon sx={{ mr: 1, color: 'text.secondary' }} />
                  }}
                />
              </Grid>
              <Grid item xs={12} sm={6} md={2}>
                <FormControl fullWidth size="small">
                  <InputLabel>Status</InputLabel>
                  <Select
                    value={filters.status}
                    label="Status"
                    onChange={(e) => handleFilterChange('status', e.target.value)}
                  >
                    <MenuItem value="">All Statuses</MenuItem>
                    <MenuItem value="PENDING">Pending</MenuItem>
                    <MenuItem value="PROCESSING">Processing</MenuItem>
                    <MenuItem value="COMPLETED">Completed</MenuItem>
                    <MenuItem value="CANCELLED">Cancelled</MenuItem>
                    <MenuItem value="SHIPPED">Shipped</MenuItem>
                    <MenuItem value="DELIVERED">Delivered</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12} sm={6} md={2}>
                <TextField
                  fullWidth
                  label="Start Date"
                  type="date"
                  value={filters.startDate}
                  onChange={(e) => handleFilterChange('startDate', e.target.value)}
                  size="small"
                  InputLabelProps={{ shrink: true }}
                />
              </Grid>
              <Grid item xs={12} sm={6} md={2}>
                <TextField
                  fullWidth
                  label="End Date"
                  type="date"
                  value={filters.endDate}
                  onChange={(e) => handleFilterChange('endDate', e.target.value)}
                  size="small"
                  InputLabelProps={{ shrink: true }}
                />
              </Grid>
              <Grid item xs={12} md={3}>
                <Button
                  variant="outlined"
                  onClick={() => setFilters({ status: '', search: '', startDate: '', endDate: '' })}
                  startIcon={<FilterIcon />}
                  size="small"
                >
                  Clear Filters
                </Button>
              </Grid>
            </Grid>
          </CardContent>
        </Card>

        {/* Orders Table */}
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Order Number</TableCell>
                <TableCell>Date</TableCell>
                <TableCell>Status</TableCell>
                <TableCell align="right">Items</TableCell>
                <TableCell align="right">Total Points</TableCell>
                <TableCell>Account Manager</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {ordersLoading ? (
                <TableRow>
                  <TableCell colSpan={7} align="center">
                    <CircularProgress />
                  </TableCell>
                </TableRow>
              ) : ordersList.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} align="center">
                    <Typography color="text.secondary">
                      No orders found
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                ordersList.map((order) => (
                  <TableRow key={order.id} hover>
                    <TableCell>
                      <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                        {order.orderNumber}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      {new Date(order.createdAt).toLocaleDateString()}
                    </TableCell>
                    <TableCell>
                      <OrderStatusChip status={order.status} />
                    </TableCell>
                    <TableCell align="right">
                      {order.totalItems}
                    </TableCell>
                    <TableCell align="right">
                      <Typography sx={{ fontWeight: 'bold' }}>
                        {order.totalPoints?.toLocaleString()} pts
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2">
                        {order.accountManager?.firstName} {order.accountManager?.lastName}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {order.accountManager?.email}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <OrderActions
                        order={order}
                        onView={handleViewOrder}
                        onProcess={handleProcessOrder}
                        onCancel={handleCancelOrderClick}
                        isProcessing={processingOrderId === order.id}
                        isCancelling={cancellingOrderId === order.id}
                      />
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
          <TablePagination
            component="div"
            count={totalElements}
            page={page - 1} // Convert to 0-based for MUI
            onPageChange={(_, newPage) => setPage(newPage + 1)} // Convert back to 1-based
            rowsPerPage={pageSize}
            onRowsPerPageChange={(e) => setPageSize(parseInt(e.target.value))}
            rowsPerPageOptions={[5, 10, 25, 50]}
          />
        </TableContainer>
      </MainCard>

      {/* Order Details Dialog */}
      <OrderDetailsDialog
        open={!!selectedOrderId}
        onClose={() => setSelectedOrderId(null)}
        orderId={selectedOrderId}
      />

      {/* Cancel Order Dialog */}
      <CancelOrderDialog
        open={cancelDialogOpen}
        onClose={() => {
          setCancelDialogOpen(false);
          setOrderToCancel(null);
        }}
        onConfirm={handleConfirmCancel}
        orderId={orderToCancel}
        isLoading={!!cancellingOrderId}
      />
    </>
  );
};

export default OrderManagement;