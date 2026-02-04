package com.sergiocodev.app.service;

import com.sergiocodev.app.config.JwtUtil;
import com.sergiocodev.app.dto.user.LoginRequest;
import com.sergiocodev.app.dto.user.LoginResponse;
import com.sergiocodev.app.dto.user.RefreshTokenRequest;
import com.sergiocodev.app.dto.user.RegisterRequest;
import com.sergiocodev.app.exception.UserAlreadyExistsException;
import com.sergiocodev.app.exception.UserNotFoundException;
import com.sergiocodev.app.model.User;
import com.sergiocodev.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtUtil jwtUtil;
        private final AuthenticationManager authenticationManager;
        private final UserDetailsService userDetailsService;

        /**
         * Authenticates a user and generates a JWT token
         */
        @Transactional
        public LoginResponse login(LoginRequest request) {
                String usernameOrEmail = request.getUsernameOrEmail();

                // Find user by username or email
                User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                                .orElseThrow(() -> new UserNotFoundException(
                                                "User not found: " + usernameOrEmail));

                // Check if user is active
                if (!user.isActive()) {
                        throw new RuntimeException("User is inactive");
                }

                // Authenticate with Spring Security
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                user.getUsername(),
                                                request.getPassword()));

                // Update last login
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);

                // Generate JWT token
                String token = jwtUtil.generateToken(user.getUsername());
                String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

                // Get role names and permissions
                Set<String> rolesNames = user.getRoles().stream()
                                .map(role -> "ROLE_" + role.getName())
                                .collect(Collectors.toSet());

                Set<String> permissionNames = user.getRoles().stream()
                                .flatMap(role -> role.getPermissions().stream())
                                .map(com.sergiocodev.app.model.Permission::getName)
                                .collect(Collectors.toSet());

                // Return token and user data
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

        /**
         * Registers a new user
         */
        @Transactional
        public LoginResponse register(RegisterRequest request) {
                // Check if username already exists
                if (userRepository.existsByUsername(request.getUsername())) {
                        throw new UserAlreadyExistsException(
                                        "Username '" + request.getUsername() + "' already exists");
                }

                // Check if email already exists
                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new UserAlreadyExistsException(
                                        "Email '" + request.getEmail() + "' is already registered");
                }

                // Create new user
                User newUser = new User();
                newUser.setUsername(request.getUsername());
                newUser.setEmail(request.getEmail());
                newUser.setFullName(request.getFullName());
                newUser.setProfilePicture(request.getProfilePicture());
                newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
                newUser.setActive(true);

                // Save to database
                User savedUser = userRepository.save(newUser);

                // Generate JWT token
                String token = jwtUtil.generateToken(savedUser.getUsername());
                String refreshToken = jwtUtil.generateRefreshToken(savedUser.getUsername());

                // Empty roles by default
                Set<String> roles = Collections.emptySet();
                Set<String> permissions = Collections.emptySet();

                // Return token and user data
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

        /**
         * Refreshes the JWT token
         */
        public LoginResponse refresh(RefreshTokenRequest request) {
                String refreshToken = request.getRefreshToken();
                String username = jwtUtil.extractUsername(refreshToken);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (!jwtUtil.validateToken(refreshToken, userDetails)) {
                        throw new RuntimeException("Invalid refresh token");
                }

                User user = userRepository.findByUsernameOrEmail(username, username)
                                .orElseThrow(() -> new UserNotFoundException("User not found"));

                String newToken = jwtUtil.generateToken(username);
                // Optionally generate a new refresh token or return the same one
                // For this implementation, we'll return the same refresh token to not force
                // re-login too often,
                // or we could rotate it. Let's rotate it for better security.
                String newRefreshToken = jwtUtil.generateRefreshToken(username);

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

        /**
         * Logs out the user
         */
        public void logout() {
                // Since we are using stateless JWT, we don't need to do anything server-side
                // unless we implement a token blacklist.
                // For now, the client just discards the token.
        }

        /**
         * Gets the current authenticated user
         */
        public User getCurrentUser() {
                String username = SecurityContextHolder.getContext().getAuthentication().getName();
                return userRepository.findByUsernameOrEmail(username, username)
                                .orElseThrow(() -> new UserNotFoundException("User not found"));
        }
}
