#!/bin/bash

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080/api"
USER_TOKEN=""
ADMIN_TOKEN=""

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}FreshMart Review Management - Manual Test${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Test 1: Create a test user and login
echo -e "${YELLOW}1. Registering test user...${NC}"
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "reviewer1",
    "email": "reviewer1@test.com",
    "phone": "0987654321",
    "password": "password123"
  }')

echo -e "Response: $REGISTER_RESPONSE"
echo ""

# Test 2: Login
echo -e "${YELLOW}2. Logging in...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "reviewer1",
    "password": "password123"
  }')

echo -e "Response: $LOGIN_RESPONSE"
USER_TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
echo -e "Token: $USER_TOKEN"
echo ""

# Test 3: Get all products
echo -e "${YELLOW}3. Getting all products...${NC}"
curl -s -X GET "$BASE_URL/products" | jq '.'
echo ""

# Test 4: Get all reviews for product 1 (public endpoint)
echo -e "${YELLOW}4. Getting reviews for product 1 (public)...${NC}"
curl -s -X GET "$BASE_URL/reviews/product/1" | jq '.'
echo ""

# Test 5: Create a review for product 1
echo -e "${YELLOW}5. Creating a review for product 1...${NC}"
curl -s -X POST "$BASE_URL/reviews/product/1" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "rating": 5,
    "comment": "Excellent product, very fresh!"
  }' | jq '.'
echo ""

# Test 6: Get review stats for product 1
echo -e "${YELLOW}6. Getting review stats for product 1...${NC}"
curl -s -X GET "$BASE_URL/reviews/product/1/stats" | jq '.'
echo ""

# Test 7: Update the review
echo -e "${YELLOW}7. Getting reviews to find review ID...${NC}"
REVIEWS=$(curl -s -X GET "$BASE_URL/reviews/product/1")
REVIEW_ID=$(echo $REVIEWS | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo -e "Review ID: $REVIEW_ID"

if [ -n "$REVIEW_ID" ] && [ "$REVIEW_ID" != "0" ]; then
    echo -e "${YELLOW}8. Updating the review...${NC}"
    curl -s -X PUT "$BASE_URL/reviews/$REVIEW_ID" \
      -H "Authorization: Bearer $USER_TOKEN" \
      -H "Content-Type: application/json" \
      -d '{
        "rating": 4,
        "comment": "Good product but a bit pricey"
      }' | jq '.'
    echo ""
fi

# Test 8: Get user's reviews
echo -e "${YELLOW}9. Getting current user's reviews...${NC}"
USER_ID=$(echo $LOGIN_RESPONSE | grep -o '"userId":[0-9]*' | cut -d':' -f2)
if [ -n "$USER_ID" ] && [ "$USER_ID" != "0" ]; then
    curl -s -X GET "$BASE_URL/reviews/user/$USER_ID" | jq '.'
fi
echo ""

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Test Complete!${NC}"
echo -e "${GREEN}========================================${NC}"
