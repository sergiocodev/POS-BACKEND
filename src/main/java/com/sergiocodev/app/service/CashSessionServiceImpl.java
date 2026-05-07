package com.sergiocodev.app.service;

import com.sergiocodev.app.exception.BadRequestException;
import com.sergiocodev.app.dto.cashsession.CashSessionRequest;
import com.sergiocodev.app.dto.cashsession.CashSessionResponse;
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
        public CashSessionResponse getById(Long id) {
                return repository.findById(id)
                                .map(CashSessionResponse::new)
                                .orElseThrow(
                                                () -> new ResourceNotFoundException("Session not found: " + id));
        }

        @Override
        @Transactional(readOnly = true)
        public CashSessionResponse getActiveSession(Long userId) {
                return repository.findByUserIdAndStatus(userId, CashSession.SessionStatus.OPEN)
                                .map(CashSessionResponse::new)
                                .orElse(null);
        }

        @Override
        @Transactional(readOnly = true)
        public CashSessionResponse getStatus(Long userId) {
                return getActiveSession(userId);
        }

        @Override
        @Transactional
        public CashSessionResponse closeActiveSession(Long userId, BigDecimal closingBalance) {
                CashSession entity = repository.findByUserIdAndStatus(userId, CashSession.SessionStatus.OPEN)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No active session found for user: " + userId));

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

        @Override
        @Transactional(readOnly = true)
        public SessionStatusResponse getCurrentSessionStatus(Long userId) {
                CashSession session = repository.findByUserIdAndStatus(userId, CashSession.SessionStatus.OPEN)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No active session found for user: " + userId));

                Long sessionId = session.getId();

                // ── Entradas de efectivo ──────────────────────────────────────────────
                // 1. Ventas cobradas en efectivo
                BigDecimal totalCashSales = salePaymentRepository
                                .sumByCashSessionIdAndPaymentMethod(sessionId, PaymentMethod.EFECTIVO);

                // 2. Cobros de CxC (cuenta por cobrar) en efectivo durante este turno
                BigDecimal totalArCashPayments = arPaymentRepository
                                .findByCashSessionIdAndPaymentMethodAndDeletedAtIsNull(
                                                sessionId, AccountReceivablePayment.PaymentMethod.EFECTIVO)
                                .stream()
                                .map(AccountReceivablePayment::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // 3. Ingresos manuales (CashMovements tipo IN)
                BigDecimal totalCashInflows = cashMovementRepository
                                .findByCashSessionIdAndCashConceptType(sessionId, ConceptType.IN)
                                .stream()
                                .filter(m -> !Boolean.TRUE.equals(m.getCashConcept().getIsSystem()))
                                .map(CashMovement::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // ── Salidas de efectivo ───────────────────────────────────────────────
                // 4. Pagos a proveedores (CxP) en efectivo durante este turno
                BigDecimal totalApCashPayments = apPaymentRepository
                                .findByCashSessionIdAndPaymentMethodAndDeletedAtIsNull(
                                                sessionId, AccountPayablePayment.PaymentMethod.EFECTIVO)
                                .stream()
                                .map(AccountPayablePayment::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // 5. Egresos manuales (CashMovements tipo OUT)
                BigDecimal totalCashOutflows = cashMovementRepository
                                .findByCashSessionIdAndCashConceptType(sessionId, ConceptType.OUT)
                                .stream()
                                .filter(m -> !Boolean.TRUE.equals(m.getCashConcept().getIsSystem()))
                                .map(CashMovement::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // ── Métodos digitales de venta (solo visualización) ───────────────────
                BigDecimal totalSalesYape = salePaymentRepository.sumByCashSessionIdAndPaymentMethod(sessionId,
                                PaymentMethod.YAPE);
                BigDecimal totalSalesPlin = salePaymentRepository.sumByCashSessionIdAndPaymentMethod(sessionId,
                                PaymentMethod.PLIN);
                BigDecimal totalSalesTarjeta = salePaymentRepository.sumByCashSessionIdAndPaymentMethod(sessionId,
                                PaymentMethod.TARJETA);
                BigDecimal totalSalesTransferencia = salePaymentRepository.sumByCashSessionIdAndPaymentMethod(sessionId,
                                PaymentMethod.TRANSFERENCIA);

                BigDecimal totalDigital = totalSalesYape
                                .add(totalSalesPlin)
                                .add(totalSalesTarjeta)
                                .add(totalSalesTransferencia);

                // ── Saldo teórico ─────────────────────────────────────────────────────
                // Apertura + entradas en efectivo - salidas en efectivo
                BigDecimal calculatedBalance = session.getOpeningBalance()
                                .add(totalCashSales)
                                .add(totalArCashPayments)
                                .add(totalCashInflows)
                                .subtract(totalApCashPayments)
                                .subtract(totalCashOutflows);

                return new SessionStatusResponse(
                                sessionId,
                                session.getCashRegister().getName(),
                                session.getOpenedAt(),
                                session.getStatus(),
                                session.getOpeningBalance(),
                                calculatedBalance,
                                totalCashSales,
                                totalArCashPayments,
                                totalCashInflows,
                                totalApCashPayments,
                                totalCashOutflows,
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

                SessionStatusResponse status = getCurrentSessionStatus(request.userId());
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
