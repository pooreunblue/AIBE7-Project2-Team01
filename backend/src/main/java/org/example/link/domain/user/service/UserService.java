package org.example.link.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;
import org.example.link.domain.user.dto.MyPageResponse;
import org.example.link.domain.user.dto.SignupRequest;
import org.example.link.domain.user.dto.SignupResponse;
import org.example.link.domain.user.dto.UpdateUserRequest;
import org.example.link.domain.user.entity.UserEntity;
import org.example.link.domain.user.repository.UserRepository;
import org.example.link.domain.wallet.entity.WalletEntity;
import org.example.link.domain.wallet.repository.WalletRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletRepository walletRepository;

    public UserEntity getUserEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.USER_NOT_FOUND)
                );
    }

    // 회원가입
    public SignupResponse signUp(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (userRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        String encodedPassword =
                passwordEncoder.encode(request.password());
        UserEntity user = new UserEntity(
                request.email(),
                encodedPassword,
                request.nickname()
        );
        UserEntity savedUser = userRepository.save(user);

        WalletEntity savedWallet = new WalletEntity(savedUser);
        walletRepository.save(savedWallet);

        return new SignupResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getNickname()
        );
    }

    //닉네임 중복 검사
    public void validateNickname(
            UserEntity user,
            String nickname
    ) {
        if (userRepository.existsByNickname(nickname)
                && !user.getNickname().equals(nickname)) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }
    }
}
