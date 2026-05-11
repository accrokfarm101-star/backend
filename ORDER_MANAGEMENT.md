# Hệ Thống Quản Lý Đơn Hàng - Tài Liệu Hướng Dẫn

## 📋 Tổng Quan

Hệ thống quản lý đơn hàng là một phần quan trọng của ứng dụng Fresh Mart, cho phép người dùng tạo đơn hàng, quản lý trạng thái đơn hàng, và cho phép admin quản lý tất cả các đơn hàng trong hệ thống.

## 🏗️ Cấu Trúc Dữ Liệu

### Entity

#### 1. Order (Đơn Hàng)
```
- id: Long (ID đơn hàng, tự tăng)
- user: User (Người dùng tạo đơn hàng)
- status: OrderStatus (Trạng thái đơn hàng)
- paymentMethod: PaymentMethod (Phương thức thanh toán)
- totalAmount: BigDecimal (Tổng tiền)
- shippingAddress: String (Địa chỉ giao hàng)
- phoneNumber: String (Số điện thoại)
- notes: String (Ghi chú)
- orderItems: List<OrderItem> (Danh sách sản phẩm)
- createdAt: LocalDateTime (Ngày tạo)
- updatedAt: LocalDateTime (Ngày cập nhật)
- cancelledAt: LocalDateTime (Ngày hủy, nếu có)
```

#### 2. OrderItem (Chi Tiết Đơn Hàng)
```
- id: Long (ID chi tiết, tự tăng)
- order: Order (Đơn hàng)
- product: Product (Sản phẩm)
- quantity: Integer (Số lượng)
- unitPrice: BigDecimal (Giá đơn vị)
- totalPrice: BigDecimal (Tổng giá = unitPrice * quantity)
```

### Enum

#### 1. OrderStatus (Trạng Thái Đơn Hàng)
- **PENDING**: Chờ xác nhận
- **CONFIRMED**: Đã xác nhận
- **SHIPPING**: Đang giao hàng
- **DELIVERED**: Đã giao thành công
- **CANCELLED**: Đã hủy

#### 2. PaymentMethod (Phương Thức Thanh Toán)
- **CASH**: Tiền mặt khi nhận hàng
- **BANK_TRANSFER**: Chuyển khoản ngân hàng
- **MOMO**: Ví điện tử MoMo

## 📡 API Endpoints

### 1. Tạo Đơn Hàng Mới

**Endpoint:**
```
POST /api/orders
```

**Authentication:** Yêu cầu (USER hoặc ADMIN)

**Request Body:**
```json
{
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
  "shippingAddress": "123 Đường Trường Chinh, TP HCM",
  "phoneNumber": "0912345678",
  "notes": "Giao vào buổi sáng"
}
```

**Response (201 Created):**
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
    "totalAmount": 200000.00,
    "shippingAddress": "123 Đường Trường Chinh, TP HCM",
    "phoneNumber": "0912345678",
    "notes": "Giao vào buổi sáng",
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
    "createdAt": "2026-05-09T10:30:00",
    "updatedAt": "2026-05-09T10:30:00",
    "cancelledAt": null
  }
}
```

### 2. Lấy Chi Tiết Đơn Hàng

**Endpoint:**
```
GET /api/orders/{id}
```

**Authentication:** Yêu cầu (USER hoặc ADMIN)

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Chi tiết đơn hàng",
  "data": { /* Chi tiết đơn hàng */ }
}
```

### 3. Lấy Danh Sách Đơn Hàng Của Người Dùng

**Endpoint:**
```
GET /api/orders/my-orders
GET /api/orders/my-orders?status=PENDING
```

**Authentication:** Yêu cầu (USER hoặc ADMIN)

**Query Parameters:**
- `status` (optional): Lọc theo trạng thái (PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED)

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Danh sách đơn hàng của bạn",
  "data": [ /* Danh sách đơn hàng */ ]
}
```

### 4. Lấy Tất Cả Đơn Hàng (Admin)

**Endpoint:**
```
GET /api/orders
GET /api/orders?status=CONFIRMED
```

**Authentication:** Yêu cầu (ADMIN)

**Query Parameters:**
- `status` (optional): Lọc theo trạng thái

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Danh sách tất cả đơn hàng",
  "data": [ /* Danh sách tất cả đơn hàng */ ]
}
```

### 5. Cập Nhật Trạng Thái Đơn Hàng (Admin)

**Endpoint:**
```
PUT /api/orders/{id}/status?status=SHIPPING
```

**Authentication:** Yêu cầu (ADMIN)

**Query Parameters:**
- `status` (required): Trạng thái mới

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Cập nhật trạng thái đơn hàng thành công",
  "data": { /* Chi tiết đơn hàng đã cập nhật */ }
}
```

### 6. Hủy Đơn Hàng

**Endpoint:**
```
POST /api/orders/{id}/cancel
```

**Authentication:** Yêu cầu (USER hoặc ADMIN)

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Hủy đơn hàng thành công",
  "data": { /* Chi tiết đơn hàng đã hủy */ }
}
```

**Lưu ý:**
- Chỉ có thể hủy đơn hàng ở trạng thái PENDING hoặc CONFIRMED
- Khi hủy, số lượng sản phẩm sẽ được hoàn lại vào kho

### 7. Xóa Đơn Hàng (Admin)

**Endpoint:**
```
DELETE /api/orders/{id}
```

**Authentication:** Yêu cầu (ADMIN)

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Xóa đơn hàng thành công",
  "data": null
}
```

### 8. Lấy Số Lượng Đơn Hàng Theo Trạng Thái (Admin)

**Endpoint:**
```
GET /api/orders/statistics/count-by-status?status=PENDING
```

**Authentication:** Yêu cầu (ADMIN)

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Số lượng đơn hàng: PENDING",
  "data": 5
}
```

### 9. Lấy Số Lượng Đơn Hàng Của Người Dùng

**Endpoint:**
```
GET /api/orders/statistics/user-count?userId=1
```

**Authentication:** Yêu cầu (USER hoặc ADMIN)

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Số lượng đơn hàng của người dùng",
  "data": 3
}
```

## 🔍 Luồng Xử Lý Chính

### Tạo Đơn Hàng
1. Người dùng gửi request tạo đơn hàng với danh sách sản phẩm
2. Hệ thống xác thực người dùng
3. Kiểm tra tồn kho của từng sản phẩm
4. Tính tổng tiền
5. Tạo đơn hàng với trạng thái PENDING
6. Giảm số lượng tồn kho của mỗi sản phẩm
7. Trả về thông tin đơn hàng đã tạo

### Cập Nhật Trạng Thái
1. Admin gửi request cập nhật trạng thái
2. Lấy thông tin đơn hàng từ database
3. Kiểm tra trạng thái hiệu lệ
4. Cập nhật trạng thái mới
5. Lưu vào database
6. Trả về thông tin đơn hàng đã cập nhật

### Hủy Đơn Hàng
1. Người dùng gửi request hủy đơn hàng
2. Kiểm tra xem đơn hàng có ở trạng thái PENDING hoặc CONFIRMED không
3. Hoàn lại số lượng sản phẩm vào kho
4. Cập nhật trạng thái thành CANCELLED
5. Lưu thời gian hủy
6. Trả về thông tin đơn hàng đã hủy

## 🔐 Quyền Truy Cập

| Endpoint | USER | ADMIN | Ghi Chú |
|----------|------|-------|---------|
| POST /api/orders | ✅ | ✅ | Tạo đơn hàng |
| GET /api/orders/{id} | ✅ | ✅ | Xem chi tiết |
| GET /api/orders/my-orders | ✅ | ✅ | Xem đơn hàng của mình |
| GET /api/orders | ❌ | ✅ | Xem tất cả đơn hàng |
| PUT /api/orders/{id}/status | ❌ | ✅ | Cập nhật trạng thái |
| POST /api/orders/{id}/cancel | ✅ | ✅ | Hủy đơn hàng |
| DELETE /api/orders/{id} | ❌ | ✅ | Xóa đơn hàng |
| GET /api/orders/statistics/* | ❌ | ✅ | Thống kê |

## ⚠️ Xử Lý Lỗi

### Lỗi Phổ Biến

1. **Sản phẩm không tồn tại**
   ```json
   {
     "success": false,
     "message": "Sản phẩm với ID 999 không tồn tại",
     "data": null
   }
   ```

2. **Không đủ tồn kho**
   ```json
   {
     "success": false,
     "message": "Sản phẩm Rau hữu cơ không đủ số lượng. Hiện có: 50",
     "data": null
   }
   ```

3. **Không thể hủy đơn hàng**
   ```json
   {
     "success": false,
     "message": "Chỉ có thể hủy đơn hàng ở trạng thái chờ xác nhận hoặc đã xác nhận",
     "data": null
   }
   ```

4. **Không được phép**
   ```json
   {
     "success": false,
     "message": "Không được phép truy cập",
     "data": null
   }
   ```

## 🎯 Tính Năng Nổi Bật

1. **Quản Lý Tồn Kho**: Tự động giảm/hoàn lại số lượng sản phẩm khi tạo/hủy đơn hàng
2. **Tính Toán Giá**: Tự động tính tổng tiền dựa trên giá sản phẩm và số lượng
3. **Trạng Thái Linh Hoạt**: Hỗ trợ 5 trạng thái đơn hàng khác nhau
4. **Nhiều Phương Thức Thanh Toán**: Tiền mặt, chuyển khoản, MoMo
5. **Lịch Sử**: Ghi lại ngày tạo, cập nhật, hủy
6. **Thống Kê**: Có thể lấy số lượng đơn hàng theo trạng thái
7. **Bộ Lọc**: Có thể lọc đơn hàng theo trạng thái

## 📝 Ghi Chú Quan Trọng

> ⚠️ **Lưu ý:** Endpoint `/api/orders` (GET) sử dụng cùng path nhưng có path variable khác. Để tránh xung đột, hãy đảm bảo client gửi request chính xác.

> ⚠️ **TODO:** Hiện tại, hệ thống sử dụng userId = 1 cho tất cả request. Cần cập nhật để lấy userId từ Principal (token JWT).

## 🚀 Hướng Phát Triển Tương Lai

1. **Thanh toán Trực Tuyến**: Tích hợp với cổng thanh toán MoMo, VNPay
2. **Thông Báo**: Gửi email/SMS khi đơn hàng được cập nhật
3. **Đánh Giá Sản Phẩm**: Cho phép người dùng đánh giá sau khi nhận hàng
4. **Hoàn Lại Hàng**: Hỗ trợ quy trình hoàn lại/đổi trả
5. **Báo Cáo**: Thống kê chi tiết doanh thu, số đơn hàng theo thời gian
6. **Khuyến Mãi**: Hỗ trợ mã giảm giá, voucher
7. **Tracking**: Theo dõi vị trí đơn hàng realtime

---

*Tài liệu cập nhật: 09/05/2026*
