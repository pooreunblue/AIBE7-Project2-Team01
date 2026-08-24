package org.example.link.auth.controller;

import lombok.RequiredArgsConstructor;
import org.example.link.auth.dto.LoginRequest;
import org.example.link.auth.dto.LoginResponse;
import org.example.link.auth.dto.RefreshRequest;
import org.example.link.auth.dto.RefreshResponse;
import org.example.link.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(
            @RequestBody RefreshRequest request
    ){
        return ResponseEntity.ok(
                authService.refresh(request)
        );
    }
}