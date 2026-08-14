package com.sergiocodev.app.aspect;

import com.sergiocodev.app.annotation.RequiresPermission;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
public class RequiresPermissionAspect {

    @Around("@within(com.sergiocodev.app.annotation.RequiresPermission) || " +
            "@annotation(com.sergiocodev.app.annotation.RequiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        RequiresPermission annotation = resolveAnnotation(joinPoint);
        if (annotation == null) {
            return joinPoint.proceed();
        }

        String requiredPermission = annotation.value();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Access denied: no authenticated user for permission {}", requiredPermission);
            throw new AccessDeniedException("Se requiere autenticación para acceder a este recurso");
        }

        boolean hasPermission = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> 
                        authority.equalsIgnoreCase(requiredPermission) ||
                        authority.equalsIgnoreCase("ROLE_" + requiredPermission) ||
                        (requiredPermission.startsWith("ROLE_") && authority.equalsIgnoreCase(requiredPermission.substring(5)))
                );

        if (!hasPermission) {
            log.warn("Access denied: user {} lacks permission or role {}",
                    authentication.getName(), requiredPermission);
            throw new AccessDeniedException("No tienes permiso para acceder a este recurso");
        }

        log.debug("Permission granted: {} for user {}", requiredPermission, authentication.getName());
        return joinPoint.proceed();
    }

    /**
     * Resuelve la anotación {@link RequiresPermission} priorizando el nivel de método
     * sobre el de clase. Usa {@link Inherited} para que clases hijas hereden la anotación
     * de la clase padre si no está presente a nivel de método.
     */
    private RequiresPermission resolveAnnotation(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // Method-level takes precedence
        RequiresPermission methodAnnotation = method.getAnnotation(RequiresPermission.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }

        // Class-level (with @Inherited, also covers superclasses)
        return joinPoint.getTarget().getClass().getAnnotation(RequiresPermission.class);
    }
}
