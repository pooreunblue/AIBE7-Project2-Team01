package org.example.link.auth.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.link.auth.jwt.JwtProvider;
import org.example.link.domain.user.entity.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler
        implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    // private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        CustomOAuth2User principal =
                (CustomOAuth2User) authentication.getPrincipal();

        Long userId = principal.getUserId();
        String email = principal.getEmail();
        Role role = principal.getRole();

        String accessToken =
                jwtProvider.createAccessToken(
                        userId,
                        email,
                        role
                );

        String refreshToken =
                jwtProvider.createRefreshToken(userId);

        // 기존 RefreshToken Redis 저장 로직 연결
        // refreshTokenService.save(userId, refreshToken);

        response.sendRedirect(
                "http://localhost:3000/oauth2/success"
                        + "?accessToken=" + accessToken
                        + "&refreshToken=" + refreshToken
        );
    }
}