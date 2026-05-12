# API Input/Output Documentation - Fresh Mart Backend

## Table of Contents
1. [Product APIs](#product-apis)
2. [Category APIs](#category-apis)
3. [Order APIs](#order-apis)
4. [Cart APIs](#cart-apis)

---

## Authentication APIs

### 1. Register User
**Endpoint**: `POST /api/auth/register`
**Authentication**: None
**Role Required**: Public

#### Input (Request Body)
```json
{
  "username": "customer001",
  "email": "customer@example.com",
  "phone": "0912345678",
  "password": "SecurePass123",
  "confirmPassword": "SecurePass123"
}
```

**Input Validation**:
- `username`: Not blank, unique, 3-50 characters
- `email`: Valid email format, unique
- `phone`: Not blank, 10+ digits
- `password`: Not blank, 6+ characters
- `confirmPassword`: Must match password

#### Output (Response)
**Status**: 201 Created (Success) / 400 Bad Request (Validation Error)

**Success Response**:
```json
{
  "success": true,
  "message": "Đăng ký thành công",
  "data": {
    "id": 1,
    "username": "customer001",
    "email": "customer@example.com",
    "phone": "0912345678",
    "role": "ROLE_CUSTOMER",
    "createdAt": "2026-05-10T00:30:00Z"
  }
}
```

**Error Response**:
```json
{
  "success": false,
  "message": "Email đã tồn tại trong hệ thống",
  "data": null
}
```

---

### 2. Login User
**Endpoint**: `POST /api/auth/login`
**Authentication**: None
**Role Required**: Public

#### Input (Request Body)
```json
{
  "usernameOrEmail": "customer@example.com",
  "password": "SecurePass123"
}
```

**Input Validation**:
- `usernameOrEmail`: Not blank (accepts username or email)
- `password`: Not blank

#### Output (Response)
**Status**: 200 OK (Success) / 401 Unauthorized (Invalid Credentials)

**Success Response**:
```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjdXN0b21lciIsImlhdCI6MTc3ODMyNjQxNCwiZXhwIjoxNzc4NDEyODE0fQ.QPhJ9vJ40nbuN7ef2XaKNMn2jfk20U-wAFrrnK4_60M",
    "username": "customer",
    "email": "customer@example.com",
    "phone": "0912345678",
    "role": "ROLE_CUSTOMER"
  }
}
```

**Error Response**:
```json
{
  "success": false,
  "message": "Tên người dùng hoặc mật khẩu không đúng",
  "data": null
}
```

---

### 3. Get Current User Info
**Endpoint**: `GET /api/auth/me`
**Authentication**: Required (JWT Token)
**Role Required**: CUSTOMER, ADMIN

#### Input
**Headers**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjdXN0b21lciIsImlhdCI6MTc3ODMyNjQxNCwiZXhwIjoxNzc4NDEyODE0fQ.QPhJ9vJ40nbuN7ef2XaKNMn2jfk20U-wAFrrnK4_60M
```

No body required.

#### Output (Response)
**Status**: 200 OK / 401 Unauthorized

**Success Response**:
```json
{
  "success": true,
  "message": "Thông tin người dùng",
  "data": {
    "id": 1,
    "username": "customer",
    "email": "customer@example.com",
    "phone": "0912345678",
    "role": "ROLE_CUSTOMER",
    "createdAt": "2026-05-10T00:00:00Z",
    "updatedAt": "2026-05-10T00:00:00Z"
  }
}
```

---

## Product APIs

### 1. Get All Products
**Endpoint**: `GET /api/products`
**Authentication**: None
**Role Required**: Public

#### Input (Query Parameters)
```
?page=0&size=10&sort=id,desc
```

**Parameters**:
- `page`: Page number (0-indexed), default: 0
- `size`: Number of items per page, default: 10
- `sort`: Sort field and direction (field,direction)

#### Output (Response)
**Status**: 200 OK

**Success Response**:
```json
{
  "success": true,
  "message": "Danh sách sản phẩm",
  "data": [
    {
      "id": 1,
      "name": "Bơ sáp",
      "description": "Bơ sạch tươi ngon",
      "price": 50000,
      "stock": 20,
      "imageUrl": "https://example.com/bo.jpg",
      "category": "Trái cây",
      "status": "IN_STOCK",
      "createdAt": "2026-05-10T00:00:00Z",
      "updatedAt": "2026-05-10T00:00:00Z"
    }
  ]
}
```

---

### 2. Get Product by ID
**Endpoint**: `GET /api/products/{id}`
**Authentication**: None
**Role Required**: Public

#### Input (Path Parameters)
```
/api/products/1
```

**Parameters**:
- `id`: Product ID (required)

#### Output (Response)
**Status**: 200 OK / 404 Not Found

**Success Response**:
```json
{
  "success": true,
  "message": "Chi tiết sản phẩm",
  "data": {
    "id": 1,
    "name": "Bơ sáp",
    "description": "Bơ sạch tươi ngon",
    "price": 50000,
    "stock": 20,
    "imageUrl": "https://example.com/bo.jpg",
    "category": "Trái cây",
    "status": "IN_STOCK",
    "createdAt": "2026-05-10T00:00:00Z",
    "updatedAt": "2026-05-10T00:00:00Z"
  }
}
```

**Error Response**:
```json
{
  "success": false,
  "message": "Không tìm thấy sản phẩm với ID: 999",
  "data": null
}
```

---

### 3. Search Products by Name
**Endpoint**: `GET /api/products/search`
**Authentication**: None
**Role Required**: Public

#### Input (Query Parameters)
```
?name=bơ&page=0&size=10
```

**Parameters**:
- `name`: Product name to search (required)
- `page`: Page number, default: 0
- `size`: Items per page, default: 10

#### Output (Response)
**Status**: 200 OK

**Success Response**:
```json
{
  "success": true,
  "message": "Kết quả tìm kiếm sản phẩm",
  "data": [
    {
      "id": 1,
      "name": "Bơ sáp",
      "description": "Bơ sạch tươi ngon",
      "price": 50000,
      "stock": 20,
      "imageUrl": "https://example.com/bo.jpg",
      "category": "Trái cây",
      "status": "IN_STOCK"
    }
  ]
}
```

---

### 4. Create Product (Admin Only)
**Endpoint**: `POST /api/products`
**Authentication**: Required (JWT Token)
**Role Required**: ADMIN

#### Input (Request Body)
```json
{
  "name": "Cam tươi",
  "description": "Cam ngọt tươi mỗi ngày",
  "price": 35000,
  "stock": 50,
  "imageUrl": "https://example.com/cam.jpg",
  "category": "Trái cây",
  "status": "IN_STOCK"
}
```

**Input Validation**:
- `name`: Not blank, unique
- `description`: Not blank
- `price`: Greater than 0
- `stock`: Greater than or equal to 0
- `imageUrl`: Valid URL format
- `category`: Not blank
- `status`: Valid enum (IN_STOCK, OUT_OF_STOCK, DISCONTINUED)

#### Output (Response)
**Status**: 201 Created / 400 Bad Request

**Success Response**:
```json
{
  "success": true,
  "message": "Tạo sản phẩm thành công",
  "data": {
    "id": 2,
    "name": "Cam tươi",
    "description": "Cam ngọt tươi mỗi ngày",
    "price": 35000,
    "stock": 50,
    "imageUrl": "https://example.com/cam.jpg",
    "category": "Trái cây",
    "status": "IN_STOCK",
    "createdAt": "2026-05-10T01:00:00Z",
    "updatedAt": "2026-05-10T01:00:00Z"
  }
}
```

---

### 5. Update Product (Admin Only)
**Endpoint**: `PUT /api/products/{id}`
**Authentication**: Required (JWT Token)
**Role Required**: ADMIN

#### Input (Request Body)
```json
{
  "name": "Cam tươi cập nhật",
  "description": "Cam ngọt tươi mỗi ngày",
  "price": 38000,
  "stock": 45,
  "imageUrl": "https://example.com/cam.jpg",
  "category": "Trái cây",
  "status": "IN_STOCK"
}
```

#### Output (Response)
**Status**: 200 OK / 404 Not Found

**Success Response**:
```json
{
  "success": true,
  "message": "Cập nhật sản phẩm thành công",
  "data": {
    "id": 2,
    "name": "Cam tươi cập nhật",
    "description": "Cam ngọt tươi mỗi ngày",
    "price": 38000,
    "stock": 45,
    "imageUrl": "https://example.com/cam.jpg",
    "category": "Trái cây",
    "status": "IN_STOCK",
    "updatedAt": "2026-05-10T01:30:00Z"
  }
}
```

---

### 6. Update Product Status (Admin Only)
**Endpoint**: `PATCH /api/products/{id}/status`
**Authentication**: Required (JWT Token)
**Role Required**: ADMIN

#### Input (Query Parameters)
```
/api/products/2/status?status=OUT_OF_STOCK
```

**Parameters**:
- `status`: New status (IN_STOCK, OUT_OF_STOCK, DISCONTINUED)

#### Output (Response)
**Status**: 200 OK / 404 Not Found

**Success Response**:
```json
{
  "success": true,
  "message": "Cập nhật trạng thái sản phẩm thành công",
  "data": {
    "id": 2,
    "name": "Cam tươi",
    "status": "OUT_OF_STOCK",
    "updatedAt": "2026-05-10T01:45:00Z"
  }
}
```

---

### 7. Delete Product (Admin Only)
**Endpoint**: `DELETE /api/products/{id}`
**Authentication**: Required (JWT Token)
**Role Required**: ADMIN

#### Input (Path Parameters)
```
/api/products/2
```

#### Output (Response)
**Status**: 200 OK / 404 Not Found

**Success Response**:
```json
{
  "success": true,
  "message": "Xóa sản phẩm thành công",
  "data": null
}
```

---

## Category APIs

### 1. Get All Categories
**Endpoint**: `GET /api/categories`
**Authentication**: None
**Role Required**: Public

#### Input
No parameters required.

#### Output (Response)
**Status**: 200 OK

**Success Response**:
```json
{
  "success": true,
  "message": "Danh sách danh mục",
  "data": [
    {
      "id": 1,
      "name": "Trái cây",
      "description": "Các loại trái cây tươi"
    },
    {
      "id": 2,
      "name": "Rau xanh",
      "description": "Các loại rau xanh tươi"
    }
  ]
}
```

---

### 2. Get Category by ID
**Endpoint**: `GET /api/categories/{id}`
**Authentication**: None
**Role Required**: Public

#### Input (Path Parameters)
```
/api/categories/1
```

#### Output (Response)
**Status**: 200 OK / 404 Not Found

**Success Response**:
```json
{
  "success": true,
  "message": "Chi tiết danh mục",
  "data": {
    "id": 1,
    "name": "Trái cây",
    "description": "Các loại trái cây tươi",
    "productCount": 15
  }
}
```

---

## Order APIs

### 1. Create Order
**Endpoint**: `POST /api/orders`
**Authentication**: Required (JWT Token)
**Role Required**: CUSTOMER, ADMIN

#### Input (Request Body)
```json
{
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 2,
      "quantity": 1
    }
  ],
  "deliveryAddress": "123 Nguyễn Huệ, TPHCM",
  "notes": "Giao hàng vào buổi sáng"
}
```

**Input Validation**:
- `items`: Not empty array
  - `productId`: Valid product ID
  - `quantity`: Greater than 0
- `deliveryAddress`: Not blank, 10-200 characters
- `notes`: Optional, max 500 characters

#### Output (Response)
**Status**: 201 Created / 400 Bad Request

**Success Response**:
```json
{
  "success": true,
  "message": "Tạo đơn hàng thành công",
  "data": {
    "id": 1,
    "userId": 1,
    "username": "customer",
    "orderDate": "2026-05-10T02:00:00Z",
    "status": "PENDING",
    "totalAmount": 135000,
    "deliveryAddress": "123 Nguyễn Huệ, TPHCM",
    "notes": "Giao hàng vào buổi sáng",
    "items": [
      {
        "id": 1,
        "productId": 1,
        "productName": "Bơ sáp",
        "quantity": 2,
        "unitPrice": 50000,
        "totalPrice": 100000
      },
      {
        "id": 2,
        "productId": 2,
        "productName": "Cam tươi",
        "quantity": 1,
        "unitPrice": 35000,
        "totalPrice": 35000
      }
    ]
  }
}
```

**Error Response**:
```json
{
  "success": false,
  "message": "Sản phẩm (ID: 1) không đủ tồn kho. Hiện có: 15",
  "data": null
}
```

---

### 2. Get Order by ID
**Endpoint**: `GET /api/orders/{id}`
**Authentication**: Required (JWT Token)
**Role Required**: CUSTOMER, ADMIN

#### Input (Path Parameters)
```
/api/orders/1
```

#### Output (Response)
**Status**: 200 OK / 404 Not Found

**Success Response**:
```json
{
  "success": true,
  "message": "Chi tiết đơn hàng",
  "data": {
    "id": 1,
    "userId": 1,
    "username": "customer",
    "orderDate": "2026-05-10T02:00:00Z",
    "status": "PENDING",
    "totalAmount": 135000,
    "deliveryAddress": "123 Nguyễn Huệ, TPHCM",
    "notes": "Giao hàng vào buổi sáng",
    "items": [...]
  }
}
```

---

### 3. Get My Orders
**Endpoint**: `GET /api/orders/my-orders`
**Authentication**: Required (JWT Token)
**Role Required**: CUSTOMER, ADMIN

#### Input (Query Parameters)
```
?status=PENDING&page=0&size=10
```

**Parameters**:
- `status`: Optional filter (PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED)
- `page`: Page number, default: 0
- `size`: Items per page, default: 10

#### Output (Response)
**Status**: 200 OK

**Success Response**:
```json
{
  "success": true,
  "message": "Danh sách đơn hàng của bạn",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "orderDate": "2026-05-10T02:00:00Z",
      "status": "PENDING",
      "totalAmount": 135000,
      "deliveryAddress": "123 Nguyễn Huệ, TPHCM"
    }
  ]
}
```

---

### 4. Get All Orders (Admin Only)
**Endpoint**: `GET /api/orders`
**Authentication**: Required (JWT Token)
**Role Required**: ADMIN

#### Input (Query Parameters)
```
?status=PENDING&page=0&size=20
```

#### Output (Response)
**Status**: 200 OK

**Success Response**:
```json
{
  "success": true,
  "message": "Danh sách tất cả đơn hàng",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "username": "customer",
      "orderDate": "2026-05-10T02:00:00Z",
      "status": "PENDING",
      "totalAmount": 135000
    }
  ]
}
```

---

### 5. Update Order Status (Admin Only)
**Endpoint**: `PUT /api/orders/{id}/status`
**Authentication**: Required (JWT Token)
**Role Required**: ADMIN

#### Input (Query Parameters)
```
/api/orders/1/status?status=SHIPPED
```

**Parameters**:
- `status`: New status (PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED)

#### Output (Response)
**Status**: 200 OK / 404 Not Found

**Success Response**:
```json
{
  "success": true,
  "message": "Cập nhật trạng thái đơn hàng thành công",
  "data": {
    "id": 1,
    "status": "SHIPPED",
    "statusUpdatedAt": "2026-05-10T03:00:00Z"
  }
}
```

---

### 6. Cancel Order
**Endpoint**: `POST /api/orders/{id}/cancel`
**Authentication**: Required (JWT Token)
**Role Required**: CUSTOMER, ADMIN

#### Input (Path Parameters)
```
/api/orders/1/cancel
```

#### Output (Response)
**Status**: 200 OK / 400 Bad Request (If order already shipped/delivered)

**Success Response**:
```json
{
  "success": true,
  "message": "Hủy đơn hàng thành công",
  "data": {
    "id": 1,
    "status": "CANCELLED",
    "cancelledAt": "2026-05-10T03:30:00Z"
  }
}
```

**Error Response**:
```json
{
  "success": false,
  "message": "Không thể hủy đơn hàng đã được giao",
  "data": null
}
```

---

### 7. Delete Order (Admin Only)
**Endpoint**: `DELETE /api/orders/{id}`
**Authentication**: Required (JWT Token)
**Role Required**: ADMIN

#### Input (Path Parameters)
```
/api/orders/1
```

#### Output (Response)
**Status**: 200 OK / 404 Not Found

**Success Response**:
```json
{
  "success": true,
  "message": "Xóa đơn hàng thành công",
  "data": null
}
```

---

### 8. Get Order Count by Status (Admin Only)
**Endpoint**: `GET /api/orders/statistics/count-by-status`
**Authentication**: Required (JWT Token)
**Role Required**: ADMIN

#### Input (Query Parameters)
```
?status=PENDING
```

#### Output (Response)
**Status**: 200 OK

**Success Response**:
```json
{
  "success": true,
  "message": "Số lượng đơn hàng: PENDING",
  "data": 5
}
```

---

### 9. Get User Order Count (Admin Only)
**Endpoint**: `GET /api/orders/statistics/user-count`
**Authentication**: Required (JWT Token)
**Role Required**: CUSTOMER, ADMIN

#### Input (Query Parameters)
```
?userId=1
```

#### Output (Response)
**Status**: 200 OK

**Success Response**:
```json
{
  "success": true,
  "message": "Số lượng đơn hàng của người dùng",
  "data": 3
}
```

---

## Cart APIs

### 1. Get User's Cart
**Endpoint**: `GET /api/cart`
**Authentication**: Required (JWT Token)
**Role Required**: CUSTOMER, ADMIN

#### Input
**Headers**:
```
Authorization: Bearer {token}
```

#### Output (Response)
**Status**: 200 OK / 404 Not Found

**Success Response**:
```json
{
  "success": true,
  "message": "Giỏ hàng của bạn",
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
    "createdAt": "2026-05-10T00:00:00Z",
    "updatedAt": "2026-05-10T02:00:00Z"
  }
}
```

**Empty Cart Response**:
```json
{
  "success": true,
  "message": "Giỏ hàng của bạn",
  "data": {
    "id": 1,
    "userId": 1,
    "username": "customer",
    "cartItems": [],
    "totalAmount": 0
  }
}
```

---

### 2. Add Item to Cart
**Endpoint**: `POST /api/cart/items`
**Authentication**: Required (JWT Token)
**Role Required**: CUSTOMER, ADMIN

#### Input (Request Body)
```json
{
  "productId": 1,
  "quantity": 2
}
```

**Input Validation**:
- `productId`: Valid product ID (must exist)
- `quantity`: Greater than 0

#### Output (Response)
**Status**: 201 Created / 400 Bad Request / 404 Not Found

**Success Response**:
```json
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
    "totalAmount": 100000
  }
}
```

**Error Response (Stock Insufficient)**:
```json
{
  "success": false,
  "message": "Sản phẩm Bơ sáp không đủ tồn kho. Hiện có: 15",
  "data": null
}
```

**Error Response (Product Not Found)**:
```json
{
  "success": false,
  "message": "Không tìm thấy sản phẩm với ID: 999",
  "data": null
}
```

---

### 3. Update Cart Item Quantity
**Endpoint**: `PUT /api/cart/items/{itemId}`
**Authentication**: Required (JWT Token)
**Role Required**: CUSTOMER, ADMIN

#### Input (Query Parameters)
```
/api/cart/items/1?quantity=3
```

**Parameters**:
- `itemId`: Cart item ID (path parameter)
- `quantity`: New quantity (query parameter, must be >= 1)

#### Output (Response)
**Status**: 200 OK / 400 Bad Request / 404 Not Found

**Success Response**:
```json
{
  "success": true,
  "message": "Cập nhật số lượng giỏ hàng thành công",
  "data": {
    "id": 1,
    "userId": 1,
    "cartItems": [
      {
        "id": 1,
        "productId": 1,
        "productName": "Bơ sáp",
        "quantity": 3,
        "unitPrice": 50000,
        "totalPrice": 150000
      }
    ],
    "totalAmount": 150000
  }
}
```

**Error Response**:
```json
{
  "success": false,
  "message": "Số lượng phải lớn hơn hoặc bằng 1",
  "data": null
}
```

---

### 4. Remove Item from Cart
**Endpoint**: `DELETE /api/cart/items/{itemId}`
**Authentication**: Required (JWT Token)
**Role Required**: CUSTOMER, ADMIN

#### Input (Path Parameters)
```
/api/cart/items/1
```

#### Output (Response)
**Status**: 200 OK / 404 Not Found

**Success Response**:
```json
{
  "success": true,
  "message": "Xóa sản phẩm khỏi giỏ hàng thành công",
  "data": {
    "id": 1,
    "userId": 1,
    "cartItems": [],
    "totalAmount": 0
  }
}
```

---

### 5. Clear Cart
**Endpoint**: `DELETE /api/cart`
**Authentication**: Required (JWT Token)
**Role Required**: CUSTOMER, ADMIN

#### Input
**Headers**:
```
Authorization: Bearer {token}
```

#### Output (Response)
**Status**: 200 OK / 404 Not Found

**Success Response**:
```json
{
  "success": true,
  "message": "Giỏ hàng đã được làm mới",
  "data": {
    "id": 1,
    "userId": 1,
    "username": "customer",
    "cartItems": [],
    "totalAmount": 0
  }
}
```

---

## Error Codes & Status

### HTTP Status Codes Used
- **200 OK** - Successful GET/PUT/DELETE request
- **201 Created** - Successful POST request
- **400 Bad Request** - Validation error or bad input
- **401 Unauthorized** - Missing or invalid JWT token
- **403 Forbidden** - Insufficient permissions for the operation
- **404 Not Found** - Resource not found
- **500 Internal Server Error** - Server error

### Common Error Messages
```json
{
  "success": false,
  "message": "Error message in Vietnamese",
  "data": null
}
```

---

## Authentication Header Format

For all authenticated endpoints, include:
```
Authorization: Bearer {JWT_TOKEN}
```

Example:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjdXN0b21lciIsImlhdCI6MTc3ODMyNjQxNCwiZXhwIjoxNzc4NDEyODE0fQ.QPhJ9vJ40nbuN7ef2XaKNMn2jfk20U-wAFrrnK4_60M
```

---

## Request/Response Format

### Standard Request Format
- **Content-Type**: `application/json`
- **Body**: JSON object

### Standard Response Format
```json
{
  "success": boolean,
  "message": "Descriptive message in Vietnamese",
  "data": {} // Can be object, array, null, or primitive
}
```

---

## Enums Reference

### OrderStatus
- `PENDING` - Đợi xử lý
- `PROCESSING` - Đang xử lý
- `SHIPPED` - Đã gửi hàng
- `DELIVERED` - Đã giao hàng
- `CANCELLED` - Đã hủy

### ProductStatus
- `IN_STOCK` - Còn hàng
- `OUT_OF_STOCK` - Hết hàng
- `DISCONTINUED` - Ngừng bán

### UserRole
- `ROLE_CUSTOMER` - Khách hàng
- `ROLE_ADMIN` - Quản trị viên

---

## Notes

1. All dates are in ISO 8601 format (UTC)
2. All prices are in VND (Vietnamese Dong)
3. Pagination starts from page 0
4. Token expiration is 24 hours (86400000 ms)
5. All endpoints return JSON format
6. Response wrapper `ApiResponse` is used for consistency
