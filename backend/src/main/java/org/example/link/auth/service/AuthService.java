package org.example.link.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.link.auth.dto.LoginRequest;
import org.example.link.auth.dto.LoginResponse;
import org.example.link.auth.dto.RefreshRequest;
import org.example.link.auth.dto.RefreshResponse;
import org.example.link.auth.jwt.JwtProvider;
import org.example.link.common.exception.CustomException;
import org.example.link.common.exception.ErrorCode;
import org.example.link.domain.user.entity.UserEntity;
import org.example.link.domain.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    //로그인
    @Transactional
    public LoginResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new CustomException(ErrorCode.INVALID_CREDENTIALS)
                );
        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword()
        )) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }
        String accessToken = jwtProvider.createAccessToken(
                user.getId(),
                user.getEmail()
        );

        String refreshToken = jwtProvider.createRefreshToken(user.getId());
        refreshTokenService.save(user.getId(), refreshToken);

        return new LoginResponse(accessToken, refreshToken);
    }

    //logout
    public void logout(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.USER_NOT_FOUND)
                );
        refreshTokenService.delete(user.getId());
    }

    //refresh
    public RefreshResponse refresh(RefreshRequest request) {

        String refreshToken = request.refreshToken();

        if (!jwtProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        Long userId = jwtProvider.getUserId(refreshToken);
        String savedRefreshToken = refreshTokenService.get(userId);

        if(savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.USER_NOT_FOUND)
                );

        String newAccessToken = jwtProvider.createAccessToken(
                user.getId(),
                user.getEmail()
        );

        return new RefreshResponse(newAccessToken);
    }
}
