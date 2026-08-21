package org.example.link.auth.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.example.link.auth.config.AuthProperties;
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
            String loginId
    ) {
        Instant now = Instant.now();
        Instant expiry =
                now.plus(authProperties.jwt().accessTokenExpiry());
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("loginId", loginId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey())
                .compact();
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
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    //userId 추출
    public Long getUserId(String token) {
        return Long.valueOf(
                getClaims(token).getSubject()
        );
    }

    //loginId 추출
    public String getLoginId(String token) {
        return getClaims(token)
                .get("loginId", String.class);
    }

}
