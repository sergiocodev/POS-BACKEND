package com.sergiocodev.app.validation;

import com.sergiocodev.app.repository.RoleRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class RoleIdsValidator implements ConstraintValidator<ValidRoleIds, Set<Long>> {

    private final RoleRepository roleRepository;

    @Override
    public boolean isValid(Set<Long> roleIds, ConstraintValidatorContext context) {
        if (roleIds == null || roleIds.isEmpty()) {
            return true; // Validación manejada por @NotNull/@NotEmpty
        }
        return roleRepository.countByIdIn(roleIds) == roleIds.size();
    }
}