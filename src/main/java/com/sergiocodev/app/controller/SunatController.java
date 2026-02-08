package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.dto.sunat.EmitInvoiceRequest;
import com.sergiocodev.app.dto.sunat.EmitInvoiceResponse;
import com.sergiocodev.app.dto.sunat.VoidInvoiceRequest;
import com.sergiocodev.app.dto.voideddocument.VoidedDocumentResponse;
import com.sergiocodev.app.service.SaleService;
import com.sergiocodev.app.service.VoidedDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sunat")
@RequiredArgsConstructor
@Tag(name = "SUNAT Electronic Invoicing", description = "Endpoints for SUNAT integration")
@SecurityRequirement(name = "bearerAuth")
public class SunatController {

    private final SaleService saleService;
    private final VoidedDocumentService voidedDocumentService;

    @PostMapping("/EmitInvoiceToOSE")
    @Operation(summary = "Emit Invoice to OSE/SUNAT")
    public ResponseEntity<ResponseApi<EmitInvoiceResponse>> emitInvoiceToOSE(
            @Valid @RequestBody EmitInvoiceRequest request) {
        EmitInvoiceResponse response = saleService.emitInvoiceToOSE(request.getSaleId());
        return ResponseEntity.ok(ResponseApi.success(response, "Document sent to SUNAT"));
    }

    @PostMapping("/VoidInvoice")
    @Operation(summary = "Void Invoice and Reverse Stock")
    public ResponseEntity<ResponseApi<VoidedDocumentResponse>> voidInvoice(
            @Valid @RequestBody VoidInvoiceRequest request,
            @RequestParam Long userId) {
        VoidedDocumentResponse response = voidedDocumentService.voidInvoice(request, userId);
        return ResponseEntity.ok(ResponseApi.success(response, "Invoice voided and stock reversed"));
    }
}
