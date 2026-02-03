package com.sergiocodev.app.dto.role;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignPermissionsRequest {

    @NotEmpty(message = "Debe proporcionar al menos un permiso")
    private Set<Long> permissionIds;
}
