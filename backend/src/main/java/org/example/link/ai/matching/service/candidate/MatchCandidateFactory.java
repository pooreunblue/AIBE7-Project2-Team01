package org.example.link.ai.matching.service.candidate;

import org.example.link.ai.embedding.enums.EmbeddingTargetType;
import org.example.link.ai.matching.service.ranking.MatchRankingService;
import org.example.link.domain.request.entity.RequestPostEntity;
import org.example.link.domain.talent.entity.TalentPostEntity;
import org.springframework.stereotype.Component;

/** SQL에서 조회한 원본 엔티티와 계산된 점수를 API 후보 형태로 변환한다. */
@Component
public class MatchCandidateFactory {
    /** 재능 판매글을 후보 응답으로 변환한다. REQUEST 전용 값은 null로 둔다. */
    public MatchCandidate createTalent(
            TalentPostEntity talent,
            double semanticScore,
            MatchRankingService.MatchScore score
    ) {
        return new MatchCandidate(
                EmbeddingTargetType.TALENT,
                talent.getId(),
                talent.getUser().getId(),
                talent.getUser().getNickname(),
                talent.getUser().getProfileImageUrl(),
                talent.getCategory().getId(),
                talent.getCategory().getName(),
                talent.getTitle(),
                talent.getContent(),
                talent.getPrice(),
                talent.getEstimatedDuration(),
                talent.getDurationUnit(),
                null,
                null,
                null,
                semanticScore,
                score.amountScore(),
                null,
                score.matchScore(),
                null
        );
    }

    /** 재능 요청글을 후보 응답으로 변환한다. TALENT 전용 값은 null로 둔다. */
    public MatchCandidate createRequest(
            RequestPostEntity request,
            double semanticScore,
            MatchRankingService.MatchScore score
    ) {
        return new MatchCandidate(
                EmbeddingTargetType.REQUEST,
                request.getId(),
                request.getUser().getId(),
                request.getUser().getNickname(),
                request.getUser().getProfileImageUrl(),
                request.getCategory().getId(),
                request.getCategory().getName(),
                request.getTitle(),
                request.getContent(),
                null,
                null,
                null,
                request.getBudgetMin(),
                request.getBudgetMax(),
                request.getDueDate(),
                semanticScore,
                score.amountScore(),
                null,
                score.matchScore(),
                null
        );
    }
}
