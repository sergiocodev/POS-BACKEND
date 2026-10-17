package com.sergiocodev.app.mapper;

import com.sergiocodev.app.dto.inventory.InventoryResponse;
import com.sergiocodev.app.dto.productunit.ProductUnitResponse;
import com.sergiocodev.app.model.Inventory;
import com.sergiocodev.app.model.ProductLot;
import com.sergiocodev.app.model.ProductUnit;
import com.sergiocodev.app.repository.PurchaseItemRepository;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class InventoryMapper {

    @Autowired
    protected PurchaseItemRepository purchaseItemRepository;

    public InventoryResponse toResponse(Inventory entity) {
        if (entity == null) {
            return null;
        }

        List<ProductUnit> units = getUnitsForInventory(entity);

        BigDecimal salesPrice = null;
        String unitName = null;

        if (!units.isEmpty()) {
            ProductUnit preferredUnit = units.stream()
                    .filter(ProductUnit::isBaseUnit)
                    .findFirst()
                    .orElse(units.get(0));

            salesPrice = preferredUnit.getPrice();
            if (preferredUnit.getUnitOfMeasure() != null) {
                unitName = preferredUnit.getUnitOfMeasure().getName();
            }
        }

        List<ProductUnitResponse> unitResponses = units.stream()
                .map(u -> new ProductUnitResponse(
                        u.getId(),
                        u.getProduct().getId(),
                        u.getUnitOfMeasure().getId(),
                        u.getUnitOfMeasure().getName(),
                        u.getFactor(),
                        u.getBarcode(),
                        u.getSunatCode(),
                        u.getPrice(),
                        u.isBaseUnit()
                )).toList();

        return new InventoryResponse(
                entity.getId(),
                entity.getEstablishment() != null ? entity.getEstablishment().getId() : null,
                entity.getEstablishment() != null ? entity.getEstablishment().getName() : null,
                entity.getLot() != null ? entity.getLot().getId() : null,
                entity.getLot() != null ? entity.getLot().getLotCode() : null,
                entity.getLot() != null && entity.getLot().getProduct() != null ? entity.getLot().getProduct().getTradeName() : null,
                entity.getQuantity(),
                entity.getMinStock(),
                entity.getMaxStock(),
                entity.getLocationShelf(),
                entity.getCostPrice(),
                salesPrice,
                unitName,
                unitResponses,
                entity.getLastMovement(),
                entity.getLot() != null ? entity.getLot().getExpiryDate() : null
        );
    }

    protected List<ProductUnit> getUnitsForInventory(Inventory entity) {
        if (entity == null || entity.getLot() == null || entity.getLot().getProduct() == null) {
            return Collections.emptyList();
        }
        ProductLot lot = entity.getLot();
        List<ProductUnit> lotUnits = purchaseItemRepository.findUnitsByProductAndLotCode(
                lot.getProduct().getId(),
                lot.getLotCode()
        );

        if (lotUnits != null && !lotUnits.isEmpty()) {
            int maxFactor = lotUnits.stream()
                    .mapToInt(pu -> pu.getFactor() != null ? pu.getFactor() : 1)
                    .max()
                    .orElse(1);

            if (lot.getProduct().getUnits() != null) {
                return lot.getProduct().getUnits().stream()
                        .filter(pu -> (pu.getFactor() != null ? pu.getFactor() : 1) <= maxFactor)
                        .toList();
            }
            return lotUnits;
        }

        if (lot.getProduct().getUnits() != null) {
            return lot.getProduct().getUnits();
        }

        return Collections.emptyList();
    }
}
