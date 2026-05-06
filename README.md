# 🌿 Green Food Store — Backend API

> Hệ thống backend cho cửa hàng thực phẩm xanh, xây dựng bằng **Java 17 + Spring Boot 3**

---

## 📋 Mục lục

- [Giới thiệu](#giới-thiệu)
- [Công nghệ sử dụng](#công-nghệ-sử-dụng)
- [Cấu trúc thư mục](#cấu-trúc-thư-mục)
- [Cài đặt & Chạy](#cài-đặt--chạy)
- [API Endpoints](#api-endpoints)
- [Cơ sở dữ liệu](#cơ-sở-dữ-liệu)
- [Bảo mật](#bảo-mật)
- [Liên hệ](#liên-hệ)

---

## Giới thiệu

**Green Food Store** là nền tảng thương mại điện tử chuyên bán thực phẩm hữu cơ, rau củ sạch và các sản phẩm tốt cho sức khỏe. Backend cung cấp RESTful API đầy đủ cho:

- Quản lý sản phẩm và danh mục
- Quản lý đơn hàng và thanh toán
- Xác thực người dùng (JWT)
- Quản lý giỏ hàng
- Hệ thống đánh giá sản phẩm
- Quản lý kho hàng

---

## Công nghệ sử dụng

| Công nghệ         | Phiên bản | Mục đích                      |
|-------------------|-----------|-------------------------------|
| Java              | 17        | Ngôn ngữ lập trình chính      |
| Spring Boot       | 3.2.0     | Framework chính               |
| Spring Security   | 6.x       | Xác thực & phân quyền         |
| Spring Data JPA   | 3.2.0     | Tương tác cơ sở dữ liệu       |
| MySQL             | 8.x       | Cơ sở dữ liệu quan hệ         |
| JWT (jjwt)        | 0.11.5    | Token xác thực                |
| Lombok            | latest    | Giảm boilerplate code         |
| MapStruct         | 1.5.5     | Chuyển đổi DTO ↔ Entity       |
| Maven             | 3.x       | Quản lý dependencies          |

---

## Cấu trúc thư mục

```
green-food-store-backend/
├── pom.xml                                         # Maven dependencies
├── README.md
│
└── src/
    ├── main/
    │   ├── java/com/greenfood/
    │   │   │
    │   │   ├── GreenFoodStoreApplication.java      # Main entry point
    │   │   │
    │   │   ├── config/                             # Cấu hình ứng dụng
    │   │   │   ├── SecurityConfig.java             #   Cấu hình Spring Security
    │   │   │   ├── CorsConfig.java                 #   Cấu hình CORS
    │   │   │   └── SwaggerConfig.java              #   Cấu hình API docs
    │   │   │
    │   │   ├── controller/                         # REST API Controllers
    │   │   │   ├── AuthController.java             #   Đăng nhập / đăng ký
    │   │   │   ├── ProductController.java          #   Quản lý sản phẩm
    │   │   │   ├── CategoryController.java         #   Quản lý danh mục
    │   │   │   ├── OrderController.java            #   Quản lý đơn hàng
    │   │   │   ├── CartController.java             #   Giỏ hàng
    │   │   │   ├── UserController.java             #   Quản lý người dùng
    │   │   │   └── ReviewController.java           #   Đánh giá sản phẩm
    │   │   │
    │   │   ├── service/                            # Interface Service Layer
    │   │   │   ├── AuthService.java
    │   │   │   ├── ProductService.java
    │   │   │   ├── CategoryService.java
    │   │   │   ├── OrderService.java
    │   │   │   ├── CartService.java
    │   │   │   ├── UserService.java
    │   │   │   └── ReviewService.java
    │   │   │
    │   │   ├── serviceImpl/                        # Triển khai Service
    │   │   │   ├── AuthServiceImpl.java
    │   │   │   ├── ProductServiceImpl.java
    │   │   │   ├── CategoryServiceImpl.java
    │   │   │   ├── OrderServiceImpl.java
    │   │   │   ├── CartServiceImpl.java
    │   │   │   ├── UserServiceImpl.java
    │   │   │   └── ReviewServiceImpl.java
    │   │   │
    │   │   ├── repository/                         # Tương tác Database (JPA)
    │   │   │   ├── UserRepository.java
    │   │   │   ├── ProductRepository.java
    │   │   │   ├── CategoryRepository.java
    │   │   │   ├── OrderRepository.java
    │   │   │   ├── OrderItemRepository.java
    │   │   │   ├── CartRepository.java
    │   │   │   └── ReviewRepository.java
    │   │   │
    │   │   ├── model/
    │   │   │   ├── entity/                         # JPA Entities (bảng DB)
    │   │   │   │   ├── User.java                   #   Người dùng
    │   │   │   │   ├── Product.java                #   Sản phẩm
    │   │   │   │   ├── Category.java               #   Danh mục
    │   │   │   │   ├── Order.java                  #   Đơn hàng
    │   │   │   │   ├── OrderItem.java              #   Chi tiết đơn hàng
    │   │   │   │   ├── Cart.java                   #   Giỏ hàng
    │   │   │   │   ├── CartItem.java               #   Sản phẩm trong giỏ
    │   │   │   │   └── Review.java                 #   Đánh giá
    │   │   │   │
    │   │   │   ├── dto/
    │   │   │   │   ├── request/                    # Request DTO (nhận từ client)
    │   │   │   │   │   ├── LoginRequest.java
    │   │   │   │   │   ├── RegisterRequest.java
    │   │   │   │   │   ├── ProductRequest.java
    │   │   │   │   │   ├── OrderRequest.java
    │   │   │   │   │   └── ReviewRequest.java
    │   │   │   │   │
    │   │   │   │   └── response/                   # Response DTO (trả về client)
    │   │   │   │       ├── AuthResponse.java
    │   │   │   │       ├── ProductResponse.java
    │   │   │   │       ├── OrderResponse.java
    │   │   │   │       ├── UserResponse.java
    │   │   │   │       └── ApiResponse.java        #   Response chuẩn chung
    │   │   │   │
    │   │   │   └── enums/                          # Enum constants
    │   │   │       ├── OrderStatus.java            #   PENDING, CONFIRMED, SHIPPING, DONE, CANCELLED
    │   │   │       ├── UserRole.java               #   ADMIN, CUSTOMER
    │   │   │       └── PaymentMethod.java          #   CASH, BANK_TRANSFER, MOMO
    │   │   │
    │   │   ├── security/                           # Bảo mật JWT
    │   │   │   ├── JwtTokenProvider.java           #   Tạo & xác thực JWT
    │   │   │   ├── JwtAuthenticationFilter.java    #   Filter xử lý JWT
    │   │   │   └── CustomUserDetailsService.java   #   Load user từ DB
    │   │   │
    │   │   ├── exception/                          # Xử lý lỗi tập trung
    │   │   │   ├── GlobalExceptionHandler.java     #   @ControllerAdvice
    │   │   │   ├── ResourceNotFoundException.java  #   404 Not Found
    │   │   │   ├── BadRequestException.java        #   400 Bad Request
    │   │   │   └── UnauthorizedException.java      #   401 Unauthorized
    │   │   │
    │   │   ├── mapper/                             # MapStruct Mappers
    │   │   │   ├── ProductMapper.java
    │   │   │   ├── OrderMapper.java
    │   │   │   └── UserMapper.java
    │   │   │
    │   │   └── utils/                              # Tiện ích
    │   │       ├── SlugUtils.java                  #   Tạo slug URL
    │   │       └── FileUploadUtils.java            #   Upload ảnh sản phẩm
    │   │
    │   └── resources/
    │       └── application.properties              # Cấu hình database, JWT, upload...
    │
    └── test/
        ├── java/com/greenfood/                     # Unit & Integration Tests
        └── resources/
```

---

## Cài đặt & Chạy

### Yêu cầu hệ thống

- Java 17+
- Maven 3.6+
- MySQL 8.x

### Các bước cài đặt

**1. Clone dự án**
```bash
git clone https://github.com/your-username/green-food-store-backend.git
cd green-food-store-backend
```

**2. Tạo database MySQL**
```sql
CREATE DATABASE green_food_store CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**3. Cấu hình kết nối DB**

Mở `src/main/resources/application.properties` và cập nhật:
```properties
spring.datasource.username=your_username
spring.datasource.password=your_password
```

**4. Build và chạy**
```bash
mvn clean install
mvn spring-boot:run
```

Server sẽ chạy tại: `http://localhost:8080`

---

## API Endpoints

### Xác thực
| Method | Endpoint              | Mô tả               |
|--------|-----------------------|---------------------|
| POST   | `/api/auth/register`  | Đăng ký tài khoản   |
| POST   | `/api/auth/login`     | Đăng nhập           |

### Sản phẩm
| Method | Endpoint                  | Mô tả                      |
|--------|---------------------------|----------------------------|
| GET    | `/api/products`           | Lấy danh sách sản phẩm     |
| GET    | `/api/products/{id}`      | Lấy chi tiết sản phẩm      |
| POST   | `/api/products`           | Thêm sản phẩm (Admin)      |
| PUT    | `/api/products/{id}`      | Cập nhật sản phẩm (Admin)  |
| DELETE | `/api/products/{id}`      | Xóa sản phẩm (Admin)       |

### Đơn hàng
| Method | Endpoint              | Mô tả                     |
|--------|-----------------------|---------------------------|
| GET    | `/api/orders`         | Danh sách đơn hàng        |
| POST   | `/api/orders`         | Tạo đơn hàng mới          |
| PUT    | `/api/orders/{id}`    | Cập nhật trạng thái đơn   |

### Giỏ hàng
| Method | Endpoint              | Mô tả                      |
|--------|-----------------------|----------------------------|
| GET    | `/api/cart`           | Xem giỏ hàng               |
| POST   | `/api/cart`           | Thêm sản phẩm vào giỏ      |
| DELETE | `/api/cart/{itemId}`  | Xóa sản phẩm khỏi giỏ     |

---

## Cơ sở dữ liệu

Sơ đồ quan hệ các bảng chính:

```
users ──────────── orders ──────── order_items ──── products
  │                                                      │
  └── cart ──── cart_items ──────────────────────────────┘
                                                          │
reviews ──────────────────────────────────────────────────┘

products ──── categories
```

---

## Bảo mật

- Xác thực bằng **JWT Bearer Token**
- Phân quyền: `ROLE_ADMIN` và `ROLE_CUSTOMER`
- Mật khẩu mã hóa bằng **BCrypt**
- Bảo vệ CSRF và cấu hình CORS

---

## Liên hệ

Nếu có câu hỏi hoặc góp ý, vui lòng tạo issue trên GitHub.

---

> 🌱 *Xây dựng với tình yêu dành cho thực phẩm sạch và cuộc sống xanh*
