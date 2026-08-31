package org.example.link.ai.matching.service.filter;

import org.example.link.ai.matching.dto.MatchCondition;
import org.example.link.domain.request.entity.RequestPostEntity;
import org.example.link.domain.request.util.RequestPostStatus;
import org.example.link.domain.talent.entity.TalentPostEntity;
import org.example.link.domain.talent.util.DurationUnit;
import org.example.link.domain.talent.util.TalentPostStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

/**
 * VectorStore가 찾은 후보가 사용자의 정확한 조건을 만족하는지 판정한다.
 * 모든 값은 벡터 metadata가 아니라 SQL에서 다시 조회한 엔티티를 기준으로 한다.
 */
@Component
public class MatchCandidateFilter {
    /** 활성 재능글 중 카테고리, 최대 가격, 최대 작업 기간을 만족하는지 확인한다. */
    public boolean matchesTalent(TalentPostEntity talent, MatchCondition condition) {
        if (talent.getStatus() != TalentPostStatus.ACTIVE) {
            return false;
        }
        if (!matchesCategory(talent.getCategory().getId(), condition.categoryId())) {
            return false;
        }
        if (!isWithinPrice(talent.getPrice(), condition.maxPrice())) {
            return false;
        }
        return isWithinDuration(talent, condition);
    }

    /** 열린 요청글 중 카테고리, 예산 범위, 마감일 조건을 만족하는지 확인한다. */
    public boolean matchesRequest(RequestPostEntity request, MatchCondition condition) {
        if (request.getStatus() != RequestPostStatus.OPEN) {
            return false;
        }
        if (!matchesCategory(request.getCategory().getId(), condition.categoryId())) {
            return false;
        }
        if (!hasValidBudgetRange(request)) {
            return false;
        }
        if (!overlapsBudgetRange(request, condition)) {
            return false;
        }
        return matchesDueDate(request.getDueDate(), condition);
    }

    private boolean matchesCategory(UUID actualCategoryId, UUID requestedCategoryId) {
        if (requestedCategoryId == null) {
            return true;
        }
        return requestedCategoryId.equals(actualCategoryId);
    }

    private boolean isWithinPrice(long price, Long maxPrice) {
        if (maxPrice == null) {
            return true;
        }
        return price <= maxPrice;
    }

    private boolean isWithinDuration(TalentPostEntity talent, MatchCondition condition) {
        if (condition.maxEstimatedDuration() == null) {
            return true;
        }

        long candidateDays = toDays(talent.getEstimatedDuration(), talent.getDurationUnit());
        long maximumDays = toDays(condition.maxEstimatedDuration(), condition.durationUnit());
        return candidateDays <= maximumDays;
    }

    private boolean hasValidBudgetRange(RequestPostEntity request) {
        return request.getBudgetMin() <= request.getBudgetMax();
    }

    private boolean overlapsBudgetRange(RequestPostEntity request, MatchCondition condition) {
        // 두 예산 구간이 조금이라도 겹치면 조건을 만족한 것으로 본다.
        if (condition.minBudget() != null) {
            if (request.getBudgetMax() < condition.minBudget()) {
                return false;
            }
        }

        if (condition.maxBudget() != null) {
            if (request.getBudgetMin() > condition.maxBudget()) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesDueDate(LocalDate dueDate, MatchCondition condition) {
        // 마감일이 없는 글은 사용자가 마감 범위를 지정하지 않았을 때만 허용한다.
        if (dueDate == null) {
            return !hasDueDateCondition(condition);
        }
        if (dueDate.isBefore(LocalDate.now())) {
            return false;
        }
        if (isBeforeRequestedDueDate(dueDate, condition.dueDateFrom())) {
            return false;
        }
        return !isAfterRequestedDueDate(dueDate, condition.dueDateTo());
    }

    private boolean hasDueDateCondition(MatchCondition condition) {
        if (condition.dueDateFrom() != null) {
            return true;
        }
        return condition.dueDateTo() != null;
    }

    private boolean isBeforeRequestedDueDate(LocalDate dueDate, LocalDate dueDateFrom) {
        if (dueDateFrom == null) {
            return false;
        }
        return dueDate.isBefore(dueDateFrom);
    }

    private boolean isAfterRequestedDueDate(LocalDate dueDate, LocalDate dueDateTo) {
        if (dueDateTo == null) {
            return false;
        }
        return dueDate.isAfter(dueDateTo);
    }

    private long toDays(int duration, DurationUnit unit) {
        // 서로 다른 기간 단위를 비교하기 위해 MVP에서는 일 단위로 통일한다.
        return switch (unit) {
            case DAY -> duration;
            case WEEK -> duration * 7L;
            case MONTH -> duration * 30L;
        };
    }
}
