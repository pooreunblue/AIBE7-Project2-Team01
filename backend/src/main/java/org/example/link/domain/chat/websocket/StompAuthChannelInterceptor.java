package org.example.link.domain.chat.websocket;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.example.link.auth.jwt.JwtProvider;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtProvider jwtProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = resolveToken(accessor);
            if (token == null) {
                throw new MessagingException("인증 토큰이 존재하지 않습니다.");
            }
            try {
                Claims claims = jwtProvider.parseClaims(token);
                if (!jwtProvider.isAccessToken(claims)) {
                    throw new MessagingException(
                            "Access Token이 아닙니다."
                    );
                }
                Long userId = jwtProvider.getUserId(claims);
                String email = jwtProvider.getEmail(claims);
                accessor.setUser(
                        new StompPrincipal(userId, email)
                );
            } catch (JwtException | IllegalArgumentException e) {

                throw new MessagingException(

                        "유효하지 않은 인증 토큰입니다."
                );
            }
        }
            return message;
        }



    private String resolveToken(StompHeaderAccessor accessor) {
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders == null || authHeaders.isEmpty()) {
            return null;
        }
        String header = authHeaders.get(0);
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring(7);
    }
        private record StompPrincipal(
                Long userId,
                String email
        ) implements Principal {
            @Override
            public String getName() {
                return email;
            }
        }
}
