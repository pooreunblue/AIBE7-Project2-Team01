package org.example.link.domain.request.service;

import lombok.RequiredArgsConstructor;
import org.example.link.auth.security.CustomUserDetails;
import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;
import org.example.link.domain.category.entity.CategoryEntity;
import org.example.link.domain.category.repository.CategoryRepository;
import org.example.link.domain.request.dto.RequestPostRequestDto;
import org.example.link.domain.request.dto.RequestPostResponseDto;
import org.example.link.domain.request.entity.RequestPostEntity;
import org.example.link.domain.request.repository.RequestPostRepository;
import org.example.link.domain.request.util.RequestPostStatus;
import org.example.link.domain.user.entity.UserEntity;
import org.example.link.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
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

    public List<RequestPostEntity> readAll() {
        return requestPostRepository.findAll();
    }

    public RequestPostEntity readOne(Long requestPostId) {
        return requestPostRepository.findById(requestPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Page<RequestPostEntity> search(
            String keyword,
            Pageable pageable
    ) {
        return requestPostRepository.search(keyword, pageable);
    }

    @Transactional
    public RequestPostEntity update(
            Long requestPostId,
            RequestPostRequestDto requestPostRequestDto,
            CustomUserDetails userDetails) throws AccessDeniedException {
        Long userId = userDetails.getUserId();
        RequestPostEntity requestPostEntity = requestPostRepository.findById(requestPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        if (!requestPostEntity.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("작성자만 수정할 수 있습니다.");
        }
        CategoryEntity category = categoryRepository.findById(requestPostRequestDto.categoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
        requestPostEntity.update(
                requestPostRequestDto.title(),
                requestPostRequestDto.content(),
                category,
                requestPostRequestDto.budgetMin(),
                requestPostRequestDto.budgetMax()
        );
        return requestPostEntity;
    }
}
