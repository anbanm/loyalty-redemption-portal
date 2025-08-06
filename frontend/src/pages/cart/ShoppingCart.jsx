import { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  IconButton,
  TextField,
  Grid,
  List,
  ListItem,
  ListItemAvatar,
  ListItemText,
  Avatar,
  Divider,
  Chip,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  CircularProgress
} from '@mui/material';
import {
  Add as AddIcon,
  Remove as RemoveIcon,
  Delete as DeleteIcon,
  ShoppingCart as CartIcon,
  Payment as PaymentIcon
} from '@mui/icons-material';
import MainCard from '../../components/MainCard';
import { useCartStore, useAuthStore } from '../../store/useStore';
import { useLoyaltyBalance, useCreateOrder } from '../../hooks/useApi';
import toast from 'react-hot-toast';

// Cart Item Component
const CartItem = ({ item, onUpdateQuantity, onRemove }) => {
  const [quantity, setQuantity] = useState(item.quantity);
  
  const handleQuantityChange = (newQuantity) => {
    if (newQuantity <= 0) {
      onRemove(item.id);
      return;
    }
    setQuantity(newQuantity);
    onUpdateQuantity(item.id, newQuantity);
  };
  
  const totalPoints = item.pointsCost * quantity;
  
  return (
    <ListItem
      sx={{
        border: 1,
        borderColor: 'divider',
        borderRadius: 1,
        mb: 1,
        '&:last-child': { mb: 0 }
      }}
    >
      <ListItemAvatar>
        <Avatar
          variant="rounded"
          src={item.imageUrl}
          sx={{ width: 60, height: 60 }}
        >
          <CartIcon />
        </Avatar>
      </ListItemAvatar>
      
      <ListItemText
        primary={item.name}
        secondary={
          <Box>
            <Typography variant="body2" color="text.secondary">
              {item.description}
            </Typography>
            <Box sx={{ display: 'flex', gap: 1, mt: 1 }}>
              <Chip 
                label={item.productType} 
                color={item.productType === 'VIRTUAL' ? 'primary' : 'secondary'}
                size="small"
              />
              {item.category && (
                <Chip label={item.category} variant="outlined" size="small" />
              )}
            </Box>
          </Box>
        }
        sx={{ mr: 2 }}
      />
      
      <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 1 }}>
        <Typography variant="h6" color="primary">
          {item.pointsCost.toLocaleString()} pts each
        </Typography>
        
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <IconButton 
            size="small" 
            onClick={() => handleQuantityChange(quantity - 1)}
          >
            <RemoveIcon />
          </IconButton>
          <TextField
            type="number"
            value={quantity}
            onChange={(e) => handleQuantityChange(parseInt(e.target.value) || 1)}
            size="small"
            sx={{ width: 80 }}
            inputProps={{ min: 1 }}
          />
          <IconButton 
            size="small" 
            onClick={() => handleQuantityChange(quantity + 1)}
          >
            <AddIcon />
          </IconButton>
        </Box>
        
        <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
          {totalPoints.toLocaleString()} pts
        </Typography>
        
        <IconButton 
          color="error" 
          onClick={() => onRemove(item.id)}
          size="small"
        >
          <DeleteIcon />
        </IconButton>
      </Box>
    </ListItem>
  );
};

// Order Summary Component
const OrderSummary = ({ items, totalPoints, onCheckout, isProcessing }) => {
  const totalItems = items.reduce((sum, item) => sum + item.quantity, 0);
  
  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom>
          Order Summary
        </Typography>
        
        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
          <Typography>Total Items:</Typography>
          <Typography>{totalItems}</Typography>
        </Box>
        
        <Divider sx={{ my: 2 }} />
        
        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
          <Typography variant="h6">Total Points:</Typography>
          <Typography variant="h6" color="primary" sx={{ fontWeight: 'bold' }}>
            {totalPoints.toLocaleString()} pts
          </Typography>
        </Box>
        
        <Button
          fullWidth
          variant="contained"
          size="large"
          startIcon={isProcessing ? <CircularProgress size={20} /> : <PaymentIcon />}
          onClick={onCheckout}
          disabled={items.length === 0 || isProcessing}
        >
          {isProcessing ? 'Processing...' : 'Proceed to Checkout'}
        </Button>
      </CardContent>
    </Card>
  );
};

// Balance Check Component
const BalanceCheck = ({ company, totalPoints }) => {
  const { data: balanceData, isLoading } = useLoyaltyBalance(company?.id);
  
  if (isLoading) {
    return (
      <Alert severity="info">
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <CircularProgress size={16} />
          Checking loyalty balance...
        </Box>
      </Alert>
    );
  }
  
  if (!balanceData?.data) {
    return (
      <Alert severity="warning">
        Unable to check loyalty balance. Please try again.
      </Alert>
    );
  }
  
  const balance = balanceData.data.balance || 0;
  const availableBalance = balanceData.data.availableBalance || balance;
  const hasSufficientBalance = availableBalance >= totalPoints;
  
  return (
    <Alert 
      severity={hasSufficientBalance ? 'success' : 'error'}
      sx={{ mb: 2 }}
    >
      <Typography variant="body2">
        <strong>Available Balance:</strong> {availableBalance.toLocaleString()} points
      </Typography>
      <Typography variant="body2">
        <strong>Order Total:</strong> {totalPoints.toLocaleString()} points
      </Typography>
      {!hasSufficientBalance && (
        <Typography variant="body2" sx={{ mt: 1 }}>
          Insufficient balance. You need {(totalPoints - availableBalance).toLocaleString()} more points.
        </Typography>
      )}
    </Alert>
  );
};

// Checkout Confirmation Dialog
const CheckoutDialog = ({ open, onClose, onConfirm, orderDetails, isLoading }) => {
  const [shippingAddress, setShippingAddress] = useState('');
  const [specialInstructions, setSpecialInstructions] = useState('');
  
  const hasPhysicalItems = orderDetails.items?.some(item => item.productType === 'PHYSICAL');
  
  const handleConfirm = () => {
    onConfirm({
      shippingAddress: hasPhysicalItems ? shippingAddress : null,
      specialInstructions: specialInstructions || null
    });
  };
  
  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Confirm Your Order</DialogTitle>
      <DialogContent>
        <Typography variant="body2" sx={{ mb: 2 }}>
          Please review your order details before proceeding.
        </Typography>
        
        <Box sx={{ mb: 3 }}>
          <Typography variant="h6" gutterBottom>
            Order Items ({orderDetails.totalItems})
          </Typography>
          {orderDetails.items?.map((item) => (
            <Box key={item.id} sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
              <Typography>{item.name} x{item.quantity}</Typography>
              <Typography>{(item.pointsCost * item.quantity).toLocaleString()} pts</Typography>
            </Box>
          ))}
          <Divider sx={{ my: 2 }} />
          <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
            <Typography variant="h6">Total:</Typography>
            <Typography variant="h6" color="primary">
              {orderDetails.totalPoints?.toLocaleString()} pts
            </Typography>
          </Box>
        </Box>
        
        {hasPhysicalItems && (
          <TextField
            fullWidth
            label="Shipping Address"
            multiline
            rows={3}
            value={shippingAddress}
            onChange={(e) => setShippingAddress(e.target.value)}
            required
            sx={{ mb: 2 }}
            helperText="Required for physical products"
          />
        )}
        
        <TextField
          fullWidth
          label="Special Instructions (Optional)"
          multiline
          rows={2}
          value={specialInstructions}
          onChange={(e) => setSpecialInstructions(e.target.value)}
          sx={{ mb: 2 }}
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isLoading}>
          Cancel
        </Button>
        <Button 
          onClick={handleConfirm} 
          variant="contained"
          disabled={isLoading || (hasPhysicalItems && !shippingAddress.trim())}
          startIcon={isLoading ? <CircularProgress size={16} /> : null}
        >
          {isLoading ? 'Creating Order...' : 'Confirm Order'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

// Main Shopping Cart Component
const ShoppingCart = () => {
  const [checkoutDialogOpen, setCheckoutDialogOpen] = useState(false);
  
  // Store hooks
  const { items, totalItems, totalPoints, updateQuantity, removeItem, clearCart } = useCartStore();
  const { user, company } = useAuthStore();
  
  // API hooks
  const { mutate: createOrder, isLoading: isCreatingOrder } = useCreateOrder();
  
  const handleUpdateQuantity = (itemId, quantity) => {
    updateQuantity(itemId, quantity);
  };
  
  const handleRemoveItem = (itemId) => {
    removeItem(itemId);
    toast.success('Item removed from cart');
  };
  
  const handleCheckout = () => {
    if (!user || !company) {
      toast.error('Please log in to proceed with checkout');
      return;
    }
    
    if (items.length === 0) {
      toast.error('Your cart is empty');
      return;
    }
    
    setCheckoutDialogOpen(true);
  };
  
  const handleConfirmOrder = (orderData) => {
    const orderRequest = {
      companyId: company.id,
      accountManagerId: user.id,
      items: items.map(item => ({
        productId: item.id,
        quantity: item.quantity
      })),
      shippingAddress: orderData.shippingAddress,
      specialInstructions: orderData.specialInstructions
    };
    
    createOrder(orderRequest, {
      onSuccess: (response) => {
        clearCart();
        setCheckoutDialogOpen(false);
        toast.success('Order created successfully!');
        // Optionally redirect to order details
      },
      onError: (error) => {
        console.error('Order creation failed:', error);
      }
    });
  };
  
  if (!user || !company) {
    return (
      <MainCard title="Shopping Cart">
        <Alert severity="warning">
          Please log in to view your shopping cart.
        </Alert>
      </MainCard>
    );
  }
  
  return (
    <>
      <MainCard title={`Shopping Cart (${totalItems} items)`}>
        <Grid container spacing={3}>
          <Grid item xs={12} md={8}>
            {items.length === 0 ? (
              <Box sx={{ textAlign: 'center', py: 8 }}>
                <CartIcon sx={{ fontSize: 64, color: 'text.secondary', mb: 2 }} />
                <Typography variant="h6" color="text.secondary" gutterBottom>
                  Your cart is empty
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Browse our product catalog to add items to your cart
                </Typography>
              </Box>
            ) : (
              <List sx={{ p: 0 }}>
                {items.map((item) => (
                  <CartItem
                    key={item.id}
                    item={item}
                    onUpdateQuantity={handleUpdateQuantity}
                    onRemove={handleRemoveItem}
                  />
                ))}
              </List>
            )}
            
            {items.length > 0 && (
              <Box sx={{ mt: 2, display: 'flex', gap: 2 }}>
                <Button 
                  variant="outlined" 
                  color="error" 
                  onClick={() => {
                    clearCart();
                    toast.success('Cart cleared');
                  }}
                >
                  Clear Cart
                </Button>
              </Box>
            )}
          </Grid>
          
          <Grid item xs={12} md={4}>
            {company && totalPoints > 0 && (
              <Box sx={{ mb: 3 }}>
                <BalanceCheck company={company} totalPoints={totalPoints} />
              </Box>
            )}
            
            <OrderSummary
              items={items}
              totalPoints={totalPoints}
              onCheckout={handleCheckout}
              isProcessing={isCreatingOrder}
            />
          </Grid>
        </Grid>
      </MainCard>
      
      <CheckoutDialog
        open={checkoutDialogOpen}
        onClose={() => setCheckoutDialogOpen(false)}
        onConfirm={handleConfirmOrder}
        orderDetails={{
          items,
          totalItems,
          totalPoints
        }}
        isLoading={isCreatingOrder}
      />
    </>
  );
};

export default ShoppingCart;