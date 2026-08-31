package org.example.link.ai.matching.service;

import org.example.link.ai.embedding.enums.EmbeddingTargetType;
import org.example.link.ai.matching.dto.AiMatchResponse;
import org.example.link.ai.matching.dto.MatchCondition;
import org.example.link.ai.matching.dto.SearchAiMatchRequest;
import org.example.link.ai.matching.service.candidate.MatchCandidateFactory;
import org.example.link.ai.matching.service.condition.MatchConditionValidator;
import org.example.link.ai.matching.service.filter.MatchCandidateFilter;
import org.example.link.ai.matching.service.ranking.MatchRankingService;
import org.example.link.ai.matching.service.search.VectorSearchService;
import org.example.link.domain.request.entity.RequestPostEntity;
import org.example.link.domain.request.repository.RequestPostRepository;
import org.example.link.domain.request.util.RequestPostStatus;
import org.example.link.domain.talent.entity.TalentPostEntity;
import org.example.link.domain.talent.repository.TalentPostRepository;
import org.example.link.domain.talent.util.TalentPostStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiMatchingServiceTest {
    private final VectorSearchService vectorSearchService = mock(VectorSearchService.class);
    private final TalentPostRepository talentPostRepository = mock(TalentPostRepository.class);
    private final RequestPostRepository requestPostRepository = mock(RequestPostRepository.class);
    private final AiMatchingService aiMatchingService = new AiMatchingService(
            vectorSearchService,
            new MatchConditionValidator(),
            new MatchCandidateFilter(),
            new MatchRankingService(),
            new MatchCandidateFactory(),
            talentPostRepository,
            requestPostRepository
    );

    @Test
    void excludesInactiveTalentUsingSqlSourceOfTruth() {
        UUID targetId = UUID.randomUUID();
        TalentPostEntity inactiveTalent = mock(TalentPostEntity.class);
        when(inactiveTalent.getId()).thenReturn(targetId);
        when(inactiveTalent.getStatus()).thenReturn(TalentPostStatus.INACTIVE);
        when(vectorSearchService.search("백엔드 개발", EmbeddingTargetType.TALENT))
                .thenReturn(List.of(new VectorSearchService.VectorMatch(targetId, 0.98)));
        when(talentPostRepository.findByIdIn(anyCollection())).thenReturn(List.of(inactiveTalent));

        AiMatchResponse response = aiMatchingService.match(new SearchAiMatchRequest(
                "백엔드 개발",
                EmbeddingTargetType.TALENT,
                MatchCondition.empty(),
                5
        ));

        assertThat(response.candidates()).isEmpty();
        verify(talentPostRepository).findByIdIn(List.of(targetId));
    }

    @Test
    void excludesClosedRequestUsingSqlSourceOfTruth() {
        UUID targetId = UUID.randomUUID();
        RequestPostEntity closedRequest = mock(RequestPostEntity.class);
        when(closedRequest.getId()).thenReturn(targetId);
        when(closedRequest.getStatus()).thenReturn(RequestPostStatus.CLOSED);
        when(vectorSearchService.search("API 개발 요청", EmbeddingTargetType.REQUEST))
                .thenReturn(List.of(new VectorSearchService.VectorMatch(targetId, 0.96)));
        when(requestPostRepository.findByIdIn(anyCollection())).thenReturn(List.of(closedRequest));

        MatchCondition condition = new MatchCondition(
                null,
                null,
                null,
                null,
                300_000L,
                700_000L,
                LocalDate.now(),
                LocalDate.now().plusMonths(1)
        );
        AiMatchResponse response = aiMatchingService.match(new SearchAiMatchRequest(
                "API 개발 요청",
                EmbeddingTargetType.REQUEST,
                condition,
                3
        ));

        assertThat(response.candidates()).isEmpty();
        verify(requestPostRepository).findByIdIn(List.of(targetId));
    }
}
