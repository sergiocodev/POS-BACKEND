package com.sergiocodev.app.config;

import com.sergiocodev.app.model.Usuario;
import com.sergiocodev.app.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + username));

        if (!usuario.isActive()) {
            throw new org.springframework.security.authentication.DisabledException("El usuario está inactivo");
        }

        java.util.List<org.springframework.security.core.GrantedAuthority> authorities = new java.util.ArrayList<>();

        // Agregar roles
        usuario.getRoles().forEach(role -> {
            authorities.add(
                    new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role.getName()));
            // Agregar permisos del rol
            role.getPermissions().forEach(permission -> {
                authorities.add(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority(permission.getName()));
            });
        });

        return new User(
                usuario.getUsername(),
                usuario.getPasswordHash(),
                authorities);
    }
}
