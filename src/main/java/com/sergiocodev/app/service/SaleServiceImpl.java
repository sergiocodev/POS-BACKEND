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
}
