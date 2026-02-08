package com.sergiocodev.app.dto.cashsession;

import com.sergiocodev.app.model.CashSession;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CashSessionResponseTest {

    @Test
    void constructor_ShouldHandleNullUserAndRegister() {
        // Arrange
        CashSession session = new CashSession();
        session.setId(1L);
        session.setUser(null);
        session.setCashRegister(null);

        // Act & Assert
        assertDoesNotThrow(() -> new CashSessionResponse(session));

        CashSessionResponse response = new CashSessionResponse(session);
        assertEquals("N/A", response.cashRegisterName());
        assertEquals("Unknown", response.username());
        assertNull(response.establishmentId());
    }

    @Test
    void constructor_ShouldHandleDeletedUser() {
        // Arrange
        com.sergiocodev.app.model.User deletedUser = new com.sergiocodev.app.model.User();
        deletedUser.setUsername("deleted_user_01");
        deletedUser.setDeletedAt(java.time.LocalDateTime.now());

        CashSession session = new CashSession();
        session.setId(2L);
        session.setUser(deletedUser);
        session.setCashRegister(null);

        // Act
        CashSessionResponse response = new CashSessionResponse(session);

        // Assert
        assertEquals("deleted_user_01", response.username());
    }
}
