package org.example.link.ai.embedding.event;

import org.example.link.ai.embedding.enums.EmbeddingTargetType;

import java.util.UUID;

/** 트랜잭션 커밋 후 실행할 VectorStore 작업을 구분한다. */
public sealed interface EmbeddingEvent {

    record Save(EmbeddingDocumentSnapshot document) implements EmbeddingEvent {
    }

    record Replace(EmbeddingDocumentSnapshot document) implements EmbeddingEvent {
    }

    record Delete(EmbeddingTargetType targetType, UUID targetId) implements EmbeddingEvent {
    }
}
