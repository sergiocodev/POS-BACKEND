package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.CashConcept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para definir conceptos de entrada/salida de dinero en caja.
 */
@Repository
public interface CashConceptRepository extends JpaRepository<CashConcept, Long> {
    /**
     * Lista conceptos por tipo (Entrada o Salida).
     * @param type Tipo de concepto.
     * @return Lista de conceptos.
     */
    List<CashConcept> findByType(CashConcept.ConceptType type);

    /**
     * Filtra conceptos por tipo y si son definidos por el sistema.
     * @param type Tipo de concepto.
     * @param isSystem true si es un concepto reservado del sistema.
     * @return Lista de conceptos filtrados.
     */
    List<CashConcept> findByTypeAndIsSystem(CashConcept.ConceptType type, Boolean isSystem);
}
