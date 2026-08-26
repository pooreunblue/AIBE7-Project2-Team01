package org.example.link.common.storage.service;

import lombok.RequiredArgsConstructor;
import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;
import org.example.link.common.storage.config.SupabaseProperties;
import org.example.link.common.storage.dto.StoredFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupabaseStorageService
        implements StorageService {
    private final RestClient restClient;
    private final SupabaseProperties properties;

    @Override
    public StoredFile upload(
            MultipartFile file,
            String directory
    ) {
        String originalFileName =
                file.getOriginalFilename();
        if (originalFileName == null
                || originalFileName.isBlank()) {
            throw new CustomException(
                    ErrorCode.INVALID_FILE
            );
        }
        String extension =
                getExtension(originalFileName);
        validateExtension(extension);

        //uuid + content type
        String storedFileName =
                UUID.randomUUID() + extension;
        //supabase storage 업로드
        String path =
                directory + "/"
                        + storedFileName;
        String uploadUrl =
                properties.url()
                        + "/storage/v1/object/"
                        + properties.bucket()
                        + "/"
                        + path;

        //content type 준비
        String contentType =
                file.getContentType();
        if (contentType == null) {
            contentType =
                    MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        //실제 upload
        try {
            restClient.post()
                    .uri(uploadUrl)
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + properties.serviceKey()
                    )
                    .header(
                            "apikey",
                            properties.serviceKey()
                    )
                    .contentType(
                            MediaType.parseMediaType(contentType)
                    )
                    .body(file.getBytes())
                    .retrieve()
                    .toBodilessEntity();
        }catch (IOException | RestClientException e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
        return null;
    }

    //파일 확장자 추출
    private String getExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index == -1 || index == fileName.length() - 1) {
            throw new CustomException(ErrorCode.INVALID_FILE);
        }
        return fileName.substring(index);
    }

    //content type 검증
    private void validateExtension(String extension) {

        String lowerExtension = extension.toLowerCase();

        if (!ALLOWED_IMAGE_EXTENSIONS.contains(lowerExtension)) {
            throw new CustomException(
                    ErrorCode.UNSUPPORTED_FILE_TYPE
            );
        }
    }

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
            ".jpg",
            ".jpeg",
            ".png",
            ".gif",
            ".webp"
    );

    @Override
    public void delete(String storedPath) {
    }
}
