package com.sergiocodev.app.util;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public class UrlHelper {
    
    /**
     * Convierte una URL relativa (ej. /uploads/...) a una URL absoluta 
     * utilizando el contexto de la petición actual.
     * Si la URL ya es absoluta (empieza con http), la devuelve tal cual.
     */
    public static String toAbsoluteUrl(String path) {
        if (!StringUtils.hasText(path)) {
            return path;
        }
        if (path.startsWith("http")) {
            return path;
        }
        
        try {
            String sanitizedPath = path.startsWith("/") ? path : "/" + path;
            return ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path(sanitizedPath)
                    .toUriString();
        } catch (Exception e) {
            // Fallback si se llama fuera de un contexto de Request HTTP
            return path;
        }
    }
}
