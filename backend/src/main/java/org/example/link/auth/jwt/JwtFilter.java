package org.example.link.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.link.auth.security.CustomUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null) {
            try {
                // JWT 파싱은 한 번만
                Claims claims = jwtProvider.parseClaims(token);

                // Access Token만 인증에 사용
                if (jwtProvider.isAccessToken(claims)) {

                    Long userId = jwtProvider.getUserId(claims);
                    String email = jwtProvider.getEmail(claims);

                    CustomUserDetails principal =
                            new CustomUserDetails(userId, email);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    principal,
                                    null,
                                    principal.getAuthorities()
                            );

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);
                }

            } catch (JwtException | IllegalArgumentException e) {
                // 잘못되거나 만료된 JWT
                // Authentication을 생성하지 않고 다음 필터로 넘김
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {

        String authorization =
                request.getHeader("Authorization");

        if (authorization == null ||
                !authorization.startsWith("Bearer ")) {
            return null;
        }

        return authorization.substring(7);
    }
}