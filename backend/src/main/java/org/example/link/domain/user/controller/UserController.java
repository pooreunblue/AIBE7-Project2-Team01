package org.example.link.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.link.domain.user.dto.LoginRequest;
import org.example.link.domain.user.dto.LoginResponse;
import org.example.link.domain.user.dto.SignupRequest;
import org.example.link.domain.user.dto.SignupResponse;
import org.example.link.domain.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signUp(
            @RequestBody SignupRequest request
    ) {
        SignupResponse response = userService.signUp(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(
                userService.login(request)
        );
    }
}