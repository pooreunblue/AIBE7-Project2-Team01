package org.example.link.ai.embedding.event;

import lombok.RequiredArgsConstructor;
import org.example.link.ai.embedding.enums.EmbeddingTargetType;
import org.example.link.domain.request.entity.RequestPostEntity;
import org.example.link.domain.talent.entity.TalentPostEntity;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** 도메인 서비스가 VectorStore 구현을 몰라도 되도록 임베딩 이벤트만 발행한다. */
@Component
@RequiredArgsConstructor
public class EmbeddingEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void saveTalent(TalentPostEntity post) {
        applicationEventPublisher.publishEvent(new EmbeddingEvent.Save(EmbeddingDocumentSnapshot.from(post)));
    }

    public void replaceTalent(TalentPostEntity post) {
        applicationEventPublisher.publishEvent(new EmbeddingEvent.Replace(EmbeddingDocumentSnapshot.from(post)));
    }

    public void deleteTalent(UUID postId) {
        applicationEventPublisher.publishEvent(new EmbeddingEvent.Delete(EmbeddingTargetType.TALENT, postId));
    }

    public void saveRequest(RequestPostEntity post) {
        applicationEventPublisher.publishEvent(new EmbeddingEvent.Save(EmbeddingDocumentSnapshot.from(post)));
    }

    public void replaceRequest(RequestPostEntity post) {
        applicationEventPublisher.publishEvent(new EmbeddingEvent.Replace(EmbeddingDocumentSnapshot.from(post)));
    }

    public void deleteRequest(UUID postId) {
        applicationEventPublisher.publishEvent(new EmbeddingEvent.Delete(EmbeddingTargetType.REQUEST, postId));
    }
}
