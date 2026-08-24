package org.example.link.domain.request.controller;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.example.link.domain.request.dto.RequestPostRequestDto;
import org.example.link.domain.request.dto.RequestPostResponseDto;
import org.example.link.domain.request.entity.RequestPostEntity;
import org.example.link.domain.request.service.RequestPostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/requests")
public class RequestPostController {
    private final RequestPostService requestPostService;

    @PostMapping
    public ResponseEntity<RequestPostResponseDto> create(
            @Validated @RequestBody RequestPostRequestDto requestPostRequestDto
            ) {
        RequestPostEntity requestPostEntity = requestPostService.create(requestPostRequestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(RequestPostResponseDto.toDto(requestPostEntity));
    }
}
