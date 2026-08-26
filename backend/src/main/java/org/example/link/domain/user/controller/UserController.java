package org.example.link.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.link.auth.security.CustomUserDetails;
import org.example.link.common.response.ApiResponse;
import org.example.link.domain.user.dto.MyPageResponse;
import org.example.link.domain.user.dto.SignupRequest;
import org.example.link.domain.user.dto.SignupResponse;
import org.example.link.domain.user.dto.UpdateUserRequest;
import org.example.link.domain.user.service.MyPageService;
import org.example.link.domain.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final MyPageService myPageService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입")
    public ResponseEntity<ApiResponse<SignupResponse>> signUp(
           @Valid @RequestBody SignupRequest request
    ) {
        SignupResponse response = userService.signUp(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    @PatchMapping(
            value = "/me/profile-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<MyPageResponse> updateProfileImage(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.ok(
                userService.updateProfileImage(
                        user.getUserId(),
                        file
                )
        );
    }

    @GetMapping("/me")
    @Operation(summary = "마이페이지 조회")
    public ApiResponse<MyPageResponse> getMyPage(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ApiResponse.ok(
                myPageService.getMyPage(
                        user.getUserId()
                )
        );
    }

    @PatchMapping("/me")
    @Operation(summary = "내 정보 수정")
    public ApiResponse<MyPageResponse> updateUser(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody UpdateUserRequest request
    ){return ApiResponse.ok(
            myPageService.updateUser(
                    user.getUserId(),
                    request
            )
    );
    }

    @DeleteMapping("/me")
    @Operation(summary = "회원 탈퇴")
    public ApiResponse<Void> deleteUser(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        myPageService.deleteUser(user.getUserId());
        return ApiResponse.ok();
    }
}
