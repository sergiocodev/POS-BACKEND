package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.dto.user.LoginRequest;
import com.sergiocodev.app.dto.user.LoginResponse;
import com.sergiocodev.app.dto.user.RefreshTokenRequest;
import com.sergiocodev.app.dto.user.RegisterRequest;
import com.sergiocodev.app.model.User;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginResponse register(RegisterRequest request);

    LoginResponse refresh(RefreshTokenRequest request);

    void logout(String token);

    User getCurrentUser();
}
