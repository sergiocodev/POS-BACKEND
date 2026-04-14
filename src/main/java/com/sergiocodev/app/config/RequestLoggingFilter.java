package com.sergiocodev.app.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Logs every HTTP request with timing, trace ID, and key headers.
 * Essential for production observability.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger("com.sergiocodev.app.access");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String traceId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("traceId", traceId);

        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
        String queryString = httpRequest.getQueryString();
        String userAgent = httpRequest.getHeader("User-Agent");
        String remoteAddr = httpRequest.getRemoteAddr();
        String authHeader = httpRequest.getHeader("Authorization");
        boolean isAuthenticated = authHeader != null && authHeader.startsWith("Bearer ");

        Instant start = Instant.now();
        try {
            chain.doFilter(request, response);
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            int status = httpResponse.getStatus();

            // Log all requests at INFO, errors at WARN
            String requestInfo = queryString != null ? uri + "?" + queryString : uri;
            String logMessage = String.format("%s %s %s %dms %s auth=%s ua=\"%s\"",
                    method, requestInfo, status, duration.toMillis(),
                    isAuthenticated ? "yes" : "no",
                    remoteAddr,
                    userAgent != null ? userAgent.substring(0, Math.min(80, userAgent.length())) : "unknown");

            if (status >= 500) {
                log.warn("REQUEST_ERROR: {}", logMessage);
            } else if (status >= 400) {
                log.info("REQUEST_WARN: {}", logMessage);
            } else {
                log.info("REQUEST: {}", logMessage);
            }

            // Add trace ID to response header for client correlation
            httpResponse.setHeader("X-Trace-Id", traceId);
            MDC.remove("traceId");
        }
    }
}
