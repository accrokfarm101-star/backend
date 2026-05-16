# Shopping Cart Feature Implementation Summary

## Overview
Successfully implemented a complete shopping cart feature for the Fresh Mart backend with full REST API endpoints, service layer logic, database models, and comprehensive integration tests.

## Components Created

### 1. Data Models
- **Cart.java** - Main cart entity with bidirectional relationship to User and one-to-many relationship to CartItem
  - Fields: id, user, cartItems, totalAmount, createdAt, updatedAt
  - Cascading delete enabled for proper cleanup
  
- **CartItem.java** - Individual item in cart
  - Fields: id, cart, product, quantity, unitPrice, totalPrice
  - Includes calculateTotalPrice() method for pricing logic

### 2. DTOs (Data Transfer Objects)
- **CartItemRequest.java** - Request DTO for adding/updating cart items
- **CartItemResponse.java** - Response DTO for cart items
- **CartResponse.java** - Response DTO for complete cart with all items and total

### 3. Data Access Layer
- **CartRepository.java** - JPA repository for Cart entity with findByUserId query
- **CartItemRepository.java** - JPA repository for CartItem with findByIdAndCartId query

### 4. Service Layer
- **CartService.java** - Interface defining cart operations
- **CartServiceImpl.java** - Implementation with:
  - `getCart(userId)` - Retrieve or create user's cart
  - `addItem(userId, cartItemRequest)` - Add/update item in cart
  - `updateItem(userId, cartItemId, quantity)` - Update item quantity
  - `removeItem(userId, cartItemId)` - Remove item from cart
  - `clearCart(userId)` - Empty entire cart
  - Stock validation on all add/update operations
  - Automatic total calculation

### 5. REST API Controller
- **CartController.java** - REST endpoints:
  - `GET /api/cart` - Get user's cart
  - `POST /api/cart/items` - Add item to cart
  - `PUT /api/cart/items/{itemId}` - Update item quantity
  - `DELETE /api/cart/items/{itemId}` - Remove item from cart
  - `DELETE /api/cart` - Clear entire cart
  - All endpoints require CUSTOMER or ADMIN role

### 6. Integration Tests
- **CartControllerTests.java** - 4 comprehensive test cases:
  - `testAddItemToCart()` - Add item and verify cart
  - `testUpdateCartItemQuantity()` - Update quantity and recalculate
  - `testRemoveCartItem()` - Remove item and verify total
  - `testClearCart()` - Empty cart completely

## Key Features

### Security
- JWT authentication required for all cart operations
- Role-based access control (CUSTOMER or ADMIN)
- Principal-based user identification

### Validation
- Stock validation before adding items
- Quantity validation (must be >= 1)
- Product existence validation
- User existence validation

### Business Logic
- Automatic cart creation for new users
- Item deduplication (updating existing items instead of duplicating)
- Real-time total amount calculation
- Stock management integration

### Database Constraints
- Cascade delete from User to Cart for data integrity
- Foreign key relationship between Cart and User (one-to-one)
- Foreign key relationship between CartItem and Cart (one-to-many)
- Foreign key relationship between CartItem and Product (many-to-one)

## Integration with Existing System

### Relationship to Order Management
- Cart provides temporary storage for selected items
- Orders are created from cart items via OrderController
- Order creation validation reuses cart stock validation logic

### Authentication Integration
- Uses existing JWT security framework
- Integrates with UserService for current user retrieval
- Works with existing Spring Security configuration

### Data Model Updates
- Updated User.java to include bidirectional Cart relationship with cascade
- Added CartItemRepository to Spring Data JPA repositories

## Testing Results

All 26 tests pass successfully:
- ✅ 8/8 AuthControllerTests pass
- ✅ 11/11 AuthenticationTests pass
- ✅ 4/4 CartControllerTests pass
- ✅ 3/3 ProductControllerTests pass

## API Usage Examples

### Add Item to Cart
```
POST /api/cart/items
Authorization: Bearer {token}
Content-Type: application/json

{
  "productId": 1,
  "quantity": 2
}

Response: 201 Created
{
  "success": true,
  "message": "Thêm sản phẩm vào giỏ hàng thành công",
  "data": {
    "id": 1,
    "userId": 1,
    "username": "customer",
    "cartItems": [
      {
        "id": 1,
        "productId": 1,
        "productName": "Bơ sáp",
        "quantity": 2,
        "unitPrice": 50000,
        "totalPrice": 100000
      }
    ],
    "totalAmount": 100000,
    "createdAt": "2026-05-10T00:21:40.911Z",
    "updatedAt": "2026-05-10T00:21:40.911Z"
  }
}
```

### Update Quantity
```
PUT /api/cart/items/1?quantity=3
Authorization: Bearer {token}

Response: 200 OK
{
  "success": true,
  "message": "Cập nhật số lượng giỏ hàng thành công",
  "data": {
    ...
    "cartItems": [
      {
        ...
        "quantity": 3,
        "totalPrice": 150000
      }
    ],
    "totalAmount": 150000
  }
}
```

### Clear Cart
```
DELETE /api/cart
Authorization: Bearer {token}

Response: 200 OK
{
  "success": true,
  "message": "Giỏ hàng đã được làm mới",
  "data": {
    ...
    "cartItems": [],
    "totalAmount": 0
  }
}
```

## File Structure
```
src/main/java/com/freshmart/
├── controller/CartController.java
├── service/CartService.java
├── serviceImpl/CartServiceImpl.java
├── repository/CartRepository.java
├── repository/CartItemRepository.java
├── model/
│   ├── entity/
│   │   ├── Cart.java (updated)
│   │   ├── CartItem.java
│   │   └── User.java (updated)
│   └── dto/
│       ├── request/CartItemRequest.java
│       └── response/
│           ├── CartItemResponse.java
│           └── CartResponse.java

src/test/java/com/freshmart/
└── CartControllerTests.java
```

## Next Steps (Recommendations)

1. **Order Integration**: Add endpoint to create order from cart items
2. **Wishlist Feature**: Extend cart concept to favorites/wishlist
3. **Cart Persistence**: Add option to save cart across sessions
4. **Bulk Operations**: Add bulk add/remove operations
5. **Coupon Support**: Implement discount/coupon application to cart
6. **Cart Expiry**: Implement auto-clear of old carts
7. **Performance**: Add caching for frequently accessed carts

## Notes

- All entities use Lombok for boilerplate reduction
- Jakarta Persistence API (JPA) with Hibernate as provider
- Uses transactional service methods for data consistency
- H2 database used for testing with in-memory persistence
- Spring Security handles authentication/authorization
- Comprehensive error handling with custom ApiResponse wrapper
