package org.example.link.common.storage.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(SupabaseProperties.class)
public class SupabaseConfig {
    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .build();
    }
}