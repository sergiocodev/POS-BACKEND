package com.sergiocodev.app.service.impl;

import com.sergiocodev.app.service.interfaces.SalePaymentService;
import com.sergiocodev.app.service.interfaces.CashMovementService;
import com.sergiocodev.app.service.interfaces.CashConceptService;
import com.sergiocodev.app.repository.AccountReceivableRepository;
import com.sergiocodev.app.repository.CashConceptRepository;
import com.sergiocodev.app.mapper.SaleMapper;
import com.sergiocodev.app.model.Sale;
import com.sergiocodev.app.model.CashSession;
import com.sergiocodev.app.model.SalePayment;
import com.sergiocodev.app.model.CashConcept;
import com.sergiocodev.app.model.AccountReceivable;
import com.sergiocodev.app.dto.sale.SaleRequest;
import com.sergiocodev.app.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalePaymentServiceImpl implements SalePaymentService {

    private final CashMovementService cashMovementService;
    private final CashConceptService cashConceptService;
    private final CashConceptRepository cashConceptRepository;
    private final AccountReceivableRepository accountReceivableRepository;
    private final SaleMapper mapper;

    @Override
    public void processPayments(SaleRequest request, Sale entity, CashSession session) {
        for (var pr : request.payments()) {
            SalePayment payment = mapper.toPaymentEntity(pr);
            payment.setSale(entity);
            payment.setCashSession(session);
            entity.getPayments().add(payment);

            if (session != null) {
                CashConcept concept = cashConceptService
                        .findOrCreateSaleConcept(pr.paymentMethod().name());

                String description = "Venta (" + pr.paymentMethod().name() + "): " + entity.getSeries()
                        + "-" + entity.getNumber();
                cashMovementService.registerInternalMovement(session, entity.getUser(), concept,
                        pr.amount(),
                        entity.getSeries() + "-" + entity.getNumber(), description);
            }
        }
    }

    @Override
    public void createAccountReceivableIfCredit(Sale savedSale, SaleRequest request) {
        if (savedSale.getPaymentCondition() == Sale.PaymentCondition.CREDITO) {
            BigDecimal totalSale = savedSale.getTotal();
            BigDecimal amountPaid = savedSale.getPayments().stream()
                    .map(SalePayment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal pendingBalance = totalSale.subtract(amountPaid);

            if (pendingBalance.compareTo(BigDecimal.ZERO) > 0) {
                AccountReceivable receivable = new AccountReceivable();
                receivable.setSale(savedSale);
                receivable.setCustomer(savedSale.getCustomer());
                receivable.setTotalAmount(totalSale);
                receivable.setAmountPaid(amountPaid);
                receivable.setPendingBalance(pendingBalance);
                receivable.setStatus(AccountReceivable.ReceivableStatus.PENDING);
                receivable.setDueDate(
                        request.dueDate() != null ? request.dueDate()
                                : java.time.LocalDate.now().plusDays(30));
                accountReceivableRepository.save(receivable);
                log.info("Account receivable created for sale {}: pendingBalance={}", savedSale.getId(),
                        pendingBalance);
            }
        }
    }

    @Override
    public void processRefund(Sale original, Sale note, Long userId, CashSession session) {
        if (session != null) {
            BigDecimal refundAmount = original.getPayments().stream()
                    .map(SalePayment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                CashConcept concept = cashConceptRepository.findByType(CashConcept.ConceptType.OUT)
                        .stream()
                        .filter(c -> c.getName().toLowerCase().contains("devolucion")
                                || c.getName().toLowerCase().contains("nota")
                                || c.getName().toLowerCase().contains("egreso"))
                        .findFirst()
                        .orElseGet(() -> cashConceptRepository
                                .findByType(CashConcept.ConceptType.OUT).stream()
                                .findFirst()
                                .orElseThrow(() -> new ResourceNotFoundException(
                                        "No se encontró un concepto de caja para egresos por devoluciones")));

                String description = "Nota de Crédito: " + note.getSeries() + "-" + note.getNumber()
                        + " (Ref: "
                        + original.getSeries() + "-" + original.getNumber() + ")";
                cashMovementService.registerInternalMovement(session, note.getUser(), concept,
                        refundAmount,
                        note.getSeries() + "-" + note.getNumber(), description);
            }
        }
    }

    @Override
    public void processVoidRefund(Sale sale, Long userId, CashSession session) {
        if (session != null) {
            BigDecimal refundAmount = sale.getPayments().stream()
                    .map(SalePayment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                CashConcept concept = cashConceptRepository.findByType(CashConcept.ConceptType.OUT)
                        .stream()
                        .filter(c -> c.getName().toLowerCase().contains("anulacion")
                                || c.getName().toLowerCase().contains("egreso"))
                        .findFirst()
                        .orElseGet(() -> cashConceptRepository
                                .findByType(CashConcept.ConceptType.OUT).stream()
                                .findFirst()
                                .orElseThrow(() -> new ResourceNotFoundException(
                                        "No se encontró un concepto de caja para egresos por anulaciones")));

                String description = "Anulación de Venta: " + sale.getSeries() + "-" + sale.getNumber();
                cashMovementService.registerInternalMovement(session,
                        sale.getUser(), concept,
                        refundAmount,
                        sale.getSeries() + "-" + sale.getNumber(), description);
            }
        }
    }

    @Override
    public void cancelAccountReceivableIfExists(Long saleId) {
        accountReceivableRepository.findBySaleId(saleId).ifPresent(receivable -> {
            receivable.setStatus(AccountReceivable.ReceivableStatus.CANCELED);
            accountReceivableRepository.save(receivable);
            log.info("Account receivable canceled for sale {}", saleId);
        });
    }
}
