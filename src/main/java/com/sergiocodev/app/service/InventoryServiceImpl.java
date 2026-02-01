package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.inventory.InventoryRequest;
import com.sergiocodev.app.dto.inventory.InventoryResponse;
import com.sergiocodev.app.model.Inventory;
import com.sergiocodev.app.repository.EstablishmentRepository;
import com.sergiocodev.app.repository.InventoryRepository;
import com.sergiocodev.app.repository.ProductLotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository repository;
    private final EstablishmentRepository establishmentRepository;
    private final ProductLotRepository lotRepository;
    private final com.sergiocodev.app.repository.KardexRepository kardexRepository;

    @Override
    @Transactional
    public InventoryResponse updateStock(InventoryRequest request) {
        Inventory entity = repository.findByEstablishmentIdAndLotId(request.getEstablishmentId(), request.getLotId())
                .orElseGet(() -> {
                    Inventory newInv = new Inventory();
                    newInv.setEstablishment(
                            establishmentRepository.findById(request.getEstablishmentId()).orElse(null));
                    newInv.setLot(lotRepository.findById(request.getLotId()).orElse(null));
                    newInv.setQuantity(java.math.BigDecimal.ZERO);
                    return newInv;
                });

        java.math.BigDecimal oldQuantity = entity.getQuantity();
        java.math.BigDecimal newQuantity = request.getQuantity();

        entity.setQuantity(newQuantity);
        if (request.getCostPrice() != null)
            entity.setCostPrice(request.getCostPrice());
        if (request.getSalesPrice() != null)
            entity.setSalesPrice(request.getSalesPrice());
        entity.setLastMovement(LocalDateTime.now());

        Inventory saved = repository.save(entity);

        // Log to Kardex
        // Determine movement type and quantity difference
        java.math.BigDecimal diff = newQuantity.subtract(oldQuantity);
        if (diff.compareTo(java.math.BigDecimal.ZERO) != 0) {
            com.sergiocodev.app.model.Kardex kardex = new com.sergiocodev.app.model.Kardex();
            kardex.setProduct(entity.getLot().getProduct());
            kardex.setEstablishment(entity.getEstablishment());

            // Default to ADJUSTMENT if not specified (or infer based on diff)
            // If movementType provided in request, parse it.
            com.sergiocodev.app.model.Kardex.MovementType type = com.sergiocodev.app.model.Kardex.MovementType.ADJUSTMENT;
            if (request.getMovementType() != null) {
                try {
                    type = com.sergiocodev.app.model.Kardex.MovementType.valueOf(request.getMovementType());
                } catch (IllegalArgumentException e) {
                    // ignore, use default
                }
            } else {
                if (diff.compareTo(java.math.BigDecimal.ZERO) > 0)
                    type = com.sergiocodev.app.model.Kardex.MovementType.IN;
                else
                    type = com.sergiocodev.app.model.Kardex.MovementType.OUT;
            }
            kardex.setMovementType(type);

            // Kardex quantity is usually integer, but inventory is BigDecimal.
            // Assuming for now we cast to int or Kardex should be BigDecimal.
            // Requirement said "Stock global" but didn't specify type.
            // Kardex entity has Integer quantity. Let's cast for now.
            kardex.setQuantity(diff.abs().intValue());
            kardex.setBalance(newQuantity.intValue());
            kardex.setNotes(request.getNotes() != null ? request.getNotes() : "Manual Adjustment");

            kardexRepository.save(kardex);
        }

        return new InventoryResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getAll() {
        return repository.findAll().stream()
                .map(InventoryResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getByEstablishment(Long establishmentId) {
        // This would need a repo method findByEstablishmentId, but I'll use findAll and
        // filter for now
        // or just create the repo method if needed.
        return repository.findAll().stream()
                .filter(i -> i.getEstablishment().getId().equals(establishmentId))
                .map(InventoryResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getById(Long id) {
        return repository.findById(id)
                .map(InventoryResponse::new)
                .orElseThrow(() -> new RuntimeException("Inventory record not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getAlerts() {
        // Expiry within 3 months (90 days)
        return repository.findExpiringSoon(java.time.LocalDate.now().plusDays(90)).stream()
                .map(InventoryResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getLowStock() {
        return repository.findLowStock().stream()
                .map(InventoryResponse::new)
                .collect(Collectors.toList());
    }
}
