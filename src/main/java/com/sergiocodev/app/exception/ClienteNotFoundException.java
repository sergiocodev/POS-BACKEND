package com.sergiocodev.app.exception;

public class ClienteNotFoundException extends RuntimeException {

    public ClienteNotFoundException(String mensaje) {
        super(mensaje);
    }

    public ClienteNotFoundException(Long id) {
        super("Cliente con ID " + id + " no encontrado");
    }
}
