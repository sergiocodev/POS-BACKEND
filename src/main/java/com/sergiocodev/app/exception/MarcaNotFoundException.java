package com.sergiocodev.app.exception;

public class MarcaNotFoundException extends RuntimeException {

    public MarcaNotFoundException(String mensaje) {
        super(mensaje);
    }

    public MarcaNotFoundException(Long id) {
        super("Marca con ID " + id + " no encontrada");
    }
}
