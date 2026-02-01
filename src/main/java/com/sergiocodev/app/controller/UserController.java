package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.user.UserRequest;
import com.sergiocodev.app.model.User;
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

    @Operation(summary = "Lista de usuarios", description = "Obtiene la lista de todos los usuarios registrados")
    @ApiResponse(responseCode = "200", description = "List of users obtained successfully")
    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        List<User> users = userService.getAll();
        // Hide passwords
        users.forEach(u -> u.setPasswordHash("***"));
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Obtener perfil", description = "Obtiene el perfil del usuario autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile obtained successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/profile")
    public ResponseEntity<User> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getByUsername(username);

        // Hide password
        user.setPasswordHash("***");

        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Obtener usuario por ID", description = "Obtiene un usuario específico por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        User user = userService.getById(id);

        // Hide password
        user.setPasswordHash("***");

        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Crear usuario", description = "Crea un nuevo usuario (ADMIN)")
    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody UserRequest request) {
        User user = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @Operation(summary = "Actualizar usuario", description = "Actualiza un usuario existente")
    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        User user = userService.update(id, request);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario por su ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
