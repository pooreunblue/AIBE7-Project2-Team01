package org.example.link.domain.request.repository;

import org.example.link.domain.request.entity.RequestPostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RequestPostRepository extends JpaRepository<RequestPostEntity, Long> {
    @Query("""
    SELECT r
    FROM RequestPostEntity r
    WHERE (:keyword IS NULL OR
           LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR
           LOWER(r.content) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """)
    Page<RequestPostEntity> search(
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
