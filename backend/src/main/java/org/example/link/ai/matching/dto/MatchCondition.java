package org.example.link.ai.matching.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.example.link.domain.talent.util.DurationUnit;

import java.time.LocalDate;
import java.util.UUID;

/**
 * SQL 원본 데이터에 적용할 정형 검색 조건이다.
 * TALENT와 REQUEST에서 사용하는 필드가 다르므로 대상에 맞지 않는 조건은 Validator가 거부한다.
 */
public record MatchCondition(
        UUID categoryId,
        @PositiveOrZero Long maxPrice,
        @Positive Integer maxEstimatedDuration,
        DurationUnit durationUnit,
        @PositiveOrZero Long minBudget,
        @PositiveOrZero Long maxBudget,
        LocalDate dueDateFrom,
        LocalDate dueDateTo
) {
    public static MatchCondition empty() {
        return new MatchCondition(null, null, null, null, null, null, null, null);
    }
}
