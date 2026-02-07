package com.sergiocodev.app.exception;

public class StockInsufficientException extends RuntimeException {
    public StockInsufficientException(String message) {
        super(message);
    }
}
