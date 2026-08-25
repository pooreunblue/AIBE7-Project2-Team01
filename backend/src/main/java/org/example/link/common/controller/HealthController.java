package org.example.link.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "서버 상태 확인")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Server is running");
    }
}
