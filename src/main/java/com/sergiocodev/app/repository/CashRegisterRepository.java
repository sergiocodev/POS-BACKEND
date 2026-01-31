package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.CashRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CashRegisterRepository extends JpaRepository<CashRegister, Long> {
    List<CashRegister> findByEstablishmentId(Long establishmentId);
}
