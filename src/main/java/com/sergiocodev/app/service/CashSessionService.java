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
}
