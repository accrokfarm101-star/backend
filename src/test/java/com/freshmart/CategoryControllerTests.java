package com.freshmart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freshmart.model.dto.request.CategoryRequest;
import com.freshmart.model.dto.request.LoginRequest;
import com.freshmart.model.entity.Category;
import com.freshmart.model.entity.User;
import com.freshmart.model.enums.UserRole;
import com.freshmart.repository.CategoryRepository;
import com.freshmart.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CategoryControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String userToken;
    private Category testCategory;

    @BeforeEach
    void setUp() throws Exception {
        // Xóa sạch data cũ
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Tạo user admin
        User adminUser = User.builder()
                .username("admin")
                .email("admin@freshmart.com")
                .phone("0987654321")
                .password(passwordEncoder.encode("admin123"))
                .role(UserRole.ROLE_ADMIN)
                .build();

        // 2. Tạo user customer
        User testUser = User.builder()
                .username("testuser")
                .email("testuser@freshmart.com")
                .phone("0123456789")
                .password(passwordEncoder.encode("user123"))
                .role(UserRole.ROLE_CUSTOMER)
                .build();

        userRepository.saveAll(java.util.List.of(adminUser, testUser));
        userRepository.flush();

        // 3. Lấy token Admin
        MvcResult adminLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                LoginRequest.builder()
                                        .usernameOrEmail("admin")
                                        .password("admin123")
                                        .build())))
                .andExpect(status().isOk())
                .andReturn();
        adminToken = objectMapper.readTree(adminLogin.getResponse().getContentAsString())
                .get("data").get("token").asText();

        // 4. Lấy token Customer
        MvcResult userLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                LoginRequest.builder()
                                        .usernameOrEmail("testuser")
                                        .password("user123")
                                        .build())))
                .andExpect(status().isOk())
                .andReturn();
        userToken = objectMapper.readTree(userLogin.getResponse().getContentAsString())
                .get("data").get("token").asText();

        // 5. Tạo sẵn 1 danh mục nông sản để test
        testCategory = Category.builder()
                .name("Rau củ hữu cơ")
                .description("Các loại rau củ tươi sạch đạt chuẩn Organic")
                // Bổ sung các trường khác nếu có (ví dụ: imageUrl, isActive...)
                .build();
        categoryRepository.save(testCategory);
        categoryRepository.flush();
    }

    @AfterEach
    void tearDown() {
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testGetAllCategories_Success() throws Exception {
        mockMvc.perform(get("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Danh sách danh mục"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("Rau củ hữu cơ"));
    }

    @Test
    void testGetCategoryById_Success() throws Exception {
        mockMvc.perform(get("/api/categories/" + testCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Chi tiết danh mục"))
                .andExpect(jsonPath("$.data.name").value("Rau củ hữu cơ"));
    }

    @Test
    void testGetCategoryById_NotFound() throws Exception {
        mockMvc.perform(get("/api/categories/99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Kỳ vọng 404
    }

    @Test
    void testCreateCategory_AsAdmin_Success() throws Exception {
        CategoryRequest newCategory = CategoryRequest.builder()
                .name("Trái cây nhập khẩu")
                .description("Trái cây tươi ngon nhập khẩu theo mùa")
                .build();

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategory)))
                .andExpect(status().isCreated()) // Mã 201 Created
                .andExpect(jsonPath("$.message").value("Tạo danh mục thành công"))
                .andExpect(jsonPath("$.data.name").value("Trái cây nhập khẩu"));
    }

    @Test
    void testCreateCategory_AsCustomer_Forbidden() throws Exception {
        CategoryRequest newCategory = CategoryRequest.builder()
                .name("Đồ uống")
                .description("Nước ép và sữa hạt")
                .build();

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + userToken) // Dùng token khách hàng
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategory)))
                .andExpect(status().isForbidden()); // Kỳ vọng 403 Forbidden
    }

    @Test
    void testUpdateCategory_AsAdmin_Success() throws Exception {
        CategoryRequest updateRequest = CategoryRequest.builder()
                .name("Rau củ quả (Đã sửa)")
                .description("Cập nhật lại mô tả danh mục")
                .build();

        mockMvc.perform(put("/api/categories/" + testCategory.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cập nhật danh mục thành công"))
                .andExpect(jsonPath("$.data.name").value("Rau củ quả (Đã sửa)"));
    }

    @Test
    void testDeleteCategory_AsAdmin_Success() throws Exception {
        mockMvc.perform(delete("/api/categories/" + testCategory.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Xóa danh mục thành công"));
    }
}