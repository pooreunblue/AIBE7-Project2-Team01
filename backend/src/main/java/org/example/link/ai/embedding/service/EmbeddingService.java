package org.example.link.ai.embedding.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.link.ai.embedding.enums.EmbeddingTargetType;
import org.example.link.ai.embedding.event.EmbeddingDocumentSnapshot;
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

    public void save(EmbeddingDocumentSnapshot snapshot) {
        saveDocument(buildDocument(snapshot));
    }

    public void replace(EmbeddingDocumentSnapshot snapshot) {
        replaceDocument(buildDocument(snapshot));
    }

    public void delete(EmbeddingTargetType targetType, java.util.UUID targetId) {
        deleteDocument(documentId(targetType, targetId));
    }

    private Document buildDocument(EmbeddingDocumentSnapshot snapshot) {
        Map<String, Object> metadata = metadata(
                snapshot.targetType(),
                snapshot.targetId(),
                snapshot.userId(),
                snapshot.categoryId(),
                snapshot.status()
        );
        String text = "[TYPE] " + typeLabel(snapshot.targetType()) + "\n"
                + "[TITLE] " + snapshot.title() + "\n"
                + "[CATEGORY] " + snapshot.categoryName() + "\n"
                + "[DESCRIPTION] " + normalize(snapshot.content());
        return new Document(documentId(snapshot.targetType(), snapshot.targetId()), text, metadata);
    }

    private Map<String, Object> metadata(
            EmbeddingTargetType targetType,
            java.util.UUID targetId,
            java.util.UUID userId,
            java.util.UUID categoryId,
            String status
    ) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("targetType", targetType.name());
        metadata.put("targetId", targetId.toString());
        metadata.put("userId", userId.toString());
        metadata.put("categoryId", categoryId.toString());
        metadata.put("status", status);
        return metadata;
    }

    private void saveDocument(Document document) {
        try {
            vectorStore.add(List.of(document));
        } catch (Exception exception) {
            log.error("임베딩 저장에 실패했습니다. documentId={}", document.getId(), exception);
        }
    }

    private void replaceDocument(Document document) {
        try {
            vectorStore.delete(document.getId());
            vectorStore.add(List.of(document));
        } catch (Exception exception) {
            log.error("임베딩 갱신에 실패했습니다. documentId={}", document.getId(), exception);
        }
    }

    private void deleteDocument(String documentId) {
        try {
            vectorStore.delete(documentId);
        } catch (Exception exception) {
            log.error("임베딩 삭제에 실패했습니다. documentId={}", documentId, exception);
        }
    }

    private String documentId(EmbeddingTargetType targetType, java.util.UUID targetId) {
        return targetType.name() + ":" + targetId;
    }

    private String typeLabel(EmbeddingTargetType targetType) {
        return switch (targetType) {
            case TALENT -> "재능 제공";
            case REQUEST -> "재능 요청";
            case PORTFOLIO -> "포트폴리오";
        };
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
