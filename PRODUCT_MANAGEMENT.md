# Hệ Thống Quản Lý Sản Phẩm (Product Management System)

## Tổng Quan
Hệ thống quản lý sản phẩm FreshMart cung cấp các tính năng hoàn chỉnh cho việc quản lý sản phẩm, bao gồm:
- Quản lý thông tin sản phẩm (CRUD)
- Quản lý danh mục sản phẩm
- Hệ thống đánh giá và bình luận sản phẩm
- Thống kê đánh giá sản phẩm

## Kiến Trúc Hệ Thống

### 1. Product Management (Quản Lý Sản Phẩm)

#### Entities
- **Product**: Đại diện cho một sản phẩm
  - `id`: ID sản phẩm (Primary Key)
  - `name`: Tên sản phẩm (bắt buộc)
  - `description`: Mô tả sản phẩm
  - `price`: Giá sản phẩm (bắt buộc)
  - `stock`: Số lượng tồn kho (bắt buộc)
  - `imageUrl`: URL hình ảnh sản phẩm
  - `category`: Danh mục sản phẩm
  - `status`: Trạng thái sản phẩm (IN_STOCK, OUT_OF_STOCK, DISCONTINUED)
  - `createdAt`: Ngày tạo
  - `updatedAt`: Ngày cập nhật

#### API Endpoints

##### Lấy tất cả sản phẩm
```
GET /api/products
Parameters: 
  - search (optional): Từ khóa tìm kiếm

Response: 200 OK
{
  "success": true,
  "message": "Danh sách sản phẩm",
  "data": [
    {
      "id": 1,
      "name": "Táo Fuji",
      "price": 45000,
      "stock": 100,
      ...
    }
  ]
}
```

##### Lấy chi tiết sản phẩm
```
GET /api/products/{id}

Response: 200 OK
{
  "success": true,
  "message": "Chi tiết sản phẩm",
  "data": { ... }
}
```

##### Tạo sản phẩm mới (Admin only)
```
POST /api/products
Authorization: Bearer [token] (ADMIN role)
Content-Type: application/json

Request Body:
{
  "name": "Cà chua tươi",
  "description": "Cà chua tươi ngon",
  "price": 25000,
  "stock": 50,
  "imageUrl": "http://...",
  "category": "Rau quả"
}

Response: 201 CREATED
{
  "success": true,
  "message": "Tạo sản phẩm thành công",
  "data": { ... }
}
```

##### Cập nhật sản phẩm (Admin only)
```
PUT /api/products/{id}
Authorization: Bearer [token] (ADMIN role)
Content-Type: application/json

Request Body: (cấu trúc giống Create)

Response: 200 OK
```

##### Cập nhật trạng thái sản phẩm (Admin only)
```
PATCH /api/products/{id}/status
Authorization: Bearer [token] (ADMIN role)
Content-Type: application/json

Request Body:
{
  "status": "OUT_OF_STOCK"  // IN_STOCK, OUT_OF_STOCK, DISCONTINUED
}

Response: 200 OK
```

##### Xóa sản phẩm (Admin only)
```
DELETE /api/products/{id}
Authorization: Bearer [token] (ADMIN role)

Response: 200 OK
```

---

### 2. Review Management (Quản Lý Đánh Giá)

#### Entities
- **Review**: Đại diện cho một đánh giá sản phẩm
  - `id`: ID đánh giá (Primary Key)
  - `product`: Sản phẩm được đánh giá (Foreign Key)
  - `user`: Người dùng đánh giá (Foreign Key)
  - `rating`: Điểm đánh giá (1-5 sao, bắt buộc)
  - `comment`: Bình luận/nhận xét
  - `createdAt`: Ngày tạo
  - `updatedAt`: Ngày cập nhật

**Constraint**: Mỗi người dùng chỉ có thể đánh giá một sản phẩm một lần

#### API Endpoints

##### Lấy tất cả đánh giá của một sản phẩm
```
GET /api/reviews/product/{productId}

Response: 200 OK
{
  "success": true,
  "message": "Danh sách đánh giá sản phẩm",
  "data": [
    {
      "id": 1,
      "productId": 5,
      "productName": "Táo Fuji",
      "userId": 2,
      "userName": "Nguyễn Văn A",
      "rating": 5,
      "comment": "Sản phẩm rất tốt",
      "createdAt": "2026-05-12T10:30:00",
      "updatedAt": "2026-05-12T10:30:00"
    }
  ]
}
```

##### Lấy thống kê đánh giá sản phẩm
```
GET /api/reviews/product/{productId}/stats

Response: 200 OK
{
  "success": true,
  "message": "Thống kê đánh giá sản phẩm",
  "data": {
    "productId": 5,
    "productName": "Táo Fuji",
    "averageRating": 4.5,
    "totalReviews": 10,
    "reviews": [ ... ]
  }
}
```

##### Lấy tất cả đánh giá của một người dùng
```
GET /api/reviews/user/{userId}

Response: 200 OK
{
  "success": true,
  "message": "Danh sách đánh giá của người dùng",
  "data": [ ... ]
}
```

##### Lấy chi tiết một đánh giá
```
GET /api/reviews/{reviewId}

Response: 200 OK
{
  "success": true,
  "message": "Chi tiết đánh giá",
  "data": { ... }
}
```

##### Tạo đánh giá sản phẩm (User only)
```
POST /api/reviews/product/{productId}
Authorization: Bearer [token] (USER role)
Content-Type: application/json

Request Body:
{
  "rating": 5,
  "comment": "Sản phẩm rất tốt, tươi tắn"
}

Response: 201 CREATED
{
  "success": true,
  "message": "Tạo đánh giá thành công",
  "data": { ... }
}

Error Cases:
- 400 Bad Request: Người dùng đã đánh giá sản phẩm này rồi
- 404 Not Found: Sản phẩm hoặc người dùng không tồn tại
```

##### Cập nhật đánh giá (User only)
```
PUT /api/reviews/{reviewId}
Authorization: Bearer [token] (USER role)
Content-Type: application/json

Request Body:
{
  "rating": 4,
  "comment": "Sản phẩm tốt"
}

Response: 200 OK

Error Cases:
- 403 Forbidden: Bạn không có quyền cập nhật đánh giá này (không phải người tạo)
- 404 Not Found: Đánh giá không tồn tại
```

##### Xóa đánh giá (User only)
```
DELETE /api/reviews/{reviewId}
Authorization: Bearer [token] (USER role)

Response: 200 OK

Error Cases:
- 403 Forbidden: Bạn không có quyền xóa đánh giá này
- 404 Not Found: Đánh giá không tồn tại
```

##### Xóa đánh giá (Admin only)
```
DELETE /api/reviews/{reviewId}/admin
Authorization: Bearer [token] (ADMIN role)

Response: 200 OK
```

---

## DTOs (Data Transfer Objects)

### ProductRequest
```java
{
  "name": "Tên sản phẩm",
  "description": "Mô tả",
  "price": 25000,
  "stock": 100,
  "imageUrl": "URL",
  "category": "Danh mục"
}
```

### ProductResponse
```java
{
  "id": 1,
  "name": "Tên sản phẩm",
  "description": "Mô tả",
  "price": 25000,
  "stock": 100,
  "imageUrl": "URL",
  "category": "Danh mục",
  "status": "IN_STOCK",
  "createdAt": "...",
  "updatedAt": "..."
}
```

### ReviewRequest
```java
{
  "rating": 5,           // 1-5 (bắt buộc)
  "comment": "Bình luận" // Không bắt buộc
}
```

### ReviewResponse
```java
{
  "id": 1,
  "productId": 5,
  "productName": "Táo Fuji",
  "userId": 2,
  "userName": "Nguyễn Văn A",
  "rating": 5,
  "comment": "Bình luận",
  "createdAt": "...",
  "updatedAt": "..."
}
```

### ProductReviewStatsResponse
```java
{
  "productId": 5,
  "productName": "Táo Fuji",
  "averageRating": 4.5,
  "totalReviews": 10,
  "reviews": [...]
}
```

---

## Services

### ProductService
- `getAllProducts(search)`: Lấy danh sách sản phẩm (có tìm kiếm)
- `getProductById(id)`: Lấy chi tiết sản phẩm
- `createProduct(request)`: Tạo sản phẩm mới
- `updateProduct(id, request)`: Cập nhật sản phẩm
- `updateProductStatus(id, status)`: Cập nhật trạng thái
- `deleteProduct(id)`: Xóa sản phẩm

### ReviewService
- `createReview(productId, userId, request)`: Tạo đánh giá
- `updateReview(reviewId, userId, request)`: Cập nhật đánh giá
- `getReviewById(reviewId)`: Lấy chi tiết đánh giá
- `getProductReviews(productId)`: Lấy đánh giá của sản phẩm
- `getUserReviews(userId)`: Lấy đánh giá của người dùng
- `getProductReviewStats(productId)`: Lấy thống kê đánh giá
- `deleteReview(reviewId, userId)`: Xóa đánh giá (user)
- `deleteReviewByAdmin(reviewId)`: Xóa đánh giá (admin)

---

## Repositories

### ProductRepository
- `findById(id)`: Tìm sản phẩm theo ID
- `findAll()`: Lấy tất cả sản phẩm
- `findByNameContainingIgnoreCase(search)`: Tìm sản phẩm theo tên
- `save(product)`: Lưu sản phẩm
- `deleteById(id)`: Xóa sản phẩm

### ReviewRepository
- `findByProductIdOrderByCreatedAtDesc(productId)`: Lấy đánh giá của sản phẩm
- `findByUserIdOrderByCreatedAtDesc(userId)`: Lấy đánh giá của người dùng
- `findByProductIdAndUserId(productId, userId)`: Kiểm tra đánh giá của người dùng
- `getAverageRatingByProductId(productId)`: Tính điểm trung bình
- `getReviewCountByProductId(productId)`: Đếm số đánh giá
- `deleteByProductIdAndUserId(productId, userId)`: Xóa đánh giá

---

## Authorization

### Public Endpoints (Không cần token)
- `GET /api/products`
- `GET /api/products/{id}`
- `GET /api/reviews/product/{productId}`
- `GET /api/reviews/product/{productId}/stats`
- `GET /api/reviews/{reviewId}`

### User Endpoints (Cần USER role)
- `POST /api/reviews/product/{productId}`: Tạo đánh giá
- `PUT /api/reviews/{reviewId}`: Cập nhật đánh giá
- `DELETE /api/reviews/{reviewId}`: Xóa đánh giá
- `GET /api/reviews/user/{userId}`: Xem đánh giá của người dùng

### Admin Endpoints (Cần ADMIN role)
- `POST /api/products`: Tạo sản phẩm
- `PUT /api/products/{id}`: Cập nhật sản phẩm
- `PATCH /api/products/{id}/status`: Cập nhật trạng thái
- `DELETE /api/products/{id}`: Xóa sản phẩm
- `DELETE /api/reviews/{reviewId}/admin`: Xóa đánh giá

---

## Xử Lý Lỗi

Tất cả endpoints trả về response có cấu trúc thống nhất:

### Success Response
```json
{
  "success": true,
  "message": "Thông báo thành công",
  "data": { ... }
}
```

### Error Response
```json
{
  "success": false,
  "message": "Thông báo lỗi",
  "data": null
}
```

### HTTP Status Codes
- `200 OK`: Thành công
- `201 CREATED`: Tạo thành công
- `400 Bad Request`: Dữ liệu không hợp lệ
- `403 Forbidden`: Không có quyền truy cập
- `404 Not Found`: Tài nguyên không tìm thấy
- `500 Internal Server Error`: Lỗi server

---

## Lưu Ý Quan Trọng

1. **Validation**: Tất cả DTOs sử dụng Jakarta Validation
2. **Security**: Endpoints được bảo vệ bằng Spring Security
3. **Transaction**: Các thao tác CRUD sử dụng @Transactional
4. **Unique Constraint**: Mỗi người dùng chỉ có thể đánh giá một sản phẩm một lần
5. **Current User**: ReviewController hiện có TODO để lấy User ID từ security context

---

## Cài Đặt & Triển Khai

### Database Migration
```sql
-- Products table already exists
-- Create Reviews table
CREATE TABLE reviews (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  product_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  rating INT NOT NULL,
  comment TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY unique_product_user (product_id, user_id),
  FOREIGN KEY (product_id) REFERENCES products(id),
  FOREIGN KEY (user_id) REFERENCES users(id),
  INDEX idx_product_id (product_id),
  INDEX idx_user_id (user_id),
  INDEX idx_created_at (created_at)
);
```

### Maven Dependencies (Đã có trong pom.xml)
- Spring Data JPA
- Spring Security
- Lombok
- Jakarta Validation

---

## Ví Dụ Sử Dụng

### Lấy thống kê đánh giá sản phẩm
```bash
curl -X GET "http://localhost:8080/api/reviews/product/1/stats"
```

### Tạo đánh giá sản phẩm
```bash
curl -X POST "http://localhost:8080/api/reviews/product/1" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "rating": 5,
    "comment": "Sản phẩm rất tốt"
  }'
```

### Cập nhật đánh giá
```bash
curl -X PUT "http://localhost:8080/api/reviews/1" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "rating": 4,
    "comment": "Cập nhật lại"
  }'
```

---

## Tính Năng Nâng Cao (Có thể mở rộng)
1. Hỗ trợ ảnh cho đánh giá (Review images)
2. Phản hồi từ người bán (Seller reply)
3. Đánh giá hữu ích (Helpful votes)
4. Bộ lọc đánh giá (Filter by rating)
5. Sắp xếp đánh giá (Sort by date, rating, helpful)
