package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE u.deletedAt IS NULL")
    java.util.List<User> findAllActive();

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE u.username = :username AND u.deletedAt IS NULL")
    java.util.Optional<User> findActiveByUsername(String username);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    java.util.Optional<User> findActiveByEmail(String email);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE (u.username = :username OR u.email = :email) AND u.deletedAt IS NULL")
    java.util.Optional<User> findActiveByUsernameOrEmail(String username, String email);

    /**
     * Busca un usuario por su nombre de usuario
     */
    Optional<User> findByUsername(String username);

    /**
     * Busca un usuario por su email
     */
    Optional<User> findByEmail(String email);

    /**
     * Busca un usuario por su username o email
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    /**
     * Verifica si existe un usuario con el nombre de usuario dado
     */
    boolean existsByUsername(String username);

    /**
     * Verifica si existe un usuario con el email dado
     */
    boolean existsByEmail(String email);

    /**
     * Verifica si existe algún usuario con el rol dado
     */
    boolean existsByRoles_Id(Long roleId);
}
