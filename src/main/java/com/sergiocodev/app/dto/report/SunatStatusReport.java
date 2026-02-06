package com.sergiocodev.app.dto.report;

import com.sergiocodev.app.model.Sale;

public record SunatStatusReport(
        Sale.SunatStatus status,
        Long count) {
}
