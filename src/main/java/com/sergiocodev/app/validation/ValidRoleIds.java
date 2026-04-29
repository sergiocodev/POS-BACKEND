package com.sergiocodev.app.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = RoleIdsValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRoleIds {
    String message() default "One or more roles not found";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}