package com.sergiocodev.app.exception;

public class UsuarioDuplicadoException extends RuntimeException {

    public UsuarioDuplicadoException(String mensaje) {
        super(mensaje);
    }

    public UsuarioDuplicadoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
