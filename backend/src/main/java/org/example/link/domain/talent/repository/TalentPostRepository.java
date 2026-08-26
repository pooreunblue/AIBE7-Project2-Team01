package org.example.link.domain.talent.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.example.link.domain.talent.entity.TalentPostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TalentPostRepository extends JpaRepository<TalentPostEntity, Long> {
    @Query("""
    SELECT t
    FROM TalentPostEntity t
    WHERE (:keyword IS NULL OR
           LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR
           LOWER(t.content) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """)
    Page<TalentPostEntity> search(
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
