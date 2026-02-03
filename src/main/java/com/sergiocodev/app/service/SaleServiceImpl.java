package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.sale.SaleRequest;
import com.sergiocodev.app.dto.sale.SaleResponse;
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

    @Override
    @Transactional
    public SaleResponse create(SaleRequest request, Long userId) {
        Sale entity = new Sale();
        entity.setEstablishment(establishmentRepository.findById(request.getEstablishmentId()).orElse(null));
        if (request.getCustomerId() != null) {
            entity.setCustomer(customerRepository.findById(request.getCustomerId()).orElse(null));
        }
        entity.setUser(userRepository.findById(userId).orElse(null));
        entity.setDocumentType(request.getDocumentType());
        entity.setSeries("B001"); // Dummy
        entity.setNumber("000001"); // Dummy
        entity.setDate(LocalDateTime.now());
        entity.setStatus(Sale.SaleStatus.COMPLETED);

        // Find active cash session
        CashSession session = cashSessionRepository.findAll().stream()
                .filter(s -> s.getUser().getId().equals(userId) && s.getStatus() == CashSession.SessionStatus.OPEN)
                .findFirst().orElse(null);
        entity.setCashSession(session);

        BigDecimal subTotal = BigDecimal.ZERO;
        for (var ir : request.getItems()) {
            Product product = productRepository.findById(ir.getProductId()).orElse(null);
            ProductLot lot = ir.getLotId() != null ? lotRepository.findById(ir.getLotId()).orElse(null) : null;

            SaleItem item = new SaleItem();
            item.setSale(entity);
            item.setProduct(product);
            item.setLot(lot);
            item.setQuantity(ir.getQuantity());
            item.setUnitPrice(ir.getUnitPrice());
            BigDecimal amount = ir.getUnitPrice().multiply(ir.getQuantity());
            item.setAmount(amount);
            item.setAppliedTaxRate(new BigDecimal("0.18"));
            entity.getItems().add(item);

            subTotal = subTotal.add(amount);

            // Update Inventory
            if (lot != null) {
                Inventory inventory = inventoryRepository
                        .findByEstablishmentIdAndLotId(request.getEstablishmentId(), lot.getId())
                        .orElseThrow(() -> new RuntimeException("No inventory for lot: " + lot.getLotCode()));

                inventory.setQuantity(inventory.getQuantity().subtract(ir.getQuantity()));
                inventory.setLastMovement(LocalDateTime.now());
                item.setUnitCost(inventory.getCostPrice()); // Record cost at time of sale
                inventoryRepository.save(inventory);

                // Stock Movement
                StockMovement movement = new StockMovement();
                movement.setEstablishment(entity.getEstablishment());
                movement.setLot(lot);
                movement.setType(StockMovement.MovementType.SALE);
                movement.setQuantity(ir.getQuantity().multiply(new java.math.BigDecimal("-1")));
                movement.setBalanceAfter(inventory.getQuantity());
                movement.setReferenceTable("sales");
                movement.setReferenceId(entity.getId());
                movement.setUser(entity.getUser());
                movement.setCreatedAt(java.time.LocalDateTime.now());
                stockMovementRepository.save(movement);
            }
        }

        entity.setSubTotal(subTotal);
        entity.setTotal(subTotal); // Assuming inclusive
        entity.setTax(subTotal.multiply(new BigDecimal("0.18"))); // Simplified

        for (var pr : request.getPayments()) {
            SalePayment payment = new SalePayment();
            payment.setSale(entity);
            payment.setPaymentMethod(pr.getPaymentMethod());
            payment.setAmount(pr.getAmount());
            payment.setReference(pr.getReference());
            entity.getPayments().add(payment);

            // Update cash session calculated balance if Cash
            if (session != null && pr.getPaymentMethod() == SalePayment.PaymentMethod.EFECTIVO) {
                session.setCalculatedBalance(session.getCalculatedBalance().add(pr.getAmount()));
                cashSessionRepository.save(session);
            }
        }

        return new SaleResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponse> getAll() {
        return repository.findAll().stream()
                .map(SaleResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SaleResponse getById(Long id) {
        return repository.findById(id)
                .map(SaleResponse::new)
                .orElseThrow(() -> new RuntimeException("Sale not found"));
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        Sale sale = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found"));
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
        // Return URL or Content. Using content for now as requested "Download XML"
        // usually implies file.
        return "<xml>Placeholder for Sale " + id + "</xml>";
    }

    @Override
    public String getCdr(Long id) {
        // Return URL or Content
        return "<cdr>Placeholder for Sale " + id + "</cdr>";
    }

    @Override
    @Transactional
    public SaleResponse createCreditNote(Long id, String reason, Long userId) {
        Sale original = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found"));

        if (original.getStatus() == Sale.SaleStatus.CANCELED || original.isVoided()) {
            throw new RuntimeException("Cannot create credit note for a canceled or voided sale");
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
                        .orElseThrow(() -> new RuntimeException("Inventory not found for lot"));

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

        // Update Cash Session if applicable
        CashSession session = original.getCashSession();
        if (session != null && session.getStatus() == CashSession.SessionStatus.OPEN) {
            BigDecimal refundAmount = original.getPayments().stream()
                    .filter(p -> p.getPaymentMethod() == SalePayment.PaymentMethod.EFECTIVO)
                    .map(SalePayment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            session.setCalculatedBalance(session.getCalculatedBalance().subtract(refundAmount));
            cashSessionRepository.save(session);
        }

        return new SaleResponse(repository.save(note));
    }

    @Override
    @Transactional
    public SaleResponse createDebitNote(Long id, String reason, Long userId) {
        // Implementation similar to credit note but with positive amounts and inverse
        // inventory if applicable
        // For simplicity in this step, throwing exception or placeholder
        throw new UnsupportedOperationException("Debit notes not yet implemented");
    }

    @Override
    @Transactional
    public void invalidate(Long id, String reason, Long userId) {
        Sale sale = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found"));

        sale.setVoided(true);
        sale.setVoidedAt(LocalDateTime.now());
        sale.setVoidReason(reason);
        repository.save(sale);

        // Logical reverse of inventory
        for (SaleItem item : sale.getItems()) {
            if (item.getLot() != null) {
                Inventory inventory = inventoryRepository
                        .findByEstablishmentIdAndLotId(sale.getEstablishment().getId(), item.getLot().getId())
                        .orElseThrow(() -> new RuntimeException("Inventory not found"));
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

        // Update Cash Session if applicable
        CashSession session = sale.getCashSession();
        if (session != null && session.getStatus() == CashSession.SessionStatus.OPEN) {
            BigDecimal refundAmount = sale.getPayments().stream()
                    .filter(p -> p.getPaymentMethod() == SalePayment.PaymentMethod.EFECTIVO)
                    .map(SalePayment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                session.setCalculatedBalance(session.getCalculatedBalance().subtract(refundAmount));
                cashSessionRepository.save(session);
            }
        }
    }
}
