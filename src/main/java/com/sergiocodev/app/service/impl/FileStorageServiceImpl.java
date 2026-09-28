package com.sergiocodev.app.service.impl;

import com.sergiocodev.app.service.interfaces.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Implementación de FileStorageService usando el File System local.
 * Los archivos se guardan en el servidor y se sirven como recursos estáticos.
 */
@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path rootLocation;

    public FileStorageServiceImpl(@Value("${file.upload-dir:uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir);
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar la carpeta de subida de archivos", e);
        }
    }

    @Override
    public String store(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("No se puede almacenar un archivo vacío.");
        }

        try {
            Path folderPath = this.rootLocation.resolve(folder);
            Files.createDirectories(folderPath);

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            Path destinationFile = folderPath.resolve(Paths.get(filename))
                    .normalize().toAbsolutePath();

            if (!destinationFile.getParent().equals(folderPath.toAbsolutePath())) {
                throw new RuntimeException("No se puede almacenar el archivo fuera del directorio actual.");
            }

            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

            // Generar la URL relativa para acceder al archivo
            String fileUrl = "/" + this.rootLocation.getFileName().toString() + "/" + folder + "/" + filename;
            
            log.info("Archivo subido localmente: {}", fileUrl);
            return fileUrl;
        } catch (IOException e) {
            log.error("Error al guardar archivo localmente", e);
            throw new RuntimeException("Fallo al almacenar el archivo.", e);
        }
    }
}
