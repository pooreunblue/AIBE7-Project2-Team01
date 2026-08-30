package org.example.link.ai.matching.service.ranking;

import org.springframework.stereotype.Service;

/**
 * 필수 조건을 통과한 후보의 노출 순서를 결정하는 점수를 계산한다.
 * 의미 유사도를 중심으로 두고, 금액 조건이 충분할 때만 금액 적합도를 보조 점수로 사용한다.
 */
@Service
public class MatchRankingService {
    private static final double SEMANTIC_WEIGHT_WITH_AMOUNT = 0.9;
    private static final double AMOUNT_WEIGHT = 0.1;

    /** 최대 가격이 있으면 저렴할수록 높은 가격 적합도를 부여한다. */
    public MatchScore scoreTalent(double semanticScore, long price, Long maxPrice) {
        if (maxPrice == null) {
            return semanticOnly(semanticScore);
        }

        double amountScore = calculateTalentPriceScore(price, maxPrice);
        return combine(semanticScore, amountScore);
    }

    /** 검색 예산의 최소/최대가 모두 있으면 게시글 예산과 겹치는 비율을 계산한다. */
    public MatchScore scoreRequest(
            double semanticScore,
            long budgetMin,
            long budgetMax,
            Long requestedMin,
            Long requestedMax
    ) {
        if (requestedMin == null || requestedMax == null) {
            return semanticOnly(semanticScore);
        }

        double amountScore = calculateBudgetOverlapScore(
                budgetMin,
                budgetMax,
                requestedMin,
                requestedMax
        );
        return combine(semanticScore, amountScore);
    }

    private MatchScore semanticOnly(double semanticScore) {
        return new MatchScore(clamp(semanticScore), null);
    }

    private double calculateTalentPriceScore(long price, long maxPrice) {
        if (maxPrice == 0) {
            if (price == 0) {
                return 1.0;
            }
            return 0.0;
        }

        double priceFit = (double) (maxPrice - price) / maxPrice;
        return clamp(priceFit);
    }

    private double calculateBudgetOverlapScore(
            long budgetMin,
            long budgetMax,
            long requestedMin,
            long requestedMax
    ) {
        long intersectionStart = Math.max(budgetMin, requestedMin);
        long intersectionEnd = Math.min(budgetMax, requestedMax);

        if (requestedMin == requestedMax) {
            if (intersectionStart <= intersectionEnd) {
                return 1.0;
            }
            return 0.0;
        }

        long intersection = Math.max(0L, intersectionEnd - intersectionStart);
        long requestedRange = requestedMax - requestedMin;
        return clamp((double) intersection / requestedRange);
    }

    private MatchScore combine(double semanticScore, double amountScore) {
        // 현재 MVP 가중치: 의미 유사도 90%, 금액 적합도 10%.
        double normalizedSemanticScore = clamp(semanticScore);
        double normalizedAmountScore = clamp(amountScore);
        double matchScore = normalizedSemanticScore * SEMANTIC_WEIGHT_WITH_AMOUNT
                + normalizedAmountScore * AMOUNT_WEIGHT;
        return new MatchScore(clamp(matchScore), normalizedAmountScore);
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

    /** matchScore는 최종 정렬 점수이고, amountScore는 금액 조건이 없으면 null이다. */
    public record MatchScore(double matchScore, Double amountScore) {
    }
}
