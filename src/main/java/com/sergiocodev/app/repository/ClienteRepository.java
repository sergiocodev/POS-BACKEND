package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByDni(String dni);

    boolean existsByDni(String dni);

    Optional<Cliente> findByRuc(String ruc);

    boolean existsByRuc(String ruc);
}
