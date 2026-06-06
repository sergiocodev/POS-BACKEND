package com.sergiocodev.app.service.impl;
import com.sergiocodev.app.service.interfaces.CashMovementService;

import com.sergiocodev.app.dto.cashmovement.CashMovementRequest;
import com.sergiocodev.app.dto.cashmovement.CashMovementResponse;
import com.sergiocodev.app.exception.BadRequestException;
import com.sergiocodev.app.exception.ResourceNotFoundException;
import com.sergiocodev.app.model.CashConcept;
import com.sergiocodev.app.model.CashMovement;
import com.sergiocodev.app.model.CashSession;
import com.sergiocodev.app.model.User;
import com.sergiocodev.app.repository.CashConceptRepository;
import com.sergiocodev.app.repository.CashMovementRepository;
import com.sergiocodev.app.repository.CashSessionRepository;
import com.sergiocodev.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CashMovementServiceImpl implements CashMovementService {

    private final CashMovementRepository repository;
    private final CashSessionRepository sessionRepository;
    private final CashConceptRepository conceptRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<CashMovementResponse> findAll(String createdAt, String conceptName, String description, String type, String reference, String username, Long establishmentId, Pageable pageable) {
        Specification<CashMovement> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            if (createdAt != null && !createdAt.isBlank()) {
                predicates.add(cb.like(root.get("createdAt").as(String.class), "%" + createdAt + "%"));
            }
            if (conceptName != null && !conceptName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.join("cashConcept").get("name")), "%" + conceptName.toLowerCase() + "%"));
            }
            if (description != null && !description.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%"));
            }
            if (type != null && !type.isBlank()) {
                String typeUpper = type.toUpperCase();
                CashConcept.ConceptType conceptType = typeUpper.contains("IN") ? CashConcept.ConceptType.IN : 
                                                    (typeUpper.contains("EG") || typeUpper.contains("OUT") ? CashConcept.ConceptType.OUT : null);
                if (conceptType != null) {
                    predicates.add(cb.equal(root.join("cashConcept").get("type"), conceptType));
                }
            }
            if (reference != null && !reference.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("reference")), "%" + reference.toLowerCase() + "%"));
            }
            if (username != null && !username.isBlank()) {
                predicates.add(cb.like(cb.lower(root.join("user").get("username")), "%" + username.toLowerCase() + "%"));
            }

            if (establishmentId != null) {
                predicates.add(cb.equal(root.join("cashSession").join("cashRegister").join("establishment").get("id"), establishmentId));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return repository.findAll(spec, pageable).map(CashMovementResponse::new);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashMovementResponse> findBySessionId(Long sessionId) {
        return repository.findByCashSessionId(sessionId).stream()
                .map(CashMovementResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CashMovementResponse createManualMovement(CashMovementRequest request) {
        CashSession session = sessionRepository.findByUserIdAndStatus(request.userId(), CashSession.SessionStatus.OPEN)
                .orElseThrow(() -> new BadRequestException("No hay una sesión de caja abierta para el usuario"));

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        CashConcept concept = conceptRepository.findById(request.conceptId())
                .orElseThrow(() -> new ResourceNotFoundException("Concepto de caja no encontrado"));

        return registerMovementInternal(session, user, concept, request.amount(), request.reference(), request.description());
    }

    @Override
    @Transactional
    public CashMovementResponse registerInternalMovement(CashSession session, User user, CashConcept concept, BigDecimal amount, String reference, String description) {
        return registerMovementInternal(session, user, concept, amount, reference, description);
    }

    private CashMovementResponse registerMovementInternal(CashSession session, User user, CashConcept concept, BigDecimal amount, String reference, String description) {
        CashMovement movement = new CashMovement();
        movement.setCashSession(session);
        movement.setUser(user);
        movement.setCashConcept(concept);
        movement.setAmount(amount);
        movement.setReference(reference);
        movement.setDescription(description);
        movement.setCreatedAt(LocalDateTime.now());

        CashMovement saved = repository.save(movement);

        // Update session balance
        updateSessionBalance(session, concept.getType(), amount);

        return new CashMovementResponse(saved);
    }

    private void updateSessionBalance(CashSession session, CashConcept.ConceptType type, BigDecimal amount) {
        BigDecimal currentBalance = session.getCalculatedBalance() != null ? session.getCalculatedBalance() : BigDecimal.ZERO;
        if (type == CashConcept.ConceptType.IN) {
            session.setCalculatedBalance(currentBalance.add(amount));
        } else {
            session.setCalculatedBalance(currentBalance.subtract(amount));
        }
        sessionRepository.save(session);
    }

    @Override
    @Transactional(readOnly = true)
    public CashMovementResponse getById(Long id) {
        return repository.findById(id)
                .map(CashMovementResponse::new)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado"));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        CashMovement movement = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado"));

        // Revert session balance (Inverse of the original movement)
        CashConcept.ConceptType originalType = movement.getCashConcept().getType();
        CashConcept.ConceptType revertType = (originalType == CashConcept.ConceptType.IN) ? CashConcept.ConceptType.OUT : CashConcept.ConceptType.IN;
        
        updateSessionBalance(movement.getCashSession(), revertType, movement.getAmount());

        movement.setDeletedAt(LocalDateTime.now());
        repository.save(movement);
    }
}
