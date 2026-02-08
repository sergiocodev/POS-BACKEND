package com.sergiocodev.app.service;

import com.sergiocodev.app.exception.BadRequestException;
import com.sergiocodev.app.dto.cashsession.CashSessionRequest;
import com.sergiocodev.app.dto.cashsession.CashSessionResponse;
import com.sergiocodev.app.dto.cash.OpenDailySessionRequest;
import com.sergiocodev.app.dto.cash.SessionStatusResponse;
import com.sergiocodev.app.dto.cash.CloseSessionRequest;
import com.sergiocodev.app.dto.cash.CashOutflowRequest;
import com.sergiocodev.app.model.CashMovement.MovementType;
import com.sergiocodev.app.model.SalePayment.PaymentMethod;
import com.sergiocodev.app.model.SalePayment;
import com.sergiocodev.app.model.CashMovement;
import com.sergiocodev.app.model.CashSession;
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

                BigDecimal totalSales = salePaymentRepository.findBySaleCashSessionIdAndPaymentMethod(
                                session.getId(), PaymentMethod.EFECTIVO).stream()
                                .map(SalePayment::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalIncome = cashMovementRepository.findByCashSessionIdAndType(
                                session.getId(), MovementType.INCOME).stream()
                                .map(CashMovement::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalExpenses = cashMovementRepository.findByCashSessionIdAndType(
                                session.getId(), MovementType.EXPENSE).stream()
                                .map(CashMovement::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalPurchases = BigDecimal.ZERO;

                BigDecimal calculatedBalance = session.getOpeningBalance()
                                .add(totalSales)
                                .add(totalIncome)
                                .subtract(totalExpenses);

                return new SessionStatusResponse(
                                session.getId(),
                                session.getCashRegister().getName(),
                                session.getOpeningBalance(),
                                calculatedBalance,
                                totalSales,
                                totalIncome,
                                totalExpenses,
                                totalPurchases,
                                session.getOpenedAt(),
                                session.getStatus());
        }

        @Override
        @Transactional
        public CashMovement registerCashOutflow(
                        CashOutflowRequest request) {
                CashSession session = repository.findByUserIdAndStatus(request.userId(), CashSession.SessionStatus.OPEN)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No active session found for user: " + request.userId()));

                CashMovement movement = new CashMovement();
                movement.setCashSession(session);
                movement.setAmount(request.amount());
                movement.setType(MovementType.EXPENSE);
                movement.setDescription(request.description());

                if (request.conceptId() != null) {
                        movement.setCashConcept(cashConceptRepository.findById(request.conceptId())
                                        .orElseThrow(
                                                        () -> new ResourceNotFoundException("Concept not found")));
                }

                CashMovement saved = cashMovementRepository.save(movement);

                BigDecimal currentCalc = session.getCalculatedBalance() != null ? session.getCalculatedBalance()
                                : BigDecimal.ZERO;
                session.setCalculatedBalance(currentCalc.subtract(request.amount()));
                repository.save(session);

                return saved;
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
