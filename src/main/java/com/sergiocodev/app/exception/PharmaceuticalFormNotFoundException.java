package com.sergiocodev.app.exception;

public class PharmaceuticalFormNotFoundException extends RuntimeException {

    public PharmaceuticalFormNotFoundException(String message) {
        super(message);
    }

    public PharmaceuticalFormNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
