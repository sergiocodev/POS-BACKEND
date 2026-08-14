package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.employee.EmployeeRequest;
import com.sergiocodev.app.dto.employee.EmployeeResponse;
import com.sergiocodev.app.service.interfaces.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sergiocodev.app.annotation.RequiresPermission;
import com.sergiocodev.app.util.PermissionConstants;
import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employees", description = "Endpoints para la gestión de empleados")
@SecurityRequirement(name = "bearerAuth")
@RequiresPermission(PermissionConstants.CONFIGURACION_PERSONAL)
public class EmployeeController {

    private final EmployeeService service;

    @PostMapping
    @Operation(summary = "Crear empleado")
    public ResponseEntity<ResponseApi<EmployeeResponse>> create(@Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(service.create(request), "Empleado creado exitosamente"));
    }

    @GetMapping
    @Operation(summary = "Listar empleados")
    public ResponseEntity<ResponseApi<List<EmployeeResponse>>> getAll() {
        return ResponseEntity.ok(ResponseApi.success(service.getAll()));
    }

    @Operation(summary = "Lista de empleados con paginación", description = "Obtiene la lista de empleados paginada y filtrada")
    @GetMapping("/paged")
    public ResponseEntity<ResponseApi<org.springframework.data.domain.Page<EmployeeResponse>>> getAllPaged(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String documentNumber,
            @RequestParam(required = false) String username,
            @org.springframework.data.web.PageableDefault(size = 10, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC) org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<EmployeeResponse> employees = service.getAllPaged(fullName, documentNumber, username, pageable);
        return ResponseEntity.ok(ResponseApi.success(employees));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener empleado por ID")
    public ResponseEntity<ResponseApi<EmployeeResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseApi.success(service.getById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar empleado")
    public ResponseEntity<ResponseApi<EmployeeResponse>> update(@PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(ResponseApi.success(service.update(id, request), "Empleado actualizado exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar empleado")
    public ResponseEntity<ResponseApi<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ResponseApi.success(null, "Empleado eliminado exitosamente"));
    }
}
