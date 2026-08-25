package org.example.link.domain.request.repository;

import org.example.link.domain.request.entity.RequestPostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestPostRepository extends JpaRepository<RequestPostEntity, Long> {
    List<RequestPostEntity> findAllById(Long userId);
}
