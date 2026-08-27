package org.example.link.domain.talent.repository;

import org.example.link.domain.talent.entity.TalentPostFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TalentPostFileRepository
        extends JpaRepository<TalentPostFileEntity, Long> {
    List<TalentPostFileEntity> findAllByTalentPostId(Long talentPostId);
    List<TalentPostFileEntity> findAllByTalentPostIdOrderByIdAsc(Long talentPostId);
    Optional<TalentPostFileEntity> findByTalentPostIdAndThumbnailTrue(Long talentPostId);
}
