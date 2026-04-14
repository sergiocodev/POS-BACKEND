package com.sergiocodev.app.service;

import com.sergiocodev.app.model.CashConcept;
import com.sergiocodev.app.repository.CashConceptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CashConceptServiceTest {

    @Mock
    private CashConceptRepository cashConceptRepository;

    @InjectMocks
    private CashConceptService cashConceptService;

    private List<CashConcept> emptyInList;
    private List<CashConcept> emptyOutList;

    @BeforeEach
    void setUp() {
        emptyInList = Collections.emptyList();
        emptyOutList = Collections.emptyList();
    }

    // ==================== findOrCreateSaleConcept TESTS ====================

    @Test
    void findOrCreateSaleConcept_shouldCreateNewConcept_whenNoneExists() {
        when(cashConceptRepository.findByType(CashConcept.ConceptType.IN))
                .thenReturn(emptyInList);
        when(cashConceptRepository.save(any(CashConcept.class)))
                .thenAnswer(inv -> {
                    CashConcept c = inv.getArgument(0);
                    c.setId(1L);
                    return c;
                });

        CashConcept result = cashConceptService.findOrCreateSaleConcept("EFECTIVO");

        assertNotNull(result);
        assertEquals("VENTA EFECTIVO", result.getName());
        assertEquals(CashConcept.ConceptType.IN, result.getType());
        assertTrue(result.getIsSystem());
        verify(cashConceptRepository).save(any(CashConcept.class));
    }

    @Test
    void findOrCreateSaleConcept_shouldReturnExistingConcept_whenFoundWithName() {
        CashConcept existing = new CashConcept();
        existing.setId(1L);
        existing.setName("VENTA EFECTIVO");
        existing.setType(CashConcept.ConceptType.IN);
        existing.setIsSystem(true);

        when(cashConceptRepository.findByType(CashConcept.ConceptType.IN))
                .thenReturn(List.of(existing));

        CashConcept result = cashConceptService.findOrCreateSaleConcept("EFECTIVO");

        assertNotNull(result);
        assertEquals("VENTA EFECTIVO", result.getName());
        verify(cashConceptRepository, never()).save(any());
    }

    @Test
    void findOrCreateSaleConcept_shouldCreateNewConcept_whenNoMatchingVentaFound() {
        CashConcept unrelated = new CashConcept();
        unrelated.setId(1L);
        unrelated.setName("OTRO CONCEPTO");
        unrelated.setType(CashConcept.ConceptType.IN);

        when(cashConceptRepository.findByType(CashConcept.ConceptType.IN))
                .thenReturn(List.of(unrelated))
                .thenReturn(List.of(unrelated));
        when(cashConceptRepository.save(any(CashConcept.class)))
                .thenAnswer(inv -> {
                    CashConcept c = inv.getArgument(0);
                    c.setId(2L);
                    return c;
                });

        CashConcept result = cashConceptService.findOrCreateSaleConcept("TARJETA");

        assertNotNull(result);
        assertEquals("VENTA TARJETA", result.getName());
        verify(cashConceptRepository, times(2)).findByType(CashConcept.ConceptType.IN);
    }

    // ==================== findOrCreatePurchaseConcept TESTS ====================

    @Test
    void findOrCreatePurchaseConcept_shouldCreateNewConcept_whenNoneExists() {
        when(cashConceptRepository.findByType(CashConcept.ConceptType.OUT))
                .thenReturn(emptyOutList);
        when(cashConceptRepository.save(any(CashConcept.class)))
                .thenAnswer(inv -> {
                    CashConcept c = inv.getArgument(0);
                    c.setId(1L);
                    return c;
                });

        CashConcept result = cashConceptService.findOrCreatePurchaseConcept("EFECTIVO");

        assertNotNull(result);
        assertEquals("COMPRA EFECTIVO", result.getName());
        assertEquals(CashConcept.ConceptType.OUT, result.getType());
        assertTrue(result.getIsSystem());
        verify(cashConceptRepository).save(any(CashConcept.class));
    }

    @Test
    void findOrCreatePurchaseConcept_shouldReturnExistingConcept_whenFoundWithName() {
        CashConcept existing = new CashConcept();
        existing.setId(1L);
        existing.setName("COMPRA TARJETA");
        existing.setType(CashConcept.ConceptType.OUT);
        existing.setIsSystem(true);

        when(cashConceptRepository.findByType(CashConcept.ConceptType.OUT))
                .thenReturn(List.of(existing));

        CashConcept result = cashConceptService.findOrCreatePurchaseConcept("TARJETA");

        assertNotNull(result);
        assertEquals("COMPRA TARJETA", result.getName());
        verify(cashConceptRepository, never()).save(any());
    }

    // ==================== findOrCreateReceivableConcept TESTS ====================

    @Test
    void findOrCreateReceivableConcept_shouldCreateNewConcept_whenNoneExists() {
        when(cashConceptRepository.findByType(CashConcept.ConceptType.IN))
                .thenReturn(emptyInList);
        when(cashConceptRepository.save(any(CashConcept.class)))
                .thenAnswer(inv -> {
                    CashConcept c = inv.getArgument(0);
                    c.setId(1L);
                    return c;
                });

        CashConcept result = cashConceptService.findOrCreateReceivableConcept("TRANSFERENCIA");

        assertNotNull(result);
        assertEquals("COBRO TRANSFERENCIA", result.getName());
        assertEquals(CashConcept.ConceptType.IN, result.getType());
        assertTrue(result.getIsSystem());
        verify(cashConceptRepository).save(any(CashConcept.class));
    }

    @Test
    void findOrCreateReceivableConcept_shouldReturnExistingConcept_whenFoundWithCobro() {
        CashConcept existing = new CashConcept();
        existing.setId(1L);
        existing.setName("COBRO EFECTIVO");
        existing.setType(CashConcept.ConceptType.IN);
        existing.setIsSystem(true);

        when(cashConceptRepository.findByType(CashConcept.ConceptType.IN))
                .thenReturn(List.of(existing));

        CashConcept result = cashConceptService.findOrCreateReceivableConcept("EFECTIVO");

        assertNotNull(result);
        assertEquals("COBRO EFECTIVO", result.getName());
        verify(cashConceptRepository, never()).save(any());
    }

    // ==================== findOrCreatePayableConcept TESTS ====================

    @Test
    void findOrCreatePayableConcept_shouldCreateNewConcept_whenNoneExists() {
        when(cashConceptRepository.findByType(CashConcept.ConceptType.OUT))
                .thenReturn(emptyOutList);
        when(cashConceptRepository.save(any(CashConcept.class)))
                .thenAnswer(inv -> {
                    CashConcept c = inv.getArgument(0);
                    c.setId(1L);
                    return c;
                });

        CashConcept result = cashConceptService.findOrCreatePayableConcept("CHEQUE");

        assertNotNull(result);
        assertEquals("PAGO CHEQUE", result.getName());
        assertEquals(CashConcept.ConceptType.OUT, result.getType());
        assertTrue(result.getIsSystem());
        verify(cashConceptRepository).save(any(CashConcept.class));
    }

    @Test
    void findOrCreatePayableConcept_shouldReturnExistingConcept_whenFoundWithPago() {
        CashConcept existing = new CashConcept();
        existing.setId(1L);
        existing.setName("PAGO TARJETA");
        existing.setType(CashConcept.ConceptType.OUT);
        existing.setIsSystem(true);

        when(cashConceptRepository.findByType(CashConcept.ConceptType.OUT))
                .thenReturn(List.of(existing));

        CashConcept result = cashConceptService.findOrCreatePayableConcept("TARJETA");

        assertNotNull(result);
        assertEquals("PAGO TARJETA", result.getName());
        verify(cashConceptRepository, never()).save(any());
    }

    // ==================== findOrCreateByType TESTS ====================

    @Test
    void findOrCreateByType_shouldCreateNewConcept_whenNoneExists() {
        when(cashConceptRepository.findByType(CashConcept.ConceptType.IN))
                .thenReturn(emptyInList);
        when(cashConceptRepository.save(any(CashConcept.class)))
                .thenAnswer(inv -> {
                    CashConcept c = inv.getArgument(0);
                    c.setId(1L);
                    return c;
                });

        CashConcept result = cashConceptService.findOrCreateByType(CashConcept.ConceptType.IN, "DONACION");

        assertNotNull(result);
        assertEquals("DONACION", result.getName());
        assertEquals(CashConcept.ConceptType.IN, result.getType());
        assertTrue(result.getIsSystem());
        verify(cashConceptRepository).save(any(CashConcept.class));
    }

    @Test
    void findOrCreateByType_shouldReturnExistingConcept_whenFound() {
        CashConcept existing = new CashConcept();
        existing.setId(1L);
        existing.setName("DONACION ESPECIAL");
        existing.setType(CashConcept.ConceptType.IN);

        when(cashConceptRepository.findByType(CashConcept.ConceptType.IN))
                .thenReturn(List.of(existing));

        CashConcept result = cashConceptService.findOrCreateByType(CashConcept.ConceptType.IN, "DONACION");

        assertNotNull(result);
        assertEquals("DONACION ESPECIAL", result.getName());
        verify(cashConceptRepository, never()).save(any());
    }

    @Test
    void findOrCreateByType_shouldCreateWithUpperCaseName() {
        when(cashConceptRepository.findByType(CashConcept.ConceptType.OUT))
                .thenReturn(emptyOutList);
        when(cashConceptRepository.save(any(CashConcept.class)))
                .thenAnswer(inv -> {
                    CashConcept c = inv.getArgument(0);
                    c.setId(1L);
                    return c;
                });

        CashConcept result = cashConceptService.findOrCreateByType(CashConcept.ConceptType.OUT, "gasto menor");

        assertNotNull(result);
        assertEquals("GASTO MENOR", result.getName());
    }

    // ==================== findByType TESTS ====================

    @Test
    void findByType_shouldReturnListFromRepository() {
        List<CashConcept> concepts = List.of(new CashConcept());
        when(cashConceptRepository.findByType(CashConcept.ConceptType.IN))
                .thenReturn(concepts);

        List<CashConcept> result = cashConceptService.findByType(CashConcept.ConceptType.IN);

        assertEquals(1, result.size());
        verify(cashConceptRepository).findByType(CashConcept.ConceptType.IN);
    }

    @Test
    void findByType_shouldReturnEmptyList_whenNoConcepts() {
        when(cashConceptRepository.findByType(CashConcept.ConceptType.OUT))
                .thenReturn(Collections.emptyList());

        List<CashConcept> result = cashConceptService.findByType(CashConcept.ConceptType.OUT);

        assertTrue(result.isEmpty());
    }
}
