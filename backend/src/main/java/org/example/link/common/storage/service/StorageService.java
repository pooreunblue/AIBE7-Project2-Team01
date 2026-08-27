package org.example.link.common.storage.service;

import org.example.link.common.storage.dto.StoredFile;
import org.example.link.common.storage.type.FileType;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    StoredFile upload(
            MultipartFile file,
            String directory,
            FileType fileType
    );
    void delete(String storedPath);
}
