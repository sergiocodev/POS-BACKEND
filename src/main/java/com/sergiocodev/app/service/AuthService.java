package com.sergiocodev.app.service;

import com.sergiocodev.app.config.JwtUtil;
import com.sergiocodev.app.dto.usuario.LoginRequest;
import com.sergiocodev.app.dto.usuario.LoginResponse;
import com.sergiocodev.app.dto.usuario.RegistroRequest;
import com.sergiocodev.app.exception.UsuarioDuplicadoException;
import com.sergiocodev.app.exception.UsuarioNoEncontradoException;
import com.sergiocodev.app.model.Usuario;
import com.sergiocodev.app.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

        private final UsuarioRepository usuarioRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtUtil jwtUtil;
        private final AuthenticationManager authenticationManager;

        /**
         * Autentica un usuario y genera un token JWT
         */
        @Transactional
        public LoginResponse login(LoginRequest request) {
                // El request ahora tiene usernameOrEmail
                String usernameOrEmail = request.getUsernameOrEmail();

                // Buscar el usuario primero
                Usuario usuario = usuarioRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                                .orElseThrow(() -> new UsuarioNoEncontradoException(
                                                "Usuario no encontrado: " + usernameOrEmail));

                // Verificar si está activo
                if (!usuario.isActive()) {
                        throw new RuntimeException("El usuario está inactivo");
                }

                // Autenticar con Spring Security (usamos el username real del usuario
                // encontrado)
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                usuario.getUsername(),
                                                request.getPassword()));

                // Actualizar último login
                usuario.setLastLogin(java.time.LocalDateTime.now());
                usuarioRepository.save(usuario);

                // Generar token JWT
                String token = jwtUtil.generateToken(usuario.getUsername());

                // Obtener nombres de roles y permisos
                java.util.Set<String> rolesNames = usuario.getRoles().stream()
                                .map(role -> "ROLE_" + role.getName())
                                .collect(java.util.stream.Collectors.toSet());

                java.util.Set<String> permissionNames = usuario.getRoles().stream()
                                .flatMap(role -> role.getPermissions().stream())
                                .map(com.sergiocodev.app.model.Permiso::getName)
                                .collect(java.util.stream.Collectors.toSet());

                // Retornar respuesta con token y datos del usuario
                return new LoginResponse(
                                token,
                                usuario.getId(),
                                usuario.getUsername(),
                                usuario.getEmail(),
                                usuario.getFullName(),
                                rolesNames,
                                permissionNames);
        }

        /**
         * Registra un nuevo usuario
         */
        @Transactional
        public LoginResponse registrar(RegistroRequest request) {
                // Verificar si el usuario ya existe
                if (usuarioRepository.existsByUsername(request.getUsername())) {
                        throw new UsuarioDuplicadoException(
                                        "El usuario '" + request.getUsername() + "' ya existe");
                }

                // Verificar si el email ya existe
                if (usuarioRepository.existsByEmail(request.getEmail())) {
                        throw new UsuarioDuplicadoException(
                                        "El email '" + request.getEmail() + "' ya está registrado");
                }

                // Crear nuevo usuario
                Usuario nuevoUsuario = new Usuario();
                nuevoUsuario.setUsername(request.getUsername());
                nuevoUsuario.setEmail(request.getEmail());
                nuevoUsuario.setFullName(request.getFullName());
                nuevoUsuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
                nuevoUsuario.setActive(true);

                // Guardar en la base de datos
                Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

                // Generar token JWT
                String token = jwtUtil.generateToken(usuarioGuardado.getUsername());

                // Roles vacíos por defecto (en un sistema real se asignaría uno)
                java.util.Set<String> roles = java.util.Collections.emptySet();
                java.util.Set<String> permissions = java.util.Collections.emptySet();

                // Retornar respuesta con token y datos del usuario
                return new LoginResponse(
                                token,
                                usuarioGuardado.getId(),
                                usuarioGuardado.getUsername(),
                                usuarioGuardado.getEmail(),
                                usuarioGuardado.getFullName(),
                                roles,
                                permissions);
        }
}
