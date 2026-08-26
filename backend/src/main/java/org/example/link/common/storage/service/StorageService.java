package org.example.link.common.storage.service;

import org.example.link.common.storage.dto.StoredFile;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    StoredFile upload(
            MultipartFile file,
            String directory
    );
    void delete(String storedPath);
}
