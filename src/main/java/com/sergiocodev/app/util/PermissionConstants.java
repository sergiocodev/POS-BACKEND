package com.sergiocodev.app.util;

/**
 * Constantes de permisos del sistema basados en módulos
 * Cada permiso representa acceso completo a un módulo
 */
public final class PermissionConstants {

    private PermissionConstants() {
        throw new UnsupportedOperationException("Esta es una clase de utilidad y no puede ser instanciada");
    }

    // Módulos principales del sistema
    public static final String DASHBOARD = "DASHBOARD";
    public static final String VENTAS = "VENTAS";
    public static final String FACTURACION = "FACTURACION";
    public static final String INVENTARIO = "INVENTARIO";
    public static final String COMPRAS = "COMPRAS";
    public static final String CAJA = "CAJA";
    public static final String FARMACIA = "FARMACIA";
    public static final String CONFIGURACION = "CONFIGURACION";
}
