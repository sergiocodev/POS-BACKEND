package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.inventory.InventoryResponse;
import com.sergiocodev.app.mapper.InventoryMapper;
import com.sergiocodev.app.model.*;
import com.sergiocodev.app.repository.*;
import com.sergiocodev.app.service.impl.InventoryServiceImpl;
import com.sergiocodev.app.service.interfaces.StockMovementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository repository;
    @Mock
    private EstablishmentRepository establishmentRepository;
    @Mock
    private ProductLotRepository lotRepository;
    @Mock
    private StockMovementRepository stockMovementRepository;
    @Mock
    private ProductUnitRepository productUnitRepository;
    @Mock
    private SaleRepository saleRepository;
    @Mock
    private StockMovementService stockMovementService;
    @Mock
    private PurchaseItemRepository purchaseItemRepository;

    @Spy
    private InventoryMapper mapper = new com.sergiocodev.app.mapper.InventoryMapperImpl();

    private InventoryServiceImpl service;

    private Establishment establishment;
    private Product product;
    private UnitOfMeasure uomCaja;
    private UnitOfMeasure uomUnidad;
    private ProductUnit unitCaja;
    private ProductUnit unitUnidad;
    private ProductLot lotDos;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mapper, "purchaseItemRepository", purchaseItemRepository);

        service = new InventoryServiceImpl(
                repository,
                establishmentRepository,
                lotRepository,
                stockMovementRepository,
                productUnitRepository,
                saleRepository,
                mapper,
                stockMovementService
        );

        establishment = new Establishment();
        establishment.setId(1L);
        establishment.setName("Sede Supte San Jorge");

        uomCaja = new UnitOfMeasure();
        uomCaja.setId(2L);
        uomCaja.setName("CAJA");

        uomUnidad = new UnitOfMeasure();
        uomUnidad.setId(1L);
        uomUnidad.setName("UNIDAD");

        product = new Product();
        product.setId(610L);
        product.setTradeName("AMOXICILINA");

        unitCaja = new ProductUnit();
        unitCaja.setId(4L);
        unitCaja.setProduct(product);
        unitCaja.setUnitOfMeasure(uomCaja);
        unitCaja.setFactor(60);
        unitCaja.setPrice(new BigDecimal("50.00"));
        unitCaja.setBaseUnit(false);

        unitUnidad = new ProductUnit();
        unitUnidad.setId(5L);
        unitUnidad.setProduct(product);
        unitUnidad.setUnitOfMeasure(uomUnidad);
        unitUnidad.setFactor(1);
        unitUnidad.setPrice(new BigDecimal("3.00"));
        unitUnidad.setBaseUnit(true);

        product.setUnits(new ArrayList<>(List.of(unitCaja, unitUnidad)));

        lotDos = new ProductLot();
        lotDos.setId(2L);
        lotDos.setProduct(product);
        lotDos.setLotCode("LTDOS");
        lotDos.setExpiryDate(LocalDate.of(2027, 5, 6));
    }

    @Test
    void getByEstablishmentPaged_shouldReturnOnlyPurchasedUnitsForLot() {
        Inventory inventory = new Inventory();
        inventory.setId(2L);
        inventory.setEstablishment(establishment);
        inventory.setLot(lotDos);
        inventory.setQuantity(new BigDecimal("2.00"));
        inventory.setCostPrice(new BigDecimal("3.00"));

        when(repository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(inventory)));

        // Simulating that lot LTDOS was purchased only as UNIDAD
        when(purchaseItemRepository.findUnitsByProductAndLotCode(610L, "LTDOS"))
                .thenReturn(List.of(unitUnidad));

        Page<InventoryResponse> result = service.getByEstablishmentPaged(
                1L, null, null, null, null, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        InventoryResponse response = result.getContent().get(0);
        assertEquals(2L, response.lotId());
        assertEquals("LTDOS", response.lotCode());
        assertEquals("UNIDAD", response.unitName());
        assertEquals(new BigDecimal("3.00"), response.salesPrice());

        // Verify units list contains ONLY UNIDAD (not CAJA)
        assertNotNull(response.units());
        assertEquals(1, response.units().size());
        assertEquals("UNIDAD", response.units().get(0).unitName());
        assertEquals(1, response.units().get(0).factor());
        assertEquals(new BigDecimal("3.00"), response.units().get(0).price());
    }

    @Test
    void getByEstablishmentPaged_shouldFallbackToProductUnitsWhenNoPurchaseItemsExist() {
        Inventory inventory = new Inventory();
        inventory.setId(3L);
        inventory.setEstablishment(establishment);
        inventory.setLot(lotDos);
        inventory.setQuantity(new BigDecimal("5.00"));

        when(repository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(inventory)));

        // No purchases registered for this lot
        when(purchaseItemRepository.findUnitsByProductAndLotCode(610L, "LTDOS"))
                .thenReturn(List.of());

        Page<InventoryResponse> result = service.getByEstablishmentPaged(
                1L, null, null, null, null, PageRequest.of(0, 10));

        assertNotNull(result);
        InventoryResponse response = result.getContent().get(0);

        // Fallback returns all product units
        assertEquals(2, response.units().size());
    }
}
