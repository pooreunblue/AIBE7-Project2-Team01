package org.example.link.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.link.domain.user.entity.UserEntity;
import org.example.link.domain.user.repository.UserRepository;
import org.example.link.domain.wallet.entity.WalletEntity;
import org.example.link.domain.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    public UserEntity registerLocal(
            String email,
            String password,
            String nickname
    ) {
        UserEntity user =
                UserEntity.create(
                        email,
                        password,
                        nickname
                );

        UserEntity savedUser =
                userRepository.save(user);

        walletRepository.save(
                WalletEntity.create(savedUser)
        );

        return savedUser;
    }

    public UserEntity registerSocial(
            String email,
            String nickname
    ) {

        UserEntity user =
                UserEntity.createSocialUser(
                        email,
                        nickname
                );

        UserEntity savedUser =
                userRepository.save(user);

        walletRepository.save(
                WalletEntity.create(savedUser)
        );

        return savedUser;
    }
}
