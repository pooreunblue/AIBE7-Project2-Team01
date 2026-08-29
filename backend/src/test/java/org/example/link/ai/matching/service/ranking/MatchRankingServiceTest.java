package org.example.link.ai.matching.service.ranking;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

class MatchRankingServiceTest {
    private final MatchRankingService matchRankingService = new MatchRankingService();

    @Test
    void usesOnlySemanticScoreWithoutAmountCondition() {
        MatchRankingService.MatchScore score =
                matchRankingService.scoreTalent(0.82, 300_000L, null);

        assertThat(score.matchScore()).isEqualTo(0.82);
        assertThat(score.amountScore()).isNull();
    }

    @Test
    void combinesSemanticAndTalentPriceFit() {
        MatchRankingService.MatchScore score =
                matchRankingService.scoreTalent(0.8, 250_000L, 500_000L);

        assertThat(score.amountScore()).isEqualTo(0.5);
        assertThat(score.matchScore()).isCloseTo(0.77, offset(0.000001));
    }

    @Test
    void combinesSemanticAndRequestBudgetOverlapWhenBothBoundsExist() {
        MatchRankingService.MatchScore score =
                matchRankingService.scoreRequest(0.8, 300_000L, 600_000L, 400_000L, 800_000L);

        assertThat(score.amountScore()).isEqualTo(0.5);
        assertThat(score.matchScore()).isCloseTo(0.77, offset(0.000001));
    }
}
