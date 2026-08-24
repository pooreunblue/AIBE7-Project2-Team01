package org.example.link.domain.category.service;

import lombok.RequiredArgsConstructor;
import org.example.link.domain.category.repository.CategoryRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
}
