package org.example.link.domain.request.service;

import lombok.RequiredArgsConstructor;
import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;
import org.example.link.domain.category.entity.CategoryEntity;
import org.example.link.domain.category.repository.CategoryRepository;
import org.example.link.domain.request.dto.RequestPostRequestDto;
import org.example.link.domain.request.entity.RequestPostEntity;
import org.example.link.domain.request.repository.RequestPostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RequestPostService {
    private final RequestPostRepository requestPostRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public RequestPostEntity create(RequestPostRequestDto requestPostRequestDto) {
        CategoryEntity category = categoryRepository.findById(requestPostRequestDto.categoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
        RequestPostEntity requestPostEntity = RequestPostEntity.builder()
                .category(category)
                .title(requestPostRequestDto.title())
                .content(requestPostRequestDto.content())
                .budgetMin(requestPostRequestDto.budgetMin())
                .budgetMax(requestPostRequestDto.budgetMax())
                .build();
        return requestPostRepository.save(requestPostEntity);
    }
}
