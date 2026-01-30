package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su nombre de usuario
     * 
     * @param usuario nombre de usuario
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> findByUsuario(String usuario);

    /**
     * Verifica si existe un usuario con el nombre de usuario dado
     * 
     * @param usuario nombre de usuario
     * @return true si existe, false si no
     */
    boolean existsByUsuario(String usuario);
}
