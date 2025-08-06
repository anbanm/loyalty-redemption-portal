# Loyalty Redemption Portal

A B2B loyalty points redemption platform built with Broadleaf Commerce Community Edition.

## Overview

This platform enables account managers to redeem company loyalty points for physical and virtual products. Features include:

- 🏢 B2B account management with company hierarchies
- 💎 Loyalty points balance tracking via API
- 📦 Product catalog with inventory management
- 🔄 Separate workflows for physical vs virtual goods
- 🔐 Role-based access for account managers
- 📱 Responsive web interface

## Technology Stack

- **Backend**: Broadleaf Commerce Community Edition (Spring Boot)
- **Database**: PostgreSQL
- **Frontend**: React with Material-UI
- **API Integration**: REST APIs for loyalty points system
- **Build Tools**: Maven, npm

## Project Structure

```
loyalty-redemption-portal/
├── backend/                 # Broadleaf Commerce application
│   ├── src/
│   ├── pom.xml
│   └── ...
├── frontend/               # React frontend application
│   ├── src/
│   ├── package.json
│   └── ...
├── docs/                   # Documentation
│   ├── architecture.md
│   ├── api-spec.md
│   └── deployment.md
├── docker/                 # Docker configurations
│   ├── docker-compose.yml
│   └── ...
└── scripts/               # Utility scripts
```

## Quick Start

### Prerequisites

- Java 11+
- Node.js 18+
- PostgreSQL 13+
- Maven 3.6+

### Installation

1. Clone the repository:
```bash
git clone https://github.com/[your-username]/loyalty-redemption-portal.git
cd loyalty-redemption-portal
```

2. Set up the backend:
```bash
cd backend
mvn clean install
```

3. Set up the frontend:
```bash
cd frontend
npm install
```

4. Configure environment variables (see `.env.example`)

5. Run the application:
```bash
# Backend
cd backend
mvn spring-boot:run

# Frontend (in another terminal)
cd frontend
npm start
```

## Features

### For Account Managers
- Browse product catalog
- Check loyalty points balance
- Redeem points for products
- Track order status
- View redemption history

### For Administrators
- Manage product catalog
- Monitor inventory levels
- Process orders
- Configure workflows
- View analytics

## Development

See [CONTRIBUTING.md](CONTRIBUTING.md) for development guidelines.

## License

This project is licensed under the MIT License - see [LICENSE](LICENSE) file for details.