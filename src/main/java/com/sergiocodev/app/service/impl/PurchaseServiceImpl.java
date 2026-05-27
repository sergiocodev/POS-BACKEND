package com.sergiocodev.app.service.impl;

import com.sergiocodev.app.service.interfaces.PurchaseService;

import com.sergiocodev.app.service.interfaces.CashConceptService;
import com.sergiocodev.app.service.interfaces.CashMovementService;
import com.sergiocodev.app.service.interfaces.StockMovementService;
import com.sergiocodev.app.dto.purchase.PurchaseRequest;
import com.sergiocodev.app.dto.purchase.PurchaseResponse;
import com.sergiocodev.app.mapper.PurchaseMapper;
import com.sergiocodev.app.model.*;
import com.sergiocodev.app.repository.*;
import lombok.RequiredArgsConstructor;
import com.sergiocodev.app.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository repository;
    private final SupplierRepository supplierRepository;
    private final EstablishmentRepository establishmentRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductUnitRepository productUnitRepository;
    private final ProductLotRepository lotRepository;
    private final InventoryRepository inventoryRepository;
    private final AccountPayableRepository accountPayableRepository;
    private final AccountPayablePaymentRepository accountPayablePaymentRepository;
    private final PurchaseMapper purchaseMapper;
    private final CashSessionRepository cashSessionRepository;
    private final CashMovementService cashMovementService;
    private final CashConceptService cashConceptService;
    private final StockMovementService stockMovementService;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public PurchaseResponse create(PurchaseRequest request, Long userId) {
        Purchase entity = purchaseMapper.toEntity(request);
        entity.setSupplier(supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + request.supplierId())));
        entity.setEstablishment(establishmentRepository.findById(request.establishmentId())
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Establishment not found: " + request.establishmentId())));
        entity.setUser(userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId)));
        entity.setArrivalDate(LocalDateTime.now());
        entity.setStatus(Purchase.PurchaseStatus.RECEIVED);

        for (var ir : request.items()) {
            Product product = productRepository.findById(ir.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + ir.productId()));

            ProductUnit productUnit = productUnitRepository.findById(ir.productUnitId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("ProductUnit not found: " + ir.productUnitId()));

            ProductLot lot = findOrCreateLot(product, ir.lotCode(), ir.expiryDate());

            PurchaseItem item = purchaseMapper.toItemEntity(ir);
            item.setPurchase(entity);
            item.setProduct(product);
            item.setProductUnit(productUnit);

            BigDecimal itemTotal = ir.unitCost().multiply(new BigDecimal(ir.quantity()));
            item.setTotalCost(itemTotal);
            entity.getItems().add(item);

            updateInventory(entity, lot, ir.quantity(), ir.bonusQuantity(), ir.unitCost(), productUnit);
        }

        calculateTotals(entity);

        Purchase savedEntity = repository.save(entity);

        BigDecimal totalAmount = savedEntity.getTotal();
        BigDecimal initialPayment = BigDecimal.ZERO;

        if (request.paymentCondition() == PurchaseRequest.PaymentCondition.CASH) {
            initialPayment = totalAmount;
        } else if (request.paymentCondition() == PurchaseRequest.PaymentCondition.CREDIT
                && request.initialPayment() != null) {
            initialPayment = request.initialPayment();
        }

        BigDecimal pendingBalance = totalAmount.subtract(initialPayment);
        AccountPayable.PayableStatus status = AccountPayable.PayableStatus.PENDING;

        if (pendingBalance.compareTo(BigDecimal.ZERO) <= 0) {
            status = AccountPayable.PayableStatus.PAID;
            pendingBalance = BigDecimal.ZERO;
        } else if (initialPayment.compareTo(BigDecimal.ZERO) > 0) {
            status = AccountPayable.PayableStatus.PARTIAL;
        }

        AccountPayable payable = new AccountPayable();
        payable.setPurchase(savedEntity);
        payable.setSupplier(savedEntity.getSupplier());
        payable.setTotalAmount(totalAmount);
        payable.setAmountPaid(initialPayment);
        payable.setPendingBalance(pendingBalance);
        payable.setStatus(status);
        if (request.paymentCondition() == PurchaseRequest.PaymentCondition.CREDIT && request.dueDate() != null) {
            payable.setDueDate(request.dueDate());
        }
        AccountPayable savedPayable = accountPayableRepository.save(payable);

        if (initialPayment.compareTo(BigDecimal.ZERO) > 0) {
            if (request.paymentMethod() == null) {
                throw new IllegalArgumentException("Payment method is required when there is an initial payment.");
            }

            CashSession session = cashSessionRepository.findByUserIdAndStatus(userId, CashSession.SessionStatus.OPEN)
                    .orElseThrow(() -> new RuntimeException(
                            "Debe tener una sesión de caja abierta para realizar compras al contado o abonar pagos iniciales."));

            AccountPayablePayment payment = new AccountPayablePayment();
            payment.setAccountPayable(savedPayable);
            payment.setUser(savedEntity.getUser());
            payment.setCashSession(session);
            payment.setAmount(initialPayment);
            payment.setPaymentMethod(request.paymentMethod());
            payment.setPaymentDate(LocalDateTime.now());
            accountPayablePaymentRepository.save(payment);

            CashConcept concept = cashConceptService.findOrCreatePurchaseConcept(request.paymentMethod().name());

            String description = "Compra "
                    + (request.paymentCondition() == PurchaseRequest.PaymentCondition.CASH ? "al contado"
                            : "con pago inicial")
                    + " (" + request.paymentMethod().name() + ") - Proveedor: "
                    + savedEntity.getSupplier().getName();

            cashMovementService.registerInternalMovement(session, savedEntity.getUser(), concept, initialPayment,
                    "COMPRA-" + savedEntity.getId(), description);
        }

        return purchaseMapper.toResponse(savedEntity);
    }

    private ProductLot findOrCreateLot(Product product, String lotCode, java.time.LocalDate expiryDate) {
        return lotRepository.findByProductIdAndLotCode(product.getId(), lotCode)
                .orElseGet(() -> {
                    ProductLot newLot = new ProductLot();
                    newLot.setProduct(product);
                    newLot.setLotCode(lotCode);
                    newLot.setExpiryDate(expiryDate);
                    return lotRepository.save(newLot);
                });
    }

    private void updateInventory(Purchase purchase, ProductLot lot, Integer quantity, Integer bonusQuantity,
            BigDecimal unitCost, ProductUnit productUnit) {

        // Convertir a unidad base multiplicando por el factor de la unidad comprada
        // Ejemplo: 5 cajas × factor 100 = 500 tabletas
        int factor = productUnit.getFactor() != null ? productUnit.getFactor() : 1;
        int totalBaseUnits = (quantity + bonusQuantity) * factor;

        Inventory inventory = inventoryRepository
                .findByEstablishmentIdAndLotId(purchase.getEstablishment().getId(), lot.getId())
                .orElseGet(() -> {
                    Inventory newInv = new Inventory();
                    newInv.setEstablishment(purchase.getEstablishment());
                    newInv.setLot(lot);
                    newInv.setQuantity(BigDecimal.ZERO);
                    return newInv;
                });

        BigDecimal newQty = inventory.getQuantity().add(new BigDecimal(totalBaseUnits));
        inventory.setQuantity(newQty);
        inventory.setCostPrice(unitCost);
        inventory.setLastMovement(LocalDateTime.now());
        inventoryRepository.save(inventory);

        createStockMovement(purchase, lot, totalBaseUnits, newQty);
    }

    private void createStockMovement(Purchase purchase, ProductLot lot, Integer quantity, BigDecimal balanceAfter) {
        stockMovementService.recordPurchaseMovement(
                purchase.getEstablishment(), lot,
                BigDecimal.valueOf(quantity), balanceAfter,
                purchase.getId(), purchase.getUser());
    }

    private void calculateTotals(Purchase entity) {
        BigDecimal subTotal = entity.getItems().stream()
                .map(PurchaseItem::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        entity.setSubTotal(subTotal);
        BigDecimal taxRate = entity.getItems().stream()
                .findFirst()
                .map(item -> item.getProduct().getTaxType().getRate())
                .orElse(new BigDecimal("0.18"));
        entity.setTax(subTotal.multiply(taxRate));
        entity.setTotal(subTotal.add(entity.getTax()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseResponse> getAll() {
        return repository.findAll().stream()
                .map(purchaseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PurchaseResponse> getAllPaged(
            LocalDateTime startDate,
            LocalDateTime endDate,
            String documentType,
            String series,
            String number,
            String supplierName,
            String supplierDocument,
            String userName,
            String status,
            String total,
            String paymentMethod,
            String columnDate,
            Pageable pageable) {

        org.springframework.data.jpa.domain.Specification<Purchase> spec = com.sergiocodev.app.specification.PurchaseSpecification
                .filterPurchases(
                        startDate, endDate, documentType, series, number, supplierName,
                        supplierDocument, userName, status,
                        total, paymentMethod, columnDate);

        Page<Purchase> purchases = repository.findAll(spec, pageable);
        return purchases.map(purchaseMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public com.sergiocodev.app.dto.purchase.PurchaseSummaryResponse getSummary(
            LocalDateTime startDate,
            LocalDateTime endDate,
            String documentType,
            String series,
            String number,
            String supplierName,
            String supplierDocument,
            String userName,
            String status,
            String total,
            String paymentMethod,
            String columnDate) {

        jakarta.persistence.criteria.CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        jakarta.persistence.criteria.CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        jakarta.persistence.criteria.Root<Purchase> root = query.from(Purchase.class);
        query.distinct(true);

        jakarta.persistence.criteria.Predicate predicate = com.sergiocodev.app.specification.PurchaseSpecification
                .buildPredicate(
                        root, cb, startDate, endDate, documentType, series, number,
                        supplierName, supplierDocument, userName, status,
                        total, paymentMethod, columnDate);

        query.where(predicate);
        query.multiselect(root.get("documentType"), cb.sum(root.get("total")));
        query.groupBy(root.get("documentType"));

        java.util.List<Object[]> results = entityManager.createQuery(query).getResultList();

        java.math.BigDecimal totalFacturas = java.math.BigDecimal.ZERO;
        java.math.BigDecimal totalBoletas = java.math.BigDecimal.ZERO;
        java.math.BigDecimal totalGuiaRemision = java.math.BigDecimal.ZERO;
        java.math.BigDecimal totalNeto = java.math.BigDecimal.ZERO;

        for (Object[] result : results) {
            Purchase.PurchaseDocumentType type = (Purchase.PurchaseDocumentType) result[0];
            java.math.BigDecimal sum = (java.math.BigDecimal) result[1];
            if (sum == null)
                sum = java.math.BigDecimal.ZERO;

            if (type != null) {
                switch (type) {
                    case FACTURA -> totalFacturas = sum;
                    case BOLETA -> totalBoletas = sum;
                    case GUIA -> totalGuiaRemision = sum;
                }
            }
            totalNeto = totalNeto.add(sum);
        }

        return new com.sergiocodev.app.dto.purchase.PurchaseSummaryResponse(
                totalFacturas, totalBoletas, totalGuiaRemision, totalNeto);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseResponse getById(Long id) {
        return repository.findById(id)
                .map(purchaseMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found: " + id));
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        Purchase purchase = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found: " + id));
        purchase.setStatus(Purchase.PurchaseStatus.CANCELED);
        repository.save(purchase);
    }
}
