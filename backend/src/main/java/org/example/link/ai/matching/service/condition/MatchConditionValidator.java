package org.example.link.ai.matching.service.condition;

import org.example.link.ai.embedding.enums.EmbeddingTargetType;
import org.example.link.ai.matching.dto.MatchCondition;
import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

/**
 * 검색을 시작하기 전에 요청 조건의 형식과 대상별 사용 가능 여부를 검사한다.
 * 잘못된 조건을 일찍 차단해 뒤쪽 검색 코드가 유효한 입력만 다루게 한다.
 */
@Component
public class MatchConditionValidator {
    /** 공통 범위를 검사한 뒤 TALENT 또는 REQUEST 전용 조건인지 확인한다. */
    public void validate(EmbeddingTargetType targetType, MatchCondition condition) {
        validateTargetType(targetType);
        validateConditionRanges(condition);
        validateConditionsForTarget(targetType, condition);
    }

    private void validateTargetType(EmbeddingTargetType targetType) {
        if (targetType == null) {
            throw invalidTargetType();
        }
        if (targetType == EmbeddingTargetType.PORTFOLIO) {
            throw invalidTargetType();
        }
    }

    private void validateConditionRanges(MatchCondition condition) {
        if (hasIncompleteDurationCondition(condition)) {
            throw invalidCondition();
        }
        if (hasInvalidBudgetCondition(condition)) {
            throw invalidCondition();
        }
        if (hasInvalidDueDateCondition(condition)) {
            throw invalidCondition();
        }
    }

    private boolean hasIncompleteDurationCondition(MatchCondition condition) {
        // 작업 기간 숫자와 단위는 둘 다 있거나 둘 다 없어야 한다.
        boolean hasDuration = condition.maxEstimatedDuration() != null;
        boolean hasDurationUnit = condition.durationUnit() != null;
        return hasDuration != hasDurationUnit;
    }

    private boolean hasInvalidBudgetCondition(MatchCondition condition) {
        if (condition.minBudget() == null || condition.maxBudget() == null) {
            return false;
        }
        return condition.minBudget() > condition.maxBudget();
    }

    private boolean hasInvalidDueDateCondition(MatchCondition condition) {
        if (condition.dueDateFrom() == null || condition.dueDateTo() == null) {
            return false;
        }
        return condition.dueDateFrom().isAfter(condition.dueDateTo());
    }

    private void validateConditionsForTarget(
            EmbeddingTargetType targetType,
            MatchCondition condition
    ) {
        if (targetType == EmbeddingTargetType.TALENT) {
            validateTalentConditions(condition);
            return;
        }
        validateRequestConditions(condition);
    }

    private void validateTalentConditions(MatchCondition condition) {
        // 재능 판매글 검색에서는 요청글 전용 예산/마감 조건을 사용할 수 없다.
        if (condition.minBudget() != null) {
            throw invalidCondition();
        }
        if (condition.maxBudget() != null) {
            throw invalidCondition();
        }
        if (condition.dueDateFrom() != null) {
            throw invalidCondition();
        }
        if (condition.dueDateTo() != null) {
            throw invalidCondition();
        }
    }

    private void validateRequestConditions(MatchCondition condition) {
        // 요청글 검색에서는 재능 판매글 전용 가격/작업 기간 조건을 사용할 수 없다.
        if (condition.maxPrice() != null) {
            throw invalidCondition();
        }
        if (condition.maxEstimatedDuration() != null) {
            throw invalidCondition();
        }
        if (condition.durationUnit() != null) {
            throw invalidCondition();
        }
    }

    private CustomException invalidTargetType() {
        return new CustomException(ErrorCode.MATCH_TARGET_NOT_SUPPORTED);
    }

    private CustomException invalidCondition() {
        return new CustomException(ErrorCode.INVALID_MATCH_CONDITION);
    }
}
