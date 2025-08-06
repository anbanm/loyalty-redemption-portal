#!/bin/bash

# Loyalty Redemption Portal - Run with Mock Services
# This script starts the application with all external dependencies mocked

echo "🚀 Starting Loyalty Redemption Portal with Mock Services..."

# Set the profile to use mock services
export SPRING_PROFILES_ACTIVE=mock

# Set mock-specific configuration
export LOYALTY_MOCK_ENABLED=true
export LOYALTY_API_MOCK_ENABLED=true
export LOYALTY_FULFILLMENT_VIRTUAL_MOCK_ENABLED=true
export LOYALTY_NOTIFICATION_MOCK_ENABLED=true

echo "📋 Configuration:"
echo "  - Profile: $SPRING_PROFILES_ACTIVE"
echo "  - Mock Services: ENABLED"
echo "  - Database: H2 In-Memory"
echo "  - Loyalty API: MOCKED"
echo "  - Virtual Fulfillment: MOCKED"
echo "  - Notifications: MOCKED"
echo ""

# Navigate to backend directory
cd "$(dirname "$0")/../backend" || exit 1

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven not found. Please install Maven first."
    exit 1
fi

# Run the application
echo "🏁 Starting application..."
echo "   Access URL: http://localhost:8080"
echo "   H2 Console: http://localhost:8080/h2-console"
echo "   API Docs: http://localhost:8080/swagger-ui.html"
echo "   Mock Management: http://localhost:8080/mock/status"
echo ""

mvn spring-boot:run \
  -Dspring-boot.run.profiles=mock \
  -Dspring-boot.run.jvmArguments="-Dloyalty.mock.enabled=true -Dloyalty.api.mock.enabled=true -Dloyalty.fulfillment.virtual.mock.enabled=true -Dloyalty.notification.mock.enabled=true"