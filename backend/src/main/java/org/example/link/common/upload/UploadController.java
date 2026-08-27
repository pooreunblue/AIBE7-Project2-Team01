package org.example.link.common.upload;

import lombok.RequiredArgsConstructor;
import org.example.link.auth.security.CustomUserDetails;
import org.example.link.common.response.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/uploads")
public class UploadController {

    private final UploadService uploadService;

    @PostMapping(
            value="/temp",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<UploadResponse>> uploadTempImage(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestPart MultipartFile file
    ){

        return ResponseEntity.ok(
                ApiResponse.ok(
                        uploadService.uploadTempImage(
                                user.getUserId(),
                                file
                        )
                )
        );
    }
}
