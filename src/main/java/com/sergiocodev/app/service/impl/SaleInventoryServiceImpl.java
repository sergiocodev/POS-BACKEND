package com.sergiocodev.app.service.impl;

import com.sergiocodev.app.service.interfaces.SaleInventoryService;
import com.sergiocodev.app.service.interfaces.StockMovementService;
import com.sergiocodev.app.repository.InventoryRepository;
import com.sergiocodev.app.repository.ProductUnitRepository;
import com.sergiocodev.app.repository.StockMovementRepository;
import com.sergiocodev.app.repository.UserRepository;
import com.sergiocodev.app.model.Sale;
import com.sergiocodev.app.model.SaleItem;
import com.sergiocodev.app.model.Inventory;
import com.sergiocodev.app.model.StockMovement;
import com.sergiocodev.app.model.Product;
import com.sergiocodev.app.model.ProductLot;
import com.sergiocodev.app.model.ProductUnit;
import com.sergiocodev.app.dto.sale.ProductForSaleResponse;
import com.sergiocodev.app.dto.sale.ProductSearchResponse;
import com.sergiocodev.app.dto.sale.BarcodeScanResponse;
import com.sergiocodev.app.exception.ResourceNotFoundException;
import com.sergiocodev.app.exception.StockInsufficientException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleInventoryServiceImpl implements SaleInventoryService {

    private final InventoryRepository inventoryRepository;
    private final StockMovementService stockMovementService;
    private final StockMovementRepository stockMovementRepository;
    private final ProductUnitRepository productUnitRepository;
    private final UserRepository userRepository;

    @Override
    public void validateStock(Long establishmentId, Long lotId, BigDecimal quantity) {
        Inventory inventory = inventoryRepository.findByEstablishmentIdAndLotId(establishmentId, lotId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for lot ID: " + lotId));

        if (inventory.getQuantity().compareTo(quantity) < 0) {
            throw new StockInsufficientException(
                    "Insufficient stock for lot: " + inventory.getLot().getLotCode());
        }
    }

    @Override
    public void updateInventory(Sale sale, SaleItem item, BigDecimal baseQuantity) {
        if (item.getLot() == null)
            return;

        Inventory inventory = inventoryRepository
                .findByEstablishmentIdAndLotId(sale.getEstablishment().getId(), item.getLot().getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "No inventory for lot: " + item.getLot().getLotCode()));

        inventory.setQuantity(inventory.getQuantity().subtract(baseQuantity));
        inventory.setLastMovement(LocalDateTime.now());
        inventoryRepository.save(inventory);

        stockMovementService.recordSaleMovement(
                sale.getEstablishment(), item.getLot(),
                baseQuantity, inventory.getQuantity(),
                sale.getId(), sale.getUser());
    }

    @Override
    public void reverseInventory(Sale sale, SaleItem item, String reason, Long userId) {
        if (item.getLot() != null) {
            Inventory inventory = inventoryRepository
                    .findByEstablishmentIdAndLotId(sale.getEstablishment().getId(),
                            item.getLot().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Inventory not found for lot: "
                                    + item.getLot().getLotCode()));

            inventory.setQuantity(inventory.getQuantity().add(item.getQuantity()));
            inventory.setLastMovement(LocalDateTime.now());
            inventoryRepository.save(inventory);

            if (reason.startsWith("Credit note")) {
                stockMovementService.recordReversalMovement(
                        sale.getEstablishment(), item.getLot(),
                        item.getQuantity(), inventory.getQuantity(),
                        reason, sale.getId(), userRepository.findById(userId).orElse(sale.getUser()));
            } else {
                StockMovement movement = new StockMovement();
                movement.setLot(item.getLot());
                movement.setEstablishment(sale.getEstablishment());
                movement.setType(StockMovement.MovementType.VOID_RETURN);
                movement.setQuantity(item.getQuantity());
                movement.setBalanceAfter(inventory.getQuantity());
                movement.setReferenceTable("sales");
                movement.setReferenceId(sale.getId());
                movement.setUser(userRepository.findById(userId).orElse(sale.getUser()));
                movement.setCreatedAt(LocalDateTime.now());
                stockMovementRepository.save(movement);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductForSaleResponse> listProductsForSale(Long establishmentId) {
        List<Inventory> inventoryList = inventoryRepository.findAllByEstablishmentId(establishmentId);

        return inventoryList.stream()
                .filter(inventory -> inventory.getLot() != null
                        && inventory.getLot().getExpiryDate() != null
                        && !inventory.getLot().getExpiryDate()
                                .isBefore(java.time.LocalDate.now())
                        && inventory.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                .sorted(java.util.Comparator.comparing(inventory -> inventory.getLot().getExpiryDate()))
                .flatMap(inventory -> {
                    ProductLot lot = inventory.getLot();
                    Product product = lot.getProduct();

                    String concentration = "";
                    if (product.getIngredients() != null && !product.getIngredients().isEmpty()) {
                        concentration = product.getIngredients().stream()
                                .map(pi -> pi.getActiveIngredient().getName() + " "
                                        + (pi.getConcentration() != null
                                                ? pi.getConcentration()
                                                : ""))
                                .collect(Collectors.joining(", "));
                    }
                    final String finalConcentration = concentration;

                    return product.getUnits().stream()
                            .map(pu -> new ProductForSaleResponse(
                                    inventory.getId(),
                                    product.getId(),
                                    pu.getId(),
                                    product.getTradeName(),
                                    product.getGenericName(),
                                    product.getDescription(),
                                    product.getPresentation() != null ? product
                                            .getPresentation()
                                            .getDescription()
                                            : null,
                                    finalConcentration,
                                    product.getCategory() != null
                                            ? product.getCategory()
                                                    .getName()
                                            : null,
                                    product.getLaboratory() != null
                                            ? product.getLaboratory()
                                                    .getName()
                                            : null,
                                    pu.getPrice(),
                                    inventory.getQuantity(),
                                    lot.getExpiryDate(),
                                    lot.getLotCode(),
                                    lot.getId(),
                                    product.getImageUrl(),
                                    pu.getBarcode(),
                                    inventory.getLocationShelf(),
                                    pu.getUnitName(),
                                    pu.getFactor(),
                                    product.getTaxType() != null
                                            ? product.getTaxType().getRate()
                                            : BigDecimal.ZERO));
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductSearchResponse> searchProductsForPOS(String query,
            Long establishmentId) {
        List<Inventory> inventoryList = inventoryRepository.searchProductsForPOS(query, establishmentId);
        return inventoryList.stream()
                .filter(inventory -> inventory.getLot() != null
                        && inventory.getLot().getExpiryDate() != null
                        && !inventory.getLot().getExpiryDate()
                                .isBefore(java.time.LocalDate.now())
                        && inventory.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                .sorted(java.util.Comparator.comparing(inventory -> inventory.getLot().getExpiryDate()))
                .flatMap(inventory -> {
                    ProductLot lot = inventory.getLot();
                    Product product = lot.getProduct();

                    String concentration = "";
                    if (product.getIngredients() != null && !product.getIngredients().isEmpty()) {
                        concentration = product.getIngredients().stream()
                                .map(pi -> pi.getActiveIngredient().getName() + " "
                                        + (pi.getConcentration() != null
                                                ? pi.getConcentration()
                                                : ""))
                                .collect(Collectors.joining(", "));
                    }
                    final String finalConcentration = concentration;

                    return product.getUnits().stream()
                            .map(pu -> new ProductSearchResponse(
                                    inventory.getId(),
                                    product.getId(),
                                    pu.getId(),
                                    product.getTradeName(),
                                    product.getGenericName(),
                                    product.getDescription(),
                                    product.getPresentation() != null ? product
                                            .getPresentation()
                                            .getDescription()
                                            : null,
                                    finalConcentration,
                                    product.getCategory() != null
                                            ? product.getCategory()
                                                    .getName()
                                            : null,
                                    product.getLaboratory() != null
                                            ? product.getLaboratory()
                                                    .getName()
                                            : null,
                                    pu.getPrice(),
                                    inventory.getQuantity(),
                                    lot.getExpiryDate(),
                                    lot.getLotCode(),
                                    lot.getId(),
                                    product.getImageUrl(),
                                    pu.getBarcode(),
                                    inventory.getLocationShelf(),
                                    pu.getUnitName(),
                                    pu.getFactor(),
                                    product.getTaxType() != null
                                            ? product.getTaxType().getRate()
                                            : BigDecimal.ZERO));
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BarcodeScanResponse getProductByBarcode(String barcode, Long establishmentId) {
        ProductUnit pu = productUnitRepository.findByBarcode(barcode).orElse(null);
        if (pu == null) {
            return new BarcodeScanResponse(
                    null,
                    "Producto no encontrado",
                    barcode,
                    BigDecimal.ZERO,
                    null, null, null, BigDecimal.ZERO, "No stock available", null, BigDecimal.ZERO);
        }

        Product product = pu.getProduct();
        Inventory inventory = inventoryRepository.findAllByEstablishmentId(establishmentId).stream()
                .filter(inv -> inv.getLot() != null
                        && inv.getLot().getProduct().getId().equals(product.getId())
                        && (inv.getLot().getExpiryDate() == null
                                || !inv.getLot().getExpiryDate()
                                        .isBefore(java.time.LocalDate.now()))
                        && inv.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                .findFirst().orElse(null);

        if (inventory == null) {
            return new BarcodeScanResponse(
                    null,
                    "Producto no encontrado",
                    barcode,
                    BigDecimal.ZERO,
                    null, null, null, BigDecimal.ZERO, "No stock available", null, BigDecimal.ZERO);
        }

        java.math.BigDecimal price = pu.getPrice();

        return new BarcodeScanResponse(
                product.getId(),
                product.getTradeName(),
                barcode,
                price,
                inventory.getLot().getId(),
                inventory.getLot().getLotCode(),
                inventory.getLot().getExpiryDate(),
                inventory.getQuantity(),
                "Stock available",
                product.getImageUrl(),
                product.getTaxType() != null ? product.getTaxType().getRate() : BigDecimal.ZERO);
    }
}
