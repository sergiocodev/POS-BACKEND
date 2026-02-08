package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.inventory.ExpiringLotResponse;
import com.sergiocodev.app.dto.inventory.InventoryRequest;
import com.sergiocodev.app.dto.inventory.InventoryResponse;
import com.sergiocodev.app.dto.inventory.KardexHistoryResponse;
import com.sergiocodev.app.dto.inventory.LowStockAlertResponse;
import com.sergiocodev.app.dto.inventory.StockAdjustmentRequest;
import com.sergiocodev.app.mapper.InventoryMapper;
import com.sergiocodev.app.model.Inventory;
import com.sergiocodev.app.model.StockMovement;
import com.sergiocodev.app.repository.EstablishmentRepository;
import com.sergiocodev.app.repository.InventoryRepository;
import com.sergiocodev.app.repository.ProductLotRepository;
import com.sergiocodev.app.repository.StockMovementRepository;
import com.sergiocodev.app.repository.SaleRepository;
import com.sergiocodev.app.model.Sale;
import com.sergiocodev.app.model.SaleItem;
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
    private final StockMovementRepository stockMovementRepository;
    private final SaleRepository saleRepository;
    private final InventoryMapper mapper;

    @Override
    @Transactional
    public InventoryResponse updateStock(InventoryRequest request) {
        Inventory entity = repository.findByEstablishmentIdAndLotId(request.establishmentId(), request.lotId())
                .orElseGet(() -> {
                    Inventory newInv = new Inventory();
                    newInv.setEstablishment(
                            establishmentRepository.findById(request.establishmentId()).orElse(null));
                    newInv.setLot(lotRepository.findById(request.lotId()).orElse(null));
                    newInv.setQuantity(java.math.BigDecimal.ZERO);
                    return newInv;
                });

        java.math.BigDecimal oldQuantity = entity.getQuantity();
        java.math.BigDecimal newQuantity = request.quantity();

        entity.setQuantity(newQuantity);
        if (request.costPrice() != null)
            entity.setCostPrice(request.costPrice());
        if (request.salesPrice() != null)
            entity.setSalesPrice(request.salesPrice());
        entity.setLastMovement(LocalDateTime.now());

        Inventory saved = repository.save(entity);

        java.math.BigDecimal diff = newQuantity.subtract(oldQuantity);
        if (diff.compareTo(java.math.BigDecimal.ZERO) != 0) {
            StockMovement movement = new StockMovement();
            movement.setLot(entity.getLot());
            movement.setEstablishment(entity.getEstablishment());

            StockMovement.MovementType type = diff.compareTo(java.math.BigDecimal.ZERO) > 0
                    ? StockMovement.MovementType.ADJUSTMENT_IN
                    : StockMovement.MovementType.ADJUSTMENT_OUT;

            if (request.movementType() != null) {
                try {
                    type = StockMovement.MovementType.valueOf(request.movementType());
                } catch (IllegalArgumentException e) {

                }
            }
            movement.setType(type);
            movement.setQuantity(diff.abs());
            movement.setBalanceAfter(newQuantity);
            movement.setReferenceTable("inventory");
            movement.setReferenceId(saved.getId());
            movement.setCreatedAt(java.time.LocalDateTime.now());

            stockMovementRepository.save(movement);
        }

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getByEstablishment(Long establishmentId) {
        return repository.findAll().stream()
                .filter(i -> i.getEstablishment().getId().equals(establishmentId))
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getById(Long id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Inventory record not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getAlerts() {
        return repository.findExpiringSoon(java.time.LocalDate.now().plusDays(90)).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getLowStock() {
        return repository.findLowStock().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LowStockAlertResponse> getLowStockAlerts() {
        return repository.findLowStockAlerts().stream()
                .map(i -> new LowStockAlertResponse(
                        i.getId(),
                        i.getLot() != null && i.getLot().getProduct() != null ? i.getLot().getProduct().getId() : null,
                        i.getLot() != null && i.getLot().getProduct() != null ? i.getLot().getProduct().getTradeName()
                                : null,
                        i.getLot() != null && i.getLot().getProduct() != null ? i.getLot().getProduct().getBarcode()
                                : null,
                        i.getLot() != null && i.getLot().getProduct() != null ? i.getLot().getProduct().getCode()
                                : null,
                        i.getQuantity(),
                        i.getMinStock(),
                        java.math.BigDecimal.valueOf(i.getMinStock()).subtract(i.getQuantity()),
                        i.getEstablishment().getId(),
                        i.getEstablishment().getName(),
                        i.getLot() != null ? i.getLot().getId() : null,
                        i.getLot() != null ? i.getLot().getLotCode() : null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpiringLotResponse> getExpiringLots(Integer days) {
        int checkDays = days != null ? days : 90;
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate endDate = today.plusDays(checkDays);

        return repository.findExpiringLotsBetween(today, endDate).stream()
                .map(i -> {
                    long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, i.getLot().getExpiryDate());
                    String alertLevel = "INFO";
                    if (daysUntil <= 30)
                        alertLevel = "CRITICAL";
                    else if (daysUntil <= 60)
                        alertLevel = "WARNING";

                    return new ExpiringLotResponse(
                            i.getId(),
                            i.getLot().getProduct().getId(),
                            i.getLot().getProduct().getTradeName(),
                            i.getLot().getProduct().getBarcode(),
                            i.getLot().getId(),
                            i.getLot().getLotCode(),
                            i.getLot().getExpiryDate(),
                            daysUntil,
                            i.getQuantity(),
                            i.getEstablishment().getId(),
                            i.getEstablishment().getName(),
                            alertLevel);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InventoryResponse registerStockAdjustment(StockAdjustmentRequest request) {
        Inventory entity = repository.findByEstablishmentIdAndLotId(request.establishmentId(), request.lotId())
                .orElseGet(() -> {
                    Inventory newInv = new Inventory();
                    newInv.setEstablishment(
                            establishmentRepository.findById(request.establishmentId()).orElseThrow(
                                    () -> new RuntimeException("Establishment not found")));
                    newInv.setLot(lotRepository.findById(request.lotId())
                            .orElseThrow(() -> new RuntimeException("Lot not found")));
                    newInv.setQuantity(java.math.BigDecimal.ZERO);
                    return newInv;
                });

        java.math.BigDecimal oldQuantity = entity.getQuantity();
        java.math.BigDecimal newQuantity = request.actualQuantity();
        java.math.BigDecimal diff = newQuantity.subtract(oldQuantity);

        entity.setQuantity(newQuantity);
        entity.setLastMovement(LocalDateTime.now());
        Inventory saved = repository.save(entity);

        if (diff.compareTo(java.math.BigDecimal.ZERO) != 0) {
            StockMovement movement = new StockMovement();
            movement.setLot(entity.getLot());
            movement.setEstablishment(entity.getEstablishment());
            movement.setType(diff.compareTo(java.math.BigDecimal.ZERO) > 0
                    ? StockMovement.MovementType.ADJUSTMENT_IN
                    : StockMovement.MovementType.ADJUSTMENT_OUT);
            movement.setQuantity(diff.abs());
            movement.setBalanceAfter(newQuantity);
            movement.setReferenceTable("inventory_adjustment");
            movement.setReferenceId(saved.getId());
            movement.setCreatedAt(LocalDateTime.now());

            stockMovementRepository.save(movement);
        }

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KardexHistoryResponse> getKardexHistoryByProduct(Long productId) {
        return stockMovementRepository.findKardexByProductId(productId).stream()
                .map(sm -> new KardexHistoryResponse(
                        sm.getId(),
                        sm.getEstablishment().getId(),
                        sm.getEstablishment().getName(),
                        sm.getLot().getProduct().getId(),
                        sm.getLot().getProduct().getTradeName(),
                        sm.getLot().getId(),
                        sm.getLot().getLotCode(),
                        sm.getType(),
                        sm.getQuantity(),
                        sm.getBalanceAfter(),
                        sm.getReferenceTable(),
                        sm.getReferenceId(),
                        sm.getUser() != null ? sm.getUser().getUsername() : null,
                        sm.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<KardexHistoryResponse> getKardexHistoryByLot(Long lotId) {
        return stockMovementRepository.findKardexByLotId(lotId).stream()
                .map(sm -> new KardexHistoryResponse(
                        sm.getId(),
                        sm.getEstablishment().getId(),
                        sm.getEstablishment().getName(),
                        sm.getLot().getProduct().getId(),
                        sm.getLot().getProduct().getTradeName(),
                        sm.getLot().getId(),
                        sm.getLot().getLotCode(),
                        sm.getType(),
                        sm.getQuantity(),
                        sm.getBalanceAfter(),
                        sm.getReferenceTable(),
                        sm.getReferenceId(),
                        sm.getUser() != null ? sm.getUser().getUsername() : null,
                        sm.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void reverseStockForSale(Long saleId) {
        Sale sale = saleRepository.findWithItemsById(saleId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada para reversión de stock"));

        for (SaleItem item : sale.getItems()) {
            if (item.getLot() == null)
                continue;

            Inventory inventory = repository.findByEstablishmentIdAndLotId(
                    sale.getEstablishment().getId(),
                    item.getLot().getId())
                    .orElseGet(() -> {
                        Inventory newInv = new Inventory();
                        newInv.setEstablishment(sale.getEstablishment());
                        newInv.setLot(item.getLot());
                        newInv.setQuantity(java.math.BigDecimal.ZERO);
                        return newInv;
                    });

            java.math.BigDecimal oldQuantity = inventory.getQuantity();
            java.math.BigDecimal quantityToReturn = item.getQuantity();
            java.math.BigDecimal newQuantity = oldQuantity.add(quantityToReturn);

            inventory.setQuantity(newQuantity);
            inventory.setLastMovement(LocalDateTime.now());
            repository.save(inventory);

            StockMovement movement = new StockMovement();
            movement.setLot(item.getLot());
            movement.setEstablishment(sale.getEstablishment());
            movement.setType(StockMovement.MovementType.ADJUSTMENT_IN);
            movement.setQuantity(quantityToReturn);
            movement.setBalanceAfter(newQuantity);
            movement.setReferenceTable("sales");
            movement.setReferenceId(sale.getId());
            movement.setCreatedAt(LocalDateTime.now());

            stockMovementRepository.save(movement);
        }
    }
}
