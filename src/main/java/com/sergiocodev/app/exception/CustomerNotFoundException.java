package com.sergiocodev.app.exception;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String mensaje) {
        super(mensaje);
    }

    public CustomerNotFoundException(Long id) {
        super("Customer with ID " + id + " not found");
    }
}
