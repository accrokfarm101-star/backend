-- =============================================================
-- FILE: src/main/resources/data.sql
-- CHỈ CHỨA LỆNH CHÈN DỮ LIỆU MẪU CHO DỰ ÁN FRESH MART
-- =============================================================

-- 1. Chèn Categories (Danh mục nông sản)
INSERT INTO `categories` (`id`, `name`, `description`, `created_at`, `updated_at`) VALUES
(1, 'Rau Củ Quả', 'Nông sản rau củ hữu cơ sạch', NOW(6), NOW(6)),
(2, 'Trái Cây', 'Trái cây tươi ngon nhập khẩu và nội địa', NOW(6), NOW(6))
ON DUPLICATE KEY UPDATE `name`=`name`;

-- 2. Chèn Users (Tài khoản mẫu: mật khẩu băm của cả 2 tài khoản đều là 'password123')
INSERT INTO `users` (`id`, `username`, `email`, `phone`, `password`, `role`, `created_at`, `updated_at`) VALUES
(1, 'admin', 'admin@freshmart.vn', '0906666666', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_ADMIN', NOW(6), NOW(6)),
(2, 'hungnv', 'hungnv@gmail.com', '0987654321', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_CUSTOMER', NOW(6), NOW(6))
ON DUPLICATE KEY UPDATE `username`=`username`;

-- 3. Chèn Products (Sản phẩm mẫu)
INSERT INTO `products` (`id`, `name`, `description`, `price`, `stock`, `image_url`, `category`, `status`, `created_at`, `updated_at`) VALUES
(1, 'Táo Mỹ Nhập Khẩu', 'Táo đỏ Mỹ giòn ngọt, giàu dinh dưỡng.', 85000.00, 100, 'taomy.png', 'Trái Cây', 'IN_STOCK', NOW(6), NOW(6)),
(2, 'Rau Cải Cúc Hữu Cơ', 'Rau cải sạch trồng tự nhiên không hóa chất.', 15000.00, 50, 'raucai.png', 'Rau Củ Quả', 'IN_STOCK', NOW(6), NOW(6))
ON DUPLICATE KEY UPDATE `name`=`name`;

-- 4. Tạo sẵn giỏ hàng (Cart) cho người dùng khách hàng (id = 2)
INSERT INTO `carts` (`id`, `user_id`, `total_amount`, `created_at`, `updated_at`) VALUES
(1, 2, 100000.00, NOW(6), NOW(6))
ON DUPLICATE KEY UPDATE `user_id`=`user_id`;

-- 5. Thêm đồ vào giỏ hàng mẫu (CartItems)
INSERT INTO `cart_items` (`id`, `cart_id`, `product_id`, `quantity`, `unit_price`, `total_price`) VALUES
(1, 1, 1, 1, 85000.00, 85000.00),
(2, 1, 2, 1, 15000.00, 15000.00)
ON DUPLICATE KEY UPDATE `cart_id`=`cart_id`;

-- 6. Chèn Đơn hàng mẫu (Orders)
INSERT INTO `orders` (`id`, `user_id`, `status`, `payment_method`, `total_amount`, `shipping_address`, `phone_number`, `notes`, `created_at`, `updated_at`) VALUES
(1, 2, 'PENDING', 'CASH', 100000.00, 'Số 1 Đại Cồ Việt, Hai Bà Trưng, Hà Nội', '0987654321', 'Giao hàng giờ hành chính', NOW(6), NOW(6))
ON DUPLICATE KEY UPDATE `user_id`=`user_id`;

-- 7. Chi tiết đơn hàng mẫu (OrderItems)
INSERT INTO `order_items` (`id`, `order_id`, `product_id`, `quantity`, `unit_price`, `total_price`) VALUES
(1, 1, 1, 1, 85000.00, 85000.00),
(2, 1, 2, 1, 15000.00, 15000.00)
ON DUPLICATE KEY UPDATE `order_id`=`order_id`;

-- 8. Chèn Đánh giá mẫu (Reviews)
INSERT INTO `reviews` (`id`, `product_id`, `user_id`, `rating`, `comment`, `created_at`, `updated_at`) VALUES
(1, 1, 2, 5, 'Táo ngon và giao hàng rất nhanh!', NOW(6), NOW(6))
ON DUPLICATE KEY UPDATE `product_id`=`product_id`;