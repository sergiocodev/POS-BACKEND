package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.cashsession.CashSessionRequest;
import com.sergiocodev.app.dto.cashsession.CashSessionResponse;
import com.sergiocodev.app.model.CashSession;
import com.sergiocodev.app.repository.CashRegisterRepository;
import com.sergiocodev.app.repository.CashSessionRepository;
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

    @Override
    @Transactional
    public CashSessionResponse openSession(CashSessionRequest request, Long userId) {
        CashSession entity = new CashSession();
        entity.setCashRegister(registerRepository.findById(request.getCashRegisterId()).orElse(null));
        entity.setUser(userRepository.findById(userId).orElse(null));
        entity.setOpeningBalance(request.getOpeningBalance());
        entity.setCalculatedBalance(request.getOpeningBalance());
        entity.setStatus(CashSession.SessionStatus.OPEN);
        entity.setOpenedAt(LocalDateTime.now());
        entity.setNotes(request.getNotes());
        return new CashSessionResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public CashSessionResponse closeSession(Long id, BigDecimal closingBalance) {
        CashSession entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        entity.setClosingBalance(closingBalance);
        entity.setClosedAt(LocalDateTime.now());
        entity.setStatus(CashSession.SessionStatus.CLOSED);

        // Calculate diff: closingBalance - calculatedBalance
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
                .orElseThrow(() -> new RuntimeException("Session not found"));
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
        // Alias for getActiveSession for now, but could include more calculated data
        return getActiveSession(userId);
    }

    @Override
    @Transactional
    public CashSessionResponse closeActiveSession(Long userId, BigDecimal closingBalance) {
        CashSession entity = repository.findByUserIdAndStatus(userId, CashSession.SessionStatus.OPEN)
                .orElseThrow(() -> new RuntimeException("No active session found for user"));

        entity.setClosingBalance(closingBalance);
        entity.setClosedAt(LocalDateTime.now());
        entity.setStatus(CashSession.SessionStatus.CLOSED);

        // Calculate diff: closingBalance - calculatedBalance
        if (entity.getCalculatedBalance() != null) {
            entity.setDiffAmount(closingBalance.subtract(entity.getCalculatedBalance()));
        }

        return new CashSessionResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashSessionResponse> getHistory(Long userId) {
        return repository.findByUserIdAndStatusOrderByOpenedAtDesc(userId, CashSession.SessionStatus.CLOSED).stream()
                .map(CashSessionResponse::new)
                .collect(Collectors.toList());
    }
}
