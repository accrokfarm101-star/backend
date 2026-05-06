package com.greenfood;

import com.greenfood.model.dto.request.LoginRequest;
import com.greenfood.model.dto.request.RegisterRequest;
import com.greenfood.model.entity.User;
import com.greenfood.model.enums.UserRole;
import com.greenfood.repository.UserRepository;
import com.greenfood.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AuthenticationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Reset DB to a known state for each test
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

        assertTrue(userRepository.existsByUsername("admin"));
    }

    @Test
    void testRegisterNewUser() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .phone("0123456789")
                .password("password123")
                .build();

        // Act
        var response = userService.registerUser(request);

        // Assert
        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("0123456789", response.getPhone());
        assertEquals("ROLE_CUSTOMER", response.getRole());
        assertNotNull(response.getToken());
    }

    @Test
    void testRegisterDuplicateUsername() {
        // Arrange
        RegisterRequest request1 = RegisterRequest.builder()
                .username("duplicate")
                .email("email1@example.com")
                .phone("0111111111")
                .password("password123")
                .build();

        RegisterRequest request2 = RegisterRequest.builder()
                .username("duplicate")
                .email("email2@example.com")
                .phone("0222222222")
                .password("password123")
                .build();

        userService.registerUser(request1);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> userService.registerUser(request2));
    }

    @Test
    void testRegisterDuplicateEmail() {
        // Arrange
        RegisterRequest request1 = RegisterRequest.builder()
                .username("user1")
                .email("duplicate@example.com")
                .phone("0111111111")
                .password("password123")
                .build();

        RegisterRequest request2 = RegisterRequest.builder()
                .username("user2")
                .email("duplicate@example.com")
                .phone("0222222222")
                .password("password123")
                .build();

        userService.registerUser(request1);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> userService.registerUser(request2));
    }

    @Test
    void testAdminLogin() {
        // Act
        var response = userService.authenticateUser("admin", "admin123");

        // Assert
        assertNotNull(response);
        assertEquals("admin", response.getUsername());
        assertEquals("ROLE_ADMIN", response.getRole());
        assertNotNull(response.getToken());
    }

    @Test
    void testCustomerLogin() {
        // Arrange - Register a customer first
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("customer1")
                .email("customer@example.com")
                .phone("0333333333")
                .password("customer123")
                .build();
        userService.registerUser(registerRequest);

        // Act - Login with username
        var response = userService.authenticateUser("customer1", "customer123");

        // Assert
        assertNotNull(response);
        assertEquals("customer1", response.getUsername());
        assertEquals("ROLE_CUSTOMER", response.getRole());
        assertNotNull(response.getToken());
    }

    @Test
    void testLoginWithEmail() {
        // Arrange - Register a customer
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("customer2")
                .email("customer2@example.com")
                .phone("0444444444")
                .password("password456")
                .build();
        userService.registerUser(registerRequest);

        // Act - Login with email
        var response = userService.authenticateUser("customer2@example.com", "password456");

        // Assert
        assertNotNull(response);
        assertEquals("customer2", response.getUsername());
        assertEquals("customer2@example.com", response.getEmail());
        assertNotNull(response.getToken());
    }

    @Test
    void testGetCurrentUserInfo() {
        // Arrange - Register a customer
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("customer3")
                .email("customer3@example.com")
                .phone("0555555555")
                .password("pass789")
                .build();
        userService.registerUser(registerRequest);

        // Act
        var userInfo = userService.getCurrentUserInfo("customer3");

        // Assert
        assertNotNull(userInfo);
        assertEquals("customer3", userInfo.getUsername());
        assertEquals("customer3@example.com", userInfo.getEmail());
        assertEquals("0555555555", userInfo.getPhone());
        assertEquals("ROLE_CUSTOMER", userInfo.getRole());
    }

    @Test
    void testPasswordIsEncoded() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username("encrypttest")
                .email("encrypt@example.com")
                .phone("0666666666")
                .password("plainpassword")
                .build();

        // Act
        userService.registerUser(request);
        User user = userService.findByUsername("encrypttest");

        // Assert
        assertNotEquals("plainpassword", user.getPassword());
        assertTrue(passwordEncoder.matches("plainpassword", user.getPassword()));
    }

    @Test
    void testInvalidLoginCredentials() {
        // Act & Assert
        assertThrows(Exception.class, () -> userService.authenticateUser("nonexistent", "password"));
    }

    @Test
    void testCheckUsernameExists() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username("existstest")
                .email("exists@example.com")
                .phone("0777777777")
                .password("pass")
                .build();
        userService.registerUser(request);

        // Act & Assert
        assertTrue(userService.existsByUsername("existstest"));
        assertFalse(userService.existsByUsername("notexist"));
    }

    @Test
    void testCheckEmailExists() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username("emailtest")
                .email("emailtest@example.com")
                .phone("0888888888")
                .password("pass")
                .build();
        userService.registerUser(request);

        // Act & Assert
        assertTrue(userService.existsByEmail("emailtest@example.com"));
        assertFalse(userService.existsByEmail("notexist@example.com"));
    }
}