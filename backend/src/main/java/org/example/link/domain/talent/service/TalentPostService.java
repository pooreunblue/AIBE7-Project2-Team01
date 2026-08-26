package org.example.link.domain.talent.service;

import lombok.RequiredArgsConstructor;
import org.example.link.auth.security.CustomUserDetails;
import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;
import org.example.link.domain.category.entity.CategoryEntity;
import org.example.link.domain.category.repository.CategoryRepository;
import org.example.link.domain.portfolio.entity.PortfolioEntity;
import org.example.link.domain.portfolio.repository.PortfolioRepository;
import org.example.link.domain.request.entity.RequestPostEntity;
import org.example.link.domain.talent.dto.TalentPostRequestDto;
import org.example.link.domain.talent.entity.TalentPostEntity;
import org.example.link.domain.talent.repository.TalentPostRepository;
import org.example.link.domain.talent.util.TalentPostStatus;
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
public class TalentPostService {
    private final TalentPostRepository talentPostRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;

    @Transactional
    public TalentPostEntity create(TalentPostRequestDto talentPostRequestDto, CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        CategoryEntity category = categoryRepository.findById(talentPostRequestDto.categoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
        PortfolioEntity portfolio = portfolioRepository.findById(talentPostRequestDto.portfolioId())
                .orElseThrow(() -> new CustomException(ErrorCode.PORTFOLIO_NOT_FOUND));
        TalentPostEntity talentPostEntity = TalentPostEntity.builder()
                .user(user)
                .category(category)
                .title(talentPostRequestDto.title())
                .content(talentPostRequestDto.content())
                .price(talentPostRequestDto.price())
                .estimatedDuration(talentPostRequestDto.estimatedDuration())
                .durationUnit(talentPostRequestDto.durationUnit())
                .portfolio(portfolio)
                .status(TalentPostStatus.ACTIVE)
                .build();
        return talentPostRepository.save(talentPostEntity);
    }

    public List<TalentPostEntity> readAll() {
        return talentPostRepository.findAll();
    }

    public TalentPostEntity readOne(Long talentPostId) {
        return talentPostRepository.findById(talentPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Page<TalentPostEntity> search(
            String keyword,
            Pageable pageable
    ) {
        return talentPostRepository.search(keyword, pageable);
    }

    @Transactional
    public TalentPostEntity update(
            Long talentPostId,
            TalentPostRequestDto talentPostRequestDto,
            CustomUserDetails userDetails) throws AccessDeniedException {
        Long userId = userDetails.getUserId();
        TalentPostEntity talentPostEntity = talentPostRepository.findById(talentPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        if (!talentPostEntity.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("작성자만 수정할 수 있습니다.");
        }
        CategoryEntity category = categoryRepository.findById(talentPostRequestDto.categoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
        PortfolioEntity portfolio = portfolioRepository.findById(talentPostRequestDto.portfolioId())
                        .orElseThrow(() -> new CustomException(ErrorCode.PORTFOLIO_NOT_FOUND));
        talentPostEntity.update(
                talentPostRequestDto.title(),
                talentPostRequestDto.content(),
                category,
                talentPostRequestDto.price(),
                talentPostRequestDto.estimatedDuration(),
                talentPostRequestDto.durationUnit(),
                portfolio
        );
        return talentPostEntity;
    }

    @Transactional
    public void delete(Long talentPostId, CustomUserDetails userDetails) throws AccessDeniedException {
        Long userId = userDetails.getUserId();
        TalentPostEntity talentPostEntity = talentPostRepository.findById(talentPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        if (!talentPostEntity.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("작성자만 삭제할 수 있습니다.");
        }
        talentPostRepository.delete(talentPostEntity);
    }

    @Transactional
    public TalentPostEntity inactiveStatus(Long talentPostId, CustomUserDetails userDetails) throws AccessDeniedException {
        Long userId = userDetails.getUserId();
        TalentPostEntity talentPostEntity = talentPostRepository.findById(talentPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        if (!talentPostEntity.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("작성자만 상태를 변경할 수 있습니다.");
        }
        talentPostEntity.inactiveStatus();
        return talentPostEntity;
    }
}
