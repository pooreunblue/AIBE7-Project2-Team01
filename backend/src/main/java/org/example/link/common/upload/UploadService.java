package org.example.link.common.upload;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.example.link.common.storage.dto.StoredFile;
import org.example.link.common.storage.service.StorageService;
import org.example.link.common.storage.type.FileType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UploadService {

    private final StorageService storageService;

    public UploadResponse uploadTempImage(
            UUID userId,
            MultipartFile file
    ){

        StoredFile storedFile =
                storageService.upload(
                        file,
                        "temp/" + userId,
                        FileType.IMAGE
                );

        return new UploadResponse(
                storedFile.url(),
                storedFile.path()
        );
    }

}
