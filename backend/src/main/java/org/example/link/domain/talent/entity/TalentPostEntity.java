package org.example.link.domain.talent.entity;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;
import org.example.link.common.entity.BaseEntity;
import org.example.link.domain.category.entity.CategoryEntity;
import org.example.link.domain.portfolio.entity.PortfolioEntity;
import org.example.link.domain.talent.util.DurationUnit;
import org.example.link.domain.talent.util.TalentPostStatus;
import org.example.link.domain.user.entity.UserEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "talent_posts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
public class TalentPostEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "talent_post_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @OneToMany(
            mappedBy = "talentPost",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private java.util.List<TalentPostFileEntity> files = new java.util.ArrayList<>();

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private Integer estimatedDuration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DurationUnit durationUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id")
    private PortfolioEntity portfolio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TalentPostStatus status;

    @Column(name = "ai_confidence")
    private BigDecimal aiConfidence;

    public void update(
            String title,
            String content,
            CategoryEntity category,
            Long price,
            Integer estimatedDuration,
            DurationUnit durationUnit,
            PortfolioEntity portfolio
    ) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.price = price;
        this.estimatedDuration = estimatedDuration;
        this.durationUnit = durationUnit;
        this.portfolio = portfolio;
    }

    public void inactiveStatus() {
        this.status = TalentPostStatus.INACTIVE;
    }
}
