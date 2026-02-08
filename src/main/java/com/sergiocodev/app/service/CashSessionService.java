package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.cash.CashOutflowRequest;
import com.sergiocodev.app.dto.cash.CloseSessionRequest;
import com.sergiocodev.app.dto.cash.OpenDailySessionRequest;
import com.sergiocodev.app.dto.cash.SessionStatusResponse;
import com.sergiocodev.app.dto.cashsession.CashSessionRequest;
import com.sergiocodev.app.dto.cashsession.CashSessionResponse;
import com.sergiocodev.app.model.CashMovement;

import java.math.BigDecimal;
import java.util.List;

public interface CashSessionService {
        CashSessionResponse openSession(CashSessionRequest request, Long userId);

        CashSessionResponse closeSession(Long id, BigDecimal closingBalance);

        List<CashSessionResponse> getAll();

        CashSessionResponse getById(Long id);

        CashSessionResponse getActiveSession(Long userId);

        CashSessionResponse getStatus(Long userId);

        CashSessionResponse closeActiveSession(Long userId, BigDecimal closingBalance);

        List<CashSessionResponse> getHistory(Long userId);

        SessionStatusResponse getCurrentSessionStatus(Long userId);

        CashSessionResponse openDailySession(OpenDailySessionRequest request);

        CashMovement registerCashOutflow(CashOutflowRequest request);

        CashSessionResponse closeSessionAndReport(CloseSessionRequest request);
}
