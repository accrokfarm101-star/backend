package com.greenfood;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenfood.model.dto.request.LoginRequest;
import com.greenfood.model.dto.request.RegisterRequest;
import com.greenfood.model.entity.User;
import com.greenfood.model.enums.UserRole;
import com.greenfood.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRepository.saveAll(List.of(
                User.builder()
                        .username("admin")
                        .email("admin@greenfood.com")
                        .phone(null)
                        .password(passwordEncoder.encode("admin123"))
                        .role(UserRole.ROLE_ADMIN)
                        .build(),
                User.builder()
                        .username("manager")
                        .email("manager@greenfood.com")
                        .phone(null)
                        .password(passwordEncoder.encode("admin123"))
                        .role(UserRole.ROLE_ADMIN)
                        .build()
        ));
        userRepository.flush();
    }

    @Test
    void testRegisterSuccess() throws Exception {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .email("newuser@example.com")
                .phone("0111111111")
                .password("password123")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đăng ký thành công"))
                .andExpect(jsonPath("$.data.username").value("newuser"))
                .andExpect(jsonPath("$.data.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.data.phone").value("0111111111"))
                .andExpect(jsonPath("$.data.role").value("ROLE_CUSTOMER"))
                .andExpect(jsonPath("$.data.token").value(notNullValue()));
    }

    @Test
    void testRegisterMissingUsername() throws Exception {
        // Arrange
        String json = "{ \"email\": \"test@example.com\", \"phone\": \"0111111111\", \"password\": \"pass123\" }";

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterMissingPhone() throws Exception {
        // Arrange
        String json = "{ \"username\": \"user\", \"email\": \"test@example.com\", \"password\": \"pass123\" }";

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterInvalidEmail() throws Exception {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username("user")
                .email("invalidemail")
                .phone("0111111111")
                .password("password123")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginWithAdminCredentials() throws Exception {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("admin")
                .password("admin123")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đăng nhập thành công"))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.role").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.data.token").value(notNullValue()));
    }

    @Test
    void testLoginInvalidCredentials() throws Exception {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("admin")
                .password("wrongpassword")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testGetCurrentUserWithoutToken() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetCurrentUserWithValidToken() throws Exception {
        // Arrange - Register a user and get token
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("tokenuser")
                .email("tokenuser@example.com")
                .phone("0222222222")
                .password("password123")
                .build();

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String response = registerResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(response).get("data").get("token").asText();

        // Act & Assert
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("tokenuser"))
                .andExpect(jsonPath("$.data.email").value("tokenuser@example.com"))
                .andExpect(jsonPath("$.data.phone").value("0222222222"))
                .andExpect(jsonPath("$.data.role").value("ROLE_CUSTOMER"));
    }
}