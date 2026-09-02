package org.example.link.ai.matching.service.candidate;

import org.example.link.ai.matching.service.ranking.MatchRankingService;
import org.example.link.domain.category.entity.CategoryEntity;
import org.example.link.domain.request.entity.RequestPostEntity;
import org.example.link.domain.talent.entity.TalentPostEntity;
import org.example.link.domain.talent.util.DurationUnit;
import org.example.link.domain.user.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MatchCandidateFactoryTest {

    private final MatchCandidateFactory candidateFactory = new MatchCandidateFactory();

    @Test
    void includesTalentThumbnailUrlInCandidate() {
        UserEntity user = user();
        CategoryEntity category = category();
        TalentPostEntity talent = TalentPostEntity.builder()
                .id(UUID.randomUUID())
                .user(user)
                .category(category)
                .title("Spring 백엔드 개발")
                .content("REST API를 개발합니다.")
                .price(500_000L)
                .estimatedDuration(7)
                .durationUnit(DurationUnit.DAY)
                .build();

        MatchCandidate candidate = candidateFactory.createTalent(
                talent,
                "https://example.com/talent-thumbnail.png",
                0.9,
                new MatchRankingService.MatchScore(0.9, null)
        );

        assertThat(candidate.thumbnailUrl())
                .isEqualTo("https://example.com/talent-thumbnail.png");
    }

    @Test
    void keepsRequestThumbnailUrlNullWhenPostHasNoThumbnail() {
        UserEntity user = user();
        CategoryEntity category = category();
        RequestPostEntity request = RequestPostEntity.builder()
                .id(UUID.randomUUID())
                .user(user)
                .category(category)
                .title("백엔드 개발 요청")
                .content("Spring API 개발이 필요합니다.")
                .budgetMin(300_000L)
                .budgetMax(700_000L)
                .dueDate(LocalDate.now().plusWeeks(2))
                .build();

        MatchCandidate candidate = candidateFactory.createRequest(
                request,
                null,
                0.8,
                new MatchRankingService.MatchScore(0.8, null)
        );

        assertThat(candidate.thumbnailUrl()).isNull();
    }

    private UserEntity user() {
        UserEntity user = mock(UserEntity.class);
        when(user.getId()).thenReturn(UUID.randomUUID());
        when(user.getNickname()).thenReturn("테스트 사용자");
        return user;
    }

    private CategoryEntity category() {
        CategoryEntity category = mock(CategoryEntity.class);
        when(category.getId()).thenReturn(UUID.randomUUID());
        when(category.getName()).thenReturn("개발");
        return category;
    }
}
