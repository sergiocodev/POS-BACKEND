package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.dto.report.CashMovementReport;
import com.sergiocodev.app.dto.report.CashSessionReport;

import java.time.LocalDateTime;
import java.util.List;

public interface CashReportService {
    List<CashSessionReport> getCashSessions(LocalDateTime start, LocalDateTime end, Long establishmentId);

    List<CashMovementReport> getCashMovementsBySession(Long sessionId);

    List<CashMovementReport> getCashMovementsByPeriod(LocalDateTime start, LocalDateTime end, Long establishmentId);
}
