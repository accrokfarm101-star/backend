package com.freshmart;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freshmart.model.dto.request.CartItemRequest;
import com.freshmart.model.entity.Product;
import com.freshmart.model.entity.User;
import com.freshmart.model.enums.ProductStatus;
import com.freshmart.model.enums.UserRole;
import com.freshmart.repository.CartRepository;
import com.freshmart.repository.CartItemRepository;
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
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CartControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String userToken;
    private Product product;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up in correct order: cart items depend on carts, carts depend on users, users are independent
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
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
    void testAddItemToCart() throws Exception {
        CartItemRequest request = new CartItemRequest(product.getId(), 2);

        mockMvc.perform(post("/api/cart/items")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.cartItems[0].productId").value(product.getId()))
                .andExpect(jsonPath("$.data.cartItems[0].quantity").value(2))
                .andExpect(jsonPath("$.data.totalAmount").value(100000));
    }

    @Test
    void testUpdateCartItemQuantity() throws Exception {
        CartItemRequest request = new CartItemRequest(product.getId(), 1);
        MvcResult addResult = mockMvc.perform(post("/api/cart/items")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long itemId = objectMapper.readTree(addResult.getResponse().getContentAsString())
                .get("data").get("cartItems").get(0).get("id").asLong();

        mockMvc.perform(put("/api/cart/items/" + itemId)
                .header("Authorization", "Bearer " + userToken)
                .param("quantity", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cartItems[0].quantity").value(3))
                .andExpect(jsonPath("$.data.totalAmount").value(150000));
    }

    @Test
    void testRemoveCartItem() throws Exception {
        CartItemRequest request = new CartItemRequest(product.getId(), 1);
        MvcResult addResult = mockMvc.perform(post("/api/cart/items")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long itemId = objectMapper.readTree(addResult.getResponse().getContentAsString())
                .get("data").get("cartItems").get(0).get("id").asLong();

        mockMvc.perform(delete("/api/cart/items/" + itemId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cartItems").isEmpty())
                .andExpect(jsonPath("$.data.totalAmount").value(0));
    }

    @Test
    void testClearCart() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CartItemRequest(product.getId(), 2))))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/cart")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cartItems").isEmpty())
                .andExpect(jsonPath("$.data.totalAmount").value(0));
    }
}
