# 🎯 API Testing Examples - Review Management System

## 📚 Nội Dung
- [1. Lấy Đánh Giá Sản Phẩm](#1-lấy-đánh-giá-sản-phẩm)
- [2. Tạo Đánh Giá Mới](#2-tạo-đánh-giá-mới)
- [3. Cập Nhật Đánh Giá](#3-cập-nhật-đánh-giá)
- [4. Xóa Đánh Giá](#4-xóa-đánh-giá)
- [5. Lấy Thống Kê](#5-lấy-thống-kê)
- [6. Lấy Đánh Giá Của Người Dùng](#6-lấy-đánh-giá-của-người-dùng)

---

## 1. Lấy Đánh Giá Sản Phẩm

### Request (Public - Không cần token)
```bash
GET /api/reviews/product/1 HTTP/1.1
Host: localhost:8080
```

### curl Command
```bash
curl -X GET "http://localhost:8080/api/reviews/product/1"
```

### Response ✅ 200 OK
```json
{
  "success": true,
  "message": "Danh sách đánh giá sản phẩm",
  "data": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Táo Fuji",
      "userId": 2,
      "userName": "nguyenvana",
      "rating": 5,
      "comment": "Sản phẩm rất tươi tắn, giao hàng nhanh chóng",
      "createdAt": "2026-05-12T10:30:00",
      "updatedAt": "2026-05-12T10:30:00"
    },
    {
      "id": 2,
      "productId": 1,
      "productName": "Táo Fuji",
      "userId": 3,
      "userName": "nguyenvanb",
      "rating": 4,
      "comment": "Tốt nhưng hơi mắc",
      "createdAt": "2026-05-12T09:15:00",
      "updatedAt": "2026-05-12T09:15:00"
    }
  ]
}
```

### Response ❌ 404 Not Found (Product không tồn tại)
```json
{
  "success": false,
  "message": "Sản phẩm không tồn tại",
  "data": null
}
```

---

## 2. Tạo Đánh Giá Mới

### Request (USER Role Required)
```bash
POST /api/reviews/product/1 HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "rating": 5,
  "comment": "Sản phẩm rất tốt, tươi tắn lắm!"
}
```

### curl Command
```bash
curl -X POST "http://localhost:8080/api/reviews/product/1" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "rating": 5,
    "comment": "Sản phẩm rất tốt!"
  }'
```

### Response ✅ 201 CREATED
```json
{
  "success": true,
  "message": "Tạo đánh giá thành công",
  "data": {
    "id": 3,
    "productId": 1,
    "productName": "Táo Fuji",
    "userId": 5,
    "userName": "nguyenvanc",
    "rating": 5,
    "comment": "Sản phẩm rất tốt!",
    "createdAt": "2026-05-12T15:45:30",
    "updatedAt": "2026-05-12T15:45:30"
  }
}
```

### Response ❌ 400 Bad Request (User đã review)
```json
{
  "success": false,
  "message": "Bạn đã đánh giá sản phẩm này rồi",
  "data": null
}
```

### Response ❌ 400 Bad Request (Rating không hợp lệ)
```json
{
  "success": false,
  "message": "Rating phải từ 1 đến 5",
  "data": null
}
```

### Response ❌ 401 Unauthorized (Không có token)
```json
{
  "timestamp": "2026-05-12T15:45:00.000+07:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource"
}
```

---

## 3. Cập Nhật Đánh Giá

### Request (Review Owner Required)
```bash
PUT /api/reviews/3 HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "rating": 4,
  "comment": "Sản phẩm tốt nhưng hơi mắc"
}
```

### curl Command
```bash
curl -X PUT "http://localhost:8080/api/reviews/3" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "rating": 4,
    "comment": "Sản phẩm tốt nhưng hơi mắc"
  }'
```

### Response ✅ 200 OK
```json
{
  "success": true,
  "message": "Cập nhật đánh giá thành công",
  "data": {
    "id": 3,
    "productId": 1,
    "productName": "Táo Fuji",
    "userId": 5,
    "userName": "nguyenvanc",
    "rating": 4,
    "comment": "Sản phẩm tốt nhưng hơi mắc",
    "createdAt": "2026-05-12T15:45:30",
    "updatedAt": "2026-05-12T16:00:00"
  }
}
```

### Response ❌ 403 Forbidden (Không phải owner)
```json
{
  "success": false,
  "message": "Bạn không có quyền cập nhật đánh giá này",
  "data": null
}
```

### Response ❌ 404 Not Found (Review không tồn tại)
```json
{
  "success": false,
  "message": "Đánh giá không tồn tại",
  "data": null
}
```

---

## 4. Xóa Đánh Giá

### 4.1 User Xóa Đánh Giá Của Mình

#### Request
```bash
DELETE /api/reviews/3 HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### curl Command
```bash
curl -X DELETE "http://localhost:8080/api/reviews/3" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Response ✅ 200 OK
```json
{
  "success": true,
  "message": "Xóa đánh giá thành công",
  "data": null
}
```

#### Response ❌ 403 Forbidden (Không phải owner)
```json
{
  "success": false,
  "message": "Bạn không có quyền xóa đánh giá này",
  "data": null
}
```

### 4.2 Admin Xóa Đánh Giá

#### Request (ADMIN Role)
```bash
DELETE /api/reviews/3/admin HTTP/1.1
Host: localhost:8080
Authorization: Bearer ADMIN_TOKEN
```

#### curl Command
```bash
curl -X DELETE "http://localhost:8080/api/reviews/3/admin" \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

#### Response ✅ 200 OK
```json
{
  "success": true,
  "message": "Xóa đánh giá thành công",
  "data": null
}
```

#### Response ❌ 403 Forbidden (User không phải Admin)
```json
{
  "success": false,
  "message": "Access Denied",
  "data": null
}
```

---

## 5. Lấy Thống Kê

### Request (Public)
```bash
GET /api/reviews/product/1/stats HTTP/1.1
Host: localhost:8080
```

### curl Command
```bash
curl -X GET "http://localhost:8080/api/reviews/product/1/stats"
```

### Response ✅ 200 OK
```json
{
  "success": true,
  "message": "Thống kê đánh giá sản phẩm",
  "data": {
    "productId": 1,
    "productName": "Táo Fuji",
    "averageRating": 4.3,
    "totalReviews": 5,
    "reviews": [
      {
        "id": 1,
        "productId": 1,
        "productName": "Táo Fuji",
        "userId": 2,
        "userName": "nguyenvana",
        "rating": 5,
        "comment": "Tươi tắn",
        "createdAt": "2026-05-12T10:30:00",
        "updatedAt": "2026-05-12T10:30:00"
      },
      {
        "id": 2,
        "productId": 1,
        "productName": "Táo Fuji",
        "userId": 3,
        "userName": "nguyenvanb",
        "rating": 4,
        "comment": "Tốt nhưng hơi mắc",
        "createdAt": "2026-05-12T09:15:00",
        "updatedAt": "2026-05-12T09:15:00"
      },
      {
        "id": 3,
        "productId": 1,
        "productName": "Táo Fuji",
        "userId": 4,
        "userName": "nguyenvanc",
        "rating": 5,
        "comment": "Excellent!",
        "createdAt": "2026-05-12T08:00:00",
        "updatedAt": "2026-05-12T08:00:00"
      },
      {
        "id": 4,
        "productId": 1,
        "productName": "Táo Fuji",
        "userId": 5,
        "userName": "nguyenvand",
        "rating": 3,
        "comment": "Bình thường",
        "createdAt": "2026-05-12T07:30:00",
        "updatedAt": "2026-05-12T07:30:00"
      },
      {
        "id": 5,
        "productId": 1,
        "productName": "Táo Fuji",
        "userId": 6,
        "userName": "nguyenvane",
        "rating": 5,
        "comment": "Sản phẩm quá tốt!",
        "createdAt": "2026-05-12T06:15:00",
        "updatedAt": "2026-05-12T06:15:00"
      }
    ]
  }
}
```

### Tính Toán Thống Kê
```
Ratings: [5, 4, 5, 3, 5]
Average: (5 + 4 + 5 + 3 + 5) / 5 = 22 / 5 = 4.4 ≈ 4.3
Total: 5
```

---

## 6. Lấy Đánh Giá Của Người Dùng

### Request (Public)
```bash
GET /api/reviews/user/2 HTTP/1.1
Host: localhost:8080
```

### curl Command
```bash
curl -X GET "http://localhost:8080/api/reviews/user/2"
```

### Response ✅ 200 OK
```json
{
  "success": true,
  "message": "Danh sách đánh giá của người dùng",
  "data": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Táo Fuji",
      "userId": 2,
      "userName": "nguyenvana",
      "rating": 5,
      "comment": "Sản phẩm rất tươi tắn",
      "createdAt": "2026-05-12T10:30:00",
      "updatedAt": "2026-05-12T10:30:00"
    },
    {
      "id": 10,
      "productId": 3,
      "productName": "Cà chua",
      "userId": 2,
      "userName": "nguyenvana",
      "rating": 4,
      "comment": "Tốt",
      "createdAt": "2026-05-10T14:20:00",
      "updatedAt": "2026-05-10T14:20:00"
    },
    {
      "id": 15,
      "productId": 5,
      "productName": "Xà lách",
      "userId": 2,
      "userName": "nguyenvana",
      "rating": 5,
      "comment": "Tươi lắm",
      "createdAt": "2026-05-08T08:45:00",
      "updatedAt": "2026-05-08T08:45:00"
    }
  ]
}
```

### Response ✅ 200 OK (Empty - User chưa review)
```json
{
  "success": true,
  "message": "Danh sách đánh giá của người dùng",
  "data": []
}
```

---

## 🔐 Authentication Setup

### Step 1: Đăng Ký Tài Khoản
```bash
curl -X POST "http://localhost:8080/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "reviewer1",
    "email": "reviewer1@example.com",
    "phone": "0987654321",
    "password": "password123"
  }'
```

### Step 2: Đăng Nhập
```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "reviewer1",
    "password": "password123"
  }'
```

**Response**:
```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "userId": 5,
    "username": "reviewer1",
    "email": "reviewer1@example.com",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "role": "ROLE_CUSTOMER"
  }
}
```

### Step 3: Sử Dụng Token
```bash
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## 📊 Validation Rules

### Rating Validation
```
- Minimum: 1
- Maximum: 5
- Type: Integer
- Required: YES
- Error: "Rating phải từ 1 đến 5"
```

### Comment Validation
```
- Type: String (TEXT)
- Required: NO
- Max Length: Unlimited (TEXT field)
- Supports: Vietnamese characters, Emoji
```

### Unique Constraint
```
TABLE: reviews
CONSTRAINT: UNIQUE(product_id, user_id)
Meaning: One review per user per product
Error: "Bạn đã đánh giá sản phẩm này rồi"
```

---

## 🎬 Complete Workflow Example

```
1. User registers and logs in → Gets JWT token
2. User searches for products → GET /api/products
3. User views reviews → GET /api/reviews/product/{id}
4. User views stats → GET /api/reviews/product/{id}/stats
5. User creates review → POST /api/reviews/product/{id}
   {rating: 5, comment: "Great!"}
6. User sees their review → GET /api/reviews/product/{id}
7. User updates review → PUT /api/reviews/{id}
   {rating: 4, comment: "Good but pricey"}
8. User views their reviews → GET /api/reviews/user/{id}
9. User deletes review → DELETE /api/reviews/{id}
```

---

## 🛠️ Tools for Testing

### Option 1: curl (Command Line)
```bash
curl -X GET "http://localhost:8080/api/reviews/product/1"
```

### Option 2: Postman
1. Import collection
2. Set Authorization tab → Bearer Token → Paste token
3. Send requests

### Option 3: VS Code REST Client
```
GET http://localhost:8080/api/reviews/product/1
Authorization: Bearer YOUR_TOKEN
```

### Option 4: JavaScript fetch
```javascript
fetch('http://localhost:8080/api/reviews/product/1')
  .then(r => r.json())
  .then(d => console.log(d))
```

---

**Created**: 2026-05-12  
**Version**: 1.0  
**Status**: ✅ Ready for API Testing
