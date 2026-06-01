package com.sergiocodev.app.task;

import com.sergiocodev.app.repository.TokenBlacklistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Tarea programada que limpia periódicamente los tokens blacklisted
 * cuya fecha de expiración ya haya pasado.
 * Esto evita que la tabla token_blacklist crezca indefinidamente.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupTask {

    private final TokenBlacklistRepository tokenBlacklistRepository;

    /**
     * Elimina todos los tokens expirados de la blacklist.
     * Se ejecuta cada hora por defecto, configurable via:
     * {@code app.token-blacklist.cleanup-rate} (en milisegundos).
     */
    @Scheduled(fixedRateString = "${app.token-blacklist.cleanup-rate:3600000}")
    @Transactional
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        int deleted = tokenBlacklistRepository.deleteExpiredTokens(now);
        if (deleted > 0) {
            log.info("Limpieza de tokens blacklisted: {} token(s) expirado(s) eliminado(s)", deleted);
        } else {
            log.debug("Limpieza de tokens blacklisted: sin tokens expirados para eliminar");
        }
    }
}
