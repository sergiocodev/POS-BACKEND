package com.sergiocodev.app.dto.product;

/**
 * Detalle de error en una fila específica durante la importación masiva.
 */
public record BulkImportRowError(
        int rowNumber,
        String code,
        String tradeName,
        String errorMessage) {
}
