package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);

    @Query("SELECT r FROM Role r WHERE r.deletedAt IS NULL AND (:name IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Role> findAllActiveFiltered(@Param("name") String name, Pageable pageable);

    @Query("SELECT COUNT(r) FROM Role r WHERE r.id IN :roleIds")
    long countByIdIn(@Param("roleIds") Set<Long> roleIds);

    boolean existsByPermissions_Id(Long permissionId);
}
