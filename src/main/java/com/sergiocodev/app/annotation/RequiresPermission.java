package com.sergiocodev.app.annotation;

import java.lang.annotation.*;

/**
 * Anotación personalizada para verificar permisos a nivel de controlador o método.
 * Reemplaza el patrón {@code @PreAuthorize("hasAuthority('PERMISO')")}
 * con una sintaxis más limpia y segura ante refactors:
 * <pre>{@code
 * // A nivel de clase (aplica a todos los métodos)
 * @RequiresPermission(PermissionConstants.VENTAS_POS)
 * public class SaleController { ... }
 *
 * // A nivel de método (sobrescribe la de clase)
 * @RequiresPermission(PermissionConstants.VENTAS_LISTA)
 * public ResponseEntity getAll() { ... }
 * }</pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RequiresPermission {
    String value();
}
