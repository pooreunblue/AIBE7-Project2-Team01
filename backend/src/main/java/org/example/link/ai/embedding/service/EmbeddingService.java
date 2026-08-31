package org.example.link.ai.embedding.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.link.ai.embedding.enums.EmbeddingTargetType;
import org.example.link.domain.category.entity.CategoryEntity;
import org.example.link.domain.request.entity.RequestPostEntity;
import org.example.link.domain.talent.entity.TalentPostEntity;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final VectorStore vectorStore;

    public void upsertTalent(TalentPostEntity post) {
        save(buildTalentDocument(post));
    }

    public void upsertRequest(RequestPostEntity post) {
        save(buildRequestDocument(post));
    }

    public void replaceTalent(TalentPostEntity post) {
        replace(buildTalentDocument(post));
    }

    public void replaceRequest(RequestPostEntity post) {
        replace(buildRequestDocument(post));
    }

    public void deleteTalent(java.util.UUID postId) {
        delete(documentId(EmbeddingTargetType.TALENT, postId));
    }

    public void deleteRequest(java.util.UUID postId) {
        delete(documentId(EmbeddingTargetType.REQUEST, postId));
    }

    private Document buildTalentDocument(TalentPostEntity post) {
        Map<String, Object> metadata = metadata(
                EmbeddingTargetType.TALENT,
                post.getId(),
                post.getUser().getId(),
                post.getCategory(),
                post.getStatus().name()
        );
        String text = "[TYPE] 재능 제공\n"
                + "[TITLE] " + post.getTitle() + "\n"
                + "[CATEGORY] " + post.getCategory().getName() + "\n"
                + "[DESCRIPTION] " + normalize(post.getContent());
        return new Document(documentId(EmbeddingTargetType.TALENT, post.getId()), text, metadata);
    }

    private Document buildRequestDocument(RequestPostEntity post) {
        Map<String, Object> metadata = metadata(
                EmbeddingTargetType.REQUEST,
                post.getId(),
                post.getUser().getId(),
                post.getCategory(),
                post.getStatus().name()
        );
        String text = "[TYPE] 재능 요청\n"
                + "[TITLE] " + post.getTitle() + "\n"
                + "[CATEGORY] " + post.getCategory().getName() + "\n"
                + "[DESCRIPTION] " + normalize(post.getContent());
        return new Document(documentId(EmbeddingTargetType.REQUEST, post.getId()), text, metadata);
    }

    private Map<String, Object> metadata(
            EmbeddingTargetType targetType,
            java.util.UUID targetId,
            java.util.UUID userId,
            CategoryEntity category,
            String status
    ) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("targetType", targetType.name());
        metadata.put("targetId", targetId.toString());
        metadata.put("userId", userId.toString());
        metadata.put("categoryId", category.getId().toString());
        metadata.put("status", status);
        return metadata;
    }

    private void save(Document document) {
        try {
            vectorStore.add(List.of(document));
        } catch (Exception exception) {
            log.error("임베딩 저장에 실패했습니다. documentId={}", document.getId(), exception);
        }
    }

    private void replace(Document document) {
        try {
            vectorStore.delete(document.getId());
            vectorStore.add(List.of(document));
        } catch (Exception exception) {
            log.error("임베딩 갱신에 실패했습니다. documentId={}", document.getId(), exception);
        }
    }

    private void delete(String documentId) {
        try {
            vectorStore.delete(documentId);
        } catch (Exception exception) {
            log.error("임베딩 삭제에 실패했습니다. documentId={}", documentId, exception);
        }
    }

    private String documentId(EmbeddingTargetType targetType, java.util.UUID targetId) {
        return targetType.name() + ":" + targetId;
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replaceAll("!\\[[^]]*]\\([^)]*\\)", "")
                .replaceAll("https?://\\S+", "")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
