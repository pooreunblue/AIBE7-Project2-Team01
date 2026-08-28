package org.example.link.auth.security;

import java.util.UUID;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.link.domain.user.entity.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final UUID userId;
    private final String email;
    private final Role role;

    @Override
    public String getUsername() {
        return email;
    }
    @Override
    public String getPassword() {
        return "";
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + role.name())
        );
    }
}
