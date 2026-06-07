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

    // Submódulos de VENTAS
    public static final String VENTAS_POS = "VENTAS_POS";
    public static final String VENTAS_LISTA = "VENTAS_LISTA";
    public static final String VENTAS_CLIENTES = "VENTAS_CLIENTES";
    public static final String VENTAS_CUENTAS_COBRAR = "VENTAS_CUENTAS_COBRAR";
    public static final String VENTAS_REPORTES = "VENTAS_REPORTES";

    // Submódulos de FACTURACION
    public static final String FACTURACION_COMPROBANTES = "FACTURACION_COMPROBANTES";
    public static final String FACTURACION_BAJAS = "FACTURACION_BAJAS";
    public static final String FACTURACION_NOTAS = "FACTURACION_NOTAS";

    // Submódulos de INVENTARIO
    public static final String INVENTARIO_CATALOGO = "INVENTARIO_CATALOGO";
    public static final String INVENTARIO_ACTUAL = "INVENTARIO_ACTUAL";
    public static final String INVENTARIO_LOTES = "INVENTARIO_LOTES";
    public static final String INVENTARIO_MOVIMIENTOS = "INVENTARIO_MOVIMIENTOS";
    public static final String INVENTARIO_TRANSFERENCIAS = "INVENTARIO_TRANSFERENCIAS";

    // Submódulos de COMPRAS
    public static final String COMPRAS_NUEVA = "COMPRAS_NUEVA";
    public static final String COMPRAS_LISTA = "COMPRAS_LISTA";
    public static final String COMPRAS_PROVEEDORES = "COMPRAS_PROVEEDORES";
    public static final String COMPRAS_CUENTAS_PAGAR = "COMPRAS_CUENTAS_PAGAR";
    public static final String COMPRAS_REPORTES = "COMPRAS_REPORTES";

    // Submódulos de CAJA
    public static final String CAJA_APERTURA_CIERRE = "CAJA_APERTURA_CIERRE";
    public static final String CAJA_MOVIMIENTOS = "CAJA_MOVIMIENTOS";
    public static final String CAJA_REGISTRADORAS = "CAJA_REGISTRADORAS";
    public static final String CAJA_REPORTES = "CAJA_REPORTES";

    // Submódulos de FARMACIA
    public static final String FARMACIA_PRINCIPIOS_ACTIVOS = "FARMACIA_PRINCIPIOS_ACTIVOS";
    public static final String FARMACIA_LABORATORIOS = "FARMACIA_LABORATORIOS";
    public static final String FARMACIA_MARCAS = "FARMACIA_MARCAS";
    public static final String FARMACIA_CATEGORIAS = "FARMACIA_CATEGORIAS";
    public static final String FARMACIA_PRESENTACIONES = "FARMACIA_PRESENTACIONES";
    public static final String FARMACIA_FORMAS = "FARMACIA_FORMAS";
    public static final String FARMACIA_ACCIONES = "FARMACIA_ACCIONES";

    // Submódulos de CONFIGURACION
    public static final String CONFIGURACION_USUARIOS = "CONFIGURACION_USUARIOS";
    public static final String CONFIGURACION_ROLES = "CONFIGURACION_ROLES";
    public static final String CONFIGURACION_ESTABLECIMIENTOS = "CONFIGURACION_ESTABLECIMIENTOS";
    public static final String CONFIGURACION_PERSONAL = "CONFIGURACION_PERSONAL";
    public static final String CONFIGURACION_IMPUESTOS = "CONFIGURACION_IMPUESTOS";
    public static final String CONFIGURACION_EMPRESA = "CONFIGURACION_EMPRESA";
}
