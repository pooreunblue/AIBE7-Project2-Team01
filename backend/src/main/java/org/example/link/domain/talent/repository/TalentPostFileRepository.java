package org.example.link.domain.talent.repository;

import java.util.UUID;

import org.example.link.domain.talent.entity.TalentPostFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TalentPostFileRepository
        extends JpaRepository<TalentPostFileEntity, UUID> {
    List<TalentPostFileEntity> findAllByTalentPostId(UUID talentPostId);
    List<TalentPostFileEntity> findAllByTalentPostIdOrderByIdAsc(UUID talentPostId);
    Optional<TalentPostFileEntity> findByTalentPostIdAndThumbnailTrue(UUID talentPostId);

    @EntityGraph(attributePaths = "talentPost")
    List<TalentPostFileEntity> findAllByTalentPostIdInAndThumbnailTrue(Collection<UUID> talentPostIds);
}
