package com.sergiocodev.app.exception;

public class CategoriaNotFoundException extends RuntimeException {

    public CategoriaNotFoundException(String mensaje) {
        super(mensaje);
    }

    public CategoriaNotFoundException(Long id) {
        super("Categoría con ID " + id + " no encontrada");
    }
}
