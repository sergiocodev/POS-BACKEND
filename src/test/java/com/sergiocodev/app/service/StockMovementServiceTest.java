package com.sergiocodev.app.service;

import com.sergiocodev.app.event.StockMovementEvent;
import com.sergiocodev.app.model.*;
import com.sergiocodev.app.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.sergiocodev.app.service.impl.StockMovementServiceImpl;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockMovementServiceTest {

        @Mock
        private StockMovementRepository repository;

        @Mock
        private EstablishmentRepository establishmentRepository;

        @Mock
        private ProductLotRepository lotRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private ApplicationEventPublisher eventPublisher;

        @InjectMocks
        private StockMovementServiceImpl service;

        private Establishment establishment;
        private ProductLot lot;
        private User user;

        @BeforeEach
        void setUp() {
                establishment = new Establishment();
                establishment.setId(1L);
                establishment.setName("Main Store");

                Product product = new Product();
                product.setId(10L);
                product.setTradeName("Test Product");

                lot = new ProductLot();
                lot.setId(100L);
                lot.setLotCode("LOT-001");
                lot.setProduct(product);

                user = new User();
                user.setId(1L);
                user.setUsername("testuser");
        }

        // ==================== recordSaleMovement TESTS ====================

        @Test
        void recordSaleMovement_shouldCreateMovementWithCorrectType() {
                when(repository.save(any(StockMovement.class)))
                                .thenAnswer(inv -> inv.getArgument(0));

                StockMovement result = service.recordSaleMovement(
                                establishment, lot,
                                new BigDecimal("5"), new BigDecimal("95"),
                                1L, user);

                assertNotNull(result);
                assertEquals(StockMovement.MovementType.SALE, result.getType());
                assertEquals(new BigDecimal("-5"), result.getQuantity());
                assertEquals(new BigDecimal("95"), result.getBalanceAfter());
                assertEquals("sales", result.getReferenceTable());
                assertEquals(1L, result.getReferenceId());
                assertEquals(establishment, result.getEstablishment());
                assertEquals(lot, result.getLot());
                assertEquals(user, result.getUser());
                assertNotNull(result.getCreatedAt());
                verify(repository).save(any(StockMovement.class));
        }

        @Test
        void recordSaleMovement_shouldPublishEvent() {
                when(repository.save(any(StockMovement.class)))
                                .thenAnswer(inv -> inv.getArgument(0));

                service.recordSaleMovement(
                                establishment, lot,
                                new BigDecimal("5"), new BigDecimal("95"),
                                1L, user);

                ArgumentCaptor<StockMovementEvent> eventCaptor = ArgumentCaptor.forClass(StockMovementEvent.class);
                verify(eventPublisher).publishEvent(eventCaptor.capture());

                StockMovementEvent event = eventCaptor.getValue();
                assertEquals(1L, event.getEstablishmentId());
                assertEquals(10L, event.getProductId());
                assertEquals(100L, event.getLotId());
                assertEquals(StockMovementEvent.MovementType.SALE, event.getMovementType());
                assertEquals(new BigDecimal("-5"), event.getQuantity());
                assertEquals(new BigDecimal("95"), event.getBalanceAfter());
                assertEquals("Sale", event.getReason());
                assertEquals(1L, event.getReferenceId());
                assertEquals("sales", event.getReferenceTable());
                assertEquals(1L, event.getUserId());
        }

        // ==================== recordPurchaseMovement TESTS ====================

        @Test
        void recordPurchaseMovement_shouldCreateMovementWithPositiveQuantity() {
                when(repository.save(any(StockMovement.class)))
                                .thenAnswer(inv -> inv.getArgument(0));

                StockMovement result = service.recordPurchaseMovement(
                                establishment, lot,
                                new BigDecimal("50"), new BigDecimal("50"),
                                2L, user);

                assertNotNull(result);
                assertEquals(StockMovement.MovementType.PURCHASE, result.getType());
                assertEquals(new BigDecimal("50"), result.getQuantity());
                assertEquals(new BigDecimal("50"), result.getBalanceAfter());
                assertEquals("purchases", result.getReferenceTable());
                assertEquals(2L, result.getReferenceId());
                verify(repository).save(any(StockMovement.class));
        }

        @Test
        void recordPurchaseMovement_shouldPublishEvent() {
                when(repository.save(any(StockMovement.class)))
                                .thenAnswer(inv -> inv.getArgument(0));

                service.recordPurchaseMovement(
                                establishment, lot,
                                new BigDecimal("50"), new BigDecimal("50"),
                                2L, user);

                ArgumentCaptor<StockMovementEvent> eventCaptor = ArgumentCaptor.forClass(StockMovementEvent.class);
                verify(eventPublisher).publishEvent(eventCaptor.capture());

                StockMovementEvent event = eventCaptor.getValue();
                assertEquals(StockMovementEvent.MovementType.PURCHASE, event.getMovementType());
                assertEquals(new BigDecimal("50"), event.getQuantity());
                assertEquals("Purchase", event.getReason());
                assertEquals(2L, event.getReferenceId());
                assertEquals("purchases", event.getReferenceTable());
        }

        // ==================== recordAdjustmentMovement TESTS ====================

        @Test
        void recordAdjustmentMovement_shouldCreateMovementWithReason() {
                when(repository.save(any(StockMovement.class)))
                                .thenAnswer(inv -> inv.getArgument(0));

                StockMovement result = service.recordAdjustmentMovement(
                                establishment, lot,
                                new BigDecimal("-3"), new BigDecimal("47"),
                                "Counting error", user);

                assertNotNull(result);
                assertEquals(StockMovement.MovementType.ADJUSTMENT, result.getType());
                assertEquals(new BigDecimal("-3"), result.getQuantity());
                assertEquals(new BigDecimal("47"), result.getBalanceAfter());
                // reason field not on entity - validated via event
                assertEquals("adjustments", result.getReferenceTable());
                assertNull(result.getReferenceId());
                verify(repository).save(any(StockMovement.class));
        }

        @Test
        void recordAdjustmentMovement_shouldPublishEvent() {
                when(repository.save(any(StockMovement.class)))
                                .thenAnswer(inv -> inv.getArgument(0));

                service.recordAdjustmentMovement(
                                establishment, lot,
                                new BigDecimal("-3"), new BigDecimal("47"),
                                "Breakage", user);

                ArgumentCaptor<StockMovementEvent> eventCaptor = ArgumentCaptor.forClass(StockMovementEvent.class);
                verify(eventPublisher).publishEvent(eventCaptor.capture());

                StockMovementEvent event = eventCaptor.getValue();
                assertEquals(StockMovementEvent.MovementType.ADJUSTMENT, event.getMovementType());
                assertEquals("Breakage", event.getReason());
                assertNull(event.getReferenceId());
                assertEquals("adjustments", event.getReferenceTable());
        }

        // ==================== recordTransferMovement TESTS ====================

        @Test
        void recordTransferMovement_shouldCreateMovementWithTransferType() {
                when(repository.save(any(StockMovement.class)))
                                .thenAnswer(inv -> inv.getArgument(0));

                StockMovement result = service.recordTransferMovement(
                                establishment, lot,
                                new BigDecimal("-10"), new BigDecimal("40"),
                                3L, user, "transfers_out");

                assertNotNull(result);
                assertEquals(StockMovement.MovementType.TRANSFER, result.getType());
                assertEquals(new BigDecimal("-10"), result.getQuantity());
                assertEquals(new BigDecimal("40"), result.getBalanceAfter());
                assertEquals("transfers_out", result.getReferenceTable());
                assertEquals(3L, result.getReferenceId());
                verify(repository).save(any(StockMovement.class));
        }

        @Test
        void recordTransferMovement_shouldPublishEvent() {
                when(repository.save(any(StockMovement.class)))
                                .thenAnswer(inv -> inv.getArgument(0));

                service.recordTransferMovement(
                                establishment, lot,
                                new BigDecimal("10"), new BigDecimal("60"),
                                4L, user, "transfers_in");

                ArgumentCaptor<StockMovementEvent> eventCaptor = ArgumentCaptor.forClass(StockMovementEvent.class);
                verify(eventPublisher).publishEvent(eventCaptor.capture());

                StockMovementEvent event = eventCaptor.getValue();
                assertEquals(StockMovementEvent.MovementType.TRANSFER, event.getMovementType());
                assertEquals("Transfer", event.getReason());
                assertEquals(4L, event.getReferenceId());
                assertEquals("transfers_in", event.getReferenceTable());
        }

        // ==================== recordReversalMovement TESTS ====================

        @Test
        void recordReversalMovement_shouldCreateMovementWithReversalType() {
                when(repository.save(any(StockMovement.class)))
                                .thenAnswer(inv -> inv.getArgument(0));

                StockMovement result = service.recordReversalMovement(
                                establishment, lot,
                                new BigDecimal("2"), new BigDecimal("52"),
                                "Cancelled sale", 5L, user);

                assertNotNull(result);
                assertEquals(StockMovement.MovementType.REVERSAL, result.getType());
                assertEquals(new BigDecimal("2"), result.getQuantity());
                assertEquals(new BigDecimal("52"), result.getBalanceAfter());
                // reason field not on entity - validated via event
                assertEquals("reversals", result.getReferenceTable());
                assertEquals(5L, result.getReferenceId());
                verify(repository).save(any(StockMovement.class));
        }

        @Test
        void recordReversalMovement_shouldPublishEvent() {
                when(repository.save(any(StockMovement.class)))
                                .thenAnswer(inv -> inv.getArgument(0));

                service.recordReversalMovement(
                                establishment, lot,
                                new BigDecimal("2"), new BigDecimal("52"),
                                "Cancelled sale", 5L, user);

                ArgumentCaptor<StockMovementEvent> eventCaptor = ArgumentCaptor.forClass(StockMovementEvent.class);
                verify(eventPublisher).publishEvent(eventCaptor.capture());

                StockMovementEvent event = eventCaptor.getValue();
                assertEquals(StockMovementEvent.MovementType.REVERSAL, event.getMovementType());
                assertEquals("Cancelled sale", event.getReason());
                assertEquals(5L, event.getReferenceId());
                assertEquals("reversals", event.getReferenceTable());
        }
}
