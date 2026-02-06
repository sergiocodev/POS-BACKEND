package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.purchase.PurchaseRequest;
import com.sergiocodev.app.dto.purchase.PurchaseResponse;
import com.sergiocodev.app.mapper.PurchaseMapper;
import com.sergiocodev.app.model.*;
import com.sergiocodev.app.repository.*;
import lombok.RequiredArgsConstructor;
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
        entity.setSupplier(supplierRepository.findById(request.supplierId()).orElse(null));
        entity.setEstablishment(establishmentRepository.findById(request.establishmentId()).orElse(null));
        entity.setUser(userRepository.findById(userId).orElse(null));
        entity.setArrivalDate(LocalDateTime.now());
        entity.setStatus(Purchase.PurchaseStatus.RECEIVED);

        BigDecimal subTotal = BigDecimal.ZERO;
        for (var ir : request.items()) {
            Product product = productRepository.findById(ir.productId()).orElse(null);

            // Create or get lot
            ProductLot lot = lotRepository.findAll().stream()
                    .filter(l -> l.getProduct().getId().equals(ir.productId())
                            && l.getLotCode().equals(ir.lotCode()))
                    .findFirst()
                    .orElseGet(() -> {
                        ProductLot newLot = new ProductLot();
                        newLot.setProduct(product);
                        newLot.setLotCode(ir.lotCode());
                        newLot.setExpiryDate(ir.expiryDate());
                        return lotRepository.save(newLot);
                    });

            PurchaseItem item = purchaseMapper.toItemEntity(ir);
            item.setPurchase(entity);
            item.setProduct(product);

            BigDecimal itemTotal = ir.unitCost().multiply(new BigDecimal(ir.quantity()));
            item.setTotalCost(itemTotal);
            entity.getItems().add(item);

            subTotal = subTotal.add(itemTotal);

            // Update Inventory
            Inventory inventory = inventoryRepository
                    .findByEstablishmentIdAndLotId(request.establishmentId(), lot.getId())
                    .orElseGet(() -> {
                        Inventory newInv = new Inventory();
                        newInv.setEstablishment(entity.getEstablishment());
                        newInv.setLot(lot);
                        newInv.setQuantity(BigDecimal.ZERO);
                        return newInv;
                    });

            BigDecimal newQty = inventory.getQuantity().add(new BigDecimal(ir.quantity() + ir.bonusQuantity()));
            inventory.setQuantity(newQty);
            inventory.setCostPrice(ir.unitCost());
            inventory.setLastMovement(LocalDateTime.now());
            inventoryRepository.save(inventory);

            // Stock Movement
            StockMovement movement = new StockMovement();
            movement.setEstablishment(entity.getEstablishment());
            movement.setLot(lot);
            movement.setType(StockMovement.MovementType.PURCHASE);
            movement.setQuantity(new java.math.BigDecimal(ir.quantity() + ir.bonusQuantity()));
            movement.setBalanceAfter(newQty);
            movement.setReferenceTable("purchases");
            movement.setReferenceId(entity.getId());
            movement.setUser(entity.getUser());
            movement.setCreatedAt(java.time.LocalDateTime.now());
            stockMovementRepository.save(movement);
        }

        entity.setSubTotal(subTotal);
        entity.setTax(subTotal.multiply(new BigDecimal("0.18"))); // Simplified
        entity.setTotal(subTotal.add(entity.getTax()));

        return purchaseMapper.toResponse(repository.save(entity));
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
                .orElseThrow(() -> new RuntimeException("Purchase not found"));
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        Purchase purchase = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase not found"));
        purchase.setStatus(Purchase.PurchaseStatus.CANCELED);
        repository.save(purchase);
        // Reverse inventory logic would go here
    }
}
