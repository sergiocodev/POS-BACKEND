package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.dto.user.UserRequest;
import com.sergiocodev.app.dto.user.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    Page<UserResponse> getAll(Pageable pageable);

    UserResponse getById(Long id);

    UserResponse getByUsername(String username);

    UserResponse create(UserRequest request);

    UserResponse update(Long id, UserRequest request);

    void delete(Long id);

    UserResponse toggleActive(Long id);
}
