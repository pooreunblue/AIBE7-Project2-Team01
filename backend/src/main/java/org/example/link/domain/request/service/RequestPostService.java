package org.example.link.domain.request.service;

import lombok.RequiredArgsConstructor;
import org.example.link.auth.security.CustomUserDetails;
import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;
import org.example.link.domain.category.entity.CategoryEntity;
import org.example.link.domain.category.repository.CategoryRepository;
import org.example.link.domain.request.dto.RequestPostRequestDto;
import org.example.link.domain.request.entity.RequestPostEntity;
import org.example.link.domain.request.repository.RequestPostRepository;
import org.example.link.domain.request.util.RequestPostStatus;
import org.example.link.domain.user.entity.UserEntity;
import org.example.link.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RequestPostService {
    private final RequestPostRepository requestPostRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public RequestPostEntity create(RequestPostRequestDto requestPostRequestDto, CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        CategoryEntity category = categoryRepository.findById(requestPostRequestDto.categoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
        RequestPostEntity requestPostEntity = RequestPostEntity.builder()
                .user(user)
                .category(category)
                .title(requestPostRequestDto.title())
                .content(requestPostRequestDto.content())
                .budgetMin(requestPostRequestDto.budgetMin())
                .budgetMax(requestPostRequestDto.budgetMax())
                .status(RequestPostStatus.OPEN)
                .build();
        return requestPostRepository.save(requestPostEntity);
    }

    public List<RequestPostEntity> readAll(CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        return requestPostRepository.findAllById(userId);
    }
}
