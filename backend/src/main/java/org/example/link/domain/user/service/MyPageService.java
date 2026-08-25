package org.example.link.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;
import org.example.link.domain.user.dto.MyPageResponse;
import org.example.link.domain.user.dto.UpdateUserRequest;
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

    //닉네임 수정
    @Transactional
    public MyPageResponse updateUser(
            Long userId,
            UpdateUserRequest request
    ){
        UserEntity user = userService.getUserEntity(userId);
        userService.validateNickname(
                user,
                request.nickname()
        );
        user.updateNickname(request.nickname());
        WalletEntity wallet = walletService.getWalletEntity(userId);
        return MyPageResponse.from(user, wallet);
    }
}
