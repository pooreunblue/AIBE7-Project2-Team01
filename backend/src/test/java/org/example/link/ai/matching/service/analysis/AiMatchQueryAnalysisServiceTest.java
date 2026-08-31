package org.example.link.ai.matching.service.analysis;

import org.example.link.ai.embedding.enums.EmbeddingTargetType;
import org.example.link.domain.category.entity.CategoryEntity;
import org.example.link.domain.category.repository.CategoryRepository;
import org.example.link.domain.talent.util.DurationUnit;
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
import static org.mockito.Mockito.when;

class AiMatchQueryAnalysisServiceTest {

    private final ChatModel chatModel = mock(ChatModel.class);
    private final CategoryRepository categoryRepository = mock(CategoryRepository.class);
    private final AiMatchQueryAnalysisService service = new AiMatchQueryAnalysisService(
            chatModel,
            categoryRepository,
            JsonMapper.builder().build()
    );

    @Test
    void analyzesTalentQueryAndMapsCategoryNameToId() {
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findAll()).thenReturn(List.of(category(categoryId, "개발")));
        ChatResponse chatResponse = response("""
                {
                  "targetType": "TALENT",
                  "semanticQuery": "Spring Boot 백엔드 개발",
                  "categoryName": "개발",
                  "maxPrice": 500000,
                  "maxEstimatedDuration": 7,
                  "durationUnit": "DAY",
                  "minBudget": null,
                  "maxBudget": null,
                  "dueDateFrom": null,
                  "dueDateTo": null
                }
                """);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        var result = service.analyze("50만원 이하 7일 안에 가능한 Spring 백엔드 개발자 찾아줘");

        assertThat(result.targetType()).isEqualTo(EmbeddingTargetType.TALENT);
        assertThat(result.semanticQuery()).isEqualTo("Spring Boot 백엔드 개발");
        assertThat(result.categoryName()).isEqualTo("개발");
        assertThat(result.condition().categoryId()).isEqualTo(categoryId);
        assertThat(result.condition().maxPrice()).isEqualTo(500_000L);
        assertThat(result.condition().maxEstimatedDuration()).isEqualTo(7);
        assertThat(result.condition().durationUnit()).isEqualTo(DurationUnit.DAY);
    }

    @Test
    void fallsBackToRuleBasedAnalysisWhenLlmFails() {
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findAll()).thenReturn(List.of(category(categoryId, "개발")));
        when(chatModel.call(any(Prompt.class))).thenThrow(new IllegalStateException("AI unavailable"));

        var result = service.analyze("개발 요청글 50만원 의뢰 찾아줘");

        assertThat(result.targetType()).isEqualTo(EmbeddingTargetType.REQUEST);
        assertThat(result.semanticQuery()).isEqualTo("개발 요청글 50만원 의뢰 찾아줘");
        assertThat(result.condition().categoryId()).isEqualTo(categoryId);
        assertThat(result.condition().minBudget()).isEqualTo(500_000L);
        assertThat(result.condition().maxBudget()).isEqualTo(500_000L);
    }

    private CategoryEntity category(UUID id, String name) {
        return CategoryEntity.builder()
                .id(id)
                .name(name)
                .description("")
                .active(true)
                .build();
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
}
