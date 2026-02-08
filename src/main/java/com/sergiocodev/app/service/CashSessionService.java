package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.cashsession.CashSessionRequest;
import com.sergiocodev.app.dto.cashsession.CashSessionResponse;
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

    com.sergiocodev.app.dto.cash.SessionStatusResponse getCurrentSessionStatus(Long userId);

    com.sergiocodev.app.dto.cashsession.CashSessionResponse openDailySession(
            com.sergiocodev.app.dto.cash.OpenDailySessionRequest request);

    com.sergiocodev.app.model.CashMovement registerCashOutflow(
            com.sergiocodev.app.dto.cash.CashOutflowRequest request);

    com.sergiocodev.app.dto.cashsession.CashSessionResponse closeSessionAndReport(
            com.sergiocodev.app.dto.cash.CloseSessionRequest request);
}
