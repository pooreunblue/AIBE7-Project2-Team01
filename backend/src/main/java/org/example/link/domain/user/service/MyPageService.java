package org.example.link.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;
import org.example.link.domain.user.dto.MyPageResponse;
import org.example.link.domain.user.entity.UserEntity;
import org.example.link.domain.user.repository.UserRepository;
import org.example.link.domain.wallet.entity.WalletEntity;
import org.example.link.domain.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    public MyPageResponse getMyPage(Long userId) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.USER_NOT_FOUND)
                );
        WalletEntity wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.WALLET_NOT_FOUND)
                );
        return new MyPageResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
//                user.getProfileImageUrl(),
                wallet.getBalance(),
                user.getCreatedAt()
        );
    }
}
