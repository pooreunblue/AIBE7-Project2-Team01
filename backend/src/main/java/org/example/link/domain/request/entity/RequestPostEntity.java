package org.example.link.domain.request.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.link.common.entity.BaseEntity;
import org.example.link.domain.category.entity.CategoryEntity;
import org.example.link.domain.user.entity.UserEntity;

import java.math.BigDecimal;

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
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private Long budget_max;

    @Column(nullable = false)
    private Long budget_min;

    private String status;
    private BigDecimal ai_confidence;
}
