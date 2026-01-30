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
        @Transactional(readOnly = true)
        public LoginResponse login(LoginRequest request) {
                // Autenticar con Spring Security
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getUsuario(),
                                                request.getPassword()));

                // Buscar el usuario en la base de datos
                Usuario usuario = usuarioRepository.findByUsuario(request.getUsuario())
                                .orElseThrow(() -> new UsuarioNoEncontradoException(
                                                "Usuario no encontrado: " + request.getUsuario()));

                // Generar token JWT
                String token = jwtUtil.generateToken(usuario.getUsuario());

                // Retornar respuesta con token y datos del usuario
                return new LoginResponse(
                                token,
                                usuario.getId(),
                                usuario.getUsuario(),
                                usuario.getNombre());
        }

        /**
         * Registra un nuevo usuario
         */
        @Transactional
        public LoginResponse registrar(RegistroRequest request) {
                // Verificar si el usuario ya existe
                if (usuarioRepository.existsByUsuario(request.getUsuario())) {
                        throw new UsuarioDuplicadoException(
                                        "El usuario '" + request.getUsuario() + "' ya existe");
                }

                // Crear nuevo usuario
                Usuario nuevoUsuario = new Usuario();
                nuevoUsuario.setUsuario(request.getUsuario());
                nuevoUsuario.setNombre(request.getNombre());
                nuevoUsuario.setTelefono(request.getTelefono());
                nuevoUsuario.setPassword(passwordEncoder.encode(request.getPassword()));

                // Guardar en la base de datos
                Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

                // Generar token JWT
                String token = jwtUtil.generateToken(usuarioGuardado.getUsuario());

                // Retornar respuesta con token y datos del usuario
                return new LoginResponse(
                                token,
                                usuarioGuardado.getId(),
                                usuarioGuardado.getUsuario(),
                                usuarioGuardado.getNombre());
        }
}
