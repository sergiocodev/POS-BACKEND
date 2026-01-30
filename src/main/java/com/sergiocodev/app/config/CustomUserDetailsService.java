package com.sergiocodev.app.config;

import com.sergiocodev.app.exception.UsuarioNoEncontradoException;
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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var usuario = usuarioRepository.findByUsuario(username)
                .orElseThrow(() -> new UsuarioNoEncontradoException(
                        "Usuario no encontrado: " + username));

        return new User(
                usuario.getUsuario(),
                usuario.getPassword(),
                new ArrayList<>() // Sin roles por ahora
        );
    }
}
