package com.sergiocodev.app.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootLocation = Paths.get("uploads");

    public FileStorageService() {
        try {
            Files.createDirectories(rootLocation);
            Files.createDirectories(rootLocation.resolve("usuarios"));
            Files.createDirectories(rootLocation.resolve("productos"));
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    public String store(MultipartFile file, String folder) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file.");
            }

            String extension = getFileExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID().toString() + extension;

            Path targetFolder = this.rootLocation.resolve(folder);
            if (!Files.exists(targetFolder)) {
                Files.createDirectories(targetFolder);
            }

            Path destinationFile = targetFolder.resolve(Paths.get(filename))
                    .normalize().toAbsolutePath();

            if (!destinationFile.getParent().equals(targetFolder.toAbsolutePath())) {

                throw new RuntimeException("Cannot store file outside current directory.");
            }

            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + folder + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null)
            return "";
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex);
    }
}
