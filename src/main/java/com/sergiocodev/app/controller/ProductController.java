package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.product.BulkImportResult;
import com.sergiocodev.app.dto.product.ProductRequest;
import com.sergiocodev.app.dto.product.ProductResponse;
import com.sergiocodev.app.service.interfaces.ProductService;
import com.sergiocodev.app.service.impl.ProductBulkImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.sergiocodev.app.annotation.RequiresPermission;
import com.sergiocodev.app.util.PermissionConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.sergiocodev.app.config.ApiVersion;
import java.util.List;

import com.sergiocodev.app.dto.productlot.ProductLotResponse;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Endpoints para la gestión de productos")
@SecurityRequirement(name = "bearerAuth")
@RequiresPermission(PermissionConstants.INVENTARIO_CATALOGO)
@ApiVersion(1)
public class ProductController {

    private final ProductService service;
    private final ProductBulkImportService bulkImportService;

    @PostMapping
    @Operation(summary = "Crear producto")
    public ResponseEntity<ResponseApi<ProductResponse>> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(service.create(request), "Producto creado exitosamente"));
    }

    @PostMapping("/with-ingredients")
    @Operation(summary = "Crear nuevo producto (Admin)", description = "Crea un nuevo producto, incluyendo ingredientes si se proporcionan")
    public ResponseEntity<ResponseApi<ProductResponse>> createNewProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(service.createNewProduct(request), "Producto creado exitosamente"));
    }

    @GetMapping
    @Operation(summary = "Listar productos con filtros", description = "Obtiene la lista de productos, opcionalmente filtrada por categoría, marca o estado")
    public ResponseEntity<ResponseApi<List<ProductResponse>>> getAll(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId) {
        return ResponseEntity.ok(ResponseApi.success(service.getAll(categoryId, brandId)));
    }

    @GetMapping("/paged")
    @Operation(summary = "Listar productos paginados", description = "Retorna una lista paginada de todos los productos.")
    public ResponseEntity<ResponseApi<Page<ProductResponse>>> getPaged(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String tradeName,
            @RequestParam(required = false) String therapeuticActionNames,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String brandName,
            @RequestParam(required = false) String laboratoryName,
            Pageable pageable) {
        return ResponseEntity.ok(ResponseApi.success(service.findAllPaged(code, tradeName, therapeuticActionNames, categoryName, brandName, laboratoryName, pageable)));
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar productos (POS)", description = "Busca productos por código de barras, nombre o principio activo")
    public ResponseEntity<ResponseApi<List<ProductResponse>>> search(@RequestParam String query) {
        return ResponseEntity.ok(ResponseApi.success(service.search(query)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID")
    public ResponseEntity<ResponseApi<ProductResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseApi.success(service.getById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto")
    public ResponseEntity<ResponseApi<ProductResponse>> update(@PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(ResponseApi.success(service.update(id, request), "Producto actualizado exitosamente"));
    }

    @GetMapping("/{id}/lots")
    @Operation(summary = "Ver lotes asociados", description = "Obtiene los lotes asociados a un producto")
    public ResponseEntity<ResponseApi<List<ProductLotResponse>>> getLots(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseApi.success(service.getLots(id)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cambiar estado del producto")
    public ResponseEntity<ResponseApi<ProductResponse>> toggleStatus(@PathVariable Long id) {
        return ResponseEntity
                .ok(ResponseApi.success(service.toggleStatus(id), "Estado del producto actualizado exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar producto")
    public ResponseEntity<ResponseApi<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ResponseApi.success(null, "Producto eliminado exitosamente"));
    }

    // ======== IMPORTACIÓN MASIVA ========

    @GetMapping("/bulk-import/template")
    @Operation(summary = "Descargar plantilla Excel", description = "Descarga un archivo .xlsx con la plantilla para importación masiva de productos")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] template = bulkImportService.generateTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=plantilla_productos.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(template);
    }

    @PostMapping(value = "/bulk-import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar productos desde Excel", description = "Sube un archivo .xlsx con productos para importar masivamente")
    public ResponseEntity<ResponseApi<BulkImportResult>> bulkImport(@RequestParam("file") MultipartFile file) {
        BulkImportResult result = bulkImportService.importFromExcel(file);
        String message = String.format("Importación completada: %d creados, %d actualizados, %d errores",
                result.createdCount(), result.updatedCount(), result.errorCount());
        return ResponseEntity.ok(ResponseApi.success(result, message));
    }
}

