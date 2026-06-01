package com.sergiocodev.app.service.impl;
import com.sergiocodev.app.service.interfaces.StockMovementService;

import com.sergiocodev.app.dto.stockmovement.StockMovementRequest;
import com.sergiocodev.app.dto.stockmovement.StockMovementResponse;
import com.sergiocodev.app.event.StockMovementEvent;
import com.sergiocodev.app.model.*;
import com.sergiocodev.app.repository.*;
import com.sergiocodev.app.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockMovementServiceImpl implements StockMovementService {

    private final StockMovementRepository repository;
    private final EstablishmentRepository establishmentRepository;
    private final ProductLotRepository lotRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public StockMovementResponse create(StockMovementRequest request) {
        StockMovement entity = new StockMovement();
        entity.setEstablishment(establishmentRepository.findById(request.establishmentId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Establishment not found: " + request.establishmentId())));
        entity.setLot(lotRepository.findById(request.lotId())
                .orElseThrow(() -> new ResourceNotFoundException("Lot not found: " + request.lotId())));
        entity.setType(request.type());
        entity.setQuantity(request.quantity());
        entity.setBalanceAfter(request.balanceAfter());
        entity.setReferenceTable(request.referenceTable());
        entity.setReferenceId(request.referenceId());
        if (request.userId() != null) {
            entity.setUser(userRepository.findById(request.userId()).orElse(null));
        }

        return new StockMovementResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getAll() {
        return repository.findAll().stream()
                .map(StockMovementResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getByProduct(Long productId) {
        return repository.findByLotProductIdOrderByCreatedAtDesc(productId).stream()
                .map(StockMovementResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getByEstablishment(Long establishmentId) {
        return repository.findByEstablishmentIdOrderByCreatedAtDesc(establishmentId).stream()
                .map(StockMovementResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockMovementResponse> getByEstablishmentPaged(Long establishmentId, String productName, String lotCode, String type, String reason, String userName, Pageable pageable) {
        Specification<StockMovement> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.join("establishment").get("id"), establishmentId));

            if (productName != null && !productName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.join("lot").join("product").get("tradeName")), "%" + productName.toLowerCase() + "%"));
            }
            if (lotCode != null && !lotCode.isBlank()) {
                predicates.add(cb.like(cb.lower(root.join("lot").get("lotCode")), "%" + lotCode.toLowerCase() + "%"));
            }
            if (type != null && !type.isBlank()) {
                // type is an enum in StockMovement, we have to parse it or just compare string. 
                // Wait, type is StockMovement.MovementType
                try {
                    predicates.add(cb.equal(root.get("type"), StockMovement.MovementType.valueOf(type.toUpperCase())));
                } catch (IllegalArgumentException e) {
                    // Ignore if enum doesn't match
                }
            }
            if (reason != null && !reason.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("reason")), "%" + reason.toLowerCase() + "%"));
            }
            if (userName != null && !userName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.join("user").get("name")), "%" + userName.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return repository.findAll(spec, pageable).map(StockMovementResponse::new);
    }

    /**
     * Record a stock movement for a sale.
     */
    @Transactional
    public StockMovement recordSaleMovement(Establishment establishment, ProductLot lot,
                                              BigDecimal quantity, BigDecimal balanceAfter,
                                              Long saleId, User user) {
        StockMovement movement = new StockMovement();
        movement.setEstablishment(establishment);
        movement.setLot(lot);
        movement.setType(StockMovement.MovementType.SALE);
        movement.setQuantity(quantity.negate());
        movement.setBalanceAfter(balanceAfter);
        movement.setReferenceTable("sales");
        movement.setReferenceId(saleId);
        movement.setUser(user);
        movement.setCreatedAt(LocalDateTime.now());
        StockMovement saved = repository.save(movement);
        eventPublisher.publishEvent(new StockMovementEvent(this,
                establishment.getId(), lot.getProduct().getId(), lot.getId(),
                StockMovementEvent.MovementType.SALE,
                quantity.negate(), balanceAfter, "Sale", saleId, "sales", user.getId()));
        return saved;
    }

    /**
     * Record a stock movement for a purchase (stock entry).
     */
    @Transactional
    public StockMovement recordPurchaseMovement(Establishment establishment, ProductLot lot,
                                                  BigDecimal quantity, BigDecimal balanceAfter,
                                                  Long purchaseId, User user) {
        StockMovement movement = new StockMovement();
        movement.setEstablishment(establishment);
        movement.setLot(lot);
        movement.setType(StockMovement.MovementType.PURCHASE);
        movement.setQuantity(quantity);
        movement.setBalanceAfter(balanceAfter);
        movement.setReferenceTable("purchases");
        movement.setReferenceId(purchaseId);
        movement.setUser(user);
        movement.setCreatedAt(LocalDateTime.now());
        StockMovement saved = repository.save(movement);
        eventPublisher.publishEvent(new StockMovementEvent(this,
                establishment.getId(), lot.getProduct().getId(), lot.getId(),
                StockMovementEvent.MovementType.PURCHASE,
                quantity, balanceAfter, "Purchase", purchaseId, "purchases", user.getId()));
        return saved;
    }

    /**
     * Record a stock adjustment movement (theft, breakage, counting error, etc.).
     */
    @Transactional
    public StockMovement recordAdjustmentMovement(Establishment establishment, ProductLot lot,
                                                    BigDecimal quantityChange, BigDecimal balanceAfter,
                                                    String reason, User user) {
        StockMovement movement = new StockMovement();
        movement.setEstablishment(establishment);
        movement.setLot(lot);
        movement.setType(StockMovement.MovementType.ADJUSTMENT);
        movement.setQuantity(quantityChange);
        movement.setBalanceAfter(balanceAfter);
        movement.setReferenceTable("adjustments");
        movement.setUser(user);
        movement.setCreatedAt(LocalDateTime.now());
        StockMovement saved = repository.save(movement);
        eventPublisher.publishEvent(new StockMovementEvent(this,
                establishment.getId(), lot.getProduct().getId(), lot.getId(),
                StockMovementEvent.MovementType.ADJUSTMENT,
                quantityChange, balanceAfter, reason, null, "adjustments", user.getId()));
        return saved;
    }

    /**
     * Record a stock transfer movement (outgoing or incoming).
     */
    @Transactional
    public StockMovement recordTransferMovement(Establishment establishment, ProductLot lot,
                                                  BigDecimal quantity, BigDecimal balanceAfter,
                                                  Long transferId, User user, String referenceTable) {
        StockMovement movement = new StockMovement();
        movement.setEstablishment(establishment);
        movement.setLot(lot);
        movement.setType(StockMovement.MovementType.TRANSFER);
        movement.setQuantity(quantity);
        movement.setBalanceAfter(balanceAfter);
        movement.setReferenceTable(referenceTable);
        movement.setReferenceId(transferId);
        movement.setUser(user);
        movement.setCreatedAt(LocalDateTime.now());
        StockMovement saved = repository.save(movement);
        eventPublisher.publishEvent(new StockMovementEvent(this,
                establishment.getId(), lot.getProduct().getId(), lot.getId(),
                StockMovementEvent.MovementType.TRANSFER,
                quantity, balanceAfter, "Transfer", transferId, referenceTable, user.getId()));
        return saved;
    }

    /**
     * Record a stock reversal (e.g., cancelled sale returns stock).
     */
    @Transactional
    public StockMovement recordReversalMovement(Establishment establishment, ProductLot lot,
                                                  BigDecimal quantity, BigDecimal balanceAfter,
                                                  String reason, Long originalReferenceId, User user) {
        StockMovement movement = new StockMovement();
        movement.setEstablishment(establishment);
        movement.setLot(lot);
        movement.setType(StockMovement.MovementType.REVERSAL);
        movement.setQuantity(quantity);
        movement.setBalanceAfter(balanceAfter);
        movement.setReferenceTable("reversals");
        movement.setReferenceId(originalReferenceId);
        movement.setUser(user);
        movement.setCreatedAt(LocalDateTime.now());
        StockMovement saved = repository.save(movement);
        eventPublisher.publishEvent(new StockMovementEvent(this,
                establishment.getId(), lot.getProduct().getId(), lot.getId(),
                StockMovementEvent.MovementType.REVERSAL,
                quantity, balanceAfter, reason, originalReferenceId, "reversals", user.getId()));
        return saved;
    }
}
