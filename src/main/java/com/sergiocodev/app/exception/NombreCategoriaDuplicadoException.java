package com.sergiocodev.app.exception;

public class NombreCategoriaDuplicadoException extends RuntimeException {

    public NombreCategoriaDuplicadoException(String nombreCategoria) {
        super("Ya existe una categoría con el nombre: " + nombreCategoria);
    }
}
