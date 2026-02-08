package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.user.UserRequest;
import com.sergiocodev.app.exception.UserAlreadyExistsException;
import com.sergiocodev.app.exception.UserNotFoundException;
import com.sergiocodev.app.model.Role;
import com.sergiocodev.app.model.User;
import com.sergiocodev.app.repository.RoleRepository;
import com.sergiocodev.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.sergiocodev.app.dto.user.UserResponse;
import com.sergiocodev.app.mapper.UserMapper;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;

    public List<UserResponse> getAll() {
        return userRepository.findAllActive().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getById(Long id) {
        return userRepository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    public UserResponse getByUsername(String username) {
        return userRepository.findActiveByUsername(username)
                .map(mapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException("Username already exists: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Email already exists: " + request.email());
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setProfilePicture(request.profilePicture());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setActive(request.active() != null ? request.active() : true);

        if (request.roleIds() != null && !request.roleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(request.roleIds()));
            user.setRoles(roles);
        }

        return mapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (!user.getUsername().equals(request.username())
                && userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException("Username already exists: " + request.username());
        }
        if (!user.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Email already exists: " + request.email());
        }

        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setProfilePicture(request.profilePicture());

        if (request.active() != null) {
            user.setActive(request.active());
        }

        if (request.password() != null && !request.password().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        if (request.roleIds() != null) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(request.roleIds()));
            user.setRoles(roles);
        }

        return mapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }

    @Transactional
    public UserResponse toggleActive(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        user.setActive(!user.isActive());
        return mapper.toResponse(userRepository.save(user));
    }
}
