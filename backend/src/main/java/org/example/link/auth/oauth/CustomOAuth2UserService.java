package org.example.link.auth.oauth;

import lombok.RequiredArgsConstructor;
import org.example.link.domain.user.entity.UserEntity;
import org.example.link.domain.user.repository.UserRepository;
import org.example.link.domain.user.service.UserRegistrationService;
import org.example.link.domain.user.service.UserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService
        implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final UserRepository userRepository;
    private final UserRegistrationService userRegistrationService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {

        OAuth2User oAuth2User =
                new DefaultOAuth2UserService().loadUser(request);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");


        UserEntity user = userRepository.findByEmail(email)
                .orElseGet(() ->
                    userRegistrationService.registerSocial(
                            email,
                            name
                    )
                );
        return new CustomOAuth2User(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                oAuth2User.getAttributes()
        );
    }
}
