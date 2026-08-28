package org.example.link.domain.talent.entity;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.link.common.entity.BaseEntity;

@Entity
@Table(name = "talent_post_files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TalentPostFileEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "talent_post_file_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "talent_post_id", nullable = false)
    private TalentPostEntity talentPost;

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
    private boolean thumbnail = false;

    private TalentPostFileEntity(
            TalentPostEntity talentPost,
            String originalFileName,
            String storagePath,
            String fileUrl,
            String contentType,
            Long fileSize
    ) {
        this.talentPost = talentPost;
        this.originalFileName = originalFileName;
        this.storagePath = storagePath;
        this.fileUrl = fileUrl;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.thumbnail = false;
    }

    public static TalentPostFileEntity create(
            TalentPostEntity talentPost,
            String originalFileName,
            String storagePath,
            String fileUrl,
            String contentType,
            Long fileSize
    ) {
        return new TalentPostFileEntity(
                talentPost,
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
