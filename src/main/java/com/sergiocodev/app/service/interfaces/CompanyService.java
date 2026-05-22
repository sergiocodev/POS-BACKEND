package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.dto.company.CompanyRequest;
import com.sergiocodev.app.dto.company.CompanyResponse;

public interface CompanyService {
    CompanyResponse getCompany();
    CompanyResponse updateCompany(CompanyRequest request);
    boolean isConfigured();
}
