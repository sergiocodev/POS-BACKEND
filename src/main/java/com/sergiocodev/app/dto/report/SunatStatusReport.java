package com.sergiocodev.app.dto.report;

import com.sergiocodev.app.model.Sale;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SunatStatusReport {
    private Sale.SunatStatus status;
    private Long count;
}
