package com.sergiocodev.app.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Validates API version from request header against controller annotation.
 * Accepts versions via header: X-API-Version: 1
 * Defaults to version 1 if header is not present.
 */
@Component
public class ApiVersionInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ApiVersionInterceptor.class);
    private static final String HEADER_NAME = "X-API-Version";
    private static final int CURRENT_VERSION = 1;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        ApiVersion versionAnnotation = handlerMethod.getBeanType().getAnnotation(ApiVersion.class);
        if (versionAnnotation == null) {
            return true; // No versioning on this controller
        }

        String versionHeader = request.getHeader(HEADER_NAME);
        int requestedVersion = versionHeader != null ? parseIntSafe(versionHeader) : CURRENT_VERSION;
        int controllerVersion = versionAnnotation.value();

        // Add version to response headers
        response.setHeader("X-API-Version", String.valueOf(controllerVersion));
        response.setHeader("X-API-Supported-Versions", String.valueOf(controllerVersion));

        if (requestedVersion != controllerVersion) {
            log.warn("API version mismatch: requested={}, supported={}", requestedVersion, controllerVersion);
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            response.setHeader("Content-Type", "application/json");
            try {
                response.getWriter().write(
                        "{\"status\":406,\"message\":\"API version \" + requestedVersion + \" is not supported. Current version: " + controllerVersion + "\"}");
            } catch (Exception e) {
                log.error("Failed to write version error response", e);
            }
            return false;
        }

        return true;
    }

    private int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return CURRENT_VERSION;
        }
    }
}
