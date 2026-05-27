package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.dto.permission.CreatePermissionRequest;
import com.sergiocodev.app.dto.permission.PermissionResponse;

import java.util.List;
import java.util.Map;

public interface PermissionService {

    List<PermissionResponse> getAll();

    List<PermissionResponse> getByModule(String module);

    List<PermissionResponse> search(String query);

    Map<String, List<PermissionResponse>> getGroupedByModule();

    List<String> getModules();

    PermissionResponse getById(Long id);

    PermissionResponse create(CreatePermissionRequest request);

    PermissionResponse update(Long id, CreatePermissionRequest request);

    void delete(Long id);
}
