# Loyalty Redemption Portal - Frontend

React-based frontend application for the B2B loyalty points redemption portal. Built on top of the Mantis React admin template with Material-UI components.

## Features

- **Product Catalog** - Browse and search products with filtering
- **Shopping Cart** - Add items, manage quantities, checkout flow
- **Order Management** - View order history, track status, manage orders
- **Responsive Design** - Works on desktop, tablet, and mobile
- **Professional UI** - Based on Mantis admin template with Material-UI v7

## Technology Stack

- **React 19** - Modern React with hooks
- **Material-UI v7** - Component library and design system
- **React Router** - Client-side routing
- **React Query** - Data fetching and caching
- **Zustand** - State management
- **Axios** - HTTP client
- **React Hot Toast** - Notifications

## Getting Started

### Prerequisites

- Node.js 18+ 
- npm or yarn

### Installation

```bash
# Install dependencies
npm install

# Start development server
npm start
```

The app will be available at http://localhost:3000

### Backend Integration

The frontend is configured to proxy API requests to the backend running on port 8080. Make sure the backend is running before starting the frontend.

## Project Structure

```
src/
├── components/         # Reusable UI components
│   └── MainCard.jsx   # Main card wrapper component
├── hooks/             # Custom React hooks
│   └── useApi.js      # API integration hooks
├── layout/            # Layout components
│   └── MainLayout.jsx # Main application layout
├── pages/             # Page components
│   ├── cart/          # Shopping cart page
│   ├── orders/        # Order management page
│   └── products/      # Product catalog page
├── services/          # API services
│   └── api.js         # Axios configuration and endpoints
├── store/             # State management
│   └── useStore.js    # Zustand stores
├── themes/            # Theme configuration
│   └── index.js       # Material-UI theme
├── App.js             # Main app component with routing
└── index.js           # React app entry point
```

## Available Scripts

- `npm start` - Start development server
- `npm build` - Build for production
- `npm test` - Run tests
- `npm lint` - Check code quality
- `npm lint:fix` - Fix linting issues

## Mock Authentication

For development, the app includes mock authentication that automatically logs in with:

- **User**: John Doe (Account Manager)
- **Company**: ACME Corporation  
- **Loyalty Account**: ACME-123456

## API Integration

The frontend integrates with the backend REST API:

- **Products**: Browse catalog, search, get details
- **Cart**: Add/remove items, manage quantities
- **Orders**: Create orders, view history, track status
- **Loyalty**: Check point balances, process redemptions

All API calls include error handling, loading states, and user feedback via toast notifications.

## Responsive Design

The application is fully responsive:

- **Desktop**: Full sidebar navigation with detailed views
- **Tablet**: Collapsible sidebar with optimized layouts  
- **Mobile**: Mobile-friendly navigation and touch interactions

---

*Built on top of the excellent [Mantis Free React Admin Template](https://mantisdashboard.com/free/) by CodedThemes*
