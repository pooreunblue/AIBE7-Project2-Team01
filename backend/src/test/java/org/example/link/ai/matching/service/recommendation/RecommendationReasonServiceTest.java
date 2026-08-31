package org.example.link.ai.matching.service.recommendation;

import org.example.link.ai.embedding.enums.EmbeddingTargetType;
import org.example.link.ai.matching.service.candidate.MatchCandidate;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecommendationReasonServiceTest {

    private final ChatModel chatModel = mock(ChatModel.class);
    private final RecommendationReasonService service = new RecommendationReasonService(
            chatModel,
            JsonMapper.builder().build()
    );

    @Test
    void addsReasonReturnedForExistingCandidate() {
        UUID targetId = UUID.randomUUID();
        MatchCandidate candidate = candidate(targetId);
        ChatResponse chatResponse = response("""
                {"reasons":[{"targetId":"%s","reason":"검색한 Spring API 개발과 직접 관련된 재능입니다."}]}
                """.formatted(targetId));
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        List<MatchCandidate> result = service.addRecommendationReasons(
                "Spring API 개발",
                List.of(candidate)
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).recommendationReason())
                .isEqualTo("검색한 Spring API 개발과 직접 관련된 재능입니다.");
        verify(chatModel).call(any(Prompt.class));
    }

    @Test
    void keepsCandidatesWhenLlmCallFails() {
        List<MatchCandidate> candidates = List.of(candidate(UUID.randomUUID()));
        when(chatModel.call(any(Prompt.class))).thenThrow(new IllegalStateException("Gemini unavailable"));

        List<MatchCandidate> result = service.addRecommendationReasons(
                "Spring API 개발",
                candidates
        );

        assertThat(result).isSameAs(candidates);
        assertThat(result.get(0).recommendationReason()).isNull();
    }

    private ChatResponse response(String text) {
        ChatResponse response = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage message = mock(AssistantMessage.class);
        when(response.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(message);
        when(message.getText()).thenReturn(text);
        return response;
    }

    private MatchCandidate candidate(UUID targetId) {
        return new MatchCandidate(
                EmbeddingTargetType.TALENT,
                targetId,
                UUID.randomUUID(),
                "테스트 사용자",
                null,
                UUID.randomUUID(),
                "개발",
                "Spring API 개발",
                "Spring Boot로 REST API를 개발합니다.",
                null,
                500_000L,
                7,
                org.example.link.domain.talent.util.DurationUnit.DAY,
                null,
                null,
                null,
                0.9,
                null,
                0.9,
                null
        );
    }
}
