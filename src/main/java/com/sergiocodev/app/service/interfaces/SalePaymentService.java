package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.model.Sale;
import com.sergiocodev.app.model.CashSession;
import com.sergiocodev.app.dto.sale.SaleRequest;

public interface SalePaymentService {
    void processPayments(SaleRequest request, Sale entity, CashSession session);
    void createAccountReceivableIfCredit(Sale savedSale, SaleRequest request);
    void processRefund(Sale original, Sale note, Long userId, CashSession session);
    void processVoidRefund(Sale sale, Long userId, CashSession session);
}
