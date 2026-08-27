package org.example.link.domain.request.repository;

import org.example.link.domain.request.entity.RequestPostFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestPostFileRepository
        extends JpaRepository<RequestPostFileEntity, Long> {
    List<RequestPostFileEntity> findAllByRequestPostId(Long requestPostId);
}
