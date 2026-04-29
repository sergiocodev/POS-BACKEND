package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Recupera todos los usuarios que no han sido marcados como eliminados lógicamente.
     * Esta consulta ignora cualquier usuario cuyo campo deletedAt contenga un valor.
     * 
     * @return Lista de usuarios activos.
     */
    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE u.deletedAt IS NULL")
    Page<User> findAllActive(Pageable pageable);

    /**
     * Busca un usuario específico por su nombre de usuario, siempre y cuando
     * no haya sido eliminado lógicamente. Útil para la autenticación estándar.
     * 
     * @param username El nombre de usuario a buscar.
     * @return Usuario envuelto en un Optional si está activo y existe.
     */
    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE u.username = :username AND u.deletedAt IS NULL")
    Optional<User> findActiveByUsername(@Param("username") String username);

    /**
     * Busca un usuario por su dirección de correo electrónico, asegurando
     * que esté activo (no eliminado). Utilizado para recuperaciones o logins alternativos.
     * 
     * @param email El correo electrónico a buscar.
     * @return Usuario envuelto en Optional si está activo y existe.
     */
    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    java.util.Optional<User> findActiveByEmail(String email);

    /**
     * Busca un usuario basándose en su nombre de usuario O su correo electrónico.
     * Es ideal para el login donde el cliente puede ingresar cualquiera de ambos datos.
     * 
     * @param username El nombre de usuario.
     * @param email El correo electrónico.
     * @return Usuario si existe bajo alguno de estos identificadores y no está eliminado.
     */
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
     * Verifica si existe un usuario activo con el nombre de usuario dado
     */
    @org.springframework.data.jpa.repository.Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.username = :username AND u.deletedAt IS NULL")
    boolean existsActiveByUsername(@org.springframework.data.repository.query.Param("username") String username);

    /**
     * Verifica si existe un usuario activo con el email dado
     */
    @org.springframework.data.jpa.repository.Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    boolean existsActiveByEmail(@org.springframework.data.repository.query.Param("email") String email);

    /**
     * Verifica si existe algún usuario con el rol dado
     */
    boolean existsByRoles_Id(Long roleId);
}
