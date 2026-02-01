package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Kardex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KardexRepository extends JpaRepository<Kardex, Long> {
    List<Kardex> findByProductIdOrderByCreatedAtDesc(Long productId);
}
