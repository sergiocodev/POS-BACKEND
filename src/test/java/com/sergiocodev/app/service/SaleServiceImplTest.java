package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.sale.SaleItemRequest;
import com.sergiocodev.app.dto.sale.SalePaymentRequest;
import com.sergiocodev.app.dto.sale.SaleRequest;
import com.sergiocodev.app.dto.sale.SaleResponse;
import com.sergiocodev.app.dto.sale.SaleItemResponse;
import com.sergiocodev.app.dto.sale.SalePaymentResponse;
import com.sergiocodev.app.exception.BadRequestException;
import com.sergiocodev.app.exception.ResourceNotFoundException;
import com.sergiocodev.app.exception.StockInsufficientException;
import com.sergiocodev.app.mapper.SaleMapper;
import com.sergiocodev.app.model.*;
import com.sergiocodev.app.repository.*;
import com.sergiocodev.app.util.XmlUblGenerator;
import com.sergiocodev.app.util.SunatOseClient;
import java.util.ArrayList;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleServiceImplTest {

    @Mock
    private SaleRepository repository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private EstablishmentRepository establishmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductLotRepository lotRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private CashSessionRepository cashSessionRepository;

    @Mock
    private ProductUnitRepository productUnitRepository;

    @Mock
    private AccountReceivableRepository accountReceivableRepository;

    @Mock
    private DocumentSequenceRepository documentSequenceRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private SaleMapper saleMapper;

    @Mock
    private XmlUblGenerator xmlUblGenerator;

    @Mock
    private DigitalSignatureService digitalSignatureService;

    @Mock
    private SunatOseClient sunatOseClient;

    @Mock
    private CashMovementService cashMovementService;

    @Mock
    private CashConceptService cashConceptService;

    @Mock
    private CashConceptRepository cashConceptRepository;

    @Mock
    private StockMovementService stockMovementService;

    @InjectMocks
    private SaleServiceImpl saleService;

    // Test fixtures
    private Establishment establishment;
    private User user;
    private Customer customer;
    private Product product;
    private ProductLot productLot;
    private ProductUnit productUnit;
    private TaxType taxType;
    private CashSession cashSession;
    private Inventory inventory;
    private CashConcept cashConcept;

    @BeforeEach
    void setUp() {
        // Establishment
        establishment = new Establishment();
        establishment.setId(1L);
        establishment.setName("Main Store");
        establishment.setAddress("123 Main St");

        // User
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setFullName("Test User");

        // Customer
        customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");
        customer.setDocumentNumber("12345678");
        customer.setDocumentType(DocumentType.DNI);

        // TaxType
        taxType = new TaxType();
        taxType.setId(1L);
        taxType.setName("IGV");
        taxType.setRate(new BigDecimal("0.18"));

        // Product
        product = new Product();
        product.setId(1L);
        product.setCode("PROD001");
        product.setTradeName("Test Product");
        product.setTaxType(taxType);

        // ProductLot
        productLot = new ProductLot();
        productLot.setId(1L);
        productLot.setLotCode("LOT001");
        productLot.setExpiryDate(LocalDate.now().plusYears(1));
        productLot.setProduct(product);

        // ProductUnit
        productUnit = new ProductUnit();
        productUnit.setId(1L);
        productUnit.setUnitName("Unit");
        productUnit.setFactor(1);
        productUnit.setPrice(new BigDecimal("10.00"));
        productUnit.setProduct(product);

        // CashSession
        cashSession = new CashSession();
        cashSession.setId(1L);
        cashSession.setUser(user);
        cashSession.setStatus(CashSession.SessionStatus.OPEN);
        cashSession.setOpeningBalance(new BigDecimal("100.00"));

        // Inventory
        inventory = new Inventory();
        inventory.setId(1L);
        inventory.setEstablishment(establishment);
        inventory.setLot(productLot);
        inventory.setQuantity(new BigDecimal("100.00"));

        // CashConcept
        cashConcept = new CashConcept();
        cashConcept.setId(1L);
        cashConcept.setName("VENTA EFECTIVO");
        cashConcept.setType(CashConcept.ConceptType.IN);
        cashConcept.setIsSystem(true);
    }

    // ==================== CREATE TESTS ====================

    @Nested
    @DisplayName("create() - Sale Creation")
    class CreateTests {

        @Test
        @DisplayName("Should throw ResourceNotFoundException when establishment not found")
        void create_ShouldThrowResourceNotFoundException_WhenEstablishmentNotFound() {
            // Arrange
            SaleRequest request = createSaleRequest();
            when(establishmentRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> saleService.create(request, 1L));

            assertEquals("Establishment not found: 1", exception.getMessage());
            verify(establishmentRepository).findById(1L);
            verifyNoMoreInteractions(establishmentRepository);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user not found")
        void create_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
            // Arrange
            SaleRequest request = createSaleRequest();
            Sale saleEntity = new Sale();
            saleEntity.setItems(new ArrayList<>());
            saleEntity.setPayments(new ArrayList<>());

            when(saleMapper.toEntity(request)).thenReturn(saleEntity);
            when(establishmentRepository.findById(1L)).thenReturn(Optional.of(establishment));
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> saleService.create(request, 1L));

            assertEquals("User not found: 1", exception.getMessage());
            verify(establishmentRepository).findById(1L);
            verify(userRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw BadRequestException when no active cash session")
        void create_ShouldThrowBadRequestException_WhenNoActiveCashSession() {
            // Arrange
            SaleRequest request = createSaleRequest();
            Sale saleEntity = new Sale();
            saleEntity.setItems(new ArrayList<>());
            saleEntity.setPayments(new ArrayList<>());

            when(saleMapper.toEntity(request)).thenReturn(saleEntity);
            when(establishmentRepository.findById(1L)).thenReturn(Optional.of(establishment));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(cashSessionRepository.findByUserIdAndStatus(1L, CashSession.SessionStatus.OPEN))
                    .thenReturn(Optional.empty());

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> saleService.create(request, 1L));

            assertTrue(exception.getMessage().contains("No specific active cash session found"));
            verify(cashSessionRepository).findByUserIdAndStatus(1L, CashSession.SessionStatus.OPEN);
        }

        @Test
        @DisplayName("Should throw StockInsufficientException when stock is insufficient")
        void create_ShouldThrowStockInsufficientException_WhenStockInsufficient() {
            // Arrange
            SaleRequest request = createSaleRequest();
            SaleItemRequest itemRequest = request.items().get(0);

            Sale saleEntity = new Sale();
            saleEntity.setItems(new ArrayList<>());
            saleEntity.setPayments(new ArrayList<>());

            when(saleMapper.toEntity(request)).thenReturn(saleEntity);
            when(establishmentRepository.findById(1L)).thenReturn(Optional.of(establishment));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(cashSessionRepository.findByUserIdAndStatus(1L, CashSession.SessionStatus.OPEN))
                    .thenReturn(Optional.of(cashSession));

            when(productRepository.findById(itemRequest.productId())).thenReturn(Optional.of(product));
            when(lotRepository.findById(itemRequest.lotId())).thenReturn(Optional.of(productLot));
            when(productUnitRepository.findById(itemRequest.productUnitId())).thenReturn(Optional.of(productUnit));

            // Inventory has only 5 units but request is for 10
            inventory.setQuantity(new BigDecimal("5.00"));
            when(inventoryRepository.findByEstablishmentIdAndLotId(1L, 1L))
                    .thenReturn(Optional.of(inventory));

            // Act & Assert
            StockInsufficientException exception = assertThrows(
                    StockInsufficientException.class,
                    () -> saleService.create(request, 1L));

            assertTrue(exception.getMessage().contains("Insufficient stock"));
        }

        @Test
        @DisplayName("Should create sale successfully with correct totals")
        void create_ShouldCreateSaleSuccessfully_WhenAllValidationsPass() {
            // Arrange
            SaleRequest request = createSaleRequest();
            SaleItemRequest itemRequest = request.items().get(0);

            Sale saleEntity = createSaleEntity();
            saleEntity.getItems().clear();
            saleEntity.getPayments().clear();

            SaleItem saleItem = new SaleItem();
            saleItem.setProduct(product);
            saleItem.setLot(productLot);
            saleItem.setProductUnit(productUnit);
            saleItem.setQuantity(new BigDecimal("10.00"));
            saleItem.setUnitPrice(new BigDecimal("100.00"));
            saleItem.setAmount(new BigDecimal("1000.00"));
            saleItem.setAppliedTaxRate(new BigDecimal("0.18"));

            SalePayment salePayment = new SalePayment();
            salePayment.setPaymentMethod(SalePayment.PaymentMethod.EFECTIVO);
            salePayment.setAmount(new BigDecimal("1180.00"));

            when(establishmentRepository.findById(1L)).thenReturn(Optional.of(establishment));
            when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(cashSessionRepository.findByUserIdAndStatus(1L, CashSession.SessionStatus.OPEN))
                    .thenReturn(Optional.of(cashSession));

            when(productRepository.findById(itemRequest.productId())).thenReturn(Optional.of(product));
            when(lotRepository.findById(itemRequest.lotId())).thenReturn(Optional.of(productLot));
            when(productUnitRepository.findById(itemRequest.productUnitId())).thenReturn(Optional.of(productUnit));
            when(inventoryRepository.findByEstablishmentIdAndLotId(1L, 1L))
                    .thenReturn(Optional.of(inventory));

            when(saleMapper.toEntity(request)).thenReturn(saleEntity);
            when(saleMapper.toItemEntity(any(SaleItemRequest.class))).thenReturn(saleItem);
            when(saleMapper.toPaymentEntity(any(SalePaymentRequest.class))).thenReturn(salePayment);
            when(cashConceptService.findOrCreateSaleConcept(anyString())).thenReturn(cashConcept);

            when(repository.save(any(Sale.class))).thenAnswer(invocation -> {
                Sale s = invocation.getArgument(0);
                s.setId(1L);
                return s;
            });

            SaleResponse expectedResponse = new SaleResponse(
                    1L, "Main Store", "John Doe", "testuser",
                    Sale.SaleDocumentType.BOLETA, "B001", "00000001",
                    LocalDateTime.now(), new BigDecimal("1000.00"), new BigDecimal("180.00"),
                    new BigDecimal("1180.00"), Sale.SaleStatus.COMPLETED, Sale.PaymentCondition.CONTADO,
                    null, null, null, null, null,
                    null, null, null,
                    false, null, null,
                    List.of(), List.of(), null,
                    "DNI", "12345678", null, "Test User");
            when(saleMapper.toResponse(any(Sale.class))).thenReturn(expectedResponse);

            // Act
            SaleResponse response = saleService.create(request, 1L);

            // Assert
            assertNotNull(response);
            assertEquals(1L, response.id());
            assertEquals(Sale.SaleStatus.COMPLETED, response.status());
            assertEquals("Main Store", response.establishmentName());
            assertEquals("John Doe", response.customerName());

            verify(repository).save(any(Sale.class));
            verify(stockMovementService).recordSaleMovement(
                    any(Establishment.class), any(ProductLot.class),
                    eq(new BigDecimal("10.00")), any(BigDecimal.class),
                    nullable(Long.class), any(User.class));
            verify(cashMovementService).registerInternalMovement(
                    any(CashSession.class), any(User.class), any(CashConcept.class),
                    any(BigDecimal.class), anyString(), anyString());
        }

        @Test
        @DisplayName("Should use default customer when customerId is null")
        void create_ShouldUseDefaultCustomer_WhenCustomerIdIsNull() {
            // Arrange
            SaleRequest request = new SaleRequest(
                    1L, null, Sale.SaleDocumentType.BOLETA, null,
                    null, null, null,
                    List.of(createSaleItemRequest()),
                    List.of(createPaymentRequest()),
                    null, null);

            Sale saleEntity = createSaleEntity();
            saleEntity.getItems().clear();
            saleEntity.getPayments().clear();

            Customer defaultCustomer = new Customer();
            defaultCustomer.setId(99L);
            defaultCustomer.setName("PUBLICO EN GENERAL");
            defaultCustomer.setDocumentNumber("00000000");
            defaultCustomer.setDocumentType(DocumentType.DNI);

            when(establishmentRepository.findById(1L)).thenReturn(Optional.of(establishment));
            when(customerRepository.findByDocumentNumber("00000000")).thenReturn(Optional.empty());
            when(customerRepository.save(any(Customer.class))).thenReturn(defaultCustomer);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(cashSessionRepository.findByUserIdAndStatus(1L, CashSession.SessionStatus.OPEN))
                    .thenReturn(Optional.of(cashSession));
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(lotRepository.findById(1L)).thenReturn(Optional.of(productLot));
            when(productUnitRepository.findById(1L)).thenReturn(Optional.of(productUnit));
            when(inventoryRepository.findByEstablishmentIdAndLotId(1L, 1L))
                    .thenReturn(Optional.of(inventory));
            when(saleMapper.toEntity(request)).thenReturn(saleEntity);
            when(saleMapper.toItemEntity(any(SaleItemRequest.class))).thenReturn(new SaleItem());
            when(saleMapper.toPaymentEntity(any(SalePaymentRequest.class))).thenReturn(new SalePayment());
            when(cashConceptService.findOrCreateSaleConcept(anyString())).thenReturn(cashConcept);
            when(repository.save(any(Sale.class))).thenAnswer(invocation -> {
                Sale s = invocation.getArgument(0);
                s.setId(1L);
                return s;
            });
            when(saleMapper.toResponse(any(Sale.class))).thenAnswer(invocation -> {
                Sale s = invocation.getArgument(0);
                return createSaleResponse(s);
            });

            // Act
            SaleResponse response = saleService.create(request, 1L);

            // Assert
            assertNotNull(response);
            verify(customerRepository).findByDocumentNumber("00000000");
            verify(customerRepository).save(any(Customer.class));
        }
    }

    // ==================== CANCEL TESTS ====================

    @Nested
    @DisplayName("cancel() - Sale Cancellation")
    class CancelTests {

        @Test
        @DisplayName("Should throw ResourceNotFoundException for non-existent sale")
        void cancel_ShouldThrowResourceNotFoundException_WhenSaleNotFound() {
            // Arrange
            when(repository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> saleService.cancel(999L));

            assertEquals("Sale not found: 999", exception.getMessage());
            verify(repository).findById(999L);
        }

        @Test
        @DisplayName("Should cancel an existing sale successfully")
        void cancel_ShouldCancelSaleSuccessfully_WhenSaleExists() {
            // Arrange
            Sale sale = createSaleEntity();
            sale.setId(1L);
            sale.setStatus(Sale.SaleStatus.COMPLETED);

            when(repository.findById(1L)).thenReturn(Optional.of(sale));
            when(repository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            saleService.cancel(1L);

            // Assert
            assertEquals(Sale.SaleStatus.CANCELED, sale.getStatus());
            verify(repository).findById(1L);
            verify(repository).save(sale);
        }
    }

    // ==================== CREATE CREDIT NOTE TESTS ====================

    @Nested
    @DisplayName("createCreditNote() - Credit Note Creation")
    class CreateCreditNoteTests {

        @Test
        @DisplayName("Should throw ResourceNotFoundException for non-existent sale")
        void createCreditNote_ShouldThrowResourceNotFoundException_WhenSaleNotFound() {
            // Arrange
            when(repository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> saleService.createCreditNote(999L, "Defective product", 1L));

            assertEquals("Original sale not found: 999", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalStateException for canceled sale")
        void createCreditNote_ShouldThrowIllegalStateException_WhenSaleIsCanceled() {
            // Arrange
            Sale canceledSale = createSaleEntity();
            canceledSale.setId(1L);
            canceledSale.setStatus(Sale.SaleStatus.CANCELED);

            when(repository.findById(1L)).thenReturn(Optional.of(canceledSale));

            // Act & Assert
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> saleService.createCreditNote(1L, "Defective product", 1L));

            assertEquals("Cannot create credit note for a canceled or voided sale", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalStateException for voided sale")
        void createCreditNote_ShouldThrowIllegalStateException_WhenSaleIsVoided() {
            // Arrange
            Sale voidedSale = createSaleEntity();
            voidedSale.setId(1L);
            voidedSale.setVoided(true);

            when(repository.findById(1L)).thenReturn(Optional.of(voidedSale));

            // Act & Assert
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> saleService.createCreditNote(1L, "Defective product", 1L));

            assertEquals("Cannot create credit note for a canceled or voided sale", exception.getMessage());
        }

        @Test
        @DisplayName("Should create credit note and restore stock for completed sale")
        void createCreditNote_ShouldCreateCreditNoteAndRestoreStock_WhenSaleIsCompleted() {
            // Arrange
            Sale originalSale = createSaleEntity();
            originalSale.setId(1L);
            originalSale.setStatus(Sale.SaleStatus.COMPLETED);
            originalSale.setSubTotal(new BigDecimal("1000.00"));
            originalSale.setTax(new BigDecimal("180.00"));
            originalSale.setTotal(new BigDecimal("1180.00"));

            SaleItem item = new SaleItem();
            item.setId(1L);
            item.setProduct(product);
            item.setLot(productLot);
            item.setQuantity(new BigDecimal("10.00"));
            item.setUnitPrice(new BigDecimal("100.00"));
            item.setAmount(new BigDecimal("1000.00"));
            originalSale.getItems().add(item);

            SalePayment payment = new SalePayment();
            payment.setId(1L);
            payment.setPaymentMethod(SalePayment.PaymentMethod.EFECTIVO);
            payment.setAmount(new BigDecimal("1180.00"));
            originalSale.getPayments().add(payment);

            // Restored inventory
            Inventory restoredInventory = new Inventory();
            restoredInventory.setId(1L);
            restoredInventory.setEstablishment(establishment);
            restoredInventory.setLot(productLot);
            restoredInventory.setQuantity(new BigDecimal("90.00"));

            when(repository.findById(1L)).thenReturn(Optional.of(originalSale));
            when(establishmentRepository.findById(1L)).thenReturn(Optional.of(establishment));
            when(documentSequenceRepository.findForUpdate(eq(1L), any(), eq("FC01")))
                    .thenReturn(Optional.empty());
            when(documentSequenceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(inventoryRepository.findByEstablishmentIdAndLotId(1L, 1L))
                    .thenReturn(Optional.of(inventory));
            when(cashSessionRepository.findByUserIdAndStatus(1L, CashSession.SessionStatus.OPEN))
                    .thenReturn(Optional.of(cashSession));

            when(cashConceptRepository.findByType(CashConcept.ConceptType.OUT))
                    .thenReturn(List.of(cashConcept));

            Sale creditNote = new Sale();
            creditNote.setId(2L);
            creditNote.setDocumentType(Sale.SaleDocumentType.NOTA_CREDITO);
            creditNote.setRelatedSale(originalSale);
            creditNote.setNoteReason("Defective product");
            creditNote.setSubTotal(new BigDecimal("-1000.00"));
            creditNote.setTax(new BigDecimal("-180.00"));
            creditNote.setTotal(new BigDecimal("-1180.00"));
            creditNote.setStatus(Sale.SaleStatus.COMPLETED);

            when(repository.save(any(Sale.class))).thenReturn(creditNote);

            SaleResponse expectedResponse = new SaleResponse(
                    2L, null, null, null,
                    Sale.SaleDocumentType.NOTA_CREDITO, "FC01", "00000001",
                    LocalDateTime.now(), new BigDecimal("-1000.00"), new BigDecimal("-180.00"),
                    new BigDecimal("-1180.00"), Sale.SaleStatus.COMPLETED, Sale.PaymentCondition.CONTADO,
                    null, null, null, null, null,
                    null, null, null,
                    false, null, null,
                    List.of(), List.of(), null,
                    null, null, null, null);
            when(saleMapper.toResponse(any(Sale.class))).thenReturn(expectedResponse);

            // Act
            SaleResponse response = saleService.createCreditNote(1L, "Defective product", 1L);

            // Assert
            assertNotNull(response);
            assertEquals(2L, response.id());
            assertEquals(Sale.SaleDocumentType.NOTA_CREDITO, response.documentType());
            assertEquals(new BigDecimal("-1000.00"), response.subTotal());
            assertEquals(new BigDecimal("-1180.00"), response.total());

            // Verify stock was restored (100 initial + 10 returned = 110)
            assertEquals(0, inventory.getQuantity().compareTo(new BigDecimal("110.00")));
            verify(inventoryRepository).save(inventory);
            verify(stockMovementService).recordReversalMovement(
                    any(Establishment.class), any(ProductLot.class),
                    eq(new BigDecimal("10.00")), any(BigDecimal.class),
                    eq("Credit note - Defective product"), nullable(Long.class), any(User.class));
            verify(repository).save(any(Sale.class));
        }
    }

    // ==================== CALCULATE TOTALS TESTS ====================

    @Nested
    @DisplayName("calculateTotals() - Cart Calculation")
    class CalculateTotalsTests {

        @Test
        @DisplayName("Should calculate subtotal, tax, and total correctly")
        void calculateTotals_ShouldCalculateCorrectly_WhenItemsExist() {
            // Arrange
            Sale sale = new Sale();
            sale.setItems(new ArrayList<>());

            // Item 1: 2 x 50 = 100 (Incl. IGV 18%)
            SaleItem item1 = new SaleItem();
            item1.setId(1L);
            item1.setProduct(product);
            item1.setQuantity(new BigDecimal("2.00"));
            item1.setUnitPrice(new BigDecimal("50.00"));
            item1.setAmount(new BigDecimal("100.00"));
            item1.setAppliedTaxRate(new BigDecimal("0.18"));
            sale.getItems().add(item1);

            // Item 2: 3 x 30 = 90 (Incl. IGV 18%)
            SaleItem item2 = new SaleItem();
            item2.setId(2L);
            item2.setProduct(product);
            item2.setQuantity(new BigDecimal("3.00"));
            item2.setUnitPrice(new BigDecimal("30.00"));
            item2.setAmount(new BigDecimal("90.00"));
            item2.setAppliedTaxRate(new BigDecimal("0.18"));
            sale.getItems().add(item2);

            // Act
            Sale result = invokeCalculateTotals(sale);

            // Assert
            // Total = 100 + 90 = 190.00
            // SubTotal = 190 / 1.18 = 161.02
            // Tax = 190 - 161.02 = 28.98
            assertEquals(0, result.getSubTotal().compareTo(new BigDecimal("161.02")));
            assertEquals(0, result.getTax().compareTo(new BigDecimal("28.98")));
            assertEquals(0, result.getTotal().compareTo(new BigDecimal("190.00")));
        }

        @Test
        @DisplayName("Should handle empty items list")
        void calculateTotals_ShouldHandleEmptyItems() {
            // Arrange
            Sale sale = new Sale();
            sale.setItems(new ArrayList<>());

            // Act
            Sale result = invokeCalculateTotals(sale);

            // Assert
            assertEquals(0, result.getSubTotal().compareTo(BigDecimal.ZERO));
            assertEquals(0, result.getTax().compareTo(BigDecimal.ZERO)); // 0 * 0.18 = 0
            assertEquals(0, result.getTotal().compareTo(BigDecimal.ZERO));
        }

        @Test
        @DisplayName("Should use default tax rate when no items exist")
        void calculateTotals_ShouldUseDefaultTaxRate_WhenNoItems() {
            // Arrange
            Sale sale = new Sale();
            sale.setItems(new ArrayList<>());

            // Act - the method calculates tax using default rate of 0.18 but since subTotal is 0, tax is also 0
            Sale result = invokeCalculateTotals(sale);

            // Assert - verify the calculation logic works (0 * 0.18 = 0)
            assertEquals(0, result.getTax().compareTo(BigDecimal.ZERO));
        }
    }

    // ==================== VALIDATE STOCK TESTS ====================

    @Nested
    @DisplayName("validateStock() - Stock Validation")
    class ValidateStockTests {

        @Test
        @DisplayName("Should pass when stock is sufficient")
        void validateStock_ShouldPass_WhenStockIsSufficient() {
            // Arrange
            inventory.setQuantity(new BigDecimal("100.00"));
            when(inventoryRepository.findByEstablishmentIdAndLotId(1L, 1L))
                    .thenReturn(Optional.of(inventory));

            // Act & Assert - should not throw
            assertDoesNotThrow(() -> invokeValidateStock(1L, 1L, new BigDecimal("50.00")));

            verify(inventoryRepository).findByEstablishmentIdAndLotId(1L, 1L);
        }

        @Test
        @DisplayName("Should pass when stock is exactly equal to requested quantity")
        void validateStock_ShouldPass_WhenStockEqualsRequested() {
            // Arrange
            inventory.setQuantity(new BigDecimal("50.00"));
            when(inventoryRepository.findByEstablishmentIdAndLotId(1L, 1L))
                    .thenReturn(Optional.of(inventory));

            // Act & Assert - should not throw
            assertDoesNotThrow(() -> invokeValidateStock(1L, 1L, new BigDecimal("50.00")));
        }

        @Test
        @DisplayName("Should throw StockInsufficientException when stock is insufficient")
        void validateStock_ShouldThrowStockInsufficientException_WhenStockIsInsufficient() {
            // Arrange
            inventory.setQuantity(new BigDecimal("10.00"));
            inventory.setLot(productLot);
            when(inventoryRepository.findByEstablishmentIdAndLotId(1L, 1L))
                    .thenReturn(Optional.of(inventory));

            // Act & Assert
            StockInsufficientException exception = assertThrows(
                    StockInsufficientException.class,
                    () -> invokeValidateStock(1L, 1L, new BigDecimal("50.00")));

            assertTrue(exception.getMessage().contains("Insufficient stock"));
            assertTrue(exception.getMessage().contains("LOT001"));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when inventory not found")
        void validateStock_ShouldThrowResourceNotFoundException_WhenInventoryNotFound() {
            // Arrange
            when(inventoryRepository.findByEstablishmentIdAndLotId(1L, 1L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> invokeValidateStock(1L, 1L, new BigDecimal("10.00")));

            assertEquals("Inventory not found for lot ID: 1", exception.getMessage());
        }
    }

    // ==================== HELPER METHODS ====================

    private SaleRequest createSaleRequest() {
        return new SaleRequest(
                1L,
                1L,
                Sale.SaleDocumentType.BOLETA,
                null,
                null, null, null,
                List.of(createSaleItemRequest()),
                List.of(createPaymentRequest()),
                Sale.PaymentCondition.CONTADO,
                null);
    }

    private SaleItemRequest createSaleItemRequest() {
        return new SaleItemRequest(
                1L,   // productId
                1L,   // lotId
                1L,   // productUnitId
                new BigDecimal("10.00"),  // quantity
                new BigDecimal("100.00"), // unitPrice
                null,                     // discountAmount
                null,                     // discountReason
                null,                     // increaseAmount
                null);                    // increaseReason
    }

    private SalePaymentRequest createPaymentRequest() {
        return new SalePaymentRequest(
                SalePayment.PaymentMethod.EFECTIVO,
                new BigDecimal("1180.00"),
                1L,     // cashSessionId
                null);  // reference
    }

    private Sale createSaleEntity() {
        Sale sale = new Sale();
        sale.setEstablishment(establishment);
        sale.setCustomer(customer);
        sale.setUser(user);
        sale.setDocumentType(Sale.SaleDocumentType.BOLETA);
        sale.setSeries("B001");
        sale.setNumber("00000001");
        sale.setDate(LocalDateTime.now());
        sale.setStatus(Sale.SaleStatus.COMPLETED);
        sale.setPaymentCondition(Sale.PaymentCondition.CONTADO);
        sale.setCashSession(cashSession);
        sale.setItems(new ArrayList<>());
        sale.setPayments(new ArrayList<>());
        return sale;
    }

    private SaleResponse createSaleResponse(Sale sale) {
        return new SaleResponse(
                sale.getId(),
                sale.getEstablishment() != null ? sale.getEstablishment().getName() : null,
                sale.getCustomer() != null ? sale.getCustomer().getName() : null,
                sale.getUser() != null ? sale.getUser().getUsername() : null,
                sale.getDocumentType(),
                sale.getSeries(),
                sale.getNumber(),
                sale.getDate(),
                sale.getSubTotal(),
                sale.getTax(),
                sale.getTotal(),
                sale.getStatus(),
                sale.getPaymentCondition(),
                sale.getSunatStatus(),
                sale.getPdfUrl(),
                sale.getCdrUrl(),
                sale.getSunatResponseJson(),
                sale.getSunatErrorCode(),
                sale.getRelatedSale() != null ? sale.getRelatedSale().getId() : null,
                sale.getNoteCode(),
                sale.getNoteReason(),
                sale.isVoided(),
                sale.getVoidedAt(),
                sale.getVoidReason(),
                List.of(),
                List.of(),
                null,
                sale.getCustomer() != null ? sale.getCustomer().getDocumentType() != null ? sale.getCustomer().getDocumentType().name() : null : null,
                sale.getCustomer() != null ? sale.getCustomer().getDocumentNumber() : null,
                sale.getCustomer() != null ? sale.getCustomer().getAddress() : null,
                sale.getUser() != null ? sale.getUser().getFullName() : null);
    }

    // Use reflection to invoke private methods for testing
    private Sale invokeCalculateTotals(Sale sale) {
        try {
            java.lang.reflect.Method method = SaleServiceImpl.class.getDeclaredMethod("calculateTotals", Sale.class);
            method.setAccessible(true);
            return (Sale) method.invoke(saleService, sale);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke calculateTotals", e);
        }
    }

    private void invokeValidateStock(Long establishmentId, Long lotId, BigDecimal quantity) {
        try {
            java.lang.reflect.Method method = SaleServiceImpl.class.getDeclaredMethod(
                    "validateStock", Long.class, Long.class, BigDecimal.class);
            method.setAccessible(true);
            method.invoke(saleService, establishmentId, lotId, quantity);
        } catch (java.lang.reflect.InvocationTargetException e) {
            // Unwrap the actual exception
            if (e.getCause() instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException("Failed to invoke validateStock", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke validateStock", e);
        }
    }
}
