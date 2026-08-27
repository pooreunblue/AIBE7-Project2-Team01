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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        Long userId = getUserId(userDetails);
        UserEntity user = getUser(userId);
        CategoryEntity category = getCategory(requestPostRequestDto);
        RequestPostEntity requestPostEntity = createRequestPost(user, category, requestPostRequestDto);
        return requestPostRepository.save(requestPostEntity);
    }

    public List<RequestPostEntity> readAll() {
        return requestPostRepository.findAll();
    }

    public RequestPostEntity readOne(Long requestPostId) {
        return getRequestPost(requestPostId);
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
        Long userId = getUserId(userDetails);
        RequestPostEntity requestPostEntity = getRequestPost(requestPostId);
        validateAuth(requestPostEntity, userId);
        CategoryEntity category = getCategory(requestPostRequestDto);
        updateRequestPost(requestPostRequestDto, requestPostEntity, category);
        return requestPostEntity;
    }

    @Transactional
    public void delete(Long requestPostId, CustomUserDetails userDetails) throws AccessDeniedException {
        Long userId = getUserId(userDetails);
        RequestPostEntity requestPostEntity = getRequestPost(requestPostId);
        validateAuth(requestPostEntity, userId);
        requestPostRepository.delete(requestPostEntity);
    }

    @Transactional
    public RequestPostEntity closeStatus(Long requestPostId, CustomUserDetails userDetails) throws AccessDeniedException {
        Long userId = getUserId(userDetails);
        RequestPostEntity requestPostEntity = getRequestPost(requestPostId);
        validateAuth(requestPostEntity, userId);
        requestPostEntity.closeStatus();
        return requestPostEntity;
    }

    private Long getUserId(CustomUserDetails userDetails) {
        return userDetails.getUserId();
    }

    private UserEntity getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private CategoryEntity getCategory(RequestPostRequestDto requestPostRequestDto) {
        return categoryRepository.findById(requestPostRequestDto.categoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private RequestPostEntity createRequestPost(UserEntity user, CategoryEntity category, RequestPostRequestDto requestPostRequestDto) {
        return RequestPostEntity.builder()
                .user(user)
                .category(category)
                .title(requestPostRequestDto.title())
                .content(requestPostRequestDto.content())
                .budgetMin(requestPostRequestDto.budgetMin())
                .budgetMax(requestPostRequestDto.budgetMax())
                .dueDate(requestPostRequestDto.dueDate())
                .status(RequestPostStatus.OPEN)
                .build();
    }

    private RequestPostEntity getRequestPost(Long requestPostId) {
        return requestPostRepository.findById(requestPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }
    
    private void validateAuth(RequestPostEntity requestPostEntity, Long userId) throws AccessDeniedException {
        if (!requestPostEntity.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.POST_ACCESS_DENIED);
        }
    }

    private void updateRequestPost(RequestPostRequestDto requestPostRequestDto, RequestPostEntity requestPostEntity, CategoryEntity category) {
        requestPostEntity.update(
                requestPostRequestDto.title(),
                requestPostRequestDto.content(),
                category,
                requestPostRequestDto.budgetMin(),
                requestPostRequestDto.budgetMax(),
                requestPostRequestDto.dueDate()
        );
    }
}
