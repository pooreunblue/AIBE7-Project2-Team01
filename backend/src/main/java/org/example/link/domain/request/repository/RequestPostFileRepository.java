package org.example.link.domain.request.repository;

import org.example.link.domain.request.entity.RequestPostFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RequestPostFileRepository
        extends JpaRepository<RequestPostFileEntity, Long> {
    List<RequestPostFileEntity> findAllByRequestPostId(Long requestPostId);
    List<RequestPostFileEntity> findAllByRequestPostIdOrderByIdAsc(Long requestPostId);
    Optional<RequestPostFileEntity> findByRequestPostIdAndThumbnailTrue(Long requestPostId);
}
