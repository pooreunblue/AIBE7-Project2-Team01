package org.example.link.domain.user.entity;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.link.auth.config.AuthProvider;
import org.example.link.common.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class UserEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String password;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column
    private String profileImageUrl;

    @Column
    private String profileImagePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider = AuthProvider.LOCAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    public UserEntity(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.provider = AuthProvider.LOCAL;
        this.role = Role.USER;
    }

    public static UserEntity create(
            String email,
            String password,
            String nickname
    ) {
        return new UserEntity(
                email,
                password,
                nickname
        );
    }

    // TODO : 닉네임 중복 시 랜덤 suffix 추가
    public static UserEntity createSocialUser(
            String email,
            String nickname
    ) {
        UserEntity user = new UserEntity();
        user.email = email;
        user.password = null;
        user.nickname = nickname;
        user.provider = AuthProvider.GOOGLE;
        user.role = Role.USER;
        return user;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateProfileImage(
            String profileImageUrl,
            String profileImagePath
    ) {
        this.profileImageUrl = profileImageUrl;
        this.profileImagePath = profileImagePath;
    }

    public void deleteProfileImage() {
        this.profileImageUrl = null;
        this.profileImagePath = null;
    }
}

