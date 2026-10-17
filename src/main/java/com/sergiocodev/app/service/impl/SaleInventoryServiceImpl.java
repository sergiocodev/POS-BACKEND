package com.sergiocodev.app.service.impl;

import com.sergiocodev.app.service.interfaces.SaleInventoryService;
import com.sergiocodev.app.service.interfaces.StockMovementService;
import com.sergiocodev.app.repository.InventoryRepository;
import com.sergiocodev.app.repository.ProductUnitRepository;
import com.sergiocodev.app.repository.PurchaseItemRepository;
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
import com.sergiocodev.app.util.UrlHelper;

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
    private final PurchaseItemRepository purchaseItemRepository;

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

    private List<ProductUnit> getUnitsForInventory(Inventory inventory) {
        if (inventory == null || inventory.getLot() == null || inventory.getLot().getProduct() == null) {
            return java.util.Collections.emptyList();
        }
        ProductLot lot = inventory.getLot();
        Product product = lot.getProduct();
        List<ProductUnit> lotUnits = purchaseItemRepository.findUnitsByProductAndLotCode(
                product.getId(),
                lot.getLotCode()
        );

        if (lotUnits != null && !lotUnits.isEmpty()) {
            int maxFactor = lotUnits.stream()
                    .mapToInt(pu -> pu.getFactor() != null ? pu.getFactor() : 1)
                    .max()
                    .orElse(1);

            if (product.getUnits() != null) {
                return product.getUnits().stream()
                        .filter(pu -> (pu.getFactor() != null ? pu.getFactor() : 1) <= maxFactor)
                        .toList();
            }
            return lotUnits;
        }

        if (product.getUnits() != null) {
            return product.getUnits();
        }

        return java.util.Collections.emptyList();
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

                    List<com.sergiocodev.app.dto.product.ProductIngredientResponse> ingredients = product.getIngredients() != null
                            ? product.getIngredients().stream()
                                    .map(pi -> new com.sergiocodev.app.dto.product.ProductIngredientResponse(
                                            pi.getActiveIngredient().getId(),
                                            pi.getActiveIngredient().getName(),
                                            pi.getConcentration()))
                                    .collect(Collectors.toList())
                            : new java.util.ArrayList<>();

                    List<ProductUnit> units = getUnitsForInventory(inventory);

                    return units.stream()
                            .map(pu -> new ProductForSaleResponse(
                                    inventory.getId(),
                                    product.getId(),
                                    pu.getId(),
                                    product.getTradeName(),
                                    product.getGenericName(),
                                    product.getDescription(),
                                    product.getPresentation() != null ? product.getPresentation().getDescription() : null,
                                    product.getCategory() != null ? product.getCategory().getName() : null,
                                    product.getLaboratory() != null ? product.getLaboratory().getName() : null,
                                    pu.getPrice(),
                                    inventory.getQuantity(),
                                    lot.getExpiryDate(),
                                    lot.getLotCode(),
                                    lot.getId(),
                                    UrlHelper.toAbsoluteUrl(product.getImageUrl()),
                                    pu.getBarcode(),
                                    inventory.getLocationShelf(),
                                    pu.getUnitOfMeasure() != null ? pu.getUnitOfMeasure().getName() : null,
                                    pu.getFactor(),
                                    product.getTaxType() != null ? product.getTaxType().getRate() : BigDecimal.ZERO,
                                    ingredients,
                                    product.getPharmaceuticalForm() != null ? product.getPharmaceuticalForm().getName() : null));
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

                    List<ProductUnit> units = getUnitsForInventory(inventory);

                    return units.stream()
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
                                    UrlHelper.toAbsoluteUrl(product.getImageUrl()),
                                    pu.getBarcode(),
                                    inventory.getLocationShelf(),
                                    pu.getUnitOfMeasure() != null ? pu.getUnitOfMeasure().getName() : null,
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
                    null, null, null, null,
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
                        && inv.getQuantity().compareTo(BigDecimal.valueOf(pu.getFactor() != null ? pu.getFactor() : 1)) >= 0
                        && getUnitsForInventory(inv).stream().anyMatch(u -> u.getId().equals(pu.getId())))
                .sorted(java.util.Comparator.comparing(inv -> inv.getLot().getExpiryDate()))
                .findFirst().orElse(null);

        if (inventory == null) {
            return new BarcodeScanResponse(
                    product.getId(), pu.getId(), pu.getUnitOfMeasure() != null ? pu.getUnitOfMeasure().getName() : null, pu.getFactor(),
                    "Producto sin stock disponible",
                    barcode,
                    BigDecimal.ZERO,
                    null, null, null, BigDecimal.ZERO, "No stock available", null, BigDecimal.ZERO);
        }

        java.math.BigDecimal price = pu.getPrice();

        return new BarcodeScanResponse(
                product.getId(),
                pu.getId(),
                pu.getUnitOfMeasure() != null ? pu.getUnitOfMeasure().getName() : null,
                pu.getFactor(),
                product.getTradeName(),
                barcode,
                price,
                inventory.getLot().getId(),
                inventory.getLot().getLotCode(),
                inventory.getLot().getExpiryDate(),
                inventory.getQuantity(),
                "Stock available",
                UrlHelper.toAbsoluteUrl(product.getImageUrl()),
                product.getTaxType() != null ? product.getTaxType().getRate() : BigDecimal.ZERO);
    }
}
