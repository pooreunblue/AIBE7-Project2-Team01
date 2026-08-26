package org.example.link.domain.portfolio.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.link.common.entity.BaseEntity;

@Entity
@Table(name = "portfolio_files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PortfolioFileEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_file_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private PortfolioEntity portfolio;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String storagePath;

    @Column(nullable = false)
    private String fileUrl;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long fileSize;

    private PortfolioFileEntity(
            PortfolioEntity portfolio,
            String originalFileName,
            String storagePath,
            String fileUrl,
            String contentType,
            Long fileSize
    ) {
        this.portfolio = portfolio;
        this.originalFileName = originalFileName;
        this.storagePath = storagePath;
        this.fileUrl = fileUrl;
        this.contentType = contentType;
        this.fileSize = fileSize;
    }

    public static PortfolioFileEntity create(
            PortfolioEntity portfolio,
            String originalFileName,
            String storagePath,
            String fileUrl,
            String contentType,
            Long fileSize
    ) {
        return new PortfolioFileEntity(
                portfolio,
                originalFileName,
                storagePath,
                fileUrl,
                contentType,
                fileSize
        );
    }
}