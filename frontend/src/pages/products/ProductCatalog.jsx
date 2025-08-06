import { useState, useEffect } from 'react';
import {
  Grid,
  Card,
  CardContent,
  CardMedia,
  Typography,
  Button,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Box,
  Chip,
  IconButton,
  Badge,
  Drawer,
  List,
  ListItem,
  ListItemText,
  Divider,
  Alert
} from '@mui/material';
import {
  Add as AddIcon,
  Remove as RemoveIcon,
  ShoppingCart as CartIcon,
  FilterList as FilterIcon,
  Search as SearchIcon
} from '@mui/icons-material';
import MainCard from '../../components/MainCard';
import { useProducts, useProductCategories, useProductBrands } from '../../hooks/useApi';
import { useCartStore, useProductStore, useAuthStore } from '../../store/useStore';
import toast from 'react-hot-toast';

// Product Card Component
const ProductCard = ({ product, onAddToCart }) => {
  const [quantity, setQuantity] = useState(1);

  const handleAddToCart = () => {
    onAddToCart(product, quantity);
    setQuantity(1);
  };

  return (
    <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <CardMedia
        component="img"
        height="200"
        image={product.imageUrl || '/api/placeholder/300/200'}
        alt={product.name}
        sx={{ objectFit: 'cover' }}
      />
      <CardContent sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column' }}>
        <Box sx={{ flexGrow: 1 }}>
          <Typography variant="h6" component="div" gutterBottom>
            {product.name}
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            {product.description}
          </Typography>
          <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
            <Chip 
              label={product.productType} 
              color={product.productType === 'VIRTUAL' ? 'primary' : 'secondary'}
              size="small"
            />
            {product.category && (
              <Chip label={product.category} variant="outlined" size="small" />
            )}
          </Box>
        </Box>
        
        <Box sx={{ mt: 'auto' }}>
          <Typography variant="h5" color="primary" sx={{ mb: 2, fontWeight: 'bold' }}>
            {product.pointsCost.toLocaleString()} pts
          </Typography>
          
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
            <IconButton 
              size="small" 
              onClick={() => setQuantity(Math.max(1, quantity - 1))}
            >
              <RemoveIcon />
            </IconButton>
            <TextField
              type="number"
              value={quantity}
              onChange={(e) => setQuantity(Math.max(1, parseInt(e.target.value) || 1))}
              size="small"
              sx={{ width: 80 }}
              inputProps={{ min: 1 }}
            />
            <IconButton 
              size="small" 
              onClick={() => setQuantity(quantity + 1)}
            >
              <AddIcon />
            </IconButton>
          </Box>
          
          <Button
            fullWidth
            variant="contained"
            startIcon={<CartIcon />}
            onClick={handleAddToCart}
            disabled={!product.isActive}
          >
            Add to Cart
          </Button>
        </Box>
      </CardContent>
    </Card>
  );
};

// Filters Drawer Component
const FiltersDrawer = ({ open, onClose, filters, onFiltersChange, categories, brands }) => {
  return (
    <Drawer anchor="left" open={open} onClose={onClose}>
      <Box sx={{ width: 300, p: 2 }}>
        <Typography variant="h6" gutterBottom>
          Filters
        </Typography>
        <Divider sx={{ mb: 2 }} />
        
        <List>
          <ListItem sx={{ px: 0, flexDirection: 'column', alignItems: 'stretch' }}>
            <FormControl fullWidth sx={{ mb: 2 }}>
              <InputLabel>Category</InputLabel>
              <Select
                value={filters.category}
                label="Category"
                onChange={(e) => onFiltersChange({ category: e.target.value })}
              >
                <MenuItem value="">All Categories</MenuItem>
                {categories.map((category) => (
                  <MenuItem key={category} value={category}>
                    {category}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </ListItem>
          
          <ListItem sx={{ px: 0, flexDirection: 'column', alignItems: 'stretch' }}>
            <FormControl fullWidth sx={{ mb: 2 }}>
              <InputLabel>Brand</InputLabel>
              <Select
                value={filters.brand}
                label="Brand"
                onChange={(e) => onFiltersChange({ brand: e.target.value })}
              >
                <MenuItem value="">All Brands</MenuItem>
                {brands.map((brand) => (
                  <MenuItem key={brand} value={brand}>
                    {brand}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </ListItem>
          
          <ListItem sx={{ px: 0, flexDirection: 'column', alignItems: 'stretch' }}>
            <FormControl fullWidth sx={{ mb: 2 }}>
              <InputLabel>Product Type</InputLabel>
              <Select
                value={filters.productType}
                label="Product Type"
                onChange={(e) => onFiltersChange({ productType: e.target.value })}
              >
                <MenuItem value="">All Types</MenuItem>
                <MenuItem value="PHYSICAL">Physical</MenuItem>
                <MenuItem value="VIRTUAL">Virtual</MenuItem>
              </Select>
            </FormControl>
          </ListItem>
          
          <Divider sx={{ my: 2 }} />
          
          <ListItem sx={{ px: 0, flexDirection: 'column', alignItems: 'stretch' }}>
            <Typography variant="subtitle2" gutterBottom>
              Points Range
            </Typography>
            <Box sx={{ display: 'flex', gap: 1 }}>
              <TextField
                label="Min Points"
                type="number"
                value={filters.minPoints}
                onChange={(e) => onFiltersChange({ minPoints: e.target.value })}
                size="small"
                fullWidth
              />
              <TextField
                label="Max Points"
                type="number"
                value={filters.maxPoints}
                onChange={(e) => onFiltersChange({ maxPoints: e.target.value })}
                size="small"
                fullWidth
              />
            </Box>
          </ListItem>
          
          <ListItem sx={{ px: 0, mt: 2 }}>
            <Button
              fullWidth
              variant="outlined"
              onClick={() => onFiltersChange({
                category: '',
                brand: '',
                productType: '',
                minPoints: '',
                maxPoints: ''
              })}
            >
              Clear Filters
            </Button>
          </ListItem>
        </List>
      </Box>
    </Drawer>
  );
};

// Main Product Catalog Component
const ProductCatalog = () => {
  const [filtersOpen, setFiltersOpen] = useState(false);
  
  // Store hooks
  const { user, company } = useAuthStore();
  const { addItem, totalItems } = useCartStore();
  const { filters, updateFilters, getFilteredProducts } = useProductStore();
  
  // API hooks
  const { data: productsData, isLoading: productsLoading, error: productsError } = useProducts();
  const { data: categoriesData } = useProductCategories();
  const { data: brandsData } = useProductBrands();
  
  // State
  const [searchTerm, setSearchTerm] = useState(filters.search || '');
  
  // Extract data from API responses
  const products = productsData?.data?.content || productsData?.data || [];
  const categories = categoriesData?.data || [];
  const brands = brandsData?.data || [];
  
  // Handle search
  useEffect(() => {
    const timeoutId = setTimeout(() => {
      updateFilters({ search: searchTerm });
    }, 500);
    
    return () => clearTimeout(timeoutId);
  }, [searchTerm, updateFilters]);
  
  // Filter products
  const filteredProducts = getFilteredProducts();
  
  const handleAddToCart = (product, quantity) => {
    if (!user || !company) {
      toast.error('Please log in to add items to cart');
      return;
    }
    
    addItem(product, quantity);
    toast.success(`Added ${product.name} to cart`);
  };
  
  const handleFiltersChange = (newFilters) => {
    updateFilters(newFilters);
  };
  
  if (!user || !company) {
    return (
      <MainCard title="Product Catalog">
        <Alert severity="warning">
          Please log in to view and purchase products.
        </Alert>
      </MainCard>
    );
  }
  
  if (productsError) {
    return (
      <MainCard title="Product Catalog">
        <Alert severity="error">
          Failed to load products. Please try again later.
        </Alert>
      </MainCard>
    );
  }
  
  return (
    <>
      <MainCard
        title="Product Catalog"
        secondary={
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Badge badgeContent={totalItems} color="primary">
              <CartIcon />
            </Badge>
            <IconButton onClick={() => setFiltersOpen(true)}>
              <FilterIcon />
            </IconButton>
          </Box>
        }
      >
        {/* Search Bar */}
        <Box sx={{ mb: 3, display: 'flex', gap: 2, alignItems: 'center' }}>
          <TextField
            fullWidth
            placeholder="Search products..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            InputProps={{
              startAdornment: <SearchIcon sx={{ mr: 1, color: 'text.secondary' }} />
            }}
          />
        </Box>
        
        {/* Active Filters */}
        {(filters.category || filters.brand || filters.productType || filters.minPoints || filters.maxPoints) && (
          <Box sx={{ mb: 3, display: 'flex', gap: 1, flexWrap: 'wrap' }}>
            <Typography variant="body2" sx={{ alignSelf: 'center', mr: 1 }}>
              Active Filters:
            </Typography>
            {filters.category && (
              <Chip 
                label={`Category: ${filters.category}`} 
                onDelete={() => handleFiltersChange({ category: '' })}
                size="small"
              />
            )}
            {filters.brand && (
              <Chip 
                label={`Brand: ${filters.brand}`} 
                onDelete={() => handleFiltersChange({ brand: '' })}
                size="small"
              />
            )}
            {filters.productType && (
              <Chip 
                label={`Type: ${filters.productType}`} 
                onDelete={() => handleFiltersChange({ productType: '' })}
                size="small"
              />
            )}
            {(filters.minPoints || filters.maxPoints) && (
              <Chip 
                label={`Points: ${filters.minPoints || '0'} - ${filters.maxPoints || '∞'}`} 
                onDelete={() => handleFiltersChange({ minPoints: '', maxPoints: '' })}
                size="small"
              />
            )}
          </Box>
        )}
        
        {/* Products Grid */}
        {productsLoading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
            <Typography>Loading products...</Typography>
          </Box>
        ) : filteredProducts.length === 0 ? (
          <Box sx={{ textAlign: 'center', p: 4 }}>
            <Typography variant="h6" color="text.secondary">
              No products found
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Try adjusting your search or filters
            </Typography>
          </Box>
        ) : (
          <Grid container spacing={3}>
            {filteredProducts.map((product) => (
              <Grid item xs={12} sm={6} md={4} lg={3} key={product.id}>
                <ProductCard
                  product={product}
                  onAddToCart={handleAddToCart}
                />
              </Grid>
            ))}
          </Grid>
        )}
      </MainCard>
      
      {/* Filters Drawer */}
      <FiltersDrawer
        open={filtersOpen}
        onClose={() => setFiltersOpen(false)}
        filters={filters}
        onFiltersChange={handleFiltersChange}
        categories={categories}
        brands={brands}
      />
    </>
  );
};

export default ProductCatalog;