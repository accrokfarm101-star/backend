package com.freshmart;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freshmart.model.entity.Product;
import com.freshmart.model.entity.User;
import com.freshmart.model.enums.ProductStatus;
import com.freshmart.model.enums.UserRole;
import com.freshmart.repository.FavoriteRepository;
import com.freshmart.repository.ProductRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FavoriteControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String userToken;
    private Product product;

    @BeforeEach
    void setUp() throws Exception {
        favoriteRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        User user = userRepository.save(User.builder()
                .username("customer")
                .email("customer@freshmart.com")
                .phone("0912345678")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.ROLE_CUSTOMER)
                .build());

        product = productRepository.save(Product.builder()
                .name("Bơ sáp")
                .description("Bơ sạch tươi ngon")
                .price(BigDecimal.valueOf(50000))
                .stock(20)
                .imageUrl("https://example.com/bo.jpg")
                .category("Trái cây")
                .status(ProductStatus.IN_STOCK)
                .build());

        String loginJson = objectMapper.writeValueAsString(
                new com.freshmart.model.dto.request.LoginRequest("customer", "password123"));

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginResponse = objectMapper.readTree(result.getResponse().getContentAsString());
        userToken = loginResponse.get("data").get("token").asText();
    }

    @Test
    void testAddFavoriteProduct() throws Exception {
        mockMvc.perform(post("/api/favorites/" + product.getId())
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(product.getId()));
    }

    @Test
    void testGetFavoriteProducts() throws Exception {
        mockMvc.perform(post("/api/favorites/" + product.getId())
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/favorites")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(product.getId()));
    }

    @Test
    void testRemoveFavoriteProduct() throws Exception {
        mockMvc.perform(post("/api/favorites/" + product.getId())
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/favorites/" + product.getId())
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
