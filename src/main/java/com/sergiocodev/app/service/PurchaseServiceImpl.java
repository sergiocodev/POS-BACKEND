package com.sergiocodev.app.service;

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
    private final ProductLotRepository lotRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final PurchaseMapper purchaseMapper;

    @Override
    @Transactional
    public PurchaseResponse create(PurchaseRequest request, Long userId) {
        Purchase entity = purchaseMapper.toEntity(request);
        entity.setSupplier(supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + request.supplierId())));
        entity.setEstablishment(establishmentRepository.findById(request.establishmentId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Establishment not found: " + request.establishmentId())));
        entity.setUser(userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId)));
        entity.setArrivalDate(LocalDateTime.now());
        entity.setStatus(Purchase.PurchaseStatus.RECEIVED);

        for (var ir : request.items()) {
            Product product = productRepository.findById(ir.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + ir.productId()));

            ProductLot lot = findOrCreateLot(product, ir.lotCode(), ir.expiryDate());

            PurchaseItem item = purchaseMapper.toItemEntity(ir);
            item.setPurchase(entity);
            item.setProduct(product);

            BigDecimal itemTotal = ir.unitCost().multiply(new BigDecimal(ir.quantity()));
            item.setTotalCost(itemTotal);
            entity.getItems().add(item);

            updateInventory(entity, lot, ir.quantity(), ir.bonusQuantity(), ir.unitCost());
        }

        calculateTotals(entity);

        return purchaseMapper.toResponse(repository.save(entity));
    }

    private ProductLot findOrCreateLot(Product product, String lotCode, java.time.LocalDate expiryDate) {
        return lotRepository.findAll().stream()
                .filter(l -> l.getProduct().getId().equals(product.getId())
                        && l.getLotCode().equals(lotCode))
                .findFirst()
                .orElseGet(() -> {
                    ProductLot newLot = new ProductLot();
                    newLot.setProduct(product);
                    newLot.setLotCode(lotCode);
                    newLot.setExpiryDate(expiryDate);
                    return lotRepository.save(newLot);
                });
    }

    private void updateInventory(Purchase purchase, ProductLot lot, Integer quantity, Integer bonusQuantity,
            BigDecimal unitCost) {
        Inventory inventory = inventoryRepository
                .findByEstablishmentIdAndLotId(purchase.getEstablishment().getId(), lot.getId())
                .orElseGet(() -> {
                    Inventory newInv = new Inventory();
                    newInv.setEstablishment(purchase.getEstablishment());
                    newInv.setLot(lot);
                    newInv.setQuantity(BigDecimal.ZERO);
                    return newInv;
                });

        BigDecimal newQty = inventory.getQuantity().add(new BigDecimal(quantity + bonusQuantity));
        inventory.setQuantity(newQty);
        inventory.setCostPrice(unitCost);
        inventory.setLastMovement(LocalDateTime.now());
        inventoryRepository.save(inventory);

        createStockMovement(purchase, lot, quantity + bonusQuantity, newQty);
    }

    private void createStockMovement(Purchase purchase, ProductLot lot, Integer quantity, BigDecimal balanceAfter) {
        StockMovement movement = new StockMovement();
        movement.setEstablishment(purchase.getEstablishment());
        movement.setLot(lot);
        movement.setType(StockMovement.MovementType.PURCHASE);
        movement.setQuantity(new java.math.BigDecimal(quantity));
        movement.setBalanceAfter(balanceAfter);
        movement.setReferenceTable("purchases");
        movement.setReferenceId(purchase.getId());
        movement.setUser(purchase.getUser());
        movement.setCreatedAt(java.time.LocalDateTime.now());
        stockMovementRepository.save(movement);
    }

    private void calculateTotals(Purchase entity) {
        BigDecimal subTotal = entity.getItems().stream()
                .map(PurchaseItem::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        entity.setSubTotal(subTotal);
        entity.setTax(subTotal.multiply(new BigDecimal("0.18"))); // Simplified
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
        // Reverse inventory logic would go here
    }
}
