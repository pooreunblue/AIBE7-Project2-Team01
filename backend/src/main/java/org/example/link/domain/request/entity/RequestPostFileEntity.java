package org.example.link.domain.request.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.link.common.entity.BaseEntity;

@Entity
@Table(name = "request_post_files")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
public class RequestPostFileEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_post_file_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_post_id", nullable = false)
    private RequestPostEntity requestPost;

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

    @Column(nullable = false)
    @Builder.Default
    private boolean thumbnail = false;

    private RequestPostFileEntity(
            RequestPostEntity requestPost,
            String originalFileName,
            String storagePath,
            String fileUrl,
            String contentType,
            Long fileSize
    ) {
        this.requestPost = requestPost;
        this.originalFileName = originalFileName;
        this.storagePath = storagePath;
        this.fileUrl = fileUrl;
        this.contentType = contentType;
        this.fileSize = fileSize;
    }

    public static RequestPostFileEntity create(
            RequestPostEntity requestPost,
            String originalFileName,
            String storagePath,
            String fileUrl,
            String contentType,
            Long fileSize
    ) {
        return new RequestPostFileEntity(
                requestPost,
                originalFileName,
                storagePath,
                fileUrl,
                contentType,
                fileSize
        );
    }

    public void updateFile(
            String originalFileName,
            String storagePath,
            String fileUrl,
            String contentType,
            Long fileSize
    ) {
        this.originalFileName = originalFileName;
        this.storagePath = storagePath;
        this.fileUrl = fileUrl;
        this.contentType = contentType;
        this.fileSize = fileSize;
    }

    public void setThumbnail() {
        this.thumbnail = true;
    }

    public void unsetThumbnail() {
        this.thumbnail = false;
    }

}
