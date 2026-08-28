package org.example.link.auth.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.link.auth.cookie.CookieUtil;
import org.example.link.auth.jwt.JwtProvider;
import org.example.link.auth.service.RefreshTokenService;
import org.example.link.domain.user.entity.Role;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
    private final CookieUtil cookieUtil;

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

        ResponseCookie accessCookie = cookieUtil.createAccessTokenCookie(accessToken);
        ResponseCookie refreshCookie = cookieUtil.createRefreshTokenCookie(refreshToken);

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                accessCookie.toString()
        );
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshCookie.toString()
        );

        response.sendRedirect(
                "http://localhost:3000/oauth2/success"
        );
    }
}