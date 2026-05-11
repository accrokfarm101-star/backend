# Order Management System - Implementation Notes

## ✅ Đã Hoàn Thành

- ✅ Entity Order với tất cả các field cần thiết
- ✅ Entity OrderItem với tính năng tính toán giá
- ✅ OrderStatus enum (5 trạng thái)
- ✅ PaymentMethod enum (3 phương thức)
- ✅ DTO cho Request/Response
- ✅ OrderRepository với các query tùy chỉnh
- ✅ OrderService interface
- ✅ OrderServiceImpl với logic đầy đủ
- ✅ OrderController với 9 endpoints
- ✅ Xác thực người dùng (Security)
- ✅ Kiểm tra tồn kho tự động
- ✅ Quản lý tồn kho khi tạo/hủy đơn hàng
- ✅ Tính toán tổng tiền tự động
- ✅ Hỗ trợ các trạng thái đơn hàng

## 🔴 Cần Cải Thiện

### 1. **Lấy UserId từ Principal (CẦN CẬP NHẬT NGAY)**

**Hiện tại:** Tất cả endpoint đều sử dụng `userId = 1L` (hardcode)

**Cần thay đổi:**
```java
// Trong OrderController.java
@PostMapping
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
        @Valid @RequestBody OrderRequest orderRequest,
        Principal principal) {
    try {
        // TODO: Implement this
        Long userId = getUserIdFromPrincipal(principal);
        OrderResponse response = orderService.createOrder(orderRequest, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Tạo đơn hàng thành công"));
    } catch (IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(e.getMessage()));
    }
}

// Helper method to get userId from Principal
private Long getUserIdFromPrincipal(Principal principal) {
    String username = principal.getName();
    User user = userService.getUserByUsername(username);
    return user.getId();
}
```

**Hoặc sử dụng SecurityContextHolder:**
```java
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

private Long getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    User user = userService.getUserByUsername(username);
    return user.getId();
}
```

### 2. **Thêm Method trong UserService**

**File:** `src/main/java/com/freshmart/service/UserService.java`

```java
User getUserByUsername(String username);
```

**Implementation:** Thêm vào `UserServiceImpl`
```java
@Override
public User getUserByUsername(String username) {
    return userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Người dùng", username));
}
```

### 3. **Thêm Method trong UserRepository**

**File:** `src/main/java/com/freshmart/repository/UserRepository.java`

```java
Optional<User> findByUsername(String username);
```

### 4. **Cảnh Báo Path Mapping Conflict**

Có xung đột giữa hai endpoint:
- `GET /api/orders` - Lấy tất cả đơn hàng
- `GET /api/orders/{id}` - Lấy chi tiết đơn hàng

**Giải Pháp:** Spring sẽ tự động xử lý đúng dựa trên path, nhưng để an toàn hơn, có thể đổi:
```java
// Cách 1: Đổi GET /api/orders/my-orders thành GET /api/orders/user/my-orders
@GetMapping("/user/my-orders")

// Cách 2: Đổi thứ tự trong controller
// Đặt endpoint chi tiết ({id}) ở cuối cùng
```

## 📦 Cấu Trúc File Đã Tạo

```
src/main/java/com/freshmart/
├── controller/
│   └── OrderController.java                 (✅ 9 endpoints)
├── service/
│   └── OrderService.java                    (✅ Interface)
├── serviceImpl/
│   └── OrderServiceImpl.java                 (✅ Implementation)
├── repository/
│   └── OrderRepository.java                 (✅ 8 queries)
├── model/
│   ├── entity/
│   │   ├── Order.java                       (✅ Đơn hàng)
│   │   └── OrderItem.java                   (✅ Chi tiết)
│   ├── dto/
│   │   ├── request/
│   │   │   ├── OrderRequest.java            (✅ Tạo đơn hàng)
│   │   │   └── OrderItemRequest.java        (✅ Chi tiết yêu cầu)
│   │   └── response/
│   │       ├── OrderResponse.java           (✅ Phản hồi đơn hàng)
│   │       └── OrderItemResponse.java       (✅ Phản hồi chi tiết)
│   └── enums/
│       ├── OrderStatus.java                 (✅ Trạng thái)
│       └── PaymentMethod.java               (✅ Thanh toán)
└── ORDER_MANAGEMENT.md                      (✅ Tài liệu)
```

## 🧪 Test Cases Khuyến Nghị

### 1. **Tạo Đơn Hàng Thành Công**
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "orderItems": [{"productId": 1, "quantity": 2}],
    "paymentMethod": "CASH",
    "shippingAddress": "123 Main St",
    "phoneNumber": "0912345678"
  }'
```

### 2. **Tạo Đơn Hàng - Không Đủ Tồn Kho**
```bash
# Sẽ trả về lỗi nếu quantity > stock
```

### 3. **Lấy Danh Sách Đơn Hàng**
```bash
curl -X GET "http://localhost:8080/api/orders/my-orders?status=PENDING" \
  -H "Authorization: Bearer <token>"
```

### 4. **Cập Nhật Trạng Thái (Admin)**
```bash
curl -X PUT "http://localhost:8080/api/orders/1/status?status=SHIPPING" \
  -H "Authorization: Bearer <admin_token>"
```

### 5. **Hủy Đơn Hàng**
```bash
curl -X POST http://localhost:8080/api/orders/1/cancel \
  -H "Authorization: Bearer <token>"
```

## 📋 Validation Rules

### OrderRequest
- `orderItems`: Không được để trống, ít nhất 1 item
- `paymentMethod`: Bắt buộc
- `shippingAddress`: Bắt buộc
- `phoneNumber`: Bắt buộc

### OrderItemRequest
- `productId`: Bắt buộc, phải tồn tại
- `quantity`: Bắt buộc, phải > 0

### Dữ Liệu Tự Động
- `status`: Mặc định = PENDING
- `totalAmount`: Tính toán tự động
- `createdAt`, `updatedAt`: Tự động gán timestamp
- `cancelledAt`: Chỉ được set khi hủy

## 🔍 Database Schema (Tự Động Tạo Bởi Hibernate)

```sql
-- Order Table
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(50) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    shipping_address VARCHAR(255),
    phone_number VARCHAR(20),
    notes TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- OrderItem Table
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);
```

## 🚨 Các Vấn Đề Cần Lưu Ý

1. **Race Condition**: Nếu 2 request tạo đơn hàng cùng lúc với sản phẩm cuối cùng, có thể gây vấn đề. Khuyến nghị:
   - Sử dụng `@Transactional` (đã có)
   - Thêm locking: `findByIdWithLock(id)`

2. **Số Lượng Âm**: Nếu có bug khác cập nhật sản phẩm, stock có thể âm. Thêm:
   ```java
   @Column(nullable = false)
   @Min(0)
   private Integer stock;
   ```

3. **Soft Delete**: Hiện tại khi xóa order, dữ liệu bị mất. Nên:
   - Thêm `deletedAt` field
   - Thêm lọc `WHERE deleted_at IS NULL` trong queries

4. **Audit Trail**: Nên ghi lại ai đã cập nhật trạng thái:
   - Thêm `updatedBy` field
   - Ghi lại thay đổi trạng thái trong bảng `order_status_history`

## ✨ Phát Triển Tiếp Theo

- [ ] Email notification khi đơn hàng được cập nhật
- [ ] SMS notification
- [ ] Push notification (Firebase)
- [ ] Webhook cho 3rd party services
- [ ] Export đơn hàng thành PDF
- [ ] In đơn hàng
- [ ] QR code tracking
- [ ] Hỗ trợ chia nhỏ đơn hàng
- [ ] Hoàn lại hàng/Đổi hàng
- [ ] Review sản phẩm sau khi nhận hàng

---

**Ngày tạo:** 09/05/2026
