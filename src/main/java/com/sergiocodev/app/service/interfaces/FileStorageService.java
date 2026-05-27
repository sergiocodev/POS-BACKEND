package com.sergiocodev.app.service.interfaces;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String store(MultipartFile file, String folder);
}
