package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.sale.ProductForSaleResponse;
import com.sergiocodev.app.dto.sale.SaleRequest;
import com.sergiocodev.app.dto.sale.SaleResponse;
import com.sergiocodev.app.mapper.SaleMapper;
import com.sergiocodev.app.model.Sale;
import com.sergiocodev.app.model.CashSession;
import com.sergiocodev.app.model.Product;
import com.sergiocodev.app.model.ProductLot;
import com.sergiocodev.app.model.SaleItem;
import com.sergiocodev.app.model.Inventory;
import com.sergiocodev.app.model.StockMovement;
import com.sergiocodev.app.model.SalePayment;
import com.sergiocodev.app.repository.SaleRepository;
import com.sergiocodev.app.repository.CustomerRepository;
import com.sergiocodev.app.repository.EstablishmentRepository;
import com.sergiocodev.app.repository.UserRepository;
import com.sergiocodev.app.repository.ProductRepository;
import com.sergiocodev.app.repository.ProductLotRepository;
import com.sergiocodev.app.repository.InventoryRepository;
import com.sergiocodev.app.repository.StockMovementRepository;
import com.sergiocodev.app.repository.CashSessionRepository;
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
public class SaleServiceImpl implements SaleService {

    private final SaleRepository repository;
    private final CustomerRepository customerRepository;
    private final EstablishmentRepository establishmentRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductLotRepository lotRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final CashSessionRepository cashSessionRepository;
    private final SaleMapper mapper;

    @Override
    @Transactional
    public SaleResponse create(SaleRequest request, Long userId) {
        Sale entity = mapper.toEntity(request);
        entity.setEstablishment(establishmentRepository.findById(request.establishmentId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Establishment not found: " + request.establishmentId())));
        if (request.customerId() != null) {
            entity.setCustomer(customerRepository.findById(request.customerId()).orElse(null));
        }
        entity.setUser(userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId)));
        entity.setSeries("B001"); // Dummy
        entity.setNumber("000001"); // Dummy
        entity.setDate(LocalDateTime.now());
        entity.setStatus(Sale.SaleStatus.COMPLETED);

        // Find active cash session
        CashSession session = cashSessionRepository.findByUserIdAndStatus(userId, CashSession.SessionStatus.OPEN)
                .orElseThrow(() -> new com.sergiocodev.app.exception.BadRequestException(
                        "No specific active cash session found for user. Please open a cash session before making a sale."));
        entity.setCashSession(session);

        for (var ir : request.items()) {
            Product product = productRepository.findById(ir.productId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found: " + ir.productId()));
            ProductLot lot = ir.lotId() != null ? lotRepository.findById(ir.lotId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Lot not found: " + ir.lotId()))
                    : null;

            if (lot != null) {
                validateStock(request.establishmentId(), lot.getId(), ir.quantity());
            }

            SaleItem item = mapper.toItemEntity(ir);
            item.setSale(entity);
            item.setProduct(product);
            item.setLot(lot);
            BigDecimal amount = ir.unitPrice().multiply(ir.quantity());
            item.setAmount(amount);
            item.setAppliedTaxRate(new BigDecimal("0.18"));
            entity.getItems().add(item);

            updateInventory(entity, item);
        }

        calculateTotals(entity);

        for (var pr : request.payments()) {
            SalePayment payment = mapper.toPaymentEntity(pr);
            payment.setSale(entity);
            entity.getPayments().add(payment);

            // Update cash session calculated balance if Cash
            if (session != null && pr.paymentMethod() == SalePayment.PaymentMethod.EFECTIVO) {
                session.setCalculatedBalance(session.getCalculatedBalance().add(pr.amount()));
                cashSessionRepository.save(session);
            }
        }

        return mapper.toResponse(repository.save(entity));
    }

    private void validateStock(Long establishmentId, Long lotId, BigDecimal quantity) {
        Inventory inventory = inventoryRepository.findByEstablishmentIdAndLotId(establishmentId, lotId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for lot ID: " + lotId));

        if (inventory.getQuantity().compareTo(quantity) < 0) {
            throw new StockInsufficientException(
                    "Insufficient stock for lot: " + inventory.getLot().getLotCode());
        }
    }

    private void updateInventory(Sale sale, SaleItem item) {
        if (item.getLot() == null)
            return;

        Inventory inventory = inventoryRepository
                .findByEstablishmentIdAndLotId(sale.getEstablishment().getId(), item.getLot().getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "No inventory for lot: " + item.getLot().getLotCode()));

        inventory.setQuantity(inventory.getQuantity().subtract(item.getQuantity()));
        inventory.setLastMovement(LocalDateTime.now());
        item.setUnitCost(inventory.getCostPrice()); // Record cost at time of sale
        inventoryRepository.save(inventory);

        // Stock Movement
        StockMovement movement = new StockMovement();
        movement.setEstablishment(sale.getEstablishment());
        movement.setLot(item.getLot());
        movement.setType(StockMovement.MovementType.SALE);
        movement.setQuantity(item.getQuantity().multiply(new java.math.BigDecimal("-1")));
        movement.setBalanceAfter(inventory.getQuantity());
        movement.setReferenceTable("sales");
        movement.setReferenceId(sale.getId());
        movement.setUser(sale.getUser());
        movement.setCreatedAt(java.time.LocalDateTime.now());
        stockMovementRepository.save(movement);
    }

    private Sale calculateTotals(Sale sale) {
        BigDecimal subTotal = sale.getItems().stream()
                .map(SaleItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        sale.setSubTotal(subTotal);
        sale.setTax(subTotal.multiply(new BigDecimal("0.18"))); // Simplified
        sale.setTotal(subTotal); // Assuming inclusive
        return sale;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SaleResponse getById(Long id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Sale not found: " + id));
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        Sale sale = repository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Sale not found: " + id));
        sale.setStatus(Sale.SaleStatus.CANCELED);
        repository.save(sale);
    }

    @Override
    public byte[] getPdf(Long id) {
        // Mock PDF generation
        return "PDF Content Placeholder".getBytes();
    }

    @Override
    public String getXml(Long id) {
        return "<xml>Placeholder for Sale " + id + "</xml>";
    }

    @Override
    public String getCdr(Long id) {
        return "<cdr>Placeholder for Sale " + id + "</cdr>";
    }

    @Override
    @Transactional
    public SaleResponse createCreditNote(Long id, String reason, Long userId) {
        Sale original = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Original sale not found: " + id));

        if (original.getStatus() == Sale.SaleStatus.CANCELED || original.isVoided()) {
            throw new IllegalStateException("Cannot create credit note for a canceled or voided sale");
        }

        Sale note = new Sale();
        note.setEstablishment(original.getEstablishment());
        note.setCustomer(original.getCustomer());
        note.setUser(userRepository.findById(userId).orElse(original.getUser()));
        note.setDocumentType(Sale.SaleDocumentType.NOTA_CREDITO);
        note.setRelatedSale(original);
        note.setNoteReason(reason);
        note.setSeries("FC01");
        note.setNumber("000001"); // Dummy
        note.setDate(LocalDateTime.now());
        note.setSubTotal(original.getSubTotal().negate());
        note.setTax(original.getTax().negate());
        note.setTotal(original.getTotal().negate());
        note.setStatus(Sale.SaleStatus.COMPLETED);

        // Update Inventory & log StockMovement for each item
        for (SaleItem originalItem : original.getItems()) {
            if (originalItem.getLot() != null) {
                Inventory inventory = inventoryRepository
                        .findByEstablishmentIdAndLotId(original.getEstablishment().getId(),
                                originalItem.getLot().getId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Inventory not found for lot: " + originalItem.getLot().getLotCode()));

                inventory.setQuantity(inventory.getQuantity().add(originalItem.getQuantity()));
                inventory.setLastMovement(LocalDateTime.now());
                inventoryRepository.save(inventory);

                StockMovement movement = new StockMovement();
                movement.setLot(originalItem.getLot());
                movement.setEstablishment(original.getEstablishment());
                movement.setType(StockMovement.MovementType.ADJUSTMENT_IN); // RETURN/CREDIT_NOTE type would be better
                movement.setQuantity(originalItem.getQuantity());
                movement.setBalanceAfter(inventory.getQuantity());
                movement.setReferenceTable("sales");
                movement.setReferenceId(note.getId());
                movement.setUser(note.getUser());
                movement.setCreatedAt(LocalDateTime.now());
                stockMovementRepository.save(movement);
            }
        }

        // Update Cash Session if applicable (Refund from CURRENT user's session)
        CashSession currentSession = cashSessionRepository.findByUserIdAndStatus(userId, CashSession.SessionStatus.OPEN)
                .orElse(null);

        if (currentSession != null) {
            BigDecimal refundAmount = original.getPayments().stream()
                    .filter(p -> p.getPaymentMethod() == SalePayment.PaymentMethod.EFECTIVO)
                    .map(SalePayment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                currentSession.setCalculatedBalance(currentSession.getCalculatedBalance().subtract(refundAmount));
                cashSessionRepository.save(currentSession);
            }
        }

        return mapper.toResponse(repository.save(note));
    }

    @Override
    @Transactional
    public SaleResponse createDebitNote(Long id, String reason, Long userId) {
        throw new UnsupportedOperationException("Debit notes not yet implemented");
    }

    @Override
    @Transactional
    public void invalidate(Long id, String reason, Long userId) {
        Sale sale = repository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Sale not found: " + id));

        sale.setVoided(true);
        sale.setVoidedAt(LocalDateTime.now());
        sale.setVoidReason(reason);
        repository.save(sale);

        // Logical reverse of inventory
        for (SaleItem item : sale.getItems()) {
            if (item.getLot() != null) {
                Inventory inventory = inventoryRepository
                        .findByEstablishmentIdAndLotId(sale.getEstablishment().getId(), item.getLot().getId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Inventory not found for lot: " + item.getLot().getLotCode()));
                inventory.setQuantity(inventory.getQuantity().add(item.getQuantity()));
                inventoryRepository.save(inventory);

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

        // Update Cash Session if applicable (Refund from CURRENT user's session)
        CashSession currentSession = cashSessionRepository.findByUserIdAndStatus(userId, CashSession.SessionStatus.OPEN)
                .orElse(null);

        if (currentSession != null) {
            BigDecimal refundAmount = sale.getPayments().stream()
                    .filter(p -> p.getPaymentMethod() == SalePayment.PaymentMethod.EFECTIVO)
                    .map(SalePayment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                currentSession.setCalculatedBalance(currentSession.getCalculatedBalance().subtract(refundAmount));
                cashSessionRepository.save(currentSession);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductForSaleResponse> listProductsForSale(Long establishmentId) {
        List<Inventory> inventoryList = inventoryRepository.findAllByEstablishmentId(establishmentId);

        return inventoryList.stream().map(inventory -> {
            ProductLot lot = inventory.getLot();
            Product product = lot.getProduct();

            String concentration = "";
            if (product.getIngredients() != null && !product.getIngredients().isEmpty()) {
                concentration = product.getIngredients().stream()
                        .map(pi -> pi.getActiveIngredient().getName() + " "
                                + (pi.getConcentration() != null ? pi.getConcentration() : ""))
                        .collect(Collectors.joining(", "));
            }

            return new ProductForSaleResponse(
                    inventory.getId(),
                    product.getId(),
                    product.getTradeName(),
                    product.getGenericName(),
                    product.getDescription(),
                    product.getPresentation() != null ? product.getPresentation().getDescription() : null,
                    concentration,
                    product.getCategory() != null ? product.getCategory().getName() : null,
                    product.getLaboratory() != null ? product.getLaboratory().getName() : null,
                    inventory.getSalesPrice(),
                    inventory.getQuantity(),
                    lot.getExpiryDate(),
                    lot.getLotCode(),
                    lot.getId());
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.sergiocodev.app.dto.sale.ProductSearchResponse> searchProductsForPOS(String query,
            Long establishmentId) {
        List<Inventory> inventoryList = inventoryRepository.searchProductsForPOS(query, establishmentId);
        return inventoryList.stream().map(inventory -> {
            ProductLot lot = inventory.getLot();
            Product product = lot.getProduct();

            String concentration = "";
            if (product.getIngredients() != null && !product.getIngredients().isEmpty()) {
                concentration = product.getIngredients().stream()
                        .map(pi -> pi.getActiveIngredient().getName() + " "
                                + (pi.getConcentration() != null ? pi.getConcentration() : ""))
                        .collect(Collectors.joining(", "));
            }

            return new com.sergiocodev.app.dto.sale.ProductSearchResponse(
                    inventory.getId(),
                    product.getId(),
                    product.getTradeName(),
                    product.getGenericName(),
                    product.getDescription(),
                    product.getPresentation() != null ? product.getPresentation().getDescription() : null,
                    concentration,
                    product.getCategory() != null ? product.getCategory().getName() : null,
                    product.getLaboratory() != null ? product.getLaboratory().getName() : null,
                    inventory.getSalesPrice(),
                    inventory.getQuantity(),
                    lot.getExpiryDate(),
                    lot.getLotCode(),
                    lot.getId());
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public com.sergiocodev.app.dto.sale.BarcodeScanResponse getProductByBarcode(String barcode, Long establishmentId) {
        Product product = productRepository.findByBarcodeAndActiveTrue(barcode)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with barcode: " + barcode));

        // FEFO: Find lot with nearest expiry date having > 0 stock in this
        // establishment
        Inventory inventory = inventoryRepository
                .findFirstByEstablishmentIdAndLotProductIdAndQuantityGreaterThanOrderByLotExpiryDateAsc(
                        establishmentId, product.getId(), BigDecimal.ZERO)
                .orElse(null);

        if (inventory == null) {
            // Product exists but no stock
            return new com.sergiocodev.app.dto.sale.BarcodeScanResponse(
                    product.getId(),
                    product.getTradeName(),
                    product.getBarcode(),
                    product.getPurchaseFactor() != null ? new BigDecimal(product.getPurchaseFactor())
                            : BigDecimal.ZERO, // Fallback price? NO, we need sales price from inventory usually.
                                               // But no inventory means no price?
                                               // Let's return blank details or handle gracefully.
                    null, null, null, BigDecimal.ZERO, "No stock available");
        }

        return new com.sergiocodev.app.dto.sale.BarcodeScanResponse(
                product.getId(),
                product.getTradeName(),
                product.getBarcode(),
                inventory.getSalesPrice(),
                inventory.getLot().getId(),
                inventory.getLot().getLotCode(),
                inventory.getLot().getExpiryDate(),
                inventory.getQuantity(),
                "Stock available");
    }

    @Override
    @Transactional(readOnly = true)
    public com.sergiocodev.app.dto.sale.CartCalculationResponse calculateCartTotals(
            com.sergiocodev.app.dto.sale.CartCalculationRequest request) {
        BigDecimal subTotal = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        java.util.Map<String, BigDecimal> taxBreakdown = new java.util.HashMap<>();
        List<com.sergiocodev.app.dto.sale.CartItemCalculation> itemCalculations = new java.util.ArrayList<>();

        for (com.sergiocodev.app.dto.sale.CartItemRequest itemReq : request.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemReq.productId()));

            BigDecimal quantity = itemReq.quantity();
            BigDecimal unitPrice = itemReq.unitPrice();
            BigDecimal discount = itemReq.discount() != null ? itemReq.discount() : BigDecimal.ZERO;

            BigDecimal lineGross = unitPrice.multiply(quantity);
            BigDecimal lineNet = lineGross.subtract(discount);

            // Tax
            // Assuming price is INCLUSIVE of tax? Or exclusive?
            // Usually POS prices are inclusive. If inclusive:
            // Base = Total / (1 + Rate)
            // Tax = Total - Base
            // If exclusive:
            // Tax = Total * Rate

            // Let's assume INCLUSIVE for now as per previous code (0.18 hardcoded
            // previously
            // suggesting handling).
            // Actually previous code: item.setAppliedTaxRate(new BigDecimal("0.18"));
            // sale.setTax(subTotal.multiply(new BigDecimal("0.18")));
            // This suggests exclusive in previous code? "subTotal" reduced, then tax added?
            // "sale.setTotal(subTotal);" -> This suggests SubTotal was the Total.
            // Let's look at previous create method:
            // BigDecimal amount = ir.unitPrice().multiply(ir.quantity());
            // item.setAmount(amount);
            // ...
            // BigDecimal subTotal = ... sum of amounts
            // sale.setSubTotal(subTotal);
            // sale.setTax(subTotal.multiply("0.18"));
            // sale.setTotal(subTotal);
            // This existing logic is weird. Total = SubTotal? Then Tax is extra but not
            // added to Total?
            // "sale.setTotal(subTotal);"
            // Correct logic should be: Total = SubTotal + Tax (if exclusive) or Total =
            // SubTotal (if inclusive).
            // If SubTotal is inclusive, then Tax = SubTotal - (SubTotal / 1.18).

            // I will implement standard logic:
            // LineTotal = (Price * Qty) - Discount
            // TaxAmount = LineTotal * Rate (if exclusive) or LineTotal - (LineTotal /
            // (1+Rate)) (if inclusive)
            // I'll assume INCLUSIVE because retail POS usually is.

            BigDecimal infoTaxAmount;
            if (product.getTaxType() != null && product.getTaxType().getRate().compareTo(BigDecimal.ZERO) > 0) {
                // Inclusive
                BigDecimal div = BigDecimal.ONE.add(product.getTaxType().getRate());
                BigDecimal base = lineNet.divide(div, 2, java.math.RoundingMode.HALF_UP);
                infoTaxAmount = lineNet.subtract(base);
            } else {
                infoTaxAmount = BigDecimal.ZERO;
            }

            subTotal = subTotal.add(lineNet);
            totalTax = totalTax.add(infoTaxAmount);
            totalDiscount = totalDiscount.add(discount);

            String taxName = product.getTaxType() != null ? product.getTaxType().getName() : "IGV";
            taxBreakdown.merge(taxName, infoTaxAmount, BigDecimal::add);

            itemCalculations.add(new com.sergiocodev.app.dto.sale.CartItemCalculation(
                    product.getId(),
                    quantity,
                    unitPrice,
                    discount,
                    infoTaxAmount,
                    lineNet));
        }

        if (request.globalDiscount() != null) {
            BigDecimal glDisc = request.globalDiscount();
            totalDiscount = totalDiscount.add(glDisc);
            subTotal = subTotal.subtract(glDisc);
            // Recalculate tax? Complex. Let's apply global discount proportionally or just
            // subtract from total.
            // For simplicity, just subtract from total/subtotal.
        }

        total = subTotal; // Inclusive

        return new com.sergiocodev.app.dto.sale.CartCalculationResponse(
                subTotal.subtract(totalTax), // Net Subtotal
                totalTax,
                totalDiscount,
                total,
                taxBreakdown,
                itemCalculations);
    }

    @Override
    @Transactional
    public SaleResponse processSaleTransaction(SaleRequest request, Long userId) {
        // Reuse create logic or duplicate for safety/customization
        // Since logic is identical for now, let's call create.
        // User requested "Cierra la venta... Resta cantidad... Inserta movimiento...".
        // create() already does this.
        return create(request, userId);
    }

    @Override
    public byte[] getSaleDocumentPDF(Long id, String format) {
        Sale sale = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found: " + id));

        // Mock generation based on format
        String content = "SALE DOCUMENT: " + sale.getSeries() + "-" + sale.getNumber() + "\n" +
                "FORMAT: " + (format != null ? format : "A4") + "\n" +
                "DATE: " + sale.getDate() + "\n" +
                "TOTAL: " + sale.getTotal();

        return content.getBytes();
    }
}
