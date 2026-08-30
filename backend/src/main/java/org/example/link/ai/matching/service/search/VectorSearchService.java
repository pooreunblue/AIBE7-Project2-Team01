package org.example.link.ai.matching.service.search;

import lombok.RequiredArgsConstructor;
import org.example.link.ai.embedding.enums.EmbeddingTargetType;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStoreRetriever;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * VectorStore에서 자연어 의미가 비슷한 게시글 후보를 찾는다.
 * 이 서비스는 게시글을 저장하거나 수정하지 않으며 읽기 전용 검색만 담당한다.
 */
@Service
@RequiredArgsConstructor
public class VectorSearchService {
    private static final int VECTOR_CANDIDATE_LIMIT = 30;
    private static final String TARGET_TYPE_KEY = "targetType";
    private static final String TARGET_ID_KEY = "targetId";

    private final VectorStoreRetriever vectorStoreRetriever;

    /**
     * targetType으로 TALENT와 REQUEST 벡터를 구분한 뒤 유사한 후보를 최대 30개 조회한다.
     * 가격, 상태 같은 정확한 조건은 여기서 판단하지 않고 SQL 조회 단계에서 다시 확인한다.
     */
    public List<VectorMatch> search(String query, EmbeddingTargetType targetType) {
        FilterExpressionBuilder filterBuilder = new FilterExpressionBuilder();
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(VECTOR_CANDIDATE_LIMIT)
                .similarityThresholdAll()
                .filterExpression(filterBuilder.eq(TARGET_TYPE_KEY, targetType.name()).build())
                .build();

        List<Document> documents = vectorStoreRetriever.similaritySearch(searchRequest);
        if (documents == null || documents.isEmpty()) {
            return List.of();
        }

        Map<UUID, Double> scoresByTargetId = new LinkedHashMap<>();
        for (Document document : documents) {
            UUID targetId = readTargetId(document.getMetadata());
            Double score = document.getScore();
            // A가 저장한 metadata 계약이 깨진 문서는 검색 결과에서 제외한다.
            if (targetId == null) {
                continue;
            }
            if (score == null) {
                continue;
            }
            scoresByTargetId.merge(targetId, clamp(score), Math::max);
        }

        // 같은 게시글이 중복 검색되면 가장 높은 유사도 점수만 남긴다.
        List<VectorMatch> matches = new ArrayList<>(scoresByTargetId.size());
        scoresByTargetId.forEach((targetId, score) -> matches.add(new VectorMatch(targetId, score)));
        matches.sort((left, right) -> Double.compare(right.semanticScore(), left.semanticScore()));
        return matches;
    }

    private UUID readTargetId(Map<String, Object> metadata) {
        Object rawTargetId = metadata.get(TARGET_ID_KEY);
        if (rawTargetId == null) {
            return null;
        }

        try {
            return UUID.fromString(rawTargetId.toString());
        }
        catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private double clamp(double score) {
        if (score < 0.0) {
            return 0.0;
        }
        if (score > 1.0) {
            return 1.0;
        }
        return score;
    }

    /** SQL에서 원본 글을 조회할 UUID와 벡터 유사도 점수만 다음 단계로 전달한다. */
    public record VectorMatch(UUID targetId, double semanticScore) {
    }
}
