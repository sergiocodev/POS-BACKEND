package com.sergiocodev.app.service.impl;

import com.sergiocodev.app.dto.report.CashMovementReport;
import com.sergiocodev.app.dto.report.CashSessionReport;
import com.sergiocodev.app.model.CashMovement;
import com.sergiocodev.app.model.CashSession;
import com.sergiocodev.app.repository.CashMovementRepository;
import com.sergiocodev.app.repository.CashSessionRepository;
import com.sergiocodev.app.service.interfaces.CashReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CashReportServiceImpl implements CashReportService {

    private static final Logger log = LoggerFactory.getLogger(CashReportServiceImpl.class);

    private final CashSessionRepository cashSessionRepository;
    private final CashMovementRepository cashMovementRepository;

    public CashReportServiceImpl(CashSessionRepository cashSessionRepository,
                                  CashMovementRepository cashMovementRepository) {
        this.cashSessionRepository = cashSessionRepository;
        this.cashMovementRepository = cashMovementRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashSessionReport> getCashSessions(LocalDateTime start, LocalDateTime end, Long establishmentId) {
        List<CashSession> sessions = cashSessionRepository.findByEstablishmentAndDateRange(
                establishmentId, start, end);

        return sessions.stream()
                .map(s -> new CashSessionReport(
                        s.getId(),
                        s.getCashRegister() != null ? s.getCashRegister().getName() : "N/A",
                        s.getUser() != null ? s.getUser().getFullName() : "N/A",
                        s.getOpeningBalance(),
                        s.getClosingBalance(),
                        s.getCalculatedBalance(),
                        s.getDiffAmount(),
                        s.getOpenedAt(),
                        s.getClosedAt(),
                        s.getStatus() != null ? s.getStatus().name() : "UNKNOWN"))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashMovementReport> getCashMovementsBySession(Long sessionId) {
        List<CashMovement> movements = cashMovementRepository.findByCashSessionId(sessionId);
        return movements.stream()
                .map(m -> new CashMovementReport(
                        m.getId(),
                        m.getCashConcept() != null ? m.getCashConcept().getName() : "N/A",
                        m.getCashConcept() != null ? m.getCashConcept().getType().name() : "UNKNOWN",
                        m.getAmount(),
                        m.getReference(),
                        m.getDescription() != null ? m.getDescription() : "Sin descripción",
                        m.getUser() != null ? m.getUser().getFullName() : "N/A",
                        m.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashMovementReport> getCashMovementsByPeriod(LocalDateTime start, LocalDateTime end, Long establishmentId) {
        List<CashSession> sessions = cashSessionRepository.findByEstablishmentAndDateRange(
                establishmentId, start, end);

        return sessions.stream()
                .flatMap(s -> cashMovementRepository.findByCashSessionId(s.getId()).stream())
                .sorted(Comparator.comparing(CashMovement::getCreatedAt))
                .map(m -> new CashMovementReport(
                        m.getId(),
                        m.getCashConcept() != null ? m.getCashConcept().getName() : "N/A",
                        m.getCashConcept() != null ? m.getCashConcept().getType().name() : "UNKNOWN",
                        m.getAmount(),
                        m.getReference(),
                        m.getDescription() != null ? m.getDescription() : "Sin descripción",
                        m.getUser() != null ? m.getUser().getFullName() : "N/A",
                        m.getCreatedAt()))
                .collect(Collectors.toList());
    }
}
