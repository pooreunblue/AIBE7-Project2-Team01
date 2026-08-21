package org.example.link.domain.user.repository;

import org.example.link.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByLoginId(String loginId);
    boolean existsByNickname(String nickname);
}
