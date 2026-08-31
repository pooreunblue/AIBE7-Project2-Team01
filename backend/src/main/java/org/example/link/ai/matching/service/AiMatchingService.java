package org.example.link.ai.matching.service;

import lombok.RequiredArgsConstructor;
import org.example.link.ai.embedding.enums.EmbeddingTargetType;
import org.example.link.ai.matching.dto.AiMatchResponse;
import org.example.link.ai.matching.dto.MatchCondition;
import org.example.link.ai.matching.dto.SearchAiMatchRequest;
import org.example.link.ai.matching.service.candidate.MatchCandidate;
import org.example.link.ai.matching.service.candidate.MatchCandidateFactory;
import org.example.link.ai.matching.service.condition.MatchConditionValidator;
import org.example.link.ai.matching.service.filter.MatchCandidateFilter;
import org.example.link.ai.matching.service.ranking.MatchRankingService;
import org.example.link.ai.matching.service.recommendation.RecommendationReasonService;
import org.example.link.ai.matching.service.search.VectorSearchService;
import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;
import org.example.link.domain.request.entity.RequestPostEntity;
import org.example.link.domain.request.entity.RequestPostFileEntity;
import org.example.link.domain.request.repository.RequestPostFileRepository;
import org.example.link.domain.request.repository.RequestPostRepository;
import org.example.link.domain.talent.entity.TalentPostEntity;
import org.example.link.domain.talent.entity.TalentPostFileEntity;
import org.example.link.domain.talent.repository.TalentPostFileRepository;
import org.example.link.domain.talent.repository.TalentPostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 매칭 전체 순서를 조율하는 서비스다.
 * 의미가 비슷한 후보는 VectorStore에서 찾고, 실제 조건과 응답 데이터는 SQL 원본으로 확인한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiMatchingService {
    private final VectorSearchService vectorSearchService;
    private final MatchConditionValidator conditionValidator;
    private final MatchCandidateFilter candidateFilter;
    private final MatchRankingService rankingService;
    private final MatchCandidateFactory candidateFactory;
    private final TalentPostRepository talentPostRepository;
    private final RequestPostRepository requestPostRepository;
    private final TalentPostFileRepository talentPostFileRepository;
    private final RequestPostFileRepository requestPostFileRepository;
    private final RecommendationReasonService recommendationReasonService;

    /**
     * 매칭 처리 순서:
     * 조건 검증 -> 벡터 후보 검색 -> SQL 원본 조회/필터 -> 점수 계산 -> 상위 결과 반환
     */
    public AiMatchResponse match(SearchAiMatchRequest request) {
        MatchCondition condition = request.resolvedCondition();
        conditionValidator.validate(request.targetType(), condition);

        // 벡터 검색 결과에는 후보 UUID와 의미 유사도 점수만 사용한다.
        List<VectorSearchService.VectorMatch> vectorMatches = vectorSearchService.search(
                request.query(),
                request.targetType()
        );
        List<MatchCandidate> candidates = findCandidates(
                request.targetType(),
                vectorMatches,
                condition
        );
        List<MatchCandidate> rankedCandidates = rankAndLimit(candidates, request.resolvedLimit());
        List<MatchCandidate> candidatesWithReasons = recommendationReasonService.addRecommendationReasons(
                request.query(),
                rankedCandidates
        );

        return new AiMatchResponse(request.query(), request.targetType(), candidatesWithReasons);
    }

    private List<MatchCandidate> findCandidates(
            EmbeddingTargetType targetType,
            List<VectorSearchService.VectorMatch> vectorMatches,
            MatchCondition condition
    ) {
        if (targetType == EmbeddingTargetType.TALENT) {
            return matchTalents(vectorMatches, condition);
        }
        if (targetType == EmbeddingTargetType.REQUEST) {
            return matchRequests(vectorMatches, condition);
        }
        throw new CustomException(ErrorCode.MATCH_TARGET_NOT_SUPPORTED);
    }

    private List<MatchCandidate> matchTalents(
            List<VectorSearchService.VectorMatch> vectorMatches,
            MatchCondition condition
    ) {
        if (vectorMatches.isEmpty()) {
            return List.of();
        }

        // 후보를 한 건씩 조회하지 않고 UUID 목록으로 한 번에 조회한다.
        Map<UUID, TalentPostEntity> talentsById = loadTalentsById(vectorMatches);
        Map<UUID, String> thumbnailUrlsById = loadTalentThumbnailUrls(vectorMatches);
        List<MatchCandidate> candidates = new ArrayList<>();

        for (VectorSearchService.VectorMatch vectorMatch : vectorMatches) {
            TalentPostEntity talent = talentsById.get(vectorMatch.targetId());
            // 벡터에는 남아 있지만 원본 글이 삭제된 경우 건너뛴다.
            if (talent == null) {
                continue;
            }
            // 상태, 카테고리, 가격, 작업 기간은 SQL에서 가져온 값으로 판단한다.
            if (!candidateFilter.matchesTalent(talent, condition)) {
                continue;
            }

            // 필수 조건을 통과한 후보만 최종 점수를 계산한다.
            MatchRankingService.MatchScore score = rankingService.scoreTalent(
                    vectorMatch.semanticScore(),
                    talent.getPrice(),
                    condition.maxPrice()
            );
            candidates.add(candidateFactory.createTalent(
                    talent,
                    thumbnailUrlsById.get(talent.getId()),
                    vectorMatch.semanticScore(),
                    score
            ));
        }
        return candidates;
    }

    private List<MatchCandidate> matchRequests(
            List<VectorSearchService.VectorMatch> vectorMatches,
            MatchCondition condition
    ) {
        if (vectorMatches.isEmpty()) {
            return List.of();
        }

        // 후보를 한 건씩 조회하지 않고 UUID 목록으로 한 번에 조회한다.
        Map<UUID, RequestPostEntity> requestsById = loadRequestsById(vectorMatches);
        Map<UUID, String> thumbnailUrlsById = loadRequestThumbnailUrls(vectorMatches);
        List<MatchCandidate> candidates = new ArrayList<>();

        for (VectorSearchService.VectorMatch vectorMatch : vectorMatches) {
            RequestPostEntity request = requestsById.get(vectorMatch.targetId());
            // 벡터에는 남아 있지만 원본 글이 삭제된 경우 건너뛴다.
            if (request == null) {
                continue;
            }
            // 상태, 카테고리, 예산, 마감일은 SQL에서 가져온 값으로 판단한다.
            if (!candidateFilter.matchesRequest(request, condition)) {
                continue;
            }

            // 필수 조건을 통과한 후보만 최종 점수를 계산한다.
            MatchRankingService.MatchScore score = rankingService.scoreRequest(
                    vectorMatch.semanticScore(),
                    request.getBudgetMin(),
                    request.getBudgetMax(),
                    condition.minBudget(),
                    condition.maxBudget()
            );
            candidates.add(candidateFactory.createRequest(
                    request,
                    thumbnailUrlsById.get(request.getId()),
                    vectorMatch.semanticScore(),
                    score
            ));
        }
        return candidates;
    }

    private Map<UUID, TalentPostEntity> loadTalentsById(
            List<VectorSearchService.VectorMatch> vectorMatches
    ) {
        List<UUID> targetIds = extractTargetIds(vectorMatches);
        List<TalentPostEntity> talents = talentPostRepository.findByIdIn(targetIds);
        Map<UUID, TalentPostEntity> talentsById = new HashMap<>();

        for (TalentPostEntity talent : talents) {
            talentsById.put(talent.getId(), talent);
        }
        return talentsById;
    }

    private Map<UUID, RequestPostEntity> loadRequestsById(
            List<VectorSearchService.VectorMatch> vectorMatches
    ) {
        List<UUID> targetIds = extractTargetIds(vectorMatches);
        List<RequestPostEntity> requests = requestPostRepository.findByIdIn(targetIds);
        Map<UUID, RequestPostEntity> requestsById = new HashMap<>();

        for (RequestPostEntity request : requests) {
            requestsById.put(request.getId(), request);
        }
        return requestsById;
    }

    private List<UUID> extractTargetIds(List<VectorSearchService.VectorMatch> vectorMatches) {
        List<UUID> targetIds = new ArrayList<>(vectorMatches.size());
        for (VectorSearchService.VectorMatch vectorMatch : vectorMatches) {
            targetIds.add(vectorMatch.targetId());
        }
        return targetIds;
    }

    private Map<UUID, String> loadTalentThumbnailUrls(
            List<VectorSearchService.VectorMatch> vectorMatches
    ) {
        List<UUID> targetIds = extractTargetIds(vectorMatches);
        List<TalentPostFileEntity> thumbnails =
                talentPostFileRepository.findAllByTalentPostIdInAndThumbnailTrue(targetIds);
        Map<UUID, String> thumbnailUrlsById = new HashMap<>();

        for (TalentPostFileEntity thumbnail : thumbnails) {
            thumbnailUrlsById.put(thumbnail.getTalentPost().getId(), thumbnail.getFileUrl());
        }
        return thumbnailUrlsById;
    }

    private Map<UUID, String> loadRequestThumbnailUrls(
            List<VectorSearchService.VectorMatch> vectorMatches
    ) {
        List<UUID> targetIds = extractTargetIds(vectorMatches);
        List<RequestPostFileEntity> thumbnails =
                requestPostFileRepository.findAllByRequestPostIdInAndThumbnailTrue(targetIds);
        Map<UUID, String> thumbnailUrlsById = new HashMap<>();

        for (RequestPostFileEntity thumbnail : thumbnails) {
            thumbnailUrlsById.put(thumbnail.getRequestPost().getId(), thumbnail.getFileUrl());
        }
        return thumbnailUrlsById;
    }

    private List<MatchCandidate> rankAndLimit(List<MatchCandidate> candidates, int limit) {
        // 최종 점수가 같으면 의미 유사도가 높은 후보를 먼저 보여준다.
        Comparator<MatchCandidate> scoreComparator = Comparator
                .comparingDouble(MatchCandidate::matchScore)
                .thenComparingDouble(MatchCandidate::semanticScore)
                .reversed();

        return candidates.stream()
                .sorted(scoreComparator)
                .limit(limit)
                .toList();
    }
}
