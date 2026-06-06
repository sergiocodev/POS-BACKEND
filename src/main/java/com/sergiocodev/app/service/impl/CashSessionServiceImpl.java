package com.sergiocodev.app.service.impl;

import com.sergiocodev.app.service.interfaces.CashSessionService;

import com.sergiocodev.app.exception.BadRequestException;
import com.sergiocodev.app.dto.cashsession.CashSessionRequest;
import com.sergiocodev.app.dto.cashsession.CashSessionResponse;
import com.sergiocodev.app.dto.cashsession.CashSessionSummaryResponse;
import com.sergiocodev.app.dto.cash.CashInflowRequest;
import com.sergiocodev.app.dto.cash.CashOutflowRequest;
import com.sergiocodev.app.dto.cash.OpenDailySessionRequest;
import com.sergiocodev.app.dto.cash.SessionStatusResponse;
import com.sergiocodev.app.dto.cash.CloseSessionRequest;
import com.sergiocodev.app.dto.cashmovement.CashMovementResponse;
import com.sergiocodev.app.model.AccountPayablePayment;
import com.sergiocodev.app.model.AccountReceivablePayment;
import com.sergiocodev.app.model.CashConcept.ConceptType;
import com.sergiocodev.app.model.SalePayment.PaymentMethod;
import com.sergiocodev.app.model.CashConcept;
import com.sergiocodev.app.model.User;
import com.sergiocodev.app.model.CashMovement;
import com.sergiocodev.app.service.interfaces.CashConceptService;
import com.sergiocodev.app.service.interfaces.CashMovementService;
import com.sergiocodev.app.model.CashSession;
import com.sergiocodev.app.repository.AccountPayablePaymentRepository;
import com.sergiocodev.app.repository.AccountReceivablePaymentRepository;
import com.sergiocodev.app.repository.CashConceptRepository;
import com.sergiocodev.app.repository.CashMovementRepository;
import com.sergiocodev.app.repository.CashRegisterRepository;
import com.sergiocodev.app.repository.CashSessionRepository;
import com.sergiocodev.app.repository.SalePaymentRepository;
import com.sergiocodev.app.exception.ResourceNotFoundException;
import com.sergiocodev.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CashSessionServiceImpl implements CashSessionService {

        private final CashSessionRepository repository;
        private final CashRegisterRepository registerRepository;
        private final UserRepository userRepository;
        private final SalePaymentRepository salePaymentRepository;
        private final CashMovementRepository cashMovementRepository;
        private final CashConceptRepository cashConceptRepository;
        private final CashMovementService cashMovementService;
        private final CashConceptService cashConceptService;
        private final AccountReceivablePaymentRepository arPaymentRepository;
        private final AccountPayablePaymentRepository apPaymentRepository;

        @Override
        @Transactional
        public CashSessionResponse openSession(CashSessionRequest request, Long userId) {
                List<CashSession> activeSessions = repository.findByUserIdAndStatusOrderByOpenedAtDesc(userId, CashSession.SessionStatus.OPEN);
                if (!activeSessions.isEmpty()) {
                        throw new BadRequestException("El usuario ya tiene una sesión de caja abierta. Debe cerrarla primero.");
                }

                repository.findByCashRegisterIdAndStatus(request.cashRegisterId(), CashSession.SessionStatus.OPEN)
                                .ifPresent(s -> {
                                        throw new BadRequestException("Already exists an open session for this cash register");
                                });

                CashSession entity = new CashSession();
                entity.setCashRegister(registerRepository.findById(request.cashRegisterId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Cash register not found: " + request.cashRegisterId())));
                entity.setUser(userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User not found: " + userId)));
                entity.setOpeningBalance(request.openingBalance());
                entity.setCalculatedBalance(request.openingBalance());
                entity.setStatus(CashSession.SessionStatus.OPEN);
                entity.setOpenedAt(LocalDateTime.now());
                entity.setNotes(request.notes());
                return new CashSessionResponse(repository.save(entity));
        }

        @Override
        @Transactional
        public CashSessionResponse closeSession(Long id, BigDecimal closingBalance) {
                CashSession entity = repository.findById(id)
                                .orElseThrow(
                                                () -> new ResourceNotFoundException("Session not found: " + id));
                entity.setClosingBalance(closingBalance);
                entity.setClosedAt(LocalDateTime.now());
                entity.setStatus(CashSession.SessionStatus.CLOSED);

                if (entity.getCalculatedBalance() != null) {
                        entity.setDiffAmount(closingBalance.subtract(entity.getCalculatedBalance()));
                }

                return new CashSessionResponse(repository.save(entity));
        }

        @Override
        @Transactional(readOnly = true)
        public List<CashSessionResponse> getAll() {
                return repository.findAll().stream()
                                .map(CashSessionResponse::new)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public org.springframework.data.domain.Page<CashSessionResponse> getAllPaged(Long establishmentId, org.springframework.data.domain.Pageable pageable) {
                return repository.findAllByEstablishmentId(establishmentId, pageable)
                                .map(CashSessionResponse::new);
        }

        @Override
        @Transactional(readOnly = true)
        public List<CashSessionSummaryResponse> getSummary(Long establishmentId) {
                List<CashSession> sessions;
                if (establishmentId != null) {
                        sessions = repository.findAllByEstablishmentId(establishmentId, org.springframework.data.domain.Pageable.unpaged()).getContent();
                } else {
                        sessions = repository.findAll();
                }

                long openSessions = 0;
                long closedSessions = 0;
                BigDecimal totalInflows = BigDecimal.ZERO;
                BigDecimal totalOutflows = BigDecimal.ZERO;

                for (CashSession session : sessions) {
                        if (session.getStatus() == CashSession.SessionStatus.OPEN) {
                                openSessions++;
                        } else {
                                closedSessions++;
                        }
                        
                        CashInflows inflows = calculateCashInflows(session.getId());
                        CashOutflows outflows = calculateCashOutflows(session.getId());
                        
                        totalInflows = totalInflows.add(inflows.getTotal());
                        totalOutflows = totalOutflows.add(outflows.getTotal());
                }

                return java.util.List.of(
                        new CashSessionSummaryResponse("CAJAS ABIERTAS", String.valueOf(openSessions), null, null, "+2", "up", "vs ayer"),
                        new CashSessionSummaryResponse("CAJAS CERRADAS", String.valueOf(closedSessions), null, null, "-1", "down", "vs ayer"),
                        new CashSessionSummaryResponse("INGRESO TOTAL", String.format("%.2f", totalInflows), "S/ ", null, "+5%", "up", "este mes"),
                        new CashSessionSummaryResponse("EGRESO TOTAL", String.format("%.2f", totalOutflows), "S/ ", null, "-2%", "down", "este mes")
                );
        }

        @Override
        @Transactional(readOnly = true)
        public CashSessionResponse getById(Long id) {
                return repository.findById(id)
                                .map(CashSessionResponse::new)
                                .orElseThrow(
                                                () -> new ResourceNotFoundException("Session not found: " + id));
        }

        @Override
        @Transactional(readOnly = true)
        public CashSessionResponse getActiveSession(Long userId, Long establishmentId) {
                CashSession globalSession = repository.findByUserIdAndStatus(userId, CashSession.SessionStatus.OPEN)
                                .orElse(null);

                if (globalSession == null) {
                        return null;
                }

                if (establishmentId != null && !globalSession.getCashRegister().getEstablishment().getId().equals(establishmentId)) {
                        throw new BadRequestException("El usuario tiene una caja abierta en la sucursal '" 
                                        + globalSession.getCashRegister().getEstablishment().getName() 
                                        + "'. Debe cerrarla antes de operar en esta sucursal.");
                }

                return new CashSessionResponse(globalSession);
        }

        @Override
        @Transactional(readOnly = true)
        public CashSessionResponse getStatus(Long userId, Long establishmentId) {
                return getActiveSession(userId, establishmentId);
        }

        @Override
        @Transactional
        public CashSessionResponse closeActiveSession(Long userId, Long establishmentId, BigDecimal closingBalance) {
                CashSession entity;
                if (establishmentId != null) {
                        entity = repository.findByUserIdAndCashRegisterEstablishmentIdAndStatus(userId, establishmentId, CashSession.SessionStatus.OPEN)
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "No active session found for user: " + userId + " in establishment: " + establishmentId));
                } else {
                        entity = repository.findByUserIdAndStatus(userId, CashSession.SessionStatus.OPEN)
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "No active session found for user: " + userId));
                }

                entity.setClosingBalance(closingBalance);
                entity.setClosedAt(LocalDateTime.now());
                entity.setStatus(CashSession.SessionStatus.CLOSED);

                if (entity.getCalculatedBalance() != null) {
                        entity.setDiffAmount(closingBalance.subtract(entity.getCalculatedBalance()));
                }

                return new CashSessionResponse(repository.save(entity));
        }

        @Override
        @Transactional(readOnly = true)
        public List<CashSessionResponse> getHistory(Long userId) {
                return repository.findByUserIdAndStatusOrderByOpenedAtDesc(userId, CashSession.SessionStatus.CLOSED)
                                .stream()
                                .map(CashSessionResponse::new)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional
        public CashSessionResponse openDailySession(OpenDailySessionRequest request) {
                List<CashSession> activeSessions = repository.findByUserIdAndStatusOrderByOpenedAtDesc(request.userId(), CashSession.SessionStatus.OPEN);
                if (!activeSessions.isEmpty()) {
                        throw new BadRequestException("El usuario ya tiene una sesión de caja abierta. Debe cerrarla primero.");
                }

                repository.findByCashRegisterIdAndStatus(request.cashRegisterId(), CashSession.SessionStatus.OPEN)
                                .ifPresent(s -> {
                                        throw new BadRequestException(
                                                        "Already exists an open session for this cash register");
                                });

                CashSession entity = new CashSession();
                entity.setCashRegister(registerRepository.findById(request.cashRegisterId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Cash register not found: " + request.cashRegisterId())));
                entity.setUser(userRepository.findById(request.userId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User not found: " + request.userId())));
                entity.setOpeningBalance(request.openingBalance());
                entity.setCalculatedBalance(request.openingBalance());
                entity.setStatus(CashSession.SessionStatus.OPEN);
                entity.setOpenedAt(LocalDateTime.now());
                entity.setNotes(request.notes());

                return new CashSessionResponse(repository.save(entity));
        }

        private record CashInflows(BigDecimal totalCashSales, BigDecimal totalArCashPayments, BigDecimal totalManualInflows) {
                public BigDecimal getTotal() {
                        return totalCashSales.add(totalArCashPayments).add(totalManualInflows);
                }
        }

        private record CashOutflows(BigDecimal totalApCashPayments, BigDecimal totalManualOutflows) {
                public BigDecimal getTotal() {
                        return totalApCashPayments.add(totalManualOutflows);
                }
        }

        private CashInflows calculateCashInflows(Long sessionId) {
                BigDecimal totalCashSales = salePaymentRepository
                                .sumByCashSessionIdAndPaymentMethod(sessionId, PaymentMethod.EFECTIVO);

                BigDecimal totalArCashPayments = arPaymentRepository
                                .findByCashSessionIdAndPaymentMethodAndDeletedAtIsNull(
                                                sessionId, AccountReceivablePayment.PaymentMethod.EFECTIVO)
                                .stream()
                                .map(AccountReceivablePayment::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalManualInflows = cashMovementRepository
                                .findByCashSessionIdAndCashConceptType(sessionId, ConceptType.IN)
                                .stream()
                                .filter(m -> !Boolean.TRUE.equals(m.getCashConcept().getIsSystem()))
                                .map(CashMovement::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                return new CashInflows(totalCashSales != null ? totalCashSales : BigDecimal.ZERO, 
                                       totalArCashPayments != null ? totalArCashPayments : BigDecimal.ZERO, 
                                       totalManualInflows != null ? totalManualInflows : BigDecimal.ZERO);
        }

        private CashOutflows calculateCashOutflows(Long sessionId) {
                BigDecimal totalApCashPayments = apPaymentRepository
                                .findByCashSessionIdAndPaymentMethodAndDeletedAtIsNull(
                                                sessionId, AccountPayablePayment.PaymentMethod.EFECTIVO)
                                .stream()
                                .map(AccountPayablePayment::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalManualOutflows = cashMovementRepository
                                .findByCashSessionIdAndCashConceptType(sessionId, ConceptType.OUT)
                                .stream()
                                .filter(m -> !Boolean.TRUE.equals(m.getCashConcept().getIsSystem()))
                                .map(CashMovement::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                return new CashOutflows(totalApCashPayments != null ? totalApCashPayments : BigDecimal.ZERO, 
                                        totalManualOutflows != null ? totalManualOutflows : BigDecimal.ZERO);
        }

        @Override
        @Transactional(readOnly = true)
        public SessionStatusResponse getCurrentSessionStatus(Long userId, Long establishmentId) {
                CashSession session = repository.findByUserIdAndStatus(userId, CashSession.SessionStatus.OPEN)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No active session found for user: " + userId));

                if (establishmentId != null && !session.getCashRegister().getEstablishment().getId().equals(establishmentId)) {
                        throw new BadRequestException("El usuario tiene una caja abierta en la sucursal '" 
                                        + session.getCashRegister().getEstablishment().getName() 
                                        + "'. Debe cerrarla antes de operar en esta sucursal.");
                }

                Long sessionId = session.getId();

                CashInflows inflows = calculateCashInflows(sessionId);
                CashOutflows outflows = calculateCashOutflows(sessionId);

                // ── Métodos digitales de venta (solo visualización) ───────────────────
                BigDecimal totalSalesYape = salePaymentRepository.sumByCashSessionIdAndPaymentMethod(sessionId,
                                PaymentMethod.YAPE);
                BigDecimal totalSalesPlin = salePaymentRepository.sumByCashSessionIdAndPaymentMethod(sessionId,
                                PaymentMethod.PLIN);
                BigDecimal totalSalesTarjeta = salePaymentRepository.sumByCashSessionIdAndPaymentMethod(sessionId,
                                PaymentMethod.TARJETA);
                BigDecimal totalSalesTransferencia = salePaymentRepository.sumByCashSessionIdAndPaymentMethod(sessionId,
                                PaymentMethod.TRANSFERENCIA);

                BigDecimal totalDigital = (totalSalesYape != null ? totalSalesYape : BigDecimal.ZERO)
                                .add(totalSalesPlin != null ? totalSalesPlin : BigDecimal.ZERO)
                                .add(totalSalesTarjeta != null ? totalSalesTarjeta : BigDecimal.ZERO)
                                .add(totalSalesTransferencia != null ? totalSalesTransferencia : BigDecimal.ZERO);

                // ── Saldo teórico ─────────────────────────────────────────────────────
                // Apertura + entradas en efectivo - salidas en efectivo
                BigDecimal calculatedBalance = session.getOpeningBalance()
                                .add(inflows.getTotal())
                                .subtract(outflows.getTotal());

                return new SessionStatusResponse(
                                sessionId,
                                session.getCashRegister().getName(),
                                session.getOpenedAt(),
                                session.getStatus(),
                                session.getOpeningBalance(),
                                calculatedBalance,
                                inflows.totalCashSales(),
                                inflows.totalArCashPayments(),
                                inflows.totalManualInflows(),
                                outflows.totalApCashPayments(),
                                outflows.totalManualOutflows(),
                                totalSalesYape,
                                totalSalesPlin,
                                totalSalesTarjeta,
                                totalSalesTransferencia,
                                totalDigital);
        }

        @Override
        @Transactional
        public CashMovementResponse registerCashOutflow(
                        CashOutflowRequest request) {
                CashSession session = repository.findByUserIdAndStatus(request.userId(), CashSession.SessionStatus.OPEN)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No active session found for user: " + request.userId()));

                User user = session.getUser();
                CashConcept concept = null;

                if (request.conceptId() != null) {
                        concept = cashConceptRepository.findById(request.conceptId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Concept not found"));
                } else {
                        // Fallback to a default OUT concept or throw error
                        concept = cashConceptService.findByType(CashConcept.ConceptType.OUT).stream()
                                        .findFirst()
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "No se encontró un concepto de egreso por defecto"));
                }

                return cashMovementService.registerInternalMovement(
                                session, user, concept, request.amount(), request.reference(), request.description());
        }

        @Override
        @Transactional
        public CashMovementResponse registerCashInflow(
                        CashInflowRequest request) {
                CashSession session = repository.findByUserIdAndStatus(request.userId(), CashSession.SessionStatus.OPEN)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No active session found for user: " + request.userId()));

                User user = session.getUser();
                CashConcept concept = null;

                if (request.conceptId() != null) {
                        concept = cashConceptRepository.findById(request.conceptId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Concept not found"));
                } else {
                        // Fallback to a default IN concept or throw error
                        concept = cashConceptService.findByType(CashConcept.ConceptType.IN).stream()
                                        .findFirst()
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "No se encontró un concepto de ingreso por defecto"));
                }

                return cashMovementService.registerInternalMovement(
                                session, user, concept, request.amount(), request.reference(), request.description());
        }

        @Override
        @Transactional
        public CashSessionResponse closeSessionAndReport(CloseSessionRequest request) {
                CashSession session = repository.findByUserIdAndStatus(request.userId(), CashSession.SessionStatus.OPEN)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No active session found for user: " + request.userId()));

                SessionStatusResponse status = getCurrentSessionStatus(request.userId(), null);
                session.setCalculatedBalance(status.calculatedBalance());

                session.setClosingBalance(request.closingBalance());
                session.setClosedAt(LocalDateTime.now());
                session.setStatus(CashSession.SessionStatus.CLOSED);
                session.setNotes(request.notes());

                if (session.getCalculatedBalance() != null) {
                        session.setDiffAmount(request.closingBalance().subtract(session.getCalculatedBalance()));
                }

                return new CashSessionResponse(repository.save(session));
        }
}
