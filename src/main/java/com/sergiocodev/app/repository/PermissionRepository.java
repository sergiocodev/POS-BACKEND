package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de permisos individuales del sistema.
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    /**
     * Busca un permiso por su nombre técnico único.
     */
    Optional<Permission> findByName(String name);

    /**
     * Lista todos los permisos pertenecientes a un módulo específico.
     * @param module Nombre del módulo.
     */
    List<Permission> findByModule(String module);

    /**
     * Búsqueda general por nombre o descripción para filtrado en interfaces de gestión.
     */
    List<Permission> findByNameContainingOrDescriptionContaining(String name, String description);
}
