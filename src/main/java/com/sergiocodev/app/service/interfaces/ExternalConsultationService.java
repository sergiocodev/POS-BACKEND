package com.sergiocodev.app.service.interfaces;

import com.sergiocodev.app.dto.external.ExternalConsultationResponse;

public interface ExternalConsultationService {

    ExternalConsultationResponse searchByDocument(String documentNumber);
}
