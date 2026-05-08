package com.greenfood;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenfood.model.dto.request.LoginRequest;
import com.greenfood.model.entity.Product;
import com.greenfood.model.entity.User;
import com.greenfood.model.enums.ProductStatus;
import com.greenfood.model.enums.UserRole;
import com.greenfood.repository.ProductRepository;
import com.greenfood.repository.UserRepository;
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
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        productRepository.deleteAll();
        userRepository.deleteAll();

        userRepository.saveAll(List.of(
                User.builder()
                        .username("admin")
                        .email("admin@greenfood.com")
                        .phone("0123456789")
                        .password(passwordEncoder.encode("admin123"))
                        .role(UserRole.ROLE_ADMIN)
                        .build()
        ));
        userRepository.flush();

        productRepository.saveAll(List.of(
                Product.builder()
                        .name("Rau cải")
                        .description("Rau sạch tươi ngon")
                        .price(BigDecimal.valueOf(25000))
                        .stock(50)
                        .imageUrl("https://example.com/rau_cai.jpg")
                        .category("Rau củ")
                        .status(ProductStatus.IN_STOCK)
                        .build(),
                Product.builder()
                        .name("Táo đỏ")
                        .description("Táo ngọt, mọng nước")
                        .price(BigDecimal.valueOf(42000))
                        .stock(0)
                        .imageUrl("https://example.com/tao_do.jpg")
                        .category("Trái cây")
                        .status(ProductStatus.OUT_OF_STOCK)
                        .build()
        ));
        productRepository.flush();

        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail("admin")
                .password("admin123")
                .build();

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        adminToken = loginJson.get("data").get("token").asText();
    }

    @Test
    void testGetAllProductsReturnsProducts() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    void testSearchProductsByName() throws Exception {
        mockMvc.perform(get("/api/products")
                .param("search", "Táo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name").value("Táo đỏ"));
    }

    @Test
    void testUpdateProductStatusToOutOfStock() throws Exception {
        Product product = productRepository.findByNameContainingIgnoreCase("Rau").get(0);
        String json = "{\"status\":\"OUT_OF_STOCK\"}";

        mockMvc.perform(patch("/api/products/" + product.getId() + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("Hết hàng"));
    }
}
