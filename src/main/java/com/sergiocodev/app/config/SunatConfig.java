package com.sergiocodev.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "sunat")
@Data
public class SunatConfig {
    private String oseUrl;
    private String oseUsername;
    private String osePassword;
    private String certificatePath;
    private String certificatePassword;
    private String ruc;
    private String razonSocial;
}
