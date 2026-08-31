package org.example.link.ai.embedding.event;

import lombok.RequiredArgsConstructor;
import org.example.link.ai.embedding.service.EmbeddingService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** 원본 데이터 커밋이 성공한 경우에만 VectorStore 작업을 비동기로 실행한다. */
@Component
@RequiredArgsConstructor
public class EmbeddingEventListener {

    private final EmbeddingService embeddingService;

    @Async("embeddingTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void save(EmbeddingEvent.Save event) {
        embeddingService.save(event.document());
    }

    @Async("embeddingTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void replace(EmbeddingEvent.Replace event) {
        embeddingService.replace(event.document());
    }

    @Async("embeddingTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void delete(EmbeddingEvent.Delete event) {
        embeddingService.delete(event.targetType(), event.targetId());
    }
}
