package com.sergiocodev.app.service.impl;

import com.sergiocodev.app.config.JwtUtil;
import com.sergiocodev.app.dto.user.LoginRequest;
import com.sergiocodev.app.dto.user.LoginResponse;
import com.sergiocodev.app.dto.user.RefreshTokenRequest;
import com.sergiocodev.app.dto.user.RegisterRequest;
import com.sergiocodev.app.exception.UserAlreadyExistsException;
import com.sergiocodev.app.exception.UserNotFoundException;
import com.sergiocodev.app.model.User;
import com.sergiocodev.app.repository.UserRepository;
import com.sergiocodev.app.service.interfaces.AuthService;
import com.sergiocodev.app.service.interfaces.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final TokenService tokenService;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.tokenService = tokenService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String usernameOrEmail = request.usernameOrEmail();
        log.debug("Login attempt: user={}", usernameOrEmail);

        User user = userRepository.findActiveByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found or deleted: " + usernameOrEmail));

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        request.password()));

        user.setLastLogin(Instant.now());
        userRepository.save(user);

        log.debug("Login successful: user={}, id={}", user.getUsername(), user.getId());

        String token = tokenService.generateAccessToken(user.getUsername());
        String refreshToken = tokenService.generateRefreshToken(user.getUsername());

        Set<String> rolesNames = user.getRoles().stream()
                .map(role -> "ROLE_" + role.getName())
                .collect(Collectors.toSet());

        Set<String> permissionNames = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(com.sergiocodev.app.model.Permission::getName)
                .collect(Collectors.toSet());

        return new LoginResponse(
                token,
                refreshToken,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getProfilePicture(),
                rolesNames,
                permissionNames);
    }

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        log.info("Registration attempt: username={}, email={}", request.username(), request.email());

        if (userRepository.existsActiveByUsername(request.username())) {
            throw new UserAlreadyExistsException(
                    "Username '" + request.username() + "' already exists");
        }

        if (userRepository.existsActiveByEmail(request.email())) {
            throw new UserAlreadyExistsException(
                    "Email '" + request.email() + "' is already registered");
        }

        User newUser = new User();
        newUser.setUsername(request.username());
        newUser.setEmail(request.email());
        newUser.setFullName(request.fullName());
        newUser.setProfilePicture(request.profilePicture());
        newUser.setPasswordHash(passwordEncoder.encode(request.password()));

        User savedUser = userRepository.save(newUser);
        log.info("User registered successfully: username={}, id={}", savedUser.getUsername(), savedUser.getId());

        String token = tokenService.generateAccessToken(savedUser.getUsername());
        String refreshToken = tokenService.generateRefreshToken(savedUser.getUsername());

        Set<String> roles = Collections.emptySet();
        Set<String> permissions = Collections.emptySet();

        return new LoginResponse(
                token,
                refreshToken,
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getProfilePicture(),
                roles,
                permissions);
    }

    @Override
    public LoginResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();
        String username = jwtUtil.extractUsername(refreshToken);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtUtil.validateToken(refreshToken, userDetails)) {
            throw new RuntimeException("Invalid refresh token");
        }

        User user = userRepository.findActiveByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UserNotFoundException("User not found or deleted"));

        String newToken = tokenService.generateAccessToken(username);
        String newRefreshToken = tokenService.generateRefreshToken(username);

        Set<String> rolesNames = user.getRoles().stream()
                .map(role -> "ROLE_" + role.getName())
                .collect(Collectors.toSet());

        Set<String> permissionNames = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(com.sergiocodev.app.model.Permission::getName)
                .collect(Collectors.toSet());

        return new LoginResponse(
                newToken,
                newRefreshToken,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getProfilePicture(),
                rolesNames,
                permissionNames);
    }

    @Override
    @Transactional
    public void logout(String token) {
        tokenService.invalidateToken(token);
    }

    @Override
    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findActiveByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UserNotFoundException("User not found or deleted"));
    }
}
