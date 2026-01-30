package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su nombre de usuario
     */
    Optional<Usuario> findByUsername(String username);

    /**
     * Busca un usuario por su email
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Busca un usuario por su username o email
     */
    Optional<Usuario> findByUsernameOrEmail(String username, String email);

    /**
     * Verifica si existe un usuario con el nombre de usuario dado
     */
    boolean existsByUsername(String username);

    /**
     * Verifica si existe un usuario con el email dado
     */
    boolean existsByEmail(String email);
}
