package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.dto.cash.CashInflowRequest;
import com.sergiocodev.app.dto.cash.CashOutflowRequest;
import com.sergiocodev.app.dto.cash.CloseSessionRequest;
import com.sergiocodev.app.dto.cash.OpenDailySessionRequest;
import com.sergiocodev.app.dto.cash.SessionStatusResponse;
import com.sergiocodev.app.dto.cashsession.CashSessionRequest;
import com.sergiocodev.app.dto.cashsession.CashSessionResponse;
import com.sergiocodev.app.dto.cashsession.CashSessionSummaryResponse;
import com.sergiocodev.app.dto.cashmovement.CashMovementResponse;

import java.math.BigDecimal;
import java.util.List;

public interface CashSessionService {
        CashSessionResponse openSession(CashSessionRequest request, Long userId);

        CashSessionResponse closeSession(Long id, BigDecimal closingBalance);

        List<CashSessionResponse> getAll();
        
        org.springframework.data.domain.Page<CashSessionResponse> getAllPaged(Long establishmentId, org.springframework.data.domain.Pageable pageable);

        CashSessionResponse getById(Long id);

        CashSessionResponse getActiveSession(Long userId);

        CashSessionResponse getStatus(Long userId);

        CashSessionResponse closeActiveSession(Long userId, BigDecimal closingBalance);

        List<CashSessionResponse> getHistory(Long userId);

        SessionStatusResponse getCurrentSessionStatus(Long userId);

        CashSessionResponse openDailySession(OpenDailySessionRequest request);

        CashMovementResponse registerCashOutflow(CashOutflowRequest request);

        CashMovementResponse registerCashInflow(CashInflowRequest request);

        List<CashSessionSummaryResponse> getSummary(Long establishmentId);

        CashSessionResponse closeSessionAndReport(CloseSessionRequest request);
}
