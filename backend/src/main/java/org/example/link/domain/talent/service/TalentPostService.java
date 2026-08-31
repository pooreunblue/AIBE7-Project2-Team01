package org.example.link.domain.talent.service;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.example.link.ai.embedding.event.EmbeddingEventPublisher;
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
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;

@Service
@RequiredArgsConstructor
@Transactional
public class TalentPostService {
    private final TalentPostRepository talentPostRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final EmbeddingEventPublisher embeddingEventPublisher;

    @Transactional
    public TalentPostEntity create(TalentPostRequestDto talentPostRequestDto, CustomUserDetails userDetails) {
        UUID userId = getUserId(userDetails);
        UserEntity user = getUser(userId);
        CategoryEntity category = getCategory(talentPostRequestDto);
        PortfolioEntity portfolio = getPortfolio(talentPostRequestDto);
        TalentPostEntity talentPostEntity = createTalentPost(talentPostRequestDto, user, category, portfolio);
        TalentPostEntity saved = talentPostRepository.save(talentPostEntity);
        embeddingEventPublisher.saveTalent(saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<TalentPostEntity> readAll(Pageable pageable) {
        return talentPostRepository.findAll(pageable);
    }

    public TalentPostEntity readOne(UUID talentPostId) {
        return getTalentPostEntity(talentPostId);
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
            UUID talentPostId,
            TalentPostRequestDto talentPostRequestDto,
            CustomUserDetails userDetails) throws AccessDeniedException {
        UUID userId = getUserId(userDetails);
        TalentPostEntity talentPostEntity = getTalentPostEntity(talentPostId);
        validateAuth(talentPostEntity, userId);
        CategoryEntity category = getCategory(talentPostRequestDto);
        PortfolioEntity portfolio = getPortfolio(talentPostRequestDto);
        updateTalentPost(talentPostRequestDto, talentPostEntity, category, portfolio);
        embeddingEventPublisher.replaceTalent(talentPostEntity);
        return talentPostEntity;
    }

    @Transactional
    public void delete(UUID talentPostId, CustomUserDetails userDetails) throws AccessDeniedException {
        UUID userId = getUserId(userDetails);
        TalentPostEntity talentPostEntity = getTalentPostEntity(talentPostId);
        validateAuth(talentPostEntity, userId);
        talentPostRepository.delete(talentPostEntity);
        embeddingEventPublisher.deleteTalent(talentPostId);
    }

    @Transactional
    public TalentPostEntity inactiveStatus(UUID talentPostId, CustomUserDetails userDetails) throws AccessDeniedException {
        UUID userId = getUserId(userDetails);
        TalentPostEntity talentPostEntity = getTalentPostEntity(talentPostId);
        validateAuth(talentPostEntity, userId);
        talentPostEntity.inactiveStatus();
        embeddingEventPublisher.deleteTalent(talentPostId);
        return talentPostEntity;
    }

    private UUID getUserId(CustomUserDetails userDetails) {
        return userDetails.getUserId();
    }

    private @NonNull UserEntity getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private @NonNull CategoryEntity getCategory(TalentPostRequestDto talentPostRequestDto) {
        return categoryRepository.findById(talentPostRequestDto.categoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private PortfolioEntity getPortfolio(TalentPostRequestDto talentPostRequestDto) {
        if (talentPostRequestDto.portfolioId() == null) {
            return null;
        }

        return portfolioRepository.findById(talentPostRequestDto.portfolioId())
                .orElseThrow(() -> new CustomException(ErrorCode.PORTFOLIO_NOT_FOUND));
    }

    private TalentPostEntity createTalentPost(TalentPostRequestDto talentPostRequestDto, UserEntity user, CategoryEntity category, PortfolioEntity portfolio) {
        return TalentPostEntity.builder()
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
    }

    private @NonNull TalentPostEntity getTalentPostEntity(UUID talentPostId) {
        return talentPostRepository.findById(talentPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private void validateAuth(TalentPostEntity talentPostEntity, UUID userId) throws AccessDeniedException {
        if (!talentPostEntity.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.POST_ACCESS_DENIED);
        }
    }

    private void updateTalentPost(TalentPostRequestDto talentPostRequestDto, TalentPostEntity talentPostEntity, CategoryEntity category, PortfolioEntity portfolio) {
        talentPostEntity.update(
                talentPostRequestDto.title(),
                talentPostRequestDto.content(),
                category,
                talentPostRequestDto.price(),
                talentPostRequestDto.estimatedDuration(),
                talentPostRequestDto.durationUnit(),
                portfolio
        );
    }
}
