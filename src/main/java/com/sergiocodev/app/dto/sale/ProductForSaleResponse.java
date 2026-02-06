package com.sergiocodev.app.dto.sale;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductForSaleResponse {
    private Long id; // Inventory ID or Product ID? Requirement says "ListProductsForSale", but we
                     // need stock. Likely Inventory ID or Product ID + Lot info.
                     // The requirement implies listing specific items available for sale.
                     // Since stock and expiration are per Lot/Inventory, this should probably be per
                     // Inventory item.
                     // Let's assume we return one row per Inventory record (Product + Lot + Stock).

    private Long productId;
    private String name;
    private String description;
    private String presentation; // Description of presentation
    private String concentration; // Calculated from ingredients
    private String category;
    private String laboratory;
    private BigDecimal salesPrice;
    private BigDecimal stock;
    private LocalDate expirationDate;
    private String lotCode;
    private Long lotId;
}
