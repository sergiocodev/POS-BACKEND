package com.sergiocodev.app.dto;

import lombok.Builder;

import java.util.Map;

@Builder
public record ErrorResponse(
    int status,
    String message,
    String timestamp,
    String error,
    String path,
    Map<String, String> details
) {
    public static ErrorResponse fromException(Exception ex, int status, String path) {
        return ErrorResponse.builder()
            .status(status)
            .message(ex.getMessage())
            .timestamp(java.time.LocalDateTime.now().toString())
            .error(ex.getClass().getSimpleName())
            .path(path)
            .details(Map.of())
            .build();
    }
}