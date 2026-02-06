package com.sergiocodev.app.dto.role;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record AssignPermissionsRequest(
        @NotEmpty(message = "Debe proporcionar al menos un permiso") Set<Long> permissionIds) {
}
