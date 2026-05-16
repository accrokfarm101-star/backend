package com.freshmart;

import com.fasterxml.jackson.databind.JsonNode;
import static org.hamcrest.Matchers.containsInAnyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freshmart.model.dto.request.LoginRequest;
import com.freshmart.model.entity.Product;
import com.freshmart.model.entity.Review;
import com.freshmart.model.entity.User;
import com.freshmart.model.enums.ProductStatus;
import com.freshmart.model.enums.UserRole;
import com.freshmart.repository.ProductRepository;
import com.freshmart.repository.ReviewRepository;
import com.freshmart.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
class ReviewControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String userToken;
    private String adminToken;
    private User testUser;
    private User adminUser;
    private Product testProduct;

    @BeforeEach
    void setUp() throws Exception {
        // Clear all data
        reviewRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        adminUser = User.builder()
                .username("admin")
                .email("admin@freshmart.com")
                .phone("0987654321")
                .password(passwordEncoder.encode("admin123"))
                .role(UserRole.ROLE_ADMIN)
                .build();

        testUser = User.builder()
                .username("testuser")
                .email("testuser@freshmart.com")
                .phone("0123456789")
                .password(passwordEncoder.encode("user123"))
                .role(UserRole.ROLE_CUSTOMER)
                .build();

        User anotherUser = User.builder()
                .username("anotheruser")
                .email("another@freshmart.com")
                .phone("0111222333")
                .password(passwordEncoder.encode("user123"))
                .role(UserRole.ROLE_CUSTOMER)
                .build();

        userRepository.saveAll(List.of(adminUser, testUser, anotherUser));
        userRepository.flush();

        // Get updated users with IDs
        adminUser = userRepository.findByUsername("admin").orElse(adminUser);
        testUser = userRepository.findByUsername("testuser").orElse(testUser);
        anotherUser = userRepository.findByUsername("anotheruser").orElse(anotherUser);

        // Create test products
        testProduct = Product.builder()
                .name("Táo Fuji")
                .description("Táo Fuji tươi sạch")
                .price(BigDecimal.valueOf(45000))
                .stock(100)
                .imageUrl("https://example.com/apple.jpg")
                .category("Trái cây")
                .status(ProductStatus.IN_STOCK)
                .build();

        Product product2 = Product.builder()
                .name("Cà chua")
                .description("Cà chua tươi")
                .price(BigDecimal.valueOf(25000))
                .stock(50)
                .imageUrl("https://example.com/tomato.jpg")
                .category("Rau quả")
                .status(ProductStatus.IN_STOCK)
                .build();

        productRepository.saveAll(List.of(testProduct, product2));
        productRepository.flush();

        // Get product IDs
        testProduct = productRepository.findByNameContainingIgnoreCase("Táo Fuji").get(0);
        product2 = productRepository.findByNameContainingIgnoreCase("Cà chua").get(0);

        // Get tokens by logging in
        MvcResult adminLoginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                LoginRequest.builder()
                                        .usernameOrEmail("admin")
                                        .password("admin123")
                                        .build())))
                .andExpect(status().isOk())
                .andReturn();

        String adminResponse = adminLoginResult.getResponse().getContentAsString();
        adminToken = objectMapper.readTree(adminResponse).get("data").get("token").asText();

        MvcResult userLoginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                LoginRequest.builder()
                                        .usernameOrEmail("testuser")
                                        .password("user123")
                                        .build())))
                .andExpect(status().isOk())
                .andReturn();

        String userResponse = userLoginResult.getResponse().getContentAsString();
        userToken = objectMapper.readTree(userResponse).get("data").get("token").asText();

        // Create some test reviews
        Review review1 = Review.builder()
                .product(testProduct)
                .user(testUser)
                .rating(5)
                .comment("Sản phẩm rất tốt, tươi tắn lắm")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Review review2 = Review.builder()
                .product(testProduct)
                .user(anotherUser)
                .rating(4)
                .comment("Tốt nhưng hơi mắc")
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();

        reviewRepository.saveAll(List.of(review1, review2));
        reviewRepository.flush();
    }

    // ==================== GET Tests ====================

    @Test
    void testGetProductReviews_Success() throws Exception {
        mockMvc.perform(get("/api/reviews/product/{productId}", testProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Danh sách đánh giá sản phẩm"))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].rating").value(4))
                .andExpect(jsonPath("$.data[1].rating").value(5));
    }

    @Test
    void testGetProductReviews_ProductNotFound() throws Exception {
        mockMvc.perform(get("/api/reviews/product/{productId}", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testGetProductReviewStats_Success() throws Exception {
        mockMvc.perform(get("/api/reviews/product/{productId}/stats", testProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Thống kê đánh giá sản phẩm"))
                .andExpect(jsonPath("$.data.productId").value(testProduct.getId()))
                .andExpect(jsonPath("$.data.productName").value("Táo Fuji"))
                .andExpect(jsonPath("$.data.averageRating").value(4.5))
                .andExpect(jsonPath("$.data.totalReviews").value(2))
                .andExpect(jsonPath("$.data.reviews", hasSize(2)));
    }

    @Test
    void testGetUserReviews_Success() throws Exception {
        mockMvc.perform(get("/api/reviews/user/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Danh sách đánh giá của người dùng"))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].userId").value(testUser.getId()))
                .andExpect(jsonPath("$.data[0].rating").value(5));
    }

    @Test
    void testGetReviewById_Success() throws Exception {
        List<Review> reviews = reviewRepository.findAll();
        Long reviewId = reviews.get(0).getId();

        mockMvc.perform(get("/api/reviews/{reviewId}", reviewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Chi tiết đánh giá"))
                .andExpect(jsonPath("$.data.id").value(reviewId))
                .andExpect(jsonPath("$.data.rating").value(5));
    }

    @Test
    void testGetReviewById_NotFound() throws Exception {
        mockMvc.perform(get("/api/reviews/{reviewId}", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==================== POST Tests (Create) ====================

    @Test
    void testCreateReview_Success() throws Exception {
        List<Product> products = productRepository.findByNameContainingIgnoreCase("Cà chua");
        Long productId = products.get(0).getId();

        String requestBody = objectMapper.writeValueAsString(
                new com.freshmart.model.dto.request.ReviewRequest(5, "Cà chua rất tươi ngon")
        );

        mockMvc.perform(post("/api/reviews/product/{productId}", productId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tạo đánh giá thành công"))
                .andExpect(jsonPath("$.data.productId").value(productId))
                .andExpect(jsonPath("$.data.rating").value(5))
                .andExpect(jsonPath("$.data.comment").value("Cà chua rất tươi ngon"));
    }

    @Test
    void testCreateReview_Unauthorized() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                new com.freshmart.model.dto.request.ReviewRequest(5, "Good product")
        );

        mockMvc.perform(post("/api/reviews/product/{productId}", testProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateReview_DuplicateReview() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                new com.freshmart.model.dto.request.ReviewRequest(4, "Updated review")
        );

        mockMvc.perform(post("/api/reviews/product/{productId}", testProduct.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("đã đánh giá")));
    }

    @Test
    void testCreateReview_InvalidRating() throws Exception {
        List<Product> products = productRepository.findByNameContainingIgnoreCase("Cà chua");
        Long productId = products.get(0).getId();

        String requestBody = objectMapper.writeValueAsString(
                new com.freshmart.model.dto.request.ReviewRequest(10, "Invalid rating")
        );

        mockMvc.perform(post("/api/reviews/product/{productId}", productId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateReview_ProductNotFound() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                new com.freshmart.model.dto.request.ReviewRequest(5, "Good product")
        );

        mockMvc.perform(post("/api/reviews/product/{productId}", 9999L)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==================== PUT Tests (Update) ====================

    @Test
    void testUpdateReview_Success() throws Exception {
        List<Review> reviews = reviewRepository.findAll();
        Long reviewId = reviews.get(0).getId();

        String requestBody = objectMapper.writeValueAsString(
                new com.freshmart.model.dto.request.ReviewRequest(4, "Updated comment - sản phẩm tốt")
        );

        mockMvc.perform(put("/api/reviews/{reviewId}", reviewId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cập nhật đánh giá thành công"))
                .andExpect(jsonPath("$.data.rating").value(4))
                .andExpect(jsonPath("$.data.comment").value("Updated comment - sản phẩm tốt"));
    }

    @Test
    void testUpdateReview_Unauthorized() throws Exception {
        // Try to update someone else's review
        List<Review> reviews = reviewRepository.findAll();
        Long reviewId = reviews.get(1).getId(); // Review from anotherUser

        String requestBody = objectMapper.writeValueAsString(
                new com.freshmart.model.dto.request.ReviewRequest(1, "Hack attempt")
        );

        mockMvc.perform(put("/api/reviews/{reviewId}", reviewId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testUpdateReview_NotFound() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                new com.freshmart.model.dto.request.ReviewRequest(5, "Update")
        );

        mockMvc.perform(put("/api/reviews/{reviewId}", 9999L)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    // ==================== DELETE Tests ====================

    @Test
    void testDeleteReview_Success() throws Exception {
        List<Review> reviews = reviewRepository.findAll();
        Long reviewId = reviews.get(0).getId();

        mockMvc.perform(delete("/api/reviews/{reviewId}", reviewId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Xóa đánh giá thành công"));

        // Verify review is deleted
        mockMvc.perform(get("/api/reviews/{reviewId}", reviewId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteReview_Unauthorized() throws Exception {
        // Try to delete someone else's review
        List<Review> reviews = reviewRepository.findAll();
        Long reviewId = reviews.get(1).getId(); // Review from anotherUser

        mockMvc.perform(delete("/api/reviews/{reviewId}", reviewId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testDeleteReviewByAdmin_Success() throws Exception {
        List<Review> reviews = reviewRepository.findAll();
        Long reviewId = reviews.get(0).getId();

        mockMvc.perform(delete("/api/reviews/{reviewId}/admin", reviewId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify review is deleted
        mockMvc.perform(get("/api/reviews/{reviewId}", reviewId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteReviewByAdmin_Unauthorized() throws Exception {
        List<Review> reviews = reviewRepository.findAll();
        Long reviewId = reviews.get(0).getId();

        // Try as regular user
        mockMvc.perform(delete("/api/reviews/{reviewId}/admin", reviewId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // ==================== Integration Tests ====================

    @Test
    void testCompleteReviewLifecycle() throws Exception {
        List<Product> products = productRepository.findByNameContainingIgnoreCase("Cà chua");
        Long productId = products.get(0).getId();

        // 1. Create review
        String createRequest = objectMapper.writeValueAsString(
                new com.freshmart.model.dto.request.ReviewRequest(5, "Excellent product")
        );

        MvcResult createResult = mockMvc.perform(post("/api/reviews/product/{productId}", productId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isCreated())
                .andReturn();

        Long reviewId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("data").get("id").asLong();

        // 2. Get review details
        mockMvc.perform(get("/api/reviews/{reviewId}", reviewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rating").value(5));

        // 3. Update review
        String updateRequest = objectMapper.writeValueAsString(
                new com.freshmart.model.dto.request.ReviewRequest(4, "Good but pricey")
        );

        mockMvc.perform(put("/api/reviews/{reviewId}", reviewId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rating").value(4));

        // 4. Get product stats
        mockMvc.perform(get("/api/reviews/product/{productId}/stats", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalReviews").value(1));

        // 5. Delete review
        mockMvc.perform(delete("/api/reviews/{reviewId}", reviewId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        // 6. Verify deletion
        mockMvc.perform(get("/api/reviews/{reviewId}", reviewId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testMultipleReviewsStats() throws Exception {
        // Stats should show average of 4.5 (5 + 4) / 2
        mockMvc.perform(get("/api/reviews/product/{productId}/stats", testProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.averageRating").value(4.5))
                .andExpect(jsonPath("$.data.totalReviews").value(2));
    }

    @Test
    void testEmptyProductReviews() throws Exception {
        List<Product> products = productRepository.findByNameContainingIgnoreCase("Cà chua");
        Long productId = products.get(0).getId();

        mockMvc.perform(get("/api/reviews/product/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void testEmptyUserReviews() throws Exception {
        User newUser = User.builder()
                .username("newuser")
                .email("new@freshmart.com")
                .phone("0999999999")
                .password(passwordEncoder.encode("user123"))
                .role(UserRole.ROLE_CUSTOMER)
                .build();
        userRepository.save(newUser);

        mockMvc.perform(get("/api/reviews/user/{userId}", newUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }
}
