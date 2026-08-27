package org.example.link.domain.request.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.link.common.entity.BaseEntity;
import org.example.link.domain.category.entity.CategoryEntity;
import org.example.link.domain.request.util.RequestPostStatus;
import org.example.link.domain.user.entity.UserEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "request_posts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
public class RequestPostEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_post_id")
    private Long id;

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
            mappedBy = "requestPost",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<RequestPostFileEntity> files = new ArrayList<>();

    @Column(name = "budget_min",nullable = false)
    private Long budgetMin;

    @Column(name = "budget_max",nullable = false)
    private Long budgetMax;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestPostStatus status;

    @Column(name = "ai_confidence")
    private BigDecimal aiConfidence;

    public void update(
            String title,
            String content,
            CategoryEntity category,
            Long budgetMin,
            Long budgetMax,
            LocalDate dueDate
    ) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.budgetMin = budgetMin;
        this.budgetMax = budgetMax;
        this.dueDate = dueDate;
    }

    public void closeStatus() {
        this.status = RequestPostStatus.CLOSED;
    }
}
