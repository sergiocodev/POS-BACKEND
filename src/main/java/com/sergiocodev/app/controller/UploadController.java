package com.sergiocodev.app.controller;

import com.sergiocodev.app.dto.ResponseApi;
import com.sergiocodev.app.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/uploads")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Uploads", description = "Endpoints para la subida de archivos")
public class UploadController {

    private final FileStorageService fileStorageService;

    @Operation(summary = "Subir un archivo", description = "Sube una imagen y devuelve la ruta pública")
    @PostMapping
    public ResponseEntity<ResponseApi<Map<String, String>>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folder") String folder) {

        String filePath = fileStorageService.store(file, folder);
        return ResponseEntity.ok(ResponseApi.success(Map.of("url", filePath), "Archivo subido exitosamente"));
    }
}
