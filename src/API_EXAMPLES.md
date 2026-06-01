# API Examples & Test Cases

## 📚 Ví Dụ Request/Response

### 1️⃣ POST /api/orders - Tạo Đơn Hàng

#### Request
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "orderItems": [
      {
        "productId": 1,
        "quantity": 2
      },
      {
        "productId": 2,
        "quantity": 1
      }
    ],
    "paymentMethod": "CASH",
    "shippingAddress": "123 Đường Trường Chinh, Quận 1, TP HCM",
    "phoneNumber": "0912345678",
    "notes": "Giao vào buổi sáng, không giao chiều"
  }'
```

#### Response (201 Created)
```json
{
  "success": true,
  "message": "Tạo đơn hàng thành công",
  "data": {
    "id": 1,
    "userId": 1,
    "username": "user1",
    "status": "PENDING",
    "paymentMethod": "CASH",
    "totalAmount": 168000.00,
    "shippingAddress": "123 Đường Trường Chinh, Quận 1, TP HCM",
    "phoneNumber": "0912345678",
    "notes": "Giao vào buổi sáng, không giao chiều",
    "orderItems": [
      {
        "id": 1,
        "productId": 1,
        "productName": "Rau hữu cơ",
        "quantity": 2,
        "unitPrice": 45000.00,
        "totalPrice": 90000.00
      },
      {
        "id": 2,
        "productId": 2,
        "productName": "Trái cây tươi",
        "quantity": 1,
        "unitPrice": 78000.00,
        "totalPrice": 78000.00
      }
    ],
    "createdAt": "2026-05-09T10:30:45.123456",
    "updatedAt": "2026-05-09T10:30:45.123456",
    "cancelledAt": null
  }
}
```

---

### 2️⃣ GET /api/orders/{id} - Lấy Chi Tiết Đơn Hàng

#### Request
```bash
curl -X GET http://localhost:8080/api/orders/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

#### Response (200 OK)
```json
{
  "success": true,
  "message": "Chi tiết đơn hàng",
  "data": {
    "id": 1,
    "userId": 1,
    "username": "user1",
    "status": "PENDING",
    "paymentMethod": "CASH",
    "totalAmount": 168000.00,
    "shippingAddress": "123 Đường Trường Chinh, Quận 1, TP HCM",
    "phoneNumber": "0912345678",
    "notes": "Giao vào buổi sáng, không giao chiều",
    "orderItems": [
      {
        "id": 1,
        "productId": 1,
        "productName": "Rau hữu cơ",
        "quantity": 2,
        "unitPrice": 45000.00,
        "totalPrice": 90000.00
      },
      {
        "id": 2,
        "productId": 2,
        "productName": "Trái cây tươi",
        "quantity": 1,
        "unitPrice": 78000.00,
        "totalPrice": 78000.00
      }
    ],
    "createdAt": "2026-05-09T10:30:45.123456",
    "updatedAt": "2026-05-09T10:30:45.123456",
    "cancelledAt": null
  }
}
```

---

### 3️⃣ GET /api/orders/my-orders - Lấy Đơn Hàng Của Tôi

#### Request
```bash
# Lấy tất cả đơn hàng
curl -X GET http://localhost:8080/api/orders/my-orders \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."

# Lấy đơn hàng chờ xác nhận
curl -X GET "http://localhost:8080/api/orders/my-orders?status=PENDING" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."

# Lấy đơn hàng đang giao
curl -X GET "http://localhost:8080/api/orders/my-orders?status=SHIPPING" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

#### Response (200 OK)
```json
{
  "success": true,
  "message": "Danh sách đơn hàng của bạn",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "username": "user1",
      "status": "PENDING",
      "paymentMethod": "CASH",
      "totalAmount": 168000.00,
      "shippingAddress": "123 Đường Trường Chinh, Quận 1, TP HCM",
      "phoneNumber": "0912345678",
      "notes": "Giao vào buổi sáng, không giao chiều",
      "orderItems": [...],
      "createdAt": "2026-05-09T10:30:45.123456",
      "updatedAt": "2026-05-09T10:30:45.123456",
      "cancelledAt": null
    },
    {
      "id": 2,
      "userId": 1,
      "username": "user1",
      "status": "DELIVERED",
      "paymentMethod": "BANK_TRANSFER",
      "totalAmount": 250000.00,
      "shippingAddress": "456 Đường Nguyễn Hữu Cảnh, Quận 2, TP HCM",
      "phoneNumber": "0987654321",
      "notes": null,
      "orderItems": [...],
      "createdAt": "2026-05-05T14:20:00.000000",
      "updatedAt": "2026-05-08T16:45:30.000000",
      "cancelledAt": null
    }
  ]
}
```

---

### 4️⃣ GET /api/orders - Lấy Tất Cả Đơn Hàng (Admin)

#### Request
```bash
# Lấy tất cả đơn hàng
curl -X GET http://localhost:8080/api/orders \
  -H "Authorization: Bearer <admin_token>"

# Lấy đơn hàng chờ xác nhận
curl -X GET "http://localhost:8080/api/orders?status=PENDING" \
  -H "Authorization: Bearer <admin_token>"

# Lấy đơn hàng đã hủy
curl -X GET "http://localhost:8080/api/orders?status=CANCELLED" \
  -H "Authorization: Bearer <admin_token>"
```

#### Response (200 OK)
```json
{
  "success": true,
  "message": "Danh sách tất cả đơn hàng",
  "data": [
    { /* Order 1 */ },
    { /* Order 2 */ },
    { /* Order 3 */ }
  ]
}
```

---

### 5️⃣ PUT /api/orders/{id}/status - Cập Nhật Trạng Thái (Admin)

#### Request
```bash
# Cập nhật thành CONFIRMED
curl -X PUT "http://localhost:8080/api/orders/1/status?status=CONFIRMED" \
  -H "Authorization: Bearer <admin_token>"

# Cập nhật thành SHIPPING
curl -X PUT "http://localhost:8080/api/orders/1/status?status=SHIPPING" \
  -H "Authorization: Bearer <admin_token>"

# Cập nhật thành DELIVERED
curl -X PUT "http://localhost:8080/api/orders/1/status?status=DELIVERED" \
  -H "Authorization: Bearer <admin_token>"
```

#### Response (200 OK)
```json
{
  "success": true,
  "message": "Cập nhật trạng thái đơn hàng thành công",
  "data": {
    "id": 1,
    "userId": 1,
    "username": "user1",
    "status": "CONFIRMED",
    "paymentMethod": "CASH",
    "totalAmount": 168000.00,
    "shippingAddress": "123 Đường Trường Chinh, Quận 1, TP HCM",
    "phoneNumber": "0912345678",
    "notes": "Giao vào buổi sáng, không giao chiều",
    "orderItems": [...],
    "createdAt": "2026-05-09T10:30:45.123456",
    "updatedAt": "2026-05-09T10:45:20.654321",
    "cancelledAt": null
  }
}
```

---

### 6️⃣ POST /api/orders/{id}/cancel - Hủy Đơn Hàng

#### Request
```bash
curl -X POST http://localhost:8080/api/orders/1/cancel \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

#### Response (200 OK)
```json
{
  "success": true,
  "message": "Hủy đơn hàng thành công",
  "data": {
    "id": 1,
    "userId": 1,
    "username": "user1",
    "status": "CANCELLED",
    "paymentMethod": "CASH",
    "totalAmount": 168000.00,
    "shippingAddress": "123 Đường Trường Chinh, Quận 1, TP HCM",
    "phoneNumber": "0912345678",
    "notes": "Giao vào buổi sáng, không giao chiều",
    "orderItems": [...],
    "createdAt": "2026-05-09T10:30:45.123456",
    "updatedAt": "2026-05-09T11:00:15.123456",
    "cancelledAt": "2026-05-09T11:00:15.123456"
  }
}
```

---

### 7️⃣ DELETE /api/orders/{id} - Xóa Đơn Hàng (Admin)

#### Request
```bash
curl -X DELETE http://localhost:8080/api/orders/1 \
  -H "Authorization: Bearer <admin_token>"
```

#### Response (200 OK)
```json
{
  "success": true,
  "message": "Xóa đơn hàng thành công",
  "data": null
}
```

---

## 🔴 Error Responses

### Lỗi 1: Sản Phẩm Không Tồn Tại

#### Request
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "orderItems": [
      {
        "productId": 999,
        "quantity": 1
      }
    ],
    "paymentMethod": "CASH",
    "shippingAddress": "123 Main St",
    "phoneNumber": "0912345678"
  }'
```

#### Response (500)
```json
{
  "success": false,
  "message": "Sản phẩm với ID 999 không tồn tại",
  "data": null
}
```

---

### Lỗi 2: Không Đủ Tồn Kho

#### Request
```bash
# Giả sử productId=1 chỉ có 10 cái trong kho
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "orderItems": [
      {
        "productId": 1,
        "quantity": 50
      }
    ],
    "paymentMethod": "CASH",
    "shippingAddress": "123 Main St",
    "phoneNumber": "0912345678"
  }'
```

#### Response (400)
```json
{
  "success": false,
  "message": "Sản phẩm Rau hữu cơ không đủ số lượng. Hiện có: 10",
  "data": null
}
```

---

### Lỗi 3: Không Thể Hủy Đơn Hàng Đã Giao

#### Request
```bash
# Đơn hàng đang ở trạng thái DELIVERED
curl -X POST http://localhost:8080/api/orders/1/cancel \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

#### Response (400)
```json
{
  "success": false,
  "message": "Chỉ có thể hủy đơn hàng ở trạng thái chờ xác nhận hoặc đã xác nhận",
  "data": null
}
```

---

### Lỗi 4: Không Được Phép (Unauthorized)

#### Request
```bash
# Không có token hoặc token không hợp lệ
curl -X POST http://localhost:8080/api/orders/1/cancel
```

#### Response (401/403)
```json
{
  "success": false,
  "message": "Không được phép truy cập",
  "data": null
}
```

---

### Lỗi 5: Validation Error

#### Request
```bash
# Thiếu field bắt buộc
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "orderItems": [],
    "paymentMethod": "CASH",
    "shippingAddress": "123 Main St"
    # Thiếu phoneNumber
  }'
```

#### Response (400)
```json
{
  "success": false,
  "message": "Số điện thoại là bắt buộc",
  "data": null
}
```

---

## 📊 Statistics API

### Lấy Số Lượng Đơn Hàng Theo Trạng Thái

#### Request
```bash
# Số đơn hàng chờ xác nhận
curl -X GET "http://localhost:8080/api/orders/statistics/count-by-status?status=PENDING" \
  -H "Authorization: Bearer <admin_token>"

# Số đơn hàng đã giao
curl -X GET "http://localhost:8080/api/orders/statistics/count-by-status?status=DELIVERED" \
  -H "Authorization: Bearer <admin_token>"
```

#### Response
```json
{
  "success": true,
  "message": "Số lượng đơn hàng: PENDING",
  "data": 5
}
```

---

### Lấy Số Lượng Đơn Hàng Của Người Dùng

#### Request
```bash
curl -X GET "http://localhost:8080/api/orders/statistics/user-count?userId=1" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

#### Response
```json
{
  "success": true,
  "message": "Số lượng đơn hàng của người dùng",
  "data": 3
}
```

---

## 🔄 Luồng Quy Trình Mua Hàng Hoàn Chỉnh

### Step 1: Tạo Đơn Hàng
```bash
POST /api/orders
Status: 201
Response: Order with status = PENDING
```

### Step 2: Admin Xác Nhận
```bash
PUT /api/orders/1/status?status=CONFIRMED
Status: 200
Response: Order with status = CONFIRMED
```

### Step 3: Admin Cập Nhật Đang Giao
```bash
PUT /api/orders/1/status?status=SHIPPING
Status: 200
Response: Order with status = SHIPPING
```

### Step 4: Admin Cập Nhật Đã Giao
```bash
PUT /api/orders/1/status?status=DELIVERED
Status: 200
Response: Order with status = DELIVERED
```

---

## ⚠️ Các Điều Cần Lưu Ý Khi Testing

1. **Kiểm tra Token JWT**: Đảm bảo token còn hiệu lực
2. **Quyền Truy Cập**: USER chỉ xem được đơn hàng của mình, ADMIN xem được tất cả
3. **Số Lượng Sản Phẩm**: Đặt số lượng nhỏ hơn hoặc bằng stock
4. **Định Dạng Ngày Giờ**: Hệ thống sử dụng ISO 8601 format
5. **Khoảng Trống**: Phone number có thể chứa khoảng trống, hyphens
6. **Trạng Thái**: Chỉ có thể cập nhật từ PENDING/CONFIRMED sang các trạng thái khác
7. **Hủy Đơn Hàng**: Chỉ hủy được từ PENDING/CONFIRMED

---

*Last Updated: 09/05/2026*
