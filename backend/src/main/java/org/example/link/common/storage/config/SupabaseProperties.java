package org.example.link.common.storage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "supabase")
public record SupabaseProperties(
        String url,
        String serviceKey,
        String bucket
) {
}
