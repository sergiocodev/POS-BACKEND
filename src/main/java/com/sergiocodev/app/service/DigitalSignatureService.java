package com.sergiocodev.app.service;

import org.springframework.stereotype.Service;

@Service
public class DigitalSignatureService {

    public String signXml(String xmlContent) {
        // Placeholder for actual XML digital signature logic
        // In a real implementation, this would load the keystore/certificate
        // and sign the UBL Extensions content

        // For now, we return the XML as is or append a dummy signature for testing
        // Note: Real OSE envs will reject unsigned XMLs
        return xmlContent;
    }
}
