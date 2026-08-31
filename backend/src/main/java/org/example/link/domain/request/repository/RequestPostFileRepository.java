package org.example.link.domain.request.repository;

import java.util.UUID;

import org.example.link.domain.request.entity.RequestPostFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RequestPostFileRepository
        extends JpaRepository<RequestPostFileEntity, UUID> {
    List<RequestPostFileEntity> findAllByRequestPostId(UUID requestPostId);
    List<RequestPostFileEntity> findAllByRequestPostIdOrderByIdAsc(UUID requestPostId);
    Optional<RequestPostFileEntity> findByRequestPostIdAndThumbnailTrue(UUID requestPostId);

    @EntityGraph(attributePaths = "requestPost")
    List<RequestPostFileEntity> findAllByRequestPostIdInAndThumbnailTrue(Collection<UUID> requestPostIds);
}
