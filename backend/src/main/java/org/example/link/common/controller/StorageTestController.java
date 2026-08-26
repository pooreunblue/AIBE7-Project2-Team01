package org.example.link.common.controller;

import lombok.RequiredArgsConstructor;
import org.example.link.common.storage.dto.StoredFile;
import org.example.link.common.storage.service.StorageService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class StorageTestController {

    private final StorageService storageService;

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public StoredFile upload(
            @RequestPart("file") MultipartFile file
    ) {
        return storageService.upload(file, "profiles");
    }
}
