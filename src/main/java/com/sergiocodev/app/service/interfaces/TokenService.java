package com.sergiocodev.app.service.interfaces;

public interface TokenService {

    void invalidateToken(String token);

    String generateAccessToken(String username);

    String generateRefreshToken(String username);
}
