package com.sergiocodev.app.exception;

public class NombreMarcaDuplicadoException extends RuntimeException {

    public NombreMarcaDuplicadoException(String nombreMarca) {
        super("Ya existe una marca con el nombre: " + nombreMarca);
    }
}
