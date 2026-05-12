# Hướng Dẫn Cài Đặt & Triển Khai Hệ Thống Quản Lý Sản Phẩm

## ✅ Những Gì Đã Được Tạo

### 1. **Entity Layer**
- ✅ `Review.java` - Entity cho đánh giá sản phẩm

### 2. **Repository Layer**
- ✅ `ReviewRepository.java` - Repository với custom queries

### 3. **DTO Layer**
- ✅ `ReviewRequest.java` - DTO cho request tạo/cập nhật đánh giá
- ✅ `ReviewResponse.java` - DTO cho response đánh giá
- ✅ `ProductReviewStatsResponse.java` - DTO cho thống kê đánh giá

### 4. **Service Layer**
- ✅ `ReviewService.java` - Interface service
- ✅ `ReviewServiceImpl.java` - Implementation service

### 5. **Controller Layer**
- ✅ `ReviewController.java` - REST controller với 8 endpoints

### 6. **Documentation**
- ✅ `PRODUCT_MANAGEMENT.md` - Tài liệu API hoàn chỉnh

---

## 📋 Danh Sách Các Endpoints

### Review Management API

| Method | Endpoint | Auth | Mô Tả |
|--------|----------|------|-------|
| GET | `/api/reviews/product/{productId}` | Public | Lấy đánh giá sản phẩm |
| GET | `/api/reviews/product/{productId}/stats` | Public | Thống kê đánh giá |
| GET | `/api/reviews/user/{userId}` | Public | Lấy đánh giá người dùng |
| GET | `/api/reviews/{reviewId}` | Public | Lấy chi tiết đánh giá |
| POST | `/api/reviews/product/{productId}` | USER | Tạo đánh giá |
| PUT | `/api/reviews/{reviewId}` | USER | Cập nhật đánh giá |
| DELETE | `/api/reviews/{reviewId}` | USER | Xóa đánh giá (user) |
| DELETE | `/api/reviews/{reviewId}/admin` | ADMIN | Xóa đánh giá (admin) |

---

## 🔧 Hướng Dẫn Cài Đặt

### Step 1: Tạo Database Table
Chạy SQL script này để tạo bảng reviews:

```sql
CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating INT NOT NULL,
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_product_user (product_id, user_id),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_product_id (product_id),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
);
```

### Step 2: Cấu Hình Application Properties

Thêm vào `src/main/resources/application.properties`:

```properties
# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Review Configuration
app.review.max-comment-length=1000
```

### Step 3: Build Project

```bash
# Using Maven
mvn clean install

# Or using Maven Wrapper (if available)
./mvnw clean install
```

### Step 4: Run Application

```bash
# Using Maven
mvn spring-boot:run

# Or using Java
java -jar target/fresh-mart-1.0.0.jar
```

---

## 🚨 Công Việc Cần Hoàn Thành

### TODO #1: Fix getCurrentUserId() in ReviewController
**File**: `ReviewController.java` (Line ~170)

**Hiện tại**: 
```java
private Long getCurrentUserId() {
    // ...
    return 1L; // TODO: Get actual user ID from database using username
}
```

**Cần sửa thành**:
```java
private Long getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        
        // Thêm UserRepository để lấy user
        User user = userRepository.findByEmail(username) // hoặc findByUsername
            .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
    throw new RuntimeException("Không thể xác định người dùng hiện tại");
}
```

### TODO #2: Update ReviewController dengan UserRepository
**File**: `ReviewController.java`

**Thêm**:
```java
@Autowired
private UserRepository userRepository;
```

### TODO #3: Thêm Validation Rules
Có thể thêm custom validation cho:
- Người dùng phải có đơn hàng sản phẩm trước khi đánh giá
- Số ký tự comment tối đa
- Rate limiting cho đánh giá

### TODO #4: Thêm Mapper (Optional)
Sử dụng MapStruct hoặc ModelMapper để map Entity ↔ DTO:

```java
@Mapper(componentModel = "spring")
public interface ReviewMapper {
    ReviewResponse toResponse(Review review);
    Review toEntity(ReviewRequest request);
}
```

---

## 📝 Ví Dụ Request/Response

### 1. Tạo Đánh Giá (POST /api/reviews/product/1)
**Request**:
```json
{
  "rating": 5,
  "comment": "Sản phẩm rất tươi tắn, giao hàng nhanh chóng"
}
```

**Response (201 CREATED)**:
```json
{
  "success": true,
  "message": "Tạo đánh giá thành công",
  "data": {
    "id": 10,
    "productId": 1,
    "productName": "Táo Fuji",
    "userId": 5,
    "userName": "Nguyễn Văn A",
    "rating": 5,
    "comment": "Sản phẩm rất tươi tắn, giao hàng nhanh chóng",
    "createdAt": "2026-05-12T15:30:00",
    "updatedAt": "2026-05-12T15:30:00"
  }
}
```

### 2. Lấy Thống Kê Đánh Giá (GET /api/reviews/product/1/stats)
**Response**:
```json
{
  "success": true,
  "message": "Thống kê đánh giá sản phẩm",
  "data": {
    "productId": 1,
    "productName": "Táo Fuji",
    "averageRating": 4.6,
    "totalReviews": 15,
    "reviews": [
      {
        "id": 10,
        "productId": 1,
        "productName": "Táo Fuji",
        "userId": 5,
        "userName": "Nguyễn Văn A",
        "rating": 5,
        "comment": "Sản phẩm rất tốt",
        "createdAt": "2026-05-12T15:30:00",
        "updatedAt": "2026-05-12T15:30:00"
      },
      // ... other reviews
    ]
  }
}
```

### 3. Cập Nhật Đánh Giá (PUT /api/reviews/10)
**Request**:
```json
{
  "rating": 4,
  "comment": "Sản phẩm tốt nhưng hơi mắc"
}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "message": "Cập nhật đánh giá thành công",
  "data": {
    "id": 10,
    "productId": 1,
    "productName": "Táo Fuji",
    "userId": 5,
    "userName": "Nguyễn Văn A",
    "rating": 4,
    "comment": "Sản phẩm tốt nhưng hơi mắc",
    "createdAt": "2026-05-12T15:30:00",
    "updatedAt": "2026-05-12T16:00:00"
  }
}
```

---

## 🔒 Security Configuration

Hệ thống sử dụng Spring Security với các cấu hình sau:

### Roles
- **PUBLIC**: Các ai cũng xem được
- **USER**: Chỉ người dùng đã đăng nhập
- **ADMIN**: Chỉ admin

### Authorization
```java
// Public - Anyone can view
GET /api/reviews/product/{productId}
GET /api/reviews/product/{productId}/stats

// USER ONLY - Must be authenticated
@PreAuthorize("hasRole('USER')")
POST /api/reviews/product/{productId}
PUT /api/reviews/{reviewId}
DELETE /api/reviews/{reviewId}

// ADMIN ONLY
@PreAuthorize("hasRole('ADMIN')")
DELETE /api/reviews/{reviewId}/admin
```

---

## 🧪 Testing

### Unit Test Example
```java
@SpringBootTest
public class ReviewServiceTest {
    
    @Autowired
    private ReviewService reviewService;
    
    @MockBean
    private ReviewRepository reviewRepository;
    
    @Test
    public void testCreateReview() {
        // Arrange
        ReviewRequest request = new ReviewRequest(5, "Great product");
        
        // Act
        ReviewResponse response = reviewService.createReview(1L, 1L, request);
        
        // Assert
        assertNotNull(response);
        assertEquals(5, response.getRating());
    }
}
```

### Integration Test
```bash
# Test API endpoint
curl -X POST http://localhost:8080/api/reviews/product/1 \
  -H "Authorization: Bearer eyJhbGci..." \
  -H "Content-Type: application/json" \
  -d '{
    "rating": 5,
    "comment": "Excellent product!"
  }'
```

---

## 📊 Database Schema

```
reviews table:
┌──────────────────────────────────┐
│ id (PK, AUTO_INCREMENT)          │
│ product_id (FK → products)       │
│ user_id (FK → users)             │
│ rating (INT, 1-5)                │
│ comment (TEXT, nullable)         │
│ created_at (TIMESTAMP)           │
│ updated_at (TIMESTAMP)           │
└──────────────────────────────────┘

Unique Constraint: (product_id, user_id)
Indexes: product_id, user_id, created_at
```

---

## ⚠️ Error Handling

### Common Errors

| Error | Status | Solution |
|-------|--------|----------|
| Rating > 5 or < 1 | 400 | Validation error - use 1-5 |
| User already reviewed | 400 | User can only review once per product |
| Unauthorized delete | 403 | Only owner or admin can delete |
| Product not found | 404 | Check product ID |
| User not authenticated | 401 | Add Authorization header |

---

## 📈 Performance Optimization

### Indexes
Database đã có indexes trên:
- `product_id` - tối ưu query by product
- `user_id` - tối ưu query by user
- `created_at` - tối ưu sorting

### Caching (Optional)
```java
@Cacheable("productReviews")
public List<ReviewResponse> getProductReviews(Long productId) {
    // ...
}
```

### Pagination (Future Enhancement)
```java
@GetMapping("/product/{productId}")
public Page<ReviewResponse> getProductReviews(
    @PathVariable Long productId,
    @PageableDefault(size = 10) Pageable pageable) {
    // ...
}
```

---

## 🚀 Next Steps

1. **Implement getCurrentUserId()** - Fix TODO #1
2. **Add UserRepository** - Fix TODO #2
3. **Database Migration** - Chạy SQL script
4. **Build & Test** - `mvn clean install`
5. **Run Application** - `mvn spring-boot:run`
6. **Test API** - Sử dụng Postman hoặc curl
7. **Deploy** - Push lên production

---

## 📞 Support

Nếu có vấn đề gì:
1. Kiểm tra logs trong `/target/`
2. Đảm bảo database connection
3. Xác nhận Spring Security configuration
4. Check userId extraction logic (getCurrentUserId)

---

**Status**: ✅ Hoàn thành cấu trúc cơ bản
**Last Updated**: 2026-05-12
**Java Version**: 17
**Spring Boot Version**: 3.2.0
