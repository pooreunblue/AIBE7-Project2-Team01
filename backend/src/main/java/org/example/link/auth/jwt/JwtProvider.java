package org.example.link.auth.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.example.link.auth.config.AuthProperties;
import org.example.link.domain.user.entity.Role;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtProvider {
    private final AuthProperties authProperties;

    // Secret Key 생성
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(
                authProperties.jwt().secret())
        );
    }

    //AccessToken 생성
    public String createAccessToken(
            Long userId,
            String email,
            Role role
    ) {
        Instant now = Instant.now();
        Instant expiry =
                now.plus(authProperties.jwt().accessTokenExpiry());
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("type", "ACCESS")
                .claim("role", role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey())
                .compact();
    }

    public boolean isAccessToken(Claims claims) {
        return "ACCESS".equals(
                claims.get("type", String.class)
        );
    }

    //RefreshToken 생성
    public String createRefreshToken(Long userId) {
        Instant now = Instant.now();
        Instant expiry =
                now.plus(authProperties.jwt().refreshTokenExpiry());

        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", "REFRESH")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey())
                .compact();
    }

    //RefreshToken 검증
    public boolean isRefreshToken(Claims claims) {
        return "REFRESH".equals(
                claims.get("type", String.class)
        );
    }

    //토큰 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //Claims 가져오기
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    //userId 추출
    public Long getUserId(Claims claims) {
        return Long.valueOf(claims.getSubject());
    }

    //email 추출
    public String getEmail(Claims claims) {
        return claims.get("email", String.class);
    }

    //Role 추출
    public Role getRole(Claims claims) {return Role.valueOf(claims.get("role", String.class));}

}
