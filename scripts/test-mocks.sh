#!/bin/bash

# Loyalty Redemption Portal - Mock Services Test Script
# This script demonstrates and tests the mock services functionality

BASE_URL="http://localhost:8080"
API_URL="$BASE_URL"
MOCK_URL="$BASE_URL/mock"

echo "🧪 Testing Loyalty Redemption Portal Mock Services"
echo "=================================================="
echo ""

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Helper function to make HTTP requests
make_request() {
    local method=$1
    local url=$2
    local data=$3
    
    if [ -n "$data" ]; then
        curl -s -X "$method" -H "Content-Type: application/json" -d "$data" "$url"
    else
        curl -s -X "$method" "$url"
    fi
}

# Helper function to check if service is running
check_service() {
    echo -n "🔍 Checking if service is running... "
    
    if curl -s "$BASE_URL/actuator/health" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Service is running${NC}"
        return 0
    else
        echo -e "${RED}✗ Service is not running${NC}"
        echo "Please start the service with: ./scripts/run-with-mocks.sh"
        exit 1
    fi
}

# Test mock services status
test_mock_status() {
    echo -e "\n${BLUE}📊 Testing Mock Services Status${NC}"
    echo "--------------------------------"
    
    response=$(make_request GET "$MOCK_URL/status")
    echo "Mock services status:"
    echo "$response" | jq '.'
}

# Test loyalty API mocks
test_loyalty_mocks() {
    echo -e "\n${BLUE}💰 Testing Loyalty API Mocks${NC}"
    echo "-----------------------------"
    
    # Test setting balance
    echo "Setting balance for ACME001 to 75000 points..."
    response=$(make_request POST "$MOCK_URL/loyalty/balance/ACME001?balance=75000")
    echo "Response: $response"
    
    # Test getting balance  
    echo -e "\nGetting balance for ACME001..."
    response=$(make_request GET "$MOCK_URL/loyalty/balance/ACME001")
    echo "Response:"
    echo "$response" | jq '.'
    
    # Test setting tier
    echo -e "\nSetting tier for ACME001 to PLATINUM..."
    response=$(make_request POST "$MOCK_URL/loyalty/tier/ACME001?tier=PLATINUM")
    echo "Response: $response"
}

# Test notification mocks
test_notification_mocks() {
    echo -e "\n${BLUE}📧 Testing Notification Mocks${NC}"
    echo "-----------------------------"
    
    # Clear email history
    echo "Clearing email history..."
    response=$(make_request POST "$MOCK_URL/notifications/clear")
    echo "Response: $response"
    
    # Get email count (should be 0)
    echo -e "\nGetting email statistics..."
    response=$(make_request GET "$MOCK_URL/notifications/statistics")
    echo "Response:"
    echo "$response" | jq '.'
}

# Test full order flow
test_order_flow() {
    echo -e "\n${BLUE}🛒 Testing Full Order Flow${NC}"
    echo "---------------------------"
    
    # This requires actual database entities, so we'll simulate what would happen
    echo "Note: Full order flow test requires database entities"
    echo "In a real test, this would:"
    echo "1. Create a company with loyalty account ACME001"
    echo "2. Create an account manager for that company"
    echo "3. Create a product"
    echo "4. Create an order through the redemption API"
    echo "5. Verify mock interactions were recorded"
    
    echo -e "\n${YELLOW}💡 To test full flow:${NC}"
    echo "1. Start the service: ./scripts/run-with-mocks.sh"
    echo "2. Access H2 console: http://localhost:8080/h2-console"
    echo "3. Use the integration tests: mvn test -Dtest=MockServicesIntegrationTest"
}

# Reset all mock data
reset_mocks() {
    echo -e "\n${BLUE}🔄 Resetting Mock Data${NC}"
    echo "----------------------"
    
    response=$(make_request POST "$MOCK_URL/reset-all")
    echo "Response: $response"
}

# Main test execution
main() {
    echo -e "${YELLOW}Starting mock services test...${NC}"
    echo ""
    
    # Check if service is running
    check_service
    
    # Run tests
    test_mock_status
    test_loyalty_mocks
    test_notification_mocks
    test_order_flow
    reset_mocks
    
    echo -e "\n${GREEN}🎉 Mock services test completed!${NC}"
    echo ""
    echo "Next steps:"
    echo "• Access API documentation: $BASE_URL/swagger-ui.html"
    echo "• View H2 database: $BASE_URL/h2-console"
    echo "• Monitor mock status: $MOCK_URL/status"
    echo "• Run integration tests: mvn test -Dtest=MockServicesIntegrationTest"
}

# Check if jq is available (for JSON formatting)
if ! command -v jq &> /dev/null; then
    echo -e "${YELLOW}⚠️  jq not found. Install jq for better JSON formatting.${NC}"
    echo ""
fi

# Run main function
main