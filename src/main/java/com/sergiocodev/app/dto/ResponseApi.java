package com.sergiocodev.app.dto;

import lombok.Builder;

@Builder
public record ResponseApi<T>(
        int status,
        String message,
        T data) {
    public static <T> ResponseApi<T> success(T data, String message) {
        return ResponseApi.<T>builder()
                .status(200)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ResponseApi<T> success(T data) {
        return success(data, "Operación exitosa");
    }

    public static <T> ResponseApi<T> error(int status, String message) {
        return ResponseApi.<T>builder()
                .status(status)
                .message(message)
                .build();
    }
}
