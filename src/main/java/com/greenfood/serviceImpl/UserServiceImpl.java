package com.greenfood.serviceImpl;

import com.greenfood.exception.ResourceNotFoundException;
import com.greenfood.model.dto.request.RegisterRequest;
import com.greenfood.model.dto.response.AuthResponse;
import com.greenfood.model.dto.response.UserInfoResponse;
import com.greenfood.model.entity.User;
import com.greenfood.model.enums.UserRole;
import com.greenfood.repository.UserRepository;
import com.greenfood.service.UserService;
import com.greenfood.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public AuthResponse registerUser(RegisterRequest registerRequest) {
        if (existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Tên người dùng đã tồn tại");
        }
        if (existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .phone(registerRequest.getPhone())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(UserRole.ROLE_CUSTOMER)
                .build();

        userRepository.saveAndFlush(user);

        String token = jwtUtils.generateTokenFromUsername(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public AuthResponse authenticateUser(String usernameOrEmail, String password) {
        User user = userRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> userRepository.findByEmail(usernameOrEmail)
                        .orElseThrow(() -> new BadCredentialsException("Invalid username/email or password")));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid username/email or password");
        }

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        null,
                        List.of(new SimpleGrantedAuthority(user.getRole().name()))
                )
        );

        String token = jwtUtils.generateTokenFromUsername(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public UserInfoResponse getCurrentUserInfo(String username) {
        User user = findByUsername(username);
        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .build();
    }
}