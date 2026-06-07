package com.sergiocodev.app.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.sergiocodev.app.service.interfaces.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Implementación de FileStorageService usando Cloudinary.
 * Los archivos se suben a la nube y persisten entre redeploys en Railway.
 * La URL retornada es una URL pública permanente de Cloudinary (https://res.cloudinary.com/...).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private final Cloudinary cloudinary;

    @Override
    @SuppressWarnings("unchecked")
    public String store(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("No se puede almacenar un archivo vacío.");
        }

        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "pos/" + folder,
                            "resource_type", "image",
                            "overwrite", true
                    )
            );

            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("Archivo subido a Cloudinary: {}", secureUrl);
            return secureUrl;

        } catch (IOException e) {
            log.error("Error al subir archivo a Cloudinary", e);
            throw new RuntimeException("Error al subir el archivo a Cloudinary: " + e.getMessage(), e);
        }
    }
}
