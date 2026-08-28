package org.example.link.domain.portfolio.entity;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.link.common.entity.BaseEntity;
import org.example.link.domain.user.entity.UserEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "portfolios")
public class PortfolioEntity extends BaseEntity {
    @Id

    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "portfolio_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(
            mappedBy = "portfolio",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PortfolioFileEntity> files = new ArrayList<>();

    private PortfolioEntity(
            UserEntity user,
            String title,
            String description
    ) {
        this.user = user;
        this.title = title;
        this.description = description;
    }

    public static PortfolioEntity create(
            UserEntity user,
            String title,
            String description
    ) {
        return new PortfolioEntity(
                user,
                title,
                description
        );
    }

    public void update(
            String title,
            String description
    ) {
        this.title = title;
        this.description = description;
    }
}
