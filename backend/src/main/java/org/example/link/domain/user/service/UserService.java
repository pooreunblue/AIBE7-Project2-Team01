package org.example.link.domain.user.service;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;
import org.example.link.common.storage.dto.StoredFile;
import org.example.link.common.storage.service.StorageService;
import org.example.link.common.storage.service.SupabaseStorageService;
import org.example.link.common.storage.type.FileType;
import org.example.link.domain.user.dto.MyPageResponse;
import org.example.link.domain.user.dto.SignupRequest;
import org.example.link.domain.user.dto.SignupResponse;
import org.example.link.domain.user.dto.UpdateUserRequest;
import org.example.link.domain.user.entity.UserEntity;
import org.example.link.domain.user.repository.UserRepository;
import org.example.link.domain.wallet.entity.WalletEntity;
import org.example.link.domain.wallet.repository.WalletRepository;
import org.example.link.domain.wallet.service.WalletService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRegistrationService userRegistrationService;
    private final WalletService walletService;
    private final StorageService storageService;

    public UserEntity getUserEntity(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.USER_NOT_FOUND)
                );
    }

    // 회원가입
    @Transactional
    public SignupResponse signUp(
            SignupRequest request,
            MultipartFile profileImage
    ) {
        if (userRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (userRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        String encodedPassword =
                passwordEncoder.encode(request.password());

        UserEntity savedUser =
                userRegistrationService.registerLocal(
                        request.email(),
                        encodedPassword,
                        request.nickname()
                );

        if (profileImage != null && !profileImage.isEmpty()) {
            StoredFile storedFile =
                    storageService.upload(
                            profileImage,
                            "profiles/" + savedUser.getId(),
                            FileType.IMAGE
                    );
            savedUser.updateProfileImage(
                    storedFile.url(),
                    storedFile.path()
            );
        }

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

    @Transactional
    public MyPageResponse updateProfileImage(
            UUID userId,
            MultipartFile file
    ) {
        UserEntity user = getUserEntity(userId);

        if (user.getProfileImagePath() != null) {
            storageService.delete(
                    user.getProfileImagePath()
            );
        }

        StoredFile storedFile =
                storageService.upload(
                        file,
                        "profiles/" + userId,
                        FileType.IMAGE
                );

        user.updateProfileImage(
                storedFile.url(),
                storedFile.path()
        );

        WalletEntity wallet =
                walletService.getWalletEntity(userId);

        return MyPageResponse.from(user, wallet);
    }
}
