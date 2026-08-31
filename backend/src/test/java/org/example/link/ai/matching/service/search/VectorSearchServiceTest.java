package org.example.link.ai.matching.service.search;

import org.example.link.ai.embedding.enums.EmbeddingTargetType;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStoreRetriever;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VectorSearchServiceTest {
    private final VectorStoreRetriever vectorStoreRetriever = mock(VectorStoreRetriever.class);
    private final VectorSearchService vectorSearchService = new VectorSearchService(vectorStoreRetriever);

    @Test
    void returnsHighestScoreForEachValidTargetId() {
        UUID targetId = UUID.randomUUID();
        Document lowerScore = document(targetId.toString(), 0.71);
        Document higherScore = document(targetId.toString(), 0.86);
        Document malformedId = document("not-a-uuid", 0.99);
        Document noScore = document(UUID.randomUUID().toString(), null);
        when(vectorStoreRetriever.similaritySearch(org.mockito.ArgumentMatchers.any(SearchRequest.class)))
                .thenReturn(List.of(lowerScore, malformedId, higherScore, noScore));

        List<VectorSearchService.VectorMatch> result =
                vectorSearchService.search("Spring 백엔드 개발", EmbeddingTargetType.TALENT);

        assertThat(result).containsExactly(new VectorSearchService.VectorMatch(targetId, 0.86));

        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(vectorStoreRetriever).similaritySearch(captor.capture());
        assertThat(captor.getValue().getQuery()).isEqualTo("Spring 백엔드 개발");
        assertThat(captor.getValue().getTopK()).isEqualTo(30);
        assertThat(captor.getValue().getFilterExpression().toString())
                .contains("targetType", "TALENT");
    }

    private Document document(String targetId, Double score) {
        return Document.builder()
                .id("TALENT:" + targetId)
                .text("테스트 문서")
                .metadata("targetType", "TALENT")
                .metadata("targetId", targetId)
                .score(score)
                .build();
    }
}
