package org.example.link.common.storage.service;

import lombok.RequiredArgsConstructor;
import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;
import org.example.link.common.storage.config.SupabaseProperties;
import org.example.link.common.storage.dto.StoredFile;
import org.example.link.common.storage.type.FileType;
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
            String directory,
            FileType fileType
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
        switch (fileType) {
            case IMAGE ->
                    validateImageExtension(extension);
            case PORTFOLIO ->
                    validatePortfolioExtension(extension);
        }

        //uuid + content type
        String storedFileName =
                UUID.randomUUID() + extension;
        //supabase storage 업로드
        String path =
                directory + "/"
                        + storedFileName;

        String uploadUrl = buildUploadUrl(path);

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
        String publicUrl = buildPublicUrl(path);

        return new StoredFile(
                originalFileName,
                storedFileName,
                path,
                publicUrl,
                contentType,
                file.getSize()
        );
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
    private void validateImageExtension(
            String extension
    ) {

        String lower =
                extension.toLowerCase();
        if (!IMAGE_EXTENSIONS.contains(lower)) {
            throw new CustomException(
                    ErrorCode.UNSUPPORTED_FILE_TYPE
            );
        }
    }

    private void validatePortfolioExtension(
            String extension
    ) {

        String lower =
                extension.toLowerCase();

        if (!PORTFOLIO_EXTENSIONS.contains(lower)) {
            throw new CustomException(
                    ErrorCode.UNSUPPORTED_FILE_TYPE
            );
        }
    }

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            ".jpg",
            ".jpeg",
            ".png",
            ".gif",
            ".webp"
    );

    private static final Set<String> PORTFOLIO_EXTENSIONS =
            Set.of(
                    ".pdf",

                    ".jpg",
                    ".jpeg",
                    ".png",

                    ".zip",

                    ".ppt",
                    ".pptx",

                    ".doc",
                    ".docx",

                    ".xls",
                    ".xlsx"
            );

    private String buildUploadUrl(String path) {
        return properties.url()
                + "/storage/v1/object/"
                + properties.bucket()
                + "/"
                + path;
    }

    private String buildPublicUrl(String path) {
        return properties.url()
                + "/storage/v1/object/public/"
                + properties.bucket()
                + "/"
                + path;
    }

    @Override
    public void delete(String storedPath) {

        String deleteUrl =
                properties.url()
                        + "/storage/v1/object/"
                        + properties.bucket()
                        + "/"
                        + storedPath;

        try {
            restClient.delete()
                    .uri(deleteUrl)
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + properties.serviceKey()
                    )
                    .header(
                            "apikey",
                            properties.serviceKey()
                    )
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientException e) {
            throw new CustomException(
                    ErrorCode.FILE_DELETE_FAILED
            );
        }
    }
}
