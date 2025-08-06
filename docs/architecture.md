# Loyalty Redemption Portal - Architecture

## System Overview

The Loyalty Redemption Portal is a B2B platform that allows company account managers to redeem loyalty points for products. The system integrates with external loyalty points APIs and manages both physical and virtual product fulfillment.

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         Frontend Layer                           │
│                                                                 │
│  ┌──────────────────┐    ┌──────────────────┐                 │
│  │   React App      │    │   Admin Portal   │                 │
│  │  (Account Mgr)   │    │    (Backend)     │                 │
│  └────────┬─────────┘    └────────┬─────────┘                 │
│           │                        │                            │
└───────────┼────────────────────────┼────────────────────────────┘
            │                        │
            ▼                        ▼
┌─────────────────────────────────────────────────────────────────┐
│                      API Gateway (Spring)                        │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │           Authentication & Authorization                  │  │
│  └─────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Broadleaf Commerce Core                        │
│                                                                 │
│  ┌───────────────┐  ┌───────────────┐  ┌──────────────────┐  │
│  │    Product    │  │   Inventory   │  │      Order       │  │
│  │   Service     │  │   Service     │  │     Service      │  │
│  └───────────────┘  └───────────────┘  └──────────────────┘  │
│                                                                 │
│  ┌───────────────┐  ┌───────────────┐  ┌──────────────────┐  │
│  │   Account     │  │  Redemption   │  │    Workflow      │  │
│  │   Service     │  │   Service     │  │     Engine       │  │
│  └───────────────┘  └───────────────┘  └──────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────────┐
│                        Data Layer                                │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │                   PostgreSQL Database                     │  │
│  │  • Companies  • Products  • Orders  • Transactions       │  │
│  │  • Users      • Inventory • Workflows                    │  │
│  └─────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────────┐
│                   External Integrations                          │
│                                                                 │
│  ┌───────────────┐  ┌───────────────┐  ┌──────────────────┐  │
│  │ Loyalty Points│  │  Fulfillment  │  │  Notification    │  │
│  │      API      │  │      API      │  │    Service       │  │
│  └───────────────┘  └───────────────┘  └──────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

## Core Components

### 1. Frontend Layer

#### Account Manager Portal (React)
- **Purpose**: Web interface for account managers to browse and redeem products
- **Key Features**:
  - Product catalog browsing
  - Points balance display
  - Order placement
  - Order history
- **Technology**: React 18, Material-UI, Redux Toolkit

#### Admin Portal
- **Purpose**: Administrative interface for managing products and orders
- **Key Features**:
  - Product management
  - Inventory tracking
  - Order processing
  - Analytics dashboard
- **Technology**: Broadleaf Admin (customized)

### 2. Backend Services

#### Product Service
- Manages product catalog
- Handles product categorization (physical/virtual)
- Maintains product metadata and pricing in points

#### Inventory Service
- Tracks available inventory
- Reserves inventory during redemption
- Handles stock updates

#### Order Service
- Creates redemption orders
- Manages order lifecycle
- Integrates with fulfillment workflows

#### Account Service
- Manages company accounts
- Handles account manager authentication
- Maintains user permissions

#### Redemption Service
- Orchestrates the redemption process
- Integrates with loyalty points API
- Handles transaction rollback on failure

#### Workflow Engine
- Routes physical product orders to manual queue
- Triggers API calls for virtual products
- Manages order state transitions

### 3. Data Model

#### Core Entities

```sql
-- Company Account
CREATE TABLE company (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    loyalty_account_id VARCHAR(100) UNIQUE,
    tier_level VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Account Manager
CREATE TABLE account_manager (
    id UUID PRIMARY KEY,
    company_id UUID REFERENCES company(id),
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255),
    role VARCHAR(50),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP
);

-- Product
CREATE TABLE product (
    id UUID PRIMARY KEY,
    sku VARCHAR(100) UNIQUE,
    name VARCHAR(255),
    description TEXT,
    product_type VARCHAR(20), -- PHYSICAL or VIRTUAL
    points_cost INTEGER,
    image_url VARCHAR(500),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP
);

-- Inventory
CREATE TABLE inventory (
    id UUID PRIMARY KEY,
    product_id UUID REFERENCES product(id),
    quantity_available INTEGER,
    quantity_reserved INTEGER,
    last_updated TIMESTAMP
);

-- Redemption Order
CREATE TABLE redemption_order (
    id UUID PRIMARY KEY,
    order_number VARCHAR(50) UNIQUE,
    company_id UUID REFERENCES company(id),
    account_manager_id UUID REFERENCES account_manager(id),
    total_points INTEGER,
    status VARCHAR(50),
    created_at TIMESTAMP,
    completed_at TIMESTAMP
);

-- Order Items
CREATE TABLE order_item (
    id UUID PRIMARY KEY,
    order_id UUID REFERENCES redemption_order(id),
    product_id UUID REFERENCES product(id),
    quantity INTEGER,
    points_per_item INTEGER,
    fulfillment_status VARCHAR(50)
);

-- Loyalty Transaction
CREATE TABLE loyalty_transaction (
    id UUID PRIMARY KEY,
    order_id UUID REFERENCES redemption_order(id),
    company_id UUID REFERENCES company(id),
    points_amount INTEGER,
    transaction_type VARCHAR(20),
    external_transaction_id VARCHAR(100),
    status VARCHAR(50),
    created_at TIMESTAMP
);
```

### 4. External Integrations

#### Loyalty Points API
- **Endpoints Required**:
  - `GET /balance/{accountId}` - Check current points balance
  - `POST /debit` - Deduct points for redemption
  - `POST /credit` - Refund points on cancellation
- **Authentication**: OAuth 2.0 or API Key
- **Error Handling**: Automatic retry with exponential backoff

#### Fulfillment API (for virtual products)
- **Purpose**: Trigger delivery of virtual products
- **Integration**: REST API with webhook callbacks

#### Notification Service
- **Purpose**: Send order confirmations and status updates
- **Channels**: Email, SMS (optional)

## Security Considerations

1. **Authentication**: JWT-based authentication for account managers
2. **Authorization**: Role-based access control (RBAC)
3. **API Security**: Rate limiting, API key management
4. **Data Encryption**: TLS for transit, AES for sensitive data at rest
5. **Audit Trail**: All redemptions and point transactions logged

## Scalability Strategy

1. **Horizontal Scaling**: Stateless services behind load balancer
2. **Caching**: Redis for session and points balance caching
3. **Async Processing**: Message queue for order processing
4. **Database**: Read replicas for reporting queries

## Deployment Architecture

```yaml
Production Environment:
  - 2x Application Servers (Spring Boot)
  - 1x PostgreSQL Primary (RDS)
  - 1x PostgreSQL Read Replica
  - 1x Redis Cache
  - 1x Load Balancer (ALB)
  - CDN for static assets
```

## Monitoring & Observability

1. **Application Metrics**: Micrometer + Prometheus
2. **Logging**: ELK Stack (Elasticsearch, Logstash, Kibana)
3. **APM**: Application Performance Monitoring
4. **Alerts**: PagerDuty integration for critical issues