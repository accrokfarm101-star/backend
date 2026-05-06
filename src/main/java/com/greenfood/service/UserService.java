package com.greenfood.service;

import com.greenfood.model.dto.request.RegisterRequest;
import com.greenfood.model.dto.response.AuthResponse;
import com.greenfood.model.dto.response.UserInfoResponse;
import com.greenfood.model.entity.User;

public interface UserService {

    AuthResponse registerUser(RegisterRequest registerRequest);

    AuthResponse authenticateUser(String usernameOrEmail, String password);

    User findByUsername(String username);

    UserInfoResponse getCurrentUserInfo(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}