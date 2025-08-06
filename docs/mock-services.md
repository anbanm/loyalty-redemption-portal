# Mock Services Guide

This guide explains how to use the mock services for testing the Loyalty Redemption Portal independently of external dependencies.

## Overview

The mock services replace all external dependencies allowing you to:
- Test the application without real loyalty API integration
- Simulate various scenarios (success, failure, edge cases)
- Control data states for predictable testing
- Run automated tests without external service dependencies

## Available Mock Services

### 1. Mock Loyalty API Client

**Purpose**: Replaces the external loyalty points API
**Features**:
- In-memory account balance management
- Configurable success/failure rates
- Transaction simulation with realistic delays
- Support for multiple loyalty accounts

**Default Mock Accounts**:
- `ACME001`: 150,000 points (GOLD tier)
- `GLOBAL002`: 75,000 points (SILVER tier)  
- `TECH003`: 200,000 points (PLATINUM tier)
- `STARTUP004`: 25,000 points (BRONZE tier)

### 2. Mock Virtual Fulfillment Service

**Purpose**: Simulates virtual product delivery
**Features**:
- Different fulfillment scenarios based on product type
- Configurable failure rate (3% by default)
- Fulfillment tracking and history
- Realistic processing delays

### 3. Mock Notification Service

**Purpose**: Captures and logs all email notifications
**Features**:
- Email history storage
- Filtering by recipient, type, date
- Statistics and analytics
- No actual email sending

## Configuration

### Enable Mock Services

Add these properties to enable mocks:

```yaml
loyalty:
  mock:
    enabled: true
  api:
    mock:
      enabled: true
  fulfillment:
    virtual:
      mock:
        enabled: true  
  notification:
    mock:
      enabled: true
```

### Profile Configuration

Use the `mock` profile for complete mock setup:

```bash
java -jar app.jar --spring.profiles.active=mock
```

Or use the provided script:
```bash
./scripts/run-with-mocks.sh
```

## Mock Management API

The `/mock` endpoint provides comprehensive mock management:

### Loyalty API Management

```bash
# Set account balance
POST /mock/loyalty/balance/{accountId}?balance=100000

# Set account tier
POST /mock/loyalty/tier/{accountId}?tier=PLATINUM

# Get current balance
GET /mock/loyalty/balance/{accountId}

# Reset all loyalty data
POST /mock/loyalty/reset
```

### Notification Management

```bash
# Get all sent emails
GET /mock/notifications/emails

# Get emails by recipient
GET /mock/notifications/emails/recipient/{email}

# Get emails by type
GET /mock/notifications/emails/type/{type}

# Get statistics
GET /mock/notifications/statistics

# Clear email history
POST /mock/notifications/clear
```

### Virtual Fulfillment Management

```bash
# Get fulfillment status
GET /mock/fulfillment/virtual/status/{fulfillmentId}

# Get customer fulfillments
GET /mock/fulfillment/virtual/customer/{email}

# Get statistics
GET /mock/fulfillment/virtual/statistics

# Reset fulfillment data
POST /mock/fulfillment/virtual/reset
```

### General Management

```bash
# Get mock services status
GET /mock/status

# Reset all mock data
POST /mock/reset-all
```

## Testing Scenarios

### Successful Order Flow

1. **Setup**: Ensure account has sufficient balance
   ```bash
   curl -X POST "http://localhost:8080/mock/loyalty/balance/ACME001?balance=50000"
   ```

2. **Create Order**: Use the standard redemption API
   ```bash
   curl -X POST "http://localhost:8080/redemption/orders" \
     -H "Content-Type: application/json" \
     -d '{
       "companyId": "uuid-here",
       "accountManagerId": "uuid-here", 
       "items": [{"productId": "uuid-here", "quantity": 1}]
     }'
   ```

3. **Verify**: Check mock services captured the interactions
   ```bash
   curl "http://localhost:8080/mock/notifications/emails"
   curl "http://localhost:8080/mock/loyalty/balance/ACME001"
   ```

### Insufficient Balance Scenario

1. **Setup**: Set low balance
   ```bash
   curl -X POST "http://localhost:8080/mock/loyalty/balance/ACME001?balance=100"
   ```

2. **Create Order**: For more points than available
3. **Verify**: Order should fail, balance unchanged

### System Failure Simulation

The mock services automatically simulate failures:
- **Loyalty API**: 5% debit failure rate, 2% credit failure rate
- **Virtual Fulfillment**: 3% failure rate
- **Delays**: Realistic processing times (100-800ms)

## Integration Testing

Use the provided test classes:

### Run Mock Service Tests
```bash
mvn test -Dspring.profiles.active=test -Dtest=MockServicesIntegrationTest
```

### Run Mock Controller Tests  
```bash
mvn test -Dspring.profiles.active=test -Dtest=MockControllerTest
```

## Monitoring Mock Services

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Mock Status
```bash
curl http://localhost:8080/mock/status
```

### Email Statistics
```bash
curl http://localhost:8080/mock/notifications/statistics
```

## Development Workflow

1. **Start with Mocks**
   ```bash
   ./scripts/run-with-mocks.sh
   ```

2. **Setup Test Data**
   ```bash
   # Set up loyalty accounts
   curl -X POST "http://localhost:8080/mock/loyalty/balance/ACME001?balance=100000"
   curl -X POST "http://localhost:8080/mock/loyalty/tier/ACME001?tier=GOLD"
   ```

3. **Test Your Scenarios**
   - Use the standard API endpoints
   - Verify behavior through mock endpoints

4. **Reset Between Tests**
   ```bash
   curl -X POST "http://localhost:8080/mock/reset-all"
   ```

## Mock vs Real Services

| Aspect | Mock Services | Real Services |
|--------|--------------|---------------|
| **Setup** | No external dependencies | Requires API keys, network access |
| **Speed** | Fast (in-memory) | Network latency |
| **Reliability** | 100% available | Subject to external service issues |
| **Data Control** | Complete control | Limited control |
| **Scenarios** | Can simulate any scenario | Real-world scenarios only |
| **Cost** | Free | May have usage costs |

## Troubleshooting

### Mock Services Not Working
1. Verify profile is active: `logging.level.com.loyaltyportal.service.mock=DEBUG`
2. Check configuration: `GET /mock/status`
3. Ensure properties are set correctly

### Tests Failing
1. Check test profile: `spring.profiles.active=test`
2. Verify H2 database setup
3. Clear mock data between tests

### Performance Issues
1. Reduce mock delays in configuration
2. Disable verbose logging
3. Use test profile for faster execution

## Best Practices

1. **Reset State**: Always reset mock data between test scenarios
2. **Verify Interactions**: Use mock endpoints to verify expected interactions
3. **Test Edge Cases**: Use mocks to test failure scenarios
4. **Monitor Logs**: Enable DEBUG logging for mock services during development
5. **Isolate Tests**: Each test should set up its own mock data state

## API Documentation

When running with mocks, access the full API documentation at:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs

The mock management endpoints are included in the API documentation.