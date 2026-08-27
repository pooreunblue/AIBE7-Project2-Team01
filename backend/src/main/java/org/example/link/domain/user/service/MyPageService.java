package org.example.link.domain.user.service;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
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

    public MyPageResponse getMyPage(UUID userId) {
        UserEntity user = userService.getUserEntity(userId);
        WalletEntity wallet = walletService.getWalletEntity(userId);
        return MyPageResponse.from(user, wallet);
    }

    //닉네임 수정
    @Transactional
    public MyPageResponse updateUser(
            UUID userId,
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

    //회원 탈퇴
    @Transactional
    public void deleteUser(UUID userId) {
        // TODO: 소프트 삭제 적용 예정
    }
}
