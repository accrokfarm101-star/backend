-- Cấp tài khoản Admin mặc định khi khởi động ứng dụng
-- Hash password: admin123 (được mã hóa bằng BCrypt)
INSERT IGNORE INTO users (username, email, phone, password, role, created_at, updated_at) 
VALUES ('admin', 'admin@greenfood.com', '0123456789', '$2a$10$slYQmyNdGzin7olVnwAjYON0J5s3pj8N2F5P6m3K2Q9Q8X7Z6Y5W4', 'ROLE_ADMIN', NOW(), NOW());

-- Thêm quản trị viên khác nếu cần
INSERT IGNORE INTO users (username, email, phone, password, role, created_at, updated_at) 
VALUES ('manager', 'manager@greenfood.com', '0987654321', '$2a$10$slYQmyNdGzin7olVnwAjYON0J5s3pj8N2F5P6m3K2Q9Q8X7Z6Y5W4', 'ROLE_ADMIN', NOW(), NOW());

-- Dữ liệu mẫu sản phẩm
INSERT IGNORE INTO products (name, description, price, stock, image_url, category, status, created_at, updated_at)
VALUES ('Rau hữu cơ', 'Rau sạch trồng theo phương pháp hữu cơ, giàu vitamin.', 45000.00, 120, 'https://example.com/images/rau.jpg', 'Rau củ', 'IN_STOCK', NOW(), NOW());
INSERT IGNORE INTO products (name, description, price, stock, image_url, category, status, created_at, updated_at)
VALUES ('Trái cây tươi', 'Trái cây nhập khẩu mới hái, ngọt mát và an toàn.', 78000.00, 80, 'https://example.com/images/trai_cay.jpg', 'Trái cây', 'IN_STOCK', NOW(), NOW());
