package com.sergiocodev.app.service.impl;

import com.sergiocodev.app.dto.user.UserRequest;
import com.sergiocodev.app.dto.user.UserResponse;
import com.sergiocodev.app.exception.ResourceNotFoundException;
import com.sergiocodev.app.exception.UserAlreadyExistsException;
import com.sergiocodev.app.exception.UserNotFoundException;
import com.sergiocodev.app.mapper.UserMapper;
import com.sergiocodev.app.model.Role;
import com.sergiocodev.app.model.User;
import com.sergiocodev.app.repository.RoleRepository;
import com.sergiocodev.app.repository.UserRepository;
import com.sergiocodev.app.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;

    @Value("${app.default.role}")
    private String defaultRoleName;

    @Value("${app.timezone}")
    private String timezone;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAll(Pageable pageable) {
        return userRepository.findAllActive(pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllPaged(String username, String email, String fullName, Pageable pageable) {
        return userRepository.findAllActiveFiltered(username, email, fullName, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return userRepository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getByUsername(String username) {
        return userRepository.findActiveByUsername(username)
                .map(mapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }

    @Override
    @Transactional
    public UserResponse create(UserRequest request) {
        if (userRepository.existsActiveByUsername(request.username())) {
            throw new UserAlreadyExistsException("Username already exists: " + request.username());
        }
        if (userRepository.existsActiveByEmail(request.email())) {
            throw new UserAlreadyExistsException("Email already exists: " + request.email());
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setProfilePicture(request.profilePicture());
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        if (request.roleIds() != null && !request.roleIds().isEmpty()) {
            validateRoles(request.roleIds());
            user.setRoles(new HashSet<>(roleRepository.findAllById(request.roleIds())));
        } else {
            Role defaultRole = roleRepository.findByName(defaultRoleName)
                    .orElseThrow(() -> new ResourceNotFoundException("Default role '" + defaultRoleName + "' not found"));
            user.setRoles(Set.of(defaultRole));
        }

        return mapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (!user.getUsername().equals(request.username())
                && userRepository.existsActiveByUsername(request.username())) {
            throw new UserAlreadyExistsException("Username already exists: " + request.username());
        }
        if (!user.getEmail().equals(request.email()) && userRepository.existsActiveByEmail(request.email())) {
            throw new UserAlreadyExistsException("Email already exists: " + request.email());
        }

        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setProfilePicture(request.profilePicture());

        if (request.password() != null && !request.password().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        if (request.roleIds() != null) {
            validateRoles(request.roleIds());
            user.setRoles(new HashSet<>(roleRepository.findAllById(request.roleIds())));
        }

        return mapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public UserResponse toggleActive(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        if (user.getDeletedAt() == null) {
            user.setDeletedAt(LocalDateTime.now().atZone(ZoneId.of(timezone)).toInstant());
        } else {
            user.setDeletedAt(null);
        }
        return mapper.toResponse(userRepository.save(user));
    }

    private void validateRoles(Set<Long> roleIds) {
        Set<Role> roles = new HashSet<>(roleRepository.findAllById(roleIds));
        if (roles.size() != roleIds.size()) {
            throw new ResourceNotFoundException("One or more roles not found");
        }
    }
}
