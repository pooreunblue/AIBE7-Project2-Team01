package org.example.link.ai.embedding.event;

import org.example.link.ai.embedding.enums.EmbeddingTargetType;
import org.example.link.domain.request.entity.RequestPostEntity;
import org.example.link.domain.talent.entity.TalentPostEntity;

import java.util.UUID;

/** 커밋 이후에도 영속성 컨텍스트 없이 임베딩 문서를 만들 수 있는 불변 데이터다. */
public record EmbeddingDocumentSnapshot(
        EmbeddingTargetType targetType,
        UUID targetId,
        UUID userId,
        UUID categoryId,
        String categoryName,
        String status,
        String title,
        String content
) {
    public static EmbeddingDocumentSnapshot from(TalentPostEntity post) {
        return new EmbeddingDocumentSnapshot(
                EmbeddingTargetType.TALENT,
                post.getId(),
                post.getUser().getId(),
                post.getCategory().getId(),
                post.getCategory().getName(),
                post.getStatus().name(),
                post.getTitle(),
                post.getContent()
        );
    }

    public static EmbeddingDocumentSnapshot from(RequestPostEntity post) {
        return new EmbeddingDocumentSnapshot(
                EmbeddingTargetType.REQUEST,
                post.getId(),
                post.getUser().getId(),
                post.getCategory().getId(),
                post.getCategory().getName(),
                post.getStatus().name(),
                post.getTitle(),
                post.getContent()
        );
    }
}
