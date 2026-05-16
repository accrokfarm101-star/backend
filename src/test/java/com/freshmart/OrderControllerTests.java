package com.freshmart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freshmart.model.dto.request.LoginRequest;
import com.freshmart.model.dto.request.OrderRequest;
import com.freshmart.model.dto.request.OrderItemRequest;
import com.freshmart.model.enums.PaymentMethod;
import com.freshmart.model.entity.Order;
import com.freshmart.model.entity.Product;
import com.freshmart.model.entity.User;
import com.freshmart.model.enums.OrderStatus;
import com.freshmart.model.enums.ProductStatus;
import com.freshmart.model.enums.UserRole;
import com.freshmart.repository.OrderRepository;
import com.freshmart.repository.ProductRepository;
import com.freshmart.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String userToken;
    private String adminToken;
    private User testUser;
    private User adminUser;
    private Product testProduct;
    private Order testOrder;

    @BeforeEach
    void setUp() throws Exception {
        // Xóa sạch data cũ
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        // Tạo user admin
        adminUser = User.builder()
                .username("admin")
                .email("admin@freshmart.com")
                .phone("0987654321")
                .password(passwordEncoder.encode("admin123"))
                .role(UserRole.ROLE_ADMIN)
                .build();

        // Tạo user customer
        testUser = User.builder()
                .username("testuser")
                .email("testuser@freshmart.com")
                .phone("0123456789")
                .password(passwordEncoder.encode("user123"))
                .role(UserRole.ROLE_CUSTOMER)
                .build();

        // Tạo user khác (để test bảo mật)
        User otherUser = User.builder()
                .username("otheruser")
                .email("other@freshmart.com")
                .phone("0111222333")
                .password(passwordEncoder.encode("user123"))
                .role(UserRole.ROLE_CUSTOMER)
                .build();

        userRepository.saveAll(List.of(adminUser, testUser, otherUser));
        userRepository.flush();

        // Lấy lại user với ID
        adminUser = userRepository.findByUsername("admin").orElseThrow();
        testUser = userRepository.findByUsername("testuser").orElseThrow();

        // Tạo sản phẩm test
        testProduct = Product.builder()
                .name("Táo Fuji")
                .description("Táo Fuji tươi sạch")
                .price(BigDecimal.valueOf(50000))
                .stock(100)
                .imageUrl("https://example.com/apple.jpg")
                .category("Trái cây")
                .status(ProductStatus.IN_STOCK)
                .build();

        productRepository.save(testProduct);
        productRepository.flush();
        testProduct = productRepository.findByNameContainingIgnoreCase("Táo Fuji").get(0);

        // Lấy token bằng cách login
        MvcResult adminLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                LoginRequest.builder()
                                        .usernameOrEmail("admin")
                                        .password("admin123")
                                        .build())))
                .andExpect(status().isOk())
                .andReturn();

        adminToken = objectMapper.readTree(
                adminLogin.getResponse().getContentAsString()
        ).get("data").get("token").asText();

        MvcResult userLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                LoginRequest.builder()
                                        .usernameOrEmail("testuser")
                                        .password("user123")
                                        .build())))
                .andExpect(status().isOk())
                .andReturn();

        userToken = objectMapper.readTree(
                userLogin.getResponse().getContentAsString()
        ).get("data").get("token").asText();

        // Tạo sẵn 1 đơn hàng để dùng trong các test
        testOrder = Order.builder()
                .user(testUser)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(100000))
                .shippingAddress("123 Đường Test, Hà Nội")
                .paymentMethod(PaymentMethod.CASH)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        orderRepository.save(testOrder);
        orderRepository.flush();
    }

    // =========================================================
    // ✅ TEST 1: Tạo đơn hàng
    // =========================================================

    @Test
    @DisplayName("POST /api/orders - Customer tạo đơn hàng thành công")
    void testCreateOrder_Success() throws Exception {
        OrderRequest request = OrderRequest.builder()
                .shippingAddress("123 Đường Test, Hà Nội")
                .phoneNumber("0123456789")
                .paymentMethod(PaymentMethod.CASH)
                .orderItems(List.of(
                        new OrderItemRequest(testProduct.getId(), 2)
                ))
                .build();

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tạo đơn hàng thành công"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.shippingAddress").value("123 Đường Test, Hà Nội"));
    }

    @Test
    @DisplayName("POST /api/orders - Chưa đăng nhập không tạo được")
    void testCreateOrder_Unauthorized() throws Exception {
        OrderRequest request = OrderRequest.builder()
                .shippingAddress("123 Đường Test, Hà Nội")
                .phoneNumber("0123456789")
                .paymentMethod(PaymentMethod.CASH)
                .orderItems(List.of(
                        new OrderItemRequest(testProduct.getId(), 2)
                ))
                .build();

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/orders - Thiếu địa chỉ giao hàng")
    void testCreateOrder_MissingAddress() throws Exception {
        OrderRequest request = OrderRequest.builder()
                .phoneNumber("0123456789")
                .paymentMethod(PaymentMethod.CASH)
                .orderItems(List.of(
                        new OrderItemRequest(testProduct.getId(), 2)
                ))
                .build();

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/orders - Sản phẩm không tồn tại")
    void testCreateOrder_ProductNotFound() throws Exception {
        OrderRequest request = OrderRequest.builder()
                .shippingAddress("123 Đường Test, Hà Nội")
                .phoneNumber("0123456789")
                .paymentMethod(PaymentMethod.CASH)
                .orderItems(List.of(
                        new OrderItemRequest(9999L, 2)
                ))
                .build();

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // =========================================================
    // ✅ TEST 2: Xem chi tiết đơn hàng
    // =========================================================

    @Test
    @DisplayName("GET /api/orders/{id} - Xem chi tiết đơn hàng thành công")
    void testGetOrderById_Success() throws Exception {
        mockMvc.perform(get("/api/orders/{id}", testOrder.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Chi tiết đơn hàng"))
                .andExpect(jsonPath("$.data.id").value(testOrder.getId()))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/orders/{id} - Không tìm thấy đơn hàng")
    void testGetOrderById_NotFound() throws Exception {
        mockMvc.perform(get("/api/orders/{id}", 9999L)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/orders/{id} - Chưa đăng nhập không xem được")
    void testGetOrderById_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/orders/{id}", testOrder.getId()))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================
    // ✅ TEST 3: Xem danh sách đơn hàng của mình
    // =========================================================

    @Test
    @DisplayName("GET /api/orders/my-orders - Xem đơn hàng của mình")
    void testGetMyOrders_Success() throws Exception {
        mockMvc.perform(get("/api/orders/my-orders")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Danh sách đơn hàng của bạn"))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /api/orders/my-orders - Lọc theo trạng thái PENDING")
    void testGetMyOrders_FilterByStatus() throws Exception {
        mockMvc.perform(get("/api/orders/my-orders")
                        .param("status", "PENDING")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[*].status", everyItem(is("PENDING"))));
    }

    @Test
    @DisplayName("GET /api/orders/my-orders - Chưa đăng nhập không xem được")
    void testGetMyOrders_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/orders/my-orders"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================
    // ✅ TEST 4: Admin xem tất cả đơn hàng
    // =========================================================

    @Test
    @DisplayName("GET /api/orders - Admin xem tất cả đơn hàng")
    void testGetAllOrders_AsAdmin_Success() throws Exception {
        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Danh sách tất cả đơn hàng"))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /api/orders - Customer không xem được tất cả đơn hàng")
    void testGetAllOrders_AsCustomer_Forbidden() throws Exception {
        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/orders - Admin lọc theo trạng thái")
    void testGetAllOrders_FilterByStatus() throws Exception {
        mockMvc.perform(get("/api/orders")
                        .param("status", "PENDING")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].status", everyItem(is("PENDING"))));
    }

    // =========================================================
    // ✅ TEST 5: Admin cập nhật trạng thái đơn hàng
    // =========================================================

    @Test
    @DisplayName("PUT /api/orders/{id}/status - Admin cập nhật PENDING → CONFIRMED")
    void testUpdateOrderStatus_Success() throws Exception {
        mockMvc.perform(put("/api/orders/{id}/status", testOrder.getId())
                        .param("status", "CONFIRMED")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cập nhật trạng thái đơn hàng thành công"))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("PUT /api/orders/{id}/status - Admin cập nhật CONFIRMED → SHIPPING")
    void testUpdateOrderStatus_ToShipping() throws Exception {
        // Cập nhật lên CONFIRMED trước
        mockMvc.perform(put("/api/orders/{id}/status", testOrder.getId())
                        .param("status", "CONFIRMED")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Rồi cập nhật lên SHIPPING
        mockMvc.perform(put("/api/orders/{id}/status", testOrder.getId())
                        .param("status", "SHIPPING")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SHIPPING"));
    }

    @Test
    @DisplayName("PUT /api/orders/{id}/status - Customer không cập nhật được")
    void testUpdateOrderStatus_AsCustomer_Forbidden() throws Exception {
        mockMvc.perform(put("/api/orders/{id}/status", testOrder.getId())
                        .param("status", "CONFIRMED")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/orders/{id}/status - Đơn hàng không tồn tại")
    void testUpdateOrderStatus_NotFound() throws Exception {
        mockMvc.perform(put("/api/orders/{id}/status", 9999L)
                        .param("status", "CONFIRMED")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // =========================================================
    // ✅ TEST 6: Hủy đơn hàng
    // =========================================================

    @Test
    @DisplayName("POST /api/orders/{id}/cancel - Hủy đơn PENDING thành công")
    void testCancelOrder_Success() throws Exception {
        mockMvc.perform(post("/api/orders/{id}/cancel", testOrder.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Hủy đơn hàng thành công"))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("POST /api/orders/{id}/cancel - Không hủy được đơn đang giao")
    void testCancelOrder_AlreadyShipping() throws Exception {
        // Cập nhật lên SHIPPING trước
        mockMvc.perform(put("/api/orders/{id}/status", testOrder.getId())
                        .param("status", "SHIPPING")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Thử hủy → phải bị chặn
        mockMvc.perform(post("/api/orders/{id}/cancel", testOrder.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/orders/{id}/cancel - Không hủy được đơn đã giao")
    void testCancelOrder_AlreadyDelivered() throws Exception {
        // Cập nhật lên DELIVERED trước
        mockMvc.perform(put("/api/orders/{id}/status", testOrder.getId())
                        .param("status", "DELIVERED")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Thử hủy → phải bị chặn
        mockMvc.perform(post("/api/orders/{id}/cancel", testOrder.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/orders/{id}/cancel - Chưa đăng nhập không hủy được")
    void testCancelOrder_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/orders/{id}/cancel", testOrder.getId()))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================
    // ✅ TEST 7: Admin xóa đơn hàng
    // =========================================================

    @Test
    @DisplayName("DELETE /api/orders/{id} - Admin xóa thành công")
    void testDeleteOrder_AsAdmin_Success() throws Exception {
        mockMvc.perform(delete("/api/orders/{id}", testOrder.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Xóa đơn hàng thành công"));

        // Kiểm tra đơn hàng đã bị xóa
        mockMvc.perform(get("/api/orders/{id}", testOrder.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/orders/{id} - Customer không xóa được")
    void testDeleteOrder_AsCustomer_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/orders/{id}", testOrder.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/orders/{id} - Đơn hàng không tồn tại")
    void testDeleteOrder_NotFound() throws Exception {
        mockMvc.perform(delete("/api/orders/{id}", 9999L)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // =========================================================
    // ✅ TEST 8: Thống kê
    // =========================================================

    @Test
    @DisplayName("GET /api/orders/statistics/count-by-status - Admin xem thống kê")
    void testGetOrderCountByStatus_AsAdmin() throws Exception {
        mockMvc.perform(get("/api/orders/statistics/count-by-status")
                        .param("status", "PENDING")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("GET /api/orders/statistics/count-by-status - Customer không xem được")
    void testGetOrderCountByStatus_AsCustomer_Forbidden() throws Exception {
        mockMvc.perform(get("/api/orders/statistics/count-by-status")
                        .param("status", "PENDING")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/orders/statistics/user-count - Xem số đơn hàng của user")
    void testGetUserOrderCount_Success() throws Exception {
        mockMvc.perform(get("/api/orders/statistics/user-count")
                        .param("userId", testUser.getId().toString())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(greaterThanOrEqualTo(1)));
    }

    // =========================================================
    // ✅ TEST 9: Test toàn bộ vòng đời đơn hàng
    // =========================================================

    @Test
    @DisplayName("Integration - Toàn bộ vòng đời đơn hàng")
    void testCompleteOrderLifecycle() throws Exception {
        // 1. Tạo đơn hàng
        OrderRequest request = OrderRequest.builder()
                .shippingAddress("123 Đường Test, Hà Nội")
                .phoneNumber("0123456789")
                .paymentMethod(PaymentMethod.CASH)
                .orderItems(List.of(
                        new OrderItemRequest(testProduct.getId(), 2)
                ))
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn();

        Long orderId = objectMapper.readTree(
                createResult.getResponse().getContentAsString()
        ).get("data").get("id").asLong();

        // 2. Xem chi tiết đơn hàng
        mockMvc.perform(get("/api/orders/{id}", orderId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        // 3. Admin xác nhận đơn → CONFIRMED
        mockMvc.perform(put("/api/orders/{id}/status", orderId)
                        .param("status", "CONFIRMED")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));

        // 4. Admin chuyển sang giao hàng → SHIPPING
        mockMvc.perform(put("/api/orders/{id}/status", orderId)
                        .param("status", "SHIPPING")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SHIPPING"));

        // 5. Admin xác nhận đã giao → DELIVERED
        mockMvc.perform(put("/api/orders/{id}/status", orderId)
                        .param("status", "DELIVERED")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DELIVERED"));

        // 6. Thử hủy đơn đã giao → phải thất bại
        mockMvc.perform(post("/api/orders/{id}/cancel", orderId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest());

        // 7. Admin xóa đơn hàng
        mockMvc.perform(delete("/api/orders/{id}", orderId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // 8. Kiểm tra đơn hàng đã bị xóa
        mockMvc.perform(get("/api/orders/{id}", orderId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}