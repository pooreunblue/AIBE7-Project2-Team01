package org.example.link.domain.request.controller;

import lombok.RequiredArgsConstructor;
import org.example.link.auth.security.CustomUserDetails;
import org.example.link.domain.request.dto.RequestPostRequestDto;
import org.example.link.domain.request.dto.RequestPostResponseDto;
import org.example.link.domain.request.entity.RequestPostEntity;
import org.example.link.domain.request.service.RequestPostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/requests")
public class RequestPostController {
    private final RequestPostService requestPostService;

    @PostMapping
    public ResponseEntity<RequestPostResponseDto> create(
            @Validated @RequestBody RequestPostRequestDto requestPostRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {
        RequestPostEntity requestPostEntity = requestPostService.create(requestPostRequestDto, userDetails);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(RequestPostResponseDto.toDto(requestPostEntity));
    }

    @GetMapping
    public ResponseEntity<List<RequestPostResponseDto>> readAll(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<RequestPostEntity> requestPostEntities = requestPostService.readAll(userDetails);
        return ResponseEntity.ok(requestPostEntities.stream()
                .map(RequestPostResponseDto::toDto)
                .toList());
    }
}
