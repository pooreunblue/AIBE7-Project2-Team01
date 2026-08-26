package org.example.link.auth.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.link.auth.jwt.JwtProvider;
import org.example.link.auth.service.RefreshTokenService;
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
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        CustomOAuth2User principal =
                (CustomOAuth2User) authentication.getPrincipal();

        String accessToken = jwtProvider.createAccessToken(
                principal.getUserId(),
                principal.getEmail(),
                principal.getRole()
        );

        String refreshToken =
                jwtProvider.createRefreshToken(principal.getUserId());

        refreshTokenService.save(
                principal.getUserId(),
                refreshToken
        );

        response.sendRedirect(
                "http://localhost:3000/oauth2/success"
                        + "?accessToken=" + accessToken
                        + "&refreshToken=" + refreshToken
        );
    }
}