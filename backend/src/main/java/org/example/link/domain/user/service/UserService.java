package org.example.link.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.link.auth.jwt.JwtProvider;
import org.example.link.auth.service.RefreshTokenService;
import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;
import org.example.link.auth.dto.LoginRequest;
import org.example.link.auth.dto.LoginResponse;
import org.example.link.domain.user.dto.SignupRequest;
import org.example.link.domain.user.dto.SignupResponse;
import org.example.link.domain.user.entity.UserEntity;
import org.example.link.domain.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    public SignupResponse signUp(SignupRequest request) {

        if (userRepository.existsByLoginId(request.loginId())) {
            throw new CustomException(ErrorCode.DUPLICATE_LOGIN_ID);
        }

        if (userRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        String encodedPassword =
                passwordEncoder.encode(request.password());
        UserEntity user = new UserEntity(
                request.loginId(),
                encodedPassword,
                request.nickname()
        );
        UserEntity savedUser = userRepository.save(user);
        return new SignupResponse(
                savedUser.getId(),
                savedUser.getLoginId(),
                savedUser.getNickname()
        );
    }
}
