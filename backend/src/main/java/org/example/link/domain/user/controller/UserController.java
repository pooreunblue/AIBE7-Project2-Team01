package org.example.link.domain.user.controller;

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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final MyPageService myPageService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signUp(
           @Valid @RequestBody SignupRequest request
    ) {
        SignupResponse response = userService.signUp(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    @GetMapping("/me")
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
    public ApiResponse<Void> deleteUser(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        myPageService.deleteUser(user.getUserId());
        return ApiResponse.ok();
    }
}
