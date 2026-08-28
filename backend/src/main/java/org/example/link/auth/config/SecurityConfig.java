package org.example.link.auth.config;

import lombok.RequiredArgsConstructor;
import org.example.link.auth.jwt.JwtFilter;
import org.example.link.auth.oauth.CustomOAuth2UserService;
import org.example.link.auth.oauth.OAuth2SuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class
SecurityConfig {
    private final JwtFilter jwtFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                // JWT 방식이면 세션 사용 안 함
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .csrf(AbstractHttpConfigurer::disable)
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/categories",
                                "/users/signup",
                                "/auth/login",
                                "/auth/refresh",
                                "/health",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/ws/**",
                                "/chat-test.html",
                                "/test/**"
                        ).permitAll()
                                .requestMatchers(HttpMethod.GET, "/requests/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/talents/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "/requests").authenticated()
                                .requestMatchers(HttpMethod.POST, "/talents").authenticated()
                                .requestMatchers(HttpMethod.PUT, "/requests/**").authenticated()
                                .requestMatchers(HttpMethod.PUT, "/talents/**").authenticated()
                                .requestMatchers(HttpMethod.DELETE, "/requests/**").authenticated()
                                .requestMatchers(HttpMethod.DELETE, "/talents/**").authenticated()
                                .anyRequest().authenticated()
                        )
                        .exceptionHandling(exception -> exception
                                .authenticationEntryPoint((request, response, authException) ->
                                        response.sendError(
                                                jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED
                                        )
                                )
                        )
                        // JWT Filter 등록
                        .addFilterBefore(
                                jwtFilter,
                                UsernamePasswordAuthenticationFilter.class
                        );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
