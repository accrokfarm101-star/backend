package com.freshmart;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freshmart.model.dto.request.UpdateUserRequest;
import com.freshmart.model.entity.User;
import com.freshmart.model.enums.UserRole;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String customerToken;
    private User adminUser;
    private User customerUser;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();

        adminUser = userRepository.save(User.builder()
                .username("admin")
                .email("admin@freshmart.com")
                .phone("0123456789")
                .password(passwordEncoder.encode("admin123"))
                .role(UserRole.ROLE_ADMIN)
                .build());

        customerUser = userRepository.save(User.builder()
                .username("customer")
                .email("customer@freshmart.com")
                .phone("0912345678")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.ROLE_CUSTOMER)
                .build());

        // Lấy token admin
        String adminLoginJson = objectMapper.writeValueAsString(
                new com.freshmart.model.dto.request.LoginRequest("admin", "admin123"));

        MvcResult adminLoginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(adminLoginJson))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode adminLoginResponse = objectMapper.readTree(adminLoginResult.getResponse().getContentAsString());
        adminToken = adminLoginResponse.get("data").get("token").asText();

        // Lấy token customer
        String customerLoginJson = objectMapper.writeValueAsString(
                new com.freshmart.model.dto.request.LoginRequest("customer", "password123"));

        MvcResult customerLoginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(customerLoginJson))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode customerLoginResponse = objectMapper.readTree(customerLoginResult.getResponse().getContentAsString());
        customerToken = customerLoginResponse.get("data").get("token").asText();
    }

    @Test
    void testGetAllUsersAsAdmin() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void testGetAllUsersAsCustomer() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetUserByIdAsAdmin() throws Exception {
        mockMvc.perform(get("/api/users/" + customerUser.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("customer"));
    }

    @Test
    void testUpdateUserProfile() throws Exception {
        UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                .email("newemail@freshmart.com")
                .phone("0987654321")
                .currentPassword("password123")
                .newPassword("newpass123")
                .build();

        mockMvc.perform(put("/api/users/" + customerUser.getId())
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("newemail@freshmart.com"))
                .andExpect(jsonPath("$.data.phone").value("0987654321"));
    }

    @Test
    void testUpdateUserProfileWithWrongPassword() throws Exception {
        UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                .email("newemail@freshmart.com")
                .phone("0987654321")
                .currentPassword("wrongpassword")
                .newPassword("newpass123")
                .build();

        mockMvc.perform(put("/api/users/" + customerUser.getId())
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteUserAsAdmin() throws Exception {
        mockMvc.perform(delete("/api/users/" + customerUser.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testDeleteUserAsCustomer() throws Exception {
        mockMvc.perform(delete("/api/users/" + adminUser.getId())
                .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }
}
