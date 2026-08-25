package org.example.link.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.link.domain.user.dto.MyPageResponse;
import org.example.link.domain.user.entity.UserEntity;
import org.example.link.domain.wallet.entity.WalletEntity;
import org.example.link.domain.wallet.service.WalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {
    private final UserService userService;
    private final WalletService walletService;
    public MyPageResponse getMyPage(Long userId) {
        UserEntity user = userService.getUserEntity(userId);
        WalletEntity wallet = walletService.getWalletEntity(userId);
        return MyPageResponse.from(user, wallet);
    }
}
