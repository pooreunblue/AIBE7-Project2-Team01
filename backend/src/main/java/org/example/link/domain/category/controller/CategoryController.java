package org.example.link.domain.category.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.example.link.domain.category.dto.CategoryResponseDto;
import org.example.link.domain.category.entity.CategoryEntity;
import org.example.link.domain.category.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "카테고리 목록")
    public ResponseEntity<List<CategoryResponseDto>> findAll() {
        List<CategoryEntity> categories = categoryService.findAll();
        return ResponseEntity
                .ok(categories.stream()
                        .map(CategoryResponseDto::toDto)
                        .toList());
    }
}
