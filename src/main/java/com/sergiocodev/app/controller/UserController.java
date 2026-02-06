package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.external.ExternalConsultationResponse;
import com.sergiocodev.app.dto.user.UserRequest;
import com.sergiocodev.app.model.User;
import com.sergiocodev.app.service.ExternalConsultationService;
import com.sergiocodev.app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Users", description = "Endpoints para la gestión de usuarios")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final ExternalConsultationService externalConsultationService;

    @Operation(summary = "Lista de usuarios", description = "Obtiene la lista de todos los usuarios registrados")
    @ApiResponse(responseCode = "200", description = "List of users obtained successfully")
    @GetMapping
    public ResponseEntity<ResponseApi<List<User>>> getAll() {
        List<User> users = userService.getAll();
        // Hide passwords
        users.forEach(u -> u.setPasswordHash("***"));
        return ResponseEntity.ok(ResponseApi.success(users));
    }

    @Operation(summary = "Obtener perfil", description = "Obtiene el perfil del usuario autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile obtained successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/profile")
    public ResponseEntity<ResponseApi<User>> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getByUsername(username);

        // Hide password
        user.setPasswordHash("***");

        return ResponseEntity.ok(ResponseApi.success(user));
    }

    @Operation(summary = "Obtener usuario por ID", description = "Obtiene un usuario específico por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseApi<User>> getById(@PathVariable Long id) {
        User user = userService.getById(id);

        // Hide password
        user.setPasswordHash("***");

        return ResponseEntity.ok(ResponseApi.success(user));
    }

    @Operation(summary = "Crear usuario", description = "Crea un nuevo usuario (ADMIN)")
    @PostMapping
    public ResponseEntity<ResponseApi<User>> create(@Valid @RequestBody UserRequest request) {
        User user = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseApi.success(user, "Usuario creado exitosamente"));
    }

    @Operation(summary = "Actualizar usuario", description = "Actualiza un usuario existente")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseApi<User>> update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        User user = userService.update(id, request);
        return ResponseEntity.ok(ResponseApi.success(user, "Usuario actualizado exitosamente"));
    }

    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario por su ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseApi<Void>> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok(ResponseApi.success(null, "Usuario eliminado exitosamente"));
    }

    @Operation(summary = "Alternar estado activo", description = "Activa o desactiva un usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User status toggled successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ResponseApi<User>> toggleActive(@PathVariable Long id) {
        User user = userService.toggleActive(id);
        // Hide password
        user.setPasswordHash("***");
        return ResponseEntity.ok(ResponseApi.success(user, "Estado de usuario actualizado exitosamente"));
    }

    @Operation(summary = "Consulta externa de documento", description = "Busca datos de una persona (DNI) o empresa (RUC) en servicios externos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data found"),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    @GetMapping("/search/{numeroDocumento}")
    public ResponseEntity<ResponseApi<ExternalConsultationResponse>> searchByDocument(
            @PathVariable String numeroDocumento) {
        ExternalConsultationResponse response = externalConsultationService.searchByDocument(numeroDocumento);
        return ResponseEntity.ok(ResponseApi.success(response));
    }
}
