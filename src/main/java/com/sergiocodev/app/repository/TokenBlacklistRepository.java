package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
    boolean existsByJti(String jti);

    /**
     * Limpia la base de datos eliminando físicamente los tokens en la lista negra
     * cuya fecha de expiración haya pasado para liberar espacio y mejorar el rendimiento de validación.
     *
     * @param now La fecha representativa del momento actual, cualquier token menor a él, será eliminado.
     */
    @Modifying
    @Query("DELETE FROM TokenBlacklist tb WHERE tb.expiryDate < :now")
    int deleteExpiredTokens(@Param("now") Instant now);
}
