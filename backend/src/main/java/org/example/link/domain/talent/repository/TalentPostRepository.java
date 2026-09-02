package org.example.link.domain.talent.repository;

import org.example.link.domain.talent.entity.TalentPostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface TalentPostRepository extends JpaRepository<TalentPostEntity, UUID> {
    /** 목록 응답에 필요한 연관 엔티티를 한 번에 조회한다. */
    @Override
    @EntityGraph(attributePaths = {"user", "category", "portfolio"})
    Page<TalentPostEntity> findAll(Pageable pageable);

    @Query("""
            SELECT t FROM TalentPostEntity t
            WHERE t.status = org.example.link.domain.talent.util.TalentPostStatus.ACTIVE
              AND (:categoryId IS NULL OR t.category.id = :categoryId)
              AND (:maxPrice IS NULL OR t.price <= :maxPrice)
              AND (:maxEstimatedDurationDays IS NULL OR
                   CASE
                       WHEN t.durationUnit = org.example.link.domain.talent.util.DurationUnit.DAY THEN t.estimatedDuration
                       WHEN t.durationUnit = org.example.link.domain.talent.util.DurationUnit.WEEK THEN t.estimatedDuration * 7
                       ELSE t.estimatedDuration * 30
                   END <= :maxEstimatedDurationDays)
            """)
    @EntityGraph(attributePaths = {"user", "category", "portfolio"})
    Page<TalentPostEntity> findAllByFilters(
            @Param("categoryId") UUID categoryId,
            @Param("maxPrice") Long maxPrice,
            @Param("maxEstimatedDurationDays") Integer maxEstimatedDurationDays,
            Pageable pageable
    );

    /** 벡터 검색 후보를 일괄 조회하고 응답에 필요한 작성자와 카테고리도 함께 로딩한다. */
    @EntityGraph(attributePaths = {"user", "category"})
    List<TalentPostEntity> findByIdIn(Collection<UUID> ids);

    @Query("""
    SELECT t
    FROM TalentPostEntity t
    WHERE t.status = org.example.link.domain.talent.util.TalentPostStatus.ACTIVE
      AND (:keyword IS NULL OR
           LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR
           LOWER(t.content) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:categoryId IS NULL OR t.category.id = :categoryId)
      AND (:maxPrice IS NULL OR t.price <= :maxPrice)
      AND (:maxEstimatedDurationDays IS NULL OR
           CASE
               WHEN t.durationUnit = org.example.link.domain.talent.util.DurationUnit.DAY THEN t.estimatedDuration
               WHEN t.durationUnit = org.example.link.domain.talent.util.DurationUnit.WEEK THEN t.estimatedDuration * 7
               ELSE t.estimatedDuration * 30
           END <= :maxEstimatedDurationDays)
    """)
    @EntityGraph(attributePaths = {"user", "category", "portfolio"})
    Page<TalentPostEntity> search(
            @Param("keyword") String keyword,
            @Param("categoryId") UUID categoryId,
            @Param("maxPrice") Long maxPrice,
            @Param("maxEstimatedDurationDays") Integer maxEstimatedDurationDays,
            Pageable pageable
    );
}
