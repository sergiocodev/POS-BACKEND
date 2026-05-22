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

    @Override
    @Transactional
    public PurchaseResponse create(PurchaseRequest request, Long userId) {
        Purchase entity = purchaseMapper.toEntity(request);
        entity.setSupplier(supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + request.getSupplierId())));
        entity.setEstablishment(establishmentRepository.findById(request.getEstablishmentId())
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Establishment not found: " + request.getEstablishmentId())));
        entity.setUser(userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId)));
        entity.setArrivalDate(LocalDateTime.now());
        entity.setStatus(Purchase.PurchaseStatus.RECEIVED);

        for (var ir : request.getItems()) {
            Product product = productRepository.findById(ir.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + ir.getProductId()));

            ProductUnit productUnit = productUnitRepository.findById(ir.getProductUnitId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("ProductUnit not found: " + ir.getProductUnitId()));

            ProductLot lot = findOrCreateLot(product, ir.getLotCode(), ir.getExpiryDate());

            PurchaseItem item = purchaseMapper.toItemEntity(ir);
            item.setPurchase(entity);
            item.setProduct(product);
            item.setProductUnit(productUnit);

            BigDecimal itemTotal = ir.getUnitCost().multiply(new BigDecimal(ir.getQuantity()));
            item.setTotalCost(itemTotal);
            entity.getItems().add(item);

            updateInventory(entity, lot, ir.getQuantity(), ir.getBonusQuantity(), ir.getUnitCost(), productUnit);
        }

        calculateTotals(entity);

        Purchase savedEntity = repository.save(entity);

        BigDecimal totalAmount = savedEntity.getTotal();
        BigDecimal initialPayment = BigDecimal.ZERO;

        if (request.getPaymentCondition() == PurchaseRequest.PaymentCondition.CASH) {
            initialPayment = totalAmount;
        } else if (request.getPaymentCondition() == PurchaseRequest.PaymentCondition.CREDIT
                && request.getInitialPayment() != null) {
            initialPayment = request.getInitialPayment();
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
        if (request.getPaymentCondition() == PurchaseRequest.PaymentCondition.CREDIT && request.getDueDate() != null) {
            payable.setDueDate(request.getDueDate());
        }
        AccountPayable savedPayable = accountPayableRepository.save(payable);

        if (initialPayment.compareTo(BigDecimal.ZERO) > 0) {
            if (request.getPaymentMethod() == null) {
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
            payment.setPaymentMethod(request.getPaymentMethod());
            payment.setPaymentDate(LocalDateTime.now());
            accountPayablePaymentRepository.save(payment);

            CashConcept concept = cashConceptService.findOrCreatePurchaseConcept(request.getPaymentMethod().name());

            String description = "Compra "
                    + (request.getPaymentCondition() == PurchaseRequest.PaymentCondition.CASH ? "al contado"
                            : "con pago inicial")
                    + " (" + request.getPaymentMethod().name() + ") - Proveedor: "
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
