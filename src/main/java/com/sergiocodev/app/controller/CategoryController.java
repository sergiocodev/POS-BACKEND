package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.category.CategoryRequest;
import com.sergiocodev.app.dto.category.CategoryResponse;
import com.sergiocodev.app.service.interfaces.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sergiocodev.app.annotation.RequiresPermission;
import com.sergiocodev.app.util.PermissionConstants;
import java.util.List;

@RestController
@RequestMapping("/api/v1/category")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Endpoints para la gestión de categorías")
@SecurityRequirement(name = "bearerAuth")
@RequiresPermission(PermissionConstants.FARMACIA_CATEGORIAS)
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Crear categoría", description = "Crea una nueva categoría en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Category created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Duplicate category name")
    })
    @PostMapping("/CreateNewCategory")
    public ResponseEntity<ResponseApi<CategoryResponse>> createNewCategory(
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createNewCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(response, "Categoría creada exitosamente"));
    }

    @Operation(summary = "Listar categorías", description = "Obtiene la lista de todas las categorías registradas")
    @ApiResponse(responseCode = "200", description = "List of categories obtained successfully")
    @GetMapping("/GetAllCategory")
    public ResponseEntity<ResponseApi<List<CategoryResponse>>> getAllCategory() {
        List<CategoryResponse> categories = categoryService.getAllCategory();
        return ResponseEntity.ok(ResponseApi.success(categories));
    }

    @Operation(summary = "Listar categorías paginadas", description = "Retorna una lista paginada de categorías.")
    @GetMapping("/paged")
    public ResponseEntity<ResponseApi<Page<CategoryResponse>>> getPaged(
            @RequestParam(required = false) String name,
            Pageable pageable) {
        return ResponseEntity.ok(ResponseApi.success(categoryService.findAllPaged(name, pageable)));
    }

    @Operation(summary = "Obtener categoría por ID", description = "Obtiene una categoría específica por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/GetCategoryById/{id}")
    public ResponseEntity<ResponseApi<CategoryResponse>> getCategoryById(@PathVariable Long id) {
        CategoryResponse response = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ResponseApi.success(response));
    }

    @Operation(summary = "Actualizar categoría", description = "Actualiza los datos de una categoría existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate category name")
    })
    @PutMapping("/UpdateCategoryById/{id}")
    public ResponseEntity<ResponseApi<CategoryResponse>> updateCategoryById(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.updateCategoryById(id, request);
        return ResponseEntity.ok(ResponseApi.success(response, "Categoría actualizada exitosamente"));
    }

    @Operation(summary = "Eliminar categoría", description = "Elimina una categoría existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @DeleteMapping("/DeleteCategoryById/{id}")
    public ResponseEntity<ResponseApi<Void>> deleteCategoryById(@PathVariable Long id) {
        categoryService.deleteCategoryById(id);
        return ResponseEntity.ok(ResponseApi.success(null, "Categoría eliminada exitosamente"));
    }
}
