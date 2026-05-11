package com.freshmart.service;

import com.freshmart.model.dto.request.RegisterRequest;
import com.freshmart.model.dto.response.AuthResponse;
import com.freshmart.model.dto.response.UserInfoResponse;
import com.freshmart.model.entity.User;

public interface UserService {

    AuthResponse registerUser(RegisterRequest registerRequest);

    AuthResponse authenticateUser(String usernameOrEmail, String password);

    User findByUsername(String username);

    UserInfoResponse getCurrentUserInfo(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
