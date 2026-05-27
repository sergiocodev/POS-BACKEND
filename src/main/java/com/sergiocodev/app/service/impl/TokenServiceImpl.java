package com.sergiocodev.app.service.impl;

import com.sergiocodev.app.config.JwtUtil;
import com.sergiocodev.app.model.TokenBlacklist;
import com.sergiocodev.app.repository.TokenBlacklistRepository;
import com.sergiocodev.app.service.interfaces.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
    private final JwtUtil jwtUtil;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Override
    @Transactional
    public void invalidateToken(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        try {
            String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
            String jti = jwtUtil.extractJti(jwt);
            String username = jwtUtil.extractUsername(jwt);
            java.util.Date expiration = jwtUtil.extractExpiration(jwt);

            if (jti != null && !tokenBlacklistRepository.existsByJti(jti)) {
                TokenBlacklist entry = new TokenBlacklist();
                entry.setJti(jti);
                entry.setUsername(username);
                entry.setExpiryDate(expiration.toInstant());
                entry.setBlacklistedAt(Instant.now());
                tokenBlacklistRepository.save(entry);
            }
        } catch (Exception e) {
            // Log the error without exposing details
        }
    }

    @Override
    public String generateAccessToken(String username) {
        return jwtUtil.generateToken(username);
    }

    @Override
    public String generateRefreshToken(String username) {
        return jwtUtil.generateRefreshToken(username);
    }
}
