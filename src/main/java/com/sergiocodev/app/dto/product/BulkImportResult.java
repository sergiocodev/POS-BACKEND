package com.sergiocodev.app.dto.product;

import java.util.List;

/**
 * Resultado de la importación masiva de productos.
 */
public record BulkImportResult(
        int totalRows,
        int createdCount,
        int updatedCount,
        int errorCount,
        List<BulkImportRowError> errors) {
}
