package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.user.LoginRequest;
import com.sergiocodev.app.dto.user.LoginResponse;
import com.sergiocodev.app.dto.user.RefreshTokenRequest;
import com.sergiocodev.app.dto.user.RegisterRequest;
import com.sergiocodev.app.service.AuthService;
import com.sergiocodev.app.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "Endpoints para la autenticación y registro de usuarios")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Iniciar sesión", description = "Autentica a un usuario y devuelve un token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful login"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<ResponseApi<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ResponseApi.success(response, "Inicio de sesión exitoso"));
    }

    @Operation(summary = "Registrar usuario", description = "Registra un nuevo usuario en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data or user already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<ResponseApi<LoginResponse>> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(response, "Usuario registrado exitosamente"));
    }

    @Operation(summary = "Refrescar token", description = "Obtiene un nuevo token de acceso usando un refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<ResponseApi<LoginResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refresh(request);
        return ResponseEntity.ok(ResponseApi.success(response, "Token refrescado exitosamente"));
    }

    @Operation(summary = "Cerrar sesión", description = "Cierra la sesión del usuario (lado del cliente)")
    @PostMapping("/logout")
    public ResponseEntity<ResponseApi<Void>> logout() {
        authService.logout();
        return ResponseEntity.ok(ResponseApi.success(null, "Sesión cerrada exitosamente"));
    }

    @Operation(summary = "Obtener usuario actual", description = "Devuelve los datos del usuario autenticado actualmente")
    @GetMapping("/me")
    public ResponseEntity<ResponseApi<User>> getCurrentUser() {
        User user = authService.getCurrentUser();
        return ResponseEntity.ok(ResponseApi.success(user));
    }

}
