package org.example.link.ai.matching.service.analysis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.link.ai.embedding.enums.EmbeddingTargetType;
import org.example.link.ai.matching.dto.AnalyzeAiMatchResponse;
import org.example.link.ai.matching.dto.MatchCondition;
import org.example.link.domain.category.entity.CategoryEntity;
import org.example.link.domain.category.repository.CategoryRepository;
import org.example.link.domain.talent.util.DurationUnit;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 검색창의 자연어를 매칭 API가 이해할 수 있는 구조화 조건으로 바꾼다.
 * LLM 분석에 실패해도 사용자의 원문 검색은 계속 가능하도록 간단한 규칙 기반 분석으로 보완한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiMatchQueryAnalysisService {
    private static final Pattern MONEY_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(만원|천원|원)?");
    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)\\s*(일|주|주일|개월|달)");

    private final ChatModel chatModel;
    private final CategoryRepository categoryRepository;
    private final JsonMapper objectMapper;

    public AnalyzeAiMatchResponse analyze(String query) {
        String normalizedQuery = normalize(query);
        List<CategoryEntity> categories = categoryRepository.findAll();

        try {
            LlmMatchAnalysis analysis = requestAnalysis(normalizedQuery, categories);
            return toResponse(normalizedQuery, analysis, categories);
        } catch (RuntimeException exception) {
            log.warn("AI 매칭 검색어 분석에 실패해 규칙 기반 분석 결과를 사용합니다.", exception);
            return fallbackResponse(normalizedQuery, categories);
        }
    }

    private LlmMatchAnalysis requestAnalysis(String query, List<CategoryEntity> categories) {
        String instruction = buildInstruction(query, categories);
        UserMessage message = UserMessage.builder().text(instruction).build();
        ChatResponse chatResponse = chatModel.call(new Prompt(message));
        String result = chatResponse.getResult().getOutput().getText();
        return objectMapper.readValue(cleanJson(result), LlmMatchAnalysis.class);
    }

    private String buildInstruction(String query, List<CategoryEntity> categories) {
        return """
                사용자의 AI 검색 문장을 읽고 재능 매칭 검색 조건을 JSON 하나로만 추출하세요.
                targetType은 사용자가 전문가/서비스를 찾으면 TALENT, 수행할 일/의뢰/요청글을 찾으면 REQUEST입니다.
                의미 검색에 필요한 핵심 문장만 semanticQuery에 넣으세요.
                카테고리는 아래 목록 중 가장 가까운 name만 categoryName에 넣고, 확실하지 않으면 null입니다.
                TALENT 조건은 maxPrice, maxEstimatedDuration, durationUnit만 사용하세요.
                REQUEST 조건은 minBudget, maxBudget, dueDateFrom, dueDateTo만 사용하세요.
                금액은 원 단위 숫자로 변환하고, 날짜는 yyyy-MM-dd 형식입니다.
                알 수 없는 값은 null로 두세요.
                결과 형식:
                {
                  "targetType": "TALENT 또는 REQUEST",
                  "semanticQuery": "검색용 핵심 문장",
                  "categoryName": "카테고리명 또는 null",
                  "maxPrice": 500000,
                  "maxEstimatedDuration": 7,
                  "durationUnit": "DAY",
                  "minBudget": null,
                  "maxBudget": null,
                  "dueDateFrom": null,
                  "dueDateTo": null
                }

                카테고리 목록:
                %s

                사용자 검색 문장:
                %s
                """.formatted(categoryNames(categories), query);
    }

    private AnalyzeAiMatchResponse toResponse(
            String originalQuery,
            LlmMatchAnalysis analysis,
            List<CategoryEntity> categories
    ) {
        EmbeddingTargetType targetType = resolveTargetType(analysis.targetType(), originalQuery);
        CategoryEntity category = findCategory(analysis.categoryName(), originalQuery, categories);
        String semanticQuery = resolveSemanticQuery(analysis.semanticQuery(), originalQuery);
        MatchCondition condition = conditionFor(targetType, category, analysis);

        String categoryName = null;
        if (category != null) {
            categoryName = category.getName();
        }

        return new AnalyzeAiMatchResponse(
                originalQuery,
                semanticQuery,
                targetType,
                condition,
                categoryName
        );
    }

    private AnalyzeAiMatchResponse fallbackResponse(String query, List<CategoryEntity> categories) {
        EmbeddingTargetType targetType = resolveTargetType(null, query);
        CategoryEntity category = findCategory(null, query, categories);
        LlmMatchAnalysis fallback = new LlmMatchAnalysis(
                targetType.name(),
                query,
                null,
                readMoney(query),
                readDuration(query),
                readDurationUnit(query),
                readMoney(query),
                readMoney(query),
                null,
                null
        );
        return toResponse(query, fallback, categories);
    }

    private MatchCondition conditionFor(
            EmbeddingTargetType targetType,
            CategoryEntity category,
            LlmMatchAnalysis analysis
    ) {
        UUID categoryId = null;
        if (category != null) {
            categoryId = category.getId();
        }

        if (targetType == EmbeddingTargetType.TALENT) {
            return new MatchCondition(
                    categoryId,
                    positiveOrNull(analysis.maxPrice()),
                    positiveIntegerOrNull(analysis.maxEstimatedDuration()),
                    resolveDurationUnit(analysis.durationUnit()),
                    null,
                    null,
                    null,
                    null
            );
        }

        return new MatchCondition(
                categoryId,
                null,
                null,
                null,
                positiveOrNull(analysis.minBudget()),
                positiveOrNull(analysis.maxBudget()),
                analysis.dueDateFrom(),
                analysis.dueDateTo()
        );
    }

    private EmbeddingTargetType resolveTargetType(String rawType, String query) {
        if (rawType != null) {
            try {
                return EmbeddingTargetType.valueOf(rawType.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                log.debug("지원하지 않는 AI 매칭 targetType 분석 결과: {}", rawType);
            }
        }

        String lowerQuery = query.toLowerCase(Locale.ROOT);
        if (containsAny(lowerQuery, "요청", "의뢰", "프로젝트", "일거리", "구인", "구해요", "맡길")) {
            return EmbeddingTargetType.REQUEST;
        }
        return EmbeddingTargetType.TALENT;
    }

    private CategoryEntity findCategory(
            String categoryName,
            String query,
            List<CategoryEntity> categories
    ) {
        CategoryEntity exactMatch = findCategoryByName(categoryName, categories);
        if (exactMatch != null) {
            return exactMatch;
        }

        for (CategoryEntity category : categories) {
            String name = normalize(category.getName());
            if (!name.isBlank() && query.contains(name)) {
                return category;
            }
        }
        return null;
    }

    private CategoryEntity findCategoryByName(String categoryName, List<CategoryEntity> categories) {
        if (categoryName == null || categoryName.isBlank()) {
            return null;
        }

        for (CategoryEntity category : categories) {
            if (categoryName.trim().equalsIgnoreCase(category.getName())) {
                return category;
            }
        }
        return null;
    }

    private String resolveSemanticQuery(String semanticQuery, String originalQuery) {
        if (semanticQuery == null || semanticQuery.isBlank()) {
            return originalQuery;
        }
        return normalize(semanticQuery);
    }

    private DurationUnit resolveDurationUnit(String rawUnit) {
        if (rawUnit == null || rawUnit.isBlank()) {
            return null;
        }

        try {
            return DurationUnit.valueOf(rawUnit.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private Long readMoney(String query) {
        Matcher matcher = MONEY_PATTERN.matcher(query);
        while (matcher.find()) {
            long amount = toWon(matcher.group(1), matcher.group(2));
            if (amount >= 10_000L) {
                return amount;
            }
        }
        return null;
    }

    private long toWon(String number, String unit) {
        double parsed = Double.parseDouble(number);
        if ("만원".equals(unit)) {
            return Math.round(parsed * 10_000L);
        }
        if ("천원".equals(unit)) {
            return Math.round(parsed * 1_000L);
        }
        return Math.round(parsed);
    }

    private Integer readDuration(String query) {
        Matcher matcher = DURATION_PATTERN.matcher(query);
        if (!matcher.find()) {
            return null;
        }
        return Integer.valueOf(matcher.group(1));
    }

    private String readDurationUnit(String query) {
        Matcher matcher = DURATION_PATTERN.matcher(query);
        if (!matcher.find()) {
            return null;
        }

        String unit = matcher.group(2);
        if ("일".equals(unit)) {
            return DurationUnit.DAY.name();
        }
        if ("주".equals(unit) || "주일".equals(unit)) {
            return DurationUnit.WEEK.name();
        }
        return DurationUnit.MONTH.name();
    }

    private Long positiveOrNull(Long value) {
        if (value == null || value < 0L) {
            return null;
        }
        return value;
    }

    private Integer positiveIntegerOrNull(Integer value) {
        if (value == null || value <= 0) {
            return null;
        }
        return value;
    }

    private boolean containsAny(String text, String... words) {
        for (String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private String categoryNames(List<CategoryEntity> categories) {
        StringBuilder names = new StringBuilder();
        for (CategoryEntity category : categories) {
            names.append("- ").append(category.getName()).append('\n');
        }
        return names.toString();
    }

    private String cleanJson(String result) {
        return result.replaceFirst("^```json\\s*", "")
                .replaceFirst("\\s*```$", "")
                .trim();
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\\s+", " ").trim();
    }

    public record LlmMatchAnalysis(
            String targetType,
            String semanticQuery,
            String categoryName,
            Long maxPrice,
            Integer maxEstimatedDuration,
            String durationUnit,
            Long minBudget,
            Long maxBudget,
            LocalDate dueDateFrom,
            LocalDate dueDateTo
    ) {
    }
}
