package org.example.link.domain.request.entity;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;
import org.example.link.common.entity.BaseEntity;
import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;
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
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "request_post_id")
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
        requireStatus(RequestPostStatus.OPEN);
        this.status = RequestPostStatus.CLOSED;
    }

    /** 결제가 완료된 요청글을 다른 거래가 선택하지 못하도록 진행 중 상태로 바꾼다. */
    public void startTrade() {
        requireStatus(RequestPostStatus.OPEN);
        this.status = RequestPostStatus.IN_PROGRESS;
    }

    /** 거래가 정상적으로 끝난 1회성 요청글을 최종 마감한다. */
    public void completeTrade() {
        requireStatus(RequestPostStatus.IN_PROGRESS);
        this.status = RequestPostStatus.CLOSED;
    }

    /** 결제된 거래가 취소됐을 때 요청글을 다시 거래 가능한 상태로 되돌린다. */
    public void reopenAfterTradeCancellation() {
        requireStatus(RequestPostStatus.IN_PROGRESS);
        this.status = RequestPostStatus.OPEN;
    }

    /** 거래가 시작되지 않은 요청글을 작성자가 취소한다. */
    public void cancelStatus() {
        requireStatus(RequestPostStatus.OPEN);
        this.status = RequestPostStatus.CANCELLED;
    }

    private void requireStatus(RequestPostStatus expectedStatus) {
        if (this.status != expectedStatus) {
            throw new CustomException(ErrorCode.INVALID_REQUEST_POST_STATUS);
        }
    }
}
