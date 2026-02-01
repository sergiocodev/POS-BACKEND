package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.purchase.PurchaseRequest;
import com.sergiocodev.app.dto.purchase.PurchaseResponse;
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
    private final com.sergiocodev.app.repository.KardexRepository kardexRepository;

    @Override
    @Transactional
    public PurchaseResponse create(PurchaseRequest request, Long userId) {
        Purchase entity = new Purchase();
        entity.setSupplier(supplierRepository.findById(request.getSupplierId()).orElse(null));
        entity.setEstablishment(establishmentRepository.findById(request.getEstablishmentId()).orElse(null));
        entity.setUser(userRepository.findById(userId).orElse(null));
        entity.setDocumentType(request.getDocumentType());
        entity.setSeries(request.getSeries());
        entity.setNumber(request.getNumber());
        entity.setIssueDate(request.getIssueDate());
        entity.setArrivalDate(LocalDateTime.now());
        entity.setNotes(request.getNotes());
        entity.setStatus(Purchase.PurchaseStatus.RECEIVED);

        BigDecimal subTotal = BigDecimal.ZERO;
        for (var ir : request.getItems()) {
            Product product = productRepository.findById(ir.getProductId()).orElse(null);

            // Create or get lot
            ProductLot lot = lotRepository.findAll().stream()
                    .filter(l -> l.getProduct().getId().equals(ir.getProductId())
                            && l.getLotCode().equals(ir.getLotCode()))
                    .findFirst()
                    .orElseGet(() -> {
                        ProductLot newLot = new ProductLot();
                        newLot.setProduct(product);
                        newLot.setLotCode(ir.getLotCode());
                        newLot.setExpiryDate(ir.getExpiryDate());
                        return lotRepository.save(newLot);
                    });

            PurchaseItem item = new PurchaseItem();
            item.setPurchase(entity);
            item.setProduct(product);
            item.setLotCode(ir.getLotCode());
            item.setExpiryDate(ir.getExpiryDate());
            item.setQuantity(ir.getQuantity());
            item.setBonusQuantity(ir.getBonusQuantity());
            item.setUnitCost(ir.getUnitCost());
            BigDecimal itemTotal = ir.getUnitCost().multiply(new BigDecimal(ir.getQuantity()));
            item.setTotalCost(itemTotal);
            entity.getItems().add(item);

            subTotal = subTotal.add(itemTotal);

            // Update Inventory
            Inventory inventory = inventoryRepository
                    .findByEstablishmentIdAndLotId(request.getEstablishmentId(), lot.getId())
                    .orElseGet(() -> {
                        Inventory newInv = new Inventory();
                        newInv.setEstablishment(entity.getEstablishment());
                        newInv.setLot(lot);
                        newInv.setQuantity(BigDecimal.ZERO);
                        return newInv;
                    });

            BigDecimal newQty = inventory.getQuantity().add(new BigDecimal(ir.getQuantity() + ir.getBonusQuantity()));
            inventory.setQuantity(newQty);
            inventory.setCostPrice(ir.getUnitCost());
            inventory.setLastMovement(LocalDateTime.now());
            inventoryRepository.save(inventory);

            // Kardex Movement
            com.sergiocodev.app.model.Kardex kardex = new com.sergiocodev.app.model.Kardex();
            kardex.setProduct(product);
            kardex.setEstablishment(entity.getEstablishment());
            kardex.setMovementType(com.sergiocodev.app.model.Kardex.MovementType.PURCHASE);
            kardex.setQuantity(ir.getQuantity() + ir.getBonusQuantity());
            kardex.setBalance(newQty.intValue());
            kardex.setNotes("Purchase: " + entity.getSeries() + "-" + entity.getNumber());
            kardexRepository.save(kardex);
        }

        entity.setSubTotal(subTotal);
        entity.setTax(subTotal.multiply(new BigDecimal("0.18"))); // Simplified
        entity.setTotal(subTotal.add(entity.getTax()));

        return new PurchaseResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseResponse> getAll() {
        return repository.findAll().stream()
                .map(PurchaseResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseResponse getById(Long id) {
        return repository.findById(id)
                .map(PurchaseResponse::new)
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
