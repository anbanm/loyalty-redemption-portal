import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import {
  Box,
  Drawer,
  AppBar,
  Toolbar,
  List,
  Typography,
  Divider,
  IconButton,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Badge,
  Avatar,
  Menu,
  MenuItem,
  Chip
} from '@mui/material';
import {
  Menu as MenuIcon,
  ShoppingCart as CartIcon,
  Inventory as ProductIcon,
  Receipt as OrderIcon,
  AccountCircle as AccountIcon,
  Logout as LogoutIcon
} from '@mui/icons-material';

// Store
import { useAuthStore, useCartStore, useUIStore } from '../store/useStore';

const drawerWidth = 240;

const MainLayout = ({ children }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const [accountMenuAnchor, setAccountMenuAnchor] = useState(null);
  
  // Store hooks
  const { user, company, logout } = useAuthStore();
  const { totalItems } = useCartStore();
  const { sidebarOpen, toggleSidebar } = useUIStore();
  
  // Navigation items
  const navItems = [
    {
      text: 'Product Catalog',
      icon: <ProductIcon />,
      path: '/products'
    },
    {
      text: 'Shopping Cart',
      icon: (
        <Badge badgeContent={totalItems} color="primary">
          <CartIcon />
        </Badge>
      ),
      path: '/cart'
    },
    {
      text: 'Order Management',
      icon: <OrderIcon />,
      path: '/orders'
    }
  ];
  
  const handleAccountMenuOpen = (event) => {
    setAccountMenuAnchor(event.currentTarget);
  };
  
  const handleAccountMenuClose = () => {
    setAccountMenuAnchor(null);
  };
  
  const handleLogout = () => {
    logout();
    handleAccountMenuClose();
    navigate('/');
  };
  
  const isActive = (path) => location.pathname === path;
  
  return (
    <Box sx={{ display: 'flex' }}>
      {/* App Bar */}
      <AppBar
        position="fixed"
        sx={{
          width: { sm: `calc(100% - ${sidebarOpen ? drawerWidth : 0}px)` },
          ml: { sm: `${sidebarOpen ? drawerWidth : 0}px` },
          backgroundColor: 'background.default',
          color: 'text.primary',
          boxShadow: '0px 1px 3px rgba(0, 0, 0, 0.12)',
          borderBottom: '1px solid',
          borderColor: 'divider'
        }}
      >
        <Toolbar>
          <IconButton
            color="inherit"
            aria-label="toggle drawer"
            edge="start"
            onClick={toggleSidebar}
            sx={{ mr: 2 }}
          >
            <MenuIcon />
          </IconButton>
          
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            Loyalty Redemption Portal
          </Typography>
          
          {/* User Info & Account Menu */}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            {company && (
              <Chip 
                label={`${company.name} (${company.tier || 'STANDARD'})`}
                variant="outlined"
                size="small"
              />
            )}
            <IconButton
              size="large"
              aria-label="account menu"
              onClick={handleAccountMenuOpen}
              color="inherit"
            >
              <Avatar sx={{ width: 32, height: 32 }}>
                {user?.firstName?.[0]}{user?.lastName?.[0]}
              </Avatar>
            </IconButton>
          </Box>
          
          {/* Account Menu */}
          <Menu
            anchorEl={accountMenuAnchor}
            open={Boolean(accountMenuAnchor)}
            onClose={handleAccountMenuClose}
            anchorOrigin={{
              vertical: 'bottom',
              horizontal: 'right',
            }}
            transformOrigin={{
              vertical: 'top',
              horizontal: 'right',
            }}
          >
            <MenuItem disabled>
              <ListItemIcon>
                <AccountIcon />
              </ListItemIcon>
              <ListItemText 
                primary={`${user?.firstName} ${user?.lastName}`}
                secondary={user?.email}
              />
            </MenuItem>
            <Divider />
            <MenuItem onClick={handleLogout}>
              <ListItemIcon>
                <LogoutIcon />
              </ListItemIcon>
              <ListItemText primary="Logout" />
            </MenuItem>
          </Menu>
        </Toolbar>
      </AppBar>
      
      {/* Navigation Drawer */}
      <Box
        component="nav"
        sx={{ width: { sm: sidebarOpen ? drawerWidth : 0 }, flexShrink: { sm: 0 } }}
      >
        <Drawer
          variant="persistent"
          sx={{
            '& .MuiDrawer-paper': {
              boxSizing: 'border-box',
              width: drawerWidth,
              borderRight: '1px solid',
              borderColor: 'divider'
            },
          }}
          open={sidebarOpen}
        >
          <Toolbar>
            <Typography variant="h6" noWrap component="div">
              Menu
            </Typography>
          </Toolbar>
          <Divider />
          
          {/* Company Info */}
          {company && (
            <Box sx={{ p: 2, backgroundColor: 'background.paper' }}>
              <Typography variant="subtitle2" color="text.secondary">
                Company
              </Typography>
              <Typography variant="body1" sx={{ fontWeight: 'bold' }}>
                {company.name}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                Account: {company.loyaltyAccountId}
              </Typography>
            </Box>
          )}
          
          <Divider />
          
          {/* Navigation Items */}
          <List>
            {navItems.map((item) => (
              <ListItem key={item.text} disablePadding>
                <ListItemButton
                  selected={isActive(item.path)}
                  onClick={() => navigate(item.path)}
                  sx={{
                    '&.Mui-selected': {
                      backgroundColor: 'primary.lighter',
                      color: 'primary.main',
                      '&:hover': {
                        backgroundColor: 'primary.lighter',
                      },
                    },
                  }}
                >
                  <ListItemIcon
                    sx={{
                      color: isActive(item.path) ? 'primary.main' : 'inherit',
                    }}
                  >
                    {item.icon}
                  </ListItemIcon>
                  <ListItemText primary={item.text} />
                </ListItemButton>
              </ListItem>
            ))}
          </List>
        </Drawer>
      </Box>
      
      {/* Main Content */}
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 3,
          width: { sm: `calc(100% - ${sidebarOpen ? drawerWidth : 0}px)` },
          minHeight: '100vh',
          backgroundColor: 'background.default'
        }}
      >
        <Toolbar />
        {children}
      </Box>
    </Box>
  );
};

export default MainLayout;